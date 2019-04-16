// By GuRui on 2015-1-17 上午3:50:49
package dlmu.mislab.config;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import dlmu.mislab.common.Config;

public class Log4JConfigure {
	private static final String MAX_LOG_FILE_SIZE= "4096KB";
	private static final int MAX_BACKUP_INDEX = 256;
	private static boolean INITIALIZED=false;
	
	private static final String LOG_FILE_NAME="grlog.log";

	public static void init(){
		if(INITIALIZED){
			return;
		}

		if(Config.IS_DEBUG){
			BasicConfigurator.configure();
		}else{
			PatternLayout layout=new PatternLayout();
			//2015-01-24 DEBUG-org.apache.ibatis.transaction.jdbc.JdbcTransaction:
			layout.setConversionPattern("[%p] %d{yyyy-MM-dd HH:mm:ss} %c %M - %m%n");
			RollingFileAppender appender=null;
			try {
				appender=new RollingFileAppender(layout, LOG_FILE_NAME, true);
				appender.setMaxFileSize(MAX_LOG_FILE_SIZE);
				appender.setMaxBackupIndex(MAX_BACKUP_INDEX);
				appender.setThreshold(Level.INFO);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(appender==null){
				BasicConfigurator.configure();
			}else{
				BasicConfigurator.configure(appender);
			}
		}
		
		INITIALIZED=true;
	}
}
