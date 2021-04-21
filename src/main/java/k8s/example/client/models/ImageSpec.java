package k8s.example.client.models;

import java.util.List;

public class ImageSpec {
	private String name = null;
	private List<String> versions = null;
	private String registry = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getVersions() {
		return versions;
	}

	public void setVersions(List<String> versions) {
		this.versions = versions;
	}

	public String getRegistry() {
		return registry;
	}

	public void setRegistry(String registry) {
		this.registry = registry;
	}

}
