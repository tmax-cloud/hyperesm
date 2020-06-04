package k8s.example.client.k8s;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import k8s.example.client.Constants;
import k8s.example.client.DataObject.User;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.StringUtil;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OAuthApiCaller {
	
	private static final String CALLER_NAME = "OAuthApiCaller";
    private static Logger logger = Main.logger;
    static OkHttpClient client = new OkHttpClient();

	private static String setAuthURL( String serviceName )  {
		int proauthHttpPort = 8080;
		if ( StringUtil.isNotEmpty(System.getenv( "PROAUTH_HTTP_PORT" ))){
			proauthHttpPort = Integer.parseInt( System.getenv( "PROAUTH_HTTP_PORT" ) );
		}
		return Constants.OAUTH_URL + ":" + proauthHttpPort +"/" + serviceName;
	}
	
//	public static void init() throws Exception {
//		
////		if ( loginSuccess( "admin@tmax.co.kr", "admin" ) ) {
////			updateAdminPW();
////		}
//	}
	
	public static JsonArray listUser() throws IOException {
		logger.info( " [OAuth] User List Service Start" );
		
		//GET svc
		Request request = new Request.Builder().url(setAuthURL( Constants.SERVICE_NAME_OAUTH_USER_LIST )).build();
	    Response response = client.newCall(request).execute();

	    String result = response.body().string();   
		logger.info(" result : " + result);

	    Gson gson = new Gson();
	    JsonObject userList = gson.fromJson(result, JsonObject.class);
	    return userList.get("user").getAsJsonArray(); 
	}
	
	public static JsonObject detailUser( String userId ) throws Exception {

	    Request request = null;
	    JsonObject userDetail = null;
	  //GET svc
		HttpUrl.Builder urlBuilder = HttpUrl.parse(setAuthURL( Constants.SERVICE_NAME_OAUTH_USER_DETAIL )).newBuilder();
		urlBuilder.addQueryParameter("userId", userId);
	    String url = urlBuilder.build().toString();
	    request = new Request.Builder().url(url).get().build();
	    try {
		    Response response = client.newCall(request).execute();

		    String result = response.body().string();   
//			logger.info(" result : " + result);

		    Gson gson = new Gson();
		    userDetail = gson.fromJson(result, JsonObject.class);
	    } catch (Exception e) {
	    	e.printStackTrace();	    	
	    	throw e;
	    }
	    return userDetail; 
	}

	public static JsonObject createUser( User userInDO ) throws IOException {
		logger.info( " [OAuth] User Create Service Start" );
	
		JsonObject createUserInDO = new JsonObject();		
		createUserInDO.addProperty("user_id", userInDO.getId());
		createUserInDO.addProperty("password", userInDO.getPassword());
		
		Gson gson = new Gson();
	    String json = gson.toJson(createUserInDO);
		
	    //POST svc
	    Request request = new Request.Builder().url(setAuthURL( Constants.SERVICE_NAME_OAUTH_USER_CREATE ))
	            .post(RequestBody.create(MediaType.parse("application/json"), json)).build();
	    
		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.info(" result : " + result);

	    JsonObject userCreateOut = gson.fromJson(result, JsonObject.class);
	    return userCreateOut; 
	}
	
	public static JsonObject deleteUser( String userUuId ) throws IOException {
//		logger.info( " [OAuth] User Delete Service Start" );
	    Request request = null;
//		logger.info( " User UUID : " + userUuId );

		//GET svc
		HttpUrl.Builder urlBuilder = HttpUrl.parse(setAuthURL( Constants.SERVICE_NAME_OAUTH_USER_DELETE )).newBuilder();
		urlBuilder.addPathSegment(userUuId);
	    String url = urlBuilder.build().toString();
	    request = new Request.Builder().url(url).delete().build();
		
	    Response response = client.newCall(request).execute();

	    String result = response.body().string();   
//		logger.info(" result : " + result);

	    Gson gson = new Gson();
	    JsonObject userDelete = gson.fromJson(result, JsonObject.class);
	    return userDelete; 
	}
	
	public static JsonObject AuthenticateCreate( String id, String password ) throws IOException {
		logger.info( " [OAuth] Login Service Start" );
	
		JsonObject loginInDO = new JsonObject();		
		loginInDO.addProperty("user_id", id);
		loginInDO.addProperty("password", password);
				
		Gson gson = new Gson();		
	    String json = gson.toJson(loginInDO);
    
	    //POST svc
	    Request request = new Request.Builder().url(setAuthURL( Constants.SERVICE_NAME_OAUTH_AUTHENTICATE_CREATE ))
	            .post(RequestBody.create(MediaType.parse("application/json"), json)).build();
	    
		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.info(" result : " + result);

	    JsonObject authenticateCreateOut = gson.fromJson(result, JsonObject.class);
	    return authenticateCreateOut; 
	}
	
	public static JsonObject AuthenticateDelete( String accessToken ) throws IOException {
		logger.info( " [OAuth] Logout Service Start" );
	
		logger.info(" AccessToken : " + accessToken);	
		
		Gson gson = new Gson();		
	    //DELETE svc
	    Request request = new Request.Builder().addHeader("token", accessToken)
	    		.url(setAuthURL( Constants.SERVICE_NAME_OAUTH_AUTHENTICATE_DELETE )).delete().build();
	    
		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.info(" result : " + result);

	    JsonObject authenticateDeleteOut = gson.fromJson(result, JsonObject.class);
	    return authenticateDeleteOut; 
	}
	
	public static JsonObject AuthenticateUpdate( String refreshToken ) throws IOException {
		logger.info( " [OAuth] Refresh Service Start" );
	
		logger.info(" RefreshToken : " + refreshToken);	
		
		JsonObject refreshInDO = new JsonObject();		
		refreshInDO.addProperty("refresh_token", refreshToken);
				
		Gson gson = new Gson();		
	    String json = gson.toJson(refreshInDO);
	    
	    //PUT svc
	    Request request = new Request.Builder()
	    		.url(setAuthURL( Constants.SERVICE_NAME_OAUTH_AUTHENTICATE_UPDATE )).put(RequestBody.create(MediaType.parse("application/json"), json)).build();
	    
		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.info("result : " + result);

	    JsonObject authenticateUpdateOut = gson.fromJson(result, JsonObject.class);
	    return authenticateUpdateOut; 
	}
	
	public static JsonObject ConfigurationUpdate( String key, String value ) throws IOException {
		logger.info( " [OAuth] ConfigurationUpdate Service Start" );
	
		logger.info(" key : " + key);	
		logger.info(" value : " + value);	
		
		JsonObject ConfigurationUpdateInDO = new JsonObject();		
		ConfigurationUpdateInDO.addProperty("key", key);
		ConfigurationUpdateInDO.addProperty("value", value);
				
		Gson gson = new Gson();		
	    String json = gson.toJson(ConfigurationUpdateInDO);
	    
	    //PUT svc
	    Request request = new Request.Builder()
	    		.url(setAuthURL( Constants.SERVICE_NAME_OAUTH_CONFIGURATION_UPDATE )).put(RequestBody.create(MediaType.parse("application/json"), json)).build();
	    
		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.info("result : " + result);

	    JsonObject ConfigurationUpdateOut = gson.fromJson(result, JsonObject.class);
	    return ConfigurationUpdateOut; 
	}
	
	public static JsonObject SetPasswordService( String id,  String alterPassword ) throws IOException {
		logger.info( "[OAuth] Password Change Service Start" );
	
		logger.info(" User ID : " + id);	
		logger.info(" Alter Password : " + alterPassword);	
	    
		JsonObject setPasswordInDO = new JsonObject();		
		setPasswordInDO.addProperty("password", alterPassword);
		setPasswordInDO.addProperty("confirmPassword", alterPassword);
				
		Gson gson = new Gson();		
	    String json = gson.toJson(setPasswordInDO);
	    Request request = null;
	
//	    POST svc	    
		HttpUrl.Builder urlBuilder = HttpUrl.parse(setAuthURL( Constants.SERVICE_NAME_SET_PASSWORD_SERVICE )).newBuilder();
	    urlBuilder.addQueryParameter("id", id);
	    String url = urlBuilder.build().toString();
	    request = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("application/json"), json)).build();
	    
		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.info("result : " + result);

	    JsonObject setPasswordOut = gson.fromJson(result, JsonObject.class);
	    return setPasswordOut; 
	}

	public static void deleteBlockedUser(List<String> deletedUserIdList) throws Exception {
//		logger.info( " [OAuth] Delete Blocked User Service Start" );
		String userUuId = null;
		for ( String deletedUserId : deletedUserIdList ) {
			JsonObject detailUser = detailUser( deletedUserId ); 
			
			if ( detailUser.get("result").toString().equalsIgnoreCase("\"true\"") ){
//				logger.info( "  User Detail Get by ProAuth success." );
				userUuId = detailUser.get("user").getAsJsonObject().get("uuid").toString().replaceAll("\"", ""); 
    		} else {
    			logger.info("  User Detail Get by ProAuth Failed.");
    			logger.info( detailUser.get("error").toString());
				throw new Exception(ErrorCode.NO_MATCHING_USER);
    		}
			if (userUuId != null) {
				JsonObject deleteUser = deleteUser ( userUuId );
				if ( deleteUser.get("result").toString().equalsIgnoreCase("\"true\"") ){
					logger.info(  " Bloced User [ " + deletedUserId + " ] delete Success in proAuth");
	    		} else {
	    			logger.info("  User Delete by ProAuth Failed.");
	    			logger.info( detailUser.get("error").toString());
					throw new Exception(ErrorCode.USER_DELETE_FAILED);
	    		}
			}	
		}	
	}

	public static JsonObject webHookAuthenticate(String token) throws IOException {
		logger.info( " [OAuth] WebHook Authenticate Service Start" );
		
		logger.info(" AcccessToken : " + token);	
				
		JsonObject webHookInDO = new JsonObject();		
		JsonObject spec = new JsonObject();		
		spec.addProperty("token", token);
		webHookInDO.add("spec", spec);
				
		Gson gson = new Gson();		
	    String json = gson.toJson(webHookInDO);
	    
	    //PUT svc
	    Request request = new Request.Builder()
	    		.url(setAuthURL( Constants.SERVICE_NAME_WEBHOOK_SAMPLE )).post(RequestBody.create(MediaType.parse("application/json"), json)).build();
	    
		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.info("result : " + result);

	    JsonObject webHookOutDO = gson.fromJson(result, JsonObject.class);
	    return webHookOutDO; 
	
	}
	
//	public static ContextDataObject makeUserCreateInDO( UserDO userInDO ) throws ProObjectException {
//		ContextDataObject userCreateIn = new ContextDataObject();
//		ContextDataObject metaIn = new ContextDataObject();
//
//		metaIn.set( "name", userInDO.getName() );
//		metaIn.set( "email", userInDO.getEmail() );
//		metaIn.set( "department", userInDO.getDepartment() );
//		metaIn.set( "position", userInDO.getPosition() );
//		metaIn.set( "mobile", userInDO.getMobile() );
//		metaIn.set( "description", userInDO.getDescription() );
//		
//		userCreateIn.set( "user_id", userInDO.getId() );
//		userCreateIn.set( "password", userInDO.getPassword() );
//		userCreateIn.set( "meta" , metaIn );
//		
//		List < String > roleList = new ArrayList<>();
//		for ( int i = 0; i < userInDO.getRoleIdList().size(); i++ ) {
//			roleList.add( userInDO.getRoleIdList().get( i ) );
//		}
//		roleList.add( Constants.CONSUMER_ROLE_ID );
//		
//		userCreateIn.set( "role_list" , roleList );
//
//		return userCreateIn;
//	}
//	
//	public static void updateUser( String userId, List<String> roleAddList, List<String> roleDeleteList ) throws ProObjectException {
//		logger.info( "[OAuth] User Update Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_USER_UPDATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		request.setWebHeader( ServiceManager.getCurrentRequestContext().getRequest().getWebHeader() );
//		
//		Map<String, String> restMap = new HashMap<>();
//		restMap.put( "users", userId );
//		request.setRESTMap( restMap );
//		
//		ContextDataObject userUpdateIn = new ContextDataObject();
//		userUpdateIn.set( "role_add", roleAddList );
//		userUpdateIn.set( "role_delete", roleDeleteList );
//		
//		ContextDataObject updateUser = new ContextDataObject();
//		updateUser.set( "update", userUpdateIn );
//		
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_USER_UPDATE, updateUser );
//		
//		if( !outDO.get( "result" ).equals( "true" ) ) {
//			PaaSExceptionFunc.throwPOException( PaaSErrorCode.OAUTH.OauthSerivceError, ( String )outDO.get( "error" ) );
//		}
//	}
//	
//	public static String deleteUser( String userId ) throws ProObjectException {
//		logger.info( "[OAuth] User Delete Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_USER_DELETE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		request.setWebHeader( ServiceManager.getCurrentRequestContext().getRequest().getWebHeader() );
//		
//		Map < String, String > restMap = new HashMap<>();
//		restMap.put( "users", userId );
//		
//		request.setRESTMap( restMap );
//		
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_USER_DELETE, new ContextDataObject() );
//		
//		if( !outDO.get( "result" ).equals( "true" ) ) {
//			PaaSExceptionFunc.throwPOException( PaaSErrorCode.OAUTH.OauthSerivceError, ( String )outDO.get( "error" ) );
//		}
//		
//		return outDO.get( "uuid" );
//	}
//	
//	public static String createPermission( String objectType, String object, String action ) throws ServiceException {		
//		logger.info( "[OAuth] Permission Create Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_PERMISSION_CREATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		request.setWebHeader( ServiceManager.getCurrentRequestContext().getRequest().getWebHeader() );
//		
//		ContextDataObject permissionCreateIn = new ContextDataObject();
//		permissionCreateIn.set( "object_type", objectType );
//		permissionCreateIn.set( "object", object );
//		permissionCreateIn.set( "action", action );
//		permissionCreateIn.set( "description", object + " " + action + " permission" );
//		
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_PERMISSION_CREATE, permissionCreateIn );
//		
//		logger.info( "Permission ID : " + outDO.get( "uuid" ) );
//		return outDO.get( "uuid" );
//	}
//	
//	public static void deletePermission( String permissionId ) throws ProObjectException {		
//		logger.info( "[OAuth] Permission Delete Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_PERMISSION_DELETE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		request.setWebHeader( ServiceManager.getCurrentRequestContext().getRequest().getWebHeader() );
//		
//		Map<String, String> restMap = new HashMap<String, String>();
//		restMap.put("permissions", permissionId);
//		request.setRESTMap(restMap);
//		
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_PERMISSION_DELETE, new ContextDataObject() );
//		
//		if( !outDO.get( "result" ).equals( "true" ) ) {
//			PaaSExceptionFunc.throwPOException( PaaSErrorCode.OAUTH.OauthSerivceError, ( String )outDO.get( "error" ) );
//		}
//	}
//	
//	public static String createRole( String name, String parentRoleId, List<String> permissionList ) throws ServiceException {			
//		logger.info( "[OAuth] Role Create Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_ROLE_CREATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		request.setWebHeader( ServiceManager.getCurrentRequestContext().getRequest().getWebHeader() );
//		
//		ContextDataObject roleCreateIn = new ContextDataObject();
//		roleCreateIn.set( "name", name );
//		roleCreateIn.set( "description", name + " role" );
//		roleCreateIn.set( "permission_list", permissionList );
//		
//		List<String> adminRole = new ArrayList<>();
//		if(parentRoleId != null) adminRole.add( parentRoleId );
//		else adminRole.add( Constants.ADMIN_ROLE_ID );
//		roleCreateIn.set( "parent_list", adminRole );
//
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_ROLE_CREATE, roleCreateIn );
//		
//		logger.info( "Role ID : " + outDO.get( "uuid" ) );
//		return outDO.get( "uuid" );
//	}
//	
//	public static void deleteRole( String roleId ) throws ProObjectException {			
//		logger.info( "[OAuth] Role Delete Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_ROLE_DELETE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		request.setWebHeader( ServiceManager.getCurrentRequestContext().getRequest().getWebHeader() );
//		
//		Map<String, String> restMap = new HashMap<String, String>();
//		restMap.put("roles", roleId);
//		request.setRESTMap(restMap);
//
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_ROLE_DELETE, new ContextDataObject() );
//		
//		if( !outDO.get( "result" ).equals( "true" ) ) {
//			PaaSExceptionFunc.throwPOException( PaaSErrorCode.OAUTH.OauthSerivceError, ( String )outDO.get( "error" ) );
//		}
//	}
//	
//	public static boolean verifyPermission( String objectType, String object, String action ) throws ServiceException {		
//		logger.info( "[OAuth] Permission Verify Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_PERMISSION_VERIFY );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		Map < String, String > header = ServiceManager.getCurrentRequestContext().getRequest().getWebHeader();
//		logger.info( "Token : " + header.get( "token" ) );
//		if(header.get( "token" ).equals(Constants.UNBEATABLE_TOKEN)) return true;
//		request.setWebHeader( header );
//
//		ContextDataObject permissionVerifyIn = new ContextDataObject();
//		ContextDataObject permission = new ContextDataObject();
//		permission.set( "object_type", objectType );
//		permission.set( "object", object );
//		permission.set( "action", action );
//		
//		permissionVerifyIn.set( "permission", permission );
//
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_PERMISSION_VERIFY, permissionVerifyIn );
//		
//		logger.info( "Permission : " + outDO.get( "permission" ) );
//		return Boolean.parseBoolean( outDO.get( "permission" ) );
//	}
//	
//	public static void updateRole( String type, String roleId, List < String > userList ) throws ServiceException {		
//		logger.info( "[OAuth] Role Update Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_ROLE_UPDATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		Map < String, String > header = ServiceManager.getCurrentRequestContext().getRequest().getWebHeader();
//		request.setWebHeader( header );
//		
//		Map<String, String> restMap = new HashMap<>();
//		restMap.put( "roles", roleId );
//		request.setRESTMap( restMap );
//
//		ContextDataObject roleUpdateIn = new ContextDataObject();
//		if ( type.equals( "userAdd" ) ) {
//			roleUpdateIn.set( "user_add", userList );
//		} else if ( type.equals( "userDelete" ) ) {
//			roleUpdateIn.set( "user_delete", userList );
//		} else if ( type.equals( "permissionAdd" ) ) {
//			roleUpdateIn.set( "permission_add", userList );
//		} else if ( type.equals( "permissionDelete" ) ) {
//			roleUpdateIn.set( "permission_delete", userList );
//		}
//
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_ROLE_UPDATE, roleUpdateIn );
//	}
//	
//	public static ContextDataObject userPasswordUpdate( String userId, String password  ) throws ServiceException {		
//		logger.info( "[OAuth] Authenticate Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_USER_UPDATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		Map < String, String > header = ServiceManager.getCurrentRequestContext().getRequest().getWebHeader();
//		request.setWebHeader( header );
//		
//		Map<String, String> pathMap = new HashMap<>();
//		pathMap.put("users", userId);
//		request.setRESTMap(pathMap);
//		
//		ContextDataObject updatePW = new ContextDataObject();
//		updatePW.set( "password", password );
//		ContextDataObject updateUser = new ContextDataObject();
//		updateUser.set( "update", updatePW );
//		
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_USER_UPDATE, updateUser );
//		
//		return outDO;
//	}
//	
//	public static ContextDataObject authenticate( String userId, String password  ) throws ServiceException {		
//		logger.info( "[OAuth] Authenticate Create Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_AUTHENTICATE_CREATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		Map < String, String > header = ServiceManager.getCurrentRequestContext().getRequest().getWebHeader();
//		request.setWebHeader( header );
//		
//		ContextDataObject authenticateIn = new ContextDataObject();
//		authenticateIn.set( "user_id", userId );
//		authenticateIn.set( "password", password );
//
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_AUTHENTICATE_CREATE, authenticateIn );
//		
//		return outDO;
//	}
//	
//	public static ContextDataObject authenticateUpdate( String refreshToken  ) throws ServiceException {		
//		logger.info( "[OAuth] Authenticate Update Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_AUTHENTICATE_UPDATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		Map < String, String > header = ServiceManager.getCurrentRequestContext().getRequest().getWebHeader();
//		request.setWebHeader( header );
//		
//		ContextDataObject authenticateIn = new ContextDataObject();
//		authenticateIn.set( "refresh_token", refreshToken );
//
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_AUTHENTICATE_UPDATE, authenticateIn );
//		
//		return outDO;
//	}
//	
//	public static ContextDataObject tokenVerify() throws ServiceException {		
//		logger.info( "[OAuth] Authenticate Update Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_TOKEN_VERIFY_CREATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		Map < String, String > header = ServiceManager.getCurrentRequestContext().getRequest().getWebHeader();
//		request.setWebHeader( header );
//
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_TOKEN_VERIFY_CREATE, new ContextDataObject() );
//		
//		return outDO;
//	}
//	
//	public static ContextDataObject configurationUpdate( ContextDataObject inDO ) throws ServiceException {		
//		logger.info( "[OAuth] Configuration Update Service Start" );
//		RequestContext requestContext = setOAuthURL ( Constants.SERVICE_NAME_OAUTH_CONFIGURATION_UPDATE );
//		
//		ProObjectRequest request = requestContext.getRequest();
//		Map < String, String > header = ServiceManager.getCurrentRequestContext().getRequest().getWebHeader();
//		request.setWebHeader( header );
//
//		ContextDataObject outDO = ( ContextDataObject ) ServiceManager.call( requestContext, Constants.SERVICE_NAME_OAUTH_CONFIGURATION_UPDATE, inDO );
//		
//		return outDO;
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	public static String createPermission( String objectType, String object, String action, String http ) throws Exception{
//		logger.info( "[OAuth] Permission Create Service Start" );
//		
//		// Make Service URL
//		StringBuilder sb = new StringBuilder();
//		sb.append(Constants.HTTP_REQUEST_PREFIX);
//		sb.append(ServerConfig.getValue("OAUTH_IP"));
//		sb.append(":");
//		sb.append("8088");
//		sb.append("/proauth/oauth/permissions");
//		logger.info( "Service URL : " + sb.toString() );
//
//		ContextDataObject permissionCreateIn = new ContextDataObject();
//		permissionCreateIn.set( "object_type", objectType );
//		permissionCreateIn.set( "object", object );
//		permissionCreateIn.set( "action", action );
//		permissionCreateIn.set( "description", object + " " + action + " Permission" );
//
//		HttpClient client = null;
//		ContextDataObject serviceOutDO = null;
//		JsonObject jsonObject = null;
//		try {
//			// Make Http Connection
//			client = HttpClientBuilder.create().build();
//			HttpPost request = new HttpPost( sb.toString() );
//			request.addHeader("Content-Type", "application/json; charset=UTF-8");
//
//			// Set InDO
//			byte[] byteData = new ContextDataObjectMsgJson().marshal( permissionCreateIn );
//			String inputData = new String(byteData);
//			logger.info("Input Data : " + inputData);
//			logger.info("Input Data Length : " + byteData.length);
//			HttpEntity entity = new StringEntity( inputData, ContentType.APPLICATION_JSON );
//			request.setEntity(entity);
//
//			// Send Request
//			HttpResponse response = client.execute( request );
//
//			// Get OutDO
//			StringBuilder textBuilder = new StringBuilder();
//			try (Reader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
//					Charset.forName(StandardCharsets.UTF_8.name())))) {
//				int c = 0;
//				while ((c = reader.read()) != -1) {
//					textBuilder.append((char) c);
//				}
//			}
//
//			jsonObject = (JsonObject) new JsonParser().parse(textBuilder.toString());
//			logger.info("Output Data : " + jsonObject.toString());
//			logger.info("Output Data Length : " + jsonObject.toString().getBytes().length);
//
//			serviceOutDO = (ContextDataObject) new ContextDataObjectMsgJson()
//					.unmarshal(jsonObject.toString().getBytes());
//
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			client.getConnectionManager().shutdown();
//		}
//		
//		logger.info( "Permission ID : " + serviceOutDO.get( "uuid" ) );
//		return serviceOutDO.get( "uuid" );
//	}
//	
//	public static String createRole( String name, List<String> permissionList, String http ) throws Exception {		
//		logger.info( "[OAuth] Role Create Service Start" );
//
//		// Make Service URL
//		StringBuilder sb = new StringBuilder();
//		sb.append(Constants.HTTP_REQUEST_PREFIX);
//		sb.append(ServerConfig.getValue("OAUTH_IP"));
//		sb.append(":");
//		sb.append("8088");
//		sb.append("/proauth/oauth/roles");
//		logger.info( "Service URL : " + sb.toString() );
//
//		ContextDataObject roleCreateIn = new ContextDataObject();
//		roleCreateIn.set( "name", name );
//		roleCreateIn.set( "description", name + " Role" );
//		roleCreateIn.set( "permission_list", permissionList );
//		
//		List<String> adminRole = new ArrayList<>();
//		adminRole.add( Constants.ADMIN_ROLE_ID );
//		roleCreateIn.set( "parent_list", adminRole );
//
//		HttpClient client = null;
//		ContextDataObject serviceOutDO = null;
//		JsonObject jsonObject = null;
//		try {
//			// Make Http Connection
//			client = HttpClientBuilder.create().build();
//			HttpPost request = new HttpPost( sb.toString() );
//			request.addHeader("Content-Type", "application/json; charset=UTF-8");
//
//			// Set InDO
//			byte[] byteData = new ContextDataObjectMsgJson().marshal( roleCreateIn );
//			String inputData = new String(byteData);
//			logger.info("Input Data : " + inputData);
//			logger.info("Input Data Length : " + byteData.length);
//			HttpEntity entity = new StringEntity( inputData, ContentType.APPLICATION_JSON );
//			request.setEntity(entity);
//
//			// Send Request
//			HttpResponse response = client.execute( request );
//
//			// Get OutDO
//			StringBuilder textBuilder = new StringBuilder();
//			try (Reader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
//					Charset.forName(StandardCharsets.UTF_8.name())))) {
//				int c = 0;
//				while ((c = reader.read()) != -1) {
//					textBuilder.append((char) c);
//				}
//			}
//
//			jsonObject = (JsonObject) new JsonParser().parse(textBuilder.toString());
//			logger.info("Output Data : " + jsonObject.toString());
//			logger.info("Output Data Length : " + jsonObject.toString().getBytes().length);
//
//			serviceOutDO = (ContextDataObject) new ContextDataObjectMsgJson()
//					.unmarshal(jsonObject.toString().getBytes());
//
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			client.getConnectionManager().shutdown();
//		}
//		
//		logger.info( "Role ID : " + serviceOutDO.get( "uuid" ) );
//		return serviceOutDO.get( "uuid" );
//	}
//	
//	public static boolean loginSuccess(String userId, String password) throws Exception {		
//		logger.info( "[OAuth] Admin Login Test" );
//
//		// Make Service URL
//		StringBuilder sb = new StringBuilder();
//		sb.append(Constants.HTTP_REQUEST_PREFIX);
//		sb.append(ServerConfig.getValue("OAUTH_IP"));
//		sb.append(":");
//		sb.append("8088");
//		sb.append("/proauth/oauth/authenticate?action");
//		logger.info( "Service URL : " + sb.toString() );
//
//		ContextDataObject login = new ContextDataObject();
//		login.set( "user_id", userId );
//		login.set( "password", password );
//		
//		HttpClient client = null;
//		ContextDataObject serviceOutDO = null;
//		JsonObject jsonObject = null;
//		try {
//			// Make Http Connection
//			client = HttpClientBuilder.create().build();
//			HttpPost request = new HttpPost( sb.toString() );
//			request.addHeader("Content-Type", "application/json; charset=UTF-8");
//
//			// Set InDO
//			byte[] byteData = new ContextDataObjectMsgJson().marshal( login );
//			String inputData = new String(byteData);
//			HttpEntity entity = new StringEntity( inputData, ContentType.APPLICATION_JSON );
//			request.setEntity(entity);
//
//			// Send Request
//			HttpResponse response = client.execute( request );
//
//			// Get OutDO
//			StringBuilder textBuilder = new StringBuilder();
//			try (Reader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
//					Charset.forName(StandardCharsets.UTF_8.name())))) {
//				int c = 0;
//				while ((c = reader.read()) != -1) {
//					textBuilder.append((char) c);
//				}
//			}
//
//			jsonObject = (JsonObject) new JsonParser().parse(textBuilder.toString());
//
//			serviceOutDO = (ContextDataObject) new ContextDataObjectMsgJson()
//					.unmarshal(jsonObject.toString().getBytes());
//
//		} catch (Exception e) {
//			logger.info("Login Service Error");
//			return false;
//		} finally {
//			client.getConnectionManager().shutdown();
//		}
//		
//		if ( serviceOutDO.get( "result" ).equals( "true" ) ) {
//			logger.info("Login Success!!");
//			return true;
//		}
//		
//		return false;
//	}
//	
//	public static void updateAdminPW() throws Exception {		
//		logger.info( "[OAuth] Update Admin Password" );
//
//		// Make Service URL
//		StringBuilder sb = new StringBuilder();
//		sb.append(Constants.HTTP_REQUEST_PREFIX);
//		sb.append(ServerConfig.getValue("OAUTH_IP"));
//		sb.append(":");
//		sb.append("8088");
//		sb.append("/proauth/oauth/users/" + Constants.ADMIN_USER_ID);
//
//		ContextDataObject updatePW = new ContextDataObject();
//		updatePW.set( "password", "c7ad44cbad762a5da0a452f9e854fdc1e0e7a52a38015f23f3eab1d80b931dd472634dfac71cd34ebc35d16ab7fb8a90c81f975113d6c7538dc69dd8de9077ec" );
//		ContextDataObject updateUser = new ContextDataObject();
//		updateUser.set( "update", updatePW );
//		
//		HttpClient client = null;
//		ContextDataObject serviceOutDO = null;
//		JsonObject jsonObject = null;
//		try {
//			// Make Http Connection
//			client = HttpClientBuilder.create().build();
//			HttpPut request = new HttpPut( sb.toString() );
//			request.addHeader("Content-Type", "application/json; charset=UTF-8");
//
//			// Set InDO
//			byte[] byteData = new ContextDataObjectMsgJson().marshal( updateUser );
//			String inputData = new String(byteData);
//			HttpEntity entity = new StringEntity( inputData, ContentType.APPLICATION_JSON );
//			request.setEntity(entity);
//
//			// Send Request
//			HttpResponse response = client.execute( request );
//
//			// Get OutDO
//			StringBuilder textBuilder = new StringBuilder();
//			try (Reader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
//					Charset.forName(StandardCharsets.UTF_8.name())))) {
//				int c = 0;
//				while ((c = reader.read()) != -1) {
//					textBuilder.append((char) c);
//				}
//			}
//
//			jsonObject = (JsonObject) new JsonParser().parse(textBuilder.toString());
//
//			serviceOutDO = (ContextDataObject) new ContextDataObjectMsgJson()
//					.unmarshal(jsonObject.toString().getBytes());
//
//		} catch (Exception e) {
//			logger.info("PW Update Error");
//		} finally {
//			client.getConnectionManager().shutdown();
//		}
//		
//		if ( serviceOutDO.get( "result" ).equals( "false" ) ) {
//			logger.info("PW Update Fail");
//		}
	
}
