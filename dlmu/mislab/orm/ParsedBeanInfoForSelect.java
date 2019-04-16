package dlmu.mislab.orm;

import java.util.LinkedList;
import java.util.List;


/***
 * Parsed annotation info of a IOrmBean
 * @author GuRui
 *
 */
public class ParsedBeanInfoForSelect{
	/**
	 * All fields in a bean
	 */
	List<BeanFieldForSelect> fields=new LinkedList<BeanFieldForSelect>();
	/***
	 * The main table name of the bean
	 */
	String tableName=null;
	/***
	 * All child table objects (IOrmBean type)
	 */
	List<Class<?>> childTypes;
}
