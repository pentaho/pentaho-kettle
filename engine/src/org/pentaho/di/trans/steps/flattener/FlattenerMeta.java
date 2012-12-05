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

package org.pentaho.di.trans.steps.flattener;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * The flattener step meta-data
 * 
 * @since 17-jan-2006
 * @author Matt
 */

public class FlattenerMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = FlattenerMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** The field to flatten */
    private String fieldName;
    
    /** Fields to flatten, same data type as input */
    private String                    targetField[];

    public FlattenerMeta()
    {
        super(); // allocate BaseStepMeta
    }

    public String getFieldName()
    {
        return fieldName;
    }
    
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
    
    public String[] getTargetField()
    {
        return targetField;
    }
    
    public void setTargetField(String[] targetField)
    {
        this.targetField = targetField;
    }
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
        readData(stepnode);
    }

    public void allocate(int nrfields)
    {
        targetField = new String[nrfields];
    }

    public Object clone()
    {
        Object retval = super.clone();
        return retval;
    }

    public void setDefault()
    {
        int nrfields = 0;

        allocate(nrfields);
    }

    @Override
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

        // Remove the key value (there will be different entries for each output row)
        //
        if (fieldName != null && fieldName.length() > 0)
        {
            int idx = row.indexOfValue(fieldName);
            if (idx < 0) { 
            	throw new KettleStepException(BaseMessages.getString(PKG, "FlattenerMeta.Exception.UnableToLocateFieldInInputFields", fieldName )); //$NON-NLS-1$ //$NON-NLS-2$ 
            } 
            
            ValueMetaInterface v = row.getValueMeta(idx);
            row.removeValueMeta(idx);
            
            for (int i=0;i<targetField.length;i++)
            {
                ValueMetaInterface value = v.clone();
                value.setName(targetField[i]);
                value.setOrigin(name);
                
                row.addValueMeta(value);
            }
        }
        else
        {
            throw new KettleStepException(BaseMessages.getString(PKG, "FlattenerMeta.Exception.FlattenFieldRequired")); //$NON-NLS-1$
        }
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            fieldName = XMLHandler.getTagValue(stepnode, "field_name"); //$NON-NLS-1$

            Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
            int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
                targetField[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "FlattenerMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
        }
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("      " + XMLHandler.addTagValue("field_name", fieldName)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("      <fields>" + Const.CR); //$NON-NLS-1$
        for (int i = 0; i < targetField.length; i++)
        {
            retval.append("        <field>" + Const.CR); //$NON-NLS-1$
            retval.append("          " + XMLHandler.addTagValue("name", targetField[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          </field>" + Const.CR); //$NON-NLS-1$
        }
        retval.append("        </fields>" + Const.CR); //$NON-NLS-1$

        return retval.toString();
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {

    	try
        {
            fieldName = rep.getStepAttributeString(id_step, "field_name"); //$NON-NLS-1$

            int nrvalues = rep.countNrStepAttributes(id_step, "target_field"); //$NON-NLS-1$

            allocate(nrvalues);

            for (int i = 0; i < nrvalues; i++)
            {
                targetField[i] = rep.getStepAttributeString(id_step, i, "target_field"); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "FlattenerMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "field_name", fieldName); //$NON-NLS-1$

            for (int i = 0; i < targetField.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "target_field", targetField[i]); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "FlattenerMeta.Exception.UnableToSaveStepInfoToRepository") + id_step, e); //$NON-NLS-1$
        }
    }
    
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {

    	CheckResult cr;

        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FlattenerMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "FlattenerMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new Flattener(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new FlattenerData();
    }

}
