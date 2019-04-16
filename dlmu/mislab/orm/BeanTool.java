package dlmu.mislab.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.TreeSet;
import dlmu.mislab.orm.annotation.AffectRow;
import dlmu.mislab.orm.annotation.CareNullValue;
import dlmu.mislab.orm.annotation.IsFakeKey;
import dlmu.mislab.orm.annotation.IsKey;
import dlmu.mislab.orm.annotation.RefTo;
import dlmu.mislab.orm.annotation.Table;
import dlmu.mislab.orm.annotation.AffectRow.AffectRowType;
import dlmu.mislab.tool.Reflect;
import dlmu.mislab.tool.Str;

class BeanTool {
	static enum GenerateCommandType{
		INSERT,
		DELETE,
		UPDATE
	}
	
	static class ParsedFieldDescriptor{
		static final int TYPE_DEFAULT=0x0;
		static final int TYPE_PRIMARY_KEY=0x1;
		static final int TYPE_FOREIGN_KEY=0x2;
		static final int TYPE_NEGLECTABLE=0x4;
		static final int TYPE_FAKE_KEY=0x8;
		
		String name;
		Object value;
		int fieldType;

		void setFieldType(int flag){
			this.fieldType |= flag;
		}
		
		boolean isPrimaryKey(){
			return (this.fieldType & TYPE_PRIMARY_KEY)==TYPE_PRIMARY_KEY;
		}
		
		boolean isForeignKey(){
			return (this.fieldType & TYPE_FOREIGN_KEY)==TYPE_FOREIGN_KEY;
		}
		
		boolean isNeglectable(){
			return (this.fieldType & TYPE_NEGLECTABLE)==TYPE_NEGLECTABLE;
		}
		
		boolean isFakeKey(){
			return (this.fieldType & TYPE_FAKE_KEY)==TYPE_FAKE_KEY;
		}
	}
	
	static class ParsedChildDescriptor{
		String fieldNameOfChildBean;
		Class<? extends IOrmBean> classOfChildBean;
		String[] foreignKeysInParent;
		Object[] foreignKeyValuesInParent;
		String[] KeysInChild;
	}
	
	static class ParsedBeanDescriptor{
		final LinkedList<ParsedFieldDescriptor> fieldPart=new LinkedList<ParsedFieldDescriptor>();
		final LinkedList<ParsedFieldDescriptor> wherePart=new LinkedList<ParsedFieldDescriptor>();
		final LinkedList<ParsedChildDescriptor> childTables=new LinkedList<ParsedChildDescriptor>();
		String tableName;
		boolean neglectNullValue=true;
		AffectRowType affectRow=AffectRowType.NO_MORE_THAN_ONE;
	}
	
	static class BeanField implements Comparable<BeanField>{
		String fieldName;
		String tableName;
		Object fieldValue;
		boolean isKey=false;   
		boolean isFKey=false;  
		boolean neglect=false;  
		@Override
		public int compareTo(BeanField o) {
			return this.tableName.compareTo(o.tableName);
		}

	}

	static class ParseBeanReturn{
		LinkedList<BeanField> fieldPart;
		LinkedList<BeanField> wherePart;
		TreeSet<String> otherTables;
		String mainTable;
		ReturnFieldDescription returnField = new ReturnFieldDescription(); //用于insertThenGetKey，用于在插入后获得自增长主键值
		public ParseBeanReturn(){
			fieldPart = new LinkedList<BeanField>();
			wherePart = new LinkedList<BeanField>();
			otherTables = new TreeSet<String>();
			mainTable=null;
		}
	}
	
	//////////////// End of static classes /////////////////
	
	
	/***
	 * Parse the field name, field value, and field annotation(table name) and store it in a private class.
	 * If a field were marked with Table annotation, then it is an exception of annotation TableClass
	 * A BeanField in wherePart:
	 *       if it isKey, then it should be assigned value in where clause;
	 *       if it isFKey, then it should be used as a connection between main and reference table (Without assigning value)
	 * PLEASE NOET:
	 * In one bean, there are at most two levels of tables are allowed to be annotated.
	 * One is the base level defined by @TableClass at bean class, the other is reference level defined by @Table at bean field.
	 * By GuRui on 2015-12-5 下午2:18:22
	 *
	 */
//	static ParseBeanReturn parseBean(IOrmBean bean){
//		ParseBeanReturn rtn=new ParseBeanReturn();
//
//		if(bean==null){
//			return rtn;
//		}
//
//		String beanTableName=null;
//		Class<? extends IOrmBean> cls=bean.getClass();
//		if(bean.getClass().isAnnotationPresent(Table.class)){
//			beanTableName=cls.getAnnotation(Table.class).Name();
//			if(Str.isNullOrEmpty(beanTableName)){
//				throw new OrmException("IOrmBean类型的Model的@TableClass标记的TableName不可为空");
//			}
//		}else{
//			throw new OrmException("IOrmBean类型的Model必须标记@TableClass");
//		}
//		rtn.mainTable=beanTableName;
//		Field[] fs=cls.getDeclaredFields();
//		sortFieldsByName(fs);
//		for(Field f:fs){
//			f.setAccessible(true);
//			try {
//				BeanField bf=new BeanField();
//				bf.fieldName=f.getName();
//				bf.fieldValue=f.get(bean);
//
//				if(f.isAnnotationPresent(IsKey.class)){
//					bf.isKey=true;
//					BeanField bfw=new BeanField();
//					bfw.fieldName=f.getName();
//					bfw.fieldValue=f.get(bean);
//					bfw.tableName=beanTableName;
//					bfw.isKey=true;
//					rtn.wherePart.addLast(bfw);
//				}
//
//				if(f.isAnnotationPresent(Table.class)){
//					String tbname=f.getAnnotation(Table.class).Name();
//					rtn.otherTables.add(tbname);
//					if(f.isAnnotationPresent(RefTo.class)){
//						bf.isFKey=true;
//						bf.tableName=beanTableName;
//
//						BeanField bfw=new BeanField();
//						bfw.tableName=tbname;
//						bfw.fieldName=f.getName();
//						bfw.fieldValue=f.get(bean);
//						bfw.isFKey=true;
//						rtn.wherePart.addLast(bfw);
//					}else{
//						bf.tableName=f.getAnnotation(Table.class).Name();
//					}
//				}else{
//					bf.tableName=beanTableName;
//				}
//				
//				if(f.isAnnotationPresent(IsAuto.class)){
//					rtn.returnField = new ReturnFieldDescription(f.getName(), f.getType());
//				}
//
//				rtn.fieldPart.addLast(bf);
//
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			}
//		}
//		return rtn;
//	}
	
	
	/***
	 * Parse IOrmBean object and arrange all fields by annotations and modifiers.
	 * Workable annotations for bean: TableClass, AffectRow, ConsdierNullValue
	 * Workable annotations for field: IsKey, ChildTable
	 * Workable modifiers: transient
	 * By GuRui on 2016-9-1 下午11:06:34
	 * @param bean
	 * @return
	 */
	static ParsedBeanDescriptor parseBean2(IOrmBean bean){
		ParsedBeanDescriptor rtn=new ParsedBeanDescriptor();
		reflectParseBeanAnnotation(bean.getClass(), rtn);
		reflectParseFieldAnnotationAndModifiers(bean, rtn);
		attachValuesToForignKeyFields(rtn);
		return rtn;
	}
	
	private static void attachValuesToForignKeyFields(ParsedBeanDescriptor pc){
		for(ParsedChildDescriptor cc : pc.childTables){
			cc.foreignKeyValuesInParent=new Object[cc.foreignKeysInParent.length];
			for(int i=0;i<cc.foreignKeysInParent.length;i++){
				for(ParsedFieldDescriptor pf: pc.fieldPart){
					if(pf.name.equals(cc.foreignKeysInParent[i])){
						cc.foreignKeyValuesInParent[i]=pf.value;
						break;
					}
				}
			}
		}
	}
	
	private static void reflectParseBeanAnnotation(Class<? extends IOrmBean> cls, ParsedBeanDescriptor rtn){
		if(cls.isAnnotationPresent(CareNullValue.class)){
			rtn.neglectNullValue=false;
		}
		if(cls.isAnnotationPresent(AffectRow.class)){
			rtn.affectRow=cls.getAnnotation(AffectRow.class).Affect();
		}
		
		if(cls.isAnnotationPresent(Table.class)){
			rtn.tableName=cls.getAnnotation(Table.class).Name();
			
			if(Str.isNullOrEmpty(rtn.tableName)){
				throw new OrmException("IOrmBean类型的Model的@TableClass标记的TableName不可为空");
			}
		}else{
			throw new OrmException("IOrmBean类型的Model必须标记@TableClass");
		}
	}
	
	private static void reflectParseFieldAnnotationAndModifiers(IOrmBean bean, ParsedBeanDescriptor rtn){
		Field[] fs=bean.getClass().getDeclaredFields();
		for(Field f:fs){
			if((f.getModifiers() &  Modifier.TRANSIENT)== Modifier.TRANSIENT){
				continue;
			}
			f.setAccessible(true);
			try {
				ParsedFieldDescriptor pf=new ParsedFieldDescriptor();
				pf.name=f.getName();
				if(f.isAnnotationPresent(RefTo.class)){
					pf.setFieldType(ParsedFieldDescriptor.TYPE_NEGLECTABLE);
					ParsedChildDescriptor cc=new ParsedChildDescriptor();
					cc.fieldNameOfChildBean=f.getName();

					@SuppressWarnings("unchecked")
					Class<? extends IOrmBean> childClass = (Class<? extends IOrmBean>) Reflect.getGernericTypeOfAList(f);
			        if(childClass==null){
			        	throw new OrmException("子Bean类型不正确，请确认其实现IOrmBean接口");
			        }
			        cc.classOfChildBean=childClass;
			        
//					cc.foreignKeysInParent=new String[]{f.getAnnotation(RefTo.class).ForeignKey()};
//					cc.KeysInChild=new String[]{f.getAnnotation(RefTo.class).RefKey()};

					
					rtn.childTables.addLast(cc);
					continue;
				}
				pf.value=f.get(bean);

				if(f.isAnnotationPresent(IsKey.class)){
					pf.setFieldType(ParsedFieldDescriptor.TYPE_PRIMARY_KEY);
					rtn.wherePart.addLast(pf);
				}
				
				if(f.isAnnotationPresent(IsFakeKey.class)){
					pf.setFieldType(ParsedFieldDescriptor.TYPE_FAKE_KEY);
					rtn.wherePart.addLast(pf);
				}

				rtn.fieldPart.addLast(pf);

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new OrmException("解析Bean时发现参数错误:"+e.getMessage());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new OrmException("解析Bean时引发访问权限问题:"+e.getMessage());
			}
		}
	}
	
	static OrmCommand prepareSelectBean(ParsedBeanDescriptor pfc, boolean neglectNullValue){
		StringBuilder sql=new StringBuilder();
		LinkedList<Object> params=new LinkedList<Object>();
		OrmCommand einfo= new OrmCommand();

		sql.append("SELECT ");
		for(ParsedFieldDescriptor pf: pfc.fieldPart){
			sql.append(pf.name).append(", ");
		}
		sql.setLength(sql.length()-2);

		sql.append(" FROM ").append(pfc.tableName);

		sql.append(" WHERE ");
		for(ParsedFieldDescriptor pf: pfc.wherePart){
			if(pf.value==null){
				if(!neglectNullValue){
					sql.append(pf.name).append(" IS NULL AND ");
				}
			}else{
				sql.append(pf.name).append("=? AND ");
			}
			params.add(pf.value);
		}
		sql.setLength(sql.length()-5);
		einfo.sql=sql.toString();
		einfo.params = params.toArray();
		return einfo;
	}

	static OrmCommand prepareInsertBean(ParsedBeanDescriptor pbd, boolean neglectNullValue){
		OrmCommand einfo= new OrmCommand();
		StringBuilder sql=new StringBuilder();
		sql.append("INSERT INTO ").append(pbd.tableName).append(" (");
		StringBuilder sql2=new StringBuilder();
		LinkedList<Object> params=new LinkedList<Object>();
				
		for(ParsedFieldDescriptor bf: pbd.fieldPart){
			if(bf.value==null){
				if(!neglectNullValue){
					sql.append(bf.name).append(", ");
					sql2.append("NULL, ");
				}
			}else{
				sql.append(bf.name).append(", ");
				sql2.append("?, ");
				params.addLast(bf.value);
			}
		}
		sql.setLength(sql.length()-2);
		sql2.setLength(sql2.length()-2);
		sql.append(") VALUES (");
		sql.append(sql2).append(")");

		einfo.sql=sql.toString();
		einfo.params = params.toArray();
		return einfo;
	}
	
	static OrmCommand prepareDeleteBean(ParsedBeanDescriptor pbd, boolean neglectNullValue){
		OrmCommand rtn= new OrmCommand();
		LinkedList<Object> params=new LinkedList<Object>();
		StringBuilder sql=new StringBuilder();
		sql.append("DELETE FROM ").append(pbd.tableName).append(" WHERE ");
		for(ParsedFieldDescriptor pf:pbd.wherePart){	
			if(pf.value==null){
				if(pf.isPrimaryKey()){
					throw new OrmException("执行删除操作时，主键不可为空值");
				}else{
					if(!neglectNullValue){
						sql.append(pf.name).append(" IS NULL AND ");
					}
				}
			}else{
				sql.append(pf.name).append("=? AND ");
			}
			params.addLast(pf.value);
			
		}
		sql.setLength(sql.length()-5);
		rtn.sql=sql.toString();
		rtn.params = params.toArray();
		return rtn;
	}
	
	static OrmCommand prepareUpdateBean(ParsedBeanDescriptor pbd, boolean neglectNullValue){
		OrmCommand einfo= new OrmCommand();
		StringBuilder sql=new StringBuilder();
		LinkedList<Object> params=new LinkedList<Object>();
		sql.append("UPDATE ").append(pbd.tableName).append(" SET ");
		for(ParsedFieldDescriptor pf:pbd.fieldPart){
			if(pf.isPrimaryKey()){
				continue;
			}
			if(pf.value==null){
				if(!pbd.neglectNullValue){
					sql.append(pf.name).append("=NULL, ");
				}
			}else{
				sql.append(pf.name).append("=?, ");
				params.addLast(pf.value);
			}
		}

		sql.setLength(sql.length()-2);
		sql.append(" WHERE ");

		for(ParsedFieldDescriptor pf:pbd.wherePart){
			if(pf.value==null){
				if(pf.isPrimaryKey()){
					throw new OrmException("执行更新操作时，主键不可为空值");
				}else{
					if(!pbd.neglectNullValue){
						sql.append(pf.name).append(" IS NULL AND ");
					}
				}
			}else{
				sql.append(pf.name).append("=? AND ");
				params.addLast(pf.value);
			}
		}
		sql.setLength(sql.length()-5);
		einfo.sql=sql.toString();
		einfo.params = params.toArray();
		return einfo;
	}

}
