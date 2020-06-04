package k8s.example.client.models;

public class ServiceInstanceSchema {

	private InputParametersSchema create = null;
	private InputParametersSchema update = null;
	
	public InputParametersSchema getCreate() {
		return create;
	}
	public void setCreate(InputParametersSchema create) {
		this.create = create;
	}
	public InputParametersSchema getUpdate() {
		return update;
	}
	public void setUpdate(InputParametersSchema update) {
		this.update = update;
	}
	@Override
	public String toString() {
		return "ServiceInstanceSchema [create=" + create + ", update=" + update + "]";
	}
}
