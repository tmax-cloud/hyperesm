package k8s.example.client.models;

import java.util.List;

public class PlanMetadata {
	
	private List<String> bullets = null;
	private Cost costs = null;
	private String displayName = null;
	
	public List<String> getBullets() {
		return bullets;
	}
	public void setBullets(List<String> bullets) {
		this.bullets = bullets;
	}
	public Cost getCosts() {
		return costs;
	}
	public void setCosts(Cost costs) {
		this.costs = costs;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	@Override
	public String toString() {
		return "PlanMetadata [bullets=" + bullets + ", costs=" + costs + ", displayName=" + displayName + "]";
	}
}
