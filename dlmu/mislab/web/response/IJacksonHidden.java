// By GuRui on 2018-1-17 下午4:33:22
package dlmu.mislab.web.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 避免Jackson序列化RuntimeException的内容
 * By GuRui on 2018-1-17 下午4:42:59
 *
 */
@JsonIgnoreProperties({"stackTrace", "cause", "localizedMessage", "message", "suppressed"})
public interface IJacksonHidden {
	StackTraceElement[] getStackTrace();
	Throwable getCause();
	String getLocalizedMessage();
	String getMessage();
	Throwable[] getSuppressed();
}
