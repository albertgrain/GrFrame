// By GuRui on 2015-12-25 上午8:02:33
package dlmu.mislab.web.response;

import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.common.LogicError;

public class ErrRedirect extends Response{
	private String url=ConfigBase.PAGE_LOGIN;
	private LogicError err=null;
	
	public ErrRedirect(LogicError err){
		super(false);
		this.err=err;
	}
	
	public ErrRedirect(String msg) {
		super(false);
		this.err=new LogicError(msg);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public LogicError getErr() {
		return err;
	}

	public void setErr(LogicError err) {
		this.err = err;
	}


	
}
