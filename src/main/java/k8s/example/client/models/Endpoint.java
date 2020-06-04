package k8s.example.client.models;

import java.util.List;

public class Endpoint {

	private String host = null;
	private List<String> ports = null;
	private String protocol = null;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public List<String> getPorts() {
		return ports;
	}
	public void setPorts(List<String> ports) {
		this.ports = ports;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	@Override
	public String toString() {
		return "Endpoint [host=" + host + ", ports=" + ports + ", protocol=" + protocol + "]";
	}
}
