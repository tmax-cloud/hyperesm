package k8s.example.client.models;

import java.util.List;

public class Services {

	private List<ServiceOffering> services = null;

	public List<ServiceOffering> getServices() {
		return services;
	}

	public void setServices(List<ServiceOffering> services) {
		this.services = services;
	}

	@Override
	public String toString() {
		return "Services [services=" + services + "]";
	}
}
