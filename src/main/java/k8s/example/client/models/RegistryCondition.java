package k8s.example.client.models;

import org.joda.time.DateTime;

public class RegistryCondition {
	private DateTime lastProbeTime;
	private DateTime lastTransitionTime;
	private String message;
	private String reason;
	private String status;
	private String type;

	public DateTime getLastProbeTime() {
		return lastProbeTime;
	}
	public void setLastProbeTime(DateTime lastProbeTime) {
		this.lastProbeTime = lastProbeTime;
	}
	public DateTime getLastTransitionTime() {
		return lastTransitionTime;
	}
	public void setLastTransitionTime(DateTime lastTransitionTime) {
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

	/* 
	 * condition types
	 * 0: Phase
	 * 1: ReplicaSet
	 * 2: Pod
	 * 3: Service
	 * 4: Secret Opaque
	 * 5: Secret kubernetes.io/dockerconfigjson
	 * 6: Secret kubernetes.io/tls
	 * 7: Ingress
	 */
	public static enum Condition {
		REPLICA_SET("ReplicaSetReady", "/status/conditions/" + Condition.INDEX_REPLICA_SET),
		POD("PodRunning", "/status/conditions/" + Condition.INDEX_POD),
		CONTAINER("ContainerReady", "/status/conditions/" + Condition.INDEX_CONTAINER),
		SERVICE("ServiceExist", "/status/conditions/" + Condition.INDEX_SERVICE),
		SECRET_OPAQUE("SecretOpaqueExist", "/status/conditions/" + Condition.INDEX_SECRET_OPAQUE),
		SECRET_DOCKER_CONFIG_JSON("SecretDockerConfigJsonExist", "/status/conditions/" + Condition.INDEX_SECRET_DOCKER_CONFIG_JSON),
		SECRET_TLS("SecretTlsExist", "/status/conditions/" + Condition.INDEX_SECRET_TLS),
		INGRESS("IngressExist", "/status/conditions/" + Condition.INDEX_INGRESS),
		;

		public static final int INDEX_REPLICA_SET = 0;
		public static final int INDEX_POD = 1;
		public static final int INDEX_CONTAINER = 2;
		public static final int INDEX_SERVICE = 3;
		public static final int INDEX_SECRET_OPAQUE = 4;
		public static final int INDEX_SECRET_DOCKER_CONFIG_JSON = 5;
		public static final int INDEX_SECRET_TLS = 6;
		public static final int INDEX_INGRESS = 7;
		private String type;
		private String path;

		Condition(String type, String path) {
			this.type = type;
			this.path = path;
		}
		
		public String getType() { return type; }
		public String getPath() { return path; }
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class V1PodCondition {\n");
		sb.append("    lastProbeTime: ").append(toIndentedString(lastProbeTime)).append("\n");
		sb.append("    lastTransitionTime: ").append(toIndentedString(lastTransitionTime)).append("\n");
		sb.append("    message: ").append(toIndentedString(message)).append("\n");
		sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
