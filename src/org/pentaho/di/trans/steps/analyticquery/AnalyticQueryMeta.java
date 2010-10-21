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

package org.pentaho.di.trans.steps.analyticquery;

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
import org.pentaho.di.core.row.RowMeta;
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
 *  @author ngoodman
 *  @since 27-jan-2009
 */

public class AnalyticQueryMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = AnalyticQuery.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final int TYPE_FUNCT_LEAD               =  0;
	public static final int TYPE_FUNCT_LAG                =  1;
	

	public static final String typeGroupCode[] =  /* WARNING: DO NOT TRANSLATE THIS. WE ARE SERIOUS, DON'T TRANSLATE! */ 
		{
			 "LEAD", "LAG", 
		};

	public static final String typeGroupLongDesc[] = 
		{
                                               
            BaseMessages.getString(PKG, "AnalyticQueryMeta.TypeGroupLongDesc.LEAD"),                 //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "AnalyticQueryMeta.TypeGroupLongDesc.LAG"), 
		};

	    
	/** Fields to partition by ie, CUSTOMER, PRODUCT */
	private String  groupField[]; 
	
	private int number_of_fields;
	/** BEGIN arrays (each of size number_of_fields) */
	
		/** Name of OUTPUT fieldname  "MYNEWLEADFUNCTION" */
		private String[]  aggregateField;
		/** Name of the input fieldname it operates on "ORDERTOTAL" */
		private String[]  subjectField; 
		/** Aggregate type (LEAD/LAG, etc) */
		private int []    aggregateType; 
		/** Offset "N" of how many rows to go forward/back */
		private int[] valueField;
	
	/** END arrays are one for each configured analytic function */
	

	public AnalyticQueryMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
    /**
     * @return Returns the aggregateField.
     */
    public String[] getAggregateField()
    {
        return aggregateField;
    }
    
    /**
     * @param aggregateField The aggregateField to set.
     */
    public void setAggregateField(String[] aggregateField)
    {
        this.aggregateField = aggregateField;
    }

    /**
     * @return Returns the aggregateTypes.
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
     * @return Returns the groupField.
     */
    public String[] getGroupField()
    {
        return groupField;
    }
    
    /**
     * @param groupField The groupField to set.
     */
    public void setGroupField(String[] groupField)
    {
        this.groupField = groupField;
    }
    
    

    
    /**
     * @return Returns the subjectField.
     */
    public String[] getSubjectField()
    {
        return subjectField;
    }
    
    /**
     * @param subjectField The subjectField to set.
     */
    public void setSubjectField(String[] subjectField)
    {
        this.subjectField = subjectField;
    }
    
    /**
     * @return Returns the valueField.
     */
    public int[] getValueField()
    {
        return valueField;
    }
    
    /**
     * @param The valueField to set.
     */
    public void setValueField(int[] valueField)
    {
        this.valueField = valueField;
    }
    
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int sizegroup, int nrfields)
	{
		groupField = new String[sizegroup];
		aggregateField  = new String[nrfields];
		subjectField = new String[nrfields];
		aggregateType  = new int[nrfields];
		valueField= new int[nrfields];
		
		number_of_fields = nrfields;
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{

			Node groupn = XMLHandler.getSubNode(stepnode, "group"); //$NON-NLS-1$
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			
			int sizegroup = XMLHandler.countNodes(groupn, "field"); //$NON-NLS-1$
			int nrfields  = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
			
			allocate(sizegroup, nrfields);
	
			for (int i=0;i<sizegroup;i++)
			{
				Node fnode    = XMLHandler.getSubNodeByNr(groupn, "field", i); //$NON-NLS-1$
				groupField[i] = XMLHandler.getTagValue(fnode, "name");		 //$NON-NLS-1$
			}
			for (int i=0;i<nrfields;i++)
			{
				Node fnode         = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				aggregateField[i]  = XMLHandler.getTagValue(fnode, "aggregate");		 //$NON-NLS-1$
				subjectField[i]    = XMLHandler.getTagValue(fnode, "subject");		 //$NON-NLS-1$
				aggregateType[i]   = getType(XMLHandler.getTagValue(fnode, "type"));	 //$NON-NLS-1$
				
				valueField[i]    = Integer.parseInt(XMLHandler.getTagValue(fnode, "valuefield"));
			}


		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "AnalyticQueryMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}
	
	public static final int getType(String desc)
	{
		for (int i=0;i<typeGroupCode.length;i++)
		{
			if (typeGroupCode[i].equalsIgnoreCase(desc)) return i;
		}
		for (int i=0;i<typeGroupLongDesc.length;i++)
		{
			if (typeGroupLongDesc[i].equalsIgnoreCase(desc)) return i;
		}
		return 0;
	}

	public static final String getTypeDesc(int i)
	{
		if (i<0 || i>=typeGroupCode.length) return null;
		return typeGroupCode[i];
	}

	public static final String getTypeDescLong(int i)
	{
		if (i<0 || i>=typeGroupLongDesc.length) return null;
		return typeGroupLongDesc[i];
	}
	
	public void setDefault()
	{

		int sizegroup= 0;
		int nrfields = 0;
		
		allocate( sizegroup, nrfields );
	}

	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// re-assemble a new row of metadata
		//
    	RowMetaInterface fields = new RowMeta();
    	
    	// Add existing values
    	fields.addRowMeta(r);
      
      // add analytic values
      for (int i = 0 ; i < number_of_fields; i ++ ){
         
      	int index_of_subject = -1;
        	index_of_subject = r.indexOfValue(subjectField[i]);

        	//  if we found the subjectField in the RowMetaInterface, and we should....
        	if (index_of_subject > -1) {
           	ValueMetaInterface vmi = r.getValueMeta(index_of_subject).clone();
           	vmi.setOrigin(origin);
           	vmi.setName(aggregateField[i]);
           	fields.addValueMeta(r.size() + i, vmi);
        	}
        	else {
        	   //  we have a condition where the subjectField can't be found from the rowMetaInterface
        	   StringBuilder sbfieldNames = new StringBuilder();
        	   String[] fieldNames = r.getFieldNames();
        	   for (int j=0; j < fieldNames.length; j++) {
        	    sbfieldNames.append("["+fieldNames[j]+"]"+(j<fieldNames.length-1?", ":""));
            }
            throw new KettleStepException(BaseMessages.getString(PKG, "AnalyticQueryMeta.Exception.SubjectFieldNotFound", getParentStepMeta().getName(), subjectField[i], sbfieldNames.toString()));
        	}
      }
        
      r.clear();
      // Add back to Row Meta
      r.addRowMeta(fields);
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(500);

        
		retval.append("      <group>").append(Const.CR); //$NON-NLS-1$
		for (int i=0;i<groupField.length;i++)
		{
			retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
			retval.append("          ").append(XMLHandler.addTagValue("name", groupField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
		}
		retval.append("      </group>").append(Const.CR); //$NON-NLS-1$

		retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
		for (int i=0;i<subjectField.length;i++)
		{
			retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
			retval.append("          ").append(XMLHandler.addTagValue("aggregate", aggregateField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("          ").append(XMLHandler.addTagValue("subject", subjectField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("          ").append(XMLHandler.addTagValue("type", getTypeDesc(aggregateType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("          ").append(XMLHandler.addTagValue("valuefield", valueField[i])); 
			retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
		}
		retval.append("      </fields>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			
            
            
			int groupsize = rep.countNrStepAttributes(id_step, "group_name"); //$NON-NLS-1$
			int nrvalues  = rep.countNrStepAttributes(id_step, "aggregate_name"); //$NON-NLS-1$
			
			allocate(groupsize, nrvalues);
			
			for (int i=0;i<groupsize;i++)
			{
				groupField[i] = rep.getStepAttributeString(id_step, i, "group_name"); //$NON-NLS-1$
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				aggregateField[i] = rep.getStepAttributeString(id_step, i, "aggregate_name"); //$NON-NLS-1$
				subjectField[i]   = rep.getStepAttributeString(id_step, i, "aggregate_subject"); //$NON-NLS-1$
				aggregateType[i]      = getType( rep.getStepAttributeString(id_step, i, "aggregate_type") ); //$NON-NLS-1$
				valueField[i]   = (int) rep.getStepAttributeInteger(id_step, i, "aggregate_value_field"); //$NON-NLS-1$
			}
			
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "AnalyticQueryMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{

			for (int i=0;i<groupField.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "group_name",       groupField[i]); //$NON-NLS-1$
			}
	
			for (int i=0;i<subjectField.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "aggregate_name",    aggregateField[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "aggregate_subject", subjectField[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "aggregate_type",    getTypeDesc(aggregateType[i])); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "aggregate_value_field", valueField[i]); 
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "AnalyticQueryMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AnalyticQueryMeta.CheckResult.ReceivingInfoOK"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AnalyticQueryMeta.CheckResult.NoInputError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new AnalyticQuery(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new AnalyticQueryData();
	}

	public int getNumberOfFields() {
		return number_of_fields;
	}

	public void setNumberOfFields(int number_of_fields) {
		this.number_of_fields = number_of_fields;
	}

}