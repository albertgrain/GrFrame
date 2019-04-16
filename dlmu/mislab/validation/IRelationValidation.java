// By GuRui on 2016-5-29 上午12:48:19
package dlmu.mislab.validation;

/***
 * This interface is to be applied on bean to do relational validation among properties/fields
 * By GuRui on 2016-5-29 上午12:49:48
 *
 */
public interface IRelationValidation {
	public boolean validate();
	public String getLastErrMsg();
}
