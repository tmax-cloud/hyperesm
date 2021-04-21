package k8s.example.client.models;

import io.kubernetes.client.openapi.models.V1ObjectMeta;

public class Image {
	private String operatorStartTime = null;
	private String apiVersion = "tmax.io/v1";
	private String kind = "Image";
	private V1ObjectMeta metadata = null;
	private ImageSpec spec = null;
	
	
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
	
	public ImageSpec getSpec() {
		return spec;
	}
	public void setSpec(ImageSpec spec) {
		this.spec = spec;
	}
	

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Image {\n");
		sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
		if(metadata != null ) 	sb.append("    metadata: ").append(toIndentedString(metadata.toString())).append("\n");
		if(spec != null ) 		sb.append("    spec: ").append(toIndentedString(spec.toString())).append("\n");
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
