/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package k8s.example.client.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * {@linkplain OkHttpClient#interceptors() application interceptor} or as a
 * {@linkplain OkHttpClient#networkInterceptors() network interceptor}.
 * <p>
 * The format of the logs created by this class should not be considered stable and may change
 * slightly between releases. If you need a stable logging format, use your own interceptor.
 */
public class HttpLoggingInterceptor implements Interceptor {
  private static final Charset UTF8 = Charset.forName("UTF-8");
  private static Logger logger = LoggerFactory.getLogger("OkhttpInterceptor");

  @Override 
  public Response intercept(Chain chain) throws IOException {
	  
    Request request = chain.request();

    boolean logBody = true;
    boolean logHeaders = true;

    RequestBody requestBody = request.body();
    boolean hasRequestBody = requestBody != null;

    Connection connection = chain.connection();
    Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
    String requestStartMessage =
        "--> " + request.method() + ' ' + requestPath(request.url()) + ' ' + protocol(protocol);
    if (!logHeaders && hasRequestBody) {
      requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
    }
    logger.info(requestStartMessage);

    if (logHeaders) {
      Headers headers = request.headers();
      for (int i = 0, count = headers.size(); i < count; i++) {
        logger.info(headers.name(i) + ": " + headers.value(i));
      }

      String endMessage = "--> END " + request.method();
      if (logBody && hasRequestBody) {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);

        Charset charset = UTF8;
        MediaType contentType = requestBody.contentType();
        if (contentType != null) {
          contentType.charset(UTF8);
        }

        logger.info("");
        logger.info(buffer.readString(charset));

        endMessage += " (" + requestBody.contentLength() + "-byte body)";
      }
      logger.info(endMessage);
    }

    long startNs = System.nanoTime();
    Response response = chain.proceed(request);
    long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

    ResponseBody responseBody = response.body();
    logger.info("<-- " + protocol(response.protocol()) + ' ' + response.code() + ' '
        + response.message() + " (" + tookMs + "ms"
        + (!logHeaders ? ", " + responseBody.contentLength() + "-byte body" : "") + ')');

    if (logHeaders) {
      Headers headers = response.headers();
      for (int i = 0, count = headers.size(); i < count; i++) {
        logger.info(headers.name(i) + ": " + headers.value(i));
      }

      String endMessage = "<-- END HTTP";
      if (logBody) {
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
          charset = contentType.charset(UTF8);
        }

        if (responseBody.contentLength() != 0) {
          logger.info("");
          logger.info(buffer.clone().readString(charset));
        }

        endMessage += " (" + buffer.size() + "-byte body)";
      }
      logger.info(endMessage);
    }

    return response;
  }

  private static String protocol(Protocol protocol) {
    return protocol == Protocol.HTTP_1_0 ? "HTTP/1.0" : "HTTP/1.1";
  }

  private static String requestPath(HttpUrl url) {
    String path = url.encodedPath();
    String query = url.encodedQuery();
    return query != null ? (path + '?' + query) : path;
  }
}