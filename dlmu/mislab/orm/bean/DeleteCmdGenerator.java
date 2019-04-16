package dlmu.mislab.orm.bean;

import java.util.LinkedList;

import dlmu.mislab.orm.OrmException;
import dlmu.mislab.orm.OrmCommand;

public class DeleteCmdGenerator implements ICmdGenerator {

	@Override
	public OrmCommand prepareCmd(CmdDescriptor pbd, boolean neglectNullValue) {
		if(pbd.wherePart.size()==0){
			throw new OrmException("使用Bean便捷Delete时，必须有Where条件限制");
		}
		
		OrmCommand rtn= new OrmCommand();
		LinkedList<Object> params=new LinkedList<Object>();
		StringBuilder sql=new StringBuilder();
		sql.append("DELETE FROM ").append(pbd.tableName).append(" WHERE ");
		for(BeanField pf:pbd.wherePart){	
			if(pf.isFakeKey){ //Ammended by Grui on 13th Dec, 2017
				continue;
			}
			if(pf.fieldValue==null){
				if(pf.isKey || pf.isFakeKey){
					throw new OrmException("执行删除操作时，主键不可为空值");
				}else{
					if(!neglectNullValue && !pf.neglectNull){
						sql.append(pf.fieldName).append(" IS NULL AND ");
					}
				}
			}else{
				sql.append(pf.fieldName).append("=? AND ");
			}
			params.addLast(pf.fieldValue);
		}
		sql.setLength(sql.length()-5);
		rtn.sql=sql.toString();
		rtn.params = params.toArray();
		return rtn;
	}
}
