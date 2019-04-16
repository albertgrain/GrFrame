package dlmu.mislab.orm;

import dlmu.mislab.common.ConfigBase;

public final class OrmTooManyRowsAffectedException extends OrmException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String EXP_MSG="SQL命令的执行结果所影响了超过"+ConfigBase.MAX_AFFECTED_ROW_BY_ONE_TRANSACTION+"行的数据库记录。为防止意外发生，命令未被执行。";
	
	public OrmTooManyRowsAffectedException(String msg) {
		super(msg);
	}
	public OrmTooManyRowsAffectedException(OrmCommand cmd) {
		super(EXP_MSG);
	}
}
