package dlmu.mislab.web.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.tool.jn;
import dlmu.mislab.web.response.Err;

/***
 * Wait for timeout. Used for queuing for exclusive resources.
 * Ref to BlockPerTokenFilter for multiple waits
 * @author GuRui
 *
 */
@WebFilter(dispatcherTypes = { DispatcherType.REQUEST, DispatcherType.FORWARD }, urlPatterns = { "*.queue" })
public class BlockSingletonFilter implements Filter {
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(!SingletonBlocker.INSTANCE.getToken()){
			response.setContentType(ConfigBase.CONTENT_TYPE_JSON);
			jn.toJson(new Err("The resources is using by other user. Please try again after " + SingletonBlocker.TIMEOUT/1000 + " seconds."),response.getWriter());
			return;
		}
		chain.doFilter(request, response);

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		

	}
}

/***
 * This class used to ensure exclusive resources to be used one by one.  
 * @author GuRui
 *
 */
enum SingletonBlocker{
	INSTANCE;
	
	private Date start=new Date();
	private boolean status;
	static int TIMEOUT= 20*1000;		// Times to remove block to prevent from dead-locking;
	
	/***
	 * Get token and set freeze
	 * @return
	 */
	public synchronized boolean getToken(){
		if(this.status){
			this.freeze();
			return true;
		}
		if(this.testToken()){
			return true;
		}
		return false;
	}
	
	/***
	 * Free the token
	 */
	public synchronized void free(){
		this.status=true;
	}
	
	/***
	 * Freeze the token
	 */
	public synchronized void freeze(){
		this.status=false;
		this.start=new Date();
	}
	
	private boolean testToken(){
		if(this.isTimeOut()){
			this.start= new Date();
			this.status=true;
			return true;
		}else{
			return false;
		}
	}
	
	private boolean isTimeOut(){
		return new Date().getTime()-start.getTime() > SingletonBlocker.TIMEOUT;
	}
}

