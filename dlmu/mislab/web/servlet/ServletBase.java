package dlmu.mislab.web.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlmu.mislab.common.ConfigBase;

import dlmu.mislab.common.DictBase;
import dlmu.mislab.common.KeyValuePair;

import dlmu.mislab.tool.JsonParseBadFieldException;
import dlmu.mislab.tool.Str;
import dlmu.mislab.validation.IRelationValidation;
import dlmu.mislab.validation.Validator;
import dlmu.mislab.web.interact.IParameter;
import dlmu.mislab.web.response.Err;



public abstract class ServletBase extends HttpServlet{
	private static final long serialVersionUID = 1L;
	protected Logger logger = null;

	public ServletBase(){
		this.logger=LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 从HttpServletRequest中获取项目名
	 * By GuRui on 2015-7-3 上午12:29:26
	 * @param request
	 * @return 失败返回null，成功返回项目名
	 */
	public static String getProjectName(HttpServletRequest request){
		if(request==null){
			return null;
		}
		return getProjectName(request.getContextPath());
	}

	public static String getProjectName(String contextPath){
		if(Str.isNullOrEmpty(contextPath)){
			return null;
		}
		return contextPath.substring(1);
	}

	protected Err err=null;
	/***
	 * Get last error message from populate http parameters to bean
	 * By GuRui on 2015-12-5 上午11:34:08
	 * @return
	 */
	public Err getErr(){
		return this.err;
	}

	public Err getErr(String msg){
		if(this.err==null){
			return null;
		}else{
			this.err.getErr().setMsg(msg);
			return this.err;
		}
	}
	/***
	 * 此方法为推荐方法。从HttpRequest中解析指定的bean。如果成功返回true，如果失败，应用this.getErr()方法获取错误信息并返回客户端。
	 * 错误信息内包含一个所有出错字段的"键-值"列表.
	 * By GuRui on 2015-8-27 下午5:11:37
	 * Usage:
	 * <pre>
	 * Bean bean=new Bean; //Bean extends IParameter
	 * if(!parseBeanFromRequest(bean, request)){
	 * 		return this.getErr();
	 * }
	 * </pre>
	 * @param bean 待填充的bean
	 * @param request 字段验证类所在的包
	 * @return true if success
	 */
	protected boolean parseBeanFromRequest(IParameter bean,HttpServletRequest request){
		return this.parseBeanFromRequest(bean, request, false);
	}

	/***
	 * 此方法为推荐方法。从HttpRequest中解析指定的bean。如果成功返回true，如果失败，应用this.getErr()方法获取错误信息并返回客户端。
	 * 错误信息内包含一个所有出错字段的"键-值"列表.
	 * Usage:
	 * <pre>
	 * Bean bean=new Bean; //Bean extends IParameter
	 * if(!parseBeanFromRequest(bean, request, [FUP_VALIDATION_PACKAGE_NAME])){
	 * 		return this.getErr();
	 * }
	 * </pre>
	 * @param bean 待填充的bean
	 * @param request 
	 * @param validationPackage 字段验证类所在的包
	 * @return true if success
	 */
	protected boolean parseBeanFromRequestAndValidate(IParameter bean,HttpServletRequest request, String validationPackage){
		return this.parseBeanFromRequestWithValidateOption(bean, request, false, true, validationPackage);
	}

	protected boolean parseBeanFromRequestAndValidate(IParameter bean,HttpServletRequest request){
		return this.parseBeanFromRequestWithValidateOption(bean, request, false, true, ConfigBase.FIELD_VALIDATION_PACKAGE_NAME);
	}

	protected boolean parseBeanFromRequest(IParameter bean,HttpServletRequest request, boolean convertStringArrayToString){
		return parseBeanFromRequestWithValidateOption(bean,request, convertStringArrayToString, false, ConfigBase.FIELD_VALIDATION_PACKAGE_NAME);
	}

	/***
	 * 从http请求中解析参数，可能自动执行对所有参数的验证
	 * By GuRui on 2015-12-8 下午9:46:37
	 * @param bean 用于承载结果的bean
	 * @param request HttpServletRequest
	 * @param convertStringArrayToString 多条错误信息是否格式化为一个字符串，默认否
	 * @param autoValidate 是否在参数解析后自动开始验证，默认否
	 * @param validationPackgeName 参数验证时所用到的验证类所在包名。若autoValidate为是，则此项必填
	 * @return
	 */
	protected boolean parseBeanFromRequestWithValidateOption(IParameter bean,HttpServletRequest request, boolean convertStringArrayToString, boolean autoValidate, String validationPackgeName){
		List<KeyValuePair> badPairs=this.populateBeanFromRequest(bean, request, convertStringArrayToString);
		if(badPairs.size()>0){
			this.err=Err.MAPPING_ERROR(badPairs);
			return false;
		}

		if(autoValidate){
			Err err=Validator.validateBean(bean, validationPackgeName);
			if(err!=null){
				this.err=err;
				return false;
			}
		}
		
		if(bean instanceof IRelationValidation){
			IRelationValidation rb=(IRelationValidation)bean;
			if(!rb.validate()){
				this.err=Err.RELATION_ERROR(rb.getLastErrMsg());
				return false;
			}
		}

		return true;
	}
	
	/***
	 * This is used to replace the old populateRequestToBean() method
	 * to solve the problem of populate populating illegal string to integer or other data type.
	 * By GuRui on 2015-8-8 上午3:01:45
	 * Usage:
	 * <pre>
	 * Bean bean=new Bean; //Bean extends IParameter
	 * List<KeyValuePair> errs= populateBeanFromRequest(bean, request);
	 * if(errs.size()>0){
	 * 		return Err.MAPPING_ERROR(errs);
	 * }
	 * </pre>
	 * @param request HttpServletRequest
	 * @param bean bean to populate
	 * @return Never return null. Return all name:val[0] pairs that failed to map. Or zero length List<KeyValuePair> if successful.
	 */
	protected List<KeyValuePair> populateBeanFromRequest(IParameter bean,HttpServletRequest request){
		return this.populateBeanFromRequest(bean, request,false);
	}

	protected List<KeyValuePair> populateBeanFromRequest(IParameter bean,HttpServletRequest request,boolean convertStringArrayToString){
		ParameterMapper mapper=new ParameterMapper(convertStringArrayToString);
		return mapper.populate(bean, request.getParameterMap());
	}

	protected static boolean isNullOrEmpty(String src) {
		return src == null || src.isEmpty();
	}

	protected void redirectError(HttpServletRequest request, HttpServletResponse response, String msg){
		try {
			//request.getRequestDispatcher(request.getContextPath() + ConfigBase.PAGE_ERROR + "?msg="+URLEncoder.encode(msg==null?"重定向到错误页面时发生错误":msg, "UTF-8"));
			response.sendRedirect(request.getContextPath() + ConfigBase.PAGE_ERROR + "?msg="+URLEncoder.encode(msg==null?"重定向到错误页面时发生错误":msg, "UTF-8"));
			//response.sendRedirect(request.getContextPath() + ConfigBase.PAGE_ERROR + "?msg="+URLEncoder.encode(msg==null?"重定向到错误页面时发生错误":msg, "ISO-8859-1"));
		} catch (IOException e) {
			this.logger.error(e.getMessage());
		}
	}
	
	/***
	 * 此方法为推荐方法。从HttpRequest中解析名为data的Jso到指定类型bean。如果成功返回该类型对象，如果失败，则返回null，随后可用this.getErr()方法获取错误信息。
	 * Usage:
	 * <pre>
	 * Bean bean=parseDataFromRequestAndValidate(Bean.class, request);
	 * if(bean==null){
	 * 		return this.getErr();
	 * }
	 * </pre>
	 * @param clazz 待返回的bean的类型
	 * @param request 
	 * @return 出错返回null，需要用this.getErr()方法获得具体错误信息
	 */
	protected  <T extends IParameter> T parseDataFromRequestAndValidate(Class<T> clazz,HttpServletRequest request){
		return this.parseDataFromRequestAndValidate(clazz, request,null);
	}
	
	/***
	 * 此方法为推荐方法。从HttpRequest中解析名为data的Jso到指定类型bean。如果成功返回该类型对象，如果失败，则返回null，随后可用this.getErr()方法获取错误信息。
	 * Usage:
	 * <pre>
	 * Bean bean=parseDataFromRequestAndValidate(Bean.class, request,  [FUP_VALIDATION_PACKAGE_NAME]);
	 * if(bean==null){
	 * 		return this.getErr();
	 * }
	 * </pre>
	 * @param clazz 待返回的bean的类型
	 * @param request 
	 * @param validationPackage 字段验证类所在的包，如果为空则用ConfigBase里的默认参数代替
	 * @return 出错返回null，需要用this.getErr()方法获得具体错误信息
	 */
	protected  <T extends IParameter> T parseDataFromRequestAndValidate(Class<T> clazz,HttpServletRequest request, String validationPackageName){
		T bean=null;	
		try {
			bean = JsonTool.getData(request, clazz);
		} catch (JsonParseBadFieldException e) {
			this.err = new Err("[" + DictBase.getFieldNameCn(e.getBadFieldName()) +"]格式错误");
			return null;
		}
		
		if(bean == null){
			this.err = new Err("请求参数错误。清注意必须用必须包含一个名为'"+DictBase.TAG_HTTP_REQUEST_DATA+"'的Json对象参数");
			return null;
		}
		String packPath = Str.isNullOrEmpty(validationPackageName)? ConfigBase.FIELD_VALIDATION_PACKAGE_NAME :validationPackageName;
		Err err = JsonTool.validateData(bean, packPath);
		if(err==null){
			return bean;
		}
		
		this.err=err;			
		return null;
	}

}

