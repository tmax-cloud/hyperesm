package k8s.example.client.models;

public class GetPlanDO {

	private String apiVersion = null;
	private String kind = null;
	private Metadata metadata = null;
	private GetPlanSpecDO spec = null;
	
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
	public Metadata getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	public GetPlanSpecDO getSpec() {
		return spec;
	}
	public void setSpec(GetPlanSpecDO spec) {
		this.spec = spec;
	}
	@Override
	public String toString() {
		return "GetPlanDO [apiVersion=" + apiVersion + ", kind=" + kind + ", metadata=" + metadata + ", spec=" + spec
				+ "]";
	}
}
