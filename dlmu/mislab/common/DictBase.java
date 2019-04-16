package dlmu.mislab.common;

import java.util.Hashtable;

import dlmu.mislab.tool.Str;

public abstract class DictBase {
	public static final String VALIDATOR_CONSTRUCTING_ERROR_MSG="验证输入字段构造验证对象时出错";
	public static final String HTPP_PARAMETERS_PARSING_FAILED="从客户端请求中解析参数失败";
	
	/**
	 * 保存英文和中文字段名对照表
	 */
	public static final Hashtable<String,String> dict=new Hashtable<String,String>();

	public static final String TAG_USER_INFO="userinfo";

	public static final String DEFAULT_OWNER_ID="1";

	//For servlet: JsonBase
	public static final String TAG_HTTP_REQUEST_DATA = "data";
	public static final String TAG_HTTP_REQUEST_OBJECT = "obj";
	
	///////// FileUpload ////////////////////

	//	static{
	//		/* add other tag names */
	//		//dict.put(TAG_DOWNLOAD_FILE_NO, TAG_DOWNLOAD_FILE_NO_CN);
	//
	//	}
	
	/**
	 * 根据英文字段名返回中文字段名。如果找不到则返回输入的英文字段名。如果输入null或空字符串，则返回空字符串
	 * @param fieldName
	 * @return 不会返回空值
	 */
	public static String getFieldNameCn(String fieldName){
		if(Str.isNullOrEmpty(fieldName)){
			return "";
		}
		String rtn  = DictBase.dict.get(fieldName);
		if(Str.isNullOrEmpty(rtn)){
			return fieldName;
		}else{
			return rtn;
		}
	}
}
