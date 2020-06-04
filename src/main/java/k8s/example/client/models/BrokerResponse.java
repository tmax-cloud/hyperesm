package k8s.example.client.models;

import java.util.List;

public class BrokerResponse {

	private String state = null;
	private String description = null;
	private Boolean instance_usable = null;
	private Boolean update_repeatable = null;
	private String dashboard_url = null;
	private String operation = null;
	private String service_id = null;
	private String plan_id = null;
	private Object parameters = null;
	private BindingMetadata metadata = null;
	private Object credentials = null;
	private String syslog_drain_url = null;
	private String route_service_url = null;
	private List<VolumeMount> volume_mounts = null;
	private List<Endpoint> endpoints = null;
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getInstance_usable() {
		return instance_usable;
	}
	public void setInstance_usable(Boolean instance_usable) {
		this.instance_usable = instance_usable;
	}
	public Boolean getUpdate_repeatable() {
		return update_repeatable;
	}
	public void setUpdate_repeatable(Boolean update_repeatable) {
		this.update_repeatable = update_repeatable;
	}
	public String getDashboard_url() {
		return dashboard_url;
	}
	public void setDashboard_url(String dashboard_url) {
		this.dashboard_url = dashboard_url;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
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
	public Object getParameters() {
		return parameters;
	}
	public void setParameters(Object parameters) {
		this.parameters = parameters;
	}
	public BindingMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(BindingMetadata metadata) {
		this.metadata = metadata;
	}
	public Object getCredentials() {
		return credentials;
	}
	public void setCredentials(Object credentials) {
		this.credentials = credentials;
	}
	public String getSyslog_drain_url() {
		return syslog_drain_url;
	}
	public void setSyslog_drain_url(String syslog_drain_url) {
		this.syslog_drain_url = syslog_drain_url;
	}
	public String getRoute_service_url() {
		return route_service_url;
	}
	public void setRoute_service_url(String route_service_url) {
		this.route_service_url = route_service_url;
	}
	public List<VolumeMount> getVolume_mounts() {
		return volume_mounts;
	}
	public void setVolume_mounts(List<VolumeMount> volume_mounts) {
		this.volume_mounts = volume_mounts;
	}
	public List<Endpoint> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(List<Endpoint> endpoints) {
		this.endpoints = endpoints;
	}
	@Override
	public String toString() {
		return "BrokerResponse [state=" + state + ", description=" + description + ", instance_usable="
				+ instance_usable + ", update_repeatable=" + update_repeatable + ", dashboard_url=" + dashboard_url
				+ ", operation=" + operation + ", service_id=" + service_id + ", plan_id=" + plan_id + ", parameters="
				+ parameters + ", metadata=" + metadata + ", credentials=" + credentials + ", syslog_drain_url="
				+ syslog_drain_url + ", route_service_url=" + route_service_url + ", volume_mounts=" + volume_mounts
				+ ", endpoints=" + endpoints + "]";
	}
}
