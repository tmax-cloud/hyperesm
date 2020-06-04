package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Main;

public class RegistryServiceWatcher extends Thread {
	private final Watch<V1Service> watchRegistryService;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;
    
	RegistryServiceWatcher(ApiClient client, CoreV1Api api, String resourceVersion) throws Exception {
		watchRegistryService = Watch.createWatch(
		        client,
		        api.listServiceForAllNamespacesCall(null, null, null, "app=registry", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<V1Service>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistryService.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.info("Interrupted!");
						watchRegistryService.close();
					}
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
				
				
				// Logic here
				try {
					V1Service service = response.object;
					
					if( service != null
							&& Integer.parseInt(service.getMetadata().getResourceVersion()) > Integer.parseInt(latestResourceVersion)) {
						
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						logger.info("[RegistryServiceWatcher] Registry Service " + eventType + "\n");

						K8sApiCaller.updateRegistryStatus(service, eventType);
						
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
			logger.info("@@@@@@@@@@@@@@@@@@@@ Registry Service 'For Each' END @@@@@@@@@@@@@@@@@@@@");
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
