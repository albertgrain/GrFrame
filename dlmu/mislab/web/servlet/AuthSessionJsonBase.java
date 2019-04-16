// By GuRui on 2017-12-7 下午5:13:08
package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AuthSessionJsonBase extends SessionBase implements IHttpJson{
	private static final long serialVersionUID = 1L;
	//Mixin
	private SessionTool authTool;
	
	/**
	 * 给用于构造AuthSessionJsonBase需要用到的授权对象传递参数 <br />
	 * 示例,设定本Serlvet可以被经理和老板访问：<br />
	 * this.setAtuthCodes(request,response, MyPageAuthCode.BOSS, MyPageAuthCode.MANAGER) <br />
	 * 其中：MyPageAuthCode implements IAuthCode <br />
	 * @param request
	 * @param response
	 */
	protected abstract void setupAuth(HttpServletRequest request,HttpServletResponse response);
	
		
	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {	
		this.setupAuth(request,response);
		if(!this.authTool.check(false)){
			SessionJsonBase.toLogin(logger, response, SessionTool.MSG_AUTH_FAILED);
			return;
		}
		super.service(request, response);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		JsonBase.setHeader(response);
		JsonTool.toJson(request, response, this, false);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		JsonBase.setHeader(response);
		JsonTool.toJson(request, response, this, true);
	}



	protected SessionTool getAuthTool() {
		
		return this.authTool;
	}

	protected void setAuthTool(SessionTool authTool) {
		this.authTool = authTool;
	}
	
	/***
	 * 给用于构造AuthSessionJsonBase需要用到的授权对象(SessionTool)传递参数 <br />
	 * @param request
	 * @param response
	 * @param pageAuth 任意个IAuthCode枚举对象
	 */
	protected void setAtuthCodes(HttpServletRequest request, HttpServletResponse response, IAuthCode... pageAuth) {
		this.authTool= new PageAuth(request,response, pageAuth);
	}
}
