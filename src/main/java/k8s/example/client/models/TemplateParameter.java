package k8s.example.client.models;

public class TemplateParameter {

	private String description = null;
	private String displayName = null;
	private String from = null;
	private String generate = null;
	private String name = null;
	private Boolean required = false;
	private String value = null;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getGenerate() {
		return generate;
	}
	public void setGenerate(String generate) {
		this.generate = generate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getRequired() {
		return required;
	}
	public void setRequired(Boolean required) {
		this.required = required;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "TemplateParameter [description=" + description + ", displayName=" + displayName + ", from=" + from
				+ ", generate=" + generate + ", name=" + name + ", required=" + required + ", value=" + value + "]";
	}
}
