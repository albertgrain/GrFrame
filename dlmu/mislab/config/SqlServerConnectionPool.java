// By GuRui on 2014-12-7 下午5:29:41
package dlmu.mislab.config;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;

import dlmu.mislab.config.Log4JConfigure;

public class SqlServerConnectionPool implements IConnectionPool{
	private static Logger logger;
	public static final SqlServerConnectionPool INSTANCE=new SqlServerConnectionPool();
	static{
		Log4JConfigure.init();
		logger = LoggerFactory.getLogger("ConnectionPool");
	}

	private SqlServerConnectionPool(){}

	private SQLServerConnectionPoolDataSource datasource=null;
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
	public Connection getConnection(){
		
		if(this.datasource==null){	
			this.datasource = new SQLServerConnectionPoolDataSource();
			this.datasource.setURL(params.getConnUrl());
			this.datasource.setPassword(params.getConnPassword());
			this.datasource.setUser(params.getConnUser());
		}
		try {
			return this.datasource.getConnection();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

}
