package dlmu.mislab.common;

import java.io.File;

public abstract class ConfigBase {
	public static int MAX_AFFECTED_ROW_BY_ONE_TRANSACTION=50;
	public static boolean logSQL=false; // Whether log all SQL to console or log file.
	
	public static String FIELD_VALIDATION_PACKAGE_NAME = null;
	public static String PAGE_LOGIN = "/login.jsp";
	public static String PAGE_LOGOUT = "/logout";
	public static String PAGE_ERROR = "/err.jsp";
	public static final int MAX_HTTP_POST_SIZE=2000 * 1024; //2M in bytes
	public static final String DEFAULT_ENCODING=Config.CHARSET_ON_CLIENT;

	public static final char PATH_DELIMETER = File.separatorChar;

	public static final String PACKAGE_VALIDATION_PARAM = "dlmu.fileupload.validation.param";
	public static final String PACKAGE_VALIDATION_FIELD = "dlmu.fileupload.validation.field";
	public static final String VALIDATION_ERROR_TAG = "ERROR";

	public static final String SERVLET_ENCODING = "utf-8";
	public static final String CONTENT_TYPE_JSON = "application/json";
	//public static final String CONTENT_TYPE_JSON = "text/plain";

	public static final String DEFAULT_CONFIG_FOLDER = "docshare";
	public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
	public static final String DEFAULT_DATETIME_PATTERN = DEFAULT_DATE_PATTERN
			+ " " + DEFAULT_TIME_PATTERN;

	// ////////// WRITABLE PROPERTIES ////////////
	public static String PROJECT_NAME=null;
	

}
