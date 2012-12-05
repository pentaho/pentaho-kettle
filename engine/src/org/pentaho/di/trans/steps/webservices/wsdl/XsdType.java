/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.webservices.wsdl;

import org.pentaho.di.core.row.ValueMetaInterface;

public class XsdType 
{
	public final static String DATE = "date";
	public final static String TIME = "time";
	public final static String DATE_TIME = "datetime";
	public final static String INTEGER = "int";
	public final static String INTEGER_DESC = "integer";
	public final static String SHORT = "short";
	public final static String BOOLEAN = "boolean";
	public final static String STRING = "string";
	public final static String DOUBLE = "double";
	public final static String FLOAT = "float";
	public final static String BINARY = "base64Binary";
	public final static String DECIMAL = "decimal";
	
    public final static String[] TYPES = new String[] {STRING, INTEGER, INTEGER_DESC, SHORT, BOOLEAN, DATE, TIME, DATE_TIME, DOUBLE, FLOAT, BINARY, DECIMAL, };
	
	public static int xsdTypeToKettleType(String aXsdType)
	{
		int vRet = ValueMetaInterface.TYPE_NONE;
        if (aXsdType != null)
        {
    		if(aXsdType.equalsIgnoreCase(DATE))
    		{
    			vRet = ValueMetaInterface.TYPE_DATE;
    		}
    		else if(aXsdType.equalsIgnoreCase(TIME))
    		{
    			vRet = ValueMetaInterface.TYPE_DATE;
    		}
    		else if(aXsdType.equalsIgnoreCase(DATE_TIME))
    		{
    			vRet = ValueMetaInterface.TYPE_DATE; 
    		}
    		else if(aXsdType.equalsIgnoreCase(INTEGER) || aXsdType.equalsIgnoreCase(INTEGER_DESC))
    		{
    			vRet = ValueMetaInterface.TYPE_INTEGER;
    		}
    		else if(aXsdType.equalsIgnoreCase(SHORT))
    		{
    			vRet = ValueMetaInterface.TYPE_INTEGER;
    		}
    		else if(aXsdType.equalsIgnoreCase(BOOLEAN))
    		{
    			vRet = ValueMetaInterface.TYPE_BOOLEAN;
    		}
    		else if(aXsdType.equalsIgnoreCase(STRING))
    		{
    			vRet = ValueMetaInterface.TYPE_STRING;
    		}
    		else if(aXsdType.equalsIgnoreCase(DOUBLE))
    		{
    			vRet = ValueMetaInterface.TYPE_NUMBER;
    		}
    		else if(aXsdType.equalsIgnoreCase(BINARY))
    		{
    			vRet = ValueMetaInterface.TYPE_BINARY;
    		}
    		else if(aXsdType.equalsIgnoreCase(DECIMAL))
    		{
    			vRet = ValueMetaInterface.TYPE_BIGNUMBER;
    		}
    		else  
    		{
    			// When all else fails, map it to a String
    			vRet = ValueMetaInterface.TYPE_NONE;
    		}
        }
		return vRet;
	}
}
