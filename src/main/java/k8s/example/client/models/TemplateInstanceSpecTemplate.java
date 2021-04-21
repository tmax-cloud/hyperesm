package k8s.example.client.models;

import java.util.List;
import java.util.Map;

public class TemplateInstanceSpecTemplate {

	private String apiVersion = null;
	private String kind = null;
	private Map<String, String> labels = null;
	private String message = null;
	private Metadata metadata = null;
	private List<Object> objects = null;
	private List<TemplateParameter> parameters = null;
	
	public String getApiVersion() {
		return apiVersion;
	}
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public Map<String, String> getLabels() {
		return labels;
	}
	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	public List<Object> getObjects() {
		return objects;
	}
	public void setObjects(List<Object> objects) {
		this.objects = objects;
	}
	public List<TemplateParameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<TemplateParameter> parameters) {
		this.parameters = parameters;
	}
	@Override
	public String toString() {
		return "TemplateInstanceSpecTemplate [apiVersion=" + apiVersion + ", kind=" + kind + ", labels=" + labels
				+ ", message=" + message + ", metadata=" + metadata + ", objects=" + objects + ", parameters="
				+ parameters + "]";
	}
}
