// By GuRui on 2015-2-5 上午12:49:07
package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dlmu.mislab.common.ConfigBase;

public abstract class SessionPageBase extends SessionBase{

	/**
	 * By GuRui on 2015-2-5 上午12:49:11
	 */
	private static final long serialVersionUID = 1L;
	protected abstract void doBiz(HttpServletRequest request, HttpServletResponse response);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if(super.isLoggedIn()){
			this.doBiz(request, response);
		}else{
			this.forward(request, response,ConfigBase.PAGE_LOGIN);
		}
	}
	
	@Override 
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if(super.isLoggedIn()){
			this.doBiz(request, response);
		}else{
			this.forward(request, response,ConfigBase.PAGE_LOGIN);
		}
	}
	
	protected void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String errMsg){
		this.forward(request, response, ConfigBase.PAGE_ERROR + "?err_msg="+errMsg);
	}
	
	protected void forward(HttpServletRequest request, HttpServletResponse response, String url){
		RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher(url);
		try {
			dispatcher.forward(request,response);
		} catch (Exception e) {
			logger.error("跳转到:["+url+"]失败",e);
			try {
				response.sendRedirect(request.getServletContext().getContextPath()+ConfigBase.PAGE_ERROR+ "?err_msg="+e.getMessage());
			} catch (IOException e1) {
				logger.error("跳转到:["+ConfigBase.PAGE_ERROR+"]失败",e1);
			}
		}
	}

}
