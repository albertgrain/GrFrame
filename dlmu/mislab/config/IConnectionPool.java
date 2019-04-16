// By GuRui on 2015-5-20 下午2:41:54
package dlmu.mislab.config;

import java.sql.Connection;

public interface IConnectionPool {
	//The following two methods are used to automatically load and use default connection.
	public Connection getConnection();
	public void setParameters(ConnParameters params);
	
	//The following method is used to manually load specific connection. (For example, connection specific for admin)
	public Connection getConnection(String confName);
}


class ConnParameters{
	private String connUrl;
	private String connUser;
	private String connPassword;
	
	public ConnParameters(String connUrl, String connUser, String connPassword){
		this.connUrl=connUrl;
		this.connUser=connUser;
		this.connPassword=connPassword;
	}

	public String getConnUrl() {
		return connUrl;
	}

	public String getConnUser() {
		return connUser;
	}

	public String getConnPassword() {
		return connPassword;
	}
}