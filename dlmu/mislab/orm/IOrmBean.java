// By GuRui on 2015-12-5 下午2:20:12
package dlmu.mislab.orm;

import dlmu.mislab.common.IJson;
import dlmu.mislab.validation.IValidatable;

/***
 * 只是一个标记，表明该类型javabean对象可以利用Bn进行便捷持久化(ORM: Object Relational Mapping)
 * By GuRui on 2015-12-7 下午2:26:23
 *
 */
public interface IOrmBean extends IJson, IValidatable{
 
}
