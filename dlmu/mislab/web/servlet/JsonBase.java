// By GuRui on 2015-2-5 上午1:30:23
package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class JsonBase extends ServletBase implements IHttpJson{

	/**
	 * By GuRui on 2015-2-5 上午1:30:26
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		setHeader(response);
		JsonTool.toJson(request,response, this, false);
	}
	
	@Override 
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		setHeader(response);
		JsonTool.toJson(request,response, this, true);
	}
	
//	static void setHeader(HttpServletResponse response){
//		response.setHeader("Cache-Control","no-cache"); //Prevent IE cache Ajax request
//	}
	
//	static void toJson(HttpServletRequest request, HttpServletResponse response, IHttpJson that, boolean doPost) throws ServletException, IOException{
//		jn.toJson(doPost?that.doJsonPost(request, response):that.doJsonGet(request, response),response.getWriter());
//	}
}
