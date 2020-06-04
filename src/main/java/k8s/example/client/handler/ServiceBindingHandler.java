package k8s.example.client.handler;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
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
import k8s.example.client.Main;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.models.BindingInDO;
import k8s.example.client.models.BindingOutDO;
import k8s.example.client.models.BrokerResponse;
import k8s.example.client.models.ProvisionInDO;

public class ServiceBindingHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response put(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** PUT /v2/service_instances/:instance_id/service_bindings/:binding_id");
		
		BindingOutDO response = null;
		Map<String, String> body = new HashMap<String, String>();
		
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}

		String instanceId = urlParams.get("instance_id");
		String bindingId = urlParams.get("binding_id");
		logger.info("Instance ID: " + instanceId);
		logger.info("Binding ID: " + bindingId);
		
        BindingInDO inDO = null;
        String outDO = null;
		IStatus status = null;
		
		try {
			String bodyStr = readFile(body.get("content"), Integer.valueOf(session.getHeaders().get("content-length")));
			logger.info("Body: " + bodyStr);
			
			inDO = new ObjectMapper().readValue(bodyStr, BindingInDO.class);
			logger.info("Service ID: " + inDO.getService_id());
			logger.info("Service Plan ID: " + inDO.getPlan_id());
			logger.info("Context: " + inDO.getContext().toString());
			if(!inDO.getBind_resource().getApp_guid().isEmpty()) {
				logger.info("Application GUID: " + inDO.getBind_resource().getApp_guid());
			}
//			if(!inDO.getBind_resource().getRoute().isEmpty()) {
//				logger.info("Application URL: " + inDO.getBind_resource().getRoute());
//			}
			
			response = K8sApiCaller.insertBindingSecret(instanceId, bindingId, inDO);
			status = Status.OK;
		} catch (Exception e) {
			logger.info( "  Failed to bind instance of service class \"" + inDO.getService_id() + "\"");
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.BAD_REQUEST;
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		outDO = gson.toJson(response).toString();
		logger.info("Response : " + outDO);
		
//		logger.info();
		return NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO);
    }
	
	@Override
    public Response delete(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** DELETE /v2/service_instances/:instance_id/service_bindings/:binding_id");
		
		String serviceClassName = session.getParameters().get("service_id").get(0);
		String instanceId = urlParams.get("instance_id");
		String bindingId = urlParams.get("binding_id");
		logger.info("Instance ID: " + instanceId);
		logger.info("Binding ID: " + bindingId);
		
		BrokerResponse response = new BrokerResponse();
		String outDO = null;
		IStatus status = null;
		
		try {
			response.setOperation("");
			status = Status.OK;
		} catch (Exception e) {
			logger.info( "  Failed to unbind instance of service class \"" + serviceClassName + "\"");
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.BAD_REQUEST;
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		outDO = gson.toJson(response).toString();
		logger.info("Response : " + outDO);
		
//		logger.info();
		return NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO);
    }
	
	private String readFile(String path, Integer length) {
		Charset charset = Charset.defaultCharset();
		String bodyStr = "";
		int byteCount;
		try {
			ByteBuffer buf = ByteBuffer.allocate(Integer.valueOf(length));
			FileInputStream fis = new FileInputStream(path);
			FileChannel dest = fis.getChannel();
			
			while(true) {
				byteCount = dest.read(buf);
				if(byteCount == -1) {
					break;
				} else {
					buf.flip();
					bodyStr += charset.decode(buf).toString();
					buf.clear();
				}
			}
			dest.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return bodyStr;
	}

}
