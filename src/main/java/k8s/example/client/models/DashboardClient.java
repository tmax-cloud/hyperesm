package k8s.example.client.models;

public class DashboardClient {

	private String id = null;
	private String secret = null;
	private String redirect_uri = null;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getRedirect_uri() {
		return redirect_uri;
	}
	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}
	
	@Override
	public String toString() {
		return "DashboardClient [id=" + id + ", secret=" + secret + ", redirect_uri=" + redirect_uri + "]";
	}
}
