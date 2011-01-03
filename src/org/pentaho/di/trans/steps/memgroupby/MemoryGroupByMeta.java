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

package org.pentaho.di.trans.steps.memgroupby;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * Created on 02-jun-2003
 *
 */

public class MemoryGroupByMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = MemoryGroupByMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final int TYPE_GROUP_NONE               =  0;
	public static final int TYPE_GROUP_SUM                =  1;
	public static final int TYPE_GROUP_AVERAGE            =  2;
	public static final int TYPE_GROUP_MIN                =  3;
	public static final int TYPE_GROUP_MAX                =  4;
	public static final int TYPE_GROUP_COUNT_ALL          =  5;
    public static final int TYPE_GROUP_CONCAT_COMMA       =  6;
    public static final int TYPE_GROUP_FIRST              =  7;
    public static final int TYPE_GROUP_LAST               =  8;
    public static final int TYPE_GROUP_FIRST_INCL_NULL    =  9;
    public static final int TYPE_GROUP_LAST_INCL_NULL     = 10;
	public static final int TYPE_GROUP_STANDARD_DEVIATION = 11;
	public static final int TYPE_GROUP_CONCAT_STRING	  = 12;
	public static final int TYPE_GROUP_COUNT_DISTINCT     = 13;
	

	public static final String typeGroupCode[] =  /* WARNING: DO NOT TRANSLATE THIS. WE ARE SERIOUS, DON'T TRANSLATE! */ 
		{
			"-", "SUM", "AVERAGE", "MIN", "MAX", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
			"COUNT_ALL", "CONCAT_COMMA", "FIRST", "LAST", "FIRST_INCL_NULL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
			"LAST_INCL_NULL", "STD_DEV",  "CONCAT_STRING",	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"COUNT_DISTINCT", //$NON-NLS-1$
		};

	public static final String typeGroupLongDesc[] = 
		{
			"-",                                                                     //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.SUM"),                 //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.AVERAGE"),             //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.MIN"),                 //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.MAX"),                 //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.CONCAT_ALL"),          //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.CONCAT_COMMA"),        //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.FIRST"),               //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.LAST"), 	             //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.FIRST_INCL_NULL"),     //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.LAST_INCL_NULL"),      //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.STANDARD_DEVIATION"),  //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.CONCAT_STRING"),  		//$NON-NLS-1$ 
            BaseMessages.getString(PKG, "MemoryGroupByMeta.TypeGroupLongDesc.COUNT_DISTINCT"),     //$NON-NLS-1$
		};

	
	
	/** Fields to group over */
	private String  groupField[]; 

	/** Name of aggregate field */
	private String  aggregateField[]; 
	/** Field name to group over */
	private String  subjectField[]; 
	/** Type of aggregate */
	private int     aggregateType[]; 
	/** Value to use as separator for ex */
	private String valueField[];
	
    /** Flag to indicate that we always give back one row.  Defaults to true for existing transformations. */
    private boolean alwaysGivingBackOneRow;
    
	public MemoryGroupByMeta()
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
    public String[] getValueField()
    {
        return valueField;
    }
    
    /**
     * @param separatorField The valueField to set.
     */
    public void setValueField(String[] valueField)
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
		valueField= new String[nrfields];
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
	
			boolean hasNumberOfValues = false;
			for (int i=0;i<nrfields;i++)
			{
				Node fnode         = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				aggregateField[i]  = XMLHandler.getTagValue(fnode, "aggregate");		 //$NON-NLS-1$
				subjectField[i]    = XMLHandler.getTagValue(fnode, "subject");		 //$NON-NLS-1$
				aggregateType[i]   = getType(XMLHandler.getTagValue(fnode, "type"));	 //$NON-NLS-1$
				
				if (aggregateType[i]==TYPE_GROUP_COUNT_ALL || aggregateType[i]==TYPE_GROUP_COUNT_DISTINCT) {
					hasNumberOfValues = true;
				}
				
				valueField[i]    = XMLHandler.getTagValue(fnode, "valuefield");	
			}

            String giveBackRow = XMLHandler.getTagValue(stepnode, "give_back_row"); // $NON-NLS-1$
            if (Const.isEmpty(giveBackRow)) {
            	alwaysGivingBackOneRow = hasNumberOfValues;
            } else {
            	alwaysGivingBackOneRow = "Y".equalsIgnoreCase( giveBackRow ); // $NON-NLS-1$
            }
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "MemoryGroupByMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
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

	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
	{
		// re-assemble a new row of metadata
		//
    	RowMetaInterface fields = new RowMeta();

    	// Add the grouping fields in the correct order...
    	//
    	for (int i=0;i<groupField.length;i++) {
    		ValueMetaInterface valueMeta = r.searchValueMeta(groupField[i]);
    		if (valueMeta!=null) {
    			valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
    			fields.addValueMeta(valueMeta);
    		}
    	}
		
		// Re-add aggregates
        //
		for (int i=0;i<subjectField.length;i++)
		{
			ValueMetaInterface subj = r.searchValueMeta(subjectField[i]);
			if (subj!=null)
			{
				String value_name = aggregateField[i];
				int value_type = ValueMetaInterface.TYPE_NONE;
                int length = -1;
                int precision = -1;
                
				switch(aggregateType[i])
				{
                    case TYPE_GROUP_FIRST              : 
                    case TYPE_GROUP_LAST               : 
                    case TYPE_GROUP_FIRST_INCL_NULL    : 
                    case TYPE_GROUP_LAST_INCL_NULL     : 
					case TYPE_GROUP_MIN                : 
					case TYPE_GROUP_MAX                : value_type = subj.getType(); break;
					case TYPE_GROUP_COUNT_DISTINCT     :
					case TYPE_GROUP_COUNT_ALL          : value_type = ValueMetaInterface.TYPE_INTEGER; break;
                    case TYPE_GROUP_CONCAT_COMMA       : value_type = ValueMetaInterface.TYPE_STRING; break;
					case TYPE_GROUP_SUM                : 
					case TYPE_GROUP_AVERAGE            : 
                    case TYPE_GROUP_STANDARD_DEVIATION : value_type = ValueMetaInterface.TYPE_NUMBER; break;
                    case TYPE_GROUP_CONCAT_STRING      : value_type = ValueMetaInterface.TYPE_STRING; break;
					default: break;
				}
				
				if (aggregateType[i]==TYPE_GROUP_COUNT_ALL || aggregateType[i]==TYPE_GROUP_COUNT_DISTINCT)
                {
                    length    = ValueMetaInterface.DEFAULT_INTEGER_LENGTH;
                    precision = 0;
                }
				// If it ain't numeric, we change it to Number
				//
                else if (aggregateType[i]==TYPE_GROUP_SUM && value_type!=ValueMetaInterface.TYPE_INTEGER && value_type!=ValueMetaInterface.TYPE_NUMBER && value_type!=ValueMetaInterface.TYPE_BIGNUMBER)
                {
                	value_type = ValueMetaInterface.TYPE_NUMBER;
					precision=-1;
					length=-1;       	
                }

				if (value_type != ValueMetaInterface.TYPE_NONE)
				{
					ValueMetaInterface v = new ValueMeta(value_name, value_type);
					v.setOrigin(origin);
                    v.setLength(length, precision);
                    fields.addValueMeta(v);
				}
			}
		}
        
        // Now that we have all the fields we want, we should clear the original row and replace the values...
        //
        r.clear();
        r.addRowMeta(fields);
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(500);

        retval.append("      ").append(XMLHandler.addTagValue("give_back_row",  alwaysGivingBackOneRow)); //$NON-NLS-1$ //$NON-NLS-2$
        
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
	
			boolean hasNumberOfValues = false;
			for (int i=0;i<nrvalues;i++)
			{
				aggregateField[i] = rep.getStepAttributeString(id_step, i, "aggregate_name"); //$NON-NLS-1$
				subjectField[i]   = rep.getStepAttributeString(id_step, i, "aggregate_subject"); //$NON-NLS-1$
				aggregateType[i]      = getType( rep.getStepAttributeString(id_step, i, "aggregate_type") ); //$NON-NLS-1$
				
				if (aggregateType[i]==TYPE_GROUP_COUNT_ALL || aggregateType[i]==TYPE_GROUP_COUNT_DISTINCT) {
					hasNumberOfValues = true;
				}
				valueField[i]   = rep.getStepAttributeString(id_step, i, "aggregate_value_field"); //$NON-NLS-1$
			}
			
            alwaysGivingBackOneRow = rep.getStepAttributeBoolean(id_step, 0, "give_back_row", hasNumberOfValues); // $NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "MemoryGroupByMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
            rep.saveStepAttribute(id_transformation, id_step, "give_back_row",    alwaysGivingBackOneRow); // $NON-NLS-1$

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
			throw new KettleException(BaseMessages.getString(PKG, "MemoryGroupByMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "MemoryGroupByMeta.CheckResult.ReceivingInfoOK"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "MemoryGroupByMeta.CheckResult.NoInputError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new MemoryGroupBy(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new MemoryGroupByData();
	}

	/**
	 * @return the alwaysGivingBackOneRow
	 */
	public boolean isAlwaysGivingBackOneRow() {
		return alwaysGivingBackOneRow;
	}

	/**
	 * @param alwaysGivingBackOneRow the alwaysGivingBackOneRow to set
	 */
	public void setAlwaysGivingBackOneRow(boolean alwaysGivingBackOneRow) {
		this.alwaysGivingBackOneRow = alwaysGivingBackOneRow;
	}
 
    public TransformationType[] getSupportedTransformationTypes() {
      return new TransformationType[] { TransformationType.Normal, };
    }
}