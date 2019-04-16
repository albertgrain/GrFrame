package dlmu.mislab.orm;

class BeanFieldForSelect{
	String fieldName;
	Object fieldValue=null;
	boolean isFakeKey=false;
	boolean isKey=false;
	boolean auto=false; //Is key an auto-increase field;
	
	boolean isFnKey=false;
	String fnTableName;
	String fnKeyName;
	
	boolean isRefToChild = false;
	Class<?> childType=null;
	boolean isArray=false;
}