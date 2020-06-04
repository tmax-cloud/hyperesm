package k8s.example.client.models;

import java.util.Date;

public class ClaimStatus {
	public static final String NAMESPACE_CLAIM_AWAITING = "Awaiting";
	public static final String NAMESPACE_CLAIM_SUCCESS = "Success";
	public static final String NAMESPACE_CLAIM_REJECT = "Reject";

	private Date lastTransitionTime;
	private String message;
	private String reason;
	private String status;

	public Date getLastTransitionTime() {
		return lastTransitionTime;
	}

	public void setLastTransitionTime(Date lastTransitionTime) {
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
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Namespace Claim Status {\n");
		if(message != null ) 	sb.append("    message: ").append(toIndentedString(message)).append("\n");
		if(reason != null ) 		sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
		if(status != null ) 	sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
