
package k8s.example.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.models.Settings;

public class Main {
	public static Logger logger = LoggerFactory.getLogger("K8SOperator");
	public static void main(String[] args) {
		try {
			String token  = readFileLines(Constants.TOKEN_FILE_PATH);
			String namespace = readFileLines(Constants.NAMESPACE_FILE_PATH);
			
			logger.info( "Token : " + token );
			logger.info( "Namespace : " + namespace );
			
			if ( StringUtil.isEmpty(token) ) {
				logger.info( "[Exception] Service Account Token is Empty" );
			} else {
				Settings.setToken( token );
				if ( StringUtil.isEmpty( System.getenv(Constants.SYSTEM_ENV_NAMESPACE) ) && StringUtil.isEmpty( namespace ) ) {
					logger.info( "[Exception] Namespace Info is Empty" );
				} else {
					if ( StringUtil.isNotEmpty( System.getenv(Constants.SYSTEM_ENV_NAMESPACE) ) ) {
						Settings.setNamespace( System.getenv(Constants.SYSTEM_ENV_NAMESPACE) );
					} else if ( StringUtil.isNotEmpty( namespace ) ) {
						Settings.setNamespace( namespace );
					}
					
					// Start webhook server
					logger.info("[Main] Start webhook server");
					new HttpHandler();

					// Init K8S Client
					logger.info("[Main] Init K8S Client");
					K8sApiCaller.initK8SClient();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info( e.getMessage() );
		}
	}
	
	public static String readFileLines( String filePath ) {
		StringBuffer sb = new StringBuffer();
		try {
			List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
			for ( String line : lines ) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.info( e.getMessage() );
		}
	
		return sb.toString();
	}
}