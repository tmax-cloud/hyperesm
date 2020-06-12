package k8s.example.client.k8s;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1LoadBalancerIngress;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.util.Config;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.BindingInDO;
import k8s.example.client.models.BindingOutDO;
import k8s.example.client.models.Cost;
import k8s.example.client.models.Endpoint;
import k8s.example.client.models.GetPlanDO;
import k8s.example.client.models.InputParametersSchema;
import k8s.example.client.models.Metadata;
import k8s.example.client.models.PlanMetadata;
import k8s.example.client.models.ProvisionInDO;
import k8s.example.client.models.Schemas;
import k8s.example.client.models.ServiceInstanceSchema;
import k8s.example.client.models.ServiceMetadata;
import k8s.example.client.models.ServiceOffering;
import k8s.example.client.models.ServicePlan;
import k8s.example.client.models.Services;
import k8s.example.client.models.Settings;
import k8s.example.client.models.TemplateInstance;
import k8s.example.client.models.TemplateInstanceSpec;
import k8s.example.client.models.TemplateInstanceSpecTemplate;
import k8s.example.client.models.TemplateParameter;

public class K8sApiCaller {
	private static ApiClient k8sClient;
	private static CoreV1Api api;
	private static CustomObjectsApi customObjectApi;
	private static ObjectMapper mapper = new ObjectMapper();
	private static Gson gson = new GsonBuilder().create();

	private static Logger logger = Main.logger;

	public static void initK8SClient() throws Exception {
		k8sClient = Config.fromCluster();
		k8sClient.setConnectTimeout(0);
		k8sClient.setReadTimeout(0);
		k8sClient.setWriteTimeout(0);
		//logger.info("Access Token for k8s client : " + Settings.getToken() );
		//k8sClient.setAccessToken( Settings.getToken() );
		Configuration.setDefaultApiClient(k8sClient);

		api = new CoreV1Api();
		customObjectApi = new CustomObjectsApi();
	}
	
	public static Services getCatalog() throws ApiException {
		Services catalog = new Services();
		List<ServiceOffering> serviceList = new ArrayList<ServiceOffering>();
		logger.info("Catalog Namespace : " + Settings.getNamespace() );
		String catalogNamespace = Settings.getNamespace();

		Object templates = null;
		
		try {
			templates = customObjectApi.listNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, catalogNamespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, null, null,
					null, null, null, null, null, false);
		} catch (ApiException e) {
			logger.info("[K8S-Exception] " + e.getResponseBody());
			throw e;
		}
		

		JsonNode templateList = numberTypeConverter(objectToJsonNode(templates).get("items"));
		logger.info("Catalog Debug 1");

		if (templateList.isArray()) {
			for (JsonNode template : templateList) {
				ServiceOffering service = new ServiceOffering();
				ServiceMetadata serviceMeta = new ServiceMetadata();
				List<ServicePlan> planList = new ArrayList<ServicePlan>();

				service.setName(template.get("metadata").get("name").asText());
				service.setId(template.get("metadata").get("name").asText());

				if (template.get("shortDescription") == null) {
					service.setDescription(template.get("metadata").get("name").asText());
				} else {
					service.setDescription(template.get("shortDescription").asText());
				}
				if (template.get("imageUrl") == null) {
					serviceMeta.setImageUrl(Constants.DEFAULT_IMAGE_URL);
				} else {
					serviceMeta.setImageUrl(template.get("imageUrl").asText());
				}
				if (template.get("longDescription") == null) {
					serviceMeta.setLongDescription(template.get("metadata").get("name").asText());
				} else {
					serviceMeta.setLongDescription(template.get("longDescription").asText());
				}
				if (template.get("longDescription") == null) {
					serviceMeta.setLongDescription(template.get("metadata").get("name").asText());
				} else {
					serviceMeta.setLongDescription(template.get("longDescription").asText());
				}
				if (template.get("urlDescription") == null) {
					serviceMeta.setUrlDescription(template.get("metadata").get("name").asText());
				} else {
					serviceMeta.setUrlDescription(template.get("urlDescription").asText());
				}
				if (template.get("markdownDescription") == null) {
					serviceMeta.setMarkdownDescription(template.get("metadata").get("name").asText());
				} else {
					serviceMeta.setMarkdownDescription(template.get("markdownDescription").asText());
				}
				if (template.get("provider") == null) {
					serviceMeta.setProviderDisplayName(Constants.DEFAULT_PROVIDER);
				} else {
					serviceMeta.setProviderDisplayName(template.get("provider").asText());
				}
				if (template.get("recommend") != null) {
					serviceMeta.setRecommend(template.get("recommend").asBoolean());
				} else {
					serviceMeta.setRecommend(false);
				}

				List<String> tags = new ArrayList<String>();
				if (template.get("tags") != null) {
					for (JsonNode tag : template.get("tags")) {
						tags.add(tag.asText());
					}
				} else {
					tags.add(Constants.DEFAULT_TAGS);
				}
				service.setTags(tags);
				service.setMetadata(serviceMeta);

				logger.info("Catalog Debug 2");
				if (template.get("objectKinds") != null) {
					JsonNode objectKinds = template.get("objectKinds");
					if (objectKinds.isArray()) {
						List<String> kinds = null;
						ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {
						});
						try {
							kinds = reader.readValue(objectKinds);
						} catch (IOException e) {
							logger.info(e.getMessage());
							;
						}

						if (kinds.contains("Secret") || kinds.contains("Service (LoadBalancer)")) {
							service.setBindable(true);
						}
					}
				} else {
					service.setBindable(false);
				}
				service.setBindings_retrievable(false);
				service.setInstances_retrievable(false);

				logger.info("Catalog Debug 3");
				try {
					if (template.get("plans") != null) {
						JsonNode plans = template.get("plans");
						if (plans.isArray()) {
							int defaultPlaneId = 1;
							for (JsonNode plan : plans) {
								try {
									ServicePlan servicePlan = new ServicePlan();
									PlanMetadata planMeta = new PlanMetadata();
									List<String> bullets = new ArrayList<String>();
									Cost planCost = new Cost();
									Schemas planSchema = new Schemas();
									ServiceInstanceSchema instanceSchema = new ServiceInstanceSchema();
									InputParametersSchema create = new InputParametersSchema();
									Map<String, String> parameters = null;

									String uuid = UUID.randomUUID().toString();
									servicePlan.setId(uuid);
									if (plan.get("name") == null) {
										servicePlan.setName(template.get("metadata").get("name").asText() + "-plan"
												+ defaultPlaneId);
									} else {
										servicePlan.setName(plan.get("name").asText());
									}
									if (plan.get("description") == null) {
										servicePlan.setDescription(template.get("metadata").get("name").asText()
												+ "-plan" + defaultPlaneId);
									} else {
										servicePlan.setDescription(plan.get("description").asText());
									}
									if (plan.get("bindable") == null) {
										servicePlan.setBindable(false);
									} else {
										servicePlan.setBindable(plan.get("bindable").asBoolean());
									}
									defaultPlaneId++;

									logger.info("Catalog Debug 4");
									try {
										if ( plan.get("metadata") != null ) {
											if (plan.get("metadata").get("bullets") != null) {
												for (JsonNode bullet : plan.get("metadata").get("bullets")) {
													bullets.add(bullet.asText());
												}
												planMeta.setBullets(bullets);
											}

											planCost.setAmount(plan.get("metadata").get("costs").get("amount").asText());
											planCost.setUnit(plan.get("metadata").get("costs").get("unit").asText());
											planMeta.setCosts(planCost);
										}
										servicePlan.setMetadata(planMeta);
										
										parameters = mapper
												.convertValue(
														plan.get("schemas").get("service_instance").get("create")
																.get("parameters"),
														new TypeReference<Map<String, String>>() {
														});
										create.setParameters(parameters);
									} catch (Exception e) {
										logger.info("This Plan is Error1");
									}

									instanceSchema.setCreate(create);
									planSchema.setService_instance(instanceSchema);
									servicePlan.setSchemas(planSchema);
									planList.add(servicePlan);
								} catch (Exception e) {
									logger.info("This Plan is Error2");
								}
							}
						}
					} else {
						ServicePlan servicePlan = new ServicePlan();
						servicePlan.setId(template.get("metadata").get("name").asText() + "-plan-default");
						servicePlan.setName(template.get("metadata").get("name").asText() + "-plan-default");
						servicePlan.setDescription(template.get("metadata").get("name").asText() + "-plan-default");
						servicePlan.setBindable(false);
						planList.add(servicePlan);
					}
				} catch (Exception e) {
					logger.info("This Plan is Empty");
				}
				logger.info("Catalog Debug 5");
				service.setPlans(planList);
				serviceList.add(service);
			}
			catalog.setServices(serviceList);
		}
		return catalog;
	}

	public static Object createTemplateInstance(String instanceId, ProvisionInDO inDO, String instanceName,
			String instanceUid) throws Exception {
		Object response = null;
		TemplateInstance instance = new TemplateInstance();
		Metadata instanceMeta = new Metadata();
		Metadata templateMeta = new Metadata();
		TemplateInstanceSpec spec = new TemplateInstanceSpec();
		TemplateInstanceSpecTemplate template = new TemplateInstanceSpecTemplate();
		List<TemplateParameter> parameters = new ArrayList<TemplateParameter>();

		List<V1OwnerReference> ownerRefs = new ArrayList<>();
		V1OwnerReference ownerRef = new V1OwnerReference();

		ownerRef.setApiVersion(Constants.SERVICE_INSTANCE_API_VERSION);
		ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
		ownerRef.setController(Boolean.TRUE);
		ownerRef.setKind(Constants.SERVICE_INSTANCE_KIND);
		ownerRef.setName(instanceName);
		ownerRef.setUid(instanceUid);
		ownerRefs.add(ownerRef);
		instanceMeta.setOwnerReferences(ownerRefs);

		logger.info("Service Instance Namespace : " + inDO.getContext().getNamespace());

		try {
			instance.setApiVersion(Constants.CUSTOM_OBJECT_GROUP + "/" + Constants.CUSTOM_OBJECT_VERSION);
			instance.setKind(Constants.CUSTOM_OBJECT_KIND_TEMPLATE_INSTANCE);
			instanceMeta.setName(instanceName + "." + instanceId);
			instanceMeta.setNamespace(inDO.getContext().getNamespace());
			instance.setMetadata(instanceMeta);

			templateMeta.setName(inDO.getService_id());
			template.setMetadata(templateMeta);

			Map<String,String> inputParameters = new HashMap<>();
			
			String planName = inDO.getPlan_id(); 
			
			try {
				logger.info("Get Plan Prameters : " + planName);
				Object planResponse = customObjectApi.getNamespacedCustomObject("servicecatalog.k8s.io", "v1beta1", inDO.getContext().getNamespace(),"serviceplans", planName);
				GetPlanDO plan = mapper.readValue(gson.toJson(planResponse), GetPlanDO.class);
						  
				if(plan.getSpec().getInstanceCreateParameterSchema() != null) {
					for(String key : plan.getSpec().getInstanceCreateParameterSchema().keySet()) {
						
						logger.info("Plan Prameter Key : " + key);
						logger.info("Plan Prameter Value : " + plan.getSpec().getInstanceCreateParameterSchema().get(key));
						
						if (!inputParameters.containsKey(key)) {
							inputParameters.put(key, plan.getSpec().getInstanceCreateParameterSchema().get(key));
						}
					}
				}
			} catch (ApiException e) {
				logger.info("Response body: " + e.getResponseBody());
				e.printStackTrace();
				throw e;
			} catch (Exception e) {
				logger.info("Exception message: " + e.getMessage());
				e.printStackTrace();
				throw e;
			}
			
			if (inDO.getParameters() != null) {
				for (String key : inDO.getParameters().keySet()) {
					if (!inputParameters.containsKey(key)) {
						inputParameters.put(key, inDO.getParameters().get(key));
					}
				}
			}
			
			for (String key : inputParameters.keySet()) {
				logger.info("Template Instance Prameter Key : " + key);
				logger.info("Template Instance Prameter Value : " + inputParameters.get(key));
				
				TemplateParameter parameter = new TemplateParameter();
				parameter.setName(key);
				parameter.setValue(inputParameters.get(key));
				parameters.add(parameter);
			}
			template.setParameters(parameters);
			
			spec.setTemplate(template);
			instance.setSpec(spec);

			JSONParser parser = new JSONParser();
			JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(instance));

			response = customObjectApi.createNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, inDO.getContext().getNamespace(),
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, bodyObj, null);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return response;
	}

	public static BindingOutDO insertBindingSecret(String instanceId, String bindingId, BindingInDO inDO)
			throws Exception {
		BindingOutDO outDO = new BindingOutDO();
		Map<String, Object> secretMap = new HashMap<String, Object>();
		logger.info(" Binding Namespace : " + inDO.getContext().getNamespace());
		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, inDO.getContext().getNamespace(),
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceId);

			TemplateInstance templateInstance = mapper.readValue(gson.toJson(response), TemplateInstance.class);
			List<Object> objects = templateInstance.getSpec().getTemplate().getObjects();

			for (Object object : objects) {
				JSONObject objectJson = (JSONObject) JSONValue.parse(gson.toJson(object));
				JSONObject metadataJson = (JSONObject) JSONValue.parse(objectJson.get("metadata").toString());

				String name = metadataJson.get("name").toString();
				String namespace = "default";
				if (metadataJson.get("namespace") != null) {
					namespace = metadataJson.get("namespace").toString();
				}

				if (objectJson.get("kind").toString().equals("Service")) {
					List<Endpoint> endPointList = new ArrayList<Endpoint>();
					V1Service service = api.readNamespacedService(name, namespace, null, null, null);
					if (service.getSpec().getType().equals("LoadBalancer")) {
						for (V1LoadBalancerIngress ip : service.getStatus().getLoadBalancer().getIngress()) {
							Endpoint endPoint = new Endpoint();
							List<String> ports = new ArrayList<String>();
							endPoint.setHost(ip.getIp());
							secretMap.put("instance-ip", ip.getIp());

							for (V1ServicePort port : service.getSpec().getPorts()) {
								ports.add(String.valueOf(port.getPort()));
							}
							endPoint.setPorts(ports);
							secretMap.put("instance-port", ports);
							endPointList.add(endPoint);
						}
						outDO.setEndpoints(endPointList);
					}
				} else if (objectJson.get("kind").toString().equals("Secret")) {
					V1Secret secret = api.readNamespacedSecret(name, namespace, null, null, null);
					Map<String, byte[]> data = secret.getData();
					for (String key : data.keySet()) {
						secretMap.put(key, new String(data.get(key)));
					}

					outDO.setCredentials(secretMap);
				}
			}
			return outDO;
		} catch (ApiException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static JsonNode numberTypeConverter(JsonNode jsonNode) {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;

			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();

			while (iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				entry.setValue(numberTypeConverter(entry.getValue()));
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;
			for (int i = 0; i < arrayNode.size(); i++) {
				arrayNode.set(i, numberTypeConverter(arrayNode.get(i)));
			}
		} else if (jsonNode.isValueNode()) {
			if (jsonNode.isDouble() && jsonNode.canConvertToInt()) {
				IntNode intNode = new IntNode(jsonNode.asInt());
				jsonNode = intNode;
			}
		}
		return jsonNode;
	}

	private static JsonNode objectToJsonNode(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new GsonBuilder().create();
		String objectStr = gson.toJson(object);
		JsonNode resultNode = null;
		try {
			resultNode = mapper.readTree(objectStr);
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		return resultNode;
	}

	public static String getUid(String namespace, String name) {
		String uid = "";
		try {
			Object result = customObjectApi.getNamespacedCustomObject(Constants.SERVICE_INSTANCE_API_GROUP,
					Constants.SERVICE_INSTANCE_API_VERSION, namespace, Constants.SERVICE_INSTANCE_PLURAL, name);

			String jsonString = gson.toJson(result);
			logger.info(jsonString);
			JsonParser parser = new JsonParser();
			uid = parser.parse(jsonString).getAsJsonObject().get("metadata").getAsJsonObject().get("uid").getAsString();
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return uid;
	}
}