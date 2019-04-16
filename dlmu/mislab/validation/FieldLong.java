// By GuRui on 2015-2-17 下午6:11:41
package dlmu.mislab.validation;

import dlmu.mislab.common.LogicError;

public abstract class FieldLong extends FieldValidationBase{
	private long max=Integer.MAX_VALUE;
	private long min=Integer.MIN_VALUE;
	
	public FieldLong(Long val) {
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
		return this.validateLong((Long)this.val,this.getMin(), this.getMax());
	}

	protected LogicError validateLong(Long aLong,long min, long max){
		if(aLong>max || aLong<min){
			StringBuilder buf= new StringBuilder();
			buf.append(this.getFieldNameCnBracket())
			.append("取值范围校验失败。取值范围：")
			.append(min).append("-").append(max);
			return new ValidationError(this.getFieldName(),buf.toString());
		}
		return null; //Validation passed
	}

	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		if(max<this.getMin()){
			throw new ValidationException("设定最大值时发生异常：最大值不可小于最小值");
		}
		this.max = max;
	}

	public long getMin() {
		return min;
	}

	public void setMin(long min) {
		if(min>this.getMax()){
			throw new ValidationException("设定最小值时发生异常：最小值不可大于最大值");
		}
		this.min = min;
	}

	@Override
	protected Long getVal(){
		if(this.val==null){
			return null;
		}
		return (Long)this.val;
	}
}
