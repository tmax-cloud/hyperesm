package k8s.example.client.models;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1Toleration;

public class RegistryReplicaSet {
	private Map<String, String> labels = null;
	private Map<String, String> nodeSelector = null;
	private V1LabelSelector selector = null;
	private List<V1Toleration> tolerations = null;
	
	public Map<String, String> getLabels() {
		return labels;
	}
	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}
	public Map<String, String> getNodeSelector() {
		return nodeSelector;
	}
	public void setNodeSelector(Map<String, String> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}
	public V1LabelSelector getSelector() {
		return selector;
	}
	public void setSelector(V1LabelSelector selector) {
		this.selector = selector;
	}
	public List<V1Toleration> getTolerations() {
		return tolerations;
	}
	public void setTolerations(List<V1Toleration> tolerations) {
		this.tolerations = tolerations;
	}
	
}
