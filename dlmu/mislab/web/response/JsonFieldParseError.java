package dlmu.mislab.web.response;

import dlmu.mislab.web.WebLogicError;

public class JsonFieldParseError extends Err{
	private String field=null;
	private String field_cn=null;
	
	public JsonFieldParseError(String badFieldName, String badFiledNameCn){
		this.setErr(WebLogicError.JSON_FIELD_FORMAT_WRONG);
		this.field=badFieldName;
		this.field_cn=badFiledNameCn;
	}

	public String getField() {
		return field;
	}

	public String getField_cn() {
		return field_cn;
	}

}
