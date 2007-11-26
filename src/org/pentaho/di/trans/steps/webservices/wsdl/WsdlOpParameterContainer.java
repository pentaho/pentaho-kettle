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
                return new String[] {parameter.getItemXmlType().getLocalPart()};
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
                return parameter.getItemXmlType().getLocalPart();
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
            if (!WebServiceMeta.XSD_NS_URI.equals(parameter.getItemXmlType().getNamespaceURI()))
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
