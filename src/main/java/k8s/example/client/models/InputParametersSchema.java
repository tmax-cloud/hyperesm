package k8s.example.client.models;

import java.util.Map;

public class InputParametersSchema {

	private Map<String, String> parameters = null;

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "InputParametersSchema [parameters=" + parameters + "]";
	}
}
