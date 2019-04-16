// By GuRui on 2015-12-6 下午6:41:12
package dlmu.mislab.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 此字段不用提供被验类，只验证子元素（子元素字段需提供验证类）
 * 此字段可以是一个Array，List或任意Collection；也可以是一个包含更多子对象的容器对象
 * 一般用作指明此字段为指向其他表的引用，此时，此字段的值应为子表对应对象的一个列表。
 * 添加了此标记的model可以使用deep方法从多张表中直接加载一棵树
 * 
 * By GuRui on 2019-03-22 
 * By GuRui on 2015-12-5 下午2:17:47
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RefTo {

}
