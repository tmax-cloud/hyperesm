package k8s.example.client.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class ChunkedInterceptor implements Interceptor {

	  private static final Charset UTF8 = Charset.forName("UTF-8");
	  private static Logger logger = LoggerFactory.getLogger("OkhttpInterceptor");

	  @Override
		public Response intercept(Chain chain) throws IOException {
		    Request request = chain.request();
		    Response response = chain.proceed(request);
		    ResponseBody responseBody = response.body();
	        BufferedSource source = responseBody.source();
	        source.request(Long.MAX_VALUE); // Buffer the entire body.
	        Buffer buffer = source.buffer();
	        
	        logger.info( "=================================================================" );
	        logger.info( "Request URL : " + request.url() );
	           
	        while (!source.exhausted()) {
	            long readBytes = source.read(buffer, Long.MAX_VALUE); // We read the whole buffer
	            String data = buffer.readString (UTF8);
	            logger.info( "Read bytes : " + readBytes );
	            logger.info( "Content : " + data );
	        }
	        
			return response;
	  }
}
	
