package k8s.example.client.models;

import java.util.List;

public class ServiceOffering {
	private String name = null;
	private String id = null;
	private String description = null;
	private List<String> tags = null;
	private List<String> requires = null;
	private boolean bindable = false;
	private boolean instances_retrievable = false;
	private boolean bindings_retrievable = false;
	private boolean allow_context_updates = false;
	private ServiceMetadata metadata = null;
	private DashboardClient dashboard_client = null;
	private boolean plan_updatable = false;
	private List<ServicePlan> plans = null;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public List<String> getRequires() {
		return requires;
	}
	public void setRequires(List<String> requires) {
		this.requires = requires;
	}
	public boolean isBindable() {
		return bindable;
	}
	public void setBindable(boolean bindable) {
		this.bindable = bindable;
	}
	public boolean isInstances_retrievable() {
		return instances_retrievable;
	}
	public void setInstances_retrievable(boolean instances_retrievable) {
		this.instances_retrievable = instances_retrievable;
	}
	public boolean isBindings_retrievable() {
		return bindings_retrievable;
	}
	public void setBindings_retrievable(boolean bindings_retrievable) {
		this.bindings_retrievable = bindings_retrievable;
	}
	public boolean isAllow_context_updates() {
		return allow_context_updates;
	}
	public void setAllow_context_updates(boolean allow_context_updates) {
		this.allow_context_updates = allow_context_updates;
	}
	public ServiceMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(ServiceMetadata metadata) {
		this.metadata = metadata;
	}
	public DashboardClient getDashboard_client() {
		return dashboard_client;
	}
	public void setDashboard_client(DashboardClient dashboard_client) {
		this.dashboard_client = dashboard_client;
	}
	public boolean isPlan_updatable() {
		return plan_updatable;
	}
	public void setPlan_updatable(boolean plan_updatable) {
		this.plan_updatable = plan_updatable;
	}
	public List<ServicePlan> getPlans() {
		return plans;
	}
	public void setPlans(List<ServicePlan> plans) {
		this.plans = plans;
	}
	
	@Override
	public String toString() {
		return "ServiceOffering [name=" + name + ", id=" + id + ", description=" + description + ", tags=" + tags
				+ ", requires=" + requires + ", bindable=" + bindable + ", instances_retrievable="
				+ instances_retrievable + ", bindings_retrievable=" + bindings_retrievable + ", allow_context_updates="
				+ allow_context_updates + ", metadata=" + metadata + ", dashboard_client=" + dashboard_client
				+ ", plan_updatable=" + plan_updatable + ", plans=" + plans + "]";
	}
}
