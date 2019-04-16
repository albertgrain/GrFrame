package dlmu.mislab.orm.bean;

import dlmu.mislab.orm.OrmCommand;

/***
 * 生成持久化所需的命令对象接口
 * @author GuRui
 *
 */
public interface ICmdGenerator {
	/***
	 * 准备一条持久化命令对象
	 * @param pbd
	 * @param neglectNullValue
	 * @return
	 */
	public OrmCommand prepareCmd(CmdDescriptor pbd, boolean neglectNullValue);
}


