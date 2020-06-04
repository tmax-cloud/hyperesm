package k8s.example.client.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.openapi.models.V1OwnerReference;

public class Metadata {
	private Map<String, String> annotations = null;
	private String clusterName = null;
	private String creationTimestamp = null;
	private Long deletionGracePeriodSeconds = null;
	private String deletionTimestamp = null;
	private List<String> finalizers = null;
	private String generateName = null;
	private Integer generation = null;
	private Map<String, String> labels = null;
	private Object managedFields = null;
	private String name = null;
	private String namespace = null;
	private List<V1OwnerReference> ownerReferences = null;
	private String resourceVersion = null;
	private String selfLink = null;
	private String uid = null;
	
	public Map<String, String> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getCreationTimestamp() {
		return creationTimestamp;
	}
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	public Long getDeletionGracePeriodSeconds() {
		return deletionGracePeriodSeconds;
	}
	public void setDeletionGracePeriodSeconds(Long deletionGracePeriodSeconds) {
		this.deletionGracePeriodSeconds = deletionGracePeriodSeconds;
	}
	public String getDeletionTimestamp() {
		return deletionTimestamp;
	}
	public void setDeletionTimestamp(String deletionTimestamp) {
		this.deletionTimestamp = deletionTimestamp;
	}
	public List<String> getFinalizers() {
		return finalizers;
	}
	public void setFinalizers(List<String> finalizers) {
		this.finalizers = finalizers;
	}
	public String getGenerateName() {
		return generateName;
	}
	public void setGenerateName(String generateName) {
		this.generateName = generateName;
	}
	public Integer getGeneration() {
		return generation;
	}
	public void setGeneration(Integer generation) {
		this.generation = generation;
	}
	public Map<String, String> getLabels() {
		return labels;
	}
	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}
	public Object getManagedFields() {
		return managedFields;
	}
	public void setManagedFields(Object managedFields) {
		this.managedFields = managedFields;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public List<V1OwnerReference> getOwnerReferences() {
		return ownerReferences;
	}
	public void setOwnerReferences(List<V1OwnerReference> ownerReferences) {
		this.ownerReferences = ownerReferences;
	}
	public void addOwnerReferences(V1OwnerReference ownerReference) {
		if (this.ownerReferences==null) this.ownerReferences = new ArrayList<>();
		this.ownerReferences.add(ownerReference);
	}
	public String getResourceVersion() {
		return resourceVersion;
	}
	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}
	public String getSelfLink() {
		return selfLink;
	}
	public void setSelfLink(String selfLink) {
		this.selfLink = selfLink;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	@Override
	public String toString() {
		return "Metadata [annotations=" + annotations + ", clusterName=" + clusterName + ", creationTimestamp="
				+ creationTimestamp + ", deletionGracePeriodSeconds=" + deletionGracePeriodSeconds
				+ ", deletionTimestamp=" + deletionTimestamp + ", finalizers=" + finalizers + ", generateName="
				+ generateName + ", generation=" + generation + ", labels=" + labels + ", managedFields="
				+ managedFields + ", name=" + name + ", namespace=" + namespace + ", ownerReferences=" + ownerReferences
				+ ", resourceVersion=" + resourceVersion + ", selfLink=" + selfLink + ", uid=" + uid + "]";
	}
}
