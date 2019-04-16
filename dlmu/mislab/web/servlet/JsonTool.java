// By GuRui on 2017-7-3 下午3:44:08
package dlmu.mislab.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.common.DictBase;
import dlmu.mislab.tool.JsonParseBadFieldException;
import dlmu.mislab.tool.Str;
import dlmu.mislab.tool.jn;
import dlmu.mislab.validation.Validator;
import dlmu.mislab.web.interact.IParameter;
import dlmu.mislab.web.response.Err;

public final class JsonTool {

	/***
	 * 从request中解析名为data的数据为指定IParameter类型的对象。
	 * 如果json为null或格式有问题返回null，否则返回一个非空对象，但其属性全为null
	 * @param <T>
	 * @param <V>
	 * @param request HttpServletRequest
	 * @param clazz 希望返回的对象类型
	 * @return 希望返回的对象。出错返回null
	 */
	public static <T extends IParameter> T getData(HttpServletRequest request, Class<T> clazz) throws JsonParseBadFieldException{
		String sdata=request.getParameter(DictBase.TAG_HTTP_REQUEST_DATA);
		return jn.fromJson(sdata, clazz);
	}
	
	@Deprecated
	/***
	 * 从request中解析名为data的数据为指定IParameter类型的对象。
	 * 如果json为null或格式有问题返回null，否则返回一个非空对象，但其属性全为null
	 * @param datePattern 日期的解析格式，默认为"yyyy-MM-dd hh:mm:ss"
	 * @param request HttpServletRequest
	 * @param clazz 希望返回的对象类型
	 * @return 希望返回的对象
	 */
	public static <T extends IParameter> T getData(String datePattern, HttpServletRequest request, Class<T> clazz){
		String sdata=request.getParameter(DictBase.TAG_HTTP_REQUEST_DATA);
		if(Str.isNullOrEmpty(sdata)){
			return null;
		}
		return jn.fromJson(sdata, clazz);
	}
	
	/***
	 * 校验Bean的所有字段，无错返回null，否则错误信息包含在Err中
	 * @param bean
	 * @return
	 */
	public static Err validateData(IParameter bean){
		return JsonTool.validateData(bean, ConfigBase.FIELD_VALIDATION_PACKAGE_NAME);
	}
	
	/***
	 * 校验Bean的所有字段，无错返回null，否则错误信息包含在Err中
	 * @param bean
	 * @param validationPackageName 校验类文件的位置
	 * @return
	 */
	public static Err validateData(IParameter bean, String validationPackageName){
		if(bean==null){
			return new Err("待检验内容为空。请检查是否从是否提交data字段");
		}
		Err err=Validator.validateBean(bean, validationPackageName);
		return err;
	}
	
	public static Byte[] getObject(HttpServletRequest request){
		String sobj= request.getParameter(DictBase.TAG_HTTP_REQUEST_OBJECT);
		if(Str.isNullOrEmpty(sobj)){
			return null;
		}
		throw new RuntimeException("TODO");
	}
	
	
	/***
	 * 将Java对象输出到response流中
	 * @param request
	 * @param response
	 * @param that
	 * @param doPost
	 * @throws ServletException
	 * @throws IOException
	 */
	public static void toJson(HttpServletRequest request, HttpServletResponse response, IHttpJson that, boolean doPost) throws ServletException, IOException{
		jn.toJson(doPost?that.doJsonPost(request, response):that.doJsonGet(request, response),response.getWriter());
	}

}
