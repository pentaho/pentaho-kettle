/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.janino;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;



public class JaninoMetaFunction implements Cloneable
{
    public static final String XML_TAG = "formula";  

    private String fieldName;
    private String formula;

    private int    valueType;
    private int    valueLength;
    private int    valuePrecision;
    
    private String replaceField;
    
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
    public JaninoMetaFunction(String fieldName, String formula, int valueType, int valueLength, int valuePrecision, String replaceField)
    {
        this.fieldName = fieldName;
        this.formula = formula;
        this.valueType = valueType;
        this.valueLength = valueLength;
        this.valuePrecision = valuePrecision;
        this.replaceField = replaceField;
    }

    public boolean equals(Object obj)
    {       
        if (obj != null && (obj.getClass().equals(this.getClass())))
        {
        	JaninoMetaFunction mf = (JaninoMetaFunction)obj;
            return (getXML() == mf.getXML());
        }

        return false;
    }    
    
    public Object clone()
    {
        try
        {
            JaninoMetaFunction retval = (JaninoMetaFunction) super.clone();
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
        xml+=XMLHandler.addTagValue("replace_field",   replaceField);
        
        
        xml+="</"+XML_TAG+">";
     
        return xml;
    }
    
    public JaninoMetaFunction(Node calcnode)
    {
        fieldName      = XMLHandler.getTagValue(calcnode, "field_name");
        formula        = XMLHandler.getTagValue(calcnode, "formula_string");
        valueType      = ValueMeta.getType( XMLHandler.getTagValue(calcnode, "value_type") );
        valueLength    = Const.toInt( XMLHandler.getTagValue(calcnode, "value_length"), -1 );
        valuePrecision = Const.toInt( XMLHandler.getTagValue(calcnode, "value_precision"), -1 );
        replaceField   = XMLHandler.getTagValue(calcnode, "replace_field");
    }
    
    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step, int nr) throws KettleException
    {
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_name",          fieldName);
        rep.saveStepAttribute(id_transformation, id_step, nr, "formula_string",      formula);
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_type",          ValueMeta.getTypeDesc(valueType));
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_length",        valueLength);
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_precision",     valuePrecision);
        rep.saveStepAttribute(id_transformation, id_step, nr, "replace_field",       replaceField);
    }

    public JaninoMetaFunction(Repository rep, ObjectId id_step, int nr) throws KettleException
    {
        fieldName      = rep.getStepAttributeString(id_step, nr, "field_name");
        formula        = rep.getStepAttributeString(id_step, nr, "formula_string");
        valueType      = ValueMeta.getType( rep.getStepAttributeString(id_step, nr, "value_type") );
        valueLength    = (int)rep.getStepAttributeInteger(id_step, nr,  "value_length");
        valuePrecision = (int)rep.getStepAttributeInteger(id_step, nr, "value_precision");
        replaceField   = rep.getStepAttributeString(id_step, nr, "replace_field");
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

	/**
	 * @return the replaceField
	 */
	public String getReplaceField() {
		return replaceField;
	}

	/**
	 * @param replaceField the replaceField to set
	 */
	public void setReplaceField(String replaceField) {
		this.replaceField = replaceField;
	}
}
