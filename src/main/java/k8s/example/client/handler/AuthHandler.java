package k8s.example.client.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.DataObject.TokenReview;
import k8s.example.client.DataObject.TokenReviewStatus;
import k8s.example.client.DataObject.TokenReviewUser;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;

public class AuthHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		//logger.info("***** POST /authenticate");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		String response = null;
		boolean authResult = false;
		try {			
			// Get token
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse( body.get( "postData" ) );
			String token = element.getAsJsonObject().get( "spec" ).getAsJsonObject().get( "token" ).getAsString();
			String issuer = null;
			String userId = null;
			
			
			//logger.info( "  Token: " + token );
			if ( !token.isEmpty() && token.equals( Constants.MASTER_TOKEN )) return Util.setCors( NanoHTTPD.newFixedLengthResponse( createAuthResponse( true, Constants.MASTER_USER_ID, null ) ) );
			logger.info( "  Token: " + token );

        	if (System.getenv( "PROAUTH_EXIST" ) != null) { 
        		if( System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1")) {
    	    		logger.info( "  [[ Integrated OAuth System! ]] " );
    	    		JsonObject webHookOutDO = OAuthApiCaller.webHookAuthenticate(token);
    	    		if( webHookOutDO.get("status").getAsJsonObject().get("authenticated").toString().equalsIgnoreCase("true") ) {
        				logger.info( "  Authentication success" );
    	    			userId = webHookOutDO.get("status").getAsJsonObject().get("user").getAsJsonObject().get("username").toString().replaceAll("\"", "");
        				authResult = true;
    	    		} else {
    	    			logger.info( "  Authentication fail" );
        				authResult = false;
    	    		}
        		}
        	}
        	
        	
        	if (System.getenv( "PROAUTH_EXIST" ) == null || !System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1") ){	
        		logger.info( "  [[ OpenAuth System! ]]" );  			

        		// Verify access token	
    			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)).build();
    			DecodedJWT jwt = verifier.verify(token);
    			
    			issuer = jwt.getIssuer();
    			userId = jwt.getClaims().get(Constants.CLAIM_USER_ID).asString();
    			String tokenId = jwt.getClaims().get(Constants.CLAIM_TOKEN_ID).asString();
    			logger.info( "  Issuer: " + issuer );
    			logger.info( "  User ID: " + userId );
    			logger.info( "  Token ID: " + tokenId );
    			
    			if(verifyAccessToken(token, userId, tokenId, issuer)) {
    				logger.info( "  Authentication success" );
    				authResult = true;
    			} else {
    				logger.info( "  Authentication fail" );
    				authResult = false;
    			}
    			
        	}

			// UserGroup GET	
			List < String > userGroupNameList = null;
			UserCR user = K8sApiCaller.getUser(userId);
			if (user.getMetadata().getLabels()!= null ) {
				Iterator<String> iter = user.getMetadata().getLabels().keySet().iterator();
				 while(iter.hasNext()) {
					String userGroupName = iter.next();
					if( userGroupName.startsWith("group-")) {
						if (userGroupNameList == null) userGroupNameList = new ArrayList<>();
						userGroupNameList.add(userGroupName.substring(6));
						logger.info( "  User Group Name: " + userGroupName.substring(6) );

					}	
				}
			}	
			
			response = createAuthResponse( authResult, userId, userGroupNameList );
		} catch (Exception e) {
			//logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			authResult = false;
		}
		
		//logger.info();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse( response ));

    }
	
	private boolean verifyAccessToken (String accessToken, String userId, String tokenId, String issuer) throws Exception {
		boolean result = false;		

		String tokenName = userId + "-" + tokenId;
		TokenCR token = K8sApiCaller.getToken(tokenName);
		
		accessToken = Util.Crypto.encryptSHA256(accessToken);
		
		if(issuer.equals(Constants.ISSUER) &&
				accessToken.equals(token.getAccessToken()))
			result = true;
		
		return result;
	}
	
	private String createAuthResponse( boolean authResult, String userId, List<String> groups ) {
		
		/*
		 * RESPONSE TRUE EXAMPLE
		 * {
			  "apiVersion": "authentication.k8s.io/v1beta1",
			  "kind": "TokenReview",
			  "status": {
			    "authenticated": true,
			    "user": {
			      "username": "seonho",
			      "uid": "9999",
			      "groups": [
			        "developers",
			        "qa"
			      ],
			      "extra": {
			        "extrafield1": [
			          "extravalue1",
			          "extravalue2"
			        ]
			      }
			    }
			  }
			}
		 */
		
		/*
		 * RESPONSE FALSE EXAMPLE
		 * {
			  "apiVersion": "authentication.k8s.io/v1beta1",
			  "kind": "TokenReview",
			  "status": {
			    "authenticated": false
			  }
			}
		 */
		
		TokenReview tr = new TokenReview();
		
		TokenReviewStatus trStatus = new TokenReviewStatus();
		trStatus.setAuthenticated(authResult);
		
		if(authResult) {
			TokenReviewUser trUser = new TokenReviewUser();
			trUser.setUsername(userId);
//			trUser.setUid("uid-xxxx");
			if (groups != null) {
				trUser.setGroups(groups);	
			}
			trStatus.setUser(trUser);
		} 
		
		tr.setStatus(trStatus);
		
		Gson gson = new Gson();
		String response = gson.toJson( tr );
		
		logger.info( "  Response: " + response );
		
		return response;
	}
}