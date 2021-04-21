package k8s.example.client;

import java.io.IOException;

import org.slf4j.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import io.sundr.shaded.org.apache.velocity.app.event.EventHandler;
import k8s.example.client.handler.CatalogHandler;
import k8s.example.client.handler.ServiceBindingHandler;
import k8s.example.client.handler.ServiceInstanceHandler;

public class HttpHandler extends RouterNanoHTTPD {
    private Logger logger = Main.logger;
    
    public HttpHandler() throws IOException {
        super(28688);
        addMappings();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        logger.info("Nano HTTPD is running!!");
    }
  
    @Override
    public void addMappings() {
    	addRoute("/v2/catalog", CatalogHandler.class);
    	addRoute("/v2/service_instances/:instance_id/service_bindings/:binding_id", ServiceBindingHandler.class);
    	addRoute("/v2/service_instances/:instance_id", ServiceInstanceHandler.class);
    	addRoute("/v1/submodules/:mod_name", EventHandler.class);
    }
}