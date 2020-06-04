package k8s.example.client.interceptor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class LogInterceptor implements Interceptor {
	public static Logger logger = LoggerFactory.getLogger("OkhttpInterceptor");

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		logger.info( "================== REQUEST ==================" );
		logger.info( "Connection : " + chain.connection() );
		logger.info( "Request Method : " + request.method() );
		logger.info( "Request Url : " + request.headers() );
		logger.info( "=============================================" );

		logger.info( "================== RESPONSE =================" );
		Response response = chain.proceed(request);
		logger.info( "Connection : " + chain.connection() );
		logger.info( "Request Url : " + response.request().url() );
		logger.info( "Response header : " + response.headers() );
		logger.info( "Response Content-Length : " + response.body().contentLength() );
		logger.info( "Response Body : " + response.body().string() );
		logger.info( "=============================================" );
		
		return response;
	}

}
