 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.trans.steps.jsonoutput;

/**
 * Describes a single field in an Json output file
 * 
 * @author Samatar
 * @since 14-june-2010
 *
 */
public class JsonOutputField implements Cloneable
{
    private String  fieldName;
    private String  elementName;
    
    public JsonOutputField(String fieldName, String elementName, int type, String format, int length, int precision, String currencySymbol, String decimalSymbol, String groupSymbol, String nullString, boolean attribute, String attributeParentName)
    {
        this.fieldName      = fieldName;
        this.elementName    = elementName;
    }
    
    public JsonOutputField()
    {
    }

    
    public int compare(Object obj)
    {
    	JsonOutputField field = (JsonOutputField)obj;
        
        return fieldName.compareTo(field.getFieldName());
    }

    public boolean equal(Object obj)
    {
    	JsonOutputField field = (JsonOutputField)obj;
        
        return fieldName.equals(field.getFieldName());
    }
    
    public Object clone()
    {
        try
        {
            Object retval = super.clone();
            return retval;
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }

    
    public String getFieldName()
    {
        return fieldName;
    }
    
    public void setFieldName(String fieldname)
    {
        this.fieldName = fieldname;
    }

    /**
     * @return Returns the elementName.
     */
    public String getElementName()
    {
        return elementName;
    }

    /**
     * @param elementName The elementName to set.
     */
    public void setElementName(String elementName)
    {
        this.elementName = elementName;
    }
}
