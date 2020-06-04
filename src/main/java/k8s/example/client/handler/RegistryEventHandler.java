package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import k8s.example.client.DataObject.RegistryEvent;
import k8s.example.client.DataObject.RegistryEventDO;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.Main;
import k8s.example.client.Util;

public class RegistryEventHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /registry/event");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
   
        RegistryEventDO regEvent = null;
		String outDO = null;
		IStatus status = null;
		
		try {
			// Read inDO
			regEvent = new ObjectMapper().readValue(body.get( "postData" ), RegistryEventDO.class);
			
			logger.info("====== Registry Event ======");
			
			logger.info("  Registry Event Count: " + regEvent.getEvents().size());
			for( RegistryEvent event : regEvent.getEvents()) {
				
				if (event.getAction().equals("push")) {
					logger.info("    Registry Action: " + event.getAction());
					logger.info("    Registry Target Repository: " + event.getTarget().getRepository());
					logger.info("    Registry Target Url: " + event.getTarget().getUrl());
					logger.info("    Registry Request Host: " + event.getRequest().getHost());
					logger.info("    Registry Actor: " + event.getActor());
					logger.info("    Registry Source Addr: " + event.getSource().getAddr());
					try {
						K8sApiCaller.createImage(event);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				else if (event.getAction().equals("delete")) {
					logger.info("    Registry Action: " + event.getAction());
					logger.info("    Registry Target Repository: " + event.getTarget().getRepository());
					logger.info("    Registry Target Url: " + event.getTarget().getUrl());
					logger.info("    Registry Request Host: " + event.getRequest().getHost());
					logger.info("    Registry Actor: " + event.getActor());
					logger.info("    Registry Source Addr: " + event.getSource().getAddr());
					
					try {
						K8sApiCaller.deleteImage(event);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				else {
					logger.info("    Registry Action: " + event.getAction());
					logger.info("    Registry Target Repository: " + event.getTarget().getRepository());
					logger.info("    Registry Target Url: " + event.getTarget().getUrl());
				}
			}

		}catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );
		}
		status = Status.OK;
		outDO = "event_get_success";
			
 		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /login");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}

/*
{
   "events": [
      {
         "id": "445bf67f-89b2-4a1e-bad0-a206be1121d1",
         "timestamp": "2020-04-07T11:32:14.731044205Z",
         "action": "push",
         "target": {
            "mediaType": "application/vnd.docker.distribution.manifest.v2+json",
            "size": 2421,
            "digest": "sha256:43cb618d4ca0f2720bb8fe9fad098261e47f7b9d248b1d66063efd5e2db67932",
            "length": 2421,
            "repository": "tomcat",
            "url": "https://192.168.6.218:443/v2/tomcat/manifests/sha256:43cb618d4ca0f2720bb8fe9fad098261e47f7b9d248b1d66063efd5e2db67932",
            "tag": "8.5"
         },
         "request": {
            "id": "fb9257ae-69a6-4b5c-b19c-c2ddc2cd2d54",
            "addr": "10.244.48.64:9931",
            "host": "192.168.6.218:443",
            "method": "PUT",
            "useragent": "docker/19.03.8 go/go1.12.17 git-commit/afacb8b7f0 kernel/4.15.0-55-generic os/linux arch/amd64 UpstreamClient(Docker-Client/19.03.8 \\(linux\\))"
         },
         "actor": {
            "name": "tmax"
         },
         "source": {
            "addr": "hpcd-registry-t2-registry-46nfk:443",
            "instanceID": "cbba8965-a19a-4732-8bdc-18c7541c8e2c"
         }
      }
   ]
}
*/