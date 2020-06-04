package k8s.example.client.k8s;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.ProgressResponseBody;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1HTTPIngressPath;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1HTTPIngressRuleValue;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1Ingress;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressBackend;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressList;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressRule;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressSpec;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressTLS;
import io.kubernetes.client.openapi.models.V1ClusterRole;
import io.kubernetes.client.openapi.models.V1ClusterRoleBinding;
import io.kubernetes.client.openapi.models.V1ClusterRoleBindingList;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1ExecAction;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1HTTPHeader;
import io.kubernetes.client.openapi.models.V1Handler;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1LabelSelectorRequirement;
import io.kubernetes.client.openapi.models.V1Lifecycle;
import io.kubernetes.client.openapi.models.V1ListMeta;
import io.kubernetes.client.openapi.models.V1LoadBalancerIngress;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1PolicyRule;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ReplicaSet;
import io.kubernetes.client.openapi.models.V1ReplicaSetBuilder;
import io.kubernetes.client.openapi.models.V1ReplicaSetList;
import io.kubernetes.client.openapi.models.V1ReplicaSetSpec;
import io.kubernetes.client.openapi.models.V1ResourceQuota;
import io.kubernetes.client.openapi.models.V1ResourceQuotaSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1Role;
import io.kubernetes.client.openapi.models.V1RoleBinding;
import io.kubernetes.client.openapi.models.V1RoleBindingList;
import io.kubernetes.client.openapi.models.V1RoleRef;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.openapi.models.V1Subject;
import io.kubernetes.client.openapi.models.V1Toleration;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeDevice;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.util.Config;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.Client;
import k8s.example.client.DataObject.ClientCR;
import k8s.example.client.DataObject.RegistryEvent;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.DataObject.User;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.DataObject.UserSecurityPolicyCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.StringUtil;
import k8s.example.client.Util;
import k8s.example.client.interceptor.ChunkedInterceptor;
import k8s.example.client.k8s.apis.CustomResourceApi;
import k8s.example.client.k8s.util.SecurityHelper;
import k8s.example.client.metering.util.UIDGenerator;
import k8s.example.client.models.BindingInDO;
import k8s.example.client.models.BindingOutDO;
import k8s.example.client.models.CommandExecOut;
import k8s.example.client.models.Cost;
import k8s.example.client.models.Endpoint;
import k8s.example.client.models.GetPlanDO;
import k8s.example.client.models.Image;
import k8s.example.client.models.ImageSpec;
import k8s.example.client.models.InputParametersSchema;
import k8s.example.client.models.Metadata;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.PlanMetadata;
import k8s.example.client.models.ProvisionInDO;
import k8s.example.client.models.Registry;
import k8s.example.client.models.RegistryCondition;
import k8s.example.client.models.RegistryPVC;
import k8s.example.client.models.RegistryPVC.CreatePvc;
import k8s.example.client.models.RegistryService;
import k8s.example.client.models.RegistryStatus;
import k8s.example.client.models.RoleBindingClaim;
import k8s.example.client.models.Schemas;
import k8s.example.client.models.ServiceInstanceSchema;
import k8s.example.client.models.ServiceMetadata;
import k8s.example.client.models.ServiceOffering;
import k8s.example.client.models.ServicePlan;
import k8s.example.client.models.Services;
import k8s.example.client.models.TemplateInstance;
import k8s.example.client.models.TemplateInstanceSpec;
import k8s.example.client.models.TemplateInstanceSpecTemplate;
import k8s.example.client.models.TemplateParameter;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class K8sApiCaller {
	private static ApiClient k8sClient;
	private static CoreV1Api api;
	private static AppsV1Api appApi;
	private static RbacAuthorizationV1Api rbacApi;
	private static CustomObjectsApi customObjectApi;
	private static CustomResourceApi templateApi;
	private static ExtensionsV1beta1Api extentionApi;
	private static ObjectMapper mapper = new ObjectMapper();
	private static Gson gson = new GsonBuilder().create();

	private static long time = System.currentTimeMillis();

	private static Logger logger = Main.logger;

	public static void initK8SClient() throws Exception {
		k8sClient = Config.fromCluster();
		//k8sClient.setHttpClient(getHttpClient()); // set network interceptors
		k8sClient.setConnectTimeout(0);
		k8sClient.setReadTimeout(0);
		k8sClient.setWriteTimeout(0);
		Configuration.setDefaultApiClient(k8sClient);

		api = new CoreV1Api();
		appApi = new AppsV1Api();
		rbacApi = new RbacAuthorizationV1Api();
		customObjectApi = new CustomObjectsApi();
		templateApi = new CustomResourceApi();
		extentionApi = new ExtensionsV1beta1Api();
	}

	public static void startWatcher() throws Exception {
		// Get latest resource version
		logger.info("Get latest resource version");

		// registry replicaSet
		int registryReplicaSetLatestResourceVersion = 0;

		try {

			V1ReplicaSetList registryReplicaSetList = appApi.listReplicaSetForAllNamespaces(null, null, null,
					"app=registry", null, null, null, null, Boolean.FALSE);
			for (V1ReplicaSet replicaSet : registryReplicaSetList.getItems()) {
				int registryReplicaSetResourceVersion = Integer.parseInt(replicaSet.getMetadata().getResourceVersion());
				registryReplicaSetLatestResourceVersion = (registryReplicaSetLatestResourceVersion >= registryReplicaSetResourceVersion)
						? registryReplicaSetLatestResourceVersion
						: registryReplicaSetResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry Replica Set Latest resource version: " + registryReplicaSetLatestResourceVersion);

		// registry pod
		int registryPodLatestResourceVersion = 0;

		try {

			V1PodList registryPodList = api.listPodForAllNamespaces(null, null, null, "app=registry", null, null, null,
					null, Boolean.FALSE);
			for (V1Pod pod : registryPodList.getItems()) {
				int registryPodResourceVersion = Integer.parseInt(pod.getMetadata().getResourceVersion());
				registryPodLatestResourceVersion = (registryPodLatestResourceVersion >= registryPodResourceVersion)
						? registryPodLatestResourceVersion
						: registryPodResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry Pod Latest resource version: " + registryPodLatestResourceVersion);

		// registry service
		int registryServiceLatestResourceVersion = 0;

		try {

			V1ServiceList registryServiceList = api.listServiceForAllNamespaces(null, null, null, "app=registry", null,
					null, null, null, Boolean.FALSE);
			for (V1Service service : registryServiceList.getItems()) {
				int registryServiceResourceVersion = Integer.parseInt(service.getMetadata().getResourceVersion());
				registryServiceLatestResourceVersion = (registryServiceLatestResourceVersion >= registryServiceResourceVersion)
						? registryServiceLatestResourceVersion
						: registryServiceResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry Service Latest resource version: " + registryServiceLatestResourceVersion);

		// registry certSecret
		int registryCertSecretLatestResourceVersion = 0;

		try {

			V1SecretList registryCertSecretList = api.listSecretForAllNamespaces(null, null, null, "secret=cert", null,
					null, null, null, Boolean.FALSE);
			for (V1Secret certSecret : registryCertSecretList.getItems()) {
				int registryCertSecretResourceVersion = Integer.parseInt(certSecret.getMetadata().getResourceVersion());
				registryCertSecretLatestResourceVersion = (registryCertSecretLatestResourceVersion >= registryCertSecretResourceVersion)
						? registryCertSecretLatestResourceVersion
						: registryCertSecretResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry CertSecret Latest resource version: " + registryCertSecretLatestResourceVersion);

		// registry dockerSecret
		int registryDockerSecretLatestResourceVersion = 0;

		try {

			V1SecretList registryDockerSecretList = api.listSecretForAllNamespaces(null, null, null, "secret=docker",
					null, null, null, null, Boolean.FALSE);
			for (V1Secret dockerSecret : registryDockerSecretList.getItems()) {
				int registryDockerSecretResourceVersion = Integer
						.parseInt(dockerSecret.getMetadata().getResourceVersion());
				registryDockerSecretLatestResourceVersion = (registryDockerSecretLatestResourceVersion >= registryDockerSecretResourceVersion)
						? registryDockerSecretLatestResourceVersion
						: registryDockerSecretResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry DockerSecret Latest resource version: " + registryDockerSecretLatestResourceVersion);

		// registry tlsSecret
		int registryTlsSecretLatestResourceVersion = 0;

		try {

			V1SecretList registryTlsSecretList = api.listSecretForAllNamespaces(null, null, null, "secret=tls",
					null, null, null, null, Boolean.FALSE);
			for (V1Secret tlsSecret : registryTlsSecretList.getItems()) {
				int registryTlsSecretResourceVersion = Integer
						.parseInt(tlsSecret.getMetadata().getResourceVersion());
				registryTlsSecretLatestResourceVersion = (registryTlsSecretLatestResourceVersion >= registryTlsSecretResourceVersion)
						? registryTlsSecretLatestResourceVersion
								: registryTlsSecretResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry TlsSecret Latest resource version: " + registryTlsSecretLatestResourceVersion);

		// registry ingress
		int registryIngressLatestResourceVersion = 0;

		try {
			ExtensionsV1beta1IngressList registryIngressList = extentionApi.listIngressForAllNamespaces(null, null, null, "app=registry",
					null, null, null, null, Boolean.FALSE);
			for (ExtensionsV1beta1Ingress ingress : registryIngressList.getItems()) {
				int registryIngressResourceVersion = Integer
						.parseInt(ingress.getMetadata().getResourceVersion());
				registryIngressLatestResourceVersion = (registryIngressLatestResourceVersion >= registryIngressResourceVersion)
						? registryIngressLatestResourceVersion
								: registryIngressResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry Ingress Latest resource version: " + registryIngressLatestResourceVersion);

		long userLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_USER, false);
		logger.info("User Latest resource version: " + userLatestResourceVersion);
		long registryLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, true);
		logger.info("Registry Latest resource version: " + registryLatestResourceVersion);
		long imageLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_IMAGE, true);
		logger.info("Image Latest resource version: " + imageLatestResourceVersion);
		long templateLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, true);
		logger.info("Template Latest resource version: " + templateLatestResourceVersion);
		long instanceLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE,
				true);
		logger.info("Instance Latest resource version: " + instanceLatestResourceVersion);
		long nscLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, false);
		logger.info("Namespace Claim Latest resource version: " + nscLatestResourceVersion);
		long rqcLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM,
				true);
		logger.info("ResourceQuota Claim Latest resource version: " + rqcLatestResourceVersion);
		long rbcLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM, true);
		logger.info("RoleBinding Claim Latest resource version: " + rbcLatestResourceVersion);
		
		long cscLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_CATALOGSERVICECLAIM, true);
		logger.info("Catalog Service Claim Latest resource version: " + cscLatestResourceVersion);

		// Init Registry Image
		initializeImageList();

		// Start user watch
		logger.info("Start user watcher");
		UserWatcher userWatcher = new UserWatcher(k8sClient, customObjectApi,
				String.valueOf(userLatestResourceVersion));
		userWatcher.start();

		// Start userDelete watch
		logger.info("Start userDelete watcher");
		UserDeleteWatcher userDeleteWatcher = new UserDeleteWatcher(k8sClient, customObjectApi,
				String.valueOf(userLatestResourceVersion));
		userDeleteWatcher.start();

		// Start registry watch
		logger.info("Start registry watcher");
		RegistryWatcher registryWatcher = new RegistryWatcher(k8sClient, customObjectApi,
				String.valueOf(registryLatestResourceVersion));
		registryWatcher.start();

		// Start registry replicaSet watch
		logger.info("Start registry replica set watcher");
		RegistryReplicaSetWatcher registryReplicaSetWatcher = new RegistryReplicaSetWatcher(k8sClient, appApi,
				String.valueOf(registryReplicaSetLatestResourceVersion));
		registryReplicaSetWatcher.start();

		// Start registry pod watch
		logger.info("Start registry pod watcher");
		RegistryPodWatcher registryPodWatcher = new RegistryPodWatcher(k8sClient, api,
				String.valueOf(registryPodLatestResourceVersion));
		registryPodWatcher.start();

		// Start registry service watch
		logger.info("Start registry service watcher");
		RegistryServiceWatcher registryServiceWatcher = new RegistryServiceWatcher(k8sClient, api,
				String.valueOf(registryServiceLatestResourceVersion));
		registryServiceWatcher.start();

		// Start registry cert secret watch
		logger.info("Start registry cert secret watcher");
		RegistryCertSecretWatcher registryCertSecretWatcher = new RegistryCertSecretWatcher(k8sClient, api,
				String.valueOf(registryCertSecretLatestResourceVersion));
		registryCertSecretWatcher.start();

		// Start registry docker secret watch
		logger.info("Start registry docker secret watcher");
		RegistryDockerSecretWatcher registryDockerSecretWatcher = new RegistryDockerSecretWatcher(k8sClient, api,
				String.valueOf(registryDockerSecretLatestResourceVersion));
		registryDockerSecretWatcher.start();

		// Start registry tls secret watch
		logger.info("Start registry tls secret watcher");
		RegistryTlsSecretWatcher registryTlsSecretWatcher = new RegistryTlsSecretWatcher(k8sClient, api,
				String.valueOf(registryTlsSecretLatestResourceVersion));
		registryTlsSecretWatcher.start();

		// Start registry ingress watch
		logger.info("Start registry ingress watcher");
		RegistryIngressWatcher registryIngressWatcher = new RegistryIngressWatcher(k8sClient, extentionApi,
				String.valueOf(registryIngressLatestResourceVersion));
		registryIngressWatcher.start();

		// Start image watch
		logger.info("Start image watcher");
		ImageWatcher imageWatcher = new ImageWatcher(k8sClient, customObjectApi,
				String.valueOf(imageLatestResourceVersion));
		imageWatcher.start();

		// Start Operator
		logger.info("Start Template Operator");
		TemplateOperator templateOperator = new TemplateOperator(k8sClient, templateApi, templateLatestResourceVersion);
		templateOperator.start();

		logger.info("Start Instance Operator");
		InstanceOperator instanceOperator = new InstanceOperator(k8sClient, templateApi, instanceLatestResourceVersion);
		instanceOperator.start();

		// Start NamespaceClaim Controller
		logger.info("Start NamespaceClaim Controller");
		NamespaceClaimController nscOperator = new NamespaceClaimController(k8sClient, customObjectApi,
				nscLatestResourceVersion);
		nscOperator.start();

		// Start ResourceQuotaClaim Controller
		logger.info("Start ResourceQuotaClaim Controller");
		ResourceQuotaClaimController rqcOperator = new ResourceQuotaClaimController(k8sClient, customObjectApi,
				rqcLatestResourceVersion);
		rqcOperator.start();

		// Start RoleBindingClaim Controller
		logger.info("Start RoleBindingClaim Controller");
		RoleBindingClaimController rbcOperator = new RoleBindingClaimController(k8sClient, customObjectApi,
				rbcLatestResourceVersion);
		rbcOperator.start();
		
		// Start CatalogServiceClaim Controller
		logger.info("Start CatalogServiceClaim Controller");
		CatalogServiceClaimController cscOperator = new CatalogServiceClaimController(k8sClient, customObjectApi,
				cscLatestResourceVersion);
		cscOperator.start();

		while (true) {
			try {
				if (!userWatcher.isAlive()) {
					String userLatestResourceVersionStr = UserWatcher.getLatestResourceVersion();
					logger.info("User watcher is not alive. Restart user watcher! (Latest resource version: "
							+ userLatestResourceVersionStr + ")");
					userWatcher.interrupt();
					userWatcher = new UserWatcher(k8sClient, customObjectApi, userLatestResourceVersionStr);
					userWatcher.start();
				}

				if (!userDeleteWatcher.isAlive()) {
					String userLatestResourceVersionStr = UserWatcher.getLatestResourceVersion();
					logger.info(
							"User Delete watcher is not alive. Restart user delete watcher! (Latest resource version: "
									+ userLatestResourceVersionStr + ")");
					userDeleteWatcher.interrupt();
					userDeleteWatcher = new UserDeleteWatcher(k8sClient, customObjectApi, userLatestResourceVersionStr);
					userDeleteWatcher.start();
				}

				if (!registryWatcher.isAlive()) {
					String registryLatestResourceVersionStr = RegistryWatcher.getLatestResourceVersion();
					logger.info("Registry watcher is not alive. Restart registry watcher! (Latest resource version: "
							+ registryLatestResourceVersionStr + ")");
					registryWatcher.interrupt();
					registryWatcher = new RegistryWatcher(k8sClient, customObjectApi, registryLatestResourceVersionStr);
					registryWatcher.start();
				}

				if (!registryReplicaSetWatcher.isAlive()) {
					String registryReplicaSetLatestResourceVersionStr = RegistryReplicaSetWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry replicaSet watcher is not alive. Restart registry replica set watcher! (Latest resource version: "
									+ registryReplicaSetLatestResourceVersionStr + ")");
					registryReplicaSetWatcher.interrupt();
					registryReplicaSetWatcher = new RegistryReplicaSetWatcher(k8sClient, appApi,
							registryReplicaSetLatestResourceVersionStr);
					registryReplicaSetWatcher.start();
				}

				if (!registryPodWatcher.isAlive()) {
					String registryPodLatestResourceVersionStr = RegistryPodWatcher.getLatestResourceVersion();
					logger.info(
							"Registry pod watcher is not alive. Restart registry pod watcher! (Latest resource version: "
									+ registryPodLatestResourceVersionStr + ")");
					registryPodWatcher.interrupt();
					registryPodWatcher = new RegistryPodWatcher(k8sClient, api, registryPodLatestResourceVersionStr);
					registryPodWatcher.start();
				}

				if (!registryServiceWatcher.isAlive()) {
					String registryServiceLatestResourceVersionStr = RegistryServiceWatcher.getLatestResourceVersion();
					logger.info(
							"Registry service watcher is not alive. Restart registry service watcher! (Latest resource version: "
									+ registryServiceLatestResourceVersionStr + ")");
					registryServiceWatcher.interrupt();
					registryServiceWatcher = new RegistryServiceWatcher(k8sClient, api,
							registryServiceLatestResourceVersionStr);
					registryServiceWatcher.start();
				}

				if (!registryCertSecretWatcher.isAlive()) {
					String registryCertSecretLatestResourceVersionStr = RegistryCertSecretWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry cert secret watcher is not alive. Restart registry cert secret watcher! (Latest resource version: "
									+ registryCertSecretLatestResourceVersionStr + ")");
					registryCertSecretWatcher.interrupt();
					registryCertSecretWatcher = new RegistryCertSecretWatcher(k8sClient, api,
							registryCertSecretLatestResourceVersionStr);
					registryCertSecretWatcher.start();
				}

				if (!registryDockerSecretWatcher.isAlive()) {
					String registryDockerSecretLatestResourceVersionStr = RegistryDockerSecretWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry docker secret watcher is not alive. Restart registry docker secret watcher! (Latest resource version: "
									+ registryDockerSecretLatestResourceVersionStr + ")");
					registryDockerSecretWatcher.interrupt();
					registryDockerSecretWatcher = new RegistryDockerSecretWatcher(k8sClient, api,
							registryDockerSecretLatestResourceVersionStr);
					registryDockerSecretWatcher.start();
				}
				
				if (!registryTlsSecretWatcher.isAlive()) {
					String registryTlsSecretLatestResourceVersionStr = RegistryTlsSecretWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry tls secret watcher is not alive. Restart registry tls secret watcher! (Latest resource version: "
									+ registryTlsSecretLatestResourceVersionStr + ")");
					registryTlsSecretWatcher.interrupt();
					registryTlsSecretWatcher = new RegistryTlsSecretWatcher(k8sClient, api,
							registryTlsSecretLatestResourceVersionStr);
					registryTlsSecretWatcher.start();
				}
				
				if (!registryIngressWatcher.isAlive()) {
					String registryIngressLatestResourceVersionStr = RegistryIngressWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry ingress watcher is not alive. Restart registry ingress watcher! (Latest resource version: "
									+ registryIngressLatestResourceVersionStr + ")");
					registryIngressWatcher.interrupt();
					registryIngressWatcher = new RegistryIngressWatcher(k8sClient, extentionApi,
							registryIngressLatestResourceVersionStr);
					registryIngressWatcher.start();
				}

				if (!imageWatcher.isAlive()) {
					String imageLatestResourceVersionStr = ImageWatcher.getLatestResourceVersion();
					logger.info("Image watcher is not alive. Restart image watcher! (Latest resource version: "
							+ imageLatestResourceVersionStr + ")");
					imageWatcher.interrupt();
					imageWatcher = new ImageWatcher(k8sClient, customObjectApi, imageLatestResourceVersionStr);
					imageWatcher.start();
				}

				if (!templateOperator.isAlive()) {
					templateLatestResourceVersion = TemplateOperator.getLatestResourceVersion();
					logger.info(("Template Operator is not Alive. Restart Operator! (Latest Resource Version: "
							+ templateLatestResourceVersion + ")"));
					templateOperator.interrupt();
					templateOperator = new TemplateOperator(k8sClient, templateApi, templateLatestResourceVersion);
					templateOperator.start();
				}

				if (!instanceOperator.isAlive()) {
					instanceLatestResourceVersion = InstanceOperator.getLatestResourceVersion();
					logger.info(("Instance Operator is not Alive. Restart Operator! (Latest Resource Version: "
							+ instanceLatestResourceVersion + ")"));
					instanceOperator.interrupt();
					instanceOperator = new InstanceOperator(k8sClient, templateApi, instanceLatestResourceVersion);
					instanceOperator.start();
				}

				if (!nscOperator.isAlive()) {
					nscLatestResourceVersion = NamespaceClaimController.getLatestResourceVersion();
					logger.info(
							("Namespace Claim Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ nscLatestResourceVersion + ")"));
					nscOperator.interrupt();
					nscOperator = new NamespaceClaimController(k8sClient, customObjectApi, nscLatestResourceVersion);
					nscOperator.start();
				}

				if (!rqcOperator.isAlive()) {
					rqcLatestResourceVersion = ResourceQuotaClaimController.getLatestResourceVersion();
					logger.info(
							("ResourceQuota Claim Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ rqcLatestResourceVersion + ")"));
					rqcOperator.interrupt();
					rqcOperator = new ResourceQuotaClaimController(k8sClient, customObjectApi,
							rqcLatestResourceVersion);
					rqcOperator.start();
				}

				if (!rbcOperator.isAlive()) {
					rbcLatestResourceVersion = RoleBindingClaimController.getLatestResourceVersion();
					logger.info(
							("RoleBinding Claim Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ rbcLatestResourceVersion + ")"));
					rbcOperator.interrupt();
					rbcOperator = new RoleBindingClaimController(k8sClient, customObjectApi, rbcLatestResourceVersion);
					rbcOperator.start();
				}
				
				if (!cscOperator.isAlive()) {
					cscLatestResourceVersion = CatalogServiceClaimController.getLatestResourceVersion();
					logger.info(
							("CatalogService Claim Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ cscLatestResourceVersion + ")"));
					cscOperator.interrupt();
					cscOperator = new CatalogServiceClaimController(k8sClient, customObjectApi, cscLatestResourceVersion);
					cscOperator.start();
				}
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.info(sw.toString());
			}

			Thread.sleep(10000); // Period: 10 sec
		}

	}

	public static UserCR getUser(String userName) throws Exception {
		UserCR user = new UserCR();

		try {
			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_USER, userName);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
//
//            V1ObjectMeta metadata = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("metadata")), V1ObjectMeta.class);
//
//            User userInfo = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("userInfo")), User.class);
//            user.setMetadata(metadata);
//
//            user.setUserInfo(userInfo);
//            user.setStatus(respJson.get("status").toString());

			mapper.registerModule(new JodaModule());
			user = mapper.readValue((new Gson()).toJson(respJson), new TypeReference<UserCR>() {
			});
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return user;
	}

	public static void updateUserMeta(User userInfo, boolean retryCountFlag) throws Exception {
		try {
			List < UserCR > userCRList = null;
			String jsonPatchStr = "[";
			
			if ( !retryCountFlag ) {
				jsonPatchStr = jsonPatchStr + "{\"op\":\"replace\",\"path\":\"/userInfo/dateOfBirth\",\"value\": \"" + userInfo.getDateOfBirth() + "\"}";
				if (userInfo.getName() != null) jsonPatchStr = jsonPatchStr + ", {\"op\":\"replace\",\"path\":\"/userInfo/name\",\"value\": " + userInfo.getName() + "}";
				if (userInfo.getDepartment() != null) jsonPatchStr = jsonPatchStr + ", {\"op\":\"replace\",\"path\":\"/userInfo/department\",\"value\": " + userInfo.getDepartment() + "}";
				if (userInfo.getPosition() != null) jsonPatchStr = jsonPatchStr + ", {\"op\":\"replace\",\"path\":\"/userInfo/position\",\"value\": " + userInfo.getPosition() + "}";
				if (userInfo.getPhone() != null) jsonPatchStr = jsonPatchStr + ", {\"op\":\"replace\",\"path\":\"/userInfo/phone\",\"value\": " + userInfo.getPhone() + "}";
				if (userInfo.getDescription() != null) jsonPatchStr = jsonPatchStr + ", {\"op\":\"replace\",\"path\":\"/userInfo/description\",\"value\": " + userInfo.getDescription() + "}";
				if (userInfo.getProfile() != null) jsonPatchStr = jsonPatchStr + ", {\"op\":\"replace\",\"path\":\"/userInfo/profile\",\"value\": " + userInfo.getProfile() + "}";
				if (userInfo.getEmail() != null) {
		    		userCRList = listUser();
		    		if ( userCRList != null ) {
		        		for(UserCR userCR : userCRList) {
		        			User user = userCR.getUserInfo();
		        			if ( user.getEmail().equalsIgnoreCase(userInfo.getEmail())) throw new Exception(ErrorCode.USER_MAIL_DUPLICATED);
		        		}
		    		}
					jsonPatchStr = jsonPatchStr + ", {\"op\":\"replace\",\"path\":\"/userInfo/email\",\"value\": " + userInfo.getEmail() + "}";
				}
			} else {
				jsonPatchStr = jsonPatchStr + "{\"op\":\"replace\",\"path\":\"/userInfo/retryCount\",\"value\": " + userInfo.getRetryCount() + "}";
			}
			
			jsonPatchStr = jsonPatchStr + "]";
			
			logger.info("jsonPatchStr: " + jsonPatchStr);	
			JsonElement jsonPatch = (JsonElement) new JsonParser().parse(jsonPatchStr);

			customObjectApi.patchClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_USER, userInfo.getId(), jsonPatch);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static List<UserCR> listUser() throws Exception {
		List<UserCR> userList = null;
		try {
			Object response = customObjectApi.listClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_USER, null, null, null, null, null,
					null, null, Boolean.FALSE);

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			if (respJson.get("items") != null)
				userList = mapper.readValue((new Gson()).toJson(respJson.get("items")),
						new TypeReference<ArrayList<UserCR>>() {
						});

		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return userList;
	}

	public static TokenCR getToken(String tokenName) throws Exception {
		TokenCR token = new TokenCR();

		try {
			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_TOKEN, tokenName);

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
			String accessToken = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("accessToken")),
					String.class);
			String refreshToken = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("refreshToken")),
					String.class);

			token.setAccessToken(accessToken);
			token.setRefreshToken(refreshToken);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return token;
	}

	public static Client getClient(Client clientInfo) throws Exception {
		Client dbClientInfo = new Client();

		try {
			logger.info("Name of Client: " + clientInfo.getAppName() + clientInfo.getClientId());

			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_CLIENT,
					clientInfo.getAppName() + clientInfo.getClientId());

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
			JsonObject clientInfoJson = respJson.get("clientInfo").getAsJsonObject();

			dbClientInfo.setAppName(
					new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("appName")), String.class));
			dbClientInfo.setClientId(
					new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("clientId")), String.class));
			dbClientInfo.setClientSecret(new ObjectMapper()
					.readValue((new Gson()).toJson(clientInfoJson.get("clientSecret")), String.class));
			dbClientInfo.setOriginUri(
					new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("originUri")), String.class));
			dbClientInfo.setRedirectUri(
					new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("redirectUri")), String.class));

		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return dbClientInfo;
	}

	public static void deleteToken(String tokenName) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();

			customObjectApi.deleteClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_TOKEN, tokenName, 0, null, null, body);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static void deleteUser(String userName) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();

			customObjectApi.deleteClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_USER, userName, 0, null, null, body);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static void saveClient(Client clientInfo) throws Exception {

		try {
			ClientCR clientCR = new ClientCR();

			// Set name & label
			V1ObjectMeta metadata = new V1ObjectMeta();
			metadata.setName(clientInfo.getAppName() + clientInfo.getClientId());
//        	Map<String, String> label = new HashMap<>();   있어야할지 판단 안됨
//        	label.put("client", );
			clientCR.setMetadata(metadata);

			// Set ClientInfo
			clientCR.setClientInfo(clientInfo);

			// Make body
			JSONParser parser = new JSONParser();
			JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(clientCR));

			customObjectApi.createClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_CLIENT, bodyObj, null);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static void saveToken(String userId, String tokenId, String accessToken, String refreshToken)
			throws Exception {
		String tokenName = userId + "-" + tokenId;

		try {
			TokenCR token = new TokenCR();

			// Set name & label
			V1ObjectMeta metadata = new V1ObjectMeta();
			metadata.setName(tokenName);
			Map<String, String> label = new HashMap<>();
			label.put("user", userId);
			metadata.setLabels(label);
			token.setMetadata(metadata);

			// Set tokens
			token.setAccessToken(accessToken);
			token.setRefreshToken(refreshToken);

			// Make body
			JSONParser parser = new JSONParser();
			JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(token));

			customObjectApi.createClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_TOKEN, bodyObj, null);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static void updateAccessToken(String tokenName, String accessToken) throws Exception {
		try {
			String jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/accessToken\",\"value\": " + accessToken + "}]";
			JsonElement jsonPatch = (JsonElement) new JsonParser().parse(jsonPatchStr);

			customObjectApi.patchClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_TOKEN, tokenName, jsonPatch);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static void encryptUserPassword(String userId, String password, UserCR user) throws Exception {
		try {
			// Encrypt password
			String passwordSalt = UUID.randomUUID().toString();
			String encryptedPassword = Util.Crypto
					.encryptSHA256(password + userId + passwordSalt);

			// Patch user CR
			user.getUserInfo().setPassword(encryptedPassword);
			user.getUserInfo().setPasswordSalt(passwordSalt);
			Map<String, String> label = user.getMetadata().getLabels();
			label.put("encrypted", "t");
			user.getMetadata().setLabels(label);

			customObjectApi.replaceClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_USER, userId, user);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static void initRegistry(String registryId, Registry registry) throws ApiException {
		logger.info("[K8S ApiCaller] initRegistry(String, Registry) Start");

		String namespace = registry.getMetadata().getNamespace();

		JSONObject patchStatus = new JSONObject();
		JSONObject status = new JSONObject();
		JSONArray conditions = new JSONArray();
		JSONObject condition1 = new JSONObject();
		JSONObject condition2 = new JSONObject();
		JSONObject condition3 = new JSONObject();
		JSONObject condition4 = new JSONObject();
		JSONObject condition5 = new JSONObject();
		JSONObject condition6 = new JSONObject();
		JSONObject condition7 = new JSONObject();
		JSONObject condition8 = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();

		condition1.put("type", RegistryCondition.Condition.REPLICA_SET.getType());
		condition1.put("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition1);

		condition2.put("type", RegistryCondition.Condition.POD.getType());
		condition2.put("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition2);

		condition3.put("type", RegistryCondition.Condition.CONTAINER.getType());
		condition3.put("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition3);

		condition4.put("type", RegistryCondition.Condition.SERVICE.getType());
		condition4.put("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition4);

		condition5.put("type", RegistryCondition.Condition.SECRET_OPAQUE.getType());
		condition5.put("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition5);

		condition6.put("type", RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getType());
		condition6.put("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition6);
		
		condition7.put("type", RegistryCondition.Condition.SECRET_TLS.getType());
		condition7.put("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition7);
		
		condition8.put("type", RegistryCondition.Condition.INGRESS.getType());
		condition8.put("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition8);

		status.put("conditions", conditions);
		status.put("phase", RegistryStatus.StatusPhase.CREATING.getStatus());
		status.put("message", "Registry is creating. All resources in registry has not yet been created.");
		status.put("reason", "RegistryNotCreated");

		patchStatus.put("op", "add");
		patchStatus.put("path", "/status");
		patchStatus.put("value", status);
		patchStatusArray.add(patchStatus);

		try {
			Object result = customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
					registry.getMetadata().getName(), patchStatusArray);
			logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}

	}

	@SuppressWarnings("unchecked")
	public static void createRegistry(Registry registry) throws Exception {
		logger.info("[K8S ApiCaller] createRegistry(Registry) Start");

		try {
			String registryId = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();
			RegistryService registryService = registry.getSpec().getService();
			RegistryPVC registryPVC = registry.getSpec().getPersistentVolumeClaim();
			String clusterIP = null;
			String lbIP = null;
			String ingressDomain = null;
			String registryDomain = null;
			String serviceType 
				= registry.getSpec().getService().getIngress() != null 
				? RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;
			
			
			// set default
			int registrySVCTargetPort = RegistryService.REGISTRY_TARGET_PORT;
			int registrySVCPort = registrySVCTargetPort;
			logger.info("registrySVCPort: " + registrySVCPort);
			
			if( serviceType.equals(RegistryService.SVC_TYPE_INGRESS) ) {
				ingressDomain = registryService.getIngress().getDomainName();
				
				if( registryService.getIngress().getPort() != 0 ) {
					registrySVCPort = registryService.getIngress().getPort();
					logger.info("[Ingress]registrySVCPort: " + registrySVCPort);
				}
			}
			else if( serviceType.equals(RegistryService.SVC_TYPE_LOAD_BALANCER) 
					&& registryService.getLoadBalancer().getPort() != 0) {
				registrySVCPort = registryService.getLoadBalancer().getPort();
				logger.info("[LB]registrySVCPort: " + registrySVCPort);
			}

			if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
//				if( StringUtil.isEmpty(ingressDomain)) {
//					V1Service service = api.readNamespacedService("ingress-nginx", "ingress-nginx", null, null, null);
//					String ingressLbIP = null;
//
//					if (service.getSpec().getType().equals("LoadBalancer")) {
//						if (service.getStatus().getLoadBalancer().getIngress() != null
//								&& service.getStatus().getLoadBalancer().getIngress().size() == 1) {
//							if (service.getStatus().getLoadBalancer().getIngress().get(0).getHostname() == null) {
//								ingressLbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
//							} else {
//								ingressLbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getHostname();
//							}
//							logger.info("[ingress-nginx LoadBalancerIP]:" + ingressLbIP);
//
//							ingressDomain = ingressLbIP + ".nip.io";
//							logger.info("[ingressDomain]:" + ingressDomain);
//
//						}
//					}
//				}

				registryDomain = registryId + "." + ingressDomain;
				logger.info("[registryDomain]:" + registryDomain);
			}

			// ----- Create Service
			V1Service lb = new V1Service();
			V1ObjectMeta lbMeta = new V1ObjectMeta();
			V1ServiceSpec lbSpec = new V1ServiceSpec();
			List<V1ServicePort> ports = new ArrayList<>();
			Map<String, String> lbSelector = new HashMap<String, String>();

			lbMeta.setName(Constants.K8S_PREFIX + registryId);

			logger.info("<Service Label List>");
			Map<String, String> serviceLabels = new HashMap<String, String>();
			serviceLabels.put("app", "registry");
			serviceLabels.put("apps", lbMeta.getName());
			logger.info("app: registry");
			logger.info("apps: " + lbMeta.getName());
			lbMeta.setLabels(serviceLabels);

			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);

			lbMeta.setOwnerReferences(ownerRefs);

			lb.setMetadata(lbMeta);

			V1ServicePort v1port = new V1ServicePort();
			v1port.setProtocol(RegistryService.REGISTRY_PORT_PROTOCOL);
			v1port.setPort(registrySVCPort);
			v1port.setName(RegistryService.REGISTRY_PORT_NAME);
			v1port.setTargetPort(new IntOrString(registrySVCTargetPort));

			ports.add(v1port);
			lbSpec.setPorts(ports);

			logger.info("Selector: " + Constants.K8S_PREFIX + registryId + "=lb");
			lbSelector.put(Constants.K8S_PREFIX + registryId, "lb");
			lbSpec.setSelector(lbSelector);

			if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
				lbSpec.setType(RegistryService.SVC_TYPE_CLUSTER_IP);
			}
			else if(serviceType.equals(RegistryService.SVC_TYPE_LOAD_BALANCER)) {
				lbSpec.setType(RegistryService.SVC_TYPE_LOAD_BALANCER);
			}
			else {
				logger.info("Service Type Is Invalid");
				throw new Exception("Service Type Is Invalid");
			}

			lb.setSpec(lbSpec);

			try {
				api.createNamespacedService(namespace, lb, null, null, null);
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
				
				try {
					patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
							e.getResponseBody(), "CreateServiceFailed");
				} catch (ApiException e2) {
					logger.info(e2.getResponseBody());
					throw e2;
				}
				
				throw e;
			}

			int RETRY_CNT = 200;
			V1Service service = null;
			for (int i = 0; i < RETRY_CNT; i++) {
				Thread.sleep(500);
				service = api.readNamespacedService(Constants.K8S_PREFIX + registryId, namespace, null, null, null);

				clusterIP = service.getSpec().getClusterIP();
				logger.info("[ClusterIP]:" + clusterIP);

				if (service.getSpec().getType().equals(RegistryService.SVC_TYPE_LOAD_BALANCER)  
						&& service.getStatus().getLoadBalancer().getIngress() != null
						&& service.getStatus().getLoadBalancer().getIngress().size() == 1) {
					if (service.getStatus().getLoadBalancer().getIngress().get(0).getHostname() == null) {
						lbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
					} else {
						lbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getHostname();
					}
					logger.info("[LoadBalancerIP]:" + lbIP);
					break;
				}
				else if (service.getSpec().getType().equals(RegistryService.SVC_TYPE_CLUSTER_IP)) {
					logger.info("Service type is ClusterIp");
					break;
				}

				if (i == RETRY_CNT - 1) {
					try {
						patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
								"Creating a registry is failed. Service(LB) is not found", "ServiceNotFound");
					} catch (ApiException e) {
						logger.info(e.getResponseBody());
						throw e;
					}

					return;
				}
			}

			
			boolean existPvcName = (registryPVC.getExist() != null);
			String volumeMode = null;
			
			if( existPvcName ) {
				try {
					V1PersistentVolumeClaim result = api.readNamespacedPersistentVolumeClaim(registryPVC.getExist().getPvcName(), namespace, null, null, null);
					volumeMode = result.getSpec().getVolumeMode();
				}catch (ApiException e) {
//					if(e.getCode() == 404) {
//						existPvcName = false;
//					}
					try {
						patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
								"Creating a registry is failed. " + registryPVC.getExist().getPvcName() + " PVC is not found", "PVCNotFound");
					} catch (ApiException e2) {
						logger.info(e2.getResponseBody());
						throw e2;
					}
					throw e;
				}
			}

			if( !existPvcName) {
				// ----- Create PVC
				CreatePvc createPvc = registryPVC.getCreate();
				V1PersistentVolumeClaim pvc = new V1PersistentVolumeClaim();
				V1ObjectMeta pvcMeta = new V1ObjectMeta();
				V1PersistentVolumeClaimSpec pvcSpec = new V1PersistentVolumeClaimSpec();
				V1ResourceRequirements pvcResource = new V1ResourceRequirements();
				Map<String, Quantity> limit = new HashMap<>();
				List<String> accessModes = new ArrayList<>();

				
				//			String storageClassName = StringUtil.isEmpty(registryPVC.getStorageClassName()) ? RegistryPVC.STORAGE_CLASS_DEFAULT : registryPVC.getStorageClassName();
				String storageClassName = createPvc.getStorageClassName();

				pvcMeta.setName(Constants.K8S_PREFIX + registryId);

				//			Map<String, String> pvcLabels = new HashMap<String, String>();
				//			pvcLabels.put("app", Constants.K8S_PREFIX.substring(0, Constants.K8S_PREFIX.length() - 1));
				//			pvcMeta.setLabels(pvcLabels);

				boolean deleteWitchPvc = (createPvc.getDeleteWithPvc() == null ? false : createPvc.getDeleteWithPvc());
				if( deleteWitchPvc ) {
					pvcMeta.setOwnerReferences(ownerRefs);
				}

				// set storage quota.
				limit.put("storage", new Quantity(createPvc.getStorageSize()));
				pvcResource.setRequests(limit);
				pvcSpec.setResources(pvcResource);

				// set storage class name
				pvcSpec.setStorageClassName(storageClassName);

				// set access mode
				//			if(registryPVC.getAccessModes() == null || registryPVC.getAccessModes().size() == 0) {
				//				accessModes.add(RegistryPVC.ACCESS_MODE_DEFAULT);
				//			}
				//			else {
				for (String mode : createPvc.getAccessModes()) {
					accessModes.add(mode);
				}
				//			}
				pvcSpec.setAccessModes(accessModes);

				// set volume mode
				if (createPvc.getVolumeMode() != null) 
					pvcSpec.setVolumeMode(createPvc.getVolumeMode());

				pvc.setMetadata(pvcMeta);
				pvc.setSpec(pvcSpec);

				// create storage.
				try {
					api.createNamespacedPersistentVolumeClaim(namespace, pvc, null, null, null);
				} catch (ApiException e) {
					logger.info(e.getResponseBody());

					try {
						patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
								e.getResponseBody(), "CreatePVCFailed");
					} catch (ApiException e2) {
						logger.info(e2.getResponseBody());
						logger.info("resCode: " + e2.getCode());
						throw e2;
					}

					if(e.getCode() != 409) // 409: Already exist
						throw e;
				}
			}

			// ----- Create Certificate & Create Secret
			// Create Cert Directory
			logger.info("Create Cert Directory");
			String registryDir = createDirectory(namespace, registryId);

			// Create Certificates
			logger.info("Create Certificates");
			List<String> commands = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			sb.append("openssl req -newkey rsa:4096 -nodes -sha256 -keyout ");
			sb.append(registryDir + "/" + Constants.CERT_KEY_FILE);
			sb.append(" -x509 -days 1000 -subj \"/C=KR/ST=Seoul/O=tmax/CN=");
			sb.append(clusterIP);
			sb.append("\" -config <(cat /etc/ssl/openssl.cnf <(printf \"[v3_ca]\\nsubjectAltName=IP:" + clusterIP);
			if(serviceType.equals(RegistryService.SVC_TYPE_LOAD_BALANCER)) {
				sb.append(",IP:" + lbIP);
			}
			else {
				sb.append(",DNS:" + registryDomain);
			}
			sb.append("\")) -out ");
			sb.append(registryDir + "/" + Constants.CERT_CRT_FILE);
			commands.clear();
			commands.add("/bin/bash"); // bash Required!!
			commands.add("-c");
			commands.add(sb.toString());

			try {
				commandExecute(commands.toArray(new String[commands.size()]));
			} catch (Exception e) {
				logger.info(e.getMessage());
				
				try {
					patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
							e.getMessage(), "CreateRegistryFailed");
				} catch (ApiException e2) {
					logger.info(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}

			sb.setLength(0);
			sb.append("\'\'openssl x509 -inform PEM -in ");
			sb.append(registryDir + "/" + Constants.CERT_CRT_FILE);
			sb.append(" -out ");
			sb.append(registryDir + "/" + Constants.CERT_CERT_FILE);
			sb.append("\'\'");
			commands.clear();
			commands.add("/bin/sh");
			commands.add("-c");
			commands.add(sb.toString());
			try {
				commandExecute(commands.toArray(new String[commands.size()]));
			} catch (Exception e) {
				try {
					patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
							e.getMessage(), "CreateRegistryFailed");
				} catch (ApiException e2) {
					logger.info(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}

			// Read cert files & Create Secret Object
			logger.info("Read cert files & Create Secret Object"); 
			Map<String, String> secrets = new HashMap<>();

			secrets.put(Constants.CERT_KEY_FILE, readFile(registryDir + "/" + Constants.CERT_KEY_FILE));
			secrets.put(Constants.CERT_CRT_FILE, readFile(registryDir + "/" + Constants.CERT_CRT_FILE));
			secrets.put(Constants.CERT_CERT_FILE, readFile(registryDir + "/" + Constants.CERT_CERT_FILE));
			secrets.put("ID", registry.getSpec().getLoginId());
			secrets.put("PASSWD", registry.getSpec().getLoginPassword());
			secrets.put("CLUSTER_IP", clusterIP);
			
			if( lbIP != null ) 
				secrets.put("LB_IP", lbIP);
			
			if( registryDomain != null )
				secrets.put("DOMAIN_NAME", registryDomain);
			
			if( registryDomain != null) {
				secrets.put("REGISTRY_URL", registryDomain + ":" + registrySVCPort);
			}
			else if(lbIP != null) {
				secrets.put("REGISTRY_URL", lbIP + ":" + registrySVCPort);
			}
			else {
				secrets.put("REGISTRY_URL", clusterIP + ":" + registrySVCPort);
			}
			
			secrets.put("PORT", Integer.toString(registrySVCPort));
			
			Map<String, String> labels = new HashMap<>();
			labels.put("secret", "cert");
			String secretName;

			try {
				// K8SApiCall createSecret
				logger.info("K8SApiCall createSecret");
				secretName = K8sApiCaller.createSecret(namespace, secrets, registryId, labels, null, ownerRefs);
			} catch (ApiException e) {
				try {
					patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
							e.getResponseBody(), "CreateRegistryFailed");
				} catch (ApiException e2) {
					logger.info(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}

			// Create docker-config-json Secret Object
			Map<String, String> secrets2 = new HashMap<>();
			List<String> domainList = new ArrayList<>();
			
			domainList.add(clusterIP + ":" + registrySVCPort);
			if(lbIP != null)
				domainList.add(lbIP + ":" + registrySVCPort);
			if(registryDomain != null)
				domainList.add(registryDomain + ":" + registrySVCPort);
			
			secrets2.put(Constants.DOCKER_CONFIG_JSON_FILE, createConfigJson(domainList,
					registry.getSpec().getLoginId(), registry.getSpec().getLoginPassword()));

			Map<String, String> labels2 = new HashMap<>();
			labels2.put("secret", "docker");
			try {
				K8sApiCaller.createSecret(namespace, secrets2, registryId, labels2,
						Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON, ownerRefs);
			} catch (ApiException e) {
				try {
					patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
							e.getResponseBody(), "CreateRegistryFailed");
				} catch (ApiException e2) {
					logger.info(e2.getResponseBody());
					throw e2;
				}
				
				throw e;
			}

			
			// ----- Create Tls Secret & Create Ingress
			if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
				// Create Tls Secret
				String tlsSecretName = null;
				Map<String, String> tlsLabels = new HashMap<>();
				Map<String, String> tlsSecrets = new HashMap<>();
				
				tlsSecrets.put(Constants.K8S_SECRET_TLS_CRT, readFile(registryDir + "/" + Constants.CERT_CRT_FILE));
				tlsSecrets.put(Constants.K8S_SECRET_TLS_KEY, readFile(registryDir + "/" + Constants.CERT_KEY_FILE));
				tlsLabels.put("secret", "tls");
				
				try {
					// K8SApiCall createSecret
					logger.info("K8SApiCall createSecret");
					tlsSecretName = K8sApiCaller.createSecret(namespace, tlsSecrets, registryId, tlsLabels, Constants.K8S_SECRET_TYPE_TLS, ownerRefs);
				} catch (ApiException e) {
					try {
						patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
								e.getResponseBody(), "CreateRegistryFailed");
					} catch (ApiException e2) {
						logger.info(e2.getResponseBody());
						throw e2;
					}

					throw e;
				}
				
				// Create Ingress
				ExtensionsV1beta1Ingress ingress = new ExtensionsV1beta1Ingress();
				V1ObjectMeta metadata = new V1ObjectMeta();
				Map<String, String> annotations = new HashMap<>();

				metadata.setName(Constants.K8S_PREFIX + registryId);
				metadata.setNamespace(namespace);
				
				Map<String, String> ingressLabels = new HashMap<String, String>();
				ingressLabels.put("app", "registry");
				ingressLabels.put("apps", metadata.getName());
				logger.info("app: registry");
				logger.info("apps: " + metadata.getName());
				metadata.setLabels(ingressLabels);
				
				annotations.put("kubernetes.io/ingress.class", "nginx"); 
				annotations.put("nginx.ingress.kubernetes.io/proxy-connect-timeout", "3600");
				annotations.put("nginx.ingress.kubernetes.io/proxy-read-timeout", "3600");
				annotations.put("nginx.ingress.kubernetes.io/ssl-redirect", "true");
				annotations.put("nginx.ingress.kubernetes.io/backend-protocol", "HTTPS");
				annotations.put("nginx.ingress.kubernetes.io/proxy-body-size", "0");
				metadata.setAnnotations(annotations);
				metadata.setOwnerReferences(ownerRefs);
				ingress.setMetadata(metadata);

				ExtensionsV1beta1IngressSpec spec = new ExtensionsV1beta1IngressSpec();
				List<ExtensionsV1beta1IngressTLS> tls = new ArrayList<>();
				ExtensionsV1beta1IngressTLS eTls = new ExtensionsV1beta1IngressTLS();
				String hostsItem = registryDomain;
				eTls.addHostsItem(hostsItem);
				eTls.setSecretName(tlsSecretName);

				tls.add(eTls);
				spec.setTls(tls);
				
				List<ExtensionsV1beta1IngressRule> rules = new ArrayList<>();
				ExtensionsV1beta1IngressRule eRule = new ExtensionsV1beta1IngressRule();
				ExtensionsV1beta1HTTPIngressRuleValue http = new ExtensionsV1beta1HTTPIngressRuleValue();
				List<ExtensionsV1beta1HTTPIngressPath> paths = new ArrayList<>();
				ExtensionsV1beta1HTTPIngressPath ePath = new ExtensionsV1beta1HTTPIngressPath();
				ExtensionsV1beta1IngressBackend backend = new ExtensionsV1beta1IngressBackend();
				
				backend.setServiceName(Constants.K8S_PREFIX + registryId);
				backend.setServicePort(new IntOrString(registrySVCPort));
				ePath.setBackend(backend);
				
				ePath.setPath("/");
				paths.add(ePath);
				http.setPaths(paths);
				eRule.setHttp(http);
				
				eRule.setHost(registryDomain);
				rules.add(eRule);
				spec.setRules(rules);

				ingress.setSpec(spec);

				try {
					extentionApi.createNamespacedIngress(namespace, ingress, null, null, null);
				}catch(ApiException e) {
					logger.info(e.getResponseBody());
				}
			}

			// ---- Create Config Map
			boolean regConfigExist = true;
			String configMapName = registry.getSpec().getCustomConfigYml();

			if (StringUtil.isNotEmpty(configMapName)) {
				try {
					V1ConfigMap regConfig = api.readNamespacedConfigMap(configMapName, namespace, null, null, null);

					logger.info("== customConfigYaml ==\n" + regConfig.toString());
					regConfigExist = true;
				} catch (ApiException e) {
					logger.info(e.getResponseBody());
					regConfigExist = false;
				}
			}

			logger.info("regConfigExist: " + regConfigExist);

			if (StringUtil.isEmpty(configMapName) || !regConfigExist) {
				configMapName = Constants.K8S_PREFIX + registry.getMetadata().getName();
				try {
					V1ConfigMap regConfig = api.readNamespacedConfigMap(Constants.REGISTRY_CONFIG_MAP_NAME,
							Constants.REGISTRY_NAMESPACE, null, null, null);

					V1ConfigMap configMap = new V1ConfigMap();
					V1ObjectMeta metadata = new V1ObjectMeta();

					metadata.setName(configMapName);
					metadata.setNamespace(namespace);
					metadata.setOwnerReferences(ownerRefs);
					configMap.setMetadata(metadata);
					configMap.setData(regConfig.getData());

					V1ConfigMap result = api.createNamespacedConfigMap(namespace, configMap, null, null, null);
					logger.info("createNamespacedConfigMap result: " + result.toString());
					regConfigExist = true;
				} catch (ApiException e) {
					logger.info(e.getResponseBody());
					regConfigExist = false;
				}
			}

			// ----- Create Registry Replica Set
			V1ReplicaSetBuilder rsBuilder = new V1ReplicaSetBuilder();

			// 1. metadata
			V1ObjectMeta rsMeta = new V1ObjectMeta();

			// 1-1. replica set name
			rsMeta.setName(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryId);
			logger.info("RS Name: " + rsMeta.getName());

			// 1-2 replica set label
			logger.info("<RS Label List>");
			Map<String, String> rsLabels = new HashMap<String, String>();
			rsLabels.put("app", "registry");
			rsLabels.put("apps", rsMeta.getName());
			logger.info("app: registry");
			logger.info("apps: " + rsMeta.getName());
			rsMeta.setLabels(rsLabels);

			// 1-3. replica set owner ref
			rsMeta.setOwnerReferences(ownerRefs);

			rsBuilder.withMetadata(rsMeta);

			// 2. spec
			V1ReplicaSetSpec rsSpec = new V1ReplicaSetSpec();

			// 2-1. replicas
			rsSpec.setReplicas(1);

			// 2-2. pod template spec
			V1PodTemplateSpec podTemplateSpec = new V1PodTemplateSpec();

			// 2-2-1. pod metadata
			V1ObjectMeta podMeta = new V1ObjectMeta();

			// 2-2-1-1. pod label
			logger.info("<Pod Label List>");
			Map<String, String> podLabels = new HashMap<String, String>();
			podLabels.put("app", "registry");
			podLabels.put("apps", rsMeta.getName());
			logger.info("app: registry");
			logger.info("apps: " + rsMeta.getName());

			podLabels.put(Constants.K8S_PREFIX + registryId, "lb");
			logger.info(Constants.K8S_PREFIX + registryId + ": lb");
			
			// add user label
			if( registry.getSpec().getReplicaSet() != null) {
				Map<String, String> userLabels = null;
				 
				if( (userLabels = registry.getSpec().getReplicaSet().getLabels()) != null) {
					for( String key : userLabels.keySet() ) {
						podLabels.put(key, userLabels.get(key));
					}
				}
			}
			
			podMeta.setLabels(podLabels);
			podTemplateSpec.setMetadata(podMeta);

			// 2-2-2. pod spec
			V1PodSpec podSpec = new V1PodSpec();

			V1Container container = new V1Container();

			// 2-2-2-2-1. container name
			container.setName(Constants.K8S_PREFIX + registryId.toLowerCase());
			logger.info("<Container Name: " + container.getName() + ">");

			// 2-2-2-2-2. image
			container.setImage(registry.getSpec().getImage());
			logger.info("- Image: " + container.getImage());
			container.setImagePullPolicy("IfNotPresent");

			// Set a Lifecycle
			V1Lifecycle lifecycle = new V1Lifecycle();
			V1Handler postStart = new V1Handler();
			V1ExecAction exec = new V1ExecAction();
			List<String> command = new ArrayList<>();
			command.add("/bin/sh");
			command.add("-c");
			command.add("/auth.sh $ID $PASSWD");

			exec.setCommand(command);
			postStart.setExec(exec);
			lifecycle.setPostStart(postStart);

			container.setLifecycle(lifecycle);

			// 2-2-2-2-3. spec
			V1ResourceRequirements resrcReq = new V1ResourceRequirements();
			Map<String, Quantity> limit = new HashMap<>();
			limit = new HashMap<String, Quantity>();
			limit.put("cpu", new Quantity(Constants.REGISTRY_CPU_STRING));
			limit.put("memory", new Quantity(String.valueOf(Constants.REGISTRY_MEMORY_STRING)));
			resrcReq.setLimits(limit);
			container.setResources(resrcReq);

			// 2-2-2-2-4. env
			// Default env
			V1EnvVar env1 = new V1EnvVar();
			env1.setName("REGISTRY_AUTH");
			env1.setValue("htpasswd");
			container.addEnvItem(env1);

			V1EnvVar env2 = new V1EnvVar();
			env2.setName("REGISTRY_AUTH_HTPASSWD_REALM");
			env2.setValue("Registry Realm");
			container.addEnvItem(env2);

			V1EnvVar env3 = new V1EnvVar();
			env3.setName("REGISTRY_AUTH_HTPASSWD_PATH");
			env3.setValue("/auth/htpasswd");
			container.addEnvItem(env3);

			V1EnvVar env4 = new V1EnvVar();
			env4.setName("REGISTRY_HTTP_ADDR");
//			env4.setValue("0.0.0.0:" + registryPort);
			env4.setValue("0.0.0.0:" + registrySVCTargetPort);
			container.addEnvItem(env4);

			V1EnvVar env5 = new V1EnvVar();
			env5.setName("REGISTRY_HTTP_TLS_CERTIFICATE");
			env5.setValue("/certs/localhub.crt");
			container.addEnvItem(env5);

			V1EnvVar env6 = new V1EnvVar();
			env6.setName("REGISTRY_HTTP_TLS_KEY");
			env6.setValue("/certs/localhub.key");
			container.addEnvItem(env6);

//			V1EnvVar env7 = new V1EnvVar();
//			env7.setName("REGISTRY_IP_PORT");
//			env7.setValue(registryIP + ":" + registryPort);
//			container.addEnvItem(env7);

			// env
			V1EnvVar secretEnv1 = new V1EnvVar();
			secretEnv1.setName("ID");
			V1EnvVarSource valueFrom = new V1EnvVarSource();
			V1SecretKeySelector secretKeyRef = new V1SecretKeySelector();
			secretKeyRef.setName(Constants.K8S_PREFIX + registryId);
			secretKeyRef.setKey("ID");
			valueFrom.setSecretKeyRef(secretKeyRef);
			secretEnv1.setValueFrom(valueFrom);
			container.addEnvItem(secretEnv1);

			V1EnvVar secretEnv2 = new V1EnvVar();
			secretEnv2.setName("PASSWD");
			V1EnvVarSource valueFrom2 = new V1EnvVarSource();
			V1SecretKeySelector secretKeyRef2 = new V1SecretKeySelector();
			secretKeyRef2.setName(Constants.K8S_PREFIX + registryId);
			secretKeyRef2.setKey("PASSWD");
			valueFrom2.setSecretKeyRef(secretKeyRef2);
			secretEnv2.setValueFrom(valueFrom2);
			container.addEnvItem(secretEnv2);

			// 2-2-2-2-5. port
			V1ContainerPort portsItem = new V1ContainerPort();
			portsItem.setContainerPort(registrySVCTargetPort);
			logger.info("Container Port: " + portsItem.getContainerPort());

			portsItem.setName(RegistryService.REGISTRY_PORT_NAME);
			portsItem.setProtocol(RegistryService.REGISTRY_PORT_PROTOCOL);
			container.addPortsItem(portsItem);

			if (regConfigExist) {
				// Configmap Volume mount
				// config.yml:/etc/docker/registry/config.yml
				V1VolumeMount configMount = new V1VolumeMount();
				configMount.setName("config");
				configMount.setMountPath("/etc/docker/registry");
				container.addVolumeMountsItem(configMount);
			}

			// Secret Volume mount
			V1VolumeMount certMount = new V1VolumeMount();
			certMount.setName("certs");
			certMount.setMountPath("/certs");
			container.addVolumeMountsItem(certMount);

			// Registry Volume mount
			String mode = null;
			String pvcMountPath = registryPVC.getMountPath() == null ? "/var/lib/registry" : registryPVC.getMountPath();
			
			if( !existPvcName && registry.getSpec().getPersistentVolumeClaim().getCreate().getVolumeMode() != null) {
				volumeMode = registry.getSpec().getPersistentVolumeClaim().getCreate().getVolumeMode();
			}
			
			if( volumeMode.equals("Block")) {
				V1VolumeDevice volumeDevicesItem = new V1VolumeDevice();
				
				volumeDevicesItem.setName("registry");
				volumeDevicesItem.setDevicePath(pvcMountPath);
				container.addVolumeDevicesItem(volumeDevicesItem);
			}
			else {
				V1VolumeMount registryMount = new V1VolumeMount();
				
				registryMount.setName("registry");
				registryMount.setMountPath(pvcMountPath);
				container.addVolumeMountsItem(registryMount);
			}

			// Get loginAuth For Readiness Probe
			String loginAuth = registry.getSpec().getLoginId() + ":" + registry.getSpec().getLoginPassword();
			loginAuth = new String(Base64.encodeBase64(loginAuth.getBytes()));

			// Set Readiness Probe
			V1Probe readinessProbe = new V1Probe();
			V1HTTPGetAction httpGet = new V1HTTPGetAction();
			List<V1HTTPHeader> headers = new ArrayList<V1HTTPHeader>();
			V1HTTPHeader authHeader = new V1HTTPHeader();
			authHeader.setName("authorization");
			authHeader.setValue("Basic " + loginAuth);
			headers.add(authHeader);

			httpGet.setPath("v2/_catalog");
			httpGet.setPort(new IntOrString(registrySVCTargetPort));
			httpGet.setScheme("HTTPS");
			httpGet.setHttpHeaders(headers);
			readinessProbe.setHttpGet(httpGet);

			readinessProbe.setFailureThreshold(10);
			readinessProbe.setInitialDelaySeconds(5);
			readinessProbe.setPeriodSeconds(3);
			readinessProbe.setSuccessThreshold(1);
			readinessProbe.setTimeoutSeconds(1);
			container.setReadinessProbe(readinessProbe);

			// Set Liveness Probe
			V1Probe livenessProbe = new V1Probe();
			V1HTTPGetAction httpGet2 = new V1HTTPGetAction();
			List<V1HTTPHeader> headers2 = new ArrayList<V1HTTPHeader>();
			V1HTTPHeader authHeader2 = new V1HTTPHeader();
			authHeader2.setName("authorization");
			authHeader2.setValue("Basic " + loginAuth);
			headers2.add(authHeader2);

			httpGet2.setPath("v2/_catalog");
			httpGet2.setPort(new IntOrString(registrySVCTargetPort));
			httpGet2.setScheme("HTTPS");
			httpGet2.setHttpHeaders(headers2);
			livenessProbe.setHttpGet(httpGet2);

			livenessProbe.setFailureThreshold(10);
			livenessProbe.setInitialDelaySeconds(5);
			livenessProbe.setPeriodSeconds(5);
			livenessProbe.setSuccessThreshold(1);
			livenessProbe.setTimeoutSeconds(30);
			container.setLivenessProbe(livenessProbe);

			podSpec.addContainersItem(container);

			List<V1Volume> volumes = new ArrayList<>();

			if (regConfigExist) {
				// Configmap Volume
				V1Volume configVolume = new V1Volume();
				V1ConfigMapVolumeSource configMap = new V1ConfigMapVolumeSource();
				configMap.setName(configMapName);
				configVolume.setConfigMap(configMap);
				configVolume.setName("config");
				volumes.add(configVolume);
			}

			// Secret Volume
			V1Volume certVolume = new V1Volume();
			certVolume.setName("certs");
			V1SecretVolumeSource volSecret = new V1SecretVolumeSource();
			logger.info("secret name: " + secretName);
			volSecret.setSecretName(secretName);
			certVolume.setSecret(volSecret);
			volumes.add(certVolume);

			// Registry Volume
			String pvcName = registryPVC.getExist() != null ? registryPVC.getExist().getPvcName() : Constants.K8S_PREFIX + registryId;
			V1Volume registryVolume = new V1Volume();
			registryVolume.setName("registry");
			V1PersistentVolumeClaimVolumeSource regPvc = new V1PersistentVolumeClaimVolumeSource();
			
			regPvc.setClaimName(pvcName);
			registryVolume.setPersistentVolumeClaim(regPvc);

			volumes.add(registryVolume);

			podSpec.setVolumes(volumes);

			// restart policy
			podSpec.setRestartPolicy("Always");
			logger.info("Restart Policy: " + podSpec.getRestartPolicy());

			// node selector
			if( registry.getSpec().getReplicaSet() != null) {
				Map<String, String> nodeSelector = null;
				List<V1Toleration> tolerations = null;
				
				if( (nodeSelector = registry.getSpec().getReplicaSet().getNodeSelector()) != null) 
					podSpec.setNodeSelector(nodeSelector);
				
				if( (tolerations = registry.getSpec().getReplicaSet().getTolerations()) != null) 
					podSpec.setTolerations(tolerations);
			}
			
			podTemplateSpec.setSpec(podSpec);
			rsSpec.setTemplate(podTemplateSpec);

			// label selector
			V1LabelSelector labelSelector = new V1LabelSelector();
			logger.info("<RS Label List>");
			Map<String, String> rsLabelSelector = new HashMap<String, String>();
			rsLabelSelector.put("app", "registry");
			rsLabelSelector.put("apps", rsMeta.getName());
			logger.info("app: registry");
			logger.info("apps: " + rsMeta.getName());

			// user label selector
			if( registry.getSpec().getReplicaSet() != null) {
				V1LabelSelector rsSelector = null;
				List<V1LabelSelectorRequirement> matchExpressions = null;
				Map<String, String> matchLabels = null;
				 
				if( (rsSelector = registry.getSpec().getReplicaSet().getSelector()) != null) {
					matchLabels = rsSelector.getMatchLabels();
					if( matchLabels != null ) {
						for( String key : matchLabels.keySet()) {
							rsLabelSelector.put(key, matchLabels.get(key));
						}
					}
					
					matchExpressions = rsSelector.getMatchExpressions();
					if( matchExpressions != null ) {
						labelSelector.setMatchExpressions(matchExpressions);
					}
				}
			}

			labelSelector.setMatchLabels(rsLabelSelector);
			rsSpec.setSelector(labelSelector);

			rsBuilder.withSpec(rsSpec);

			try {
				logger.info("Create ReplicaSet");
				V1ReplicaSet result = appApi.createNamespacedReplicaSet(namespace, rsBuilder.build(), null, null, null);
				logger.info("createNamespacedReplicaSet result: " + result.toString());
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
				
				try {
					patchRegistryStatus(registry, RegistryStatus.StatusPhase.ERROR.getStatus(),
							e.getResponseBody(), "CreateRegistryFailed");
				} catch (ApiException e2) {
					logger.info(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	/*
	 * {"op":"remove","path":"/apiVersion"}
	 * {"op":"replace","path":"/kind","value":"Registry3"}
	 * {"op":"add","path":"/kind2","value":"Registry"}
	 * 
	 */
	public static void updateRegistrySubresources(Registry registry, JsonNode diff) throws ApiException {
		logger.info("[K8S ApiCaller] updateRegistrySubresources(Registry, JsonNode) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryId = registry.getMetadata().getName();
		Set<String> updateSubResources = new HashSet<>();
		boolean restartPodRequired = false;
		boolean renewLoginAuthRequired = false;
		JsonArray jArrayPatchReplicaSet = new JsonArray();
		JsonArray jArrayPatchSecret = new JsonArray();

		for (final JsonNode obj : diff) {
			String path = "";

			logger.info("update object: " + obj.toString());
			if (obj.get("path") != null) {
				path = obj.get("path").toString().split("\"")[1];
			}

			if (path.equals("/spec/image")) {
				restartPodRequired = true;
				JsonObject replJson = new JsonObject();
				replJson.addProperty("op", "replace");
				replJson.addProperty("path", "/spec/template/spec/containers/0/image");
				replJson.addProperty("value", registry.getSpec().getImage());

				jArrayPatchReplicaSet.add(replJson);

				if (!updateSubResources.contains("ReplicaSet"))
					updateSubResources.add("ReplicaSet");
			}
			if (path.equals("/spec/loginId")) {
				restartPodRequired = true;
				renewLoginAuthRequired = true;
				String dataStr = registry.getSpec().getLoginId();
				byte[] encodeData = Base64.encodeBase64(dataStr.getBytes());

				JsonObject replJson = new JsonObject();
				replJson.addProperty("op", "replace");
				replJson.addProperty("path", "/data/ID");
				replJson.addProperty("value", new String(encodeData));

				jArrayPatchSecret.add(replJson);

				if (!updateSubResources.contains("ReplicaSet"))
					updateSubResources.add("ReplicaSet");
				if (!updateSubResources.contains("Secret"))
					updateSubResources.add("Secret");

			}
			if (path.equals("/spec/loginPassword")) {
				restartPodRequired = true;
				renewLoginAuthRequired = true;
				String dataStr = registry.getSpec().getLoginPassword();
				byte[] encodeData = Base64.encodeBase64(dataStr.getBytes());
				JsonObject replJson = new JsonObject();
				replJson.addProperty("op", "replace");
				replJson.addProperty("path", "/data/PASSWD");
				replJson.addProperty("value", new String(encodeData));

				jArrayPatchSecret.add(replJson);

				if (!updateSubResources.contains("ReplicaSet"))
					updateSubResources.add("ReplicaSet");
				if (!updateSubResources.contains("Secret"))
					updateSubResources.add("Secret");
			}
			if(path.equals("/spec/persistentVolumeClaim/create/deleteWithPvc")) {
				if( registry.getSpec().getPersistentVolumeClaim().getCreate().getDeleteWithPvc() ) {
					JsonArray jArrayPatchPvc = new JsonArray();
					JsonArray ownerRefs = new JsonArray();
					JsonObject replJson = new JsonObject();
					JsonObject ownerRef = new JsonObject();
					
					ownerRef.addProperty("apiVersion", Constants.CUSTOM_OBJECT_GROUP + "/" + Constants.CUSTOM_OBJECT_VERSION);
					ownerRef.addProperty("blockOwnerDeletion", Boolean.TRUE);
					ownerRef.addProperty("controller", Boolean.TRUE);
					ownerRef.addProperty("kind", registry.getKind());
					ownerRef.addProperty("name", registry.getMetadata().getName());
					ownerRef.addProperty("uid", registry.getMetadata().getUid());
					
					ownerRefs.add(ownerRef);
					
					replJson.addProperty("op", "replace");
					replJson.addProperty("path", "/metadata/ownerReferences");
					replJson.add("value", ownerRefs);

					jArrayPatchPvc.add(replJson);
					
					try {
						Object result = api.patchNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registryId, namespace, new V1Patch(jArrayPatchPvc.toString()), null, null, null, null);

						logger.info("patchNamespacedPersistentVolumeClaim result: " + result.toString());
					
					} catch (ApiException e) {
						logger.info(e.getResponseBody());
					}
				}
				else {
					try {
						V1PersistentVolumeClaim pvc = api.readNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registryId, namespace, null, null, null);
						pvc.getMetadata().setOwnerReferences(null);
						 
						Object result = api.replaceNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registryId, namespace, pvc, null, null, null);
						logger.info("replaceNamespacedCustomObject result: " + result.toString());
					} catch (ApiException e) {
						logger.info(e.getResponseBody());
						throw e;
					}
				}
			}

			if (renewLoginAuthRequired) {
				String loginAuth = registry.getSpec().getLoginId() + ":" + registry.getSpec().getLoginPassword();
				loginAuth = new String(Base64.encodeBase64(loginAuth.getBytes()));

				JsonObject replJson = new JsonObject();
				replJson.addProperty("op", "replace");
				replJson.addProperty("path",
						"/spec/template/spec/containers/0/readinessProbe/httpGet/httpHeaders/0/value");
				replJson.addProperty("value", "Basic " + loginAuth);

				jArrayPatchReplicaSet.add(replJson);

				JsonObject replJson2 = new JsonObject();
				replJson2.addProperty("op", "replace");
				replJson2.addProperty("path",
						"/spec/template/spec/containers/0/livenessProbe/httpGet/httpHeaders/0/value");
				replJson2.addProperty("value", "Basic " + loginAuth);

				jArrayPatchReplicaSet.add(replJson2);
			}
		}

		for (String res : updateSubResources) {
			if (res.equals("ReplicaSet")) {
				updateRegistryReplicaSet(registry, jArrayPatchReplicaSet);
			}
			if (res.equals("Secret")) {
				updateRegistrySecret(registry, jArrayPatchSecret);
			}
		}

		if (restartPodRequired) {
			String podName = "";
			V1PodList pods = null;
			String labelSelector = "apps=" + Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryId;

			try {
				pods = api.listNamespacedPod(namespace, null, null, null, null, labelSelector, null, null, null, false);

				if (pods.getItems().size() == 0) {
					logger.info("RegistryPod is not found");
					return;
				}

				for (V1Pod pod : pods.getItems()) {
					podName = pod.getMetadata().getName();
					break;
				}

				api.deleteNamespacedPod(podName, namespace, null, null, null, null, null, null);

			} catch (ApiException e) {
				logger.info(e.getResponseBody());
			}
		}
	}

	public static void updateRegistryReplicaSet(Registry registry, JsonElement patchJson) throws ApiException {
		logger.info("[K8S ApiCaller] updateRegistryReplicaSet(Registry, JsonElement) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryId = registry.getMetadata().getName();
		logger.info("updateRegistryReplicaSet's Json: " + patchJson.toString());

		try {
			V1ReplicaSet result = appApi.patchNamespacedReplicaSet(
					Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryId, namespace, new V1Patch(patchJson.toString()), null, null,
					null, null);
			logger.info("patchNamespacedReplicaSet result: " + result.toString());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static void updateRegistrySecret(Registry registry, JsonElement patchJson) throws ApiException {
		logger.info("[K8S ApiCaller] updateRegistrySecret(Registry, JsonElement) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryId = registry.getMetadata().getName();
		logger.info("updateRegistrySecret's Json: " + patchJson.toString());

		try {
			V1Secret result = api.patchNamespacedSecret(Constants.K8S_PREFIX + registryId, namespace, new V1Patch(patchJson.toString()), null,
					null, null, null);
			logger.info("patchNamespacedSecret result: " + result.toString());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}

	}

	public static void updateRegistryAnnotationLastCR(Registry registry) throws ApiException {
		logger.info("[K8S ApiCaller] updateRegistryAnnotationLastCR(Registry) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryId = registry.getMetadata().getName();

		// ------ Patch Registry
		Map<String, String> annotations = registry.getMetadata().getAnnotations() == null ? new HashMap<>()
				: registry.getMetadata().getAnnotations();

		JsonObject json = (JsonObject) Util.toJson(registry);

		annotations.put(Constants.LAST_CUSTOM_RESOURCE, json.toString());
		registry.getMetadata().setAnnotations(annotations);

		try {

			Object result = customObjectApi.replaceNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryId,
					registry);
			logger.info("replaceNamespacedCustomObject result: " + result.toString());

		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static void addRegistryAnnotation(Registry registry) throws ApiException {
		logger.info("[K8S ApiCaller] addRegistryAnnotation(Registry) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryId = registry.getMetadata().getName();
		String registryIpPort = "";

		V1Secret secretRet = api.readNamespacedSecret(Constants.K8S_PREFIX + registryId, namespace, null, null, null);

		Map<String, byte[]> secretMap = secretRet.getData();
		registryIpPort = new String(secretMap.get("REGISTRY_URL"));
		logger.info("REGISTRY_IP_PORT = " + registryIpPort);

		// ------ Patch Registry
		Map<String, String> annotations = registry.getMetadata().getAnnotations() == null ? new HashMap<>()
				: registry.getMetadata().getAnnotations();

		JsonObject json = (JsonObject) Util.toJson(registry);

		annotations.put(Constants.LAST_CUSTOM_RESOURCE, json.toString());
		annotations.put(Registry.REGISTRY_LOGIN_URL, "https://" + registryIpPort);
		registry.getMetadata().setAnnotations(annotations);

		try {
			Object result = customObjectApi.replaceNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryId,
					registry);

			logger.info("replaceNamespacedCustomObject result: " + result.toString());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static void patchRegistryStatus(Registry registry, String phase, String message, String reason) throws ApiException{
		String namespace = registry.getMetadata().getNamespace();
		JSONObject phaseObj = new JSONObject();
		JSONObject messageObj = new JSONObject();
		JSONObject reasonObj = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();

		if (phase != null && message != null && reason != null) {
			phaseObj.put("op", "replace");
			phaseObj.put("path", "/status/phase");
			phaseObj.put("value", phase);
			patchStatusArray.add(phaseObj);

			messageObj.put("op", "replace");
			messageObj.put("path", "/status/message");
			messageObj.put("value", message);
			patchStatusArray.add(messageObj);

			reasonObj.put("op", "replace");
			reasonObj.put("path", "/status/reason");
			reasonObj.put("value", reason);
			patchStatusArray.add(reasonObj);

			try {
				Object result = customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
						registry.getMetadata().getName(), patchStatusArray);
				logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
				throw e;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void patchRegistryStatus(String registryName, String namespace, String phase, String message, String reason) throws ApiException{
		JSONObject phaseObj = new JSONObject();
		JSONObject messageObj = new JSONObject();
		JSONObject reasonObj = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();

		if (phase != null && message != null && reason != null) {
			phaseObj.put("op", "replace");
			phaseObj.put("path", "/status/phase");
			phaseObj.put("value", phase);
			patchStatusArray.add(phaseObj);

			messageObj.put("op", "replace");
			messageObj.put("path", "/status/message");
			messageObj.put("value", message);
			patchStatusArray.add(messageObj);

			reasonObj.put("op", "replace");
			reasonObj.put("path", "/status/reason");
			reasonObj.put("value", reason);
			patchStatusArray.add(reasonObj);

			try {
				Object result = customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
						registryName, patchStatusArray);
				logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
				throw e;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void updateRegistryStatus(V1ReplicaSet rs, String eventType)
			throws ApiException, JsonParseException, JsonMappingException, IOException {
		logger.info("[K8S ApiCaller] updateRegistryStatus(V1ReplicaSet, String) Start");

		String registryName = "";
		String registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
		String namespace = rs.getMetadata().getNamespace();

		registryName = rs.getMetadata().getName();
		registryName = registryName.substring(registryPrefix.length());
		logger.info("registry name: " + registryName);

		if (rs.getMetadata().getOwnerReferences() == null) {
			logger.info(rs.getMetadata().getName() + "/" + namespace + " replicaset ownerReference is null");
			return;
		}

		try {
			if (!isCurrentRegistry(rs.getMetadata().getOwnerReferences().get(0).getUid(), registryName, namespace, "RegistryReplicaSet")) {
				logger.info("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		}catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(rs.getMetadata().getName() + "/" + namespace + " replica set is deleted");
			}
			throw e;
		}
		JSONObject patchStatus = new JSONObject();
		JSONObject condition = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();

		switch (eventType) {
		case Constants.EVENT_TYPE_ADDED:
			condition.put("type", RegistryCondition.Condition.REPLICA_SET.getType());
			condition.put("status", RegistryStatus.Status.TRUE.getStatus());

			patchStatus.put("op", "replace");
			patchStatus.put("path", RegistryCondition.Condition.REPLICA_SET.getPath());
			patchStatus.put("value", condition);
			patchStatusArray.add(patchStatus);

			break;
		case Constants.EVENT_TYPE_MODIFIED:

			break;
		case Constants.EVENT_TYPE_DELETED:
			condition.put("type", RegistryCondition.Condition.REPLICA_SET.getType());
			condition.put("status", RegistryStatus.Status.FALSE.getStatus());

			patchStatus.put("op", "replace");
			patchStatus.put("path", RegistryCondition.Condition.REPLICA_SET.getPath());
			patchStatus.put("value", condition);
			patchStatusArray.add(patchStatus);

			break;
		}

		try {
			Object result = customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					patchStatusArray);
			logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.info("[RegistryReplicaSet]" + registryName + "/" + namespace + " registry was deleted!!");
			}
			else {
				logger.info("[RegistryReplicaSet]" + e.getResponseBody());
			}
			throw e;
		}

	}

	@SuppressWarnings("unchecked")
	public static void updateRegistryStatus(V1Pod pod)
			throws ApiException, JsonParseException, JsonMappingException, IOException {
		logger.info("[K8S ApiCaller] updateRegistryStatus(V1Pod) Start");
		String registryName = "";
		String registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
		String namespace = pod.getMetadata().getNamespace();
		String reason = "";

		registryName = pod.getMetadata().getLabels().get("apps");
		registryName = registryName.substring(registryPrefix.length());
		logger.info("registry name: " + registryName);

		String verifyUid = null;
		try {
			V1ReplicaSet rs = appApi.readNamespacedReplicaSet(
					Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryName, namespace, null, null, null);

			if (pod.getMetadata().getOwnerReferences() != null) {
				if (pod.getMetadata().getOwnerReferences().get(0).getUid().equals(rs.getMetadata().getUid())) {
					if (rs.getMetadata().getOwnerReferences() != null) {
						verifyUid = rs.getMetadata().getOwnerReferences().get(0).getUid();
					}
				} else {
					logger.info("This pod uid(" + pod.getMetadata().getOwnerReferences().get(0).getUid()
							+ ") is not for current replicaset(" + rs.getMetadata().getOwnerReferences().get(0).getUid()
							+ ")");
				}
			}
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.info(registryName + "/" + namespace + " registry was deleted!!");
			}
			else {
				logger.info(e.getResponseBody());
			}
			throw e;
		}
		
		try {
			if (!isCurrentRegistry(verifyUid, registryName, namespace, "RegistryPod")) {
				logger.info("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		}catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(pod.getMetadata().getName() + "/" + namespace + " pod is deleted");
			}
			throw e;
		}

		if (pod.getStatus().getContainerStatuses() != null) {
			if (pod.getStatus().getContainerStatuses().get(0).getState().getWaiting() != null) {
				logger.info(pod.getStatus().getContainerStatuses().get(0).getState().getWaiting().toString());
				reason = pod.getStatus().getContainerStatuses().get(0).getState().getWaiting().getReason();
			} else if (pod.getStatus().getContainerStatuses().get(0).getState().getRunning() != null) {
				logger.info(pod.getStatus().getContainerStatuses().get(0).toString());
				if (pod.getStatus().getContainerStatuses().get(0).getReady())
					reason = "Running";
				else
					reason = "NotReady";
			} else if (pod.getStatus().getContainerStatuses().get(0).getState().getTerminated() != null) {
				logger.info(pod.getStatus().getContainerStatuses().get(0).getState().getTerminated().toString());
				reason = pod.getStatus().getContainerStatuses().get(0).getState().getTerminated().getReason();
			} else
				reason = "Unknown";

			if (reason == null)
				reason = "";
			logger.info("registry pod state's reason: " + reason);

			Object response = null;
			try {
				response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
						registryName);
				logger.info("getNamespacedCustomObject result: " + response.toString());

			} catch (ApiException e) {
				if(e.getCode() == 404) {
					logger.info("[RegistryPod]" + registryName + "/" + namespace + " registry was deleted!!");
				}
				else {
					logger.info("[RegistryPod]" + e.getResponseBody());
				}
				throw e;
			}

			Registry registry = null;
			try {
				registry = mapper.readValue(gson.toJson(response), Registry.class);
			} catch (JsonParseException | JsonMappingException e) {
				logger.info(e.getMessage());
			}

			logger.info("REGISTRY RESOURCE VERSION: " + registry.getMetadata().getResourceVersion());
			logger.info("REGISTRY UID: " + registry.getMetadata().getUid());

			JSONArray patchStatusArray = new JSONArray();

			if (registry.getStatus().getPhase() != null) {
				if (reason.equals("NotReady")) {
					JSONObject patchStatus = new JSONObject();
					JSONObject patchStatus2 = new JSONObject();
					JSONObject condition = new JSONObject();
					JSONObject condition2 = new JSONObject();

					condition.put("type", RegistryCondition.Condition.POD.getType());
					condition.put("status", RegistryStatus.Status.TRUE.getStatus());

					patchStatus.put("op", "replace");
					patchStatus.put("path", RegistryCondition.Condition.POD.getPath());
					patchStatus.put("value", condition);
					patchStatusArray.add(patchStatus);

					condition2.put("type", RegistryCondition.Condition.CONTAINER.getType());
					condition2.put("status", RegistryStatus.Status.FALSE.getStatus());

					patchStatus2.put("op", "replace");
					patchStatus2.put("path", RegistryCondition.Condition.CONTAINER.getPath());
					patchStatus2.put("value", condition2);
					patchStatusArray.add(patchStatus2);
				} else if (reason.equals("Running")) {
					JSONObject patchStatus = new JSONObject();
					JSONObject patchStatus2 = new JSONObject();
					JSONObject condition = new JSONObject();
					JSONObject condition2 = new JSONObject();

					condition.put("type", RegistryCondition.Condition.POD.getType());
					condition.put("status", RegistryStatus.Status.TRUE.getStatus());

					patchStatus.put("op", "replace");
					patchStatus.put("path", RegistryCondition.Condition.POD.getPath());
					patchStatus.put("value", condition);
					patchStatusArray.add(patchStatus);

					condition2.put("type", RegistryCondition.Condition.CONTAINER.getType());
					condition2.put("status", RegistryStatus.Status.TRUE.getStatus());

					patchStatus2.put("op", "replace");
					patchStatus2.put("path", RegistryCondition.Condition.CONTAINER.getPath());
					patchStatus2.put("value", condition2);
					patchStatusArray.add(patchStatus2);
				} else {
					JSONObject patchStatus = new JSONObject();
					JSONObject patchStatus2 = new JSONObject();
					JSONObject condition = new JSONObject();
					JSONObject condition2 = new JSONObject();

					condition.put("type", RegistryCondition.Condition.POD.getType());
					condition.put("status", RegistryStatus.Status.FALSE.getStatus());

					patchStatus.put("op", "replace");
					patchStatus.put("path", RegistryCondition.Condition.POD.getPath());
					patchStatus.put("value", condition);
					patchStatusArray.add(patchStatus);

					condition2.put("type", RegistryCondition.Condition.CONTAINER.getType());
					condition2.put("status", RegistryStatus.Status.FALSE.getStatus());

					patchStatus2.put("op", "replace");
					patchStatus2.put("path", RegistryCondition.Condition.CONTAINER.getPath());
					patchStatus2.put("value", condition2);
					patchStatusArray.add(patchStatus2);

				}

				try {
					Object result = customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
							Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
							registryName, patchStatusArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e) {
					if(e.getCode() == 404) {
						logger.info("[RegistryPod]" + registryName + "/" + namespace + " registry was deleted!!");
					}
					else {
						logger.info("[RegistryPod]" + e.getResponseBody());
					}
					throw e;
				}

			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void updateRegistryStatus(V1Service svc, String eventType) throws ApiException, IOException {
		logger.info("[K8S ApiCaller] updateRegistryStatus(V1Service, String) Start");
		String registryName = "";
		String registryPrefix = Constants.K8S_PREFIX;
		String namespace = svc.getMetadata().getNamespace();

		registryName = svc.getMetadata().getName();
		registryName = registryName.substring(registryPrefix.length());
		logger.info("registry name: " + registryName);

		if (svc.getMetadata().getOwnerReferences() == null) {
			logger.info(svc.getMetadata().getName() + "/" + namespace + " service ownerReference is null");
			return;
		}

		try {
			if (!isCurrentRegistry(svc.getMetadata().getOwnerReferences().get(0).getUid(), registryName, namespace, "RegistryService")) {
				logger.info("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		}catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(svc.getMetadata().getName() + "/" + namespace + " service is deleted");
			}
			throw e;
		}

		JSONArray patchStatusArray = new JSONArray();
		JSONObject patchStatus = new JSONObject();
		JSONObject condition = new JSONObject();

		switch (eventType) {
		case Constants.EVENT_TYPE_ADDED:
			condition.put("type", RegistryCondition.Condition.SERVICE.getType());
			condition.put("status", RegistryStatus.Status.TRUE.getStatus());

			patchStatus.put("op", "replace");
			patchStatus.put("path", RegistryCondition.Condition.SERVICE.getPath());
			patchStatus.put("value", condition);
			patchStatusArray.add(patchStatus);

			break;
		case Constants.EVENT_TYPE_MODIFIED:

			break;
		case Constants.EVENT_TYPE_DELETED:
			condition.put("type", RegistryCondition.Condition.SERVICE.getType());
			condition.put("status", RegistryStatus.Status.FALSE.getStatus());

			patchStatus.put("op", "replace");
			patchStatus.put("path", RegistryCondition.Condition.SERVICE.getPath());
			patchStatus.put("value", condition);
			patchStatusArray.add(patchStatus);

			break;
		}

		try {
			Object result = customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					patchStatusArray);
			logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.info("[RegistryService]" + registryName + "/" + namespace + " registry was deleted!!");
			}
			else {
				logger.info("[RegistryService]" + e.getResponseBody());
			}
			throw e;
		}

	}

	@SuppressWarnings("unchecked")
	public static void updateRegistryStatus(V1Secret secret, String eventType) throws ApiException, IOException {
		logger.info("[K8S ApiCaller] updateRegistryStatus(V1Secret, String) Start");
		String registryName = "";
		String namespace = secret.getMetadata().getNamespace();
		String registryPrefix = null;
		
		if( secret.getType().equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
			registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
		} else if (secret.getType().equals(Constants.K8S_SECRET_TYPE_TLS)) {
			registryPrefix = Constants.K8S_PREFIX + Constants.K8S_TLS_PREFIX;
		}else {
			registryPrefix = Constants.K8S_PREFIX;
		}

		logger.info("registry secret type: " + secret.getType());
		logger.info("registry prefix: " + registryPrefix);
		registryName = secret.getMetadata().getName();
		registryName = registryName.substring(registryPrefix.length());
		logger.info("registry name: " + registryName);

		if (secret.getMetadata().getOwnerReferences() == null) {
			logger.info(secret.getMetadata().getName() + "/" + namespace + " secret ownerReference is null");
			return;
		}
		
		try {
			if (!isCurrentRegistry(secret.getMetadata().getOwnerReferences().get(0).getUid(), registryName, namespace, "RegistrySecret")) {
				logger.info("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		}catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(secret.getMetadata().getName() + "/" + namespace + " secret is deleted");
			}
			throw e;
		}

		JSONArray patchStatusArray = new JSONArray();
		JSONObject patchStatus = new JSONObject();
		JSONObject condition = new JSONObject();
		
		if (secret.getType().equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
			// DOCKEER CONFIG JSON TYPE SECRET
			
			switch (eventType) {
			case Constants.EVENT_TYPE_ADDED:
				condition.put("type", RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getType());
				condition.put("status", RegistryStatus.Status.TRUE.getStatus());

				patchStatus.put("op", "replace");
				patchStatus.put("path", RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getPath());
				patchStatus.put("value", condition);
				patchStatusArray.add(patchStatus);

				break;
			case Constants.EVENT_TYPE_MODIFIED:

				break;
			case Constants.EVENT_TYPE_DELETED:
				condition.put("type", RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getType());
				condition.put("status", RegistryStatus.Status.FALSE.getStatus());

				patchStatus.put("op", "replace");
				patchStatus.put("path", RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getPath());
				patchStatus.put("value", condition);
				patchStatusArray.add(patchStatus);

				break;
			}
			
		} else if (secret.getType().equals(Constants.K8S_SECRET_TYPE_TLS)) {
			// TLS TYPE SECRET
			
			switch (eventType) {
			case Constants.EVENT_TYPE_ADDED:
				condition.put("type", RegistryCondition.Condition.SECRET_TLS.getType());
				condition.put("status", RegistryStatus.Status.TRUE.getStatus());

				patchStatus.put("op", "replace");
				patchStatus.put("path", RegistryCondition.Condition.SECRET_TLS.getPath());
				patchStatus.put("value", condition);
				patchStatusArray.add(patchStatus);

				break;
			case Constants.EVENT_TYPE_MODIFIED:

				break;
			case Constants.EVENT_TYPE_DELETED:
				condition.put("type", RegistryCondition.Condition.SECRET_TLS.getType());
				condition.put("status", RegistryStatus.Status.FALSE.getStatus());

				patchStatus.put("op", "replace");
				patchStatus.put("path", RegistryCondition.Condition.SECRET_TLS.getPath());
				patchStatus.put("value", condition);
				patchStatusArray.add(patchStatus);

				break;
			}
			
		} else {
			// OPAQUE TYPE SECRET
			
			switch (eventType) {
			case Constants.EVENT_TYPE_ADDED:
				condition.put("type", RegistryCondition.Condition.SECRET_OPAQUE.getType());
				condition.put("status", RegistryStatus.Status.TRUE.getStatus());

				patchStatus.put("op", "replace");
				patchStatus.put("path", RegistryCondition.Condition.SECRET_OPAQUE.getPath());
				patchStatus.put("value", condition);
				patchStatusArray.add(patchStatus);

				break;
			case Constants.EVENT_TYPE_MODIFIED:

				break;
			case Constants.EVENT_TYPE_DELETED:
				condition.put("type", RegistryCondition.Condition.SECRET_OPAQUE.getType());
				condition.put("status", RegistryStatus.Status.FALSE.getStatus());

				patchStatus.put("op", "replace");
				patchStatus.put("path", RegistryCondition.Condition.SECRET_OPAQUE.getPath());
				patchStatus.put("value", condition);
				patchStatusArray.add(patchStatus);

				break;
			}
		}
		
		try {
			Object result = customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
					registryName, patchStatusArray);
			logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.info("[RegistrySecret]" + registryName + "/" + namespace + " registry was deleted!!");
			}
			else {
				logger.info("[RegistrySecret]" + e.getResponseBody());
			}
			throw e;
		}
	}
	

	@SuppressWarnings("unchecked")
	public static void updateRegistryStatus(ExtensionsV1beta1Ingress ingress, String eventType) throws ApiException, IOException {
		logger.info("[K8S ApiCaller] updateRegistryStatus(ExtensionsV1beta1Ingress, String) Start");
		String registryName = "";
		String registryPrefix = Constants.K8S_PREFIX;
		String namespace = ingress.getMetadata().getNamespace();

		registryName = ingress.getMetadata().getName();
		registryName = registryName.substring(registryPrefix.length());
		logger.info("registry name: " + registryName);

		if (ingress.getMetadata().getOwnerReferences() == null) {
			logger.info(ingress.getMetadata().getName() + "/" + namespace + " ingress ownerReference is null");
			return;
		}

		try {
			if (!isCurrentRegistry(ingress.getMetadata().getOwnerReferences().get(0).getUid(), registryName, namespace, "RegistryIngress")) {
				logger.info("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		}catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(ingress.getMetadata().getName() + "/" + namespace + " ingress is deleted");
			}
			throw e;
		}

		JSONArray patchStatusArray = new JSONArray();
		JSONObject patchStatus = new JSONObject();
		JSONObject condition = new JSONObject();

		switch (eventType) {
		case Constants.EVENT_TYPE_ADDED:
			condition.put("type", RegistryCondition.Condition.INGRESS.getType());
			condition.put("status", RegistryStatus.Status.TRUE.getStatus());

			patchStatus.put("op", "replace");
			patchStatus.put("path", RegistryCondition.Condition.INGRESS.getPath());
			patchStatus.put("value", condition);
			patchStatusArray.add(patchStatus);

			break;
		case Constants.EVENT_TYPE_MODIFIED:

			break;
		case Constants.EVENT_TYPE_DELETED:
			condition.put("type", RegistryCondition.Condition.INGRESS.getType());
			condition.put("status", RegistryStatus.Status.FALSE.getStatus());

			patchStatus.put("op", "replace");
			patchStatus.put("path", RegistryCondition.Condition.INGRESS.getPath());
			patchStatus.put("value", condition);
			patchStatusArray.add(patchStatus);

			break;
		}

		try {
			Object result = customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					patchStatusArray);
			logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.info("[RegistryIngress]" + registryName + "/" + namespace + " registry was deleted!!");
			}
			else {
				logger.info("[RegistryIngress]" + e.getResponseBody());
			}
			throw e;
		}
	}

	public static boolean isCurrentRegistry(String verifyUid, String registryName, String namespace, String callFrom)
			throws ApiException, IOException {
		String existRegistryUID = null;
		Object response = null;

		if (verifyUid == null) {
			logger.info("verifyUid is null!!");
			return false;
		}

		try {
			response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName);
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.info("[" + callFrom + "]" + registryName + "/" + namespace + " registry was deleted!!");
			}
			else {
				logger.info("[" + callFrom + "]" + e.getResponseBody());
			}
			throw e;
		}

		try {
			Registry existRegistry = null;

			existRegistry = mapper.readValue(gson.toJson(response), Registry.class);
			existRegistryUID = existRegistry.getMetadata().getUid();
			logger.info("VERIFY REGISTRY UID: " + verifyUid);
			logger.info("EXIST REGISTRY UID: " + existRegistryUID);
		} catch (JsonParseException | JsonMappingException e) {
			logger.info(e.getMessage());
			throw e;
		}

		return verifyUid.equals(existRegistryUID);
	}

	public static List<Image> getAllImageData(Registry registry) throws ApiException, Exception {
		List<Image> imageList = null;

		try {
			Object response = customObjectApi.listNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, registry.getMetadata().getNamespace(),
					Constants.CUSTOM_OBJECT_PLURAL_IMAGE, null, null, null,
					"registry=" + registry.getMetadata().getName(), null, null, null, Boolean.FALSE);

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			// Register Joda deserialization module because of creationTimestamp of k8s
			// object
			mapper.registerModule(new JodaModule());
			imageList = mapper.readValue((new Gson()).toJson(respJson.get("items")),
					new TypeReference<ArrayList<Image>>() {
					});

		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		} catch (JsonParseException | JsonMappingException e) {
			logger.info(e.getMessage());
			throw e;
		}

		return imageList;
	}

	public static void initializeImageList() throws ApiException, Exception {
		try {
			logger.info("initializeImageList");
			Object response = customObjectApi.listClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, null, null, null, null,
					null, null, null, Boolean.FALSE);

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			List<Object> registryList = mapper.readValue((new Gson()).toJson(respJson.get("items")),
					new TypeReference<ArrayList<Object>>() {
					});

			if (registryList != null) {
				for (Object registryObj : registryList) {
					try {
						Registry registry = mapper.treeToValue(mapper.valueToTree(registryObj), Registry.class);
						
						logger.info(registry.getMetadata().getName() + "/" + registry.getMetadata().getNamespace()
							+ " registry sync");
					
						syncImageList(registry);
					} catch (ApiException e) {
						logger.info(e.getResponseBody());
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
				}
			}
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
		} catch (JsonParseException | JsonMappingException e) {
			logger.info(e.getMessage());
//			throw e;
		}
	}

	public static void syncImageList(Registry registry) throws ApiException, Exception {
		logger.info("[K8S ApiCaller] syncImageList(Registry) Start");
		String namespace = registry.getMetadata().getNamespace();
		SSLSocketFactory sf = null;
		Map<String, String> header = new HashMap<>();
		Map<String, String> certMap = new HashMap<>();

		try {
			
			V1Secret secretReturn = api.readNamespacedSecret(Constants.K8S_PREFIX + registry.getMetadata().getName(), namespace, null, null, null);
			Map<String, byte[]> secretMap = new HashMap<>();
			secretMap = secretReturn.getData();
			
			for (String key : secretMap.keySet()) {
				logger.info("secret key: " + key);
				certMap.put(key, new String(secretMap.get(key)));
			}
			
			sf = SecurityHelper.createSocketFactory(certMap.get(Constants.CERT_CRT_FILE),
					certMap.get(Constants.CERT_CERT_FILE), certMap.get(Constants.CERT_KEY_FILE));
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		} catch (Exception e) {
			logger.info(e.getMessage());
			throw e;
		}

		// Set authorization Header
		String auth = certMap.get("ID") + ":" + certMap.get("PASSWD");
		String encodedAuth = new String(Base64.encodeBase64(auth.getBytes()));
		String registryIpPort = certMap.get("REGISTRY_IP_PORT");
		logger.info("[encodedAuth]" + encodedAuth);
		header.put("authorization", "Basic " + encodedAuth);

		logger.info("Image Registry [ " + registryIpPort + "] : Get Current Image List from Repository ");
		String imagelistResponse = httpsCommander(sf, header, null, registryIpPort, "v2/_catalog?n=10000", null);

		JsonObject imagelistJson = (JsonObject) new JsonParser().parse(imagelistResponse.toString());
		JsonArray repositories = imagelistJson.getAsJsonArray("repositories");
		List<String> imageList = new ArrayList<>();
		if (repositories != null) {
			for (JsonElement imageElement : repositories) {
				String imageName = imageElement.toString();
				imageName = imageName.replaceAll("\"", "");
				logger.info("Repository Image : " + imageName);
				imageList.add(imageName);
			}
		}

		logger.info("Previous Image List");
		List<Image> imageListDB = getAllImageData(registry);
		List<String> imageNameListDB = null;
		if (imageListDB != null) {
			for (Image imageDB : imageListDB) {
				if (imageNameListDB == null) {
					imageNameListDB = new ArrayList<>();
				}
				imageNameListDB.add(imageDB.getSpec().getName());
			}
		}

		logger.info("Comparing ImageName and get New Images Name & Deleted Images Name");
		List<String> newImageList = new ArrayList<>();
		List<String> deletedImageList = new ArrayList<>();

		for (String imageName : imageList) {
			if (imageNameListDB == null) {
				imageNameListDB = new ArrayList<>();
			}
			if (!imageNameListDB.contains(imageName)) {
				if (newImageList == null) {
					newImageList = new ArrayList<>();
				}
				newImageList.add(imageName);
				logger.info("new Image : " + imageName);
			}
		}

		if (imageNameListDB != null) {
			for (String imageName : imageNameListDB) {
				if (imageList == null) {
					imageList = new ArrayList<>();
				}
				if (!imageList.contains(imageName)) {
					if (deletedImageList == null) {
						deletedImageList = new ArrayList<>();
					}
					deletedImageList.add(imageName);
					logger.info("deleted Image : " + imageName);
				}
			}
		}

		if (newImageList != null) {
			logger.info("For New Image, Insert Image and Versions Data from Repository");
			for (String newImage : newImageList) {
				logger.info(
						"Image Registry [ " + registryIpPort + "] : Get Current Image [" + newImage + "] tags List ");
				String tagslistResponse = null;

				tagslistResponse = K8sApiCaller.httpsCommander(sf, header, null, registryIpPort,
						"v2/" + newImage + "/tags/list?n=10000", null);
				JsonObject tagsListJson = (JsonObject) new JsonParser().parse(tagslistResponse.toString());
				JsonArray tags = null;

				if (tagsListJson.get("errors") == null) {
					if (!tagsListJson.get("tags").isJsonNull()) {
						tags = tagsListJson.getAsJsonArray("tags");
					}
				} else {
					continue;
				}

				List<String> tagsList = null;

				if (tags != null) {
					tagsList = new ArrayList<>();
					for (JsonElement tagElement : tags) {
						String tag = tagElement.toString();
						tag = tag.replaceAll("\"", "");
						tagsList.add(tag);
					}
				}
				if (tagsList != null) {
					createImage(registry, newImage, tagsList);
					logger.info("Insert Image and Versions Data from Repository Success!");
				}
			}
		}

		if (imageListDB != null) {
			logger.info("For Exist Image, Compare tags List, Insert Version Data from Repository");
			for (Image imageDB : imageListDB) {

				List<String> tagsListDB = null;
				for (String imageVersion : imageDB.getSpec().getVersions()) {
					if (tagsListDB == null) {
						tagsListDB = new ArrayList<>();
					}
					tagsListDB.add(imageVersion);
				}

				String tagslistResponse = null;
				logger.info("Image Registry [ " + registryIpPort + "] : Get Current Image ["
						+ imageDB.getSpec().getName() + "] tags List ");

				tagslistResponse = K8sApiCaller.httpsCommander(sf, header, null, registryIpPort,
						"v2/" + imageDB.getSpec().getName() + "/tags/list?n=10000", null);
				JsonObject tagsListJson = (JsonObject) new JsonParser().parse(tagslistResponse.toString());

				JsonArray tags = null;

				if (tagsListJson.get("errors") == null) {
					if (!tagsListJson.get("tags").isJsonNull()) {
						tags = tagsListJson.getAsJsonArray("tags");
					}
				} else {
					continue;
				}

				List<String> tagsList = null;

				if (tags != null) {
					tagsList = new ArrayList<>();
					for (JsonElement tagElement : tags) {
						String tag = tagElement.toString();
						tag = tag.replaceAll("\"", "");
						tagsList.add(tag);
					}
				}

//				logger.info("Comparing Tags and get New Tag");
				List<String> newTagsList = null;
				List<String> deletedTagsList = null;
				if (tagsList != null && tagsList.size() > 0) {
					for (String tag : tagsList) {
						if (tagsListDB != null && !tagsListDB.contains(tag)) {
							if (newTagsList == null) {
								newTagsList = new ArrayList<>();
							}
							newTagsList.add(tag);
							logger.info("Image Name [" + imageDB.getSpec().getName() + "] New Tag : " + tag);
						}
					}
				}

				if (tagsListDB != null && tagsListDB.size() > 0) {
					for (String tag : tagsListDB) {
						if (tagsList != null && !tagsList.contains(tag)) {
							if (deletedTagsList == null) {
								deletedTagsList = new ArrayList<>();
							}
							deletedTagsList.add(tag);
							logger.info("Image Name [" + imageDB.getSpec().getName() + "] Deleted Tag : " + tag);
						}
					}
				}

				if (newTagsList != null) {
					addImageVersions(registry, imageDB.getSpec().getName(), newTagsList);
					logger.info("Insert Versions Data from Repository Success!");
				}

				if (deletedTagsList != null) {
					deleteImageVersions(registry, imageDB.getSpec().getName(), deletedTagsList);
					logger.info("Delete Versions Data from Repository Success!");
				}
			}
		}

		if (deletedImageList != null) {
			logger.info("For Deleted Image, Delete Image Data from Repository");
			for (String deletedImage : deletedImageList) {
				deleteImage(registry, deletedImage);
				logger.info("Delete Image Data from Repository Success!");
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static void deleteImageVersions(Registry registry, String imageName, List<String> tagsList)
			throws ApiException {
		logger.info("[K8S ApiCaller] deleteImageVersions(Registry, String, List<String>) Start");

		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();
		Image image = new Image();

		logger.info("imageName: " + imageName);
		logger.info("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;

		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName);

			image = mapper.readValue(gson.toJson(response), Image.class);

			logger.info("IMAGE RESOURCE VERSION: " + image.getMetadata().getResourceVersion());
			logger.info("IMAGE UID: " + image.getMetadata().getUid());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		try {
			JSONArray patchArray = new JSONArray();
			JSONObject patchContent = new JSONObject();
			JSONArray versions = new JSONArray();

			for (String version : image.getSpec().getVersions()) {
				logger.info("Exist Image Version: " + version);

				if (!tagsList.contains(version)) {
					versions.add(version);
				} else {
					logger.info("Deleted Image Version: " + version);
				}
			}

			patchContent.put("op", "replace");
			patchContent.put("path", "/spec/versions");
			patchContent.put("value", versions);
			patchArray.add(patchContent);

			try {
				Object result = customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
						patchArray);
				logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
				throw e;
			}

		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		}
	}

	@SuppressWarnings("unchecked")
	public static void addImageVersions(Registry registry, String imageName, List<String> tagsList)
			throws ApiException {
		logger.info("[K8S ApiCaller] addImageVersions(Registry, String, List<String>) Start");

		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();
		Image image = new Image();

		logger.info("imageName: " + imageName);
		logger.info("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;

		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName);

			image = mapper.readValue(gson.toJson(response), Image.class);

			logger.info("IMAGE RESOURCE VERSION: " + image.getMetadata().getResourceVersion());
			logger.info("IMAGE UID: " + image.getMetadata().getUid());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		JSONArray patchArray = new JSONArray();
		JSONObject patchContent = new JSONObject();
		JSONArray versions = new JSONArray();
		Set<String> versionSet = new HashSet<>();

		for (String version : image.getSpec().getVersions()) {
			logger.info("Exist Image Version: " + version);
			versionSet.add(version);
		}

		for (String version : tagsList) {
			logger.info("New Image Version: " + version);
			versionSet.add(version);
		}

		Iterator<String> iter = versionSet.iterator();
		while (iter.hasNext()) {
			String version = iter.next();
			versions.add(version);
		}

		patchContent.put("op", "replace");
		patchContent.put("path", "/spec/versions");
		patchContent.put("value", versions);
		patchArray.add(patchContent);

		try {
			Object result = customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
					patchArray);
			logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		}

	}

	public static void deleteImage(Registry registry, String imageName) throws ApiException {
		logger.info("[K8S ApiCaller] deleteImage(Registry, String, List<String>) Start");
		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();

		logger.info("imageName: " + imageName);
		logger.info("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;
		try {
			Object result = customObjectApi.deleteNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
					0, null, null, null);
			logger.info("deleteNamespacedCustomObject result: " + result.toString());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		}
	}

	public static void createImage(Registry registry, String imageName, List<String> tagsList) throws ApiException {
		logger.info("[K8S ApiCaller] createImage(Registry, String, List<String>) Start");

		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();
		Image image = null;

		try {
			image = new Image();
			String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;
			V1ObjectMeta metadata = new V1ObjectMeta();
			metadata.setName(imageCRName);
			metadata.setNamespace(namespace);

			Map<String, String> labels = new HashMap<>();
			labels.put("registry", imageRegistry);
			metadata.setLabels(labels);

			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);

			metadata.setOwnerReferences(ownerRefs);
			image.setMetadata(metadata);

			ImageSpec spec = new ImageSpec();
			List<String> versions = new ArrayList<>();
			for (String imageVersion : tagsList) {
				versions.add(imageVersion);
			}

			spec.setName(imageName);
			spec.setVersions(versions);
			spec.setRegistry(imageRegistry);
			image.setSpec(spec);

			Object result = customObjectApi.createNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, image, null);
			logger.info("createNamespacedCustomObject result: " + result.toString());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		}

	}

	@SuppressWarnings("unchecked")
	public static void createImage(RegistryEvent event) throws ApiException, IOException {
		logger.info("[K8S ApiCaller] createImage(RegistryEvent) Start");
		boolean imageExist = true;

		Registry registry = getRegistry(event);
		String namespace = registry.getMetadata().getNamespace();

		Image image = null;
		String imageName = event.getTarget().getRepository();
		String imageVersion = event.getTarget().getTag();
		String imageRegistry = registry.getMetadata().getName();

		logger.info("imageName: " + imageName);
		logger.info("imageVersion: " + imageVersion);
		logger.info("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;

		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName);

			image = mapper.readValue(gson.toJson(response), Image.class);

			logger.info("IMAGE RESOURCE VERSION: " + image.getMetadata().getResourceVersion());
			logger.info("IMAGE UID: " + image.getMetadata().getUid());
			imageExist = true;
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			imageExist = false;
		} catch (IOException e) {
			logger.info(e.getMessage());
			throw e;
		}

		if (imageExist) {
			try {
				JSONArray patchArray = new JSONArray();
				JSONObject patchContent = new JSONObject();
				JSONArray versions = new JSONArray();
				Set<String> versionSet = new HashSet<>();

				for (String version : image.getSpec().getVersions()) {
					logger.info("Exist Image Version: " + version);
					versionSet.add(version);
				}

				logger.info("New Image Version: " + imageVersion);
				versionSet.add(imageVersion);

				Iterator<String> iter = versionSet.iterator();
				while (iter.hasNext()) {
					String version = iter.next();
					versions.add(version);
				}

				patchContent.put("op", "replace");
				patchContent.put("path", "/spec/versions");
				patchContent.put("value", versions);
				patchArray.add(patchContent);

				try {
					Object result = customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
							Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE,
							imageCRName, patchArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e) {
					logger.info(e.getResponseBody());
					throw e;
				}

				imageExist = true;
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
			}
		} else {
			try {
				image = new Image();
				V1ObjectMeta metadata = new V1ObjectMeta();
				metadata.setName(imageCRName);
				metadata.setNamespace(namespace);

				Map<String, String> labels = new HashMap<>();
				labels.put("registry", imageRegistry);
				metadata.setLabels(labels);

				List<V1OwnerReference> ownerRefs = new ArrayList<>();
				V1OwnerReference ownerRef = new V1OwnerReference();

				ownerRef.setApiVersion(registry.getApiVersion());
				ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
				ownerRef.setController(Boolean.TRUE);
				ownerRef.setKind(registry.getKind());
				ownerRef.setName(registry.getMetadata().getName());
				ownerRef.setUid(registry.getMetadata().getUid());
				ownerRefs.add(ownerRef);

				metadata.setOwnerReferences(ownerRefs);

				image.setMetadata(metadata);

				ImageSpec spec = new ImageSpec();
				List<String> versions = new ArrayList<>();
				versions.add(imageVersion);
				spec.setName(imageName);
				spec.setVersions(versions);
				spec.setRegistry(imageRegistry);
				image.setSpec(spec);

				Object result = customObjectApi.createNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, image, null);
				logger.info("createNamespacedCustomObject result: " + result.toString());
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
			}
		}

	}

	public static void deleteImage(RegistryEvent event) throws ApiException {
		logger.info("[K8S ApiCaller] deleteImage(RegistryEvent) Start");

		Registry registry = getRegistry(event);
		String namespace = registry.getMetadata().getNamespace();

		String imageName = event.getTarget().getRepository();
		String imageVersion = event.getTarget().getTag();
		String imageRegistry = registry.getMetadata().getName();

		logger.info("imageName: " + imageName);
		logger.info("imageVersion: " + imageVersion);
		logger.info("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;

		try {
			Object result = customObjectApi.deleteNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
					0, null, null, null);
			logger.info("deleteNamespacedCustomObject result: " + result.toString());

		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static Registry getRegistry(RegistryEvent event) throws ApiException {
		Registry registry = null;
		String namespace = null;
		String registryId = null;

		try {
			/*
			 * /events/0/source/addr: "hpcd-registry-t2-registry-46nfk:443"
			 */
			V1PodList pods = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, Boolean.FALSE);
			String searchName = null;

			if (event.getSource() == null || event.getSource().getAddr() == null) {
				return null;
			}

			searchName = event.getSource().getAddr().split(":")[0];
			logger.info("RegistrySearchName: " + searchName);

			for (V1Pod pod : pods.getItems()) {
				if (pod.getMetadata().getName().equals(searchName)) {
					namespace = pod.getMetadata().getNamespace();
					logger.info("RegistryNamespace: " + namespace);

					String registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
					registryId = pod.getMetadata().getLabels().get("apps").substring(registryPrefix.length());

					break;
				}
			}

		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}

		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryId);

			registry = mapper.readValue(gson.toJson(response), Registry.class);

			logger.info("REGISTRY RESOURCE VERSION: " + registry.getMetadata().getResourceVersion());
			logger.info("REGISTRY UID: " + registry.getMetadata().getUid());

		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		return registry;
	}

	public static CommandExecOut commandExecute(String[] command) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		CommandExecOut cmdOutDo = new CommandExecOut();

		// command exec.
		logger.info("command: " + Arrays.asList(command));

		processBuilder.command(command);

		try {
			Process process = processBuilder.start();

			ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
			ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
			Thread outThread = new Thread(new Runnable() {
				public void run() {
					try {
						ByteStreams.copy(process.getInputStream(), baosOut);
					} catch (IOException ex) {
						ex.printStackTrace();
						logger.info(ex.getMessage());

						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						logger.info(sw.toString());
					} finally {
						logger.info("out end");
					}
				}
			});
			outThread.start();

			Thread errThread = new Thread(new Runnable() {
				public void run() {
					try {
						ByteStreams.copy(process.getErrorStream(), baosErr);
					} catch (IOException ex) {
						ex.printStackTrace();
						logger.info(ex.getMessage());

						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						logger.info(sw.toString());
					} finally {
						logger.info("err end");
					}
				}
			});
			errThread.start();

			// wait for current thread and return immediately if the subprocess has already
			// terminated.
			process.waitFor();

			// wait for any last output;
			outThread.join();
			errThread.join();

			process.getOutputStream().close();
			process.getErrorStream().close();
			process.getInputStream().close();

			// exit process
			process.destroy();

			String outString = new String(baosOut.toByteArray(), Charset.forName("UTF-8"));
			String errString = new String(baosErr.toByteArray(), Charset.forName("UTF-8"));

			logger.info("out: " + outString);
			logger.info("err: " + errString);

			cmdOutDo.setCmdStdOut(outString);
			cmdOutDo.setCmdStdErr(errString);

			logger.info("exit code: " + process.exitValue());
			cmdOutDo.setCmdExitCode(process.exitValue());

		} catch (IOException e) {
			logger.info(e.getMessage());

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
			throw e;
		} catch (InterruptedException e) {
			logger.info(e.getMessage());

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
			throw e;
		}
		return cmdOutDo;
	}

	// type1: null => Opaque
	// type2: kubernetes.io/dockerconfigjson
	public static String createSecret(String namespace, Map<String, String> secrets, String secretName,
			Map<String, String> labels, String type, List<V1OwnerReference> ownerRefs) throws ApiException {
		logger.info("[K8S ApiCaller] createSecret Service Start");

		V1Secret secret = new V1Secret();
		secret.setApiVersion("v1");
		secret.setKind("Secret");
		V1ObjectMeta metadata = new V1ObjectMeta();

		if (type != null && type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
			metadata.setName(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + secretName.toLowerCase());
		} else if( type != null && type.equals(Constants.K8S_SECRET_TYPE_TLS)) {
			metadata.setName(Constants.K8S_PREFIX + Constants.K8S_TLS_PREFIX + secretName.toLowerCase());
		}
		else {
			metadata.setName(Constants.K8S_PREFIX + secretName.toLowerCase());
		}

		if (ownerRefs != null) {
			metadata.setOwnerReferences(ownerRefs);
		}

		// logger.info("== secret map == ");
		for (String key : secrets.keySet()) {
			// logger.info("[secretMap]" + key + "=" + secrets.get(key));
			secret.putStringDataItem(key, secrets.get(key));
			// secret.putDataItem(key, secrets.get(key).getBytes(StandardCharsets.UTF_8));
		}

		// 2-2-1-1. pod label
		logger.info("<Pod Label List>");
		Map<String, String> podLabels = new HashMap<String, String>();

		if (labels == null) {
			podLabels.put("secret", "obj");
			podLabels.put("apps", Constants.K8S_PREFIX + secretName);
			logger.info("secret: obj");
			logger.info("apps: " + Constants.K8S_PREFIX + secretName);
		} else {
			for (String key : labels.keySet()) {
				podLabels.put(key, labels.get(key));
			}
		}

		metadata.setLabels(podLabels);
		secret.setMetadata(metadata);
		if (type != null) {
			if (type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
				secret.setType(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON);
			}
			else if (type.equals(Constants.K8S_SECRET_TYPE_TLS)) {
				secret.setType(Constants.K8S_SECRET_TYPE_TLS);
			}
		}

		try {
			V1Secret result;
			Map<String, byte[]> secretMap = new HashMap<>();
			result = api.createNamespacedSecret(namespace, secret, "true", null, null);

			logger.info("[result]" + result);

			// V1Secret secretRet = api.readNamespacedSecret(Constants.K8S_PREFIX +
			// secretName.toLowerCase(), Constants.K8S_PREFIX + domainId.toLowerCase(),
			// null, null, null);

			secretMap = result.getData();
			// logger.info("== real secret data ==");
//			for( String key : secretMap.keySet()) {
//				//					logger.info("[secret]" + key + "=" + new String(secretMap.get(key))); 
//			}

		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}

		return secret.getMetadata().getName();
	}
	
	public static void patchSecret(String namespace, Map<String, String> secrets, String secretName, Map<String, String> labels) throws Throwable {
		logger.info("[K8S ApiCaller] patchSecret Service Start");

		V1Secret result;
		
		for( String key : secrets.keySet()) {
			String dataStr = secrets.get(key);
			byte[] encodeData = Base64.encodeBase64(dataStr.getBytes());
			String jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/data/" + key + "\",\"value\": \"" + new String(encodeData) + "\" }]";
			logger.info("JsonPatchStr: " + jsonPatchStr);

			JsonElement jsonPatch = (JsonElement) new JsonParser().parse(jsonPatchStr);
			try {
				Map<String, byte[]> secretMap = new HashMap<>();
				result = api.patchNamespacedSecret(Constants.K8S_PREFIX + secretName.toLowerCase(), namespace, new V1Patch(jsonPatch.toString()), "true", null, null, null);
//				logger.info("[result]" + result);
				
//				secretMap = result.getData();
//				logger.info("== real secret data ==");
//				for( String key2 : secretMap.keySet()) {
//					logger.info("[secret]" + key2 + "=" + new String(secretMap.get(key2)));
//				}
			
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
				e.printStackTrace();
				throw e;
			}
		}
		
	}

	public static void deleteSecret(String namespace, String secretName, String type) throws ApiException {
		secretName = secretName.toLowerCase();

		try {
			if (type != null && type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + secretName, namespace,
						null, null, 0, null, null, new V1DeleteOptions());

			} else {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX + secretName, namespace, null, null, 0, null, null,
						new V1DeleteOptions());
			}
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}

	}

	public static V1Secret readSecret(String namespace, String secretName) throws ApiException {
		logger.info(" [k8sApiCaller] Read Secret Service Start ");
		V1Secret secretReturn = null;
		try {
			logger.info(" secretName.toLowerCase() :  " + secretName.toLowerCase());
			secretReturn = api.readNamespacedSecret(secretName.toLowerCase(), namespace, null, null, null);

		} catch (ApiException e) {
			e.getStackTrace();
			logger.info(e.getResponseBody());
			throw e;
		}
		return secretReturn;
	}

	private static String readFile(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		StringBuilder stringBuilder = new StringBuilder();
		char[] buffer = new char[1];

		while (reader.read(buffer) != -1) {
			stringBuilder.append(new String(buffer));
			buffer = new char[1];
		}

		reader.close();

		String content = stringBuilder.toString();
		logger.info("[" + filePath + "]:" + content);

		return content;
	}

	private static String createConfigJson(List<String> domainList, String id, String password) {
		// $(echo "{\"auths\": {\"192.168.6.218:443\": {\"auth\": \"$(echo
		// "tmax:tmax123" | base64)\"}}}" | base64)
		StringBuilder configSb = new StringBuilder();
		String auth = id + ":" + password;
		
		configSb.append("{\"auths\": {");
		int i=0;
		for(String domain : domainList) {
			if(i++ > 0) {
				configSb.append(", ");
			}
			
			configSb.append("\"" + domain + "\": ");
			configSb.append("{");
			configSb.append("\"auth\": \""+ new String( Base64.encodeBase64( auth.getBytes() ) ) + "\"");
			configSb.append("}");
		}
		configSb.append("}}");

		return configSb.toString();
//		return new String( Base64.encodeBase64( configSb.toString().getBytes() ) );
	}

	private static String createDirectory(String domainId, String registryId) throws IOException {
		Path opensslHome = Paths.get(Constants.OPENSSL_HOME_DIR);
		if (!Files.exists(opensslHome)) {
			Files.createDirectory(opensslHome);
			logger.info("Directory created: " + Constants.OPENSSL_HOME_DIR);
		}

		String domainDir = Constants.OPENSSL_HOME_DIR + "/" + domainId;
		if (!Files.exists(Paths.get(domainDir))) {
			Files.createDirectory(Paths.get(domainDir));
			logger.info("Directory created: " + domainDir);
		}

		String registryDir = Constants.OPENSSL_HOME_DIR + "/" + domainId + "/" + registryId;
		if (!Files.exists(Paths.get(registryDir))) {
			Files.createDirectory(Paths.get(registryDir));
			logger.info("Directory created: " + registryDir);
		}

		Path dockerLoginHome = Paths.get(Constants.DOCKER_LOGIN_HOME_DIR);
		if (!Files.exists(dockerLoginHome)) {
			Files.createDirectory(dockerLoginHome);
			logger.info("Directory created: " + Constants.DOCKER_LOGIN_HOME_DIR);
		}

		return registryDir;
	}

	public static Services getCatalog() throws ApiException {
		Services catalog = new Services();
		List<ServiceOffering> serviceList = new ArrayList<ServiceOffering>();
		String catalogNamespace = Constants.DEFAULT_NAMESPACE;
		if (System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE) != null
				&& !System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE).isEmpty()) {
			catalogNamespace = System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE);
		}
		Object templates = customObjectApi.listNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
				Constants.CUSTOM_OBJECT_VERSION, catalogNamespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, null, null,
				null, null, null, null, null, false);

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
				
				Object planResponse = customObjectApi.getClusterCustomObject("servicecatalog.k8s.io", "v1beta1","clusterserviceplans", planName);
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

	private static JsonNode objectToJsonNodeForBinding(Object object) throws IOException {
		JsonNode resultNode = mapper.valueToTree(object);
		return resultNode;
	}

	/**
	 * START method for Namespace Claim Controller by seonho_choi DO-NOT-DELETE
	 */
	private static long getLatestResourceVersion(String customResourceName, boolean isNamespaced) throws Exception {
		long latestResourceVersion = 0;
		try {

			Object result = customObjectApi.listClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, customResourceName, null, null, null, null, null, null, null,
					Boolean.FALSE);

			String JsonInString = gson.toJson(result);
			JsonFactory factory = mapper.getFactory();
			com.fasterxml.jackson.core.JsonParser parser = factory.createParser(JsonInString);
			JsonNode customObjectList = mapper.readTree(parser);

			if (customObjectList.get("items").isArray()) {
				for (JsonNode instance : customObjectList.get("items")) {
					long resourceVersion = instance.get("metadata").get("resourceVersion").asLong();
					if (isNamespaced) {
						latestResourceVersion = patchOperatorStartTime(customResourceName,
								instance.get("metadata").get("name").asText(),
								instance.get("metadata").get("namespace").asText(), resourceVersion);
					} else {
						latestResourceVersion = patchOperatorStartTime(customResourceName,
								instance.get("metadata").get("name").asText(), resourceVersion);
					}
				}
			}
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return latestResourceVersion;
	}

	public static V1Namespace createNamespace(NamespaceClaim claim) throws Throwable {
		logger.info("[K8S ApiCaller] Create Namespace Start");

		V1Namespace namespace = new V1Namespace();
		V1ObjectMeta namespaceMeta = new V1ObjectMeta();
		Map<String, String> labels = new HashMap<>();
		labels.put("fromClaim", claim.getMetadata().getName());
		
		//Add Trial Label if exists
		if (claim.getMetadata().getLabels() != null && claim.getMetadata().getLabels().get("trial") != null
				 && claim.getMetadata().getLabels().get("owner") != null) {
			labels.put("trial", claim.getMetadata().getLabels().get("trial"));
			labels.put("owner", claim.getMetadata().getLabels().get("owner"));
		}
		
		//Add Trial Annotations if exists
		if (claim.getMetadata().getAnnotations() != null ) {
			namespaceMeta.setAnnotations(claim.getMetadata().getAnnotations());
		}
				
		namespaceMeta.setLabels(labels);
		namespaceMeta.setName(claim.getResourceName());
		namespace.setMetadata(namespaceMeta);

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.createNamespace(namespace, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
		
		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName(claim.getResourceName());
		quotaMeta.setNamespace(claim.getResourceName());
		Map<String, String> quotaLabel = new HashMap<>();
		quotaLabel.put("fromClaim", claim.getMetadata().getName());
		quotaMeta.setLabels(quotaLabel);
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata(quotaMeta);
		quota.setSpec(spec);

		V1ResourceQuota quotaResult;
		try {
			quotaResult = api.createNamespacedResourceQuota(claim.getResourceName(), quota, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
		
		return namespaceResult;
	}

	public static void updateNamespace(NamespaceClaim claim) throws Throwable {
		logger.info("[K8S ApiCaller] Update Namespace Start");

		V1Namespace namespace = new V1Namespace();
		V1ObjectMeta namespaceMeta = new V1ObjectMeta();
		Map<String, String> labels = new HashMap<>();
		labels.put("fromClaim", claim.getMetadata().getName());
		
		//Add Trial Label if exists
		if (claim.getMetadata().getLabels() != null && claim.getMetadata().getLabels().get("trial") != null 
				&& claim.getMetadata().getLabels().get("owner") !=null) {
			labels.put("trial", claim.getMetadata().getLabels().get("trial"));
			labels.put("owner", claim.getMetadata().getLabels().get("owner"));
		}
		
		//Add Trial Annotations if exists
		if (claim.getMetadata().getAnnotations() != null ) {
			namespaceMeta.setAnnotations(claim.getMetadata().getAnnotations());
		}
				
		namespaceMeta.setLabels(labels);
		namespaceMeta.setName(claim.getResourceName());
		namespace.setMetadata(namespaceMeta);

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.replaceNamespace(claim.getResourceName(), namespace, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}

		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName(claim.getResourceName());
		quotaMeta.setNamespace(claim.getResourceName());
		Map<String, String> quotaLabel = new HashMap<>();
		quotaLabel.put("fromClaim", claim.getMetadata().getName());
		quotaMeta.setLabels(quotaLabel);
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata(quotaMeta);
		quota.setSpec(spec);

		V1ResourceQuota quotaResult;
		try {
			quotaResult = api.replaceNamespacedResourceQuota(claim.getResourceName(), claim.getResourceName(), quota,
					null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}
	
	public static void replaceNamespace(V1Namespace namespace) throws Throwable {
		logger.info("[K8S ApiCaller] Update Namespace Start");

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.replaceNamespace(namespace.getMetadata().getName(), namespace, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static void createResourceQuota(NamespaceClaim claim) throws Throwable {
		logger.info("[K8S ApiCaller] Create Resource Quota Start");

		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName(claim.getResourceName());
		quotaMeta.setNamespace(claim.getMetadata().getNamespace());
		Map<String, String> quotaLabel = new HashMap<>();
		quotaLabel.put("fromClaim", claim.getMetadata().getName());
		quotaMeta.setLabels(quotaLabel);
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata(quotaMeta);
		quota.setSpec(spec);

		V1ResourceQuota quotaResult;
		try {
			quotaResult = api.createNamespacedResourceQuota(claim.getMetadata().getNamespace(), quota, null, null,
					null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static void updateResourceQuota(NamespaceClaim claim) throws Throwable {
		logger.info("[K8S ApiCaller] Update Resource Quota Start");

		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName(claim.getResourceName());
		quotaMeta.setNamespace(claim.getMetadata().getNamespace());
		Map<String, String> quotaLabel = new HashMap<>();
		quotaLabel.put("fromClaim", claim.getMetadata().getName());
		quotaMeta.setLabels(quotaLabel);
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata(quotaMeta);
		quota.setSpec(spec);

		try {
			api.replaceNamespacedResourceQuota(claim.getResourceName(), claim.getMetadata().getNamespace(), quota, null,
					null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static boolean namespaceAlreadyExist(String name) throws Throwable {
		logger.info("[K8S ApiCaller] Get Namespace Start");

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.readNamespace(name, null, null, null);
		} catch (ApiException e) {
			logger.info("[K8S ApiCaller][Exception] Namespace-" + name + " is not Exist");
			return false;
		}

		if (namespaceResult == null) {
			logger.info("[K8S ApiCaller] Namespace-" + name + " is not Exist");
			return false;
		} else {
			logger.info(namespaceResult.toString());
			return true;
		}
	}

	public static boolean resourcequotaAlreadyExist(String name, String namespace) throws Throwable {
		logger.info("[K8S ApiCaller] Get Resource Quota Start");

		V1ResourceQuota resourceQuotaResult;
		try {
			resourceQuotaResult = api.readNamespacedResourceQuota(name, namespace, null, null, null);
		} catch (ApiException e) {
			logger.info("[K8S ApiCaller][Exception] ResourceQuota-" + name + " is not Exist");
			return false;
		}

		if (resourceQuotaResult == null) {
			logger.info("[K8S ApiCaller][Exception] ResourceQuota-" + name + " is not Exist");
			return false;
		} else {
			logger.info(resourceQuotaResult.toString());
			return true;
		}
	}

	public static boolean roleBindingAlreadyExist(String name, String namespace) throws Throwable {
		logger.info("[K8S ApiCaller] Get RoleBinding Start");

		V1RoleBinding roleBindingResult;
		try {
			roleBindingResult = rbacApi.readNamespacedRoleBinding(name, namespace, null);
		} catch (ApiException e) {
			logger.info("[K8S ApiCaller][Exception] RoleBinding-" + name + " is not Exist");
			return false;
		}

		if (roleBindingResult == null) {
			logger.info("[K8S ApiCaller][Exception] RoleBinding-" + name + " is not Exist");
			return false;
		} else {
			logger.info(roleBindingResult.toString());
			return true;
		}
	}
	
	public static boolean templateAlreadyExist(String name, String namespace) throws Throwable {
		logger.info("[K8S ApiCaller] Get Template Start");

		Object template;
		try {
			template = customObjectApi.getNamespacedCustomObject("tmax.io", "v1", namespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, name);
		} catch (ApiException e) {
			logger.info("[K8S ApiCaller][Exception] Template-" + name + " is not Exist");
			return false;
		}

		if (template == null) {
			logger.info("[K8S ApiCaller][Exception] Template-" + name + " is not Exist");
			return false;
		} else {
			logger.info(objectToJsonNode(template).toString());
			return true;
		}
	}
	
	public static void createTemplate(JsonNode claim, String namespace) throws ApiException, ParseException {
		logger.info("[K8S ApiCaller] Create Template Start");
		logger.info("[K8S ApiCaller] Create Template Info : " + claim.get("spec").toString() );
		
		JSONParser parser = new JSONParser();
		JSONObject bodyObj = (JSONObject) parser.parse(claim.get("spec").toString());
		
		try {
			templateApi.createNamespacedCustomObject("tmax.io", "v1", namespace, "Template", bodyObj, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void createRoleBinding(RoleBindingClaim claim) throws ApiException {
		logger.info("[K8S ApiCaller] Create RoleBinding Start");

		V1RoleBinding roleBinding = new V1RoleBinding();
		V1ObjectMeta roleBindingMeta = new V1ObjectMeta();
		roleBindingMeta.setName(claim.getResourceName());
		roleBindingMeta.setNamespace(claim.getMetadata().getNamespace());
		roleBindingMeta.setLabels(claim.getMetadata().getLabels());
		roleBinding.setMetadata(roleBindingMeta);
		roleBinding.setSubjects(claim.getSubjects());
		roleBinding.setRoleRef(claim.getRoleRef());

		try {
			rbacApi.createNamespacedRoleBinding(claim.getMetadata().getNamespace(), roleBinding, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void createClusterRoleBinding(RoleBindingClaim claim) throws ApiException {
		logger.info("[K8S ApiCaller] Create ClusterRoleBinding Start");

		V1ClusterRoleBinding clusterRoleBinding = new V1ClusterRoleBinding();
		V1ObjectMeta clusterRoleBindingMeta = new V1ObjectMeta();
		clusterRoleBindingMeta.setName(claim.getResourceName());
		clusterRoleBindingMeta.setLabels(claim.getMetadata().getLabels());
		clusterRoleBinding.setMetadata(clusterRoleBindingMeta);
		clusterRoleBinding.setSubjects(claim.getSubjects());
		clusterRoleBinding.setRoleRef(claim.getRoleRef());

		try {
			rbacApi.createClusterRoleBinding( clusterRoleBinding, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
	}
	

	public static void updateRoleBinding(RoleBindingClaim claim) throws Throwable {
		logger.info("[K8S ApiCaller] Update Role Binding Start");

		V1RoleBinding roleBinding = new V1RoleBinding();
		V1ObjectMeta roleBindingMeta = new V1ObjectMeta();
		roleBindingMeta.setName(claim.getResourceName());
		roleBindingMeta.setNamespace(claim.getMetadata().getNamespace());
		roleBinding.setMetadata(roleBindingMeta);
		roleBinding.setSubjects(claim.getSubjects());
		roleBinding.setRoleRef(claim.getRoleRef());

		try {
			rbacApi.replaceNamespacedRoleBinding(claim.getResourceName(), claim.getMetadata().getNamespace(),
					roleBinding, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static void createClusterRoleForNewUser(User userInDO) throws ApiException {
		logger.info("[K8S ApiCaller] Create Temporary ClusterRole for New User Start");

		V1ClusterRole clusterRole = new V1ClusterRole();
		V1ObjectMeta clusterRoleMeta = new V1ObjectMeta();
		clusterRoleMeta.setName(userInDO.getId());
		clusterRole.setMetadata(clusterRoleMeta);
		List<V1PolicyRule> rules = new ArrayList<>();

		V1PolicyRule rule = new V1PolicyRule();
		// User Rule
		rule.addApiGroupsItem(Constants.CUSTOM_OBJECT_GROUP);
		rule.addResourcesItem("users");
		rule.addResourceNamesItem(userInDO.getId());
		rule.addVerbsItem("*");
		rules.add(rule);

		// ClusterRole & ClusterRoleBinding Rule
		rule = new V1PolicyRule();
		rule.addApiGroupsItem(Constants.RBAC_API_GROUP);
		rule.addResourcesItem("clusterroles");
		rule.addResourcesItem("clusterrolebindings");
		rule.addResourceNamesItem(userInDO.getId());
		rule.addVerbsItem("*");
		rules.add(rule);
	
		// Claims Rule
		rule = new V1PolicyRule();
		rule.addApiGroupsItem(Constants.CUSTOM_OBJECT_GROUP);
//		rule.addResourcesItem("rolebindingclaims");
//		rule.addResourcesItem("resourcequotaclaims");
		rule.addResourcesItem("namespaceclaims");
		rule.addVerbsItem("create");
		rule.addVerbsItem("list");		
		rules.add(rule);

		clusterRole.setRules(rules);

		try {
			rbacApi.createClusterRole(clusterRole, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static void createClusterRoleBindingForNewUser(User userInDO) throws ApiException {
		logger.info("[K8S ApiCaller] Create Temporary ClusterRoleBinding for New User Start");

		V1ClusterRoleBinding clusterRoleBinding = new V1ClusterRoleBinding();
		V1ObjectMeta clusterRoleBindingMeta = new V1ObjectMeta();
		clusterRoleBindingMeta.setName(userInDO.getId());
		clusterRoleBinding.setMetadata(clusterRoleBindingMeta);

		// RoleRef
		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup(Constants.RBAC_API_GROUP);
		roleRef.setKind("ClusterRole");
		roleRef.setName(userInDO.getId());
		clusterRoleBinding.setRoleRef(roleRef);

		// subject
		V1Subject subject = new V1Subject();
		subject.setApiGroup(Constants.RBAC_API_GROUP);
		subject.setKind("User");
		subject.setName(userInDO.getId());
		clusterRoleBinding.addSubjectsItem(subject);

		try {
			rbacApi.createClusterRoleBinding(clusterRoleBinding, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}

	public static void deleteClusterRole(String name) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();
			rbacApi.deleteClusterRole(name, null, null, null, null, null, body);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static void deleteClusterRoleBinding(String name) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();
			rbacApi.deleteClusterRoleBinding(name, null, null, null, null, null, body);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void deleteRoleBinding(String nsName, String roleBindingName) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();
			rbacApi.deleteNamespacedRoleBinding(roleBindingName, nsName, null, null, 0, null, null, body);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}		
	}
	
	public static V1ClusterRole readClusterRole(String clusterRoleName) {
		V1ClusterRole clusterRole = null;
		try {
			clusterRole = rbacApi.readClusterRole(clusterRoleName, "true"); 

		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return clusterRole;
	}
	
	public static V1ClusterRole replaceClusterRole(V1ClusterRole clusterRole) {
		V1ClusterRole replaceResult = null;
		try {
			replaceResult = rbacApi.replaceClusterRole(clusterRole.getMetadata().getName(), clusterRole, null, null, null);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return replaceResult;
	}

	/**
	 * END method for Namespace Claim Controller by seonho_choi DO-NOT-DELETE
	 */

	/**
	 * START method for Namespace Claim Controller by seonho_choi DO-NOT-DELETE
	 */
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

	@SuppressWarnings("null")
	public static V1NamespaceList getAccessibleNS(String userId) throws Exception {
		V1NamespaceList nsList = null;
		List<String> nsNameList = null;
		List<String> userGroupList = null;
		
		V1ClusterRoleBindingList crbList = null;
		List<String> clusterRoleList = null;
		boolean clusterRoleFlag = false;
		try {
			// 1. Get UserGroup List if Exists
			logger.info(" userId :" + userId);
			UserCR user = getUser(userId);
			Map< String, String > userLabel = user.getMetadata().getLabels();
			if (userLabel != null) {
				Iterator<String> iter = userLabel.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					logger.info(" User label key " + key);

					if( key.startsWith("group-")) {
						if( userGroupList == null ) userGroupList = new ArrayList<>();
						logger.info(" userGroup Name " + key.substring(6));
						userGroupList.add(key.substring(6));
					}
				}
			}					
			// 2. List of ClusterRoleBinding
			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000, null, 60, false);
			for (V1ClusterRoleBinding item : crbList.getItems()) {
				List<V1Subject> subjects = item.getSubjects();
				V1RoleRef roleRef = item.getRoleRef();
				if (subjects != null) {
					for (V1Subject subject : subjects) {
						if (subject.getKind().equalsIgnoreCase("User")) {
							if (subject.getName().equalsIgnoreCase(userId)) {
								if (clusterRoleList == null)
									clusterRoleList = new ArrayList<>();
								clusterRoleList.add(roleRef.getName()); // get ClusterRole name
							}
						} else if (subject.getKind().equalsIgnoreCase("Group")) {
							if ( userGroupList != null ) {
								logger.info(" subject.getName() " + subject.getName());
								if ( userGroupList.contains(subject.getName())) {
									if (clusterRoleList == null)
										clusterRoleList = new ArrayList<>();
									clusterRoleList.add(roleRef.getName()); // get ClusterRole name
								}
							}
						}
					}
				}
			}

			// 3. Check if ClusterRole has NameSpace GET rule
			if (clusterRoleList != null) {
				for (String clusterRoleName : clusterRoleList) {
					logger.info("User [ " + userId + " ] has ClusterRole [ " + clusterRoleName + " ]");
					V1ClusterRole clusterRole = null;
					List<V1PolicyRule> rules = null;
					try{
						clusterRole = rbacApi.readClusterRole(clusterRoleName, "true"); // rolebinding은 있는데 role을 지웠을 경우를 대비
						rules = clusterRole.getRules();
					} catch (ApiException e) {
						if( !e.getResponseBody().contains("NotFound") ) throw e;			
					}
					if (rules != null) {
						for (V1PolicyRule rule : rules) {
							if (rule.getResources() != null) {
								if (rule.getResources().contains("*") || rule.getResources().contains("namespaces")) {
									if ( rule.getApiGroups().contains("*") || rule.getApiGroups().contains("") || rule.getApiGroups().contains("core") ) {
										logger.info("clusterRoleName : " + clusterRoleName);
										if (rule.getVerbs() != null) {
											if (rule.getVerbs().contains("list") || rule.getVerbs().contains("*")) {
												if (rule.getResourceNames() == null
														|| rule.getResourceNames().size() == 0) {
													clusterRoleFlag = true;
												} else {
													for (String nsName : rule.getResourceNames()) {
														if (nsNameList == null)
															nsNameList = new ArrayList<>();
														nsNameList.add(nsName);
													}
												}
											}
										}
									}		
								}
							}
						}
					}
				}
			}
			logger.info("clusterRoleflag : " + clusterRoleFlag);
			// Get All NameSpace
			if (clusterRoleFlag) {
				nsList = api.listNamespace("true", false, null, null, null, 100, null, 60, false);
			} else {
				V1NamespaceList nsListK8S = api.listNamespace("true", false, null, null, null, 100, null, 60, false);
				// 4. List of RoleBinding
				if (nsListK8S.getItems() != null) {
					for (V1Namespace ns : nsListK8S.getItems()) {
						V1RoleBindingList rbList = rbacApi.listNamespacedRoleBinding(ns.getMetadata().getName(), "true",
								false, null, null, null, 100, null, 60, false);
						for (V1RoleBinding item : rbList.getItems()) {
							List<V1Subject> subjects = item.getSubjects();
							V1RoleRef roleRef = item.getRoleRef();
							for (V1Subject subject : subjects) {
								if (( subject.getKind().equalsIgnoreCase("User") && subject.getName().equalsIgnoreCase(userId) ) 
										|| ( userGroupList!= null && subject.getKind().equalsIgnoreCase("Group") && userGroupList.contains(subject.getName()) ) ) {
										
									// 5. Check if Role has NameSpace GET rule
									if (roleRef.getKind().equalsIgnoreCase("Role")) {
										logger.info(
												"User [ " + userId + " ] has Role [" + roleRef.getName() + " ]");
										V1Role role = null;
										List<V1PolicyRule> rules = null;
										try{
											role = rbacApi.readNamespacedRole(roleRef.getName(), ns.getMetadata().getName(), "true");  // rolebinding은 있는데 role을 지웠을 경우를 대비
											rules = role.getRules();
										} catch (ApiException e) {
											if( !e.getResponseBody().contains("NotFound") ) throw e;			
										}
										if (rules != null) {
											for (V1PolicyRule rule : rules) {
												if (rule.getResources() != null) {
													if (rule.getResources().contains("*") || rule.getResources().contains("namespaces")) {
														if ( rule.getApiGroups().contains("*") || rule.getApiGroups().contains("") 
																|| rule.getApiGroups().contains("core") ) {
															if (rule.getVerbs() != null) {
																if (rule.getVerbs().contains("list")
																		|| rule.getVerbs().contains("*")) {
																	if (nsNameList == null)
																		nsNameList = new ArrayList<>();
																	nsNameList.add(ns.getMetadata().getName());
																}
															}
														}	
													}
												}
											}
										}

										// 6. Check if ClusterRole has NameSpace GET rule
									} else if (roleRef.getKind().equalsIgnoreCase("ClusterRole")) {
										logger.info("User [ " + userId + " ] has ClusterRole [" + roleRef.getName() + " ]");
										
										V1ClusterRole role = null;
										List<V1PolicyRule> rules = null;
										try{
											role = rbacApi.readClusterRole(roleRef.getName(), "true"); // rolebinding은 있는데 role을 지웠을 경우를 대비
											rules = role.getRules();
										} catch (ApiException e) {
											if( !e.getResponseBody().contains("NotFound") ) throw e;			
										}
										if (rules != null) {
											for (V1PolicyRule rule : rules) {
												if (rule.getResources() != null) {
													if (rule.getResources().contains("*") || rule.getResources().contains("namespaces")) {
														if ( rule.getApiGroups().contains("*") || rule.getApiGroups().contains("") 
																|| rule.getApiGroups().contains("core") ) {
															if (rule.getVerbs() != null) {
																if (rule.getVerbs().contains("list")
																		|| rule.getVerbs().contains("*")) {
																	if (nsNameList == null)
																		nsNameList = new ArrayList<>();
																	nsNameList.add(ns.getMetadata().getName());
																}
															}
														}	
													}
												}
											}
										}
									}			
								}
							}
						}
					}
				}
				if (nsNameList != null) {
					// Stream distinct (중복제거)
					nsNameList = nsNameList.stream().distinct().collect(Collectors.toList());

					for (String nsName : nsNameList) {
						if (nsList == null)
							nsList = new V1NamespaceList();
						nsList.setKind("NamespaceList");
						V1ListMeta nsListMeta = new V1ListMeta();
						nsListMeta.setSelfLink("/api/v1/namespaces");
						nsList.setMetadata(nsListMeta);
						V1Namespace ns = api.readNamespace(nsName, "true", false, false);
						ns.setKind(null);
						ns.setApiVersion(null);
						nsList.addItemsItem(ns);
					}
				}
			}
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}

		if (nsList != null) {
			for (V1Namespace ns : nsList.getItems()) {
				logger.info(" [ Accessible NameSpace ] : " + ns.getMetadata().getName());
			}
		}
		return nsList;
	}

	public static boolean verifyAdmin(String userId) throws ApiException {
		V1ClusterRoleBindingList crbList = null;
		boolean isAdmin = false;
		try {
			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000, null, 60, false);
			for (V1ClusterRoleBinding item : crbList.getItems()) {
				List<V1Subject> subjects = item.getSubjects();
				V1RoleRef roleRef = item.getRoleRef();
				if (subjects != null) {
					for (V1Subject subject : subjects) {
						if (subject.getKind().equalsIgnoreCase("User")) {
							if (subject.getName().equalsIgnoreCase(userId)) {
								V1ClusterRole clusterRole = rbacApi.readClusterRole(roleRef.getName(), "true");
								List<V1PolicyRule> rules = clusterRole.getRules();
								if (rules != null) {
									for (V1PolicyRule rule : rules) {
										if (rule.getApiGroups().contains("*") && rule.getResources().contains("*")
												&& rule.getVerbs().contains("*")) { // check admin rule
											isAdmin = true;
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		} catch (Exception e2) {
			logger.info(e2.getStackTrace().toString());
		}
		return isAdmin;
	}

	public static void createUser(User userInDO) throws Exception {
		try {
			UserCR userCR = new UserCR();
			// Set name & label
			V1ObjectMeta metadata = new V1ObjectMeta();
			metadata.setName(userInDO.getId());
			userCR.setMetadata(metadata);

			// Set userInfo
			userCR.setUserInfo(userInDO);
			// Truncate PW
			userCR.getUserInfo().setPassword(null);
			userCR.setStatus("active"); 

			// Make body
			JSONParser parser = new JSONParser();
			JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(userCR));

			customObjectApi.createClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_USER, bodyObj, null);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

	}

	public static List<String> deleteBlockedUser() throws Exception {
		List<String> deletedUserIdList = null;
		try {
			List<UserCR> userList = listUser();
			if (userList != null) {
				for (UserCR user : userList) {
					if (user.getStatus().equalsIgnoreCase(Constants.USER_STATUS_BLOCKED)) {
						deleteUser(user.getMetadata().getName());
						logger.info(" Bloced User [ " + user.getMetadata().getName() + " ] delete Success in k8s");

						deleteClusterRole(user.getMetadata().getName());
//						logger.info(  " Cluster Role [ " + user.getMetadata().getName() + " ] delete Success in k8s");

						deleteClusterRoleBinding(user.getMetadata().getName());
//						logger.info(  " Cluster RoleBindning [ " + user.getMetadata().getName() + " ] delete Success in k8s");

						if (deletedUserIdList == null)
							deletedUserIdList = new ArrayList<>();
						deletedUserIdList.add(user.getMetadata().getName());
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

		if (deletedUserIdList == null)
			logger.info(" No Blocked User to Delete !!");

		return deletedUserIdList;
	}

	private static long patchOperatorStartTime(String plural, String name, String namespace, long version) {

		JsonArray patchArray = new JsonArray();
		JsonObject patch = new JsonObject();
		patch.addProperty("op", "replace");
		patch.addProperty("path", "/operatorStartTime");
		patch.addProperty("value", Long.toString(time));
		patchArray.add(patch);

		// logger.info( "Patch Annotation Object : " + patchArray );

		Object response = null;
		try {
			response = customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, plural, name, patchArray);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			logger.info("ApiException Code: " + e.getCode());
		}

		long resourceVersion = version;
		if (response != null) {
			JsonObject json = (JsonObject) new JsonParser().parse(new Gson().toJson(response));
			resourceVersion = json.get("metadata").getAsJsonObject().get("resourceVersion").getAsLong();
		}

		return resourceVersion;
	}

	private static long patchOperatorStartTime(String plural, String name, long version) {

		JsonArray patchArray = new JsonArray();
		JsonObject patch = new JsonObject();
		patch.addProperty("op", "replace");
		patch.addProperty("path", "/operatorStartTime");
		patch.addProperty("value", Long.toString(time));
		patchArray.add(patch);

		// logger.info( "Patch Annotation Object : " + patchArray );

		Object response = null;
		try {
			response = customObjectApi.patchClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, plural, name, patchArray);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			logger.info("ApiException Code: " + e.getCode());
		}

		long resourceVersion = version;
		if (response != null) {
			JsonObject json = (JsonObject) new JsonParser().parse(new Gson().toJson(response));
			resourceVersion = json.get("metadata").getAsJsonObject().get("resourceVersion").getAsLong();
		}

		return resourceVersion;
	}

	private static OkHttpClient getHttpClient() {
		OkHttpClient httpClient;
		//List lists = Arrays.asList(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT);
		 ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
		            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
		            .cipherSuites(
		                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
		                    CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
		                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
		                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
		            .build();

		//httpClient.connectionSpecs(Collections.singletonList(spec))

		
		
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.addNetworkInterceptor(getProgressInterceptor()); // K8S Interceptor
		builder.addNetworkInterceptor(new ChunkedInterceptor()); // HyperCloud Interceptor
		//builder.addInterceptor(new HttpLoggingInterceptor());
		builder.connectionSpecs(Collections.singletonList(spec));
		httpClient = builder.build();
		return httpClient;
	}

	/**
	 * Get network interceptor to add it to the httpClient to track download
	 * progress for async requests.
	 */
	private static Interceptor getProgressInterceptor() {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				final Request request = chain.request();
				final Response originalResponse = chain.proceed(request);
				if (request.tag() instanceof ApiCallback) {
					final ApiCallback callback = (ApiCallback) request.tag();
					return originalResponse.newBuilder()
							.body(new ProgressResponseBody(originalResponse.body(), callback)).build();
				}
				return originalResponse;
			}
		};
	}

	public static String httpsCommander(SSLSocketFactory sf, Map<String, String> requestHeader,
			Map<String, String> responseHeader, String address, String command, String method) throws Exception {
		// Properties propertiesClone =
		// ProzoneConfig.makeClone(ServiceManager.getCurrentRequestContext().getRequest());

		StringBuilder textBuilder = new StringBuilder();

		logger.info("[ InfraAPICaller-K8s ] httpsCommander ");
		StringBuilder sb = new StringBuilder();
		sb.append(Constants.HTTPS_SCHEME_PREFIX);
		sb.append(address);
		sb.append("/");
		sb.append(command);
		String serviceURL = sb.toString();
		logger.info("Service URL : " + serviceURL);

		try {
			URL url = new URL(serviceURL);
			HttpsURLConnection client = (HttpsURLConnection) url.openConnection();
			client.setSSLSocketFactory(sf);
			client.setRequestMethod(method == null ? "GET" : method);
			client.setDoInput(true);
			client.setDoOutput(true);
			client.setUseCaches(false);
			client.setConnectTimeout(30 * 1000);
			client.setReadTimeout(60 * 1000);
			client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			// set HTTPS Header
			for (String key : requestHeader.keySet()) {
				logger.info("[Request*Header]" + key + ": " + requestHeader.get(key));
				client.setRequestProperty(key, requestHeader.get(key));
			}

			InputStream is = client.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name()));
			Reader reader = new BufferedReader(isr);

			int c = 0;
			while ((c = reader.read()) != -1) {
				textBuilder.append((char) c);
			}

			reader.close();

			logger.info("[https]: " + textBuilder.toString());

		} catch (Exception e) {
			logger.info(e.getMessage());
			throw e;
		} finally {
		}
		return textBuilder.toString();

	}
	
	public static V1NamespaceList listNameSpace() throws Exception {
		V1NamespaceList nsList = null;
		try {
			nsList = api.listNamespace("true", false, null, null, null, 100, null, 60, false);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return nsList;
	}
	
	public static V1Namespace getNameSpace(String nsName) throws Exception {
		V1Namespace nameSpace = null;

		try {
			logger.info("nameSpace [ " + nsName + " ] Get Service Start");

			nameSpace = api.readNamespace(nsName, "true", false, false);
		
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return nameSpace;
	}
	
	public static void deleteNameSpace(String nsName) throws Exception {
		try {
			logger.info("nameSpace [ " + nsName + " ] Delete Service Start");
			V1Status deleteStatus = api.deleteNamespace(nsName, null, null, 0, null, "Background" , new V1DeleteOptions());
			logger.info("delete Status : "  + deleteStatus.getStatus());
			logger.info("delete message : "  + deleteStatus.getMessage());
			logger.info("delete reason : "  + deleteStatus.getReason());
			logger.info("delete whole : "  + deleteStatus.toString());

			logger.info("nameSpace [ " + nsName + " ] Deleted");
		} catch (IllegalStateException e) {
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	public static UserSecurityPolicyCR getUserSecurityPolicy(String uspName) throws Exception {
		UserSecurityPolicyCR uspCR = new UserSecurityPolicyCR();
		logger.info("UserSecurityPolicy [ " + uspName + " ] Get Service Start");

		try {
			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_USER_SECURITY_POLICY, uspName);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			uspCR = mapper.readValue((new Gson()).toJson(respJson), new TypeReference<UserSecurityPolicyCR>() {
			});
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return uspCR;
	}

	public static void createUserSecurityPolicy(String uspName) throws Exception {
		try {
			UserSecurityPolicyCR uspCR = new UserSecurityPolicyCR();
			// Set name & label
			V1ObjectMeta metadata = new V1ObjectMeta();
			metadata.setName( uspName );
			uspCR.setMetadata( metadata );

			// Set otpEnable false
			uspCR.setOtpEnable("f");

			// Make body
			JSONParser parser = new JSONParser();
			JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(uspCR));

			customObjectApi.createClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_USER_SECURITY_POLICY, bodyObj, null);
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}		
	}
	
	public static void patchUserSecurityPolicy( String uspName, String value) throws Throwable {
		logger.info("[K8S ApiCaller] patchUserSecurityPolicy Service Start");
		logger.info("uspName : " + uspName);
		logger.info("otp value : " + value);
		
		DateTime currentTime = new DateTime();
		logger.info("Current Time : " + currentTime );
			
		try {
			String jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/otp\",\"value\": " + Integer.parseInt(value) + " },"
					+ "{\"op\":\"replace\",\"path\":\"/otpRegisterTime\",\"value\": \"" + currentTime.toString() + "\" }]";
			logger.info("JsonPatchStr: " + jsonPatchStr);

			JsonElement jsonPatch = (JsonElement) new JsonParser().parse(jsonPatchStr);
			customObjectApi.patchClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_USER_SECURITY_POLICY, uspName, jsonPatch);
		
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
		
		
	}


//	public static void updateClusterRoleBindingOfGroup(String userGroupName, String userId) throws Exception {
//		V1ClusterRoleBindingList crbList = null;
//		boolean flag = false;
//		try{
//			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000 , null, 60, false);
//			for (V1ClusterRoleBinding item : crbList.getItems()) {
//				List<V1Subject> subjects = item.getSubjects();
////				V1RoleRef roleRef = item.getRoleRef();
//				if (subjects != null) {
//					for( V1Subject subject : subjects ) {
//						if ( subject.getKind().equalsIgnoreCase("Group")) {
//							if( subject.getName().equalsIgnoreCase( userGroupName )) {
//								// 유저 이름으로 만들어진 해당 클러스터 롤이 존재하는지 파악
//								for( V1Subject subject2 : subjects ) {
//									if ( subject2.getKind().equalsIgnoreCase("User")) {
//										if( subject2.getName().equalsIgnoreCase( userId )) {
//											flag = true;  // 이미 만들어짐
//										}
//									}
//								}
//								if (!flag) {
//									// 존재하지 않으면 subject 추가해준다 만들어준다
//									V1Subject newSubject = new V1Subject();
//									newSubject.setKind("User");
//									newSubject.setApiGroup("rbac.authorization.k8s.io");
//									newSubject.setName( userId );
//									item.addSubjectsItem(newSubject);
//									rbacApi.replaceClusterRoleBinding(item.getMetadata().getName(), item, null, null, null);
//								}	
//							}
//						}
//					}	
//				}			
//			}
//		} catch (Exception e) {
//			logger.info(e.getMessage());
//			throw e;
//		} finally {
//		}
//		
//	}
//
//	public static void deleteClusterRoleBindingOfGroup(Set<String> keySet, String userId) throws Exception {
//		V1ClusterRoleBindingList crbList = null;
//		boolean flag = false;
//		try{
//			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000 , null, 60, false);
//			for (V1ClusterRoleBinding item : crbList.getItems()) {
//				List<V1Subject> subjects = item.getSubjects();
////				V1RoleRef roleRef = item.getRoleRef();
//				if (subjects != null) {
//					for( int i=0 ; i < subjects.size(); i++) {   
//						V1Subject subject = subjects.get(i);
//						if ( subject.getKind().equalsIgnoreCase("User")) {
//							if( subject.getName().equalsIgnoreCase( userId )) {
//								// user로 맵핑 된 clusterRoleBinding 중  Group을 역시나 가지고 있는애가 있는지 파악
//								for( V1Subject subject2 : subjects ) {
//									if ( subject2.getKind().equalsIgnoreCase("Group")) {
//										for (String userGroupName : keySet) {
//											if( subject2.getName().equalsIgnoreCase( userGroupName )) {
//												flag = true;  // 존재함
//											}
//										}
//										if (!flag) {
//											// 존재하지 subject를 지우고 replace를 해라
//											subjects.remove(i);
//											item.setSubjects(subjects);
//											rbacApi.replaceClusterRoleBinding(item.getMetadata().getName(), item, null, null, null);
//										}
//										
//										
//									}
//								}
//							}
//						}
//					}	
//				}			
//			}
//		} catch (Exception e) {
//			logger.info(e.getMessage());
//			throw e;
//		} finally {
//		}
//	}
}
