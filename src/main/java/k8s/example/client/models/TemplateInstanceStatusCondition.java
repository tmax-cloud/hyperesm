package k8s.example.client.models;

public class TemplateInstanceStatusCondition {

	private String lastTransitionTime = null;
	private String message = null;
	private String reason = null;
	private String status = null;
	private String type = null;
	
	public String getLastTransitionTime() {
		return lastTransitionTime;
	}
	public void setLastTransitionTime(String lastTransitionTime) {
		this.lastTransitionTime = lastTransitionTime;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "TemplateInstanceStatusCondition [lastTransitionTime=" + lastTransitionTime + ", message=" + message
				+ ", reason=" + reason + ", status=" + status + ", type=" + type + "]";
	}
}
