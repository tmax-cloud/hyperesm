package k8s.example.client.metering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class TimerMap {
	private static Map<String, Timer> timerList = new HashMap<>();
	
	public static void addTimer( String nsName, Timer timer ) {
		timerList.put(nsName, timer);
	}
	
	public static void removeTimer( String nsName ) {
		timerList.remove(nsName);
	}
	
	public static Timer getTimer( String nsName ) {
		return timerList.get(nsName);
	}
	
	public static boolean isExists( String nsName ) {
		if (timerList.containsKey(nsName)) return true;
		return false;
	}
	
	public static List<String> getTimerList() {
		List <String> nsNameList = null;
		for(String nsName :  timerList.keySet()) {
			if (nsNameList == null) nsNameList = new ArrayList<>();
			nsNameList.add(nsName);
		}
		return nsNameList;
	}
}



