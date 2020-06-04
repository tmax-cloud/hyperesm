package k8s.example.client.models;

public class BindResource {

	private String app_guid = null;
	private String route = null;
	
	public String getApp_guid() {
		return app_guid;
	}
	public void setApp_guid(String app_guid) {
		this.app_guid = app_guid;
	}
	public String getRoute() {
		return route;
	}
	public void setRoute(String route) {
		this.route = route;
	}
	@Override
	public String toString() {
		return "BindResource [app_guid=" + app_guid + ", route=" + route + "]";
	}
}
