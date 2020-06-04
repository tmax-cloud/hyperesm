package k8s.example.client.metering;

import java.util.Random;

public class Test {

	public static void main(String[] args) throws Exception {
        
		Random rand = new Random();
		String ran = Integer.toString(rand.nextInt(10));
		System.out.println("ran : "  +  ran);
		

	}
}
