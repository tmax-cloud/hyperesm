package k8s.example.client.models;

public class ServicePlan {
	
	private String id = null;
	private String name = null;
	private String description = null;
	private PlanMetadata metadata = null;
	private boolean free = false;
	private boolean bindable = false;
	private boolean plan_updateable = false;
	private Schemas schemas = null;
	private Integer maximum_polling_duration = null;
	private MaintenanceInfo maintenance_info = null;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public PlanMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(PlanMetadata metadata) {
		this.metadata = metadata;
	}
	public boolean isFree() {
		return free;
	}
	public void setFree(boolean free) {
		this.free = free;
	}
	public boolean isBindable() {
		return bindable;
	}
	public void setBindable(boolean bindable) {
		this.bindable = bindable;
	}
	public boolean isPlan_updateable() {
		return plan_updateable;
	}
	public void setPlan_updateable(boolean plan_updateable) {
		this.plan_updateable = plan_updateable;
	}
	public Schemas getSchemas() {
		return schemas;
	}
	public void setSchemas(Schemas schemas) {
		this.schemas = schemas;
	}
	public Integer getMaximum_polling_duration() {
		return maximum_polling_duration;
	}
	public void setMaximum_polling_duration(Integer maximum_polling_duration) {
		this.maximum_polling_duration = maximum_polling_duration;
	}
	public MaintenanceInfo getMaintenance_info() {
		return maintenance_info;
	}
	public void setMaintenance_info(MaintenanceInfo maintenance_info) {
		this.maintenance_info = maintenance_info;
	}
	
	@Override
	public String toString() {
		return "ServicePlan [id=" + id + ", name=" + name + ", description=" + description + ", metadata=" + metadata
				+ ", free=" + free + ", bindable=" + bindable + ", plan_updateable=" + plan_updateable + ", schemas="
				+ schemas + ", maximum_polling_duration=" + maximum_polling_duration + ", maintenance_info="
				+ maintenance_info + "]";
	}
}
