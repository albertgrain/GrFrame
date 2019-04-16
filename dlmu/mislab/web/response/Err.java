package dlmu.mislab.web.response;

import java.util.List;

import dlmu.mislab.common.DictBase;
import dlmu.mislab.common.KeyValuePair;
import dlmu.mislab.common.LogicError;
import dlmu.mislab.web.MappingError;
import dlmu.mislab.web.ServerError;
import dlmu.mislab.web.WebLogicError;

/**
 * 执行过程中发生可以预计的业务逻辑错误时信息载体类，不可继承。强烈建议直接采用LogicError对象的单例，不要自定义错误。
 * @author Gurui
 *
 */

/**
 * 
 * @author GuRui
 *
 */
public class Err extends Response {
	private LogicError err=null;
	public static Err LOGIN_REQUIRED=Err.getInstance(WebLogicError.BIZ_LOGIN_REQUIRED);
	public static Err METHOD_GET_NOT_SUPPORTED=Err.getInstance(WebLogicError.SERVER_ERROR_GET_METHOD_NOT_SUPPORTED);
	public static Err METHOD_POST_NOT_SUPPORTED=Err.getInstance(WebLogicError.SERVER_ERROR_POST_METHOD_NOT_SUPPORTED);
	public static Err METHOD_GET_NOT_IMPLEMENTED=Err.getInstance(WebLogicError.SERVER_ERROR_GET_METHOD_NOT_IMPLEMENTED);
	public static Err METHOD_POST_NOT_IMPLEMENTED=Err.getInstance(WebLogicError.SERVER_ERROR_POST_METHOD_NOT_IMPLEMENTED);
	
	
	public String getMsg(){
		if(this.err==null){
			return "";
		}
		return this.err.getMsg();
	}
	
	@Override
	public String toString(){
		return this.getMsg();
	}
	
	public static Err PARAMETER_WRONG(String msg){
		return new Err(msg);
	}
	
	public static Err getInstance(LogicError err){
		Err rtn=new Err();
		rtn.setErr(err);
		return rtn;
	}
	
	public static Err MAPPING_ERROR(List<KeyValuePair> badPairs){
		return MAPPING_ERROR(DictBase.HTPP_PARAMETERS_PARSING_FAILED, badPairs);
	}
	
	public static Err MAPPING_ERROR(String msg, List<KeyValuePair> badPairs){
		WebLogicError err= new MappingError(msg,badPairs);
		return Err.getInstance(err);
	}
	
	public static Err RELATION_ERROR(String msg){
		return new Err(msg);
	}
	
//	@Deprecated
//	public static Err newServerError(String msg){
//		return Err.SERVER_ERROR(msg);
//	}
	
	public static Err SERVER_ERROR(String msg){
		WebLogicError err= new ServerError(msg);
		return Err.getInstance(err);
	}

	public Err() {
		super(false);
	}

	public Err(String msg){
		super(false);
		this.err=new WebLogicError(LogicError.CODE_BIZ_LOGIC_ERROR,msg);
	}

	public Err(LogicError err){
		super(false);
		this.setErr(err);
	}

	public LogicError getErr() {
		return err;
	}

	public void setErr(LogicError err) {
		this.err = err;
	}
}