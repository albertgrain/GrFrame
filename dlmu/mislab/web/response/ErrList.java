package dlmu.mislab.web.response;

import java.util.List;

/*
 * 执行成功时返回信息(JSON格式)的载体类。不可继承。
 */
public final class ErrList<T> extends Err {
	/*
	 * 待返回内容
	 */
	private List<T> errList =null;
		
	public ErrList(List<T> errList) {
		this(errList, "ErrList");
	}

	public ErrList(List<T> errList, String msg) {
		super(msg);
		this.setErrList(errList);
	}
	
	public void add(T rspn){
		this.errList.add(rspn);
	}
	
	public T get(int index){
		if(index<this.getErrList().size()){
			return this.getErrList().get(index);
		}else{
			return null;
		}
	}

	public List<T> getErrList() {
		return errList;
	}

	public void setErrList(List<T> errList) {
		if(errList!=null){
			this.errList = errList;
		}
	}

}