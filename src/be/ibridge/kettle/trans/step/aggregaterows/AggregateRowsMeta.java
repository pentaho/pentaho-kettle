 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step.aggregaterows;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/*
 * Created on 24-jun-2003
 *
 */

public class AggregateRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	public final static int TYPE_AGGREGATE_NONE    = 0;
	public final static int TYPE_AGGREGATE_SUM     = 1;
	public final static int TYPE_AGGREGATE_AVERAGE = 2;
	public final static int TYPE_AGGREGATE_COUNT   = 3;
	public final static int TYPE_AGGREGATE_MIN     = 4;
	public final static int TYPE_AGGREGATE_MAX     = 5;
	public final static int TYPE_AGGREGATE_FIRST   = 6;
	public final static int TYPE_AGGREGATE_LAST    = 7;
	 
	public final static String aggregateTypeDesc[] =
		{
			"NONE(=first value)",
			"SUM", "AVERAGE", "COUNT", "MIN", "MAX", "FIRST", "LAST"
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
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
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
				//if (i==0) return TYPE_AGGREGATE_FIRST;
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
			retval.fieldNewName[i]   = fieldNewName[i];
			retval.aggregateType[i] = aggregateType[i];
		}
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			int i, nrfields;
			String type;
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			nrfields= XMLHandler.countNodes(fields, "field");
	
			allocate(nrfields);
			
			for (i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				fieldName[i]     = XMLHandler.getTagValue(fnode, "name");
				fieldNewName[i]   = XMLHandler.getTagValue(fnode, "rename");
				type              = XMLHandler.getTagValue(fnode, "type");
				aggregateType[i] = getType(type);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML node (Aggr.Rows)", e);
		}
	}

	public void setDefault()
	{
		int i, nrfields;
		
		nrfields=0;
		
		allocate(nrfields);
		
		for (i=0;i<nrfields;i++)
		{
			fieldName[i]     = "fieldname";
			fieldNewName[i]   = "new name";
			aggregateType[i] = TYPE_AGGREGATE_SUM; 
		}
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		int i;
		int fieldnrs[];
		Value values[];

		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		// Remember the types of the row.
		fieldnrs  = new int[fieldName.length];
		values    = new Value[fieldName.length];
		
		for (i=0;i<fieldName.length;i++)
		{
			fieldnrs[i] = row.searchValueIndex(fieldName[i]);
			Value v = row.getValue(fieldnrs[i]);
			values[i] = new Value(v); // copy value : default settings!
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
		
		for (i=0;i<fieldName.length;i++)
		{
			Value v = values[i];
			v.setName(fieldNewName[i]);
			v.setOrigin(name);
			row.addValue(v);
		}
		
		return row;
	}

	public String getXML()
	{
		String retval="";
		int i;
		
		retval+="    <fields>"+Const.CR;
		for (i=0;i<fieldName.length;i++)
		{
		retval+="      <field>"+Const.CR;
		retval+="        "+XMLHandler.addTagValue("name", fieldName[i]);
		retval+="        "+XMLHandler.addTagValue("rename", fieldNewName[i]);
		retval+="        "+XMLHandler.addTagValue("type", getTypeDesc(aggregateType[i]));
		retval+="        </field>"+Const.CR;
		}
		retval+="      </fields>"+Const.CR;

		return retval;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
				fieldNewName[i] = rep.getStepAttributeString(id_step, i, "field_rename");
				aggregateType[i] = getType( rep.getStepAttributeString(id_step, i, "field_type"));
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}

	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			for (int i=0;i<fieldName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",    fieldName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_rename",  fieldNewName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",    getTypeDesc(aggregateType[i]));
			}
		}
		catch(KettleException e)
		{
			throw new KettleException("Unable to save step info to the repository for id_step = "+id_step, e);
		}
	}



	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		String message = "";
		
		if (fieldName.length>0)
		{
			boolean error_found=false;
			// See if all fields are available in the input stream...
			message="The following fields are not found in the input stream: "+Const.CR;
			for (int i=0;i<fieldName.length;i++)
			{
				if (prev.searchValueIndex(fieldName[i])<0)
				{
					message+="  "+fieldName[i]+Const.CR;
					error_found=true;
				}
			}
			if (error_found)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, stepinfo);
			}
			else
			{
				message = "All fields are present in the input rows.";
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, message, stepinfo);
			}
			remarks.add(cr);
			
			// See which fields are dropped: comment on it!
			message="The following fields from the input stream are ignored and dropped: "+Const.CR;
			error_found=false;
			
			for (int i=0;i<prev.size();i++)
			{
				Value v = prev.getValue(i);
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
					message+="  "+v.getName()+" ("+v.toStringMeta()+")"+Const.CR;
					error_found=true;
				}
			}
			if (error_found)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, message, stepinfo);
			}
			else
			{
				message = "All fields from the input rows are used.";
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, message, stepinfo);
			}
			remarks.add(cr);
		}
		else
		{
			message = "Nothing specified to aggregate: result is going to be empty";
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, message, stepinfo);
			remarks.add(cr);
		}

		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
			remarks.add(cr);
		}
		
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new AggregateRowsDialog(shell, info, transMeta, name);
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
