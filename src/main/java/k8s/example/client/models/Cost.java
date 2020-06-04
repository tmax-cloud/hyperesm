package k8s.example.client.models;

public class Cost {
	
	private Object amount = null;
	private String unit = null;
	
	public Object getAmount() {
		return amount;
	}
	public void setAmount(Object amount) {
		this.amount = amount;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	@Override
	public String toString() {
		return "Cost [amount=" + amount + ", unit=" + unit + "]";
	}
}
