package dlmu.mislab.web.response;

import dlmu.mislab.web.interact.IResponse;

/**
 * Servlet返回信息的基类。请用Ok和Err对象返回信息，尽量不要从此类继承
 * @author hx
 *
 */
public abstract class Response implements IResponse{
	private boolean ok=true;

	protected Response(boolean ok){
		this.ok=ok;
	}
	
	public boolean isOk() {
		return ok;
	}

	protected void setOk(boolean ok) {
		this.ok = ok;
	}
	
	
}