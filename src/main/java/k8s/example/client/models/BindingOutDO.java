package k8s.example.client.models;

import java.util.List;

public class BindingOutDO {

	private BindingMetadata metadata = null;
	private Object credentials = null;
	private String syslog_drain_url = null;
	private String route_service_url = null;
	private List<VolumeMount> volume_mounts = null;
	private List<Endpoint> endpoints = null;
	
	public List<Endpoint> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(List<Endpoint> endpoints) {
		this.endpoints = endpoints;
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
	@Override
	public String toString() {
		return "BindingOutDO [metadata=" + metadata + ", credentials=" + credentials + ", syslog_drain_url="
				+ syslog_drain_url + ", route_service_url=" + route_service_url + ", volume_mounts=" + volume_mounts
				+ ", endpoints=" + endpoints + "]";
	}
}
