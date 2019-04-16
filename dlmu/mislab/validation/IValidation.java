
// By GuRui on 2014-11-28 上午6:13:54
package dlmu.mislab.validation;

import dlmu.mislab.common.LogicError;

interface IValidation {
	public LogicError validate();
	public boolean isNullable();
}
