package dlmu.mislab.orm.bean;

import dlmu.mislab.orm.IOrmBean;

class BeanField{
	String fieldName;
	Object fieldValue=null;
	boolean isAuto=false;
	boolean isFakeKey=false;
	boolean isKey=false;
	boolean neglectNull=true;

	boolean isRefToChild = false;
	IOrmBean[] children=null;	
}