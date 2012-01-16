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

package org.pentaho.di.trans.steps.mappingoutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
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
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.w3c.dom.Node;




/*
 * Created on 02-jun-2003
 * 
 */

public class MappingOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = MappingOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/*
    private String  fieldName[];

    private int     fieldType[];

    private int     fieldLength[];

    private int     fieldPrecision[];
    
    private boolean fieldAdded[];
    */
	
    private volatile List<MappingValueRename> inputValueRenames;
    private volatile List<MappingValueRename> outputValueRenames;
	
    public MappingOutputMeta()
    {
        super(); // allocate BaseStepMeta
        inputValueRenames = new ArrayList<MappingValueRename>();
        inputValueRenames = new ArrayList<MappingValueRename>();
    }

    /*
     * @return Returns the fieldLength.
     *
    public int[] getFieldLength()
    {
        return fieldLength;
    }
    */

    /*
     * @param fieldLength The fieldLength to set.
    public void setFieldLength(int[] fieldLength)
    {
        this.fieldLength = fieldLength;
    }
     */

    /*
     * @return Returns the fieldName.
    public String[] getFieldName()
    {
        return fieldName;
    }
     */

    /*
     * @param fieldName The fieldName to set.
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }
     */

    /*
     * @return Returns the fieldPrecision.
    public int[] getFieldPrecision()
    {
        return fieldPrecision;
    }
     */

    /*
     * @param fieldPrecision The fieldPrecision to set.
    public void setFieldPrecision(int[] fieldPrecision)
    {
        this.fieldPrecision = fieldPrecision;
    }
     */

    /*
     * @return Returns the fieldType.
    public int[] getFieldType()
    {
        return fieldType;
    }
     */

    /*
     * @param fieldType The fieldType to set.
    public void setFieldType(int[] fieldType)
    {
        this.fieldType = fieldType;
    }
     */

    /*
    public boolean[] getFieldAdded()
    {
        return fieldAdded;
    }
    
    public void setFieldAdded(boolean[] fieldAdded)
    {
        this.fieldAdded = fieldAdded;
    }
    */

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public Object clone()
    {
        MappingOutputMeta retval = (MappingOutputMeta) super.clone();

        /*
        int nrfields = fieldName.length;

        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.fieldType[i] = fieldType[i];
            retval.fieldLength[i] = fieldLength[i];
            retval.fieldPrecision[i] = fieldPrecision[i];
            retval.fieldAdded[i] = fieldAdded[i];
        }
		*/
        
        return retval;
    }

    public void allocate(int nrfields)
    {
    	/*
        fieldName      = new String[nrfields];
        fieldType      = new int[nrfields];
        fieldLength    = new int[nrfields];
        fieldPrecision = new int[nrfields];
        fieldAdded     = new boolean[nrfields];
        */
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
        	/*
            Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
            int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

                fieldName[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
                fieldType[i] = ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")); //$NON-NLS-1$
                String slength = XMLHandler.getTagValue(fnode, "length"); //$NON-NLS-1$
                String sprecision = XMLHandler.getTagValue(fnode, "precision"); //$NON-NLS-1$

                fieldLength[i] = Const.toInt(slength, -1);
                fieldPrecision[i] = Const.toInt(sprecision, -1);

                fieldAdded[i] = "Y".equalsIgnoreCase( XMLHandler.getTagValue(fnode, "added") ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		
			*/
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "MappingOutputMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
        }
    }
    
    public String getXML()
    {
        StringBuffer retval = new StringBuffer(300);

        /*
        retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$
        for (int i = 0; i < fieldName.length; i++)
        {
            if (fieldName[i] != null && fieldName[i].length() != 0)
            {
                retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
                retval.append("        ").append(XMLHandler.addTagValue("name", fieldName[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(fieldType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("length", fieldLength[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("precision", fieldPrecision[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("added", fieldAdded[i]?"Y":"N")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                retval.append("      </field>").append(Const.CR); //$NON-NLS-1$
            }
        }
        retval.append("    </fields>").append(Const.CR); //$NON-NLS-1$
		*/
        
        return retval.toString();
    }

    public void setDefault()
    {
        int nrfields = 0;

        allocate(nrfields);

        /*
        for (int i = 0; i < nrfields; i++)
        {
            fieldName[i] = "field" + i; //$NON-NLS-1$
            fieldType[i] = ValueMeta.TYPE_STRING;
            fieldLength[i] = 30;
            fieldPrecision[i] = -1;
            fieldAdded[i] = true;
        }
        */
    }

    public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException {
    	// It's best that this method doesn't change anything by itself.
    	// Eventually it's the Mapping step that's going to tell this step how to behave meta-data wise.
    	// It is the mapping step that tells the mapping output step what fields to rename.
    	// 
    	if (inputValueRenames!=null) {
    		for (MappingValueRename valueRename : inputValueRenames) {
    			ValueMetaInterface valueMeta = r.searchValueMeta(valueRename.getTargetValueName());
    			if (valueMeta!=null) {
    				valueMeta.setName(valueRename.getSourceValueName());
    			}
    		}
    	}
    	
    	// This is the optionally entered stuff in the output tab of the mapping dialog.
    	//
    	if (outputValueRenames!=null) {
    		for (MappingValueRename valueRename : outputValueRenames) {
    			ValueMetaInterface valueMeta = r.searchValueMeta(valueRename.getSourceValueName());
    			if (valueMeta!=null) {
    				valueMeta.setName(valueRename.getTargetValueName());
    			}
    		}
    	}
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
        try
        {
        	/*
            int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
                fieldType[i] = ValueMeta.getType( rep.getStepAttributeString(id_step, i, "field_type") ); //$NON-NLS-1$
                fieldLength[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
                fieldPrecision[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
                fieldAdded[i] = rep.getStepAttributeBoolean(id_step, i, "field_added"); //$NON-NLS-1$
            }
            */
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "MappingOutputMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
        	/*
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] != null && fieldName[i].length() != 0)
                {
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_type", ValueMeta.getTypeDesc(fieldType[i])); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_length", fieldLength[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_added", fieldAdded[i]); //$NON-NLS-1$
                }
            }
            */
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "MappingOutputMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;
        if (prev == null || prev.size() == 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "MappingOutputMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "MappingOutputMeta.CheckResult.StepReceivingDatasOK",prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "MappingOutputMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "MappingOutputMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
    {
        return new MappingOutput(stepMeta, stepDataInterface, cnr, tr, trans);
    }

    public StepDataInterface getStepData()
    {
        return new MappingOutputData();
    }
    
	/**
	 * @return the inputValueRenames
	 */
	public List<MappingValueRename> getInputValueRenames() {
		return inputValueRenames;
	}

	/**
	 * @param inputValueRenames the inputValueRenames to set
	 */
	public void setInputValueRenames(List<MappingValueRename> inputValueRenames) {
		this.inputValueRenames = inputValueRenames;
	}

	/**
	 * @return the outputValueRenames
	 */
	public List<MappingValueRename> getOutputValueRenames() {
		return outputValueRenames;
	}

	/**
	 * @param outputValueRenames the outputValueRenames to set
	 */
	public void setOutputValueRenames(List<MappingValueRename> outputValueRenames) {
		this.outputValueRenames = outputValueRenames;
	}
}
