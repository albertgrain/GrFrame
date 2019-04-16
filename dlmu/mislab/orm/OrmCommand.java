package dlmu.mislab.orm;

public class OrmCommand {
	public String sql;
	public Object[] params;
	public String[] keyFields;
	
	public OrmCommand(){
		
	}
	
	public OrmCommand(String sql, Object[] params){
		this.sql=sql;
		this.params=params;
	}	
}