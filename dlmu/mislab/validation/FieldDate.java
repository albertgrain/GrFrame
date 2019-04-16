package dlmu.mislab.validation;

import java.text.ParseException;
import java.util.Date;

import dlmu.mislab.common.LogicError;
import dlmu.mislab.tool.DateTool;

public abstract class FieldDate extends FieldValidationBase{
	private Date min;
	private Date max;

	public FieldDate(Date val) {
		super(val);
		this.min=DateTool.createDate(1800, 1, 1, 0, 0, 0);
		this.max=DateTool.createDate(3000, 1, 1, 0, 0, 0);
	}

	public Date getMin() {
		return min;
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
		return this.validateDate((Date)this.val,this.getMin(), this.getMax());
	}

	protected LogicError validateDate(Date dt,Date min, Date max){
		if(dt.getTime()>max.getTime() || dt.getTime()<min.getTime()){
			StringBuilder buf= new StringBuilder();
			buf.append(this.getFieldNameCnBracket())
			.append("取值范围校验失败。取值范围：")
			.append(min).append("-").append(max);
			return new ValidationError(this.getFieldName(),buf.toString());
		}
		return null; //Validation passed
	}

	/**
	 * 根据字符串设置日期最小值，格式"yyyy-MM-dd"
	 * By GuRui on 2014-12-4 上午1:09:56
	 * @param sDate
	 */
	public void setMin(String sDate){
		try {
			this.setMin(DateTool.createDate(sDate));
		} catch (ParseException e) {
			throw new ValidationException("日期范围设定时发现日期格式错误",e);
		}
	}

	/**
	 * 设置一个只包含时间部分的Date对象
	 * By GuRui on 2014-12-4 上午12:58:27
	 * @param hour
	 * @param minute
	 * @param second
	 * @param setTimePartFlag 标识位，随便赋值
	 */
	public void setMin(int hour, int minute, int second, boolean setTimePartFlag){
		Date tmp=DateTool.createDate(1900, 1, 1, hour, minute, second);
		long tp=DateTool.getTimePortion(tmp);
		this.setMin(new Date(tp));
	}

	/**
	 * 设置一个只包含日期部分的Date对象
	 * By GuRui on 2014-12-4 上午12:59:45
	 * @param year
	 * @param day
	 * @param month
	 */
	public void setMin(int year, int day, int month){
		this.setMin(DateTool.createDate(year, month, day, 0, 0, 0));
	}

	/**
	 * 设置一个由年月日时分秒构造的Date对象
	 * By GuRui on 2014-12-4 上午1:00:04
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public void setMin(int year, int month, int day, int hour, int minute, int second){
		this.setMin(DateTool.createDate(year, month, day, hour, minute, second));
	}

	public void setMin(Date min) {
		if(min.getTime()>this.getMax().getTime()){
			throw new ValidationException("设定最小值时发生异常：最小值不可大于最大值");
		}
		this.min = min;
	}

	public Date getMax() {
		return max;
	}

	/**
	 * 根据字符串设置日期最大值，格式"yyyy-MM-dd"
	 * By GuRui on 2014-12-4 上午1:09:56
	 * @param sDate
	 */
	public void setMax(String sDate){
		try {
			this.setMax(DateTool.createDate(sDate));
		} catch (ParseException e) {
			throw new ValidationException("日期范围设定时发现日期格式错误",e);
		}
	}

	/**
	 * 设置一个只包含时间部分的Date对象
	 * By GuRui on 2014-12-4 上午12:58:27
	 * @param hour
	 * @param minute
	 * @param second
	 * @param setTimePartFlag 标识位，随便赋值
	 */
	public void setMax(int hour, int minute, int second, boolean setTimePartFlag){
		Date tmp=DateTool.createDate(1900, 1, 1, hour, minute, second);
		long tp=DateTool.getTimePortion(tmp);
		this.setMax(new Date(tp));
	}

	/**
	 * 设置一个只包含日期部分的Date对象
	 * By GuRui on 2014-12-4 上午12:59:45
	 * @param year
	 * @param day
	 * @param month
	 */
	public void setMax(int year, int day, int month){
		this.setMax(DateTool.createDate(year, month, day, 0, 0, 0));
	}

	/**
	 * 设置一个由年月日时分秒构造的Date对象
	 * By GuRui on 2014-12-4 上午1:00:04
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public void setMax(int year, int month, int day, int hour, int minute, int second){
		this.setMax(DateTool.createDate(year, month, day, hour, minute, second));
	}

	public void setMax(Date max) {
		if(max.getTime()<this.getMin().getTime()){
			throw new ValidationException("设定最大值时发生异常：最大值不可小于最小值");
		}
		this.max = max;
	}

	@Override
	protected Date getVal(){
		return (Date)this.val;
	}

}
