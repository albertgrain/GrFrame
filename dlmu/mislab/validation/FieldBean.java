package dlmu.mislab.validation;

import dlmu.mislab.common.LogicError;
import dlmu.mislab.web.interact.IParameter;
import dlmu.mislab.web.response.Err;

public abstract class FieldBean<T extends IParameter> extends FieldValidationBase{

	public FieldBean(T val) {
		super(val);
	}

	@Override
	public LogicError validate() {
		Err err=Validator.validateBeanStatic(this.getVal());
		if(err!=null){
			return new LogicError(LogicError.CODE_VALIDATION_ERROR, err.getMsg());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T getVal() {
		return (T)this.val;
	}
}
