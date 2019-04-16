// By GuRui on 2015-5-21 上午12:19:45
package dlmu.mislab.config;

import java.sql.Connection;
import java.sql.SQLException;
//import java.sql.Statement;

//import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlmu.mislab.config.IConnectionPool;
import dlmu.mislab.config.Log4JConfigure;


public class OracleConnectionPool implements IConnectionPool {
	private static Logger logger;
	public static final OracleConnectionPool INSTANCE=new OracleConnectionPool();
	static{
		Log4JConfigure.init();
		logger = LoggerFactory.getLogger("ConnectionPool");
	}

	private OracleConnectionPool(){}
	private PoolDataSource datasource=null;
	private ConnParameters params=null;
	
	@Override
	public Connection getConnection(String confName){
		throw new RuntimeException("TODO");
	}
	
	@Override
	public void setParameters(ConnParameters params) {
		this.params=params;
	}
	
	@Override
	public Connection getConnection() {
		if(this.datasource==null){
			try {
				this.datasource = PoolDataSourceFactory.getPoolDataSource();
				this.datasource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
				
				this.datasource.setURL(params.getConnUrl());    
				this.datasource.setUser(params.getConnUser());    
				this.datasource.setPassword(params.getConnPassword());
			
				this.datasource.setInitialPoolSize(3);
				this.datasource.setMinPoolSize(3);
				this.datasource.setMaxPoolSize(10);
				return this.datasource.getConnection();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				return null;
			}
		}else{
			try {
				return this.datasource.getConnection();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				return null;
			}
		}
	}

}
