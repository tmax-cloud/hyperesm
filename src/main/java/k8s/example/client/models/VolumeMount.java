package k8s.example.client.models;

public class VolumeMount {

	private String driver = null;
	private String container_dir = null;
	private String mode = null;
	private String device_type = null;
	private Device device = null;
	
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getContainer_dir() {
		return container_dir;
	}
	public void setContainer_dir(String container_dir) {
		this.container_dir = container_dir;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getDevice_type() {
		return device_type;
	}
	public void setDevice_type(String device_type) {
		this.device_type = device_type;
	}
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	@Override
	public String toString() {
		return "VolumeMount [driver=" + driver + ", container_dir=" + container_dir + ", mode=" + mode
				+ ", device_type=" + device_type + ", device=" + device + "]";
	}
}
