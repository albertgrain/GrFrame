package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

import dlmu.mislab.common.DictBase;
import dlmu.mislab.common.IUserInfo;
import dlmu.mislab.common.LogicError;
import dlmu.mislab.tool.Str;
import dlmu.mislab.tool.jn;
import dlmu.mislab.web.WebLogicError;
import dlmu.mislab.web.response.ErrRedirect;

/**
 * 
 * By GuRui on 2014-11-28
 *
 */
public abstract class SessionBase extends ServletBase{
	private static final long serialVersionUID = 1L;
	
	private IUserInfo userInfo=null;
	protected IUserInfo getUserInfo(){
		return this.userInfo;
	}

	private boolean isLoggedIn=false;
	protected boolean isLoggedIn(){
		return this.isLoggedIn;
	}
	
	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.isLoggedIn=this.checkLogin(request);
		super.service(request, response);
	}
	
	private IUserInfo getUserInfo(HttpServletRequest request) throws ServletException, IOException {
		HttpSession session=request.getSession();
		if(session!=null){
			return (IUserInfo)session.getAttribute(DictBase.TAG_USER_INFO);
		}
		return null; 
	}
	
	private boolean checkLogin(HttpServletRequest request) throws ServletException, IOException {
		this.isLoggedIn=false;
		IUserInfo userInfo=this.getUserInfo(request);
		if(userInfo!=null){
			this.userInfo=userInfo;
			if(!Str.isNullOrEmpty(this.getUserInfo().getUserId())) {
				this.isLoggedIn=true;
			}
		}
		return this.isLoggedIn;
	}
	
	static void toLogin(Logger logger, HttpServletResponse response) throws ServletException, IOException{
		toLogin(logger, response, null);
	}
	static void toLogin(Logger logger, HttpServletResponse response, String msg) throws ServletException, IOException{
		toLogin(logger, response, msg, WebLogicError.CODE_PLEASE_LOGIN);
	}
	
	static void toLogin(Logger logger, HttpServletResponse response, String msg, int code) throws ServletException, IOException{
		logger.error(msg);
		LogicError err=null;
		if(Str.isNullOrEmpty(msg)){
			err = WebLogicError.BIZ_LOGIN_REQUIRED;
		}else{
			err = new LogicError(code, msg);
		}
		jn.toJson(new ErrRedirect(err),response.getWriter());
	}
}

