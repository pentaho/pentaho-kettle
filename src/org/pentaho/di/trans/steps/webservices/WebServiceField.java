package org.pentaho.di.trans.steps.webservices;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.webservices.wsdl.XsdType;


public class WebServiceField
{
    private String name;
    
    private String wsName;
    
    private String xsdType;

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
