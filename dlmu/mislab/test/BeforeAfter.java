package dlmu.mislab.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.config.BeanUtilsConfigure;
import dlmu.mislab.config.DbConfigure;
import dlmu.mislab.config.IConnectionPool;
import dlmu.mislab.config.Log4JConfigure;
import dlmu.mislab.config.MySqlConnectionPool;
import dlmu.mislab.orm.OrmTrans;
import dlmu.mislab.tool.Str;

public class BeforeAfter{
	private static Logger logger =LoggerFactory.getLogger(BeforeAfter.class);
	

	public BeforeAfter(String projectName){
		this(projectName,null);
	}
	
	public BeforeAfter(String projectName, String validationPackage){
		if(Str.isNullOrEmpty(projectName)){
			throw new RuntimeException("项目名称不可为空");
		}
		Log4JConfigure.init();
		BeanUtilsConfigure.init();
		DbConfigure.init(projectName);
		ConfigBase.FIELD_VALIDATION_PACKAGE_NAME = validationPackage;
	}

	public boolean execNonQuery(String... sqls){
		boolean rtn=true;
		IConnectionPool pool = MySqlConnectionPool.INSTANCE;
		Connection conn=pool.getConnection();

		try {
			Statement stmt = conn.createStatement();
			for(String sql : sqls){
				boolean rst = stmt.execute(sql);
				if(rst){ //rst=true意味着是一个查询操作，必需出错
					logger.error("execNonQuery不可执行查询命令");
					rtn=false;
					break;
				}
			}
			stmt.close();
			conn.commit();
			return rtn;
		} catch (SQLException e) {
			logger.error("创建预备表失败(请检查当跟前用户的create权限):"+e.getMessage());
			return false;
		}finally{
			try{
				conn.close();
			}catch(Throwable e){}
		}
	}
	/***
	 * 批量执行sql命令，常用于批量插入多行，尽量不要用于批量更新和删除。【请谨慎使用！！！】
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean execBatch(String sql){
		OrmTrans trans=new OrmTrans();
		return trans.tryExceuteBatch(sql);
	}

}
