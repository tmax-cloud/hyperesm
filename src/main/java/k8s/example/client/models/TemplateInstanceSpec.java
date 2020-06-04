package k8s.example.client.models;

public class TemplateInstanceSpec {

	private TemplateInstanceSpecRequester requester = null;
	private TemplateInstanceSpecSecret secret = null;
	private TemplateInstanceSpecTemplate template = null;
	
	public TemplateInstanceSpecRequester getRequester() {
		return requester;
	}
	public void setRequester(TemplateInstanceSpecRequester requester) {
		this.requester = requester;
	}
	public TemplateInstanceSpecSecret getSecret() {
		return secret;
	}
	public void setSecret(TemplateInstanceSpecSecret secret) {
		this.secret = secret;
	}
	public TemplateInstanceSpecTemplate getTemplate() {
		return template;
	}
	public void setTemplate(TemplateInstanceSpecTemplate template) {
		this.template = template;
	}
	@Override
	public String toString() {
		return "TemplateInstanceSpec [requester=" + requester + ", secret=" + secret + ", template=" + template + "]";
	}
}
