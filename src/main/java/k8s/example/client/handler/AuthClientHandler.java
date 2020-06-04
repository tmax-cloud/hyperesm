package k8s.example.client.handler;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.DataObject.Client;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.StringUtil;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.models.BrokerResponse;

public class AuthClientHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /authClient");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Client clientInfo = null;
		String outMsg = null;
		IStatus status = null;
		try {
			// Read inDO
			clientInfo = new ObjectMapper().readValue(body.get( "postData" ), Client.class);
			
			// Verify Input Values
			if( StringUtil.isEmpty(clientInfo.getAppName()))	throw new Exception(ErrorCode.APP_NAME_EMPTY);
			if( StringUtil.isEmpty(clientInfo.getOriginUri()))	throw new Exception(ErrorCode.ORIGIN_URI_EMPTY);
			if( StringUtil.isEmpty(clientInfo.getRedirectUri()))	throw new Exception(ErrorCode.REDIRECT_URI_EMPTY);
			
			// Issue Client ID & Secret
			clientInfo.setClientId(generateClientId());
			clientInfo.setClientSecret(generateClientSecret());
			
    		logger.info( "  Client Id: " + clientInfo.getClientId() );
    		logger.info( "  Client Secret: " + clientInfo.getClientSecret() );
    		logger.info( "  AppName: " + clientInfo.getAppName() );
    		logger.info( "  Origin URI: " + clientInfo.getOriginUri() );
    		logger.info( "  Redirect URI: " + clientInfo.getRedirectUri() );

			// Save clients in ClientCR
        	K8sApiCaller.saveClient(clientInfo);
			
			// Make outDO			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			outMsg = gson.toJson(clientInfo).toString();
			status = Status.OK;

		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getMessage() );
			outMsg = "Client Register failed.";
			status = Status.BAD_REQUEST;

		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			outMsg = "Client Register failed.";
			status = Status.BAD_REQUEST;
		}
//		logger.info();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outMsg));
    }
	
	@Override
    public Response delete(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** DELETE /v2/service_instances/:instance_id/service_bindings/:binding_id");
		
		String serviceClassName = session.getParameters().get("service_id").get(0);
		String instanceId = urlParams.get("instance_id");
		String bindingId = urlParams.get("binding_id");
		logger.info("Instance ID: " + instanceId);
		logger.info("Binding ID: " + bindingId);
		
		BrokerResponse response = new BrokerResponse();
		String outDO = null;
		IStatus status = null;
		
		try {
			response.setOperation("");
			status = Status.OK;
		} catch (Exception e) {
			logger.info( "  Failed to unbind instance of service class \"" + serviceClassName + "\"");
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.BAD_REQUEST;
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		outDO = gson.toJson(response).toString();
		logger.info("Response : " + outDO);
		
//		logger.info();
		return NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO);
    }
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /authClient");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
	
	
	private String generateClientId() {
		return UUID.randomUUID().toString();
	}
	
	private String generateClientSecret() {
		String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    String ALPHA = "abcdefghijklmnopqrstuvwxyz";
	    String NUMERIC = "0123456789";
	    
	    SecureRandom random = new SecureRandom();
		String result = "";
		String dic = ALPHA_CAPS + ALPHA + NUMERIC;
	    for (int i = 0; i < 30; i++) {
	        int index = random.nextInt(dic.length());
	        result += dic.charAt(index);
	    }
	    return result;
	}

}
