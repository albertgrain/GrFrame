package dlmu.mislab.validation;

import dlmu.mislab.common.LogicError;

/**
 * 此类为所有字符串类型字段验证类的基类
 * 已经完成对字符串长度的验证，因此只需在子类中设定长度上下限即可
 * By GuRui on 2014-12-4 上午12:17:46
 *
 */
public abstract class FieldString extends FieldValidationBase {

	private int maxLength=Integer.MAX_VALUE;
	private int minLength=0;


	public FieldString(String val) {
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
		return this.validateStringLength((String)this.val,this.minLength, this.maxLength);
	}

	protected LogicError validateStringLength(String str,int minLength, int maxLength){
		if(str.length()>maxLength || str.length()<minLength){
			StringBuilder buf= new StringBuilder();
			buf.append(this.getFieldNameCnBracket())
			.append("长度校验失败。长度范围：")
			.append(minLength).append("-").append(maxLength)
			.append("个半角字符");
			return new ValidationError(this.getFieldName(),buf.toString());
		}
		return null; //Validation passed
	}

	public int getMaxLength() {
		return this.maxLength;
	}


	public void setMaxLength(int maxLength) {
		if(maxLength<this.getMinLength()){
			throw new ValidationException("设定字符串类型字段最大长度时发生异常：最大长度不可小于最小长度");
		}
		this.maxLength = maxLength;
	}


	public int getMinLength() {
		return this.minLength;
	}


	public void setMinLength(int minLength) {
		if(minLength<0){
			throw new ValidationException("设定字符串类型字段最小长度时发生异常：最小长度不可小于0");
		}
		if(minLength>this.getMaxLength()){
			throw new ValidationException("设定字符串类型字段最小长度时发生异常：最小长度不可大于最大长度");
		}
		this.minLength = minLength;
	}

	@Override
	protected String getVal(){
		if(this.val==null){
			return null;
		}
		return (String)this.val;
	}

}
