package k8s.example.client.models;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import k8s.example.client.Constants;

public class Registry {
	public static String REGISTRY_LOGIN_URL = Constants.CUSTOM_OBJECT_GROUP + "/" + "registry-login-url";
	
	public static String REGISTRY_KIND = "Registry";
	private String operatorStartTime = null;
	private String apiVersion = "tmax.io/v1";
	private String kind = "Registry";
	private V1ObjectMeta metadata = null;
	private RegistrySpec spec = null;
	private RegistryStatus status = null;
	
	
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
	
	public V1ObjectMeta getMetadata() {
		return metadata;
	}
	public void setMetadata(V1ObjectMeta metadata) {
		this.metadata = metadata;
	}
	
	public RegistrySpec getSpec() {
		return spec;
	}
	public void setSpec(RegistrySpec spec) {
		this.spec = spec;
	}
	
	public RegistryStatus getStatus() {
		return status;
	}
	public void setStatus(RegistryStatus status) {
		this.status = status;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Registry {\n");
		sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
		if(metadata != null ) 	sb.append("    metadata: ").append(toIndentedString(metadata.toString())).append("\n");
		if(spec != null ) 		sb.append("    spec: ").append(toIndentedString(spec.toString())).append("\n");
		if(status != null ) 	sb.append("    status: ").append(toIndentedString(status.toString())).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
	
}
