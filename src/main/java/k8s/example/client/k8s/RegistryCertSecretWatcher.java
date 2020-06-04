package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Main;

public class RegistryCertSecretWatcher extends Thread {
	private final Watch<V1Secret> watchRegistrySecret;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;
    
	RegistryCertSecretWatcher(ApiClient client, CoreV1Api api, String resourceVersion) throws Exception {
		watchRegistrySecret = Watch.createWatch(
		        client,
		        api.listSecretForAllNamespacesCall(null, null, null, "secret=cert", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<V1Secret>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistrySecret.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.info("Interrupted!");
						watchRegistrySecret.close();
					}
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
				
				
				// Logic here
				try {
					V1Secret secret = response.object;
					
					if( secret != null
							&& Integer.parseInt(secret.getMetadata().getResourceVersion()) > Integer.parseInt(latestResourceVersion)) {
						
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						logger.info("[RegistryCertSecretWatcher] Registry Cert Secret " + eventType + "\n");

						K8sApiCaller.updateRegistryStatus(secret, eventType);
						
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
			logger.info("@@@@@@@@@@@@@@@@@@@@ Registry Cert Secret 'For Each' END @@@@@@@@@@@@@@@@@@@@");
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
