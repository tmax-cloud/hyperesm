package k8s.example.client.models;

public class Settings {
	private static String token = "";
	private static String namespace = "";
	
	private Settings(){}
	
	public static String getToken() {
		return token;
	}
	public static void setToken(String input) {
		token = input;
	}
	public static String getNamespace() {
		return namespace;
	}
	public static void setNamespace(String input) {
		namespace = input;
	}
}
