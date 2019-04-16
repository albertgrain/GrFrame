package dlmu.mislab.orm.bean;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import dlmu.mislab.orm.OrmCommand;
import dlmu.mislab.orm.IOrmBean;
import dlmu.mislab.orm.annotation.AnnotationException;
import dlmu.mislab.orm.annotation.CareNullValue;
import dlmu.mislab.orm.annotation.IsAuto;
import dlmu.mislab.orm.annotation.IsFakeKey;
import dlmu.mislab.orm.annotation.IsKey;
import dlmu.mislab.orm.annotation.RefTo;
import dlmu.mislab.orm.annotation.Table;
import dlmu.mislab.tool.Str;

/***
 * 解析IOrmBean对象，根据其Annotation和值生成SQL命令对象
 * 注意：此类仅为Update, Delete, Insert服务。 Select清调用BeanParserForSelect类的方法
 */
public class BeanParser {
	/***
	 * 解析IOrmBean对象，根据其Annotation和值生成SQL命令对象
	 * @param bean IOrmBean类型的一个对象
	 * @param generator 生成器。可以是InsertCmdGenerator、DeleteCmdGenerator、UpdateCmdGenerator、SelectCmdGenerator之一
	 * @return
	 */
	public static List<OrmCommand> generateCmds(IOrmBean bean, ICmdGenerator generator){
		return BeanParser.generateCmds(bean, generator, true);
	}
	
	/***
	 * 
	 * 解析IOrmBean对象，根据其Annotation和值生成SQL命令对象
	 * @param bean IOrmBean类型的一个对象
	 * @param generator 生成器。可以是InsertCmdGenerator、DeleteCmdGenerator、UpdateCmdGenerator、SelectCmdGenerator之一
	 * @param negelectNull 是否忽略空值，默认为true
	 * @return
	 */
	public static List<OrmCommand> generateCmds(IOrmBean bean, ICmdGenerator generator, boolean negelectNull){
		ParsedBeanInfo beanInfo = parseBean(bean);
		if(beanInfo==null || beanInfo.fields==null || beanInfo.fields.size()==0 || Str.isNullOrEmpty(beanInfo.tableName)){
			throw new AnnotationException("Bean解析失败");
		}
		LinkedList<CmdDescriptor> pbds = new LinkedList<CmdDescriptor>();
		BeanParser.prepareDescriptor(pbds, beanInfo);
		
		Iterator<CmdDescriptor> itr=null;
		//if(generator instanceof DeleteCmdGenerator || generator instanceof SelectCmdGenerator){
		if(generator instanceof DeleteCmdGenerator){
			itr=pbds.descendingIterator();
		}else{
			itr=pbds.iterator();
		}
		
		List<OrmCommand> cmdBeans = new LinkedList<OrmCommand>();
		
		while(itr.hasNext()){
			OrmCommand cmdBean = generator.prepareCmd(itr.next(), negelectNull);
			if(cmdBean!=null){
				cmdBeans.add(cmdBean);
			}
		}
		return cmdBeans;
	}
	
	/***
	 * Get all fields, including inherited from a type.
	 * @param fields an list containing the result
	 * @param type the class to be parsed
	 * @return the same passed in by parameter
	 */
	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
	    fields.addAll(Arrays.asList(type.getDeclaredFields()));
	    if (type.getSuperclass() != null) {
	        getAllFields(fields, type.getSuperclass());
	    }
	    return fields;
	}
	
	/***
	 * 解析IOrmBean对象，将其中的Annotation全部解析为属性，子表递归解析
	 * @param bean 一个IOrmBean类型的实例
	 * @return 解析后的中间对象，用于进一步处理，包含子表的树形结构
	 */
	private static ParsedBeanInfo parseBean(IOrmBean bean){
		if(bean==null){
			throw new AnnotationException("IOrmBean不可为空");
		}

		ParsedBeanInfo rtn=new ParsedBeanInfo();	
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
		
		//Field[] fs=cls.getDeclaredFields();
		//Changed by GR:2019-3-8: to get all inherited fields
		List<Field> fs = new ArrayList<Field>();
		fs = getAllFields(fs, cls);
		
		try{
			for(Field f:fs){
				if((f.getModifiers() &  Modifier.TRANSIENT)== Modifier.TRANSIENT){
					continue;
				}
				
				BeanField bf=parseFieldAnnotation(f, bean);
				
				rtn.fields.add(bf);
				
				if(bf.children!=null && bf.isRefToChild){
					for(IOrmBean b: bf.children){
						if(b!=null){ //If null, neglect this property
							ParsedBeanInfo bi=BeanParser.parseBean(b);
							rtn.childTables.add(bi);
						}
					}
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
	private static  BeanField parseFieldAnnotation(Field f, IOrmBean bean) throws IllegalArgumentException, IllegalAccessException{
		BeanField bf=new BeanField();
		f.setAccessible(true);
		bf.fieldName=f.getName();
		bf.fieldValue=f.get(bean);
		
		if(f.isAnnotationPresent(IsKey.class)){
			bf.isKey=true;
		}else if(f.isAnnotationPresent(IsFakeKey.class)){
			if(bf.isKey){
				throw new AnnotationException("IsKey和IsFakeKey标记不可同时出现在同一字段上");
			}
			bf.isFakeKey=true;
		}
				
		if(f.isAnnotationPresent(IsAuto.class)){
			bf.isAuto=true;
		}
		
		if(f.isAnnotationPresent(CareNullValue.class)){
			bf.neglectNull=false;
		}
		
		if(f.isAnnotationPresent(RefTo.class)){
			if(bf.isAuto || bf.isKey || bf.isFakeKey){
				throw new AnnotationException("IsAuto, IsKey, IsFakeKey标记不可出现在RefTo字段上");
			}
			bf.isRefToChild=true;
			
			if(f.getType().isArray()){
				if(bf.fieldValue!=null){				
					int len=Array.getLength(bf.fieldValue);
					bf.children=new IOrmBean[len];
					for(int i=0; i<len; i++){
						Object obj=Array.get(bf.fieldValue, i);
						bf.children[i]=(IOrmBean)obj;
					}
				}
			}else{
				bf.children = new IOrmBean[1];
				bf.children[0]=(IOrmBean)bf.fieldValue;
			}
		}

		return bf;
	}
	
	/***
	 * 1、将解析出来的信息转化为适合SQL的描述
	 * 2、完成子表的递归描述，将树形的Bean信息转化为SQL描述列表
	 * @param rtn
	 * @param beanInfo
	 */
	private static void prepareDescriptor(List<CmdDescriptor> rtn, ParsedBeanInfo beanInfo){
		CmdDescriptor cds=new CmdDescriptor();
		cds.tableName=beanInfo.tableName;
		for(BeanField bf: beanInfo.fields){
			cds.targetPart.add(bf);
			if(bf.isKey || bf.isFakeKey){
				cds.wherePart.add(bf);
			}
		}
		for(ParsedBeanInfo bi: beanInfo.childTables){
			
			prepareDescriptor(rtn, bi);
		}
		rtn.add(cds);
	}

}
