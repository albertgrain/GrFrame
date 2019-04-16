// By GuRui on 2014-12-20 下午12:34:48
package dlmu.mislab.web.interact;

import dlmu.mislab.common.IJson;

public class SimpleIJson implements IJson{
	private String msg=null;
	public SimpleIJson(String msg){
		this.setMsg(msg);
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
