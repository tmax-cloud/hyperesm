package k8s.example.client.models;

public class ServiceBindingSchema {

	private InputParametersSchema create = null;

	public InputParametersSchema getCreate() {
		return create;
	}

	public void setCreate(InputParametersSchema create) {
		this.create = create;
	}

	@Override
	public String toString() {
		return "ServiceBindingSchema [create=" + create + "]";
	}
}
