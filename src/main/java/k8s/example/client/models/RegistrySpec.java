package k8s.example.client.models;

public class RegistrySpec {
	private String image = null;
	private String description = null;
	private String loginId = null;
	private String loginPassword = null;
	private String customConfigYml = null;
	
	private String domainName = null;
	private RegistryReplicaSet replicaSet = null;
	private RegistryService service = null;
	private RegistryPVC persistentVolumeClaim = null;

	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public String getLoginPassword() {
		return loginPassword;
	}
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	public String getCustomConfigYml() {
		return customConfigYml;
	}
	public void setCustomConfigYml(String customConfigYml) {
		this.customConfigYml = customConfigYml;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public RegistryReplicaSet getReplicaSet() {
		return replicaSet;
	}
	public void setReplicaSet(RegistryReplicaSet replicaSet) {
		this.replicaSet = replicaSet;
	}
	public RegistryService getService() {
		return service;
	}
	public void setService(RegistryService service) {
		this.service = service;
	}
	public RegistryPVC getPersistentVolumeClaim() {
		return persistentVolumeClaim;
	}
	public void setPersistentVolumeClaim(RegistryPVC persistentVolumeClaim) {
		this.persistentVolumeClaim = persistentVolumeClaim;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RegistrySpec {\n");
		sb.append("    image: ").append(toIndentedString(image)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    loginId: ").append(toIndentedString(loginId)).append("\n");
		sb.append("    loginPassword: ").append(toIndentedString(loginPassword)).append("\n");
		sb.append("    customConfigYml: ").append(toIndentedString(customConfigYml)).append("\n");
		sb.append("    domainName: ").append(toIndentedString(domainName)).append("\n");
		sb.append("    replicaSet: ").append(toIndentedString(replicaSet)).append("\n");
		sb.append("    service: ").append(toIndentedString(service)).append("\n");
		sb.append("    persistentVolumeClaim: ").append(toIndentedString(persistentVolumeClaim)).append("\n");
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
