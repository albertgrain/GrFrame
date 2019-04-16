package dlmu.mislab.orm;


public class SelectCommand extends OrmCommand {

	public SelectCommand(){
		
	}
	
	public SelectCommand(String sql, Object[] params){
		this.sql=sql;
		this.params=params;
	}	

}


