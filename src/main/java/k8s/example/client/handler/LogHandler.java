package k8s.example.client.handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hdds.conf.OzoneConfiguration;
import org.apache.hadoop.ozone.client.ObjectStore;
import org.apache.hadoop.ozone.client.OzoneBucket;
import org.apache.hadoop.ozone.client.OzoneClient;
import org.apache.hadoop.ozone.client.OzoneClientFactory;
import org.apache.hadoop.ozone.client.OzoneVolume;
import org.apache.hadoop.ozone.client.VolumeArgs;
import org.apache.hadoop.ozone.client.io.OzoneOutputStream;
import org.slf4j.Logger;

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
import k8s.example.client.models.BindingInDO;
import k8s.example.client.models.BindingOutDO;

public class LogHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /v1/submodules/:mod_name");
		
		BindingOutDO response = null;
		Map<String, String> body = new HashMap<String, String>();
		
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        logger.info("***** BODY TEST");
		for(String key : body.keySet()) {
			String value = body.get(key);
			logger.info("Body Key: " + key);
			logger.info("Body value: " + value);
		}
		
		String moduleName = urlParams.get("mod_name");
		logger.info("Instance ID: " + moduleName);
		
		String outDO = null;
		IStatus status = null;
		
		try {
			OzoneClient ozClient = OzoneClientFactory.getRpcClient("172.23.4.114", 9862, new OzoneConfiguration());
			ObjectStore objectStore = ozClient.getObjectStore();
//			objectStore.createVolume("tmax", VolumeArgs.newBuilder().setOwner("tmax").setAdmin("tmax").build());
			OzoneVolume vol = objectStore.getVolume("tmax");
//			vol.createBucket("tmax");
			OzoneBucket buc = vol.getBucket("tmax");
			OzoneOutputStream logStream = buc.createKey(moduleName + "-" + String.valueOf(System.currentTimeMillis()/1000), 104857600);
			logStream.write(body.get("postData").getBytes());
			logStream.close();
			status = Status.OK;
		} catch (Exception e) {
			logger.info("    Failed to put log  \"");
			logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			status = Status.BAD_REQUEST;
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		outDO = gson.toJson(response).toString();
		logger.info("Response : " + outDO);
		
//		logger.info();
		return NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO);
    }
}
