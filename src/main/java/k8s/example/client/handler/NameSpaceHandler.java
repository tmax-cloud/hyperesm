package k8s.example.client.handler;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1Secret;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;
import k8s.example.client.metering.util.SimpleUtil;

public class NameSpaceHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response get(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** GET /nameSpace");
		
		IStatus status = null;
		String accessToken = null;
		V1NamespaceList nsList = null;
		String outDO = null; 
		String issuer = null;
		String userId = null;
		// if limit exists
		String limit = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_LIMIT );
		
		try {
			// Read AccessToken from Header
			if(!session.getHeaders().get("authorization").isEmpty()) {
				accessToken = session.getHeaders().get("authorization");
			} else {
				status = Status.BAD_REQUEST;
				throw new Exception(ErrorCode.TOKEN_EMPTY);
			}
    		logger.info( "  Token: " + accessToken );
    		
    		if (System.getenv( "PROAUTH_EXIST" ) != null) { 
        		if( System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1")) {
        			logger.info( "  [[ Integrated OAuth System! ]] " );
    	    		JsonObject webHookOutDO = OAuthApiCaller.webHookAuthenticate(accessToken);
    	    		if( webHookOutDO.get("status").getAsJsonObject().get("authenticated").toString().equalsIgnoreCase("true") ) {
    	    			userId = webHookOutDO.get("status").getAsJsonObject().get("user").getAsJsonObject().get("username").toString().replaceAll("\"", "");
    	    			logger.info( "  Token Validated " );
        				nsList = K8sApiCaller.getAccessibleNS(userId);
        				status = Status.OK;

        				// Limit 
        				if( nsList!= null) {
        					if( limit != null ) {
            					nsList.setItems( nsList.getItems().stream().limit(Integer.parseInt(limit)).collect(Collectors.toList()));		
            				}
        				}				
    	    		} else {
    	    			logger.info( "  Authentication fail" );
    	    			logger.info( "  Token is not valid" );
        				status = Status.UNAUTHORIZED;
        				outDO = "Get NameSpace List failed. Token is not valid.";
    	    		}
        		}
    		}
    		if (System.getenv( "PROAUTH_EXIST" ) == null || !System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1") ){	
        		logger.info( "  [[ OpenAuth System! ]]" );  			
        		// Verify access token	
    			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)).build();
    			DecodedJWT jwt = verifier.verify(accessToken);
    			
    			issuer = jwt.getIssuer();
    			userId = jwt.getClaims().get(Constants.CLAIM_USER_ID).asString();
    			String tokenId = jwt.getClaims().get(Constants.CLAIM_TOKEN_ID).asString();
    			logger.info( "  Issuer: " + issuer );
    			logger.info( "  User ID: " + userId );
    			logger.info( "  Token ID: " + tokenId );
    			
    			if(verifyAccessToken(accessToken, userId, tokenId, issuer)) {		
    				logger.info( "  Token Validated " );
    				nsList = K8sApiCaller.getAccessibleNS(userId);
    				status = Status.OK;

    				// Limit
    				if( nsList!= null) {
    					if( limit != null ) {
        					nsList.setItems( nsList.getItems().stream().limit(Integer.parseInt(limit)).collect(Collectors.toList()));		
        				}
    				} 
    			} else {
    				logger.info( "  Token is not valid" );
    				status = Status.UNAUTHORIZED;
    				outDO = "Get NameSpace List failed. Token is not valid.";
    			}
    		}

			// Make outDO					
    		if( nsList!=null ) {
    			Gson gson = new GsonBuilder().setPrettyPrinting().create();
    			outDO = gson.toJson( nsList ).toString();
    		} else {
    			status = Status.FORBIDDEN;
    			JsonObject result = new JsonObject();
    			outDO = "Cannot Access Any NameSpace";
    			result.addProperty("message", outDO);
    			Gson gson = new Gson();		
    		    outDO = gson.toJson(result);		
    		}
			
		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getMessage() );
			outDO = "Get NameSpace List failed.";
			status = Status.BAD_REQUEST;
			e.printStackTrace();

		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			outDO = "Get NameSpace List failed.";
			status = Status.BAD_REQUEST;
		}
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, "application/json", outDO));
    }
	
	public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** PUT /NameSpace");
		logger.info(" Trial Namespace Period Extend Service Start");

		IStatus status = null;
		String outDO = null;
		
		String nsName = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_NAMESPACE );
		String period = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_PERIOD );

		try {			
			// Read NameSpace
    		V1Namespace namespace = K8sApiCaller.getNameSpace(nsName);
    		
    		// Update Period Label
    		Map<String, String> labels = namespace.getMetadata().getLabels();
    		if ( labels.keySet().contains("period")) {
    			labels.replace("period", period);
    		}else {
    			labels.put("period", period);
    		}
    		
    		// Delete Exist Trial Timer with previous Period
    		Util.deleteTrialNSTimer ( nsName );
    		
    		// Set New Trial Timer 
    		Util.setTrialNSTimer(namespace);
    		
    		// patchNameSpace with new label
    		K8sApiCaller.replaceNamespace(namespace);
    		
			// Make outDO
			outDO = "Trial NameSpace Period Extend Success";
			status = Status.OK;    			
        			

		} catch (ApiException e) {
			logger.info("Exception message: " + e.getResponseBody());
			e.printStackTrace();

			status = Status.UNAUTHORIZED;
			outDO = Constants.TRIAL_PERIOD_EXTEND_FAILED;

		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.TRIAL_PERIOD_EXTEND_FAILED;

		} catch (Throwable e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.TRIAL_PERIOD_EXTEND_FAILED;
		}
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));

	}
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /authClient");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
	
	private boolean verifyAccessToken (String accessToken, String userId, String tokenId, String issuer) throws Exception {
		boolean result = false;		
	
		// for master token
		if(accessToken.equalsIgnoreCase(Constants.MASTER_TOKEN)) return true;
		
		String tokenName = userId.replace("@", "-") + "-" + tokenId;
		TokenCR token = K8sApiCaller.getToken(tokenName);
		
		accessToken = Util.Crypto.encryptSHA256(accessToken);
		
		if(issuer.equals(Constants.ISSUER) &&
				accessToken.equals(token.getAccessToken()))
			result = true;		
		
		return result;
	}
	
	private String readFile(String path, Integer length) {
		Charset charset = Charset.defaultCharset();
		String bodyStr = "";
		int byteCount;
		try {
			ByteBuffer buf = ByteBuffer.allocate(Integer.valueOf(length));
			FileInputStream fis = new FileInputStream(path);
			FileChannel dest = fis.getChannel();

			while (true) {
				byteCount = dest.read(buf);
				if (byteCount == -1) {
					break;
				} else {
					buf.flip();
					bodyStr += charset.decode(buf).toString();
					buf.clear();
				}
			}
			dest.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bodyStr;
	}

}
