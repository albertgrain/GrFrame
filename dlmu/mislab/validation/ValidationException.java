// By GuRui on 2014-11-28 上午5:41:33
package dlmu.mislab.validation;

import dlmu.mislab.common.DictBase;
import dlmu.mislab.common.LogicError;

/**
 * 验证反射中发生的逻辑及运行时异常
 * 请注意和字段验证错误(ValidationError相区分)
 * 不可将此异常抛给用户
 * By GuRui on 2014-11-28 上午5:44:04
 *
 */
public class ValidationException extends LogicError {
	private static final long serialVersionUID = 1L;

	public ValidationException(){
		super(DictBase.VALIDATOR_CONSTRUCTING_ERROR_MSG);
	}

	public ValidationException(String msg){
		super(msg);
	}

	public ValidationException(Throwable cause){
		super(DictBase.VALIDATOR_CONSTRUCTING_ERROR_MSG,cause);
	}

	public ValidationException(String msg, Throwable cause) {
		super(msg,cause);
	}
}
