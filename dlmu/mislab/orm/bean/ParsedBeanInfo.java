package dlmu.mislab.orm.bean;

import java.util.LinkedList;
import java.util.List;

/***
 * Parsed annotation info of a IOrmBean
 * @author GuRui
 *
 */
public class ParsedBeanInfo{
	/**
	 * All fields in a bean
	 */
	List<BeanField> fields=new LinkedList<BeanField>();
	/***
	 * The main table name of the bean
	 */
	String tableName=null;
	/***
	 * All child table objects (IOrmBean type)
	 */
	List<ParsedBeanInfo> childTables = new LinkedList<ParsedBeanInfo>();
}
