/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
