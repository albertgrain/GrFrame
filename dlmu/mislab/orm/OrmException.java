// By GuRui on 2014-12-6 下午12:22:53
package dlmu.mislab.orm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OrmException extends RuntimeException {
	private static final long serialVersionUID = 2441133667903276696L;
	
	protected static Logger logger=LoggerFactory.getLogger(OrmException.class);
	
	public OrmException(String msg){
		this(msg, null);
	}
	
	public OrmException(String msg, OrmCommand cmd) {
		super(msg);
		logger.error(this.getMessage());
		if(cmd!=null){
			OrmTools.error(OrmException.logger, this.getMessage(), cmd.sql, cmd.params);
		}
	}
	
	public OrmException(Throwable e){
		super(e);
		logger.error(e.getMessage());
	}
}