// By GuRui on 2014-12-7 下午5:29:41
package dlmu.mislab.config;

import java.sql.Connection;
import java.sql.SQLException;
//import java.sql.Statement;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlmu.mislab.config.IConnectionPool;
import dlmu.mislab.config.Log4JConfigure;

public class MySqlConnectionPool implements IConnectionPool{
	private static Logger logger;
	public static final MySqlConnectionPool INSTANCE=new MySqlConnectionPool();
	static{
		Log4JConfigure.init();
		logger = LoggerFactory.getLogger("ConnectionPool");
	}
	
	
	private MySqlConnectionPool(){}
	private DataSource datasource=null;
	private ConnParameters params=null;
	
	@Override
	public void setParameters(ConnParameters params) {
		this.params=params;
	}

	@Override
	public Connection getConnection(){
		return this.getConnection(params);
	}
	
	@Override
	public Connection getConnection(String confName){
		DbConfigure cfg= DbConfigure.load(confName);
		ConnParameters params=new ConnParameters(cfg.getDbConnUrl(), cfg.getUsername(), cfg.getPassword());
		return this.getConnection(params);
	}
	
	private Connection getConnection(ConnParameters params){
		
		if(this.datasource==null){
			if(params==null){
				logger.error("没有发现连接参数，无法初始化数据库连接");
				return null;
			}
			PoolProperties p = new PoolProperties();
			//Set parameter
			p.setUrl(params.getConnUrl());
			p.setDriverClassName("com.mysql.jdbc.Driver");
			p.setUsername(params.getConnUser());
			p.setPassword(params.getConnPassword());
			//Config
			p.setDefaultAutoCommit(false);
			p.setJmxEnabled(true);
			p.setTestWhileIdle(false);
			p.setTestOnBorrow(true);
			p.setValidationQuery("SELECT 1");
			p.setTestOnReturn(false);
			p.setValidationInterval(30000);
			p.setTimeBetweenEvictionRunsMillis(30000);
			p.setMaxActive(100);
			p.setInitialSize(10);
			p.setMaxWait(10000);
			p.setRemoveAbandonedTimeout(60);
			p.setMinEvictableIdleTimeMillis(30000);
			p.setMinIdle(10);
			p.setLogAbandoned(true);
			p.setRemoveAbandoned(true);
			p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
					+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
			//不太好用
//			java.util.Properties extraProps=new java.util.Properties();
//			extraProps.put("charset", "UTF8"); //似乎必须是小写utf8 
//			p.setDbProperties(extraProps);
			
			this.datasource = new DataSource();
			this.datasource.setPoolProperties(p);
		}
		try {
			return this.datasource.getConnection();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
}
