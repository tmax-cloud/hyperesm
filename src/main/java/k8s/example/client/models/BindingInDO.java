package k8s.example.client.models;

import java.util.Map;

public class BindingInDO {

	private Context context = null;
	private String service_id = null;
	private String plan_id = null;
	private String app_guid = null;
	private BindResource bind_resource = null;
	private Map<String, String> parameters = null;

	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public String getService_id() {
		return service_id;
	}
	public void setService_id(String service_id) {
		this.service_id = service_id;
	}
	public String getPlan_id() {
		return plan_id;
	}
	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}
	public String getApp_guid() {
		return app_guid;
	}
	public void setApp_guid(String app_guid) {
		this.app_guid = app_guid;
	}
	public BindResource getBind_resource() {
		return bind_resource;
	}
	public void setBind_resource(BindResource bind_resource) {
		this.bind_resource = bind_resource;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	@Override
	public String toString() {
		return "BindingInDO [context=" + context + ", service_id=" + service_id + ", plan_id=" + plan_id + ", app_guid="
				+ app_guid + ", bind_resource=" + bind_resource + ", parameters=" + parameters + "]";
	}
}
