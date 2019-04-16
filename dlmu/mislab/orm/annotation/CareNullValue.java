// By GuRui on 2016-9-2 下午1:58:08
package dlmu.mislab.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 此标记表明此字段值若为空，则强制生成[字段名 IS NULL]的SQL语句
 * By GuRui on 2016-9-2 下午9:33:36
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CareNullValue {
	
}