package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1Ingress;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Main;

public class RegistryIngressWatcher extends Thread {
	private final Watch<ExtensionsV1beta1Ingress> watchRegistryIngress;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;

	RegistryIngressWatcher(ApiClient client, ExtensionsV1beta1Api api, String resourceVersion) throws Exception {
		watchRegistryIngress = Watch.createWatch(
		        client,
		        api.listIngressForAllNamespacesCall(null, null, null, "app=registry", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<ExtensionsV1beta1Ingress>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistryIngress.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.info("Interrupted!");
						watchRegistryIngress.close();
					}
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
				
				
				// Logic here
				try {
					ExtensionsV1beta1Ingress ingress = response.object;
					
					if( ingress != null
							&& Integer.parseInt(ingress.getMetadata().getResourceVersion()) > Integer.parseInt(latestResourceVersion)) {
						
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						logger.info("[RegistryIngressWatcher] Registry Ingress " + eventType + "\n"
						);

						K8sApiCaller.updateRegistryStatus(ingress, eventType);
						
					}
				} catch (ApiException e) {
//					logger.info("ApiException: " + e.getMessage());
//					logger.info(e.getResponseBody());
				} catch (Exception e) {
					logger.info("Exception: " + e.getMessage());
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.info(sw.toString());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			logger.info("@@@@@@@@@@@@@@@@@@@@ Registry Ingress 'For Each' END @@@@@@@@@@@@@@@@@@@@");
		} catch (Exception e) {
			logger.info("Registry Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
