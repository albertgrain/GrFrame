// By GuRui on 2016-5-22 上午10:59:50
package dlmu.mislab.web.response;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import dlmu.mislab.tool.jn;

public class OkMapRaw<K,V> extends Response {
	private Map<String,V> rspnMap=null;
	
	public OkMapRaw() {
		this(new LinkedHashMap<String,V>());
	}
	
	public OkMapRaw(Map<String,V> map){
		super(true);
		this.rspnMap=map;
	}
	@Override
	public String toString(){
		return jn.toJson(this.rspnMap);
	}
	public V get(String key){
		if(this.rspnMap.containsKey(key)){
			return this.rspnMap.get(key);
		}else{
			return null;
		}
	}
	
	/***
	 * Add or replace the key-value pair
	 * By GuRui on 2016-5-22 上午11:49:44
	 * @param key
	 * @param val
	 */
	public void set(String key, V val){
		if(this.rspnMap.containsKey(key)){
			this.rspnMap.remove(key);
		}
		this.rspnMap.put(key, val);
	}
	
	public Iterator<Entry<String,V>> getIterator(){
		return this.rspnMap.entrySet().iterator();
	}
	
	public void setRspnMap(Map<String,V> map){
		this.rspnMap=map;
	}
	public Map<String, V> getRspnMap() {
		return rspnMap;
	}
}
