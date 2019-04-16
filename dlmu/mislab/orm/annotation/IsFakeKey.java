// By GuRui on 2016-9-3 上午1:50:49
package dlmu.mislab.orm.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 标记一个字段为伪主键。伪主键作用和主键一样，但不对应物理表中的主键，一般应对应物理表中的UNIQUE字段
 * 标记为FakeKey的字段在Delete和Update时不会出现在Where中（2017-12-13修正），但在Select中起作用
 * By GuRui on 2017-12-17 下午2:17:47
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IsFakeKey {
	
}
