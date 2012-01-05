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

package org.pentaho.di.trans.steps.rowsfromresult;

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
import org.pentaho.di.core.row.ValueMeta;
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


/*
 * Created on 02-jun-2003
 *
 */

public class RowsFromResultMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = RowsFromResult.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String fieldname[];
    private int    type[];
    private int    length[];
    private int    precision[];
    
	/**
     * @return Returns the length.
     */
    public int[] getLength()
    {
        return length;
    }

    /**
     * @param length The length to set.
     */
    public void setLength(int[] length)
    {
        this.length = length;
    }

    /**
     * @return Returns the name.
     */
    public String[] getFieldname()
    {
        return fieldname;
    }

    /**
     * @param name The name to set.
     */
    public void setFieldname(String[] name)
    {
        this.fieldname = name;
    }

    /**
     * @return Returns the precision.
     */
    public int[] getPrecision()
    {
        return precision;
    }

    /**
     * @param precision The precision to set.
     */
    public void setPrecision(int[] precision)
    {
        this.precision = precision;
    }

    /**
     * @return Returns the type.
     */
    public int[] getType()
    {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(int[] type)
    {
        this.type = type;
    }

    public RowsFromResultMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
    public void allocate(int nrFields)
    {
        fieldname = new String[nrFields];
        type = new int[nrFields];
        length = new int[nrFields];
        precision = new int[nrFields];
    }
    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer();
        retval.append("    <fields>"); //$NON-NLS-1$
        for (int i=0;i<fieldname.length;i++)
        {
            retval.append("      <field>"); //$NON-NLS-1$
            retval.append("        "+XMLHandler.addTagValue("name",      fieldname[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        "+XMLHandler.addTagValue("type",      ValueMeta.getTypeDesc(type[i]))); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        "+XMLHandler.addTagValue("length",    length[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        "+XMLHandler.addTagValue("precision", precision[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        </field>"); //$NON-NLS-1$
        }
        retval.append("      </fields>"); //$NON-NLS-1$

        return retval.toString();
    }
    
	private void readData(Node stepnode)
	{
        Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
        int nrfields   = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

        allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            Node line = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
            fieldname     [i] = XMLHandler.getTagValue(line, "name"); //$NON-NLS-1$
            type     [i] = ValueMeta.getType(XMLHandler.getTagValue(line, "type")); //$NON-NLS-1$
            length   [i] = Const.toInt(XMLHandler.getTagValue(line, "length"), -2); //$NON-NLS-1$
            precision[i] = Const.toInt(XMLHandler.getTagValue(line, "precision"), -2); //$NON-NLS-1$
        }

	}

	public void setDefault()
	{
        allocate(0);
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
        try
        {
           int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
            allocate(nrfields);
    
            for (int i=0;i<nrfields;i++)
            {
                fieldname[i]      =      rep.getStepAttributeString (id_step, i, "field_name"); //$NON-NLS-1$
                type[i]      = ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type")); //$NON-NLS-1$
                length[i]    = (int)rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
                precision[i] = (int)rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
            }
        }
        catch(Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "RowsFromResultMeta.Exception.ErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
        }

	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
	throws KettleException
	{
        try
        {
            for (int i=0;i<fieldname.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldname[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      ValueMeta.getTypeDesc(type[i])); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    length[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", precision[i]); //$NON-NLS-1$
            }
        }
        catch(Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "RowsFromResultMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
        }
	}
    
    public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        for (int i=0;i<this.fieldname.length;i++)
        {
        	ValueMeta v = new ValueMeta(fieldname[i], type[i], length[i], precision[i]);
            v.setOrigin(origin);
            r.addValueMeta(v);
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "RowsFromResultMeta.CheckResult.StepExpectingNoReadingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "RowsFromResultMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new RowsFromResult(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new RowsFromResultData();
	}

}
