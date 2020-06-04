package k8s.example.client.models;

import java.util.Map;

public class GetPlanSpecDO {

	private Boolean bindable = null;
	private String description = null;
	private String externalID = null;
	private Object externalMetadata = null;
	private String externalName = null;
	private Boolean free = null;
	private Map<String, String> instanceCreateParameterSchema = null;
	private String clusterServiceBrokerName = null;
	private Object clusterServiceClassRef = null;
	
	public Boolean getBindable() {
		return bindable;
	}
	public void setBindable(Boolean bindable) {
		this.bindable = bindable;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getExternalID() {
		return externalID;
	}
	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}
	public Object getExternalMetadata() {
		return externalMetadata;
	}
	public void setExternalMetadata(Object externalMetadata) {
		this.externalMetadata = externalMetadata;
	}
	public String getExternalName() {
		return externalName;
	}
	public void setExternalName(String externalName) {
		this.externalName = externalName;
	}
	public Boolean getFree() {
		return free;
	}
	public void setFree(Boolean free) {
		this.free = free;
	}
	public Map<String, String> getInstanceCreateParameterSchema() {
		return instanceCreateParameterSchema;
	}
	public void setInstanceCreateParameterSchema(Map<String, String> instanceCreateParameterSchema) {
		this.instanceCreateParameterSchema = instanceCreateParameterSchema;
	}
	public String getClusterServiceBrokerName() {
		return clusterServiceBrokerName;
	}
	public void setClusterServiceBrokerName(String clusterServiceBrokerName) {
		this.clusterServiceBrokerName = clusterServiceBrokerName;
	}
	public Object getClusterServiceClassRef() {
		return clusterServiceClassRef;
	}
	public void setClusterServiceClassRef(Object clusterServiceClassRef) {
		this.clusterServiceClassRef = clusterServiceClassRef;
	}
	@Override
	public String toString() {
		return "GetPlanSpecDO [bindable=" + bindable + ", description=" + description + ", externalID=" + externalID
				+ ", externalMetadata=" + externalMetadata + ", externalName=" + externalName + ", free=" + free
				+ ", instanceCreateParameterSchema=" + instanceCreateParameterSchema + ", clusterServiceBrokerName="
				+ clusterServiceBrokerName + ", clusterServiceClassRef=" + clusterServiceClassRef + "]";
	}
}
