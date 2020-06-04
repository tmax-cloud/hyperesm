package k8s.example.client.handler;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.models.BrokerResponse;
import k8s.example.client.models.ProvisionInDO;

public class ServiceInstanceHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response put(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** PUT /v2/service_instances/:instance_id");
		
		Object response = null;
		Map<String, String> body = new HashMap<String, String>();
		
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String instanceId = urlParams.get("instance_id");
		logger.info("Instance ID: " + instanceId);
		
        ProvisionInDO inDO = null;
        String outDO = null;
		IStatus status = null;
		
		try {
			String bodyStr = readFile(body.get("content"), Integer.valueOf(session.getHeaders().get("content-length")));
			logger.info("Body: " + bodyStr);
			
			inDO = new ObjectMapper().readValue(bodyStr, ProvisionInDO.class);
			logger.info("Service ID: " + inDO.getService_id());
			logger.info("Service Plan ID: " + inDO.getPlan_id());
			logger.info("Context: " + inDO.getContext().toString());
			
			/**
			 * 			List<V1OwnerReference> ownerRefs = new ArrayList<>();
						V1OwnerReference ownerRef = new V1OwnerReference();
						
						ownerRef.setApiVersion(registry.getApiVersion());
						ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
						ownerRef.setController(Boolean.TRUE);
						ownerRef.setKind(registry.getKind());
						ownerRef.setName(registry.getMetadata().getName());
						ownerRef.setUid(registry.getMetadata().getUid());
						ownerRefs.add(ownerRef);
						
						lbMeta.setOwnerReferences(ownerRefs);
			 */
			String uid = K8sApiCaller.getUid( inDO.getContext().getNamespace(), inDO.getContext().getInstance_name() );
			
			response = K8sApiCaller.createTemplateInstance(instanceId, inDO, inDO.getContext().getInstance_name(), uid);
			status = Status.OK;
		} catch (Exception e) {
			logger.info( "  Failed to provision instance of service class \"" + inDO.getService_id() + "\"");
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
		logger.info("***** DELETE /v2/service_instances/:instance_id");
		
		String serviceClassName = session.getParameters().get("service_id").get(0);
		String instanceId = urlParams.get("instance_id");
		for ( String key : session.getParameters().keySet() ) {
			logger.info("Delete Service input key : " + key);
			for ( String value : session.getParameters().get(key) ) {
				logger.info("Delete Service input value : " + value);
			}
		}
		logger.info("Service Class Name: " + serviceClassName);
		logger.info("Instance ID: " + instanceId);
		
		Object response = null;
		String outDO = null;
		IStatus status = null;
		
		try {
			//response = K8sApiCaller.deleteTemplateInstance(instanceId);
			status = Status.OK;
		} catch (Exception e) {
			logger.info( "  Failed to delete instance of service class \"" + serviceClassName + "\"");
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.BAD_REQUEST;
		}
		
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
