// By GuRui on 2014-12-14 下午7:16:47
package dlmu.mislab.common;

import dlmu.mislab.web.interact.IParameter;

/***
 * 键-值（字符串）对
 * @author GuRui
 *
 */
public class KeyValuePair implements IParameter, IJson {
	private String key;
	private String value;

	public KeyValuePair(){}
	public KeyValuePair(String key, String value){
		this.setKey(key);
		this.setValue(value);
	}

	@Override
	public String toString(){
		return this.getKey() + ":" + this.getValue();
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}


}
