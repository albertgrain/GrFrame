package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Version
 */
@WebServlet({ "/json/Version", "/Version" })
public class Version extends ServletBase {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().print(
				"{\"rspn\":{\"apple\":{\"ver\":\"1.0\",\"code\":1.0},"+
						"\"android\":{\"ver\":\"1.0\",\"code\":1.0}"+
						"},"+
				"\"ok\":true}");

	}

}
