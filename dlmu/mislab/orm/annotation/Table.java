// By GuRui on 2015-12-5 下午3:43:46
package dlmu.mislab.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 标记IBean对象对应的表名
 * By GuRui on 2015-12-5 下午2:18:22
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Table {
	String Name();
}