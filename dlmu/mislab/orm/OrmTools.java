// By GuRui on 2015-12-5 下午11:04:57
package dlmu.mislab.orm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;

import dlmu.mislab.common.Config;
/***
 * 辅助记录Dao执行信息
 * 1. 根据是否是debug状态决定写入控制台还是log文件
 * 2. 帮助将SQL命令和参数格式化好并输出
 * 注意，所有的log都是写到error里的
 * @author GuRui
 *
 */
public class OrmTools {
	static void error(Logger logger, String msg){
		if(logger==null || Config.IS_DEBUG){
			System.out.println(msg);
		}else{
			logger.debug(msg);
		}
	}
	
	static void error(Logger logger, OrmCommand cmd, String msg){
		if(cmd!=null){
			OrmTools.error(logger, msg, cmd.sql, cmd.params);
		}else{
			OrmTools.error(logger, msg);
		}
	}
	
	static void error(Logger logger, String sql, Object... parameters){
		OrmTools.error(logger, "", sql, parameters);
	}
	
	static void error(Logger logger,String msg,  String sql, Object... parameters){
		OrmTools.doLog(true, logger, msg, sql, parameters);
	}
	
	static void log(Logger logger, OrmCommand cmd){
		if(logger==null || cmd ==null){
			return;
		}
		OrmTools.log(logger, cmd.sql, cmd.params);
	}
	
	static void log(Logger logger, String sql, Object... parameters){
		OrmTools.doLog(false, logger, "", sql, parameters);
	}
	
	
	private static void doLog(boolean isErrorMode, Logger logger,String msg,  String sql, Object... parameters){
		StringBuilder buf=new StringBuilder();
		buf.append(msg).append("\n");
		buf.append("SQL:").append(sql).append("\n");
		buf.append("PMS:");
		if(parameters!=null){
			for(Object obj : parameters){
				if(obj==null){
					buf.append("[null],");
				}else{
					buf.append("[").append(obj.toString()).append("] ");
				}
			}
		}
		buf.append("\n---------------------------------\n");

		if(logger==null || Config.IS_DEBUG){
			System.out.println(buf.toString());
		}else{
			if(isErrorMode){
				logger.error(buf.toString());
			}else{
				logger.debug(buf.toString());
			}
		}
	}
	
	static void populateParameters(PreparedStatement stmt, Object... parameters) throws SQLException{
		if(parameters!=null && parameters.length>0){
			int i=1;
			for(Object obj:parameters){
//				if(obj instanceof IValidationDate){
//					//stmt.setDate(i, ((IValidationDate)obj).toSqlDate());
//					stmt.setTimestamp(i, new java.sql.Timestamp(((IValidationDate)obj).getTime()));
//				}else{
					stmt.setObject(i, obj);
//				}
				i++;
			}
		}
	}
			
}
