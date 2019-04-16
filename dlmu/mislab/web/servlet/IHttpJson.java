// By GuRui on 2015-2-5 上午1:31:16
package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dlmu.mislab.web.interact.IResponse;


public interface IHttpJson {
	IResponse doJsonGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException;
	IResponse doJsonPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException;
}
