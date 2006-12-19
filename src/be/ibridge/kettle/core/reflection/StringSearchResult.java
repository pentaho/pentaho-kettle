package be.ibridge.kettle.core.reflection;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;

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

    public Row toRow()
    {
        Row row = new Row();
        row.addValue(new Value("Transformation or Job", grandParentObject.toString()));
        row.addValue(new Value("Step or Database", parentObject.toString()));
        row.addValue(new Value("String", string));
        row.addValue(new Value("Field name", fieldName));
        return row;
    }
    
    public String toString()
    {
        return parentObject.toString()+" : " + string + " ("+fieldName+")";
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
