package k8s.example.client.handler;

import java.util.Map;

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
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.models.Services;

public class CatalogHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response get(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		
		logger.info("***** GET /v2/catalog");
		
		Services catalog = null;
		String outDO = null;
		IStatus status = null;
		
		try {
			catalog = K8sApiCaller.getCatalog();
			status = Status.OK;
		} catch (ApiException e) {
			logger.info( "  Get Catalog fail" );
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.NOT_FOUND;
		} catch (Exception e) {
			logger.info( "  Get Catalog fail" );
			logger.info( "Exception message: " + e.getMessage() );
			logger.info( "Exception message: " + e.getStackTrace().toString() );
			logger.info( "Exception message: " + e.toString() );


			
			e.printStackTrace();
			status = Status.NOT_FOUND;
			throw e;
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		outDO = gson.toJson(catalog).toString();
		logger.info("Catalog : " + outDO);
		
		//logger.info();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
	}
}