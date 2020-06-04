package k8s.example.client.models;

public class TemplateInstanceSpecSecret {

	private String name = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "TemplateInstanceSpecSecret [name=" + name + "]";
	}
}
