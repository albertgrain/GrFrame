// By GuRui on 2015-8-8 上午2:32:32
package dlmu.mislab.web.servlet;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.common.KeyValuePair;
import dlmu.mislab.config.BeanUtilsConfigure;
import dlmu.mislab.tool.Reflect;
import dlmu.mislab.tool.Str;
import dlmu.mislab.web.interact.IParameter;

public class ParameterMapper {
	private boolean convertStringArrayToString=false;
	public ParameterMapper(boolean convertStringArrayToString){
		this.convertStringArrayToString=convertStringArrayToString;
	}
	public ParameterMapper(){
		
	}
	/**
	 * Return all name:val[0] pairs that failed to map. Or zero length List<KeyValuePair> if successful. Never return null.
	 * By GuRui on 2015-8-8 上午2:34:40
	 * @param bean
	 * @param map
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public List<KeyValuePair> populate(IParameter bean, Map<String, String[]> map) {
		List<Field> fields = Reflect.getFieldsUpTo(bean.getClass());
		List<KeyValuePair> rtn=new LinkedList<KeyValuePair>();
		for(Field fld: fields){
			String nm=fld.getName();
			if (map.containsKey(nm)) {
				String[] strs=map.get(nm);
				if(strs!=null && !Str.isNullOrEmpty(strs[0])){
					if(strs.length>1){
						try{
							if(this.convertStringArrayToString){
								int size=4096*strs.length;
								if(size>ConfigBase.MAX_HTTP_POST_SIZE){
									rtn.add(new KeyValuePair(nm,"Field should not larger than "+ ConfigBase.MAX_HTTP_POST_SIZE + " bytes."));
									continue;
								}
								StringBuilder buf=new StringBuilder(size);
								for(int i=0;i<strs.length;i++){
									if(strs[i]!=null){
										buf.append(strs[i]);
									}
								}
								BeanUtils.setProperty(bean, nm, buf.toString());
							}else{
								Object val=BeanUtilsConfigure.getConverter().convert(strs,fld.getClass());
								if(val!=null){ //reserve the original value in bean
									BeanUtils.setProperty(bean, nm, val);
								}
							}
						}catch(Exception e){
							rtn.add(new KeyValuePair(nm,strs[0]));
						}
					}else{
						try{
							Object val=BeanUtilsConfigure.getConverter().convert(strs[0],fld.getClass());
							if(val!=null){
								BeanUtils.setProperty(bean, nm, val);
							}
						}catch(Exception e){
							rtn.add(new KeyValuePair(nm,strs[0]));
						}
					}//End of if(strs.length>1)
				}//End of if(strs!=null && strs[0]!=null)
			}//End of if (map.containsKey(nm))
		}//End of for
		return rtn;
	}
}
