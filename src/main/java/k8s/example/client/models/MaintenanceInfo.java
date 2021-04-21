package k8s.example.client.models;

public class MaintenanceInfo {

	private String version = null;
	private String description = null;
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return "MaintenanceInfo [version=" + version + ", description=" + description + "]";
	}
}
