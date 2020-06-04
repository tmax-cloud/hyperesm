package k8s.example.client.models;

public class Device {

	private String volume_id = null;
	private Object mount_config = null;
	
	public String getVolume_id() {
		return volume_id;
	}
	public void setVolume_id(String volume_id) {
		this.volume_id = volume_id;
	}
	public Object getMount_config() {
		return mount_config;
	}
	public void setMount_config(Object mount_config) {
		this.mount_config = mount_config;
	}
	@Override
	public String toString() {
		return "Device [volume_id=" + volume_id + ", mount_config=" + mount_config + "]";
	}
}
