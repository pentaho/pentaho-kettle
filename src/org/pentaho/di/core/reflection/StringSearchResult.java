package org.pentaho.di.core.reflection;

import org.pentaho.di.core.Messages;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class StringSearchResult
{
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
        rowMeta.addValueMeta(new ValueMeta(Messages.getString("SearchResult.TransOrJob"), ValueMetaInterface.TYPE_STRING));
        rowMeta.addValueMeta(new ValueMeta(Messages.getString("SearchResult.StepDatabaseNotice"), ValueMetaInterface.TYPE_STRING));
        rowMeta.addValueMeta(new ValueMeta(Messages.getString("SearchResult.String"), ValueMetaInterface.TYPE_STRING));
        rowMeta.addValueMeta(new ValueMeta(Messages.getString("SearchResult.FieldName"), ValueMetaInterface.TYPE_STRING));
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
