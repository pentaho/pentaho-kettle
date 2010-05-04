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

import javax.xml.namespace.QName;

import org.pentaho.di.trans.steps.webservices.WebServiceMeta;

public class WsdlOpParameterContainer implements WsdlParamContainer
{
    private WsdlOpParameter parameter;

    public WsdlOpParameterContainer(WsdlOpParameter parameter)
    {
        this.parameter = parameter;
    }

    public String getContainerName()
    {
        return parameter.getName().getLocalPart();
    }

    public String[] getParamNames()
    {
        if (parameter.isArray())
        {
            if (parameter.getItemComplexType() != null)
            {
                return (String[]) parameter.getItemComplexType().getElementNames().toArray(new String[parameter.getItemComplexType().getElementNames().size()]);
            }
            else
            {
            	if (parameter.getItemXmlType()!=null) 
            	{
            		return new String[] { parameter.getItemXmlType().getLocalPart()};
            	}
            	else
            	{
            		return new String[] { parameter.getName().getLocalPart(), };
            	}
            }
        }
        else
        {
            return new String[] {parameter.getName().getLocalPart()};
        }
    }

    public String getParamType(String paramName)
    {
        if (parameter.isArray())
        {
            if (parameter.getItemComplexType() != null)
            {
                QName name = parameter.getItemComplexType().getElementType(paramName);
                return name == null ? null : name.getLocalPart();
            }
            else
            {
            	if (parameter.getItemXmlType()!=null) 
            	{
            		return parameter.getItemXmlType().getLocalPart();
            	}
            	else
            	{
            		return null;
            	}
            }
        }
        else if (paramName.equals(parameter.getName().getLocalPart()))
        {
            return parameter.getXmlType().getLocalPart();
        }
        else
        {
            return null;
        }
    }

    public String getItemName()
    {
        if (parameter.isArray())
        {
            if (parameter.getItemXmlType()!=null && !WebServiceMeta.XSD_NS_URI.equals(parameter.getItemXmlType().getNamespaceURI()))
            {
                return parameter.getItemXmlType().getLocalPart();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return parameter.getName().getLocalPart();
        }
    }

    public boolean isArray()
    {
        return parameter.isArray();
    }
}
