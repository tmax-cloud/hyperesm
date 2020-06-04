package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.Main;
import k8s.example.client.models.StateCheckInfo;

public class UserWatcher extends Thread {
	private Watch<UserCR> watchUser;
	private static String latestResourceVersion = "0";
	private ApiClient client;
	private CustomObjectsApi api;
    private Logger logger = Main.logger;
	StateCheckInfo sci = new StateCheckInfo();

	UserWatcher(ApiClient client, CustomObjectsApi api, String resourceVersion) throws Exception {
		watchUser = Watch.createWatch(client,
				//api.listClusterCustomObjectCall("tmax.io", "v1", "users", null, null, null, "encrypted=f", null, resourceVersion, null, Boolean.TRUE, null),
				api.listClusterCustomObjectCall("tmax.io", "v1", "users", null, null, null, "encrypted=f", null, null, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<UserCR>>() {}.getType());
		this.client = client;
		this.api = api;
		this.latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				sci.checkThreadState();
				watchUser.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.info("Interrupted!");
							watchUser.close();
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
					
					latestResourceVersion = response.object.getMetadata().getResourceVersion();
					
					// Logic here
					try {
						logger.info("[UserWatcher] Encrypt password of User " + response.object.getMetadata().getName());

						K8sApiCaller.encryptUserPassword(
								response.object.getMetadata().getName(), 
								response.object.getUserInfo().getPassword(),
								response.object);
			    		
			    		// Create UserSecurityPolicy with otpEnable false
//						try {
//							K8sApiCaller.createUserSecurityPolicy(response.object.getMetadata().getName());  
//							logger.info("[UserWatcher] UserSecurityPolicy of User" + response.object.getMetadata().getName() + " Create Success");
//						} catch (ApiException e) {
//						}
					} catch (ApiException e) {
						logger.info("ApiException: " + e.getMessage());
						logger.info(e.getResponseBody());
					} catch (Exception e) {
						logger.info("Exception: " + e.getMessage());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						logger.info(sw.toString());
					}
				});
				logger.info("=============== User 'For Each' END ===============");
				watchUser = Watch.createWatch(client,
						api.listClusterCustomObjectCall("tmax.io", "v1", "users", null, null, null, "encrypted=f", null, null, null, Boolean.TRUE, null),
						new TypeToken<Watch.Response<UserCR>>() {}.getType());
			}
		} catch (Exception e) {
			logger.info("User Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
			if( e.getMessage().equals("abnormal") ) {
				logger.info("Catch abnormal conditions!! Exit process");
				System.exit(1);
			}
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
