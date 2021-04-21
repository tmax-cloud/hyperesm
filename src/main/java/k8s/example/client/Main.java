
package k8s.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import k8s.example.client.k8s.K8sApiCaller;

public class Main {
	public static Logger logger = LoggerFactory.getLogger("HyperESM");
	public static void main(String[] args) {
		try {		
			// Start HyperESM
			logger.info("[Main] Init & Start HyperESM");
			new HttpHandler();
			K8sApiCaller.initK8SClient();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info( e.getMessage() );
		}
	}
}