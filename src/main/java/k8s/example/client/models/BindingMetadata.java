package k8s.example.client.models;

public class BindingMetadata {

	private String expires_at = null;

	public String getExpires_at() {
		return expires_at;
	}
	public void setExpires_at(String expires_at) {
		this.expires_at = expires_at;
	}
	@Override
	public String toString() {
		return "BindingMetadata [expires_at=" + expires_at + "]";
	}
}
