// By GuRui on 2016-5-22 下午1:38:41
package dlmu.mislab.common;

import dlmu.mislab.web.response.OkMapRaw;

public interface ILoadConstant {
	/***
	 * 存储所有的常量对象到静态内存中以备重复利用
	 * 注意：实现此接口的集合应该位于dlmu.mislab.orm.constants包内
	 * By GuRui on 2017-10-20 下午5:37:46
	 * @param map 待加载的常量对象集合
	 * @param nameOfConstSet 常量集套名。一般项目以项目名为常量集合名，这样只有一套常量集合。如果项目中需要多套常量，则需根据套名分别存储
	 * @return 存储成功返回true
	 */
	boolean loadAll(OkMapRaw<String,Object> map,String nameOfConstSet);
}
