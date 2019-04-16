package dlmu.mislab.validation;

import dlmu.mislab.common.LogicError;

public class FieldBool extends FieldValidationBase{

	public FieldBool(Boolean val) {
		super(val);
	}

	@Override
	public LogicError validate(){
		if(this.val==null){
			if(!this.isNullable()){
				return new ValidationError(this.getFieldName(),this.getFieldNameCnBracket()+"不可为空");
			}else{
				return null;
			}
		}
		return null;
	}

	@Override
	protected Boolean getVal(){
		if(this.val==null){
			return null;
		}
		return (Boolean)this.val;
	}
}
