package k8s.example.client.handler;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import k8s.example.client.DataObject.User;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;

public class EmailHandler extends GeneralHandler {
	private Logger logger = Main.logger;

	@Override
	public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /Email");
		logger.info(" User Email Authenticate Code Send Service Start ");

		Map<String, String> body = new HashMap<String, String>();
		try {
			session.parseBody(body);
		} catch (Exception e) {
			e.printStackTrace();
		}

		User userInDO = null;
		String outDO = null;
		IStatus status = null;

		try {
			// Read inDO
			userInDO = new ObjectMapper().readValue(body.get("postData"), User.class);
			logger.info("  User E-Mail: " + userInDO.getEmail());

			// Validate
			if (userInDO.getEmail() == null)
				throw new Exception(ErrorCode.USER_MAIL_EMPTY);

			// Issue VerifyCode
			String verifyCode = Util.numberGen(4, 1);
			logger.info(" verifyCode: " + verifyCode); 

			// Insert VerifyCode into Secret
			try {
				V1Secret secretReturn = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE,
						Constants.K8S_PREFIX + Util.makeK8sFieldValue( userInDO.getEmail() ));

				// Delete old secret
				K8sApiCaller.deleteSecret(Constants.TEMPLATE_NAMESPACE, Util.makeK8sFieldValue( userInDO.getEmail() ), null);

				// Create new secret
				Map<String, String> createMap = new HashMap<>();
				createMap.put("verifycode", verifyCode);
				K8sApiCaller.createSecret(Constants.TEMPLATE_NAMESPACE, createMap, Util.makeK8sFieldValue( userInDO.getEmail() ),
						null, null, null);
			} catch (ApiException e) {
				logger.info("Exception message: " + e.getResponseBody());
				e.printStackTrace();
				Map<String, String> createMap = new HashMap<>();
				createMap.put("verifycode", verifyCode);
				K8sApiCaller.createSecret(Constants.TEMPLATE_NAMESPACE, createMap, Util.makeK8sFieldValue( userInDO.getEmail() ),
						null, null, null);
			}
			
			// Send E-mail to User
			String subject = "[ 인증번호 : " + verifyCode + " ] 이메일을 인증해 주세요";
			String content = Constants.VERIFY_MAIL_CONTENTS.replaceAll("@@verifyNumber@@", verifyCode);
			Util.sendMail(userInDO.getEmail(), subject, content);
			
			status = Status.CREATED;
			outDO = "User Email Authenticate Code Send Success";

		} catch (ApiException e) {
			logger.info("Exception message: " + e.getResponseBody());
			e.printStackTrace();

			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFICATION_NUMBER_SEND_FAIL;

		} catch (Exception e) {
			logger.info("Exception message: " + e.getMessage());

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFICATION_NUMBER_SEND_FAIL;

		} catch (Throwable e) {
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFICATION_NUMBER_SEND_FAIL;
		}

		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
	}

	public Response put( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
		logger.info("***** put/Email");
		logger.info(" User Email Verify Service Start ");
		
		IStatus status = null;
		String outDO = null; 
		User userInDO = null;

		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        try {
			String bodyStr = readFile(body.get("content"), Integer.valueOf(session.getHeaders().get("content-length")));
			logger.info("Body: " + bodyStr);	
			userInDO = new ObjectMapper().readValue(bodyStr, User.class);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		try {
			// Read inDO
    		logger.info( "  User E-Mail: " + userInDO.getEmail() );
    		logger.info( "  User VerifyCode: " + userInDO.getVerifyCode() );
    		
    		V1Secret secretReturn = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, Constants.K8S_PREFIX + Util.makeK8sFieldValue( userInDO.getEmail() ));
    		Map<String, byte[]> secretMap = new HashMap<>();
    		Map<String, String> returnMap = new HashMap<>();
    		secretMap = secretReturn.getData();
			for (String key : secretMap.keySet()) {
				returnMap.put(key, new String(secretMap.get(key)));
			}
			
//			DateTime currentTimeUTC = new DateTime().withZone(DateTimeZone.UTC);
			DateTime currentTimeUTC = new DateTime();
    		if (returnMap.size() != 0) {
//        		logger.info( "  currentTimeUTC.minusMinutes(Constants.VERIFICATAION_DURATION_MINUTES): " + currentTimeUTC.minusMinutes(Constants.VERIFICATAION_DURATION_MINUTES) );
//        		logger.info( "  secretReturn.getMetadata().getCreationTimestamp(): " + secretReturn.getMetadata().getCreationTimestamp() );
        		
    			 if( currentTimeUTC.minusMinutes(Constants.VERIFICATAION_DURATION_MINUTES).isBefore( secretReturn.getMetadata().getCreationTimestamp()) ) {
    				  if ( returnMap.get("verifycode").equalsIgnoreCase(userInDO.getVerifyCode()) ){
	    					status = Status.OK;
	 		        		outDO = "User Email Verify Success";
	 		        		// Delete Secret 
	 		        		K8sApiCaller.deleteSecret(Constants.TEMPLATE_NAMESPACE, Util.makeK8sFieldValue( userInDO.getEmail() ), null);	 
    				  } else {
    					status = Status.OK; //ui요청
    		    		outDO = "Verification Number is Wrong";		
    				  }
    			 } else {
    				 status = Status.OK;//ui요청
 		    		 outDO = "Authentication time has expired";
    			 }		
    		}	
    		
		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getResponseBody() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFY_FAIL;
			
		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFY_FAIL;
			
		} catch (Throwable e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFY_FAIL;
		}	
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));

	}

	@Override
	public Response other(String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /Email");

		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
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