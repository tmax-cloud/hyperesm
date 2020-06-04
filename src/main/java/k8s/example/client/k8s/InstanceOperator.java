package k8s.example.client.k8s;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.JSON.DateTimeTypeAdapter;
import io.kubernetes.client.openapi.JSON.DateTypeAdapter;
import io.kubernetes.client.openapi.JSON.SqlDateTypeAdapter;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.StringUtil;
import k8s.example.client.k8s.apis.CustomResourceApi;
import k8s.example.client.models.StateCheckInfo;
import okio.ByteString;

public class InstanceOperator extends Thread {
    private Logger logger = Main.logger;
	private Watch<Object> watchInstance;
	private static long latestResourceVersion = 0;
	
	ApiClient client = null;
	CustomResourceApi tpApi = null;
	ObjectMapper mapper = new ObjectMapper();
	Gson gson = new GsonBuilder().create();
	StateCheckInfo sci = new StateCheckInfo();
	
	private DateTypeAdapter dateTypeAdapter = new DateTypeAdapter();
	private SqlDateTypeAdapter sqlDateTypeAdapter = new SqlDateTypeAdapter();
	private DateTimeTypeAdapter dateTimeTypeAdapter = new DateTimeTypeAdapter();
	private LocalDateTypeAdapter localDateTypeAdapter = new LocalDateTypeAdapter();
	private ByteArrayAdapter byteArrayTypeAdapter = new ByteArrayAdapter();
	
	public Gson kubeGson = new GsonBuilder()
            .registerTypeAdapter(Date.class, dateTypeAdapter)
            .registerTypeAdapter(java.sql.Date.class, sqlDateTypeAdapter)
            .registerTypeAdapter(DateTime.class, dateTimeTypeAdapter)
            .registerTypeAdapter(LocalDate.class, localDateTypeAdapter)
            .registerTypeAdapter(byte[].class, byteArrayTypeAdapter)
//            .registerTypeAdapter(new TypeToken<Watch.Response<Object>>(){}.getType(),  new MapDeserializerDoubleAsIntFix())
            .create();
	
	InstanceOperator(ApiClient client, CustomResourceApi api, long resourceVersion) throws Exception {
		JSON clientJson = client.getJSON();
		clientJson.setGson(kubeGson);
		client.setJSON(clientJson);
		
		ApiClient customClient = api.getApiClient();
		JSON customJson = api.getApiClient().getJSON();
		customJson.setGson(kubeGson);
		customClient.setJSON(customJson);
		api.setApiClient(customClient);
		
		watchInstance = Watch.createWatch(
		        client,
		        api.listClusterCustomObjectCall(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, null, null, null, null, null, String.valueOf(resourceVersion), null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<Object>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;
		this.client = client;
		this.tpApi = api;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			while(true) {
				sci.checkThreadState();
				watchInstance.forEach(response -> {
					try {
						if(Thread.interrupted()) {
							logger.info("Interrupted!");
							watchInstance.close();
						}
					} catch(Exception e) {
						logger.info(e.getMessage());
					}
					
					try {
						JsonNode instanceObj = numberTypeConverter(objectToJsonNode(response.object));
						logger.info("[Instance Operator] Event Type : " + response.type.toString()); //ADDED, MODIFIED, DELETED
						logger.info("[Instance Operator] Object : " + instanceObj.toString());
						
		        		latestResourceVersion = instanceObj.get("metadata").get("resourceVersion").asLong();
		        		String instanceNamespace = instanceObj.get("metadata").get("namespace").asText();
		        		logger.info("[Instance Operator] Instance Name : " + instanceObj.get("metadata").get("name").asText());
		        		logger.info("[Instance Operator] Instance Namespace : " + instanceObj.get("metadata").get("namespace").asText());
		        		logger.info("[Instance Operator] ResourceVersion : " + latestResourceVersion);
		        		
		        		if(response.type.toString().equals("ADDED")) {
		        			String templateName = instanceObj.get("spec").get("template").get("metadata").get("name").asText();
		        			
		        			logger.info("[Instance Operator] Template Name : " + templateName);
						
							String templateNamespace = instanceObj.get("metadata").get("namespace").asText();
		        			if ( instanceObj.get("metadata").get("ownerReferences") != null ) {
		        				for(JsonNode owner : instanceObj.get("metadata").get("ownerReferences")) {
		        					if (owner.get("kind") != null && owner.get("kind").asText().equals(Constants.SERVICE_INSTANCE_KIND)) {
		        						templateNamespace = Constants.DEFAULT_NAMESPACE;
		        						if ( System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE) != null && !System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE).isEmpty() ) {
		        							templateNamespace = System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE);
		        						}
		        					}
								}
		        			}
		        			logger.info("[Instance Operator] Template Namespace : " + templateNamespace);
		        			Object template = null;
		        			try {
		        				template = tpApi.getNamespacedCustomObject(
			        					Constants.CUSTOM_OBJECT_GROUP, 
			        					Constants.CUSTOM_OBJECT_VERSION, 
			        					templateNamespace, 
			        					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, 
			        					templateName);
		        			} catch (Exception e) {
		        				throw new Exception("Template Not Found");
		        			}
		        			
		        			//logger.info("[Instance Operator] Template : " + template.toString());
		        			
		        			JsonNode templateObjs = numberTypeConverter(objectToJsonNode(template).get("objects"));
		        			JsonNode parameters = instanceObj.get("spec").get("template").get("parameters");
		        			
		        			JSONObject specObj = new JSONObject();
	    					JSONObject tpObj = new JSONObject();
	    					JSONObject obj = new JSONObject();
	    					
	    					JSONArray objArr = new JSONArray();
		        			
	    					JSONObject parmPatch = null;
	    					JSONArray parmPatchArray = null;
	    					List<String> existParm = new ArrayList<>();
	    					
	    					Map<String,String> defaultValueMap = new HashMap<>();
	    					if ( objectToJsonNode(template).get("parameters") != null ) {
	        					for(JsonNode parameter : objectToJsonNode(template).get("parameters")) {
	        						String name = "";
	        						String defaultValue = "";
        							if( parameter.has("value") ) {
        								defaultValue = parameter.get("value").asText();
	        						}
        							if ( parameter.has("name") ) {
        								name = parameter.get("name").asText();
        							}
        							if ( !defaultValueMap.containsKey(name) ) {
        								defaultValueMap.put(name, defaultValue);
        							}
	        					}
	    					}
        							
		        			if(templateObjs.isArray()) {
		        				for(JsonNode object : templateObjs) {
			        				String objStr = object.toString();
			        				//logger.info("[Instance Operator] Template Object : " + objStr);
			        				if ( parameters != null ) {
			        					
			        					for(JsonNode parameter : parameters) {
					        				String paramName = null;
					        				String paramValue = null;
					        				if(parameter.has("name") && parameter.has("value")) {
					        					paramName = parameter.get("name").asText();
						        				paramValue = parameter.get("value").asText();
						        				if ( StringUtil.isEmpty(paramValue) && defaultValueMap.containsKey(paramName) ) {
						        					paramValue = defaultValueMap.get(paramName);
						        				}
						        				if ( !existParm.contains( paramName ) ) {
						        					if (parmPatchArray == null) parmPatchArray = new JSONArray();
						        					parmPatch = new JSONObject();
						        					parmPatch.put("name", paramName);
						        					parmPatch.put("value", paramValue);
						        					parmPatchArray.add(parmPatch);
						        					existParm.add( paramName );
						        				}

						        				String dataType = existParameter( objectToJsonNode(template).get("parameters"), paramName );
						        				if ( objectToJsonNode(template).get("parameters") != null && dataType != null ) {
						        					
						        					if (dataType.equals(Constants.TEMPLATE_DATA_TYPE_NUMBER)) {
						        						String replaceString = "\"${" + paramName + "}\"";
						        						if( objStr.contains( replaceString ) ) {
								        					logger.info("[Instance Operator] Parameter Number Name to be replaced : " + replaceString);
									        				logger.info("[Instance Operator] Parameter Number Value to be replaced : " + paramValue);
								        					objStr = objStr.replace( replaceString, paramValue );
								        				}
						        					}
						        					
						        					String replaceString = "${" + paramName + "}";
						        					if( objStr.contains( replaceString ) ) {
							        					logger.info("[Instance Operator] Parameter Name to be replaced : " + replaceString);
								        				logger.info("[Instance Operator] Parameter Value to be replaced : " + paramValue);
							        					objStr = objStr.replace( replaceString, paramValue );
							        				}
						        				}
					        				}
					        				
			        					}
			        					
			        				}

			        				if ( objectToJsonNode(template).get("parameters") != null ) {
			        					for(JsonNode parameter : objectToJsonNode(template).get("parameters")) {
			        						String defaultValue = "";
		        							if( parameter.has("value") ) {
		        								defaultValue = parameter.get("value").asText();
			        						}
		        							if ( parameter.has("name") ) {
		        								String paramName = parameter.get("name").asText();
		        								if( parameter.has("valueType") && parameter.get("valueType").asText().equals( Constants.TEMPLATE_DATA_TYPE_NUMBER )) {
			        								String replaceString = "\"${" + paramName + "}\"";
					        						if( objStr.contains( replaceString ) ) {
							        					logger.info("[Instance Operator] Default Parameter Number Name to be replaced : " + replaceString);
								        				logger.info("[Instance Operator] Default Parameter Number Value to be replaced : " + defaultValue);
							        					objStr = objStr.replace( replaceString, defaultValue );
							        					
							        					if ( !existParm.contains( paramName ) ) {
								        					if (parmPatchArray == null) parmPatchArray = new JSONArray();
								        					parmPatch = new JSONObject();
								        					parmPatch.put("name", paramName);
								        					parmPatch.put("value", defaultValue);
								        					parmPatchArray.add(parmPatch);
								        					existParm.add( paramName );
								        				}
							        				}
				        						}
		        								String replaceString = "${" + paramName + "}";
					        					if( objStr.contains( replaceString ) ) {
						        					logger.info("[Instance Operator] Default Parameter Name to be replaced : " + replaceString);
							        				logger.info("[Instance Operator] Default Parameter Value to be replaced : " + defaultValue);
						        					objStr = objStr.replace( replaceString, defaultValue );
						        					
						        					if ( !existParm.contains( paramName ) ) {
							        					if (parmPatchArray == null) parmPatchArray = new JSONArray();
							        					parmPatch = new JSONObject();
							        					parmPatch.put("name", paramName);
							        					parmPatch.put("value", defaultValue);
							        					parmPatchArray.add(parmPatch);
							        					existParm.add( paramName );
							        				}
						        				}
		        							}
			        					}
			        				}
			        				
			        				String[] splitStr = objStr.split("\"metadata\":\\{");
			        				StringBuilder sb = new StringBuilder();
			        				sb.append("\"ownerReferences\": [{\"apiVersion\": \"v1\",\"blockOwnerDeletion\": true,\"controller\": false,\"kind\": \"TemplateInstance\",");
			        				sb.append("\"name\": \"");
			        				sb.append(instanceObj.get("metadata").get("name").asText());
			        				sb.append("\",\"uid\": \"");
			        				sb.append(instanceObj.get("metadata").get("uid").asText());
			        				sb.append("\"}],");
			        				
			        				StringBuilder objSb = new StringBuilder();
			        				objSb.append( splitStr[0] );
			        				for ( int i = 1; i < splitStr.length; i++ ) {
			        					objSb.append( "\"metadata\":{" );
			        					objSb.append( sb.toString() );
			        					objSb.append( splitStr[i] );
			        				}
			        				//objStr = splitStr[0] + "\"metadata\":{" + sb.toString() + splitStr[1];
			        				//logger.info("[Instance Operator] @@@@@@@@@@@@@@@@@ Split Template Object[0] : " + splitStr[0]);
			        				//logger.info("[Instance Operator] @@@@@@@@@@@@@@@@@ Split Template Object[1] : " + splitStr[1]);
			        				//logger.info("[Instance Operator] Template Object : " + objStr);

			        				JsonNode replacedObject = numberTypeConverter(mapper.readTree(objSb.toString()));
			        				logger.info("[Instance Operator] Replaced Template Object : " + replacedObject);
			        				
			        				//if(!objStr.contains("${")) {
			        					String apiGroup = null;
			        					String apiVersion = null;
			        					String namespace = null;
			        					String kind = null;
			        					
			        					if(replacedObject.has("apiVersion")) {
			        						if(replacedObject.get("apiVersion").asText().contains("/")) {
			        							apiGroup = replacedObject.get("apiVersion").asText().split("/")[0];
			        							apiVersion = replacedObject.get("apiVersion").asText().split("/")[1];
			        						} else {
			        							apiGroup = "core";
			        							apiVersion = replacedObject.get("apiVersion").asText();
			        						}
			        					}
			        					
			        					if(replacedObject.get("metadata").has("namespace")) {
			        						namespace = replacedObject.get("metadata").get("namespace").asText();
			        					} else {
			        						if (instanceObj.get("metadata").has("namespace")) {
			        							namespace = instanceObj.get("metadata").get("namespace").asText();
			        						} else {
			        							namespace = "default";
			        						}
			        						
			        					}
			        					
			        					if(replacedObject.has("kind")) {
			        						kind = replacedObject.get("kind").asText();
			        					}
			        								        					
			        					JSONParser parser = new JSONParser();
			        					JSONObject bodyObj = (JSONObject) parser.parse(replacedObject.toString());
			        					objArr.add(bodyObj);
			        							        							        					
			        					try {
			        						Object result = tpApi.createNamespacedCustomObject(apiGroup, apiVersion, namespace, kind, bodyObj, null);
			        						logger.info(result.toString());
			        						patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_RUNNING, instanceNamespace);
			        					} catch (ApiException e) {
			        						logger.info("[Instance Operator] ApiException: " + e.getMessage());
			        						logger.info(e.getResponseBody());
			        						patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getResponseBody(), instanceNamespace);
			        						throw e;
			        					} catch (Exception e) {
			        						logger.info("[Instance Operator] Exception: " + e.getMessage());
			        						StringWriter sw = new StringWriter();
			        						e.printStackTrace(new PrintWriter(sw));
			        						logger.info(sw.toString());
			        						patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getMessage(), instanceNamespace);
			        						throw e;
			        					}
			        				//} else {
			        				//	throw new Exception("Some non-replaced parameters or invaild values exist");
			        				//}
			        			}
		        			}
		        			
		        			obj.put("objects", objArr);
	    					tpObj.put("template", obj);
	    					specObj.put("spec", tpObj);
	    					logger.info("[Instance Operator] Object to be patched : " + specObj.toString());
	    					
	    					JSONObject patch = new JSONObject();
	    					JSONArray patchArray = new JSONArray();
	    					patch.put("op", "add");
	    					patch.put("path", "/spec/template/objects");
	    					patch.put("value", objArr);
	    					patchArray.add(patch);
	    					
	    					try{
	    						Object result = tpApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, instanceNamespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceObj.get("metadata").get("name").asText(), patchArray);
	    						logger.info(result.toString());
	    					} catch (ApiException e) {
	    						throw new Exception(e.getResponseBody());
	    					}
	    					
	    					if ( parmPatchArray != null ) {
	    						for ( int i = 0; i < parmPatchArray.size(); i++ ) {
	    							parmPatchArray.get(i).toString();
	    						}
	    						JSONObject parmPatchInput = new JSONObject();
		    					JSONArray parmPatchArrayInput = new JSONArray();
		    					parmPatchInput.put("op", "replace");
		    					parmPatchInput.put("path", "/spec/template/parameters");
		    					parmPatchInput.put("value", parmPatchArray);
		    					parmPatchArrayInput.add(parmPatchInput);
		    					
		    					try{
		    						Object result = tpApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, instanceNamespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceObj.get("metadata").get("name").asText(), parmPatchArrayInput);
		    						logger.info(result.toString());
		    					} catch (ApiException e) {
		    						throw new Exception(e.getResponseBody());
		    					}
	    					}
	    					
		        		} else if(response.type.toString().equals("DELETED")) {
		        			V1DeleteOptions body = new V1DeleteOptions();
		        			logger.info("[Instance Operator] Template Instance " + instanceObj.get("metadata").get("name") + " is DELETED");
		        			JsonNode instanceObjs = instanceObj.get("spec").get("template").get("objects");
		        			
		        			/*if(instanceObjs.isArray()) {
		        				for(JsonNode object : instanceObjs) {
		        					String apiGroup = null;
		        					String apiVersion = null;
		        					String kind = null;
		        					String namespace = null;
		        					String name = object.get("metadata").get("name").asText();
		        					
		        					if(object.has("apiVersion")) {
		        						if(object.get("apiVersion").asText().contains("/")) {
		        							apiGroup = object.get("apiVersion").asText().split("/")[0];
		        							apiVersion = object.get("apiVersion").asText().split("/")[1];
		        						} else {
		        							apiGroup = "core";
		        							apiVersion = object.get("apiVersion").asText();
		        						}
		        					}
		        					
		        					if(object.get("metadata").has("namespace")) {
		        						namespace = object.get("metadata").get("namespace").asText();
		        					} else {
		        						namespace = "default";
		        					}
		        					
		        					if(object.has("kind")) {
		        						kind = object.get("kind").asText();
		        					}
		        					
		        					logger.info(apiVersion + "/" + kind + " \"" + name + "\" deleted");
		        					try {
		        						Object result = tpApi.deleteNamespacedCustomObject(apiGroup, apiVersion, namespace, kind, name, body, 0, null, null);
		        						logger.info(result.toString());
		        					} catch (ApiException e) {
		        						throw new Exception(e.getResponseBody());
		        					}
		        				}
		        			}*/
		        		}
					} catch(Exception e) {
						logger.info("[Instance Operator] Instance Operator Exception: " + e.getMessage());
					}
	        	});
				logger.info("=============== Instance 'For Each' END ===============");
				watchInstance = Watch.createWatch(
				        client,
				        tpApi.listClusterCustomObjectCall(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, null, null, null, null, null, String.valueOf(latestResourceVersion), null, Boolean.TRUE, null),
				        new TypeToken<Watch.Response<Object>>(){}.getType());
			}

		} catch (Exception e) {
			logger.info("[Instance Operator] Instance Operator Exception: " + e.getMessage());
			if( e.getMessage().equals("abnormal") ) {
				logger.info("Catch abnormal conditions!! Exit process");
				System.exit(1);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void patchStatus(String instanceName, String phrase, String namespace) throws Exception {
		JSONObject patchStatus = new JSONObject();
		JSONObject status = new JSONObject();
		JSONArray conditions = new JSONArray();
		JSONObject condition = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();
		condition.put("type", "Phase");
		condition.put("status", phrase);
		conditions.add(condition);
		status.put("conditions", conditions);
		patchStatus.put("op", "add");
		patchStatus.put("path", "/status");
		patchStatus.put("value", status);
		patchStatusArray.add(patchStatus);
		
		try{
			tpApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceName, patchStatusArray);
		} catch (ApiException e) {
			throw new Exception(e.getResponseBody());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void patchStatus(String instanceName, String phrase, String message, String namespace) throws Exception {
		JSONObject patchStatus = new JSONObject();
		JSONObject status = new JSONObject();
		JSONArray conditions = new JSONArray();
		JSONObject condition = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();
		condition.put("type", "Phase");
		condition.put("status", phrase);
		condition.put("message", message);
		conditions.add(condition);
		status.put("conditions", conditions);
		patchStatus.put("op", "add");
		patchStatus.put("path", "/status");
		patchStatus.put("value", status);
		patchStatusArray.add(patchStatus);
		
		try{
			tpApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceName, patchStatusArray);
		} catch (ApiException e) {
			throw new Exception(e.getResponseBody());
		}
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

	private JsonNode objectToJsonNode(Object object) {
		String objectStr = gson.toJson(object);
		JsonNode resultNode = null;
		try {
			resultNode = mapper.readTree(objectStr);
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		return resultNode;
	}

	public static long getLatestResourceVersion() {
		return latestResourceVersion;
	}
	
	public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

        private DateTimeFormatter formatter;

        public LocalDateTypeAdapter() {
            this(ISODateTimeFormat.date());
        }

        public LocalDateTypeAdapter(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        public void setFormat(DateTimeFormatter dateFormat) {
            this.formatter = dateFormat;
        }

        @Override
        public void write(JsonWriter out, LocalDate date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(formatter.print(date));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            switch (in.peek()) {
            case NULL:
                in.nextNull();
                return null;
            default:
                String date = in.nextString();
                return formatter.parseLocalDate(date);
            }
        }
    }
	
	public class ByteArrayAdapter extends TypeAdapter<byte[]> {

        @Override
        public void write(JsonWriter out, byte[] value) throws IOException {
            boolean oldHtmlSafe = out.isHtmlSafe();
            out.setHtmlSafe(false);
            if (value == null) {
                out.nullValue();
            } else {
                out.value(ByteString.of(value).base64());
            }
            out.setHtmlSafe(oldHtmlSafe);
        }

        @Override
        public byte[] read(JsonReader in) throws IOException {
            switch (in.peek()) {
            case NULL:
                in.nextNull();
                return null;
            default:
                String bytesAsBase64 = in.nextString();
                ByteString byteString = ByteString.decodeBase64(bytesAsBase64);
                return byteString.toByteArray();
            }
        }
    }
	
	private String existParameter( JsonNode parameters, String paramName ) {
		String dataType = null;
		for(JsonNode parameter : parameters) {
			if( parameter.has("name") && parameter.get("name").asText().toUpperCase().equals( paramName.toUpperCase() )) {
				if( parameter.has("valueType") && parameter.get("valueType").asText().equals( Constants.TEMPLATE_DATA_TYPE_NUMBER )) {
					dataType = Constants.TEMPLATE_DATA_TYPE_NUMBER;
				} else {
					dataType = Constants.TEMPLATE_DATA_TYPE_STRING;
				}
			}
		}
		return dataType;
	}
}
