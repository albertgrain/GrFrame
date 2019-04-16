// By GuRui on 2016-6-21 上午11:29:49
package dlmu.mislab.common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlmu.mislab.web.interact.IResponse;
import dlmu.mislab.web.response.Err;
import dlmu.mislab.web.response.OkMapRaw;

/***
 * 通过此类获得系统常量（封装为IResponse对象）。此类内含单例机制保证常量不会被重复读取
 * @author GuRui
 *
 */
public class ConstantsManager {
	private static List<ConstStatus> constList=new LinkedList<ConstStatus>();
	private static Logger logger=LoggerFactory.getLogger(ConstantsManager.class);
	public static final String DEFAULT_CONSTANT_SET_NAME = "default";
	public static ILoadConstant loader;
	
	/***
	 * 对整个常量套集合设置更新标记。之后在获取常量套集合的过程中会导致常量刷新加载。
	 * By GuRui on 2016-5-22 上午11:52:19
	 */
	public static void setRefresh(String nameOfConstSet){
		for(ConstStatus ss : constList){
			if(ss.nameOfConstSet.equals(nameOfConstSet)){
				ss.refresh=true;
				break;
			}
		}
	}
	
	/***
	 * 读取所有常数并返回IResponse对象
	 * By GuRui on 2016-6-21 下午3:02:52
	 * @param apps 需要返回的常数组名称
	 * @param loader 特定的用于从数据库加载常数数据的loader类
	 * @return IResponse对象，可以直接供基于JsonBase的servlet转化为json字符串
	 */
	public static IResponse load(String[] apps){
		return load(apps, DEFAULT_CONSTANT_SET_NAME);
	}
	
	/***
	 * 读取所有常数并返回IResponse对象
	 * By GuRui on 2016-6-21 下午3:02:52
	 * @param constsNeeded 需要返回的常数组名称
	 * @param loader 特定的用于从数据库加载常数数据的loader类
	 * @param nameOfConstSet  常量集套名。一般项目以项目名为常量集合名，这样只有一套常量集合。如果项目中需要多套常量，则需根据套名分别存储。如以引航站编号为key，分别加载各站的引航员列表（常量）
	 * @return IResponse对象，可以直接供基于JsonBase的servlet转化为json字符串
	 */
	public static IResponse load(String[] constsNeeded, String nameOfConstSet){
		if(loader==null){
			return new Err("常量加载对象为空，无法获得常量");
		}
		for(ConstStatus ss : constList){
			if(ss.nameOfConstSet.equals(nameOfConstSet)){
				if(ss.refresh){
					if(loadConstants(loader, nameOfConstSet)==null){
						return new Err("读取某个（些）常量失败");
					}
					ss.refresh=false;
				}
				return chooseSpecificConstants(constsNeeded, ss.constants);
			}
		}
		//nameOfConstSet not found:
		OkMapRaw<String,Object> map=loadConstants(loader, nameOfConstSet);
		if(map!=null){
			constList.add(new ConstStatus(nameOfConstSet, false, map));
			return chooseSpecificConstants(constsNeeded,map);
		}
		return new Err("没有找到键为["+nameOfConstSet+"]的常数（列表）");
	}
	
	
	/***
	 * 获取默认常量套项下全部常量
	 * @return 默认常量套所包含的全部常量Map。出错返回null
	 */
	public static OkMapRaw<String,Object> loadAll(){
		return loadAll(DEFAULT_CONSTANT_SET_NAME);
	}
	
	/***
	 * 获取某个常量套项下全部常量
	 * @param nameOfConstSet 常量套名
	 * @return 该常量套所包含的全部常量Map。出错返回null
	 */
	public static OkMapRaw<String,Object> loadAll(String nameOfConstSet){
		if(loader==null){
			logger.error("常量加载对象为空，无法获得常量");
			return null;
		}
		for(ConstStatus ss : constList){
			if(ss.nameOfConstSet.equals(nameOfConstSet)){
				if(ss.refresh){
					if(loadConstants(loader, nameOfConstSet)==null){
						logger.error("读取某个（些）常量失败");
						return null;
					}
					ss.refresh=false;
				}
				return ss.constants;
			}
		}
		OkMapRaw<String,Object> map=loadConstants(loader, nameOfConstSet);
		constList.add(new ConstStatus(nameOfConstSet,false,map));
		return map;
	}
	
	/***
	 * Choose specific constants according to application name
	 * Refer to the 'dc.CONST_PREFIX_***' for the application name details. 
	 * By GuRui on 2016-5-28 下午1:30:56
	 * @param app
	 * @param map
	 * @return
	 */
	private static IResponse chooseSpecificConstants(String[] apps, OkMapRaw<String,Object> map){
		OkMapRaw<String,Object> rtn= new OkMapRaw<String,Object>();
		Iterator<Map.Entry<String,Object>> itr=map.getIterator();
		while(itr.hasNext()){
			Entry<String, Object> entry=itr.next();
			for(int i=0;i<apps.length;i++){
				if(entry.getKey().equals(apps[i])){
					rtn.set(entry.getKey(), entry.getValue());	
				}
			}
		}
		return rtn;
	}
	
	private static OkMapRaw<String,Object> loadConstants(ILoadConstant loader, String key){
		OkMapRaw<String,Object> map=new OkMapRaw<String,Object>();
		boolean rtn=false;
		try{
			rtn=loader.loadAll(map,key);
		}catch(Exception e){
			logger.error("读取常数信息失败:"+ e.getMessage());
			return null;
		}
		if(rtn){
			return map;
		}else{
			return null;
		}
	}
}

class ConstStatus{
	public ConstStatus(String nameOfConstSet, boolean refresh,OkMapRaw<String,Object> constants ){
		this.nameOfConstSet=nameOfConstSet;
		this.refresh=refresh;
		this.constants=constants;
	}
	public String nameOfConstSet;
	public boolean refresh=true;
	public OkMapRaw<String,Object> constants=new OkMapRaw<String,Object>();
}
