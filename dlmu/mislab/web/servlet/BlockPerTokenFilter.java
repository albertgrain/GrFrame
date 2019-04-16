package dlmu.mislab.web.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.common.DictBase;
import dlmu.mislab.common.IUserInfo;
import dlmu.mislab.tool.Str;
import dlmu.mislab.tool.jn;
import dlmu.mislab.web.response.Err;

/***
 * Wait for timeout. Used for preventing quick re-submission.
 * The block is checked per token. Means there maybe multiple waits than a single queue
 * Ref to BlockSingletonFilter for a single queue
 * @author GuRui
 *
 */
@WebFilter(dispatcherTypes = { DispatcherType.REQUEST, DispatcherType.FORWARD }, urlPatterns = { "*.wait" })
public class BlockPerTokenFilter implements Filter {

	@Override
	public void destroy() {
		PerTokenBlocker.INSTANCE.clean();
	}
	
	private String getId(ServletRequest request){
		if(!(request instanceof HttpServletRequest)){
			return null;
		}
		HttpSession session=((HttpServletRequest)request).getSession();
		if(session!=null){
			IUserInfo uif= (IUserInfo)session.getAttribute(DictBase.TAG_USER_INFO);
			if(uif==null){
				return null;
			}
			return uif.getUserId();
		}
		return null; 
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(!PerTokenBlocker.INSTANCE.getToken(this.getId(request))){
			response.setContentType(ConfigBase.CONTENT_TYPE_JSON);
			jn.toJson(new Err("The resources is using by other user. Please try again after " + PerTokenBlocker.TIMEOUT/1000 + " seconds."),response.getWriter());
			return;
		}
		chain.doFilter(request, response);

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		//blocker.setBlockInterval(PerTokenBlocker.TIMEOUT);
	}
}

/***
 * This class used to prevent double-click on submit button or links
 * @author GuRui
 *
 */
enum PerTokenBlocker{
	INSTANCE;
	
	static final int TIMEOUT = 3*1000 ; // The max time ticks before blocking is removed
	private int blockInterval= TIMEOUT;	//
	
	/***
	 * The table stores the key and time the key is stored
	 */
	private HashMap<String, Long> blockTable=new HashMap<String, Long>();
	
	synchronized boolean getToken(String id){
		if(Str.isNullOrEmpty(id)){
			return false;
		}
		Long rtn = this.blockTable.get(id);
		if(rtn==null || (new Date().getTime() - rtn) > blockInterval){
			this.blockTable.put(id, new Date().getTime());
			return true;
		}else{
			return false;
		}
	}
	
	synchronized void clean(){
		 Iterator<Entry<String, Long>> itr = this.blockTable.entrySet().iterator();
		 Long curTime=new Date().getTime();
		 while(itr.hasNext()){
			 Entry<String, Long> item=itr.next();
			 Long t=item.getValue();
			 if((curTime - t) > blockInterval){
				 itr.remove();
			 }
		 }
	}

	int getBlockInterval() {
		return blockInterval;
	}

	void setBlockInterval(int blockInterval) {
		this.blockInterval=blockInterval;
	}
	
}

