// By GuRui on 2016-9-3 上午12:47:51
package dlmu.mislab.orm.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Annotation of a bean, telling whether non-query SQL command (update/delte/insert) affects only one row
 * If this annotation exists, then affectRow parameter should be set for no-query SQL operation regarding this bean;
 * By GuRui on 2016-9-2 下午9:33:36
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AffectRow {
	public static final int DEFAULT_NUM_OF_ROWS_TO_BE_AFFECTED=1;
	/**
	 * Non-query SQL command affect how many rows
	 * By GuRui on 2016-9-3 上午12:59:18
	 *
	 */
	public static enum AffectRowType{
		STRICTLY_ONE_ROW,						//==1
		ANY_NUMBER_OF_ROWS,						//Any but less than ConfigBase.MAX_AFFECTED_ROW_BY_ONE_TRANSACTION
		NO_MORE_THAN_ONE,						//<=1
		NO_MORE_THAN_SPECIFIED_NUMBER_OF_ROWS,	//You should specify an affected row number.
		SPECIFIED_NUMBER_OF_ROWS				//You should specify an affected row number.
	}
	
	AffectRowType Affect() default AffectRowType.NO_MORE_THAN_ONE;
}
