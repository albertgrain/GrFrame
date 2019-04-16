// By GuRui on 2017-7-19 下午4:50:14
package dlmu.mislab.web.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.common.DictBase;
import dlmu.mislab.tool.Str;

class EmptyAuthUser implements IAuthUserInfo{

	@Override
	public String getUserId() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getRole() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public int getAuthCode() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
/***
 * 配合Servlet Session使用的用户权限处理工具
 * By GuRui on 2017-12-22 上午7:56:44
 *
 */
public abstract class SessionTool {
	protected static final int CODE_PLEASE_LOGIN = 5555;
	protected static final String MSG_PLEASE_LOGIN = "请登录";
	protected static final int CODE_ROLE_MISMATCH = 5557;
	protected static final String MSG_ROLE_MISMATCH = "当前账号未被授权，请重新登录";
	protected static final int CODE_AUTH_FAILED = 5556;
	protected static final String MSG_AUTH_FAILED = "当前账号未被授权，请重新登录";

	private IAuthUserInfo userInfo =null;
	private HttpServletRequest request=null;
	private HttpServletResponse response=null;
	private String loginPage = ConfigBase.PAGE_LOGIN;
		
	public SessionTool(HttpServletRequest request, HttpServletResponse response){
		this(request,response, ConfigBase.PAGE_LOGIN);
	}
	
	public SessionTool(HttpServletRequest request, HttpServletResponse response, String loginPage){
		this.request=request;
		Object uinfo=this.request.getSession().getAttribute(DictBase.TAG_USER_INFO);
		if(uinfo instanceof IAuthUserInfo) {
			this.userInfo = (IAuthUserInfo)uinfo;
		}else {
			this.userInfo = new EmptyAuthUser();
		}
		this.response=response;
		this.loginPage= loginPage;
	}
	
	/***
	 * 检查用户登录情况，可以根据需要设定为检查isLogin, isAuthroized 或 isRoleMatch，没有通过检查接将页面跳转到登录页
	 * @param jump 是否跳转。为true表示跳转不返值；为false表示返值不跳转
	 * @return
	 * @throws IOException
	 */
	public abstract boolean check(boolean jump) throws IOException;
	
	/***
	 * 通过isLogin检查是否登录,如果未登录将返回false或页面跳转到登录页
	 * @param jump 是否跳转
	 * @return
	 * @throws IOException
	 */
	public boolean checkLogin(boolean jump) throws IOException{
		if(this.isLogin()){
			return true;
		}else{
			if(jump){
				this.redirectToLogin(SessionTool.MSG_PLEASE_LOGIN, SessionTool.CODE_PLEASE_LOGIN);
			}
		}
		return false;
	}
	
	/***
	 * 通过isAuthorized检查角授权是否匹配,如果不匹配将返回false或页面跳转到登录页
	 * @param jump 是否跳转
	 * @param allowedAuthCodes
	 * @return
	 * @throws IOException
	 */
	public boolean checkAuth(boolean jump, IAuthCode... allowedAuthCodes) throws IOException{
		if(this.isAuthorized(allowedAuthCodes)){
			return true;
		}else{
			if(jump){
				this.redirectToLogin(SessionTool.MSG_AUTH_FAILED, SessionTool.CODE_AUTH_FAILED);
			}
			return false;
		}
	}
	
	/***
	 * 通过isRoleMatch检查角色名是否匹配,如果不匹配将返回false或页面跳转到登录页
	 * @param jump 是否跳转
	 * @param pageRole
	 * @return
	 * @throws IOException
	 */
	public boolean checkRole(boolean jump, String pageRole) throws IOException{
		if(this.isRoleMatch(pageRole)){
			return true;
		}else{
			if(jump){
				this.redirectToLogin(SessionTool.MSG_ROLE_MISMATCH, SessionTool.CODE_ROLE_MISMATCH);
			}
			return false;
		}
	}
	
	/***
	 * 检测是否已经登录
	 * @return
	 */
	protected boolean isLogin(){
		if(this.userInfo==null){
			return false;
		}
		if(Str.isNullOrEmpty(userInfo.getUserId())){
			return false;
		}
		return true;
	}
	
	/***
	 * 检查是否满足页面授权要求
	 * @param pageAuthCode: 页面允许的授权
	 * @return
	 */
	protected boolean isAuthorized(IAuthCode... pageAuthCode) {
		return this.getUserInfo()!=null && 
				SessionTool.checkAuth(this.userInfo.getAuthCode(), pageAuthCode);
	}
	
	/***
	 * 检查用户角色是否匹配（用户角色字符串匹配）
	 * @param pageRole 页面允许的角色
	 * @return
	 */
	protected boolean isRoleMatch(String pageRole){
		if(pageRole.equals(this.userInfo.getRole())){
			return true;
		}
		return false;
	}
	
	
	public void logout(){
		this.userInfo=null;
		this.request.getSession().setAttribute(DictBase.TAG_USER_INFO, null);
		this.request.getSession().invalidate();
		this.request.getSession(true);
	}
		
	public IAuthUserInfo getUserInfo(){
		return this.userInfo;
	}
	

	/***
	 * 检查用户权限码是否在可访问权限包含的范围内
	 * 例如：用户权限码为1000，可访问权限包括1000，0100，1110，则用户访问权限验证通过；如用户权限码为0001，则不通过
	 * By GuRui on 2017-11-16 下午5:19:57
	 * @param userAuthCode 待检查用户权限码
	 * @param authCodes 可访问用户权限列表
	 * @return 通过返回true
	 */
	public static boolean checkAuth(int userAuthCode, IAuthCode... authCodes){
		int required=0;
		for(IAuthCode ac: authCodes){
			required |= ac.getValue();
		}
		return (required & userAuthCode) == userAuthCode;
	}

	/***
	 * 导向到登录页
	 * @param msg 传递给登录页面的消息（msg=***）
	 * @param otherParams 以&开头的附加信息字符串如："&isPlatformUser=false"
	 * @throws IOException
	 */
	public void redirectToLogin(String msg, String otherParams) throws IOException{
		this.redirectToLogin(msg, null, this.loginPage);
	}
	
	/***
	 * 导向到登录页
	 * @param msg: 传递给登录页面的消息（msg=***）
	 * @param code: Error code for the msg
	 * @throws IOException
	 */
	public void redirectToLogin(String msg, int code) throws IOException{
		this.redirectToLogin(msg, "&code="+code, this.loginPage);
	}
	
	/***
	 * 导向到登录页。会在url中携带backurl,msg,
	 * @param msg 传递给登录页面的消息（msg=***）
	 * @param otherParams 以&开头的附加信息字符串如："&isPlatformUser=false"，若无填写""或null
	 * @param loginPage 默认为"/login.jsp"
	 * @throws IOException
	 */
	public void redirectToLogin(String msg, String otherParams, String loginPage) throws IOException{
		String query="?backurl="+ URLEncoder.encode(this.request.getRequestURI(),ConfigBase.DEFAULT_ENCODING)+"&msg="+URLEncoder.encode(msg,ConfigBase.DEFAULT_ENCODING) + (otherParams==null?"":otherParams);
		this.response.sendRedirect(this.request.getContextPath()+loginPage+query);
	}
}
