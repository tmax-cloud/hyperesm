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
import k8s.example.client.models.StateCheckInfo;

public class ResourceQuotaClaimController extends Thread {
	private Watch<NamespaceClaim> rqcController;
	private static long latestResourceVersion = 0;
	private CustomObjectsApi api = null;
	ApiClient client = null;
    private Logger logger = Main.logger;
	StateCheckInfo sci = new StateCheckInfo();

	ResourceQuotaClaimController(ApiClient client, CustomObjectsApi api, long resourceVersion) throws Exception {
		rqcController = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM, null, null, null, null, null, Long.toString( resourceVersion ), null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<NamespaceClaim>>() {}.getType());
		this.api = api;
		this.client = client;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				sci.checkThreadState();
				rqcController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.info("Interrupted!");
							rqcController.close();
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
									
					// Logic here
					String claimName = "unknown";
					String resourceName = "unknown";
					String claimNamespace = "unknown";
					try {
						NamespaceClaim claim = response.object;

						if( claim != null) {
							latestResourceVersion = Long.parseLong( response.object.getMetadata().getResourceVersion() );
							String eventType = response.type.toString(); //ADDED, MODIFIED, DELETED
							logger.info("[ResourceQuotaClaim Controller] Event Type : " + eventType );
							logger.info("[ResourceQuotaClaim Controller] == ResourceQuotaClaim == \n" + claim.toString());
							claimName = claim.getMetadata().getName();
							resourceName = claim.getResourceName();
							claimNamespace = claim.getMetadata().getNamespace();
							switch( eventType ) {
								case Constants.EVENT_TYPE_ADDED :
									// Patch Status to Awaiting
									replaceRqcStatus( claimName, Constants.CLAIM_STATUS_AWAITING, "wait for admin permission", claimNamespace );				
									break;
								case Constants.EVENT_TYPE_MODIFIED : 
									String status = getClaimStatus( claimName, claimNamespace );
									if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && K8sApiCaller.resourcequotaAlreadyExist( resourceName, claimNamespace ) ) {
										K8sApiCaller.updateResourceQuota( claim );
										replaceRqcStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "resource quota update success.", claimNamespace );
									} else if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && !K8sApiCaller.resourcequotaAlreadyExist( resourceName, claimNamespace ) ) {
										K8sApiCaller.createResourceQuota( claim );
										replaceRqcStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "resource quota create success.", claimNamespace );
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
							replaceRqcStatus( claimName, Constants.CLAIM_STATUS_ERROR, e.getMessage(), claimNamespace );
						} catch (ApiException e1) {
							e1.printStackTrace();
							logger.info("Resource Quota Claim Controller Exception : Change Status 'Error' Fail ");
						}
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				logger.info("=============== RQC 'For Each' END ===============");
				rqcController = Watch.createWatch(client,
						api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM, null, null, null, null, null, Long.toString( latestResourceVersion ), null, Boolean.TRUE, null),
						new TypeToken<Watch.Response<NamespaceClaim>>() {}.getType());
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
	private void replaceRqcStatus( String name, String status, String reason, String namespace ) throws ApiException {
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
					Constants.CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM,  
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
					Constants.CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM,  
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
