package be.ibridge.kettle.trans.step.webservices;

import be.ibridge.kettle.core.value.Value;

public class FieldLinkWebServiceField 
{
	private Value field;
	
	private String webServiceField;
	
	public FieldLinkWebServiceField(Value aField, String aWebServiceField)
	{
		field = aField;
		webServiceField = aWebServiceField;
	}

	public Value getField() {
		return field;
	}

	public void setField(Value field) {
		this.field = field;
	}

	public String getWebServiceField() {
		return webServiceField;
	}

	public void setWebServiceField(String webServiceField) {
		this.webServiceField = webServiceField;
	}
}
