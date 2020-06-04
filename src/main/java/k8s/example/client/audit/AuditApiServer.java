package k8s.example.client.audit;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;

import org.slf4j.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import k8s.example.client.Util;
import k8s.example.client.audit.handler.AuditApiHandler;

@Deprecated
public class AuditApiServer extends RouterNanoHTTPD {
	
	public static Logger logger = AuditController.logger;
	
	public AuditApiServer(int port) throws Exception {
		super(port);
		this.addRoute("/audit", AuditApiHandler.class);
		
		/*
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] password = "tmax@23".toCharArray();
			FileInputStream fis = new FileInputStream("/run/secrets/tls/tls.jks");
			ks.load(fis, password);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password);
			
			this.makeSecure(NanoHTTPD.makeSSLSocketFactory(ks, kmf), null);
		} catch(Exception e) {
			logger.error("Failed to make audit server to https.");
			logger.error(Util.printExceptionError(e));
		}*/
	}
	
}
