package k8s.example.client.models;

import java.util.ArrayList;
import java.util.List;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1RoleRef;
import io.kubernetes.client.openapi.models.V1Subject;

public class RoleBindingClaim {
	private String apiVersion = "tmax.io/v1";
	private String kind = "RoleBindingClaim";
	private String resourceName = null;
	private V1ObjectMeta metadata = null;
	private List<V1Subject> subjects = null;
	private V1RoleRef roleRef = null;
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

	public List<V1Subject> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<V1Subject> subjects) {
		this.subjects = subjects;
	}
	
	public void addSubjects(V1Subject subjects) {
		if ( this.subjects == null ) this.subjects = new ArrayList<>(); 
		this.subjects.add(subjects);
	}

	public V1RoleRef getRoleRef() {
		return roleRef;
	}

	public void setRoleRef(V1RoleRef roleRef) {
		this.roleRef = roleRef;
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
		if(subjects != null ) 		sb.append("    subjects: ").append(toIndentedString(subjects.toString())).append("\n");
		if(roleRef != null ) 		sb.append("    roleRef: ").append(toIndentedString(roleRef.toString())).append("\n");
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
