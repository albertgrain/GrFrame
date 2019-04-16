// By GuRui on 2015-12-5 下午2:14:49
package dlmu.mislab.orm;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.config.DbConnection;
import dlmu.mislab.config.Log4JConfigure;
import dlmu.mislab.orm.annotation.AffectRow;
import dlmu.mislab.orm.annotation.Table;
import dlmu.mislab.orm.annotation.AffectRow.AffectRowType;
import dlmu.mislab.orm.bean.BeanParser;
import dlmu.mislab.orm.bean.DeleteCmdGenerator;
import dlmu.mislab.orm.bean.InsertCmdGenerator;
import dlmu.mislab.orm.bean.UpdateCmdGenerator;
import dlmu.mislab.tool.Str;

public final class Bn{
	private static Logger logger = null;
	static{
		Log4JConfigure.init();
		//DbConfigurator.init();
		logger = LoggerFactory.getLogger(Bn.class);
	}
	
	
//	/***
//	 * 根据SelectBean（应该只有键赋值）加载所有字段和子表对象及字段
//	 * 未查出结果返回null，出错抛出异常
//	 * @param bean
//	 * @return
//	 */
//	public static IOrmBean LoadOne(IOrmBean bean){
//		return LoadOne(bean, false);
//	}
	
	/***
	 * 根据SelectBean（应该只有键赋值）加载所有字段和子表对象及字段
	 * 未查出结果返回null，出错抛出异常
	 * @param bean
	 * @param oneLevelOnly 是否只查询一层。如果为是，则退化为SelectOne()方法
	 * @return 没有找到返回null，否则返回结果对象
	 */
	private static <T extends IOrmBean> T LoadOne(T bean, boolean oneLevelOnly){
		List<T> rtn=Bn.Load(bean, oneLevelOnly);
		if(rtn.size()>1){
			throw new OrmTooManyRowsAffectedException("查出多余一个可状态对象，请检查主键的唯一性");
		}else if(rtn.size()<=0){
			return null;
		}
		return rtn.get(0);
	}
	
//	/***
//	 * 根据SelectBean（除主键或伪键外都无需赋值）加载所有字段和子表对象及字段
//	 * @param bean
//	 * @return 未查出结果返回0长列表，出错返回null
//	 */
//	public static List<? extends IOrmBean> Load(IOrmBean bean){
//		return Load(bean, false);
//	}
	
	/***
	 * 根据SelectBean（除主键或伪键外都无需赋值）加载所有字段和子表对象及字段
	 * @param bean
	 * @param oneLevelOnly 是否只查询一层。如果为是，则退化为Select()方法
	 * @return 未查出结果返回0长列表，出错返回null
	 */
	private static <T extends IOrmBean> List<T> Load(T bean, boolean oneLevelOnly){
		ParsedBeanInfoForSelect pb = BeanParserForSelect.parseBean(bean);
		CmdDescriptorForSelect cds=BeanParserForSelect.prepareSelectDescriptor(pb);
		SelectCommand cmd = new SelectCmdGenerator().prepareSelectCmd(cds);
		@SuppressWarnings("unchecked")
		List<T> rtn = (List<T>) Bn.executeQuery(cmd, bean.getClass());
		if(rtn==null){
			return null;
		}
		
		if(oneLevelOnly){ //Do only one level select.
			return rtn;
		}
		
		for(IOrmBean b: rtn){	//For each parent object
			if(pb.childTypes==null){
				break;
			}
			for(Class<?> cls : pb.childTypes){	//For each child table
				List<? extends IOrmBean> children;
				IOrmBean cb = createChildAndSetKeyVal(b, cls, pb);
				if(cb==null){
					return null; //error
				}
				children = Load(cb, oneLevelOnly);
				attachChildrenToParent(children, b, pb);
			}
		}
		
		return rtn;
	}
	
	/***
	 * 将子表对象列表附加到父表对象的对应字段上
	 * @param children
	 * @param parent
	 * @param pb
	 */
	private static <T extends IOrmBean> boolean attachChildrenToParent(List<T> children, IOrmBean parent, ParsedBeanInfoForSelect pb){	
		for(BeanFieldForSelect f: pb.fields){
			if(f.isRefToChild){
				if(f.isArray){
					@SuppressWarnings("unchecked")
					T[] chds= (T[]) Array.newInstance(f.childType, children.size());
					for(int i=0;i<chds.length;i++){
						chds[i]=children.get(i);
					}
					try {
						BeanUtils.setProperty(parent, f.fieldName, chds);
					} catch (IllegalAccessException | InvocationTargetException e) {
						logger.error(e.getMessage());
						return false;
					}
				}else{
					T chd=null;
					if(children.size()==0){
						chd=null;
					}else if(children.size()==1){
						chd=children.get(0);
					}else{
						logger.error("将子对象绑定到父对象时发生错误：本应只有1或0个子对象，结果发现多余1个。绑定失败。");
						return false;
					}
					
					try {
						BeanUtils.setProperty(parent, f.fieldName, chd);
					} catch (IllegalAccessException | InvocationTargetException e) {
						logger.error(e.getMessage());
						return false;
					}
				}
				
			}
		}
		return true;
	}
	
	/**
	 * 创建子表对象，并将主表的外键字段（可多个）值赋予子表的主键字段（可多个）
	 * @param cls
	 * @param pb
	 * @return 出错返回null
	 * @throws NoSuchMethodException 
	 */
	private static IOrmBean createChildAndSetKeyVal(IOrmBean parent, Class<?> cls, ParsedBeanInfoForSelect pb){
		IOrmBean rtn= null;
		try {
			rtn=(IOrmBean)cls.newInstance();
			if(cls.isAnnotationPresent(Table.class)){
				String childTableName=cls.getAnnotation(Table.class).Name();
				for(BeanFieldForSelect f:pb.fields){
					if(f.isFnKey){
						if(f.fnTableName.equalsIgnoreCase(childTableName)){
							Object val=BeanUtils.getProperty(parent, f.fieldName);
							BeanUtils.setProperty(rtn, f.fnKeyName, val);
							break;
						}
					}else{
						continue;
					}
				}
				
			}
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			logger.error("创建OrmBean的实例失败\n"+e.getMessage());
			return null;
		} catch( InvocationTargetException e){
			logger.error("将父对象外键字段值赋给子对象主键时失败。请检查父子对象外键与主键是否一一对应\n"+e.getMessage());
			return null;
		}
		return rtn;
	}
	
	
	
	/***
	 * 将Bean所包含的嘻嘻写入数据库表中。
	 * 该操先尝试图更新操作，如果失败，则再做插入操作
	 * 如果影响超过1行记录，则一定失败
	 * By GuRui on 2016-3-22 下午9:34:10
	 * @param bean 将Bean所包含的嘻嘻写入数据库表中。必须给Bean的TableName和IsKey属性设值，否则会导致保存失败
	 * @return 成功返回true
	 */
	public static boolean Save(IOrmBean bean){
		return Save(bean, true);
	}
	
	/***
	 * 将Bean所包含的嘻嘻写入数据库表中。
	 * 该操先尝试图更新操作，如果失败，则再做插入操作
	 * 如果影响超过1行记录，则一定失败
	 * By GuRui on 2016-3-22 下午9:36:47
	 * @param bean 将Bean所包含的嘻嘻写入数据库表中。必须给Bean的TableName和IsKey属性设值，否则会导致保存失败
	 * @param neglectNull 忽略值为空的字段（值为空的字段不保存）
	 * @return 成功返回true
	 */
	public static boolean Save(IOrmBean bean, boolean neglectNull){
		boolean isUpdateOk=false;
		try{
			isUpdateOk=Update(bean, neglectNull);
		}catch(OrmException e){
			isUpdateOk=false;
		}	
		if(!isUpdateOk){
			if(!Insert(bean, neglectNull)){
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 在一个事务里插入一系列Bean。每个Bean对应插入一条记录，因此必须确定主键，
	 * By GuRui on 2015-12-29 下午2:13:46
	 * @param jar 一系列Bean。如果删除需要保证插入顺序，请用LinkedList等有序列表传入参数
	 * @param neglectNullValue 是否忽略空值
	 * @return 成功返回true
	 */
	public static boolean Insert(IOrmBean[] jar, boolean neglectNullValue){
		List<OrmCommand> exes=new LinkedList<OrmCommand>();
		for(IOrmBean bean: jar){
			exes.addAll(BeanParser.generateCmds(bean,new InsertCmdGenerator(),neglectNullValue));
		}
		return executeNoQuery(exes);
	}

	/***
	 * 将Bean中的内容插入数据库表。空值字段内容不会插入
	 * By GuRui on 2015-12-28 下午11:20:19
	 * @param bean : 待插入的行内容对应的bean。请自行注意哪些字段不可为空
	 * @return 成功返回true
	 */
	public static boolean Insert(IOrmBean bean){
		return Insert(bean, true);
	}

	/***
	 * 将Bean中的内容插入数据库表
	 * By GuRui on 2015-12-28 下午11:19:57
	 * @param bean 待插入的行内容对应的bean。请自行注意哪些字段不可为空
	 * @param neglectNullValue 是否忽略空值，即空值字段不插入
	 * @return 成功返回true
	 */
	public static boolean Insert(IOrmBean bean, boolean neglectNullValue){
		return Insert(new IOrmBean[]{bean},neglectNullValue);
	}
	
	/***
	 * 插入一个新行并返回自@IsAuto指定的一个字段值，通常用于获得自增长字段和有缺省值的字段
	 * By GuRui on 2016-7-17 下午1:43:40
	 * @param bean 待插入数据，注意自增长关键字应为null
	 * @param keyFieldNames 待返回的关键字字段名列表
	 * @return 
	 * @return 失败返回null，否则返回新增加的行的自增长列值。如果一次影响超过2行，则抛出DaoSingleRowAffectedException异常
	 */
	public static <T> T insertThenGetOneValue(IOrmBean bean){
		OrmTrans trans=new OrmTrans();
		try{
			trans.begin();
			T rtn= trans.tryInsertThenGetAutoValue(bean);
			trans.commit();
			return rtn;
		}catch(OrmException e){	
			logger.error(e.getMessage());
			// already rollbacked by trans object.
			return null;			
		}
	}

	/***
	 * 更新一个Bean对应的一行记录行。如果没有给全部主键赋值，则会导致更新失败
	 * By GuRui on 2016-7-17 上午11:06:34
	 * @param bean 每个Bean对应更新一条记录，必须给全部主键赋值。空值不会更新进数据库
	 * @return 成功返回true
	 */
	public static boolean Update(IOrmBean bean){
		return Update(bean, true);
	}
	
	/***
	 * 更新一个Bean对应的记录行。如果没有给全部主键赋值，会导致更新多条记录，请慎用！
	 * By GuRui on 2016-7-17 上午11:14:05
	 * @param bean 每个Bean对应更新一条记录，必须给全部主键赋值。空值不会更新进数据库
	 * @param strictlyAffectOneRow 是否更新仅会影响一行记录
	 * @return 成功返回true
	 */
	public static boolean Update(IOrmBean bean, boolean strictlyAffectOneRow){
		return Update(bean,true,strictlyAffectOneRow);
	}

	/***
	 * 更新一个Bean对应的记录行。如果没有给全部主键赋值，会导致更新多条记录，请慎用！
	 * By GuRui on 2016-7-17 上午11:14:05
	 * @param bean 每个Bean对应更新一条记录，必须给全部主键赋值。空值不会更新进数据库
	 * @param neglectNullValue 是否忽略空值。如果为false，则会以"[key] IS NULL"的形式形成SQL条件
	 * @param strictlyAffectOneRow 是否更新仅会影响一行记录
	 * @return 成功返回true
	 */
	public static boolean Update(IOrmBean bean, boolean neglectNullValue,  boolean strictlyAffectOneRow){
		return Update(new IOrmBean[]{bean},neglectNullValue, strictlyAffectOneRow);
	}
	
	/***
	 * 在一个事务里更新一系列Bean。每个Bean对应更新一条记录，因此必须确定主键，
	 * By GuRui on 2015-12-29 下午2:13:46
	 * @param jar 一系列Bean。如果删除需要保证更新顺序，请用LinkedList等有序列表传入参数
	 * @param neglectNullValue 是否忽略空值
	 * @return 成功返回true
	 */
	public static boolean Update(IOrmBean[] jar, boolean neglectNullValue, boolean strictlyAffectOneRow){
		List<OrmCommand> exes=new LinkedList<OrmCommand>();
		for(IOrmBean bean: jar){
			exes.addAll(BeanParser.generateCmds(bean, new UpdateCmdGenerator(),neglectNullValue));
		}
		return executeNoQuery(exes, strictlyAffectOneRow);
	}
	
	/***
	 * Generate OrmCommand object for insert
	 * By GuRui on 2016-6-22 下午10:36:32
	 * @param bean source bean
	 * @return
	 */
	public static List<OrmCommand> genInsertCommands(IOrmBean bean){
		return genInsertCommands(bean, true);
	}
	
	/***
	 * Generate OrmCommand object for insert
	 * By GuRui on 2016-5-23 上午2:57:53
	 * @param bean source bean
	 * @param neglectNullValue
	 * @return generate OrmCommand list
	 */
	public static List<OrmCommand> genInsertCommands(IOrmBean bean, boolean neglectNullValue){
		return BeanParser.generateCmds(bean, new InsertCmdGenerator(),neglectNullValue);
	}
	
	/***
	 * Generate command bean for update
	 * By GuRui on 2016-6-22 下午10:36:49
	 * @param bean source bean
	 * @return
	 */
	public static List<OrmCommand> genUpdateCommands(IOrmBean bean){
		return genUpdateCommands(bean, true);
	}
	
	/***
	 * Generate command bean for update
	 * By GuRui on 2016-6-22 下午10:37:12
	 * @param bean source bean
	 * @param neglectNullValue
	 * @return
	 */
	public static List<OrmCommand> genUpdateCommands(IOrmBean bean, boolean neglectNullValue){
		return BeanParser.generateCmds(bean, new UpdateCmdGenerator(),neglectNullValue);
	}
	
	/***
	 * 通过sql语句删除数据库记录。删除且仅删除1行记录为成功，其余情况皆为失败。
	 * By GuRui on 2016-8-4 上午12:30:26
	 * @param sql 做删除操作的sql语句
	 * @param parameters 删除所需参数
	 * @return 被删除行数
	 */
	public static boolean delete(String sql, Object... parameters){
		if(sql.toLowerCase().trim().startsWith("delete")){
			OrmTrans trans=new OrmTrans();
			try{
				trans.begin();
				trans.tryExecute(sql, parameters);
				trans.commit();
			}catch(OrmException e){
				logger.error(e.getMessage());
				return false;
			}
			return true;
		}else{
			logger.error("不是Delete语句");
			return false;
		}
	}
	
	/***
	 * 通过sql语句删除数据库记录。删除0行或1行记录为成功，删除多行或执行异常为失败。
	 * 请谨慎使用无限制的AffectRowType!
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public static boolean delete(AffectRowType affectRowType, String sql, Object... parameters){
		if(sql.toLowerCase().trim().startsWith("delete")){
			OrmTrans trans=new OrmTrans();
			try{
				trans.begin();
				trans.tryExecute(affectRowType, AffectRow.DEFAULT_NUM_OF_ROWS_TO_BE_AFFECTED, sql, parameters);
				trans.commit();
			}catch(OrmException e){
				logger.error(e.getMessage());
				return false;
			}
			return true;
		}else{
			logger.error("不是Delete语句");
			return false;
		}
	}
	
	/***
	 * 删除一个Bean对应的一行记录行。如果没有给全部主键赋值，则会导致删除失败
	 * By GuRui on 2016-7-17 上午11:06:34
	 * @param bean 每个Bean对应删除一条记录，Bean只有主键的赋值有作用，必须确定主键
	 * @return 成功返回true
	 */
	public static boolean Delete(IOrmBean bean){
		return Delete(bean, true);
	}

	/***
	 * 删除一个Bean对应的记录行。
	 * By GuRui on 2015-12-28 下午11:18:55
	 * @param bean 每个Bean只有主键的赋值有作用，应确定主键，
	 * @param strictlyAffectOneRow 是否强制只影响一行记录（影响多行会失败）。对于多字段为主键的情况，缺少一个主键将导致删除多条记录，请慎用！
	 * @return 成功返回true
	 */
	public static boolean Delete(IOrmBean bean, boolean strictlyAffectOneRow){
		return executeNoQuery(BeanParser.generateCmds(bean, new DeleteCmdGenerator(), true), strictlyAffectOneRow);
	}

	/***
	 * 删除一个Bean对应的记录行。
	 * By GuRui on 2016-3-22 下午2:08:13
	 * @param bean 每个Bean对应删除一条记录，因此必须确定主键，对于多字段为主键的情况，缺少一个主键将导致删除多条记录，请慎用！
	 * @param neglectNullValue 是否忽略空值。如果为false，则会以"[key] IS NULL"的形式形成SQL条件
	 * @param strictlyAffectOneRow 是否强制只影响一行记录（影响多行会失败）
	 * @return 成功返回true
	 */
	public static boolean Delete(IOrmBean bean, boolean neglectNullValue, boolean strictlyAffectOneRow){
		return Delete(new IOrmBean[]{bean},neglectNullValue, strictlyAffectOneRow);
	}

	/***
	 * 在一个事务里删除系列Bean。每个Bean对应删除一条记录，因此必须确定主键，
	 * By GuRui on 2015-12-29 下午2:13:46
	 * @param jar 一系列Bean。如果删除需要保证删除顺序，请用LinkedList等有序列表传入参数
	 * @param neglectNullValue 是否忽略空值。如果为false，则会以"[key] IS NULL"的形式形成SQL条件
	 * @return 成功返回true
	 */
	public static boolean Delete(IOrmBean[] jar, boolean neglectNullValue, boolean strictlyAffectOneRow){
		LinkedList<OrmCommand> exes=new LinkedList<OrmCommand>();
		for(IOrmBean bean: jar){
			exes.addAll(BeanParser.generateCmds(bean,new DeleteCmdGenerator(), neglectNullValue));
		}
		return executeNoQuery(exes);
	}
	
	public static List<OrmCommand> genDeleteCommands(IOrmBean bean){
		return genDeleteCommands(bean, true);
	}
	
	public static List<OrmCommand> genDeleteCommands(IOrmBean bean, boolean neglectNullValue){
		return BeanParser.generateCmds(bean, new DeleteCmdGenerator(), true);
	}


	/***
	 * 根据SQL查询并返回一个IOrmBean对象
	 * By GuRui on 2016-4-11 下午2:24:44
	 * @param cls 返值类型的class
	 * @param sql 查询语句
	 * @param parameters 查询所需参数
	 * @return 返回查询到的IOrmBean对象; 返回null:出错或没找到记录; 抛出异常DaoSingleRowAffectedException:查询出多条记录
	 */
	public static <T extends IOrmBean> T SelectOne(Class<T> cls, String sql, Object... parameters){
		List<T> rtn=null;
		try {
			rtn = Bn.doSelect(cls, sql, parameters);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			return null;
		}
		if(rtn==null || rtn.size()==0){
			return null;
		}else if(rtn.size()>1){
			throw new OrmException("执行结果未能只影响1行记录。系统将回滚");
		}
		return rtn.get(0);
	}
	
	/***
	 * 执行单表查询，并根据Bean的关键字(@IsKey)值返回一个IOrmBean类型对象（只查询第一层对象，不进行深层查询，效率较高，推荐使用）
	 * @param bean
	 * @return 未查出结果返回null，出错抛出Orm异常
	 */
	public static <T extends IOrmBean> T SelectOne(T bean){
		return (T) Bn.LoadOne(bean,true);
	}
	
	/***
	 * 执行单表查询，并根据Bean的关键字(@IsKey)值返回一个IOrmBean类型对象。将会执行深层查询，效率较低，不建议使用在关键查询上。
	 * @param bean
	 * @return 未查出结果返回null，出错抛出Orm异常
	 */
	public static <T extends IOrmBean> T SelectOneDeep(T bean){
		return (T) Bn.LoadOne(bean,false);
	}
	
	public static final String SQL_SELECT_ALL="select * from ";
	/***
	 * 从第一个表开始，深度查询并返回整个关联的森林。比较适合返回关联代码表的全部数据。如返回上品大类、种类、小类代码及名称
	 * @param cls 初始表。必需用@Table标记，并设定Name 
	 * @return 如果没有查到发回一个长度为0的List，出错返回null。Table为设定抛出OrmException
	 */
	public static <T extends IOrmBean> List<T> SelectAllDeep(Class<T> cls){
		if(!cls.isAnnotationPresent(Table.class)){
			throw new OrmException("必需给"+cls.getName()+"设定@Table标记");
		}
		String  tbName=cls.getAnnotation(Table.class).Name();
		if(Str.isNullOrEmpty(tbName)){
			throw new OrmException(cls.getName()+"的@Table的Name不可为空");
		}
		List<T> all=Bn.Select(cls, SQL_SELECT_ALL + tbName);
		List<T> rtn=new LinkedList<T>();
		for(T t: all){
			rtn.add(Bn.SelectOneDeep(t));
		}
		return rtn;
	}
	
	/***
	 * 执行单表查询，并根据Bean的伪关键字(@IsFakeKey)值返回一个IOrmBean类型对象。将会执行深层查询，效率较低，不建议使用在关键查询上。
	 * @param bean
	 * @return 如果没有查到发回一个长度为0的List，出错抛出Orm异常
	 */
	public static <T extends IOrmBean> List<T> SelectDeep(T bean){
		return (List<T>) Bn.Load(bean,false);
	}

	/***
	 * 通过SQL语句查询，返回指定类型的对象。
	 * 此方法直接根据SQL查询返回行，对应Bean只能有一层（不可包含子对象），因此效率较高。若非包含多层的查询，请尽量使用此方法做Select操作。
	 * By GuRui on 2016-8-28 上午9:45:18
	 * @param cls 任何IOrmBean类型，请自行保证与查询结果匹配
	 * @param sql 查询语句
	 * @param parameters 查询所需参数
	 * @return 一个自定义类型的对象列表(List)
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> Select(Class<? extends IOrmBean> cls, String sql, Object... parameters){
		try{
			return (List<T>)doSelect(cls, sql, parameters);
		}catch(SQLException e){
			OrmCommand cmd=new OrmCommand(sql, parameters);
			throw new OrmException("执行Dao.Select失败:"+e.getMessage(),cmd);
		}
	}
	
	/***
	 * 通过SQL语句查询，返回结果集中第一行第一列的字段值。
	 * 此方法直接根据SQL查询返回行，因此效率较高。若非包含多层的查询，请尽量使用此方法做Select操作。
	 * @param cls
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public static <T> T selectScalar(Class<T> cls, String sql, Object... parameters){
		return new OrmTrans().begin().trySelectScalar(cls, sql, parameters);
	}
	
	private static <T extends IOrmBean> List<T> doSelect(Class<T> cls, String sql, Object... parameters) throws SQLException{
		OrmTrans trans=new OrmTrans().begin();
		try{
			List<T> rtn = trans.trySelect(cls, sql, parameters);
			trans.commit();
			return rtn;
		}catch(OrmException e){
			return null;
		}
	}
	
		
	/***
	 * 查询数据库并返回一个由key-object组成的对象列表的列表。
	 * 外层列表内的每一个列表代表一行，内层列表每个KeyObject对象代表一个元素
	 * By GuRui on 2016-6-18 上午10:38:43
	 * @param sql 查询sql
	 * @param parameters 查询所需菜蔬，可以为空
	 * @return 如果没有结果返回一个长度为0的list，如果出错返回null
	 */
	public static List<List<SimpleEntry<String, Object>>> selectKeyObjPairs(String sql, Object... parameters){
		Connection conn=DbConnection.pool.getConnection();
		List<List<SimpleEntry<String, Object>>> rtn=null;
		try {
			PreparedStatement stmt= conn.prepareStatement(sql);
			OrmTools.populateParameters(stmt, parameters);
			if(ConfigBase.logSQL){
				OrmTools.log(logger, sql, parameters);
			}
			ResultSet rs = stmt.executeQuery();
			conn.commit();
			rtn=ResultSetMapper.resultSet2KeyValuePair(rs);
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			return null;
		}
		
		return rtn;
	}


	/***
	 * 执行批处理。有事务保证，每个处理只能影响1行
	 * By GuRui on 2016-5-23 上午2:42:29
	 * @param exes 利用Gen***ComdBean生成的执行信息
	 * @return -1为失败。或为最后一条语句影响的行数
	 */
	private static boolean executeNoQuery(List<OrmCommand> exes){
		return executeNoQuery(exes, true);
	}

	/***
	 * 执行批处理。有事务保证，每个处理只能影响1行
	 * By GuRui on 2016-5-23 上午2:42:29
	 * @param exes 利用Gen***ComdBean生成的执行信息
	 * @param strictlyAffectOneRow 是否只允许影响一行记录
	 * @return true 为成功
	 */
	private static boolean executeNoQuery(List<OrmCommand> exes, boolean strictlyAffectOneRow){
		if(strictlyAffectOneRow){
			return executeNoQuery(exes, AffectRowType.STRICTLY_ONE_ROW);
		}else{
			return executeNoQuery(exes, AffectRowType.ANY_NUMBER_OF_ROWS);
		}
	}
	

	private static boolean executeNoQuery(List<OrmCommand> exes, AffectRowType affectType){
		OrmTrans trans=new OrmTrans();
		try{
			trans.begin();
			for(OrmCommand cmd: exes){
				trans.tryExecute(cmd, affectType, AffectRow.DEFAULT_NUM_OF_ROWS_TO_BE_AFFECTED);
			}
			trans.commit();
			return true;
		}catch(OrmException e){
			logger.error("Insert/Delete/Update数据库失败:"+e.getMessage());
			return false;
		}
	}
	
	/***
	 * 用原生的SQL语句+参数执行非查询（Insert/Delete/Update)
	 * 只能影响至多一行，否则返回false
	 * By GuRui on 2017-6-28 上午8:01:08
	 * @param sql 
	 * @param params
	 * @return
	 */
	public static boolean executeNoQuery(String sql, Object... params){
		OrmTrans trans=new OrmTrans(true, AffectRowType.NO_MORE_THAN_ONE,1);
		try{
			trans.begin();
			trans.tryExecute(sql, params)
				.commit();
			return true;
		}catch(OrmException e){
			logger.error("Insert/Delete/Update数据库失败:"+e.getMessage());
			return false;
		}
	}
	
	/***
	 * 执行查询。不会抛出异常
	 * @param exe
	 * @param cls
	 * @return 出错返回null，没查到内容返回0长列表。
	 */
	private static <T extends IOrmBean> List<T> executeQuery(OrmCommand exe, Class<T> cls){
		OrmTrans trans = new OrmTrans();
		try{
			trans.begin();
			List<T> rtn =trans.trySelect(cls, exe.sql, exe.params);
			trans.commit();
			return rtn;
		}catch(OrmException e){
			logger.error("查询失败:"+e.getMessage());
			return null;
		}
	}
	
}

/***
 * 为获取自增长字段值而用 
 * 被insertThenGetOneValue方法使用
 * @author GuRui
 *
 */
class ReturnFieldDescription {
	String returnFieldName;
	Class<?> returnKeyClass;
	
	public ReturnFieldDescription(){}
	public ReturnFieldDescription(String returnFieldName, Class<?> returnKeyClass){
		this.returnFieldName = returnFieldName;
		this.returnKeyClass = returnKeyClass;
	}
}
/***
 * 为获取自增长字段值而用
 * 被insertThenGetOneValue方法使用
 * @author GuRui
 *
 */
class OrmInsertCommand extends OrmCommand{
	
	public OrmInsertCommand(OrmCommand cmd){
		super.keyFields=cmd.keyFields;
		super.params=cmd.params;
		super.sql=cmd.sql;
	}
	
	ReturnFieldDescription returnField = new ReturnFieldDescription(); //只是为了insertThenGetKey顺利返回自增长键而用
	
	public String[] getReturnFieldNames(){
		return new String[]{this.returnField.returnFieldName};
	}
}




