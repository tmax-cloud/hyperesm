package k8s.example.client.models;

public class Schemas {

	private ServiceInstanceSchema service_instance = null;
	private ServiceBindingSchema service_binding = null;
	
	public ServiceInstanceSchema getService_instance() {
		return service_instance;
	}
	public void setService_instance(ServiceInstanceSchema service_instance) {
		this.service_instance = service_instance;
	}
	public ServiceBindingSchema getService_binding() {
		return service_binding;
	}
	public void setService_binding(ServiceBindingSchema service_binding) {
		this.service_binding = service_binding;
	}
	
	@Override
	public String toString() {
		return "Schemas [service_instance=" + service_instance + ", service_binding=" + service_binding + "]";
	}
}
