package k8s.example.client.models;

import java.util.List;

public class TemplateInstanceStatus {

	private List<TemplateInstanceStatusCondition> conditions = null;

	public List<TemplateInstanceStatusCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<TemplateInstanceStatusCondition> conditions) {
		this.conditions = conditions;
	}
	@Override
	public String toString() {
		return "TemplateInstanceStatus [conditions=" + conditions + "]";
	}
}
