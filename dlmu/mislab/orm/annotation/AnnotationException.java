// By GuRui on 2014-12-6 下午12:22:53
package dlmu.mislab.orm.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	protected static Logger logger=LoggerFactory.getLogger(AnnotationException.class);
		
	public AnnotationException(String msg) {
		super(msg);
		logger.error(this.getMessage());
	}
}