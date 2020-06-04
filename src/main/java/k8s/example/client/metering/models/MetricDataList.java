package k8s.example.client.metering.models;

import java.util.ArrayList;
import java.util.List;

public class MetricDataList {
	private String resultType;
	private List<Metric> result;
	public String getResultType() {
		return resultType;
	}
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	public List<Metric> getResult() {
		return result;
	}
	public void setResult(List<Metric> result) {
		this.result = result;
	}
	public void addResult(Metric value) {
		if(this.result == null) this.result = new ArrayList<>();
		this.result.add(value);
	}
}
