package k8s.example.client.k8s;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import k8s.example.client.models.StateCheckInfo;

public class CatalogServiceClaimController extends Thread {
	private Watch<Object> cscController;
	private static long latestResourceVersion = 0;
	private CustomObjectsApi api = null;
	ApiClient client = null;
    private Logger logger = Main.logger;
	StateCheckInfo sci = new StateCheckInfo();
	
	ObjectMapper mapper = new ObjectMapper();
	Gson gson = new GsonBuilder().create();

	CatalogServiceClaimController(ApiClient client, CustomObjectsApi api, long resourceVersion) throws Exception {
		cscController = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_CATALOGSERVICECLAIM, null, null, null, null, null, Long.toString( resourceVersion ), null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<Object>>() {}.getType());
		this.api = api;
		this.client = client;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				sci.checkThreadState();
				cscController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.info("Interrupted!");
							cscController.close();
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
					
					
					// Logic here
					String claimName = "unknown";
					String claimNamespace = "unknown";
					String resourceName = "unknown";
					String catalogNamespace = "unknown";
		
					try {
						JsonNode claim = numberTypeConverter(objectToJsonNode(response.object));

						if( claim != null) {
							latestResourceVersion = claim.get("metadata").get("resourceVersion").asLong();
							String eventType = response.type.toString(); //ADDED, MODIFIED, DELETED
							logger.info("[CatalogServiceClaim Controller] Event Type : " + eventType );
							logger.info("[CatalogServiceClaim Controller] == CatalogServiceClaim == \n" + claim.toString());
							claimName = claim.get("metadata").get("name").asText();
							claimNamespace = claim.get("metadata").get("namespace").asText();
							resourceName = claim.get("spec").get("metadata").get("name").asText();
							catalogNamespace = Constants.DEFAULT_NAMESPACE;
    						if ( System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE) != null && !System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE).isEmpty() ) {
    							catalogNamespace = System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE);
    						}
							
							logger.info("[CatalogServiceClaim Controller] Claim Name : " + claimName );
							logger.info("[CatalogServiceClaim Controller] Claim Namespace : " + claimNamespace );
							logger.info("[CatalogServiceClaim Controller] Template Name : " + resourceName );
							logger.info("[CatalogServiceClaim Controller] Catalog Namespace : " + catalogNamespace );

							switch( eventType ) {
								case Constants.EVENT_TYPE_ADDED : 
									// Patch Status to Awaiting
									replaceCscStatus( claimName, Constants.CLAIM_STATUS_AWAITING, "wait for admin permission", claimNamespace );
									break;
								case Constants.EVENT_TYPE_MODIFIED : 
									String status = getClaimStatus( claimName, claimNamespace );
									if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && K8sApiCaller.templateAlreadyExist( resourceName, catalogNamespace ) ) {
										//K8sApiCaller.updateTemplate( claim );
										replaceCscStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "template update success.", claimNamespace );
									} else if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && !K8sApiCaller.templateAlreadyExist( resourceName, catalogNamespace ) ) {
										K8sApiCaller.createTemplate( claim, catalogNamespace );
										replaceCscStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "template create success.", claimNamespace );
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
							replaceCscStatus( claimName, Constants.CLAIM_STATUS_ERROR, e.getMessage(), claimNamespace );
						} catch (ApiException e1) {
							e1.printStackTrace();
							logger.info("Resource Quota Claim Controller Exception : Change Status 'Error' Fail ");
						}
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				logger.info("=============== CSC 'For Each' END ===============");
				cscController = Watch.createWatch(client,
						api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_CATALOGSERVICECLAIM, null, null, null, null, null, Long.toString( latestResourceVersion ), null, Boolean.TRUE, null),
						new TypeToken<Watch.Response<Object>>() {}.getType());
			}
		} catch (Exception e) {
			logger.info("Catalog Service Claim Controller Exception: " + e.getMessage());
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
	private void replaceCscStatus( String name, String status, String reason, String namespace ) throws ApiException {
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
					Constants.CUSTOM_OBJECT_PLURAL_CATALOGSERVICECLAIM, 
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
					Constants.CUSTOM_OBJECT_PLURAL_CATALOGSERVICECLAIM,  
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
	
	private JsonNode numberTypeConverter(JsonNode jsonNode) {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			
			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			
			while(iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				entry.setValue(numberTypeConverter(entry.getValue()));
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;
			for(int i = 0; i < arrayNode.size(); i++) {
				arrayNode.set(i, numberTypeConverter(arrayNode.get(i)));
			}
		} else if (jsonNode.isValueNode()) {
			if(jsonNode.isDouble() && jsonNode.canConvertToInt()) {
				IntNode intNode = new IntNode(jsonNode.asInt());
				jsonNode = intNode;
			}
		}
		return jsonNode;
	}

	private JsonNode objectToJsonNode(Object object) throws IOException {
		JsonNode resultNode = mapper.valueToTree(object);
		return resultNode;
	}
	
	public static long getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
