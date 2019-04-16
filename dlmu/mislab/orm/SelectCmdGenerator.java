package dlmu.mislab.orm;

import java.util.LinkedList;


class SelectCmdGenerator{

	SelectCommand prepareSelectCmd(CmdDescriptorForSelect pbd) {
		if(pbd.wherePart.size()==0){
			throw new OrmException("使用Bean便捷Where时，必须有Where条件限制");
		}
		
		StringBuilder sql=new StringBuilder();
		LinkedList<Object> params=new LinkedList<Object>();
		SelectCommand einfo= new SelectCommand();

		sql.append("SELECT ");
		for(BeanFieldForSelect pf: pbd.targetPart){
			if(!pf.isRefToChild){
				sql.append(pf.fieldName).append(", ");
			}
		}
		sql.setLength(sql.length()-2);

		sql.append(" FROM ").append(pbd.tableName);

		sql.append(" WHERE ");
		for(BeanFieldForSelect pf: pbd.wherePart){
			sql.append(pf.fieldName).append("=? AND ");
			params.add(pf.fieldValue);
		}
		sql.setLength(sql.length()-5);
		einfo.sql=sql.toString();
		einfo.params = params.toArray();

		return einfo;
	}

}
