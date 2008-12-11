package org.pentaho.di.trans.steps.formula;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;



public class FormulaMetaFunction implements Cloneable
{
    public static final String XML_TAG = "formula";  

    private String fieldName;
    private String formula;

    private int    valueType;
    private int    valueLength;
    private int    valuePrecision;
    
    private boolean removedFromResult;
    
    /**
     * @param fieldName
     * @param calcType
     * @param fieldA
     * @param fieldB
     * @param fieldC
     * @param valueType
     * @param valueLength
     * @param valuePrecision
     */
    public FormulaMetaFunction(String fieldName, String formula, int valueType, int valueLength, int valuePrecision, boolean removedFromResult)
    {
        this.fieldName = fieldName;
        this.formula = formula;
        this.valueType = valueType;
        this.valueLength = valueLength;
        this.valuePrecision = valuePrecision;
        this.removedFromResult = removedFromResult;
    }

    public boolean equals(Object obj)
    {       
        if (obj != null && (obj.getClass().equals(this.getClass())))
        {
        	FormulaMetaFunction mf = (FormulaMetaFunction)obj;
            return (getXML() == mf.getXML());
        }

        return false;
    }    
    
    public Object clone()
    {
        try
        {
            FormulaMetaFunction retval = (FormulaMetaFunction) super.clone();
            return retval;
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }
    
    public String getXML()
    {
        String xml="";
        
        xml+="<"+XML_TAG+">";
        
        xml+=XMLHandler.addTagValue("field_name",      fieldName);
        xml+=XMLHandler.addTagValue("formula_string",  formula);
        xml+=XMLHandler.addTagValue("value_type",      ValueMeta.getTypeDesc(valueType));
        xml+=XMLHandler.addTagValue("value_length",    valueLength);
        xml+=XMLHandler.addTagValue("value_precision", valuePrecision);
        xml+=XMLHandler.addTagValue("remove",          removedFromResult);
        
        xml+="</"+XML_TAG+">";
     
        return xml;
    }
    
    public FormulaMetaFunction(Node calcnode)
    {
        fieldName      = XMLHandler.getTagValue(calcnode, "field_name");
        formula        = XMLHandler.getTagValue(calcnode, "formula_string");
        valueType      = ValueMeta.getType( XMLHandler.getTagValue(calcnode, "value_type") );
        valueLength    = Const.toInt( XMLHandler.getTagValue(calcnode, "value_length"), -1 );
        valuePrecision = Const.toInt( XMLHandler.getTagValue(calcnode, "value_precision"), -1 );
        removedFromResult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(calcnode, "remove"));
    }
    
    public void saveRep(Repository rep, long id_transformation, long id_step, int nr) throws KettleException
    {
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_name",          fieldName);
        rep.saveStepAttribute(id_transformation, id_step, nr, "formula_string",      formula);
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_type",          ValueMeta.getTypeDesc(valueType));
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_length",        valueLength);
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_precision",     valuePrecision);
        rep.saveStepAttribute(id_transformation, id_step, nr, "remove",              removedFromResult);
    }

    public FormulaMetaFunction(Repository rep, long id_step, int nr) throws KettleException
    {
        fieldName      = rep.getStepAttributeString(id_step, nr, "field_name");
        formula        = rep.getStepAttributeString(id_step, nr, "formula_string");
        valueType      = ValueMeta.getType( rep.getStepAttributeString(id_step, nr, "value_type") );
        valueLength    = (int)rep.getStepAttributeInteger(id_step, nr,  "value_length");
        valuePrecision = (int)rep.getStepAttributeInteger(id_step, nr, "value_precision");
        removedFromResult = rep.getStepAttributeBoolean(id_step, nr, "remove");
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
     * @return Returns the valueLength.
     */
    public int getValueLength()
    {
        return valueLength;
    }

    /**
     * @param valueLength The valueLength to set.
     */
    public void setValueLength(int valueLength)
    {
        this.valueLength = valueLength;
    }

    /**
     * @return Returns the valuePrecision.
     */
    public int getValuePrecision()
    {
        return valuePrecision;
    }

    /**
     * @param valuePrecision The valuePrecision to set.
     */
    public void setValuePrecision(int valuePrecision)
    {
        this.valuePrecision = valuePrecision;
    }

    /**
     * @return Returns the valueType.
     */
    public int getValueType()
    {
        return valueType;
    }

    /**
     * @param valueType The valueType to set.
     */
    public void setValueType(int valueType)
    {
        this.valueType = valueType;
    }

    /**
     * @return Returns the removedFromResult.
     */
    public boolean isRemovedFromResult()
    {
        return removedFromResult;
    }

    /**
     * @param removedFromResult The removedFromResult to set.
     */
    public void setRemovedFromResult(boolean removedFromResult)
    {
        this.removedFromResult = removedFromResult;
    }

    /**
     * @return the formula
     */
    public String getFormula()
    {
        return formula;
    }

    /**
     * @param formula the formula to set
     */
    public void setFormula(String formula)
    {
        this.formula = formula;
    }
}
