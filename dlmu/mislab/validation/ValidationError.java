// By GuRui on 2015-4-16 下午11:06:42
package dlmu.mislab.validation;

import dlmu.mislab.common.LogicError;

/***
 * 字段验证错误。根据字段取值范围和业务逻辑规则，字段验证失败
 * 注意和ValidationException区分
 * 不可将此一场抛给用户
 * @author GuRui
 *
 */
public class ValidationError extends LogicError {
	private static final long serialVersionUID = 1L;
	private String fieldName;
	
	public ValidationError(String fieldName, String errMsg) {
		super(LogicError.CODE_VALIDATION_ERROR,errMsg);
		this.fieldName = fieldName;
	}
	public String getFieldName(){
		return this.fieldName;
	}
}
