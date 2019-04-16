package dlmu.mislab.orm.bean;

import java.util.LinkedList;

import dlmu.mislab.orm.OrmException;
import dlmu.mislab.orm.OrmCommand;

public class UpdateCmdGenerator implements ICmdGenerator {

	@Override
	public OrmCommand prepareCmd(CmdDescriptor pbd, boolean neglectNullValue) {
		if(pbd.targetPart.size()==0){
			throw new OrmException("使用Bean便捷Update时，必须有待更新内容");
		}
		
		if(pbd.wherePart.size()==0){
			throw new OrmException("使用Bean便捷Update时，必须有Where条件限制");
		}
		
		OrmCommand einfo= new OrmCommand();
		StringBuilder sql=new StringBuilder();
		LinkedList<Object> params=new LinkedList<Object>();
		sql.append("UPDATE ").append(pbd.tableName).append(" SET ");
		
		boolean hasSthToUpdate=false;
		for(BeanField pf:pbd.targetPart){
			if(pf.isKey || pf.isRefToChild){
				continue;
			}
			if(pf.fieldValue==null){
				if(!neglectNullValue && !pf.neglectNull){
					sql.append(pf.fieldName).append("=NULL, ");
					hasSthToUpdate=true;
				}
			}else{
				sql.append(pf.fieldName).append("=?, ");
				params.addLast(pf.fieldValue);
				hasSthToUpdate=true;
			}
		}
		if(!hasSthToUpdate){
			throw new OrmException("没有可以更新的字段");
		}

		sql.setLength(sql.length()-2);
		sql.append(" WHERE ");

		for(BeanField pf:pbd.wherePart){
			if(pf.isFakeKey){ //Ammended by Grui on 13th Dec, 2017
				continue;
			}
			if(pf.fieldValue==null){
				if(pf.isKey){
					throw new OrmException("执行更新操作时，主键不可为空值");
				}else{
					if(!neglectNullValue && !pf.neglectNull){
						sql.append(pf.fieldName).append(" IS NULL AND ");
					}
				}
			}else{
				sql.append(pf.fieldName).append("=? AND ");
				params.addLast(pf.fieldValue);
			}
		}
		sql.setLength(sql.length()-5);
		einfo.sql=sql.toString();
		einfo.params = params.toArray();
		return einfo;
	}
}
