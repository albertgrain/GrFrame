// By GuRui on 2015-8-27 下午4:52:49
package dlmu.mislab.web;

import java.util.List;

import dlmu.mislab.common.KeyValuePair;


public class MappingError extends WebLogicError {
	private static final long serialVersionUID = 1L;
	private List<KeyValuePair> badPairs=null;
	public MappingError(String errMsg,List<KeyValuePair> badPairs) {
		super(errMsg);
		this.badPairs=badPairs;
	}
	public List<KeyValuePair> getBadPairs() {
		return badPairs;
	}
	public void setBadPairs(List<KeyValuePair> badPairs) {
		this.badPairs = badPairs;
	}

	@Override
	public String getMsg(){
		if(this.badPairs==null){
			return super.getMsg();
		}else{
			StringBuilder buf=new StringBuilder();
			buf.append(super.getMsg());
			for(KeyValuePair pair:this.badPairs){
				buf.append(" \n").append(pair.getValue());
			}
			return buf.toString();
		}
	}
}
