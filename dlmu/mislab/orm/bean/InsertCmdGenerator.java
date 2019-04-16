package dlmu.mislab.orm.bean;

import java.util.LinkedList;

import dlmu.mislab.orm.OrmException;
import dlmu.mislab.orm.OrmCommand;

public class InsertCmdGenerator implements ICmdGenerator{

	@Override
	public OrmCommand prepareCmd(CmdDescriptor pbd, boolean neglectNullValue) {
		if(pbd.targetPart.size()==0){
			throw new OrmException("使用Bean便捷Insert时，必须给待插入内容赋值");
		}
		
		OrmCommand einfo= new OrmCommand();
		StringBuilder sql=new StringBuilder();
		sql.append("INSERT INTO ").append(pbd.tableName).append(" (");
		StringBuilder sql2=new StringBuilder();
		LinkedList<Object> params=new LinkedList<Object>();
				
		for(BeanField bf: pbd.targetPart){
			if(bf.isRefToChild){
				continue;
			}
			if(bf.fieldValue==null){
				if(!neglectNullValue && !bf.neglectNull){
					sql.append(bf.fieldName).append(", ");
					sql2.append("NULL, ");
				}
			}else{
				sql.append(bf.fieldName).append(", ");
				sql2.append("?, ");
				params.addLast(bf.fieldValue);
			}
		}
		sql.setLength(sql.length()-2);
		sql2.setLength(sql2.length()-2);
		sql.append(") VALUES (");
		sql.append(sql2).append(")");

		einfo.sql=sql.toString();
		einfo.params = params.toArray();
		return einfo;
	}
	
}
