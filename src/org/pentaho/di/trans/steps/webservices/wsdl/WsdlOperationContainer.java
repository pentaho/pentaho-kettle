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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOpParameter.ParameterMode;

public class WsdlOperationContainer implements WsdlParamContainer
{

    private ParameterMode mode;

    private WsdlOperation operation;

    public WsdlOperationContainer(WsdlOperation operation, ParameterMode mode)
    {
        this.mode = mode;
        this.operation = operation;
    }

    public String getContainerName()
    {
        return null;
    }

    public String[] getParamNames()
    {
        List<String> paramsRet = new ArrayList<String>();
        for (WsdlOpParameter param : operation.getParameters()) {
            if (param.getMode().equals(mode))
            {
                paramsRet.add(param.getName().getLocalPart());
            }
        }
        return (String[]) paramsRet.toArray(new String[paramsRet.size()]);
    }

    public String getParamType(String paramName)
    {
        String typeRet = null;
        for (WsdlOpParameter param : operation.getParameters())
        {
            if (param.getMode().equals(mode) && param.getName().getLocalPart().equals(paramName))
            {
                typeRet = param.getXmlType().getLocalPart();
                break;
            }
        }
        return typeRet;
    }

    public String getItemName()
    {
        // This method is only relevant for output containers
        return null;
    }

    public boolean isArray()
    {
        return false;
    }
}
