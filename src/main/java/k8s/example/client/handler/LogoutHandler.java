package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.audit.AuditController;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;

public class LogoutHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /logout");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Token logoutInDO = null;
		String outDO = null;
		String userId = null;
		IStatus status = null;
		try {
			// Read inDO
			logoutInDO = new ObjectMapper().readValue(body.get( "postData" ), Token.class);
			String accessToken = logoutInDO.getAccessToken();
    		logger.info( "  Token: " + accessToken );
    		
    		// Integrated Auth or OpenAuth
    		if (System.getenv( "PROAUTH_EXIST" ) != null) {   		
        		if( System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1")) {
    	    		logger.info( "  [[ Integrated OAuth System! ]] " );
					JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)).build();
        			DecodedJWT jwt = verifier.verify(accessToken);
        			
        			userId = jwt.getClaims().get(Constants.CLAIM_USER_ID).asString();
        			logger.info(" User ID: " + userId);    	    		
        			JsonObject logOutOut = OAuthApiCaller.AuthenticateDelete(accessToken);
    	    		logger.info( "  logOutOut.get(\"result\") : " + logOutOut.get("result").toString() );
    	    		if ( logOutOut.get("result").toString().equalsIgnoreCase("\"true\"") ){
        				logger.info( "  Logout success." );
        				outDO = Constants.LOGOUT_SUCCESS;
    	    			status = Status.OK; 
    	    		} else {
    	    			logger.info("  LogOut failed by ProAuth.");		    			
		    			status = Status.OK; //ui요청
	    				outDO = Constants.LOGOUT_FAILED;
    	    		}

        		}
    	    } 
    		
        	if (System.getenv( "PROAUTH_EXIST" ) == null || !System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1") ){	
        		
        		logger.info( "  [[ OpenAuth System! ]]" );  			
        		// Verify access token	
    			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)).build();
    			DecodedJWT jwt = verifier.verify(accessToken);

    			userId = jwt.getClaims().get(Constants.CLAIM_USER_ID).asString();
    			String issuer = jwt.getIssuer();
    			String tokenId = jwt.getClaims().get(Constants.CLAIM_TOKEN_ID).asString();
    			logger.info( "  Issuer: " + issuer );
    			logger.info( "  User ID: " + userId );
    			logger.info( "  Token ID: " + tokenId );
    			
    			if(verifyAccessToken(accessToken, userId, tokenId, issuer)) {
    				status = Status.OK;
    				
    				String tokenName = userId.replace("@", "-") + "-" + tokenId;
    				logger.info( "  Logout success." );
    				K8sApiCaller.deleteToken(tokenName);
    				outDO = "Logout success.";
    			} else {
    				logger.info( "  Token is not valid" );
    				status = Status.UNAUTHORIZED;
    				outDO = Constants.LOGOUT_FAILED;
    			}
        	}
    		
		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getMessage() );
			
			if (e.getResponseBody().contains("NotFound")) {
				logger.info( "  Logout fail. Token not exist." );
				status = Status.UNAUTHORIZED;
				outDO = Constants.LOGOUT_FAILED;
			} else {
				logger.info( "Response body: " + e.getResponseBody() );
				e.printStackTrace();
				
				status = Status.UNAUTHORIZED;
				outDO = Constants.LOGOUT_FAILED;
			}
		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
			outDO = Constants.LOGOUT_FAILED;
		}
		
//		logger.info();
		AuditController.auditLogoutActivity(userId, session.getHeaders().get("user-agent"), status.getRequestStatus(), outDO);
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));

	}
	
	private boolean verifyAccessToken (String accessToken, String userId, String tokenId, String issuer) throws Exception {
		boolean result = false;		
	
		String tokenName = userId.replace("@", "-") + "-" + tokenId;
		TokenCR token = K8sApiCaller.getToken(tokenName);
		
		accessToken = Util.Crypto.encryptSHA256(accessToken);
		
		if(issuer.equals(Constants.ISSUER) &&
				accessToken.equals(token.getAccessToken()))
			result = true;
		
		return result;
	}
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /logout");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}