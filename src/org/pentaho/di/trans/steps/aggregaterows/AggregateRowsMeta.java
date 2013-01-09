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

package org.pentaho.di.trans.steps.aggregaterows;

import java.util.List;
import java.util.Map;

import org.pentaho.di.compatibility.Value;
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

/*
 * Created on 24-jun-2003
 *
 */
public class AggregateRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = AggregateRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public final static int TYPE_AGGREGATE_NONE       = 0;
	public final static int TYPE_AGGREGATE_SUM        = 1;
	public final static int TYPE_AGGREGATE_AVERAGE    = 2;
	public final static int TYPE_AGGREGATE_COUNT      = 3;
	public final static int TYPE_AGGREGATE_MIN        = 4;
	public final static int TYPE_AGGREGATE_MAX        = 5;
	public final static int TYPE_AGGREGATE_FIRST      = 6;
	public final static int TYPE_AGGREGATE_LAST       = 7;
    public final static int TYPE_AGGREGATE_FIRST_NULL = 8;
    public final static int TYPE_AGGREGATE_LAST_NULL  = 9;
    
	 
	public final static String aggregateTypeDesc[] =
		{
			BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.NONE"),     //$NON-NLS-1$
			BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.SUM"),     //$NON-NLS-1$
            BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.AVERAGE"),//$NON-NLS-1$
            BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.COUNT"), //$NON-NLS-1$
            BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.MIN"),  //$NON-NLS-1$
            BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.MAX"),          //$NON-NLS-1$
            BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.FIRST"),        //$NON-NLS-1$
            BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.LAST"),        //$NON-NLS-1$
            BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.FIRST_NULL"), //$NON-NLS-1$
            BaseMessages.getString(PKG, "AggregateRowsMeta.AggregateTypeDesc.LAST_NULL"), //$NON-NLS-1$
		};
	
	private  String fieldName[];
	private  String fieldNewName[];
	private  int    aggregateType[];

	public AggregateRowsMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
	 * @return Returns the aggregateType.
	 */
	public int[] getAggregateType()
	{
		return aggregateType;
	}
	
	/**
	 * @param aggregateType The aggregateType to set.
	 */
	public void setAggregateType(int[] aggregateType)
	{
		this.aggregateType = aggregateType;
	}
	
	/**
	 * @return Returns the fieldName.
	 */
	public String[] getFieldName()
	{
		return fieldName;
	}
	
	/**
	 * @param fieldName The fieldName to set.
	 */
	public void setFieldName(String[] fieldName)
	{
		this.fieldName = fieldName;
	}
	
	/**
	 * @return Returns the fieldNewName.
	 */
	public String[] getFieldNewName()
	{
		return fieldNewName;
	}
	
	/**
	 * @param fieldNewName The fieldNewName to set.
	 */
	public void setFieldNewName(String[] fieldNewName)
	{
		this.fieldNewName = fieldNewName;
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}


	public void allocate(int nrfields)
	{
		fieldName     = new String[nrfields];
		fieldNewName   = new String[nrfields];
		aggregateType = new int[nrfields];
	}
	
	public static final String getTypeDesc(int t)
	{
		if (t<0 || t>=aggregateTypeDesc.length) return null;
		return aggregateTypeDesc[t];
	}

	
	public final static int getType(String at)
	{
		int i;
		for (i=0;i<aggregateTypeDesc.length;i++)
		{
			if (aggregateTypeDesc[i].equalsIgnoreCase(at)) 
			{
				return i;
			} 
		}
		return TYPE_AGGREGATE_NONE;
	}
	
	public Object clone()
	{
		AggregateRowsMeta retval = (AggregateRowsMeta)super.clone();
		
		int nrfields=fieldName.length;
		
		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldName[i]     = fieldName[i];
			retval.fieldNewName[i]  = fieldNewName[i];
			retval.aggregateType[i] = aggregateType[i];
		}
		return retval;
	}
	
	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			int i, nrfields;
			String type;
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			nrfields= XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
	
			allocate(nrfields);
			
			for (i=0;i<nrfields;i++)
			{
				Node fnode       = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				fieldName[i]     = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
				fieldNewName[i]  = XMLHandler.getTagValue(fnode, "rename"); //$NON-NLS-1$
				type             = XMLHandler.getTagValue(fnode, "type"); //$NON-NLS-1$
				aggregateType[i] = getType(type);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "AggregateRowsMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		int i, nrfields;
		
		nrfields=0;
		
		allocate(nrfields);
		
		for (i=0;i<nrfields;i++)
		{
			fieldName[i]     = BaseMessages.getString(PKG, "AggregateRowsMeta.Fieldname.Label"); //$NON-NLS-1$
			fieldNewName[i]  = BaseMessages.getString(PKG, "AggregateRowsMeta.NewName.Label"); //$NON-NLS-1$
			aggregateType[i] = TYPE_AGGREGATE_SUM; 
		}
	}

	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

		// Remember the types of the row.
		int fieldnrs[] = new int[fieldName.length];
		ValueMetaInterface[] values = new ValueMetaInterface[fieldName.length];
		
		for (int i=0;i<fieldName.length;i++)
		{
			fieldnrs[i] = row.indexOfValue(fieldName[i]);
			ValueMetaInterface v = row.getValueMeta(fieldnrs[i]);
			values[i] = v.clone(); // copy value : default settings!
			switch(aggregateType[i])
			{
			case TYPE_AGGREGATE_AVERAGE:
			case TYPE_AGGREGATE_COUNT:
			case TYPE_AGGREGATE_SUM:
				values[i].setType(Value.VALUE_TYPE_NUMBER);
				values[i].setLength(-1, -1);
				break;
			}
		}
		
		// Only the aggregate is returned!
		row.clear();
		
		for (int i=0;i<fieldName.length;i++)
		{
			ValueMetaInterface v = values[i];
			v.setName(fieldNewName[i]);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$
		for (int i=0;i<fieldName.length;i++)
		{
    		retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
    		retval.append("        ").append(XMLHandler.addTagValue("name", fieldName[i])); //$NON-NLS-1$ //$NON-NLS-2$
    		retval.append("        ").append(XMLHandler.addTagValue("rename", fieldNewName[i])); //$NON-NLS-1$ //$NON-NLS-2$
    		retval.append("        ").append(XMLHandler.addTagValue("type", getTypeDesc(aggregateType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
    		retval.append("      </field>").append(Const.CR); //$NON-NLS-1$
		}
		retval.append("    </fields>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
	
		try
		{
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
				fieldNewName[i] = rep.getStepAttributeString(id_step, i, "field_rename"); //$NON-NLS-1$
				aggregateType[i] = getType( rep.getStepAttributeString(id_step, i, "field_type")); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "AggregateRowsMeta.Exception.UnexpectedErrorWhileReadingStepInfo"), e); //$NON-NLS-1$
		}

	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			for (int i=0;i<fieldName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",    fieldName[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_rename",  fieldNewName[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",    getTypeDesc(aggregateType[i])); //$NON-NLS-1$
			}
		}
		catch(KettleException e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "AggregateRowsMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {

		CheckResult cr;
		String message = ""; //$NON-NLS-1$
		
		if (fieldName.length>0)
		{
			boolean error_found=false;
			// See if all fields are available in the input stream...
			message=BaseMessages.getString(PKG, "AggregateRowsMeta.CheckResult.FieldsNotFound.DialogMessage")+Const.CR; //$NON-NLS-1$
			for (int i=0;i<fieldName.length;i++)
			{
				if (prev.indexOfValue(fieldName[i])<0)
				{
					message+="  "+fieldName[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				}
			}
			if (error_found)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, stepMeta);
			}
			else
			{
				message = BaseMessages.getString(PKG, "AggregateRowsMeta.CheckResult.AllFieldsOK.DialogMessage"); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, message, stepMeta);
			}
			remarks.add(cr);
			
			// See which fields are dropped: comment on it!
			message=BaseMessages.getString(PKG, "AggregateRowsMeta.CheckResult.IgnoredFields.DialogMessage")+Const.CR; //$NON-NLS-1$
			error_found=false;
			
			for (int i=0;i<prev.size();i++)
			{
				ValueMetaInterface v = prev.getValueMeta(i);
				boolean value_found=false;
				for (int j=0;j<fieldName.length && !value_found;j++)
				{
					if (v.getName().equalsIgnoreCase(fieldName[j])) 
					{
						value_found=true;
					} 
				}
				if (!value_found)
				{
					message+="  "+v.getName()+" ("+v.toStringMeta()+")"+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					error_found=true;
				}
			}
			if (error_found)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, message, stepMeta);
			}
			else
			{
				message = BaseMessages.getString(PKG, "AggregateRowsMeta.CheckResult.AllFieldsUsed.DialogMessage"); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, message, stepMeta);
			}
			remarks.add(cr);
		}
		else
		{
			message = BaseMessages.getString(PKG, "AggregateRowsMeta.CheckResult.NothingSpecified.DialogMessage"); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, message, stepMeta);
			remarks.add(cr);
		}

		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AggregateRowsMeta.CheckResult.StepReceiveInfo.DialogMessage"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AggregateRowsMeta.CheckResult.NoInputReceived.DialogMessage"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new AggregateRows(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new AggregateRowsData();
	}
}