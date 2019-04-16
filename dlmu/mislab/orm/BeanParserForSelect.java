// By GuRui on 2017-6-19 下午3:20:06
package dlmu.mislab.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import dlmu.mislab.orm.annotation.AnnotationException;
import dlmu.mislab.orm.annotation.IsFakeKey;
import dlmu.mislab.orm.annotation.IsFnKey;
import dlmu.mislab.orm.annotation.IsKey;
import dlmu.mislab.orm.annotation.RefTo;
import dlmu.mislab.orm.annotation.Table;
import dlmu.mislab.tool.Reflect;
import dlmu.mislab.tool.Str;

public class BeanParserForSelect {
	static SelectCommand generateSelectCmd(IOrmBean bean){
		ParsedBeanInfoForSelect pb = BeanParserForSelect.parseBean(bean);
		CmdDescriptorForSelect cds=BeanParserForSelect.prepareSelectDescriptor(pb);
		SelectCommand rtn = new SelectCmdGenerator().prepareSelectCmd(cds);
		
		return rtn;
	}
	
	static CmdDescriptorForSelect prepareSelectDescriptor(ParsedBeanInfoForSelect beanInfo){
		CmdDescriptorForSelect rtn=new CmdDescriptorForSelect();
		rtn.tableName=beanInfo.tableName;
		for(BeanFieldForSelect bf: beanInfo.fields){
			if(!bf.isRefToChild){
				rtn.targetPart.add(bf);
			}
			
			if((bf.isKey || bf.isFakeKey) && !bf.auto){
				rtn.wherePart.add(bf);
			}
		}
		return rtn;
	}
	
	
	/***
	 * 解析IOrmBean对象，将其中的Annotation全部解析为属性，子表递归解析
	 * @param bean 一个IOrmBean类型的实例
	 * @return 解析后的中间对象，用于进一步处理，包含子表的树形结构
	 */
	static <T extends IOrmBean> ParsedBeanInfoForSelect parseBean(T bean){
		if(bean==null){
			throw new AnnotationException("IOrmBean不可为空");
		}

		ParsedBeanInfoForSelect rtn=new ParsedBeanInfoForSelect();	
		Class<? extends IOrmBean> cls=bean.getClass();
		
		String beanTableName=null;
		if(cls.isAnnotationPresent(Table.class)){
			beanTableName=cls.getAnnotation(Table.class).Name();
			if(Str.isNullOrEmpty(beanTableName)){
				throw new AnnotationException("IOrmBean类型的Model的@Table标记的Name不可为空");
			}
		}else{
			throw new AnnotationException("IOrmBean类型的Model必须标记@Table");
		}
		
		rtn.tableName=beanTableName;
					
		List<Field> fs= Reflect.getFieldsUpTo(cls);
		try{
			for(Field f:fs){
				if((f.getModifiers() &  Modifier.TRANSIENT)== Modifier.TRANSIENT){
					continue;
				}
				
				BeanFieldForSelect bf=parseFieldAnnotation(f,beanTableName, bean);
				
				rtn.fields.add(bf);
				
				if(bf.isRefToChild){
					if(rtn.childTypes==null){
						rtn.childTypes=new LinkedList<Class<?>>();
					}
					rtn.childTypes.add(bf.childType);
				}
			}	
		}catch(AnnotationException e){
			throw e;
		} catch (Throwable e) {
			throw new AnnotationException("解析Bean的Annotation时发生错误：" + e.getMessage());
		}
		return rtn;
	}
	
	/***
	 * 解析每个字段的Annotation
	 * @param f
	 * @param beanTableName
	 * @param bean
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private static <T extends IOrmBean> BeanFieldForSelect parseFieldAnnotation(Field f, String beanTableName,T bean) throws IllegalArgumentException, IllegalAccessException{
		BeanFieldForSelect bf=new BeanFieldForSelect();
		f.setAccessible(true);
		bf.fieldName=f.getName();
		bf.fieldValue=f.get(bean);
		
		if(f.isAnnotationPresent(IsKey.class)){
			bf.isKey=true;
			bf.auto = f.getAnnotation(IsKey.class).Auto();
		}else if(f.isAnnotationPresent(IsFakeKey.class)){
			if(bf.isKey){
				throw new AnnotationException("IsKey和IsFakeKey标记不可同时出现在同一字段上");
			}
			bf.isFakeKey=true;
		}
				
		if(f.isAnnotationPresent(IsFnKey.class)){
			bf.isFnKey=true;
			String fkname=f.getAnnotation(IsFnKey.class).FnKeyName();
			bf.fnKeyName = "".equals(fkname)?bf.fieldName:fkname;
			bf.fnTableName = f.getAnnotation(IsFnKey.class).FnTableName();
		}
		
		
		if(f.isAnnotationPresent(RefTo.class)){
			if(bf.isKey || bf.isFakeKey){
				throw new AnnotationException("IsAuto, IsKey, IsFakeKey标记不可出现在RefTo字段上");
			}
			bf.isRefToChild=true;
			
			if(f.getType().isArray()){
				bf.isArray=true;
				@SuppressWarnings("unchecked")
				Class<T> x=(Class<T>) f.getType().getComponentType();
				//Class<? extends IOrmBean[]> y= (Class<? extends IOrmBean[]>) f.getType();
				if(x==null){
					throw new AnnotationException("解析RefTo字段对应的自对象类型失败。请确保其类型为IOrmBean的子类型的数组");
				}
				bf.childType=x;				
			}else{
				bf.childType = f.getType();
			}
		}

		return bf;
	}
}
