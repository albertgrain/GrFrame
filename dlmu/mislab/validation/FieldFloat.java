package dlmu.mislab.validation;

import dlmu.mislab.common.LogicError;


public abstract class FieldFloat extends FieldValidationBase{

	private Double min=0.0;
	private Double max=Double.MAX_VALUE;

	public FieldFloat(Double val){
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
		return this.validateFloat((Double)this.val,this.getMin(), this.getMax());
	}

	protected LogicError validateFloat(Double dbl,Double min, Double max){
		if(dbl>max || dbl<min){
			StringBuilder buf= new StringBuilder();
			buf.append(this.getFieldNameCnBracket())
			.append("取值范围校验失败。取值范围：")
			.append(min).append("-").append(max);
			return new ValidationError(this.getFieldName(),buf.toString());
		}
		return null; //Validation passed
	}


	public Double getMin() {
		return this.min;
	}

	public void setMin(Double min) {
		if(min>this.getMax()){
			throw new ValidationException("设定最小值时发生异常：最小值不可大于最大值");
		}
		this.min = min;
	}

	public Double getMax() {
		return this.max;
	}

	public void setMax(Double max) {
		if(max<this.getMin()){
			throw new ValidationException("设定最大值时发生异常：最大值不可小于最小值");
		}
		this.max = max;
	}

	@Override
	protected Double getVal(){
		if(this.val==null){
			return null;
		}
		return (Double)this.val;
	}

}
