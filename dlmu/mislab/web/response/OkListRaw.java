// By GuRui on 2015-12-7 下午9:35:37
package dlmu.mislab.web.response;

import java.util.ArrayList;
import java.util.List;

import dlmu.mislab.tool.jn;

public class OkListRaw extends Response{
	/*
	 * 待返回内容
	 */
	private List<Object> rspnList = new ArrayList<Object>();
	
	public OkListRaw() {
		this(null);
	}

	public OkListRaw(List<Object> rspnList) {
		super(true);
		this.setRspnList(rspnList);
	}
	@Override
	public String toString(){
		return jn.toJson(this.rspnList);
	}
	
	public void add(Object rspn){
		this.rspnList.add(rspn);
	}
	
	public Object get(int index){
		if(index<this.getRspnList().size()){
			return this.getRspnList().get(index);
		}else{
			return null;
		}
	}

	public List<Object> getRspnList() {
		return rspnList;
	}

	public void setRspnList(List<Object> rspnList) {
		if(rspnList!=null){
			this.rspnList = rspnList;
		}
	}
}
