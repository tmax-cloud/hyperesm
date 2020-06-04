package k8s.example.client.metering.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metric {
	private Map<String,String> metric;
	private List<String> value;
	public Map<String, String> getMetric() {
		return metric;
	}
	public void setMetric(Map<String, String> metric) {
		this.metric = metric;
	}
	public List<String> getValue() {
		return value;
	}
	public void setValue(List<String> value) {
		this.value = value;
	}
	public void putMetric(String key, String value) {
		if(this.metric == null) this.metric = new HashMap<>();
		this.metric.put(key, value);
	}
	public void addValue(String value) {
		if(this.value == null) this.value = new ArrayList<>();
		this.value.add(value);
	}
}
