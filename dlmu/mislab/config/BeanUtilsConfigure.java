package dlmu.mislab.config;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;

import dlmu.mislab.tool.DateTool;

/***
 * 为从parameter获得参数转化为bean而作的配置。主要内容是string 到java.util.Date类型的转化
 * @author GuRui
 * 
 */
public class BeanUtilsConfigure {
	private static boolean INITIALIZED = false;

	private static final ConvertUtilsBean converter = BeanUtilsBean.getInstance().getConvertUtils();

	public static void init() {
		if (INITIALIZED) {
			return;
		}
		doInit();
		INITIALIZED = true;
	}
	
	public static ConvertUtilsBean getConverter(){
		if(!INITIALIZED){
			init();
		}
		return converter;
	}

	private static void doInit() {
		converter.register(new Converter() {
			@SuppressWarnings("rawtypes")
			@Override
			public Object convert(Class arg0, Object arg1) {
				if(arg1 == null){
					return null;
				}
				if(arg1 instanceof java.util.Date){
					return arg1;
				}
				else if(arg1 instanceof String){
					return DateTool.strToDateOrTime((String) arg1);
				}else if(arg1 instanceof java.sql.Date){
					return new java.util.Date(((java.sql.Date)arg1).getTime());
				}else if (arg1 instanceof java.sql.Time){
					return new java.util.Date(((java.sql.Time)arg1).getTime());
				}else if(arg1 instanceof java.sql.Timestamp){
					return new java.util.Date(((java.sql.Timestamp)arg1).getTime());
				}
				throw new ConversionException("未知类型，无法转换为java.util.Date");
			}

		}, java.util.Date.class);

	}
}
