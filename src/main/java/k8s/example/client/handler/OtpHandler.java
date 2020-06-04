package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

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
import k8s.example.client.Constants;
import k8s.example.client.DataObject.CommonOutDO;
import k8s.example.client.DataObject.LoginInDO;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.DataObject.UserSecurityPolicyCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.StringUtil;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;

public class OtpHandler extends GeneralHandler {
    private Logger logger = Main.logger;

	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /otp");
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
   
		LoginInDO loginInDO = null;
		String outDO = null;
		IStatus status = null;
		boolean otpEnable = false;
			try {
				// Read inDO
	    		loginInDO = new ObjectMapper().readValue(body.get( "postData" ), LoginInDO.class);
	    		logger.info( "  User ID: " + loginInDO.getId() );
	    		logger.info( "  User Password: " + loginInDO.getPassword() );
	    		
	    		// Validate
	    		if (StringUtil.isEmpty(loginInDO.getId())) 	throw new Exception(ErrorCode.USER_ID_EMPTY);
	    		if (StringUtil.isEmpty(loginInDO.getPassword()) || loginInDO.getPassword().equalsIgnoreCase(Constants.EMPTY_PASSWORD) ) 
	    			throw new Exception(ErrorCode.USER_PASSWORD_EMPTY);
	    				   		
	        	if (System.getenv( "PROAUTH_EXIST" ) != null) {  
	        		if( System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1")) {
		        		logger.info( "  [[ IntegratedAuth System! ]]" );  			
	        			//TODO : 요건 들어오면 하자
		    			status = Status.OK;			    			

	        		}
	    	    } 
	        	if (System.getenv( "PROAUTH_EXIST" ) == null || !System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1") ){	
	        		
	        		logger.info( "  [[ OpenAuth System! ]]" );  			
		    		// Get user info
		    		String userId = loginInDO.getId();
		    		UserCR user = K8sApiCaller.getUser(userId);
		    		String encryptedPassword = Util.Crypto.encryptSHA256(loginInDO.getPassword() + loginInDO.getId() + user.getUserInfo().getPasswordSalt());
		    		logger.info("  DB password: " + user.getUserInfo().getPassword() + " / Input password: " + encryptedPassword);
		    		
		    		if (user.getUserInfo().getPassword() != null ) {
		    			if(user.getUserInfo().getPassword().equals(encryptedPassword)) {
			    			logger.info(" Password Correct! ");		    			
			    			status = Status.OK;	
			    			try {
			    				UserSecurityPolicyCR uspCR = K8sApiCaller.getUserSecurityPolicy( loginInDO.getId() );
				            	
				            	// otpEnable true
				    			if (uspCR.getOtpEnable().equalsIgnoreCase("t")) {
				            		// Issue otpCode & Save into K8s
				        			String otpCode = Util.numberGen(6, 1);
				        			logger.info(" otpCode: " + otpCode);
				        			K8sApiCaller.patchUserSecurityPolicy(loginInDO.getId(), otpCode);

				        			// Send E-mail to User
				        			String subject = "인증번호 : " + otpCode;
				        			String content = Constants.OTP_VERIFICATION_CONTENTS.replaceAll("%%otpCode%%", otpCode);
				        			Util.sendMail(user.getUserInfo().getEmail(), subject, content); 
				        			otpEnable = true;
				    			} else {
					    			status = Status.OK;			    			
				    			}
			    			} catch ( ApiException e ) {
			    				if (e.getResponseBody().contains("NotFound")) {
				    				// If no USP, same as OTPEnable false
					    			status = Status.OK;			
			    				}else {
			    					logger.info( "Response body: " + e.getResponseBody() );
			    					e.printStackTrace();
			    					status = Status.UNAUTHORIZED;
			    					outDO = Constants.OTP_ERROR;
			    				}
			    			}
			    		} else {
			    			logger.info(" Wrong password.");	
			    			status = Status.BAD_REQUEST; 
			    			outDO = Constants.WRONG_PASSWORD;
			    		}
		    		} else {
		    			logger.info("  Login fail. Check if the user is belong to Integrated Auth User");
		    			status = Status.BAD_REQUEST; 
		    			outDO = Constants.LOGIN_FAILED;
		    		}	
	        	} 		   			
	    		 
			} catch (ApiException e) {
				logger.info( "Exception message: " + e.getResponseBody() );
				logger.info( "Exception message: " + e.getMessage() );
				
				if (e.getResponseBody().contains("NotFound")) {
					logger.info( "  Login fail. User not exist." );
					status = Status.BAD_REQUEST; //ui요청
					outDO = Constants.LOGIN_FAILED;
				} else {
					logger.info( "Response body: " + e.getResponseBody() );
					e.printStackTrace();
					
					status = Status.UNAUTHORIZED;
					outDO = Constants.LOGIN_FAILED;
				}
			} catch (Exception e) {
				logger.info( "Exception message: " + e.getMessage() );
				e.printStackTrace();
				status = Status.UNAUTHORIZED;
				outDO = Constants.LOGIN_FAILED;

			} catch (Throwable e) {
				logger.info("Exception message: " + e.getMessage());
				e.printStackTrace();
				status = Status.UNAUTHORIZED;
				outDO = Constants.LOGIN_FAILED;
			}
			
			
			// Make OutDO
			if (status.equals(Status.OK)){
        		// Make outDO
				Token loginOutDO = new Token();
    			loginOutDO.setOtpEnable(otpEnable);
    			logger.info("  otpEnable: " + otpEnable);
    			
    			Gson gson = new GsonBuilder().setPrettyPrinting().create();
    			outDO = gson.toJson(loginOutDO).toString();
        	} else if (status.equals(Status.UNAUTHORIZED)) {
				//Make OutDO
				CommonOutDO out = new CommonOutDO();
				out.setStatus(Constants.LOGIN_FAILED);
				out.setMsg(outDO);
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				outDO = gson.toJson(out).toString();
				
			} else if ( status.equals(Status.BAD_REQUEST)) { 
				CommonOutDO out = new CommonOutDO();
				out.setMsg(outDO);
    			status = Status.OK; //ui요청
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				outDO = gson.toJson(out).toString();
			} else if ( status.equals(Status.FORBIDDEN)) {
				CommonOutDO out = new CommonOutDO();
				out.setMsg(outDO);
    			status = Status.OK; //ui요청
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				outDO = gson.toJson(out).toString();
			}
		
 		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /login");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
	
}