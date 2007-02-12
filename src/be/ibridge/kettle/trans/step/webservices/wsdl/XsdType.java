package be.ibridge.kettle.trans.step.webservices.wsdl;

import be.ibridge.kettle.core.value.Value;

public class XsdType 
{
	public final static String DATE = "xsd:date";
	public final static String TIME = "xsd:time";
	public final static String DATE_TIME = "xsd:datetime";
	public final static String INTEGER = "xsd:int";
	public final static String SHORT = "xsd:short";
	public final static String BOOLEAN = "xsd:boolean";
	public final static String STRING = "xsd:string";
	public final static String DOUBLE = "xsd:double";
	public final static String FLOAT = "xsd:float";
	public final static String BINARY = "xsd:base64Binary";
	
	
	public static int xdsTypeToKettleType(String aXsdType)
	{
		int vRet = Value.VALUE_TYPE_NONE;
		if(aXsdType.equals(DATE))
		{
			vRet = Value.VALUE_TYPE_DATE;
		}
		else if(aXsdType.equals(TIME))
		{
			vRet = Value.VALUE_TYPE_DATE;
		}
		else if(aXsdType.equals(DATE_TIME))
		{
			vRet = Value.VALUE_TYPE_DATE; 
		}
		else if(aXsdType.equals(INTEGER))
		{
			vRet = Value.VALUE_TYPE_INTEGER;
		}
		else if(aXsdType.equals(SHORT))
		{
			vRet = Value.VALUE_TYPE_INTEGER;
		}
		else if(aXsdType.equals(BOOLEAN))
		{
			vRet = Value.VALUE_TYPE_BOOLEAN;
		}
		else if(aXsdType.equals(STRING))
		{
			vRet = Value.VALUE_TYPE_STRING;
		}
		else if(aXsdType.equals(DOUBLE))
		{
			vRet = Value.VALUE_TYPE_NUMBER;
		}
		else if(aXsdType.equals(BINARY))
		{
			vRet = Value.VALUE_TYPE_BINARY;
		}
		return vRet;
	}
}
