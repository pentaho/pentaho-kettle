package be.ibridge.kettle.trans.step.webservices.wsdl;

import be.ibridge.kettle.core.value.Value;

public class XsdType 
{
	public final static String DATE = "date";
	public final static String TIME = "time";
	public final static String DATE_TIME = "datetime";
	public final static String INTEGER = "int";
	public final static String SHORT = "short";
	public final static String BOOLEAN = "boolean";
	public final static String STRING = "string";
	public final static String DOUBLE = "double";
	public final static String FLOAT = "float";
	public final static String BINARY = "base64Binary";
	
	
	public static int xsdTypeToKettleType(String aXsdType)
	{
		int vRet = Value.VALUE_TYPE_NONE;
        if (aXsdType != null)
        {
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
        }
		return vRet;
	}
}
