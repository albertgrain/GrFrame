package dlmu.mislab.validation;

import java.util.List;
import java.util.Set;

import dlmu.mislab.common.LogicError;
import dlmu.mislab.web.interact.IParameter;
import dlmu.mislab.web.response.Err;

/***
 * 对所有Collectiont类型(Array, List, Set)的字段进行验证
 * 如果是IParameter类型的对象，则必须使用RefTo标记，而不要写验证类
 * @author GuRui
 *
 * @param <T>
 */
public abstract class FieldCollection<T> extends FieldValidationBase{
	private T[] list=null;
	private int maxListLen=1000000;
	private int minListLen=0;
	
	/***
	 * 对Collection(可以是[],List或Set)中所有的基本元素(如String,Integer等）进行检验。
	 * 如果子元素是IParameter类型对象，则会自动递归检验，此方法直接返回null即可。
	 * @param item List中的元素
	 * @return 错误信息提示。如无错误返回null
	 */
	abstract protected LogicError validateEachBasicItem(T item);
	
	@SuppressWarnings("unchecked")
	public FieldCollection(List<T> list) {
		super(list);
		this.list=(T[])list.toArray();
	}
	
	public FieldCollection(T[] list) {
		super(list);
		this.list=list;
	}
	
	@SuppressWarnings("unchecked")
	public FieldCollection(Set<T> list){
		super(list);
		this.list=(T[])list.toArray();
	}

	protected void setMinListLength(int i){
		if(i>0){
			this.minListLen=i;
		}
	}
	protected void setMaxListLength(int i){
		if(i>0){
			this.maxListLen=i;
		}
	};
	
	@Override
	public LogicError validate() {
		if(this.list==null){
			if(this.minListLen==0 || this.isNullable()){
				return null;
			}else{
				throw new ValidationError(this.getFieldName(),this.getFieldNameCnBracket() + "不可为空列表");
			}
		}
		if(this.list.length<this.minListLen || this.list.length>this.maxListLen){
			throw new ValidationError(this.getFieldName(),this.getFieldNameCnBracket() + "列表中元素个数应在["+this.minListLen+"-"+this.maxListLen+"]个之间");
		}
		
		//do deep validation
		for(T t : this.list){
			if(t instanceof IParameter){
				Err err = Validator.validateBeanStatic((IParameter)t);
				if(err!=null){
					return new LogicError(LogicError.CODE_VALIDATION_ERROR, err.getMsg());
				}
			}else{
				return this.validateEachBasicItem(t);
			}
		}
		
		return null;
	}

	@Override
	protected Object getVal() {
		return this.list;
	}
}
