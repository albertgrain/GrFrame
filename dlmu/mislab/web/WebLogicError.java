package dlmu.mislab.web;

import dlmu.mislab.common.LogicError;

/*
 * 不可预料的非业务逻辑错误。错误代码为-1表示错误的由系统抛出的异常直接转化而来，这不是常态。
 * 字段验证（但控件实体）验证错误代码全部为1
 * 常态错误代码应大于0
 * 不建议使用用户自定义错误，如必须，则错误代码应大于一万(10000)
 * 错误提示内容应存放与dc类中
 */
public class WebLogicError extends LogicError {
	private static final long serialVersionUID = 1L;

	public static final int CODE_PLEASE_LOGIN = 5555;
	public static final int CODE_JSON_FIELD_FORMAT_WRONG = 5601;

	public static final LogicError BIZ_LOGIC_ERROR=new LogicError(CODE_BIZ_LOGIC_ERROR,"业务逻辑错误");
	
	public static final LogicError BIZ_LOGIN_REQUIRED=new LogicError(CODE_PLEASE_LOGIN,"请先登录");
	public static final LogicError JSON_FIELD_FORMAT_WRONG=new LogicError(CODE_JSON_FIELD_FORMAT_WRONG,"JSON字段格式错误");
	
	public static final LogicError SERVER_ERROR_GET_METHOD_NOT_SUPPORTED=new LogicError(CODE_SERVER_ERROR,"服务器不支持对此资源的GET方法");
	public static final LogicError SERVER_ERROR_POST_METHOD_NOT_SUPPORTED=new LogicError(CODE_SERVER_ERROR,"服务器不支持对此资源的POST方法");
	public static final LogicError SERVER_ERROR_GET_METHOD_NOT_IMPLEMENTED=new LogicError(CODE_SERVER_ERROR,"服务器尚未实现对此资源的GET方法");
	public static final LogicError SERVER_ERROR_POST_METHOD_NOT_IMPLEMENTED=new LogicError(CODE_SERVER_ERROR,"服务器不尚未实现对此资源的POST方法");
	
	//////////////////////////////////////////////////////////
	public WebLogicError(String errMsg){
		super(CODE_BIZ_LOGIC_ERROR,errMsg);
	}

	public WebLogicError(int errCode, String errMsg){
		super(errCode, errMsg);
	}
	public WebLogicError setExplain(String exp){
		this.setMsg(exp);
		return this;
	}
}
