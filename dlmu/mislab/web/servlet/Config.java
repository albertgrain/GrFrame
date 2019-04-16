// By GuRui on 2015-5-22 下午12:43:35
package dlmu.mislab.web.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import dlmu.mislab.config.BeanUtilsConfigure;
import dlmu.mislab.config.DbConfigure;
import dlmu.mislab.config.Log4JConfigure;

@WebListener
public class Config implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		//Do nothing
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		Log4JConfigure.init();
		BeanUtilsConfigure.init();
		String cfgName=ServletBase.getProjectName(event.getServletContext().getContextPath());
		DbConfigure.init(cfgName);
	}

}
