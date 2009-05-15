/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.core.reflection;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

public class StringSearchResult
{
	private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String string;
    private Object parentObject;
    private String fieldName;
    private Object grandParentObject;
    
    /**
     * @param string
     * @param parentObject
     */
    public StringSearchResult(String string, Object parentObject, Object grandParentObject, String fieldName)
    {
        super();

        this.string = string;
        this.parentObject = parentObject;
        this.grandParentObject = grandParentObject;
        this.fieldName = fieldName;
    }
    
    public Object getParentObject()
    {
        return parentObject;
    }
    
    public void setParentObject(Object parentObject)
    {
        this.parentObject = parentObject;
    }
    
    public String getString()
    {
        return string;
    }
    
    public void setString(String string)
    {
        this.string = string;
    }
    
    public static final RowMetaInterface getResultRowMeta()
    {
    	RowMetaInterface rowMeta = new RowMeta();
        rowMeta.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "SearchResult.TransOrJob"), ValueMetaInterface.TYPE_STRING));
        rowMeta.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "SearchResult.StepDatabaseNotice"), ValueMetaInterface.TYPE_STRING));
        rowMeta.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "SearchResult.String"), ValueMetaInterface.TYPE_STRING));
        rowMeta.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "SearchResult.FieldName"), ValueMetaInterface.TYPE_STRING));
    	return rowMeta;
    }

    public Object[] toRow()
    {
    	return new Object[] { grandParentObject.toString(), parentObject.toString(), string, fieldName, };
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(parentObject.toString()).append(" : ").append(string);
        sb.append(" (").append(fieldName).append(")");
        return sb.toString();
    }

    /**
     * @return Returns the fieldName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return the grandParentObject
     */
    public Object getGrandParentObject()
    {
        return grandParentObject;
    }

    /**
     * @param grandParentObject the grandParentObject to set
     */
    public void setGrandParentObject(Object grandParentObject)
    {
        this.grandParentObject = grandParentObject;
    }
}
