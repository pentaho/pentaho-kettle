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
package org.pentaho.di.trans.steps.webservices;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.webservices.wsdl.XsdType;


public class WebServiceField implements Cloneable
{
    private String name;
    
    private String wsName;
    
    private String xsdType;
    
    public WebServiceField clone() {
    	try {
    		return (WebServiceField) super.clone();
    	}
    	catch(CloneNotSupportedException e) {
    		e.printStackTrace();
    		return null;
    	}
    }
    
    @Override
    public String toString() {
    	
    	return name!=null ? name : super.toString();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getWsName()
    {
        return wsName;
    }

    public void setWsName(String wsName)
    {
        this.wsName = wsName;
    }

    public String getXsdType()
    {
        return xsdType;
    }

    public void setXsdType(String xsdType)
    {
        this.xsdType = xsdType;
    }
    
    public int getType()
    {
        return XsdType.xsdTypeToKettleType(xsdType);
    }
    
    /**
     * We consider a field to be complex if it's a type we don't recognize.
     * In that case, we will give back XML as a string.
     * @return
     */
    public boolean isComplex() {
    	return getType()==ValueMetaInterface.TYPE_NONE;
    }
}
