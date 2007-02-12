package be.ibridge.kettle.trans.step.webservices.wsdl;

import be.ibridge.kettle.core.value.Value;

public class WSDLParameter 
{
	public final static String XSD_DATE = "xsd:date";
	private String name;
	
	private String type;
	
	public WSDLParameter(String aName, String aType)
	{
		name = aName;
		type = aType;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
	
	public int getValueType()
	{
		int typeRet;
		if (type.equals(XsdType.DATE) || type.equals(XsdType.TIME) || type.equals(XsdType.DATE_TIME))
		{
			typeRet = Value.VALUE_TYPE_DATE;
		}
		else if (type.equals(XsdType.DOUBLE) || type.equals(XsdType.FLOAT))
		{
			typeRet = Value.VALUE_TYPE_NUMBER;
		}
		else if (type.equals(XsdType.INTEGER) || type.equals(XsdType.SHORT))
		{
			typeRet = Value.VALUE_TYPE_INTEGER;
		}
		else if (type.equals(XsdType.BOOLEAN))
		{
			typeRet = Value.VALUE_TYPE_BOOLEAN;
		}
		else
		{
			typeRet = Value.VALUE_TYPE_STRING;
		}
		return typeRet;
	}
	
	public String toString()
	{
		return name;
	}
}
