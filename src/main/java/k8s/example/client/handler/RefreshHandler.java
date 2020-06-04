package k8s.example.client.handler;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
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
import io.kubernetes.client.openapi.models.V1Secret;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;

public class RefreshHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /refresh");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Token refreshInDO = null;
		Token refreshOutDO = null;
		String outDO = null;
		IStatus status = null;
		
		try {
			// Read inDO
			refreshInDO = new ObjectMapper().readValue(body.get( "postData" ), Token.class);
			logger.info( "  Access token: " + refreshInDO.getAccessToken() );
    		logger.info( "  Refresh token: " + refreshInDO.getRefreshToken() );
			
    		// Integrated Auth or OpenAuth
    		if (System.getenv( "PROAUTH_EXIST" ) != null) {   		
        		if( System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1")) {
        			
    	    		logger.info( "  [[ Integrated OAuth System! ]] " );
    	    		JsonObject refreshOut = OAuthApiCaller.AuthenticateUpdate(refreshInDO.getRefreshToken());
    	    		logger.info( "  Oauth Call Result : " + refreshOut.get("result").toString() );
    	    		logger.info( "  New Access Token : " + refreshOut.get("token").toString() );
    	    		logger.info( "  New Refresh Token : " + refreshOut.get("refresh_token").toString() );
    	    		
    	    		if ( refreshOut.get("result").toString().equalsIgnoreCase("\"true\"") ){
        				logger.info( "  refresh success." );
        				
        				// Make outDO
            			refreshOutDO = new Token();
            			refreshOutDO.setAccessToken(refreshOut.get("token").toString().replaceAll("\"", ""));
            			refreshOutDO.setRefreshToken(refreshOut.get("refresh_token").toString().replaceAll("\"", ""));
            			Gson gson = new GsonBuilder().setPrettyPrinting().create();
            			outDO = gson.toJson(refreshOutDO).toString();
    	    			status = Status.OK; 
    	    		} else {
    	    			logger.info(" Refresh failed by ProAuth.");		    			
		    			status = Status.UNAUTHORIZED; //ui요청
	    				outDO = Constants.REFRESH_FAILED;
    	    		}
        		}
    		}
    		
        	if (System.getenv( "PROAUTH_EXIST" ) == null || !System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1") ){

        		logger.info( "  [[ OpenAuth System! ]]" );  			
        		// Get token name
        		DecodedJWT jwt = JWT.decode(refreshInDO.getAccessToken());
        		String userId = jwt.getClaim(Constants.CLAIM_USER_ID).asString();
        		String tokenId = jwt.getClaim(Constants.CLAIM_TOKEN_ID).asString();
        		logger.info( "  User ID: " + userId );
        		logger.info( "  Token ID: " + tokenId );
        		String tokenName = userId + "-" + tokenId;
        		
    			// Verify refresh token	
    			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.REFRESH_TOKEN_SECRET_KEY)).build();
    			try {
    				jwt = verifier.verify(refreshInDO.getRefreshToken());
    			} catch (Exception e) {
    				logger.info( "Exception message: " + e.getMessage() );
    				K8sApiCaller.deleteToken(tokenName);
    			}

    			String issuer = jwt.getIssuer();
    			logger.info( "  Issuer: " + issuer );
    			
    			if(verifyRefreshToken(refreshInDO.getAccessToken(), refreshInDO.getRefreshToken(), tokenName, issuer)) {
    				logger.info( "  Refresh success" );	
    				status = Status.OK;
    				
    				// Get Access Token Expire Time from secret
    				int atExpireTimeSec = 3600;
    				try {
        				V1Secret secretReturn = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, Constants.K8S_PREFIX + Constants.OPERATOR_TOKEN_EXPIRE_TIME );
        				atExpireTimeSec = Integer.parseInt( new String(secretReturn.getData().get(Constants.TOKEN_EXPIRED_TIME_KEY)));

        			} catch (ApiException e) {
        				logger.info("Token Expire Time Secret does not exist Yet, Set Default value 60 min");
        			}
    				
    				// Make a new access token	  				
    				Builder tokenBuilder = JWT.create().withIssuer(Constants.ISSUER)
    						.withExpiresAt(Util.getDateFromSecond(atExpireTimeSec))
    						.withClaim(Constants.CLAIM_USER_ID, userId)
    						.withClaim(Constants.CLAIM_TOKEN_ID, tokenId);
    				
    				// TODO
        			if ( userId.equals( Constants.MASTER_USER_ID ) ) {
        				tokenBuilder.withClaim( Constants.CLAIM_ROLE, Constants.ROLE_ADMIN );
        			} else {
        				tokenBuilder.withClaim( Constants.CLAIM_ROLE, Constants.ROLE_USER );
        			}
        			
        			String newAccessToken = tokenBuilder.sign(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY));
        			logger.info( "  New access token: " + newAccessToken );
        			
        			// Make outDO
        			refreshOutDO = new Token();
        			refreshOutDO.setAccessToken(newAccessToken);
        			refreshOutDO.setAtExpireTime(refreshInDO.getAtExpireTime());
        			Gson gson = new GsonBuilder().setPrettyPrinting().create();
        			outDO = gson.toJson(refreshOutDO).toString();
        			
        			// Update access token
        			K8sApiCaller.updateAccessToken(tokenName, Util.Crypto.encryptSHA256(newAccessToken));
    			} else {
    				logger.info( "  Refresh fail" );
    				status = Status.UNAUTHORIZED;
    				outDO = Constants.REFRESH_FAILED;
    			}
        	}  		
		} catch (Exception e) {
			logger.info( "  Refresh fail" );
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.REFRESH_FAILED;
		}
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
	}
	
	public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** PUT /Refresh");
		logger.info(" Token Expire Time Change Service Start");

		IStatus status = null;
		String outDO = null;
		
		Token refreshInDO = null;
		Token refreshOutDO = null;
		int atExpireTimeSec = 0;

		Map<String, String> body = new HashMap<String, String>();
		try {
			session.parseBody(body);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String bodyStr = readFile(body.get("content"), Integer.valueOf(session.getHeaders().get("content-length")));
			logger.info("Body: " + bodyStr);
			refreshInDO = new ObjectMapper().readValue(bodyStr, Token.class);
			
    		if ( refreshInDO.getAtExpireTime() < 1 || refreshInDO.getAtExpireTime() >720 ) throw new Exception(ErrorCode.INVALID_TOKEN_EXPIRED_TIME);
    		atExpireTimeSec = refreshInDO.getAtExpireTime() * 60;
			logger.info( "  AT Expire Time : " +  atExpireTimeSec/60  + " min");
			
    		// Integrated Auth or OpenAuth
    		if (System.getenv( "PROAUTH_EXIST" ) != null) {   		
        		if( System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1")) {
        			
    	    		logger.info( "  [[ Integrated OAuth System! ]] " );
    	    		JsonObject atExpireUpdateOut = OAuthApiCaller.ConfigurationUpdate(Constants.TOKEN_EXPIRED_TIME_KEY, Integer.toString(atExpireTimeSec));
    	    		JsonObject rtExpireUpdateOut = OAuthApiCaller.ConfigurationUpdate(Constants.REFRESH_TOKEN_EXPIRED_TIME_KEY, Integer.toString(atExpireTimeSec));
//    	    		logger.info( "  Oauth Call Result : " + configurationUpdateOut.get("result").toString() );
//    	    		logger.info( "  Updated Key : " + configurationUpdateOut.get("key").toString() );
//    	    		logger.info( "  Updated Value : " + configurationUpdateOut.get("value").toString() );
    	    		
    	    		if ( atExpireUpdateOut.get("result").toString().equalsIgnoreCase("\"true\"") && rtExpireUpdateOut.get("result").toString().equalsIgnoreCase("\"true\"")){
        				logger.info( " Token Expire Time Change Service success." );     
        				// Make outDO
            			refreshOutDO = new Token();
            			refreshOutDO.setAtExpireTime(Integer.parseInt(atExpireUpdateOut.get("value").toString().replaceAll("\"", ""))/60);
            			Gson gson = new GsonBuilder().setPrettyPrinting().create();
            			outDO = gson.toJson(refreshOutDO).toString();
    	    			status = Status.OK; 
    	    		} else {
    	    			logger.info(" Token Expire Time Change Service failed by ProAuth.");		    			
		    			status = Status.UNAUTHORIZED; 
	    				outDO = Constants.EXPIRE_TIME_UPDATE_FAILED;
    	    		}
        		}
    		}
    		
        	if (System.getenv( "PROAUTH_EXIST" ) == null || !System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1") ){
        		
        		logger.info( "  [[ OpenAuth System! ]]" );  			//TODO : 나중에 한번 다시 보자!
        		try {
    				V1Secret secretReturn = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, Constants.K8S_PREFIX + Constants.OPERATOR_TOKEN_EXPIRE_TIME );
    				Map<String, String> patchMap = new HashMap<>();
    				patchMap.put(Constants.TOKEN_EXPIRED_TIME_KEY, Integer.toString(atExpireTimeSec));
    				try {
						K8sApiCaller.patchSecret(Constants.TEMPLATE_NAMESPACE, patchMap, Constants.OPERATOR_TOKEN_EXPIRE_TIME, null);
					} catch (Throwable e1) {
						e1.printStackTrace();
					} 
    			} catch (ApiException e) {
    				logger.info("Exception message: " + e.getResponseBody());
    				e.printStackTrace();
    				
    				// Create new secret
    				Map<String, String> createMap = new HashMap<>();
    				createMap.put(Constants.TOKEN_EXPIRED_TIME_KEY, Integer.toString(atExpireTimeSec));
    				K8sApiCaller.createSecret(Constants.TEMPLATE_NAMESPACE, createMap, Constants.OPERATOR_TOKEN_EXPIRE_TIME ,null, null, null);
    			}
        		
        		logger.info( " Token Expire Time Change Service success." );
				
				// Make outDO
    			refreshOutDO = new Token();
    			refreshOutDO.setAtExpireTime(atExpireTimeSec);
    			Gson gson = new GsonBuilder().setPrettyPrinting().create();
    			outDO = gson.toJson(refreshOutDO).toString();
    			status = Status.OK;    			
        	}  		

		} catch (ApiException e) {
			logger.info("Exception message: " + e.getResponseBody());
			e.printStackTrace();

			status = Status.UNAUTHORIZED;
			outDO = Constants.EXPIRE_TIME_UPDATE_FAILED;

		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.EXPIRE_TIME_UPDATE_FAILED;

		} catch (Throwable e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.EXPIRE_TIME_UPDATE_FAILED;
		}
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));

	}

	
	private boolean verifyRefreshToken(String accessToken, String refreshToken, String tokenName, String issuer) throws Exception {
		boolean result = false;		

		TokenCR token = K8sApiCaller.getToken(tokenName);
		
		accessToken = Util.Crypto.encryptSHA256(accessToken);
		refreshToken = Util.Crypto.encryptSHA256(refreshToken);
		
		logger.info("  In AccessToken : " + accessToken);
		logger.info("  DB AccessToken : " + token.getAccessToken());
		
		logger.info("  In RefreshToken : " + refreshToken);
		logger.info("  DB RefreshToken : " + token.getRefreshToken());
		
		if(issuer.equals(Constants.ISSUER) &&
				accessToken.equals(token.getAccessToken()) &&
				refreshToken.equals(token.getRefreshToken()))
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
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /refresh");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}