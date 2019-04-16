package dlmu.mislab.orm;

import java.util.LinkedList;


class CmdDescriptorForSelect{
	/***
	 * CRUD的动作对象，是WHERE之前的字段列表
	 */
	final LinkedList<BeanFieldForSelect> targetPart=new LinkedList<BeanFieldForSelect>();
	/***
	 * CRUD的限定条件，是WHERE之后的条件
	 */
	final LinkedList<BeanFieldForSelect> wherePart=new LinkedList<BeanFieldForSelect>();
	/***
	 * 所有链接及有链接关系的子表对象
	 */
	//final LinkedList<ParsedChildDescriptor> childTables=new LinkedList<ParsedChildDescriptor>();

	/***
	 * 本表名
	 */
	String tableName;
}
