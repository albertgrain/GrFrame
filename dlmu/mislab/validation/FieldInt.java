package dlmu.mislab.validation;

import dlmu.mislab.common.LogicError;


/**
 * 此类为所有整形字段验证类的基类
 * 已经完成对整数范围验证，因此只需在子类中设定上下限即可
 * By GuRui on 2014-12-4 上午1:14:23
 *
 */
public abstract class FieldInt extends FieldValidationBase{
	private int max=Integer.MAX_VALUE;
	private int min=Integer.MIN_VALUE;

	public FieldInt(Integer val){
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
		return this.validateInteger((Integer)this.val,this.getMin(), this.getMax());
	}

	protected LogicError validateInteger(Integer aInt,int min, int max){
		if(aInt>max || aInt<min){
			StringBuilder buf= new StringBuilder();
			buf.append(this.getFieldNameCnBracket())
			.append("取值范围校验失败。取值范围：")
			.append(min).append("-").append(max);
			return new ValidationError(this.getFieldName(),buf.toString());
		}
		return null; //Validation passed
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		if(max<this.getMin()){
			throw new ValidationException("设定最大值时发生异常：最大值不可小于最小值");
		}
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		if(min>this.getMax()){
			throw new ValidationException("设定最小值时发生异常：最小值不可大于最大值");
		}
		this.min = min;
	}

	@Override
	protected Integer getVal(){
		if(this.val==null){
			return null;
		}
		return (Integer)this.val;
	}
}
