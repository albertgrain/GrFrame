// By GuRui on 2017-10-27 下午1:40:47
package dlmu.mislab.test;

import java.util.List;

import dlmu.mislab.common.IJson;
import dlmu.mislab.common.LogicError;
import dlmu.mislab.web.interact.IResponse;
import dlmu.mislab.web.response.Err;
import dlmu.mislab.web.response.Ok;
import dlmu.mislab.web.response.OkList;

public class TestHelper<T extends IJson> {
	
	/***
	 * 检测rspn是否是Ok，并转换为指定类型。此方法绝对不会返回null
	 * By GuRui on 2017-10-27 下午1:47:43
	 * @param rspn
	 * @return 指定类型对象，不会返回null（如为null则抛出异常）
	 */
	public T assertIsOk(IResponse rspn){
		if(rspn==null){
			throw new TestException("rspn is null");
		}
		if(rspn instanceof Ok){
			@SuppressWarnings("unchecked")
			T rtn=(T) ((Ok)rspn).getrspn();
			if(rtn==null){
				throw new TestException("rspn.rspn is not of type T");
			}
			return rtn; 
		}else{
			throw new TestException("rspn is NOT Ok");
		}
	}
	
	
	/***
	 * 检测rspn是否是OkList，并转换为指定类型。此方法绝对不会返回null
	 * By GuRui on 2017-10-27 下午1:49:08
	 * @param rspn
	 * @return 指定类型对象，不会返回null（如为null则抛出异常）
	 */
	public List<T> assertIsOkList(IResponse rspn){
		if(rspn==null){
			throw new TestException("rspn is null");
		}
		if(rspn instanceof OkList){
			@SuppressWarnings("unchecked")
			List<T> rtn = ((OkList<T>)rspn).getRspnList();
			if(rtn==null){
				throw new TestException("rspn.rspnList is not of type List<T>");
			}
			return rtn;
		}else{
			throw new TestException("rspn.rspnList is NOT OkList");
		}
	}
	
	/***
	 * 检测rspn是否是OkList且不为空（size>0)，并转换为指定类型。此方法绝对不会返回null
	 * By GuRui on 2017-10-27 下午2:19:01
	 * @param rspn
	 * @return 指定类型对象，不会返回null（如为null或不满足条件则抛出异常）
	 */
	public List<T> assertIsOkListAndNotEmpty(IResponse rspn){
		List<T> rtn=this.assertIsOkList(rspn);
		if(rtn.size()<=0){
			throw new TestException("rspn.rspnList is not null but is empty");
		}
		return rtn;
	}
		
	/***
	 * 检测rspn是否是Err，并返回一个LogicError对象。此方法绝对不会返回null
	 * By GuRui on 2017-10-27 下午1:49:39
	 * @param rspn
	 * @return 一个LogicError对象，不会返回null（如为null则抛出异常）
	 */
	public static  LogicError assertIsErr(IResponse rspn){
		if(rspn==null){
			throw new TestException("rspn is null");
		}
		if(rspn instanceof Err){
			LogicError rtn=((Err)rspn).getErr();
			if(rtn==null){
				throw new TestException("rspn.rspn is not of type Err");
			}
			return rtn;
		}else{
			throw new TestException("rspn is NOT Err");
		}
	}

}
