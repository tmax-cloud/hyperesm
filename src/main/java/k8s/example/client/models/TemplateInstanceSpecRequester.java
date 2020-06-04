package k8s.example.client.models;

import java.util.List;

public class TemplateInstanceSpecRequester {

	private Object extra = null;
	private List<String> groups = null; 
	private String uid = null;
	private String username = null;
	
	public Object getExtra() {
		return extra;
	}
	public void setExtra(Object extra) {
		this.extra = extra;
	}
	public List<String> getGroups() {
		return groups;
	}
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String toString() {
		return "TemplateInstanceSpecRequester [extra=" + extra + ", groups=" + groups + ", uid=" + uid + ", username="
				+ username + "]";
	}
}
