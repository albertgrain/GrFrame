// By GuRui on 2015-5-21 上午12:08:07
package dlmu.mislab.config;

import java.util.Properties;

import dlmu.mislab.common.Config;
import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.orm.OrmException;
import dlmu.mislab.tool.Base64Coder;
import dlmu.mislab.tool.DateTool;
import dlmu.mislab.tool.PropertyProxy;
import dlmu.mislab.tool.Str;

public class DbConfigure {
	private static boolean INITIALIZED=false;
	private String DbConnUrl=null;
	private String username=null;
	private String password=null;

	private boolean readConfig(String projectName){
		if(Str.isNullOrEmpty(projectName)){
			return false;
		}

		PropertyProxy pp=new PropertyProxy();
		Properties props=pp.load(Config.DEFAULT_CONFIG_FOLDER + projectName + Config.CONFIG_FILE_EXT);

		this.DbConnUrl=props.getProperty("DbConnUrl","jdbc:mysql://127.0.0.1:3306/"+projectName);
		this.username=props.getProperty("username","mis2");
		this.password=Base64Coder.simpleDecode(props.getProperty("password", "VktNSlpAAG8F")); //123
		ConfigBase.logSQL= Boolean.parseBoolean(props.getProperty("logSQL","false"));
		DateTool.PATTERN_DEFAULT = props.getProperty("datePattern",DateTool.PATTERN_FULL_DATE_TIME);
		
		if(Str.isNullOrEmpty(this.DbConnUrl) || Str.isNullOrEmpty(this.username) || Str.isNullOrEmpty(this.password)){
			return false;
		}
		ConfigBase.PROJECT_NAME=projectName;
		return true;
	}
	
	private void parseDbTypeFromDbUrl(String sUrl){
		INITIALIZED=false;
		if(sUrl.toLowerCase().indexOf("mysql")>0){
			DbConnection.pool=MySqlConnectionPool.INSTANCE;
		}else if(sUrl.toLowerCase().indexOf("oracle")>0){
			DbConnection.pool=OracleConnectionPool.INSTANCE;
		}else if(sUrl.toLowerCase().indexOf("sqlserver")>0){
			DbConnection.pool=SqlServerConnectionPool.INSTANCE;
		}else{
			throw new OrmException("未知数据库类型:"+ sUrl);
		}
		
	}

	/***
	 * Load database configuration according to appName (/docshare/conf/[appName].cfg)
	 * Then initialize DbConnection.pool
	 * This method should be called automatically during system initiation
	 * @param appName
	 */
	public static void init(String appName){
		if(INITIALIZED){
			return;
		}
		DbConfigure helper= DbConfigure.load(appName);
		ConnParameters params=new ConnParameters(helper.DbConnUrl, helper.username, helper.password);
		DbConnection.pool.setParameters(params);
		
		INITIALIZED=true;
	}
	
	/***
	 *  Load database configuration according to confName (/docshare/conf/[confName].cfg)
	 *  This method may be called manually at any time
	 * @param confName
	 * @return
	 */
	static DbConfigure load(String confName){
		DbConfigure rtn=new DbConfigure();
		if(!rtn.readConfig(confName)){
			throw new RuntimeException("Failed to read configuration file ["+confName+"].cfg System halt.");
		}
		rtn.readConfig(confName);
		rtn.parseDbTypeFromDbUrl(rtn.DbConnUrl);
			
		return rtn;
	}

	public String getDbConnUrl() {
		return DbConnUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}	
}
