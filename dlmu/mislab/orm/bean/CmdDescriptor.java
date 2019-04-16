package dlmu.mislab.orm.bean;

import java.util.LinkedList;

class CmdDescriptor{
	/***
	 * CRUD的动作对象，是WHERE之前的字段列表
	 */
	final LinkedList<BeanField> targetPart=new LinkedList<BeanField>();
	/***
	 * CRUD的限定条件，是WHERE之后的条件
	 */
	final LinkedList<BeanField> wherePart=new LinkedList<BeanField>();
	/***
	 * 所有链接及有链接关系的子表对象
	 */
	//final LinkedList<ParsedChildDescriptor> childTables=new LinkedList<ParsedChildDescriptor>();

	/***
	 * 本表名
	 */
	String tableName;
}
