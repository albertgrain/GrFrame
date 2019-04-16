package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PageAuth extends SessionTool{
	private IAuthCode[] allowedAuthCodes;
	public PageAuth(HttpServletRequest request, HttpServletResponse response, IAuthCode... allowedAuthCodes) {
		super(request, response);
		this.allowedAuthCodes = allowedAuthCodes;
	}
	
	public boolean check() throws IOException {
		return this.check(true);
	}

	@Override
	public boolean check(boolean jump) throws IOException {
		return this.checkLogin(jump) &&	this.checkAuth(jump, this.allowedAuthCodes);
	}
}