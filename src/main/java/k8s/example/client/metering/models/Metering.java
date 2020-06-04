package k8s.example.client.metering.models;

public class Metering {
	String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	String namespace;
	double cpu = 0;
	long memory = 0;
	long storage = 0;
	double gpu = 0;
	int publicIp = 0;
	int privateIp = 0;
	long meteringTime = 0;
	public double getGpu() {
		return gpu;
	}
	public void setGpu(double gpu) {
		this.gpu = gpu;
	}
	public long getMeteringTime() {
		return meteringTime;
	}
	public void setMeteringTime(long meteringTime) {
		this.meteringTime = meteringTime;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public double getCpu() {
		return cpu;
	}
	public void setCpu(double cpu) {
		this.cpu = cpu;
	}
	public long getMemory() {
		return memory;
	}
	public void setMemory(long memory) {
		this.memory = memory;
	}
	public long getStorage() {
		return storage;
	}
	public void setStorage(long storage) {
		this.storage = storage;
	}
	public int getPublicIp() {
		return publicIp;
	}
	public void setPublicIp(int publicIp) {
		this.publicIp = publicIp;
	}
	public int getPrivateIp() {
		return privateIp;
	}
	public void setPrivateIp(int privateIp) {
		this.privateIp = privateIp;
	}
}
