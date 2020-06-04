package k8s.example.client.models;

public class ServiceMetadata {

	private String displayName = null;
	private String imageUrl = null;
	private String longDescription = null;
	private String providerDisplayName = null;
	private String documentationUrl = null;
	private String supportUrl = null;
	private String urlDescription = null;
	private String markdownDescription = null;
	private boolean recommend = false;
	
	public String getUrlDescription() {
		return urlDescription;
	}

	public void setUrlDescription(String urlDescription) {
		this.urlDescription = urlDescription;
	}

	public String getMarkdownDescription() {
		return markdownDescription;
	}

	public void setMarkdownDescription(String markdownDescription) {
		this.markdownDescription = markdownDescription;
	}

	public boolean isRecommend() {
		return recommend;
	}

	public void setRecommend(boolean recommend) {
		this.recommend = recommend;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	
	public String getLongDescription() {
		return longDescription;
	}
	
	public String getProviderDisplayName() {
		return providerDisplayName;
	}
	
	public String getDocumentationUrl() {
		return documentationUrl;
	}
	
	public String getSupportUrl() {
		return supportUrl;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public void setProviderDisplayName(String providerDisplayName) {
		this.providerDisplayName = providerDisplayName;
	}

	public void setDocumentationUrl(String documentationUrl) {
		this.documentationUrl = documentationUrl;
	}

	public void setSupportUrl(String supportUrl) {
		this.supportUrl = supportUrl;
	}

	@Override
	public String toString() {
		return "ServiceMetadata [displayName=" + displayName + ", imageUrl=" + imageUrl + ", longDescription="
				+ longDescription + ", providerDisplayName=" + providerDisplayName + ", documentationUrl="
				+ documentationUrl + ", supportUrl=" + supportUrl + "]";
	}
}
