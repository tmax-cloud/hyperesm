
package k8s.example.client;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import k8s.example.client.audit.AuditController;
import k8s.example.client.handler.UserDeleteJob;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.metering.MeteringJob;

public class Main {
	public static Logger logger = LoggerFactory.getLogger("K8SOperator");
	public static void main(String[] args) {
		try {
			// Start webhook server
			logger.info("[Main] Start webhook server");
			new WebHookServer();
			
			// Start Metering
			logger.info("[Main] Start Metering per 30 mins");
			startMeteringTimer();
			
			// Start UserDelete
			logger.info("[Main] Start User Delete per Week");
			startUserDeleteTimer();

			// Init K8S Client
			logger.info("[Main] Init K8S Client");
			K8sApiCaller.initK8SClient();
			
			// Start Trial Namespace Timer
			logger.info("[Main] Start Trial Namespace Timer");
			startTrialNSTimer();
			
			// Start Audit
			logger.info("[Main] Start Audit controller");
			AuditController.start();
			
			// Start Start K8S watchers & Controllers
			logger.info("[Main] Start K8S watchers");
			K8sApiCaller.startWatcher(); // Infinite loop
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void startTrialNSTimer() {
		try {
			V1NamespaceList nsList = K8sApiCaller.listNameSpace();
			for ( V1Namespace ns : nsList.getItems()) {
				if( ns.getMetadata().getLabels() != null && ns.getMetadata().getLabels().get("trial") != null
						&& ns.getMetadata().getLabels().get("owner") != null) {
					logger.info("[Main] Trial NameSpace : " + ns.getMetadata().getName());
					Util.setTrialNSTimer(ns);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void startMeteringTimer() throws SchedulerException {
		JobDetail job = JobBuilder.newJob( MeteringJob.class )
				.withIdentity( "MeteringJob" ).build();

		CronTrigger cronTrigger = TriggerBuilder
				.newTrigger()
				.withIdentity( "MeteringCronTrigger" )
				.withSchedule(
				CronScheduleBuilder.cronSchedule( Constants.METERING_CRON_EXPRESSION ))
				.build();

		Scheduler sch = new StdSchedulerFactory().getScheduler();
		sch.start(); sch.scheduleJob( job, cronTrigger );
	}
	
	private static void startUserDeleteTimer() throws SchedulerException {
		JobDetail job = JobBuilder.newJob( UserDeleteJob.class )
				.withIdentity( "UserDeleteJob" ).build();

		CronTrigger cronTrigger = TriggerBuilder
				.newTrigger()
				.withIdentity( "UserDeleteCronTrigger" )
				.withSchedule(
				CronScheduleBuilder.cronSchedule( Constants.USER_DELETE_CRON_EXPRESSION ))
				.build();

		Scheduler sch = new StdSchedulerFactory().getScheduler();
		sch.start(); sch.scheduleJob( job, cronTrigger );
	}
}