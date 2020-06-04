package k8s.example.client.handler;

import java.util.Calendar;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Main;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;

public class UserDeleteJob implements Job{
	
	long time = System.currentTimeMillis();
    private Logger logger = Main.logger;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		logger.info( "============= User Clear Time =============" );
		logger.info( "minute of hour	 : " + calendar.get(Calendar.MINUTE) );
		logger.info( "hour of day	 : " + calendar.get(Calendar.HOUR_OF_DAY) );
		logger.info( "day of month	 : " + calendar.get(Calendar.DAY_OF_MONTH) );
		logger.info( "day of year	 : " + calendar.get(Calendar.DAY_OF_YEAR) );
		
		try {
			List < String > deletedUserIdList = K8sApiCaller.deleteBlockedUser();
			if ( deletedUserIdList !=null ) OAuthApiCaller.deleteBlockedUser( deletedUserIdList );

		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getResponseBody() );
			e.printStackTrace();
		} catch (Exception e) {
			logger.info(" Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}	
}
