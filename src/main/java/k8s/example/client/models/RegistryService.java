package k8s.example.client.models;

public class RegistryService {
	public class Ingress {
		private int port = 443;
		private String domainName = null;
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public String getDomainName() {
			return domainName;
		}
		public void setDomainName(String domainName) {
			this.domainName = domainName;
		}
	}
	
	public class LoadBalancer{
		private int port = 443;

		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
	}
	
	public static final String SVC_TYPE_CLUSTER_IP = "ClusterIP";
	public static final String SVC_TYPE_NODE_PORT = "NodePort";
	public static final String SVC_TYPE_LOAD_BALANCER = "LoadBalancer";
	public static final String SVC_TYPE_INGRESS = "Ingress";
	public static final int REGISTRY_TARGET_PORT = 443;
	public static final String REGISTRY_PORT_NAME = "tls";
	public static final String REGISTRY_PORT_PROTOCOL = "TCP";
	
	private Ingress ingress = null;
	private LoadBalancer loadBalancer = null;
	
	public Ingress getIngress() {
		return ingress;
	}
	public void setIngress(Ingress ingress) {
		this.ingress = ingress;
	}
	public LoadBalancer getLoadBalancer() {
		return loadBalancer;
	}
	public void setLoadBalancer(LoadBalancer loadBalancer) {
		this.loadBalancer = loadBalancer;
	}
	
	
}
