package dlmu.mislab.web.response;

import dlmu.mislab.web.WebLogicError;



/***
 * 返回用户可以理解的逻辑错误信息
 * @author GuRui
 *
 */
@Deprecated
public class BizLogicError extends Err {
	public BizLogicError(String msg){
		this.setErr(WebLogicError.BIZ_LOGIC_ERROR.setExplain(msg));
	}
}
