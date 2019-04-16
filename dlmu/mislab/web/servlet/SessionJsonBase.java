// By GuRui on 2015-2-5 上午12:49:39
package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SessionJsonBase extends SessionBase implements IHttpJson {

	/**
	 * By GuRui on 2015-2-5 上午12:49:41
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		JsonBase.setHeader(response);
		if(super.isLoggedIn()){
			JsonTool.toJson(request, response, this, false);
		}else{
			toLogin(logger, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		JsonBase.setHeader(response);
		if(super.isLoggedIn()){
			JsonTool.toJson(request, response, this, true);
		}else{
			toLogin(logger, response);
		}
	}
}
