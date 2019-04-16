package dlmu.mislab.common;

import dlmu.mislab.web.response.IJacksonHidden;

/*
 * 不可预料的非业务逻辑错误。错误代码为-1表示错误的由系统抛出的异常直接转化而来，这不是常态。
 * 字段验证（但控件实体）验证错误代码全部为1
 * 常态错误代码应大于0
 * 不建议使用用户自定义错误，如必须，则错误代码应大于一万(10000)
 * 错误提示内容应存放与dc类中
 */
public class LogicError extends RuntimeException implements IJson, IJacksonHidden { //IJacksonHidden:避免cause和stackTrace被序列化
	private static final long serialVersionUID = 1L;
	
	public static final int CODE_SYSTEM_ERROR=-1;
	public static final int CODE_VALIDATION_ERROR=1; //字段验证的错误代码全部为1
	public static final int CODE_SERVER_ERROR=100; //服务器端错误，给程序员用，不可直接返回客户
	public static final int CODE_BIZ_LOGIC_ERROR=5000; //字段验证的错误代码全部为1
	public static final int CODE_USER_DEFINED_ERROR=10000;  //用户自定义错误错误代码应大于一万(不建议使用)
	//////////////////////////////////////////////////////////
	private int code=0;
	private String msg=null;
	
	public LogicError(String errMsg){
		this(CODE_BIZ_LOGIC_ERROR,errMsg);
	}

	public LogicError(int errCode, String errMsg){
		super(errMsg);
		this.code=errCode;
		this.msg=errMsg;
	}

	public LogicError(String msg, Throwable exp){
		super(msg,exp);
		this.code=-1;
		this.msg=msg;
	}

	public LogicError setExplain(String exp){
		this.setMsg(exp);
		return this;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
