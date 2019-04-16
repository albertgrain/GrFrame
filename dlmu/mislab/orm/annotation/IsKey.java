// By GuRui on 2015-12-5 下午2:17:16
package dlmu.mislab.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 标记一个字段为本表主键
 * By GuRui on 2015-12-5 下午2:17:47
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface IsKey {
	/***
	 * 若标记为true，则此字段将不出现在Select语句的Where条件中。默认为false
	 */
	boolean Auto() default false;
}
