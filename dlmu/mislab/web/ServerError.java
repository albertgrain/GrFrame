// By GuRui on 2015-8-27 下午5:00:31
package dlmu.mislab.web;

import dlmu.mislab.common.LogicError;

public class ServerError extends WebLogicError {
	private static final long serialVersionUID = 1L;

	public ServerError(String msg) {
		super(LogicError.CODE_SERVER_ERROR , msg);
	}

}
