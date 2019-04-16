// By GuRui on 2017-10-17 下午1:06:42
package dlmu.mislab.test;

import dlmu.mislab.tool.jn;

public class Test {
	private Integer aa;
	private String bb;
	public Integer getAa() {
		return aa;
	}
	public void setAa(Integer aa) {
		this.aa = aa;
	}
	public String getBb() {
		return bb;
	}
	public void setBb(String bb) {
		this.bb = bb;
	}
	
	public static void main(String[] args){
		Test t= jn.fromJson("{'aa':'xxx', 'bb':'bbb'}", Test.class);
		System.out.println(t.getAa());		
	}
	
}
