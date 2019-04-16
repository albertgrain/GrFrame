package dlmu.mislab.validation;

import dlmu.mislab.common.DictBase;

public abstract class FieldValidationBase implements IValidation,IProperty {
	private boolean isNullable=true;

	protected Object val;

	protected abstract Object getVal();

	public FieldValidationBase(Object val){
		this.val=val;
	}

	protected String getFieldName(){
		String name=this.getClass().getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}

	private String getFieldNameCn(){
		return DictBase.getFieldNameCn(this.getFieldName());
	}

	protected String getFieldNameCnBracket(){
		return "["+this.getFieldNameCn()+"]";
	}

	@Override
	public boolean isNullable() {
		return isNullable;
	}

	@Override
	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}
}
