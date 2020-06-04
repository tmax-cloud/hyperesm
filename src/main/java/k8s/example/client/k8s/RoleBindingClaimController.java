package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.RoleBindingClaim;
import k8s.example.client.models.StateCheckInfo;

public class RoleBindingClaimController extends Thread {
	private Watch<RoleBindingClaim> rbcController;
	private static long latestResourceVersion = 0;
	private CustomObjectsApi api = null;
	ApiClient client = null;
    private Logger logger = Main.logger;
	StateCheckInfo sci = new StateCheckInfo();

	RoleBindingClaimController(ApiClient client, CustomObjectsApi api, long resourceVersion) throws Exception {
		rbcController = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM, null, null, null, null, null, Long.toString( resourceVersion ), null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<RoleBindingClaim>>() {}.getType());
		this.api = api;
		this.client = client;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				sci.checkThreadState();
				rbcController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.info("Interrupted!");
							rbcController.close();
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
					
					
					// Logic here
					String claimName = "unknown";
					String resourceName = "unknown";
					String claimNamespace = "unknown";
					try {
						RoleBindingClaim claim = response.object;

						if( claim != null) {
							latestResourceVersion = Long.parseLong( response.object.getMetadata().getResourceVersion() );
							String eventType = response.type.toString(); //ADDED, MODIFIED, DELETED
							logger.info("[RoleBindingClaim Controller] Event Type : " + eventType );
							logger.info("[RoleBindingClaim Controller] == ResourceQuotaClaim == \n" + claim.toString());
							claimName = claim.getMetadata().getName();
							resourceName = claim.getResourceName();
							claimNamespace = claim.getMetadata().getNamespace();
							switch( eventType ) {
								case Constants.EVENT_TYPE_ADDED : 
									// Patch Status to Awaiting
									replaceRbcStatus( claimName, Constants.CLAIM_STATUS_AWAITING, "wait for admin permission", claimNamespace );
									break;
								case Constants.EVENT_TYPE_MODIFIED : 
									String status = getClaimStatus( claimName, claimNamespace );
									if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && K8sApiCaller.roleBindingAlreadyExist( resourceName, claimNamespace ) ) {
										K8sApiCaller.updateRoleBinding( claim );
										replaceRbcStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "rolebinding update success.", claimNamespace );
									} else if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && !K8sApiCaller.roleBindingAlreadyExist( resourceName, claimNamespace ) ) {
										K8sApiCaller.createRoleBinding( claim );
										replaceRbcStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "rolebinding create success.", claimNamespace );
									} 
									break;
								case Constants.EVENT_TYPE_DELETED : 
									// Nothing to do
									break;
							}
						}
						
					} catch (Exception e) {
						logger.info("Exception: " + e.getMessage());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						logger.info(sw.toString());
						try {
							replaceRbcStatus( claimName, Constants.CLAIM_STATUS_ERROR, e.getMessage(), claimNamespace );
						} catch (ApiException e1) {
							e1.printStackTrace();
							logger.info("Resource Quota Claim Controller Exception : Change Status 'Error' Fail ");
						}
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				logger.info("=============== RBC 'For Each' END ===============");
				rbcController = Watch.createWatch(client,
						api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM, null, null, null, null, null, Long.toString( latestResourceVersion ), null, Boolean.TRUE, null),
						new TypeToken<Watch.Response<RoleBindingClaim>>() {}.getType());
			}
		} catch (Exception e) {
			logger.info("Resource Quota Claim Controller Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
			if( e.getMessage().equals("abnormal") ) {
				logger.info("Catch abnormal conditions!! Exit process");
				System.exit(1);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void replaceRbcStatus( String name, String status, String reason, String namespace ) throws ApiException {
		JsonArray patchStatusArray = new JsonArray();
		JsonObject patchStatus = new JsonObject();
		JsonObject statusObject = new JsonObject();
		patchStatus.addProperty("op", "replace");
		patchStatus.addProperty("path", "/status");
		statusObject.addProperty( "status", status );
		statusObject.addProperty( "reason", reason );
		patchStatus.add("value", statusObject);
		patchStatusArray.add( patchStatus );
		
		logger.info( "Patch Status Object : " + patchStatusArray );
		/*[
		  "op" : "replace",
		  "path" : "/status",
		  "value" : {
		    "status" : "Awaiting"
		  }
		]*/
		try {
			api.patchNamespacedCustomObjectStatus(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					namespace, 
					Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM, 
					name, 
					patchStatusArray );
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			logger.info("ApiException Code: " + e.getCode());
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	private String getClaimStatus( String name, String namespace ) throws ApiException {
		Object claimJson = null;
		try {
			claimJson = api.getNamespacedCustomObject(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					namespace, 
					Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM,  
					name );
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			logger.info("ApiException Code: " + e.getCode());
			throw e;
		}

		String objectStr = new Gson().toJson( claimJson );
		logger.info( objectStr );
		
		JsonParser parser = new JsonParser();
		String status = parser.parse( objectStr ).getAsJsonObject().get( "status" ).getAsJsonObject().get( "status" ).getAsString();
		
		logger.info( "Status : " + status );

		return status;
	}
	
	public static long getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
