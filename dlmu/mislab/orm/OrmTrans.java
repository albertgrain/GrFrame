// By GuRui on 2015-1-19 下午5:37:11
package dlmu.mislab.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.config.DbConnection;

import dlmu.mislab.orm.annotation.AffectRow;
import dlmu.mislab.orm.annotation.AffectRow.AffectRowType;
import dlmu.mislab.orm.bean.BeanParser;
import dlmu.mislab.orm.bean.DeleteCmdGenerator;
import dlmu.mislab.orm.bean.InsertCmdGenerator;
import dlmu.mislab.orm.bean.UpdateCmdGenerator;
import dlmu.mislab.tool.Str;

/**
 * 慎用！
 * 此类的执行需要顺序调用begin() -> commit() / rollback()。
 * 次类仅用于需要根据执行结果确定下一步执行的复杂事务操作，必须自行处理各种例外
 * @author GuRui
 *
 */
public final class OrmTrans {
	private static Logger logger=LoggerFactory.getLogger(OrmTrans.class);
	private Connection conn=DbConnection.pool.getConnection();
	private boolean transStarted=false;
	private boolean transEnded=false;
	
	private AffectRowType affectRowType;
	private int affectRows;
	private boolean neglectNull;
	
	public OrmTrans(){
		this(true, AffectRowType.STRICTLY_ONE_ROW, AffectRow.DEFAULT_NUM_OF_ROWS_TO_BE_AFFECTED);
	}
	
	public OrmTrans(boolean neglectNull, AffectRowType affectRowType, int affectRows){
		this.neglectNull = neglectNull;
		this.affectRowType = affectRowType;
		this.affectRows = affectRows;
		if(this.conn==null){
			throw new OrmException("尚未进行数据库连接初始化，请检查配置文件并调用DbConfigure.init([配置文件名]);");
		}
	}
	
	/***
	 * 启动事务。执行tryExecute之前必须先启动事务
	 * @return
	 */
	public OrmTrans begin(){
		try {
			this.conn.setAutoCommit(false);
			this.transStarted=true;
		} catch (SQLException e) {
			OrmTools.error(logger,"关闭连接的自动更新失败。数据库可能不支持事务。\n"+e.getMessage());
		}
		return this;
	}

	/***
	 * 执行一个命令，默认只影响一行
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param cmd
	 * @return
	 */
	OrmTrans tryExecute(OrmCommand cmd){
		this.execute(cmd, AffectRowType.STRICTLY_ONE_ROW, AffectRow.DEFAULT_NUM_OF_ROWS_TO_BE_AFFECTED);
		return this;
	}
	
	/***
	 * 执行一个命令，并指定影响的方式和行数
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param cmd 命令
	 * @param affectRow 影响的类型
	 * @param rowNumToBeAffected 影响的行数
	 * @return
	 */
	OrmTrans tryExecute(OrmCommand cmd, AffectRowType affectRow, int rowNumToBeAffected){
		this.execute(cmd, affectRow, rowNumToBeAffected);
		return this;
	}
	
	/***
	 * 执行一个命令，默认只影响一行
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param sql
	 * @param params
	 * @return
	 */
	public OrmTrans tryExecute(String sql, Object... params){
		this.tryExecute(this.affectRowType, this.affectRows, sql, params);
		return this;
	}
	
	/***
	 * 执行一个命令，并指定影响的方式和行数
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param affectRowType 执行结果影响行数的类型
	 * @param affectRows 影响的行数
	 * @param sql
	 * @param params
	 * @return
	 */
	public OrmTrans tryExecute(AffectRowType affectRowType, int affectRows, String sql, Object... params){
		if(Str.isNullOrEmpty(sql)){
			throw new OrmException("Sql语句不可为空");
		}
//		if(params==null || params.length==0){
//			throw new OrmException("执行Sql命令所需参数不可为空");
//		}
		this.execute(new OrmCommand(sql,params), affectRowType, affectRows);
		return this;
		
	}
	
	@Deprecated
	/***
	 * 非常不安全的执行方法，强烈不建议使用
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param sql 批处理执行的SQL语句
	 * @return 成功返回true
	 */
	public boolean tryExceuteBatch(String sql){
		try {
			Connection con=DbConnection.pool.getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			boolean rtn= stmt.execute();
			con.commit();
			return rtn;
		} catch (SQLException e) {
			logger.error(e.getMessage());
			logger.error(sql);
			return false;
		}
		
	}
	
	private void execute(OrmCommand cmd, AffectRowType afType, int rowsShouldBeAffected){
		if(!this.transStarted || this.transEnded){
			throw new OrmException("事务未开始或已结束");
		}
		try {
			PreparedStatement stmt = conn.prepareStatement(cmd.sql);
			OrmTools.populateParameters(stmt, cmd.params);
			if(ConfigBase.logSQL){
				OrmTools.log(logger, cmd);
			}
			int affectedRows = stmt.executeUpdate();
			this.checkAffectedRows(cmd, affectedRows, afType, rowsShouldBeAffected);
			stmt.close();
		} catch (SQLException e) {
			String msg="执行数据库操作出现错误，数据未能提交\n"+e.getMessage();
			OrmTools.error(logger, cmd, msg);
			this.rollback();
			throw new OrmException(msg);
		} catch (Throwable e){
			String msg="执行数据库操作出现系统性错误，数据未能提交\n"+e.getMessage();
			OrmTools.error(logger, cmd, msg);
			this.rollback();
			throw new OrmException(msg);
		}
	}
	
	private void checkAffectedRows(OrmCommand cmd, int affectedRows, AffectRowType afType, int rowsShouldBeAffected){
		if(affectedRows > ConfigBase.MAX_AFFECTED_ROW_BY_ONE_TRANSACTION){
			String msg="执行结果超过系统设定的最大允许影响行数："+ConfigBase.MAX_AFFECTED_ROW_BY_ONE_TRANSACTION+"。系统回滚";
			OrmTools.error(logger, cmd, msg);
			this.rollback();
			throw new OrmTooManyRowsAffectedException(cmd);
		}
		
		if(afType.equals(AffectRowType.STRICTLY_ONE_ROW)){
			if(affectedRows!=1){
				throw new OrmException("执行结果未能只影响1行记录。系统将回滚");
			}
		}else if(afType.equals(AffectRowType.SPECIFIED_NUMBER_OF_ROWS)){
			if(affectedRows!=rowsShouldBeAffected){
				throw new OrmException("执行结果未能只影响"+rowsShouldBeAffected+"行记录。系统将回滚");
			}
			
		}else if(afType.equals(AffectRowType.NO_MORE_THAN_ONE)){
			if(affectedRows>1){
				throw new OrmException("执行结果未能只影响不多余1行记录。系统将回滚");
			}
		}else if(afType.equals(AffectRowType.NO_MORE_THAN_SPECIFIED_NUMBER_OF_ROWS)){
			if(affectedRows>rowsShouldBeAffected){
				throw new OrmException("执行结果未能只影响不多于"+rowsShouldBeAffected+"行记录。系统将回滚");
			}
		}
	}
	

	/***
	 * 将之前tryExecute的内容一次性提交。可能抛出OrmException
	 */
	public void commit(){
		this.transEnded=false;
		try {
			this.conn.commit();
			this.conn.close();
		} catch (SQLException e) {
			try{
				this.conn.close();
			}catch(Throwable e2){}
			this.transEnded=true;
			String msg="数据提交失败或数据连接不正常。数据有可能未能正常写入数据库中\n" + e.getMessage();
			OrmTools.error(logger,msg);
			throw new OrmException(msg);
		}
	}
	
	/**
	 * 手动撤销提交内容。可能抛出OrmException
	 */
	public void rollback(){
		this.transEnded=false;
		try {
			this.conn.rollback();
			this.conn.close();
		} catch (Throwable e) {
			try{
				this.conn.close();
			}catch(Throwable e2){}
			this.transEnded=true;
			OrmTools.error(logger,"数据回滚失败或数据连接不正常。数据有可能未能正常写入数据库中\n" + e.getMessage());
			throw new OrmException("TOOO");
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T tryInsertThenGetAutoValue(IOrmBean bean){
		List<OrmCommand> cbs = BeanParser.generateCmds(bean, new InsertCmdGenerator(), false);
		if(cbs==null || cbs.size() ==0){
			return null;
		}
		if(cbs.size()>1){
			throw new OrmException("不可同时插入超过1行自增长记录");
		}
		OrmInsertCommand cmd = new OrmInsertCommand(cbs.get(0));
		return (T)this.tryInsertThenGetAutoValue(cmd);
	}
	
	/***
	 * 向主键为自增长列（AUTO-INCREASEMENT）的表插入一条记录并返回该行的主键值
	 * @param 一个插入命令
	 * @return 返回插入表的自增长主键值。返回null表示出错
	 */
	Object tryInsertThenGetAutoValue(OrmInsertCommand cmd){
		if(cmd==null){
			throw new OrmException("插入命令不可为空");
		}
				
		try{
			//PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); //only works on MySql
			String[] names=cmd.getReturnFieldNames();
			PreparedStatement stmt = conn.prepareStatement(cmd.sql, names);
			OrmTools.populateParameters(stmt, cmd.params);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			
			if(rs==null){
				throw new OrmException("未能得到自动生成的键值");	
			}
			rs.next();
			Object rtn = rs.getObject(1);
						
			rs.close();
			stmt.close();
			
			return rtn;
		}catch(SQLException e){
			logger.error(e.getMessage());
			logger.error("发生错误的SQL: " + cmd.sql);
			logger.error("参数:" + this.parametersToString(cmd.params));
			this.rollback();
			throw new OrmException("执行插入命令失败:" + e.getMessage());			
		}catch(Throwable e){
			this.rollback();
			throw new OrmException("执行插入命令发生严重错误:" + e.getMessage());
		}
	}
	
	/***
	 * 执行查询。如果出错抛出OrmException，否则返回查询结果。
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param cls
	 * @param sql
	 * @param parameters
	 * @return 不会发回null
	 */
	public <T extends IOrmBean> List<T> trySelect(Class<T> cls, String sql, Object... parameters){
		try{
			PreparedStatement stmt = conn.prepareStatement(sql);
			OrmTools.populateParameters(stmt, parameters);
			if(ConfigBase.logSQL){
				OrmTools.log(logger, sql, parameters);
			}
			ResultSet rs = stmt.executeQuery();
			List<T> rtn=ResultSetMapper.resultSet2Bean(rs, cls);
			rs.close();
			stmt.close();
			return (List<T>) rtn;
		}catch(SQLException e){
			logger.error(e.getMessage());
			logger.error("发生错误的SQL: " + sql);
			logger.error("参数:" + this.parametersToString(parameters));
			this.rollback();
			throw new OrmException("查询命令失败:" + e.getMessage());		
		}
	}
	
	private String parametersToString(Object[] params){
		StringBuilder buf=new StringBuilder(1000);
		for(Object obj: params){
			if(obj==null){
				buf.append("NULL,");
			}else{
				buf.append(obj.toString()).append(",");
			}
		}
		return buf.toString();
	}
	
	/***
	 * 查询单个对象
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param cls
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public <T extends IOrmBean> T trySelectOne(Class<T> cls, String sql, Object... parameters){
		List<T> rtn = this.trySelect(cls, sql, parameters);
		
		if(rtn==null || rtn.size()==0){
			return null;
		}else if(rtn.size()>1){
			logger.error("发生错误的SQL: " + sql);
			logger.error("参数:" + this.parametersToString(parameters));
			throw new OrmException("执行结果未能只影响1行记录。系统将回滚");
		}
		return rtn.get(0);
	}
	
	/***
	 * 查询单值
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param cls
	 * @param sql
	 * @param parameters
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T trySelectScalar(Class<T> cls, String sql, Object... parameters){
		try{
			PreparedStatement stmt = conn.prepareStatement(sql);
			OrmTools.populateParameters(stmt, parameters);
			if(ConfigBase.logSQL){
				OrmTools.log(logger, sql, parameters);
			}
			ResultSet rs = stmt.executeQuery();
			T rtn = null;
			if(rs.next()){
				rtn = (T)rs.getObject(1);
			}
			conn.commit();
			rs.close();
			stmt.close();
			return rtn;
		}catch(SQLException e){
			logger.error(e.getMessage());
			logger.error("发生错误的SQL: " + sql);
			logger.error("参数:" + this.parametersToString(parameters));
			this.rollback();
			throw new OrmException("查询命令失败:" + e.getMessage());		
		}
	}
	
	/***
	 * 更新
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @return
	 */
	public OrmTrans tryUpdate(IOrmBean bean){
		this.tryUpdate(bean, this.neglectNull, this.affectRowType, this.affectRows);
		return this;
	}
	/***
	 * 更新
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @param affectRowType
	 * @param affectRows
	 * @return
	 */
	public OrmTrans tryUpdate(IOrmBean bean, AffectRowType affectRowType, int affectRows){
		this.tryUpdate(bean, this.neglectNull, affectRowType, affectRows);
		return this;
	}
	
	/***
	 * 更新
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param neglectNull
	 * @param bean
	 * @return
	 */
	public OrmTrans tryUpdate(boolean neglectNull, IOrmBean bean){
		this.tryUpdate(bean, neglectNull, this.affectRowType, this.affectRows);
		return this;
	}
	
	/***
	 * 更新
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @param neglectNull
	 * @param affectRowType
	 * @param affectRows
	 * @return
	 */
	public OrmTrans tryUpdate(IOrmBean bean, boolean neglectNull, AffectRowType affectRowType, int affectRows){
		List<OrmCommand> cmds=BeanParser.generateCmds(bean, new UpdateCmdGenerator(),neglectNull);
		for(OrmCommand cmd : cmds){
			this.tryExecute(cmd,affectRowType, affectRows);
		}
		return this;
	}
	
	/***
	 * 删除
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @return
	 */
	public OrmTrans tryDelete(IOrmBean bean){
		this.tryDelete(bean, this.neglectNull, this.affectRowType, this.affectRows);
		return this;
	}
	
	/***
	 * 删除
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @param affectRowType
	 * @param affectRows
	 * @return
	 */
	public OrmTrans tryDelete(IOrmBean bean, AffectRowType affectRowType, int affectRows){
		this.tryDelete(bean, this.neglectNull, affectRowType, affectRows);
		return this;
	}
	
	/***
	 * 删除
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param neglectNull
	 * @param bean
	 * @return
	 */
	public OrmTrans tryDelete(boolean neglectNull, IOrmBean bean){
		this.tryDelete(bean, neglectNull, this.affectRowType, this.affectRows);
		return this;
	}
	
	/***
	 * 删除
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @param neglectNull
	 * @param affectRowType
	 * @param affectRows
	 * @return
	 */
	public OrmTrans tryDelete(IOrmBean bean, boolean neglectNull, AffectRowType affectRowType, int affectRows){
		List<OrmCommand> cmds=BeanParser.generateCmds(bean, new DeleteCmdGenerator(),neglectNull);
		for(OrmCommand cmd : cmds){
			this.tryExecute(cmd,affectRowType, affectRows);
		}
		return this;
	}
	
	/***
	 * 插入
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @return
	 */
	public OrmTrans tryInsert(IOrmBean bean){
		this.tryInsert(bean, this.neglectNull, this.affectRowType, this.affectRows);
		return this;
	}
	
	/***
	 * 插入
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @param affectRowType
	 * @param affectRows
	 * @return
	 */
	public OrmTrans tryInsert(IOrmBean bean, AffectRowType affectRowType, int affectRows){
		this.tryInsert(bean, this.neglectNull, affectRowType, affectRows);
		return this;
	}
	
	/***
	 * 删除
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param neglectNull
	 * @param bean
	 * @return
	 */
	public OrmTrans tryInsert(boolean neglectNull, IOrmBean bean){
		this.tryInsert(bean, neglectNull, this.affectRowType, this.affectRows);
		return this;
	}
	
	/***
	 * 删除
	 * 执行tryExecute之前必须先启动事务，之后必须commit
	 * @param bean
	 * @param neglectNull
	 * @param affectRowType
	 * @param affectRows
	 * @return
	 */
	public OrmTrans tryInsert(IOrmBean bean, boolean neglectNull, AffectRowType affectRowType, int affectRows){
		List<OrmCommand> cmds=BeanParser.generateCmds(bean, new InsertCmdGenerator(),neglectNull);
		for(OrmCommand cmd : cmds){
			this.tryExecute(cmd,affectRowType, affectRows);
		}
		return this;
	}
}
