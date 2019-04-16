// By GuRui on 2017-6-19 下午3:08:37
package dlmu.mislab.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 此标记只用于Select
 * By GuRui on 2017-6-19 下午3:09:15
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface IsFnKey {
	/***
	 * 外键指向的表名，不可省略
	 * By GuRui on 2017-6-19 下午3:14:26
	 * @return
	 */
	String FnTableName();
	/***
	 * 外键指向的表中的主键字段，缺省则取和主表同名字段
	 * 注意不可省略
	 * By GuRui on 2017-6-19 下午3:14:48
	 * @return
	 */
	String FnKeyName() default "";
}
