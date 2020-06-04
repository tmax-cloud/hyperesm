package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.models.StateCheckInfo;

public class UserDeleteWatcher extends Thread {
	private Watch<UserCR> watchUser;
	private static String latestResourceVersion = "0";
	private ApiClient client;
	private CustomObjectsApi api;
	private Logger logger = Main.logger;
	StateCheckInfo sci = new StateCheckInfo();

	UserDeleteWatcher(ApiClient client, CustomObjectsApi api, String resourceVersion) throws Exception {
		watchUser = Watch.createWatch(client,
				// api.listClusterCustomObjectCall("tmax.io", "v1", "users", null, null, null,
				// "encrypted=f", null, resourceVersion, null, Boolean.TRUE, null),
				api.listClusterCustomObjectCall("tmax.io", "v1", "users", null, null, null, null, null, null, null,
						Boolean.TRUE, null),
				new TypeToken<Watch.Response<UserCR>>() {
				}.getType());
		this.client = client;
		this.api = api;
		this.latestResourceVersion = resourceVersion;
	}

	@Override
	public void run() {
		try {
			while (true) {
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
						String eventType = response.type.toString(); // ADDED, MODIFIED, DELETED
						logger.info("[UserDeleteWatcher] Event Type : " + eventType);

						switch (eventType) {
						case Constants.EVENT_TYPE_ADDED:
							// Nothing to do
							break;
						case Constants.EVENT_TYPE_MODIFIED:
							// Nothing to do
							break;
						case Constants.EVENT_TYPE_DELETED:
							logger.info("[UserDeleteWatcher] User ( " + response.object.getMetadata().getName()	+ " ) Deleted");
							if (System.getenv( "PROAUTH_EXIST" ) != null && System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1")) {
			    	    		logger.info( "  [[ Integrated OAuth System! ]] " );
								String userUuId = null;
								// Call ProAuth DetailUser to get user Uuid
								JsonObject detailUser = OAuthApiCaller.detailUser(response.object.getMetadata().getName());

								if (detailUser.get("result").toString().equalsIgnoreCase("\"true\"")) {
//									logger.info( "  User Detail Get by ProAuth success." );
									userUuId = detailUser.get("user").getAsJsonObject().get("uuid").toString()
											.replaceAll("\"", "");
								} else {
									logger.info("  User Detail Get by ProAuth Failed.");
									logger.info(detailUser.get("error").toString());
									throw new Exception(ErrorCode.NO_MATCHING_USER);
								}
								if (userUuId != null) {
									// Call ProAuth Delete User
									JsonObject deleteUser = OAuthApiCaller.deleteUser(userUuId);
									if (deleteUser.get("result").toString().equalsIgnoreCase("\"true\"")) {
										logger.info(" User [ " + response.object.getMetadata().getName()
												+ " ] delete Success in proAuth");
										K8sApiCaller.deleteClusterRole(response.object.getMetadata().getName());
										K8sApiCaller.deleteClusterRoleBinding(response.object.getMetadata().getName());
										logger.info(" ClusterRole & ClusterRoleBinding [ " + response.object.getMetadata().getName()
												+ " ] delete Success in K8S");
									} else {
										logger.info("  User Delete by ProAuth Failed.");
										logger.info(detailUser.get("error").toString());
										throw new Exception(ErrorCode.USER_DELETE_FAILED);
									}
								}
				        	}						
							break;
						}
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
				logger.info("=============== User 'For Each' END ===============");
				watchUser = Watch
						.createWatch(
								client, api.listClusterCustomObjectCall("tmax.io", "v1", "users", null, null, null,
										null, null, null, null, Boolean.TRUE, null),
								new TypeToken<Watch.Response<UserCR>>() {
								}.getType());
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
