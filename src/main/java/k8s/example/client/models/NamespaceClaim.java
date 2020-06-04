package k8s.example.client.models;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1ResourceQuotaSpec;

public class NamespaceClaim {
	private String apiVersion = "tmax.io/v1";
	private String kind = null;
	private String resourceName = null;	
	private V1ObjectMeta metadata = null;
	private V1ResourceQuotaSpec spec = null;
	private ClaimStatus status = null;
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
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public V1ObjectMeta getMetadata() {
		return metadata;
	}
	public void setMetadata(V1ObjectMeta metadata) {
		this.metadata = metadata;
	}
	public V1ResourceQuotaSpec getSpec() {
		return spec;
	}
	public void setSpec(V1ResourceQuotaSpec spec) {
		this.spec = spec;
	}
	public ClaimStatus getStatus() {
		return status;
	}
	public void setStatus(ClaimStatus status) {
		this.status = status;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Namespace Claim {\n");
		sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
		if(metadata != null ) 	sb.append("    metadata: ").append(toIndentedString(metadata.toString())).append("\n");
		if(resourceName != null ) 	sb.append("    resourceName: ").append(toIndentedString(resourceName.toString())).append("\n");
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
