package k8s.example.client.models;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

public class Template {

	private String apiVersion = null;
	private String kind = null;
	private Map<String, String> labels = null;
	private String shortDescription = null;
	private String longDescription = null;
	private String provider = null;
	private String imageUrl = null;
	private List<String> objectKinds = null;
	private Metadata metadata = null;
	private JSONObject objects = null;
	private List<ServicePlan> plans = null;
	private List<TemplateParameter> parameters = null;
	private String operatorStartTime = null;
	
	public String getOperatorStartTime() {
		return operatorStartTime;
	}
	public void setOperatorStartTime(String operatorStartTime) {
		this.operatorStartTime = operatorStartTime;
	}
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
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public List<String> getObjectKinds() {
		return objectKinds;
	}
	public void setObjectKinds(List<String> objectKinds) {
		this.objectKinds = objectKinds;
	}
	public Metadata getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	public JSONObject getObjects() {
		return objects;
	}
	public void setObjects(JSONObject objects) {
		this.objects = objects;
	}
	public List<ServicePlan> getPlans() {
		return plans;
	}
	public void setPlans(List<ServicePlan> plans) {
		this.plans = plans;
	}
	public List<TemplateParameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<TemplateParameter> parameters) {
		this.parameters = parameters;
	}
	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	public String getLongDescription() {
		return longDescription;
	}
	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	@Override
	public String toString() {
		return "Template [apiVersion=" + apiVersion + ", kind=" + kind + ", labels=" + labels + ", shortDescription="
				+ shortDescription + ", longDescription=" + longDescription + ", provider=" + provider + ", imageUrl="
				+ imageUrl + ", objectKinds=" + objectKinds + ", metadata=" + metadata + ", objects=" + objects
				+ ", plans=" + plans + ", parameters=" + parameters + "]";
	}
}
