package dlmu.mislab.web.response;

import java.util.ArrayList;
import java.util.List;

import dlmu.mislab.common.IJson;

/*
 * 执行成功时返回信息(JSON格式)的载体类。不可继承。
 */
public final class OkList<T extends IJson> extends Response {
	/*
	 * 待返回内容
	 */
	private List<T> rspnList =null;
	
	public OkList() {
		this(new ArrayList<T>());
	}

	public OkList(List<T> rspnList) {
		super(true);
		this.setRspnList(rspnList);
	}
	
	public void add(T rspn){
		this.rspnList.add(rspn);
	}
	
	public T get(int index){
		if(index<this.getRspnList().size()){
			return this.getRspnList().get(index);
		}else{
			return null;
		}
	}

	public List<T> getRspnList() {
		return rspnList;
	}

	public void setRspnList(List<T> rspnList) {
		if(rspnList!=null){
			this.rspnList = rspnList;
		}
	}

}