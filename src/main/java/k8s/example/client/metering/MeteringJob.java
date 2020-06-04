package k8s.example.client.metering;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.k8s.util.LogPreparedStatement;
import k8s.example.client.metering.models.Metering;
import k8s.example.client.metering.models.Metric;
import k8s.example.client.metering.models.MetricDataList;
import k8s.example.client.metering.util.UIDGenerator;

public class MeteringJob implements Job{
	
	private Map<String,Metering> meteringData = new HashMap<>();
	Connection conn = null;
    private Logger logger = Main.logger;
	long time = System.currentTimeMillis();
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			logger.info("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.info("Class Not Found Exection");
			e.printStackTrace();
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		logger.info( "============= Metering Time =============" );
		logger.info("Current Time : " + time);
		logger.info( "minute of hour	 : " + calendar.get(Calendar.MINUTE) );
		logger.info( "hour of day	 : " + calendar.get(Calendar.HOUR_OF_DAY) );
		logger.info( "day of month	 : " + calendar.get(Calendar.DAY_OF_MONTH) );
		logger.info( "day of year	 : " + calendar.get(Calendar.DAY_OF_YEAR) );
		
		if ( calendar.get(Calendar.MINUTE) == 0 ) {
			// Insert to metering_hour
			insertMeteringHour();
		} else if ( calendar.get(Calendar.HOUR_OF_DAY) == 0 ) {
			// Insert to metering_day
			insertMeteringDay();
		} else if ( calendar.get(Calendar.DAY_OF_MONTH) == 1 ) {
			// Insert to metering_month
			insertMeteringMonth();
		} else if ( calendar.get(Calendar.DAY_OF_YEAR) == 1 ) {
			// Insert to metering_year
			insertMeteringYear();
		}
		
		
		// Insert to metering (new data)
		makeMeteringMap();
		logger.info( "============= Metering Data =============" );
		for( String key : meteringData.keySet() ){
			logger.info( key + "/cpu : " + meteringData.get(key).getCpu() );
			logger.info( key + "/memory : " + meteringData.get(key).getMemory() );
			logger.info( key + "/storage : " + meteringData.get(key).getStorage() );
			logger.info( key + "/publicIp : " + meteringData.get(key).getPublicIp() );
        }
		
		insertMeteringData();
		
		deleteMeteringData();
	}
	
	private void insertMeteringData() {
		try {
			logger.info("Insert into Mertering Start!!");
			logger.info("Current Time : " + time);
			
			String query = "insert into metering.metering (id,namespace,cpu,memory,storage,gpu,public_ip,private_ip,metering_time,status) "
					+ "values (?,?,truncate(?,2),?,?,truncate(?,2),?,?,?,?)";
			LogPreparedStatement pstmt = new LogPreparedStatement( conn, query );
			for( String key : meteringData.keySet() ){
				pstmt.setString( 1, UIDGenerator.getInstance().generate32( meteringData.get(key), 16, time ) );
				pstmt.setString( 2, key );
				pstmt.setDouble( 3, meteringData.get(key).getCpu() );
				pstmt.setLong( 4, meteringData.get(key).getMemory() );
				pstmt.setLong( 5, meteringData.get(key).getStorage() );
				pstmt.setDouble( 6, meteringData.get(key).getGpu() );
				pstmt.setInt( 7, meteringData.get(key).getPublicIp() );
				pstmt.setInt( 8, meteringData.get(key).getPrivateIp() );
				pstmt.setTimestamp( 9, new Timestamp(time) );
				pstmt.setString( 10, "Success" );
				pstmt.addBatch();
			}

			pstmt.executeBatch();
			pstmt.close();
			conn.commit();
			conn.close();
			logger.info("Insert into Mertering Success!!");

		} catch (SQLException e) {
			logger.info("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void makeMeteringMap() {
		MetricDataList cpu = getMeteringData("sum(kube_pod_container_resource_requests{resource=\"cpu\"})by(namespace)");
		for ( Metric metric : cpu.getResult() ) {
			if ( meteringData.containsKey(metric.getMetric().get("namespace"))) {
				meteringData.get(metric.getMetric().get("namespace")).setCpu(Double.parseDouble(metric.getValue().get(1)));
			} else {
				Metering metering = new Metering();
				metering.setNamespace(metric.getMetric().get("namespace"));
				metering.setCpu(Double.parseDouble(metric.getValue().get(1)));
				meteringData.put(metric.getMetric().get("namespace"), metering);
			}
		}
		MetricDataList memory = getMeteringData("sum(kube_pod_container_resource_requests{resource=\"memory\"})by(namespace)");
		for ( Metric metric : memory.getResult() ) {
			if ( meteringData.containsKey(metric.getMetric().get("namespace"))) {
				meteringData.get(metric.getMetric().get("namespace")).setMemory(Long.parseLong(metric.getValue().get(1)));
			} else {
				Metering metering = new Metering();
				metering.setNamespace(metric.getMetric().get("namespace"));
				metering.setMemory(Long.parseLong(metric.getValue().get(1)));
				meteringData.put(metric.getMetric().get("namespace"), metering);
			}
		}
		MetricDataList storage = getMeteringData("sum(kube_persistentvolumeclaim_resource_requests_storage_bytes)by(namespace)");
		for ( Metric metric : storage.getResult() ) {
			if ( meteringData.containsKey(metric.getMetric().get("namespace"))) {
				meteringData.get(metric.getMetric().get("namespace")).setStorage(Long.parseLong(metric.getValue().get(1)));
			} else {
				Metering metering = new Metering();
				metering.setNamespace(metric.getMetric().get("namespace"));
				metering.setStorage(Long.parseLong(metric.getValue().get(1)));
				meteringData.put(metric.getMetric().get("namespace"), metering);
			}
		}
		MetricDataList publicIp = getMeteringData("count(kube_service_spec_type{type=\"LoadBalancer\"})by(namespace)");
		for ( Metric metric : publicIp.getResult() ) {
			if ( meteringData.containsKey(metric.getMetric().get("namespace"))) {
				meteringData.get(metric.getMetric().get("namespace")).setPublicIp(Integer.parseInt(metric.getValue().get(1)));
			} else {
				Metering metering = new Metering();
				metering.setNamespace(metric.getMetric().get("namespace"));
				metering.setPublicIp(Integer.parseInt(metric.getValue().get(1)));
				meteringData.put(metric.getMetric().get("namespace"), metering);
			}
		}
		//TODO : gpu 샘플 필요
	}
	
	private MetricDataList getMeteringData(String query) {
		MetricDataList metricObject = null;
		try {
			OkHttpClient client = new OkHttpClient();
			URL url = new URL("http://prometheus-k8s.monitoring:9090/api/v1/query");
			HttpUrl.Builder httpBuilder = HttpUrl.get(url).newBuilder();
			httpBuilder.addQueryParameter("query", query);

			Request request = new Request.Builder()
					//.addHeader("x-api-key", RestTestCommon.API_KEY)
					.url(httpBuilder.build())
					.build();

			Response response = client.newCall(request).execute(); 
			String message = response.body().string();
			//logger.info(message);

			JsonParser parser = new JsonParser();
			String metricData = parser.parse( message ).getAsJsonObject().get("data").toString();
			metricObject = new Gson().fromJson(metricData, MetricDataList.class);
			
		} catch (Exception e){
			logger.info("Exception : " + e.getMessage());
		}
		return metricObject;
	}

	private void deleteMeteringData() {
		logger.info( "============ Retention Time =============" );
		logger.info( "Retention Time - Hour  : " + System.getenv( "RETENTION_HOUR" ) );
		logger.info( "Retention Time - Day   : " + System.getenv( "RETENTION_DAY" ) );
		logger.info( "Retention Time - Month : " + System.getenv( "RETENTION_MONTH" ) );
		logger.info( "=========================================" );	
		//TODO :  일해라 태건아
		}
	
	
	private Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName( Constants.JDBC_DRIVER );
		return DriverManager.getConnection( Constants.DB_URL, Constants.USERNAME, System.getenv( "DB_PASSWORD" ) );
	}
	
	
	
	@SuppressWarnings("resource")
	private void insertMeteringHour() {
		try {
			logger.info("Insert into Metering_hour Start!!");
			logger.info("Current Time : " + time);

			String insertQuery = "insert into metering.metering_hour values (?,?,?,?,?,?,?,?,?,?)"; 
			String selectQuery = "select id, namespace, truncate(sum(cpu)/count(*),2) as cpu, truncate(sum(memory)/count(*),0) as memory, "
					+ "truncate(sum(storage)/count(*),0) as storage, truncate(sum(gpu)/count(*),2) as gpu, "
					+ "truncate(sum(public_ip)/count(*),0) as public_ip, truncate(sum(private_ip)/count(*),0) as private_ip, "
					+ "date_format(metering_time,'%Y-%m-%d %H:00:00') as metering_time, status from metering.metering group by hour(metering_time), namespace"; 
			LogPreparedStatement pstmtinsert = new LogPreparedStatement( conn, insertQuery );
			LogPreparedStatement pstmtSelect = new LogPreparedStatement( conn, selectQuery );
			ResultSet rsSelect = pstmtSelect.executeQuery();
			while ( rsSelect.next() ) {
				int i = 1;
				pstmtinsert.setString(i++, UIDGenerator.getInstance().generate32( rsSelect.getString("id"), 16, time ) );
				pstmtinsert.setString(i++, rsSelect.getString("namespace"));
				pstmtinsert.setDouble(i++, rsSelect.getDouble("cpu"));
				pstmtinsert.setLong(i++, rsSelect.getLong("memory"));
				pstmtinsert.setLong(i++, rsSelect.getLong("storage"));
				pstmtinsert.setDouble(i++, rsSelect.getDouble("gpu"));
				pstmtinsert.setInt(i++, rsSelect.getInt("public_ip"));
				pstmtinsert.setInt(i++, rsSelect.getInt("private_ip"));
				pstmtinsert.setTimestamp(i++, rsSelect.getTimestamp("metering_time"));
				pstmtinsert.setString(i++, rsSelect.getString("status"));
//				logger.info("pstmtinsert : " + pstmtinsert.getQueryString());
				pstmtinsert.addBatch();							
			}
			try {
				pstmtinsert.executeBatch();
			}catch(SQLException e) {
				logger.info("SQL Exception : " + e.getMessage());
			}
			pstmtinsert.close();
			pstmtSelect.close();
			logger.info("Insert into Metering_hour Success!!");

			logger.info("Delete Metering for past 1 hour Start!!");
			String deleteQuery = "truncate metering.metering";
			LogPreparedStatement pstmtDelete = new LogPreparedStatement( conn, deleteQuery );
			pstmtDelete.execute();
			pstmtDelete.close();
			logger.info("Delete Metering for past 1 hour Success!!");

			conn.commit();

		} catch (SQLException e) {
			logger.info("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	private void insertMeteringDay() {
		try {
			logger.info("Insert into Metering_day Start!!");
			logger.info("Current Time : " + time);

			String insertQuery = "insert into metering.metering_day values (?,?,?,?,?,?,?,?,?,?)"; 
			String selectQuery = "select id, namespace, truncate(sum(cpu)/count(*),2) as cpu, truncate(sum(memory)/count(*),0) as memory, "
					+ "truncate(sum(storage)/count(*),0) as storage, truncate(sum(gpu)/count(*),2) as gpu, "
					+ "truncate(sum(public_ip)/count(*),0) as public_ip, truncate(sum(private_ip)/count(*),0) as private_ip, "
					+ "date_format(metering_time,'%Y-%m-%d 00:00:00') as metering_time, status from metering.metering_hour where status = 'Success' "
					+ "group by day(metering_time), namespace"; 
			LogPreparedStatement pstmtinsert = new LogPreparedStatement( conn, insertQuery );
			LogPreparedStatement pstmtSelect = new LogPreparedStatement( conn, selectQuery );
			ResultSet rsSelect = pstmtSelect.executeQuery();
			while ( rsSelect.next() ) {
				int i = 1;
				pstmtinsert.setString(i++, UIDGenerator.getInstance().generate32( rsSelect.getString("id"), 16, time ) );
				pstmtinsert.setString(i++, rsSelect.getString("namespace"));
				pstmtinsert.setDouble(i++, rsSelect.getDouble("cpu"));
				pstmtinsert.setLong(i++, rsSelect.getLong("memory"));
				pstmtinsert.setLong(i++, rsSelect.getLong("storage"));
				pstmtinsert.setDouble(i++, rsSelect.getDouble("gpu"));
				pstmtinsert.setInt(i++, rsSelect.getInt("public_ip"));
				pstmtinsert.setInt(i++, rsSelect.getInt("private_ip"));
				pstmtinsert.setTimestamp(i++, rsSelect.getTimestamp("metering_time"));
				pstmtinsert.setString(i++, rsSelect.getString("status"));
//				logger.info("pstmtinsert : " + pstmtinsert.getQueryString());
				pstmtinsert.addBatch();							
			}
			try {
				pstmtinsert.executeBatch();
			}catch(SQLException e) {
				logger.info("SQL Exception : " + e.getMessage());
			}
			pstmtinsert.close();
			pstmtSelect.close();
			logger.info("Insert into Metering_Day Success!!");
			
			String updateQuery = "update metering.metering_hour set status = 'Merged' where status = 'Success'";
			LogPreparedStatement pstmtUpdate = new LogPreparedStatement( conn, updateQuery );
			pstmtUpdate.execute();
			logger.info("Update Metering_hour Past data to 'Merged' Success!!");

			pstmtUpdate.close();
			conn.commit();

		} catch (SQLException e) {
			logger.info("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	private void insertMeteringMonth() {
		try {
			logger.info("Insert into Metering_Month Start!!");
			logger.info("Current Time : " + time);

			String insertQuery = "insert into metering.metering_month values (?,?,?,?,?,?,?,?,?,?)"; 
			String selectQuery = "select id, namespace, truncate(sum(cpu)/count(*),2) as cpu, truncate(sum(memory)/count(*),0) as memory, "
					+ "truncate(sum(storage)/count(*),0) as storage, truncate(sum(gpu)/count(*),2) as gpu, "
					+ "truncate(sum(public_ip)/count(*),0) as public_ip, truncate(sum(private_ip)/count(*),0) as private_ip, "
					+ "date_format(metering_time,'%Y-%m-01 00:00:00') as metering_time, status from metering.metering_day where status = 'Success' "
					+ "group by month(metering_time), namespace"; 
			LogPreparedStatement pstmtinsert = new LogPreparedStatement( conn, insertQuery );
			LogPreparedStatement pstmtSelect = new LogPreparedStatement( conn, selectQuery );
			ResultSet rsSelect = pstmtSelect.executeQuery();
			while ( rsSelect.next() ) {
				int i = 1;
				pstmtinsert.setString(i++, UIDGenerator.getInstance().generate32( rsSelect.getString("id"), 16, time ) );
				pstmtinsert.setString(i++, rsSelect.getString("namespace"));
				pstmtinsert.setDouble(i++, rsSelect.getDouble("cpu"));
				pstmtinsert.setLong(i++, rsSelect.getLong("memory"));
				pstmtinsert.setLong(i++, rsSelect.getLong("storage"));
				pstmtinsert.setDouble(i++, rsSelect.getDouble("gpu"));
				pstmtinsert.setInt(i++, rsSelect.getInt("public_ip"));
				pstmtinsert.setInt(i++, rsSelect.getInt("private_ip"));
				pstmtinsert.setTimestamp(i++, rsSelect.getTimestamp("metering_time"));
				pstmtinsert.setString(i++, rsSelect.getString("status"));
				logger.info("pstmtinsert : " + pstmtinsert.getQueryString());
				pstmtinsert.addBatch();							
			}
			try {
				pstmtinsert.executeBatch();
			}catch(SQLException e) {
				logger.info("SQL Exception : " + e.getMessage());
			}
			pstmtinsert.close();
			pstmtSelect.close();
			logger.info("Insert into Metering_Month Success!!");
			
			String updateQuery = "update metering.metering_day set status = 'Merged' where status = 'Success'";
			LogPreparedStatement pstmtUpdate = new LogPreparedStatement( conn, updateQuery );
			pstmtUpdate.execute();	
			pstmtUpdate.close();
			logger.info("Update Metering_day Past data to 'Merged' Success!!");

			conn.commit();

		} catch (SQLException e) {
			logger.info("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	private void insertMeteringYear() {
		try {
			logger.info("Insert into Metering_Year Start!!");
			logger.info("Current Time : " + time);

			String insertQuery = "insert into metering.metering_year values (?,?,?,?,?,?,?,?,?,?)"; 
			String selectQuery = "select id, namespace, truncate(sum(cpu)/count(*),2) as cpu, truncate(sum(memory)/count(*),0) as memory, "
					+ "truncate(sum(storage)/count(*),0) as storage, truncate(sum(gpu)/count(*),2) as gpu, "
					+ "truncate(sum(public_ip)/count(*),0) as public_ip, truncate(sum(private_ip)/count(*),0) as private_ip, "
					+ "date_format(metering_time,'%Y-01-01 %H:00:00') as metering_time, status from metering.metering_month where status = 'Success' "
					+ "group by year(metering_time), namespace"; 
			LogPreparedStatement pstmtinsert = new LogPreparedStatement( conn, insertQuery );
			LogPreparedStatement pstmtSelect = new LogPreparedStatement( conn, selectQuery );
			ResultSet rsSelect = pstmtSelect.executeQuery();
			while ( rsSelect.next() ) {
				int i = 1;
				pstmtinsert.setString(i++, UIDGenerator.getInstance().generate32( rsSelect.getString("id"), 16, time ) );
				pstmtinsert.setString(i++, rsSelect.getString("namespace"));
				pstmtinsert.setDouble(i++, rsSelect.getDouble("cpu"));
				pstmtinsert.setLong(i++, rsSelect.getLong("memory"));
				pstmtinsert.setLong(i++, rsSelect.getLong("storage"));
				pstmtinsert.setDouble(i++, rsSelect.getDouble("gpu"));
				pstmtinsert.setInt(i++, rsSelect.getInt("public_ip"));
				pstmtinsert.setInt(i++, rsSelect.getInt("private_ip"));
				pstmtinsert.setTimestamp(i++, rsSelect.getTimestamp("metering_time"));
				pstmtinsert.setString(i++, rsSelect.getString("status"));
				logger.info("pstmtinsert : " + pstmtinsert.getQueryString());
				pstmtinsert.addBatch();							
			}
			try {
				pstmtinsert.executeBatch();
			}catch(SQLException e) {
				logger.info("SQL Exception : " + e.getMessage());
			}
			pstmtinsert.close();
			pstmtSelect.close();
			logger.info("Insert into Metering_Year Success!!");
			
			String updateQuery = "update metering.metering_month set status = 'Merged' where status = 'Success'";
			LogPreparedStatement pstmtUpdate = new LogPreparedStatement( conn, updateQuery );
			pstmtUpdate.execute();
			pstmtUpdate.close();
			logger.info("Update Metering_month Past data to 'Merged' Success!!");

			conn.commit();

		} catch (SQLException e) {
			logger.info("SQL Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}
}
