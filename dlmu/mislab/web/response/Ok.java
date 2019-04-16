package dlmu.mislab.web.response;
import dlmu.mislab.common.IJson;
import dlmu.mislab.web.interact.SimpleIJson;

/*
 * 执行成功时返回信息(JSON格式)的载体类。不可继承。
 */
public final class Ok extends Response {
	/*
	 * 待返回内容
	 */
	private IJson rspn = null;
	
	public static Ok getInstance(IJson rspn){
		Ok o=new Ok();
		o.setrspn(rspn);
		return o;
	}
	
	public Ok(){
		this("");
	}
	
	public Ok(String msg) {
		this(new SimpleIJson(msg));
	}

	public Ok(IJson rspn) {
		super(true);
		this.rspn=rspn;
	}

	public IJson getrspn() {
		return this.rspn;
	}

	public void setrspn(IJson rspn) {
		this.rspn = rspn;
	}

}