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

package be.ibridge.kettle.trans.step.groupby;

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


/**
 * Created on 02-jun-2003
 *
 */

public class GroupByMeta extends BaseStepMeta implements StepMetaInterface
{
	public static final int TYPE_GROUP_NONE           = 0;
	public static final int TYPE_GROUP_SUM            = 1;
	public static final int TYPE_GROUP_AVERAGE        = 2;
	public static final int TYPE_GROUP_MIN            = 3;
	public static final int TYPE_GROUP_MAX            = 4;
	public static final int TYPE_GROUP_COUNT_ALL      = 5;
    public static final int TYPE_GROUP_CONCAT_COMMA   = 6;

	public static final String type_group_desc[] = 
		{
			"-", "SUM", "AVERAGE", "MIN", "MAX", "COUNT_ALL", "CONCAT_COMMA"	
		};

	public static final String typeGroupLongDesc[] = 
		{
			"-", "Sum", "Average", "Minimum", "Maximum", "Number of Values", "Concatinate strings separated by ,"	
		};

	
	
	/** All rows need to pass, adding an extra row at the end of each group/block. */
	private boolean passAllRows;
	/** name of the boolean field indicating that the row is an aggragate 
     *  @deprecated */
	private String  passFlagField;
    
    /** Directory to store the temp files */
    private String  directory;
    /** Temp files prefix... */
    private String  prefix;
    
	/** Indicate that some rows don't need to be considered : TODO: make work in GUI & worker */
	private boolean aggregateIgnored; 
	/** name of the boolean field that indicates we need to ignore the row : TODO: make work in GUI & worker */
	private String  aggregateIgnoredField; 

	/** Fields to group over */
	private String  groupField[]; 

	/** Name of aggregate field */
	private String  aggregateField[]; 
	/** Field name to group over */
	private String  subjectField[]; 
	/** Type of aggregate */
	private int     aggregateType[]; 
	
	public GroupByMeta()
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
     * @return Returns the aggregateIgnored.
     */
    public boolean isAggregateIgnored()
    {
        return aggregateIgnored;
    }
    
    /**
     * @param aggregateIgnored The aggregateIgnored to set.
     */
    public void setAggregateIgnored(boolean aggregateIgnored)
    {
        this.aggregateIgnored = aggregateIgnored;
    }
    
    /**
     * @return Returns the aggregateIgnoredField.
     */
    public String getAggregateIgnoredField()
    {
        return aggregateIgnoredField;
    }
    
    /**
     * @param aggregateIgnoredField The aggregateIgnoredField to set.
     */
    public void setAggregateIgnoredField(String aggregateIgnoredField)
    {
        this.aggregateIgnoredField = aggregateIgnoredField;
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
     * @return Returns the passAllRows.
     */
    public boolean passAllRows()
    {
        return passAllRows;
    }
    
    /**
     * @param passAllRows The passAllRows to set.
     */
    public void setPassAllRows(boolean passAllRows)
    {
        this.passAllRows = passAllRows;
    }
    
    /**
     * @return Returns the passFlagField.
     * @deprecated
     */
    public String getPassFlagField()
    {
        return passFlagField;
    }
    
    /**
     * @param passFlagField The passFlagField to set.
     * @deprecated
     */
    public void setPassFlagField(String passFlagField)
    {
        this.passFlagField = passFlagField;
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
    
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
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
			passAllRows    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "all_rows"));
			aggregateIgnored = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "ignore_aggregate"));
			aggregateIgnoredField     = XMLHandler.getTagValue(stepnode, "field_ignore");
	
            directory = XMLHandler.getTagValue(stepnode, "directory");
            prefix    = XMLHandler.getTagValue(stepnode, "prefix");

			Node groupn = XMLHandler.getSubNode(stepnode, "group");
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			
			int sizegroup = XMLHandler.countNodes(groupn, "field");
			int nrfields  = XMLHandler.countNodes(fields, "field");
			
			allocate(sizegroup, nrfields);
	
			for (int i=0;i<sizegroup;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(groupn, "field", i);
				groupField[i]  = XMLHandler.getTagValue(fnode, "name");		
			}
	
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				aggregateField[i]  = XMLHandler.getTagValue(fnode, "aggregate");		
				subjectField[i]    = XMLHandler.getTagValue(fnode, "subject");		
				aggregateType[i]       = getType(XMLHandler.getTagValue(fnode, "type"));	
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public static final int getType(String desc)
	{
		for (int i=0;i<type_group_desc.length;i++)
		{
			if (type_group_desc[i].equalsIgnoreCase(desc)) return i;
		}
		for (int i=0;i<typeGroupLongDesc.length;i++)
		{
			if (typeGroupLongDesc[i].equalsIgnoreCase(desc)) return i;
		}
		return 0;
	}

	public static final String getTypeDesc(int i)
	{
		if (i<0 || i>=type_group_desc.length) return null;
		return type_group_desc[i];
	}

	public static final String getTypeDescLong(int i)
	{
		if (i<0 || i>=typeGroupLongDesc.length) return null;
		return typeGroupLongDesc[i];
	}
	
	public void setDefault()
	{
        directory="%%java.io.tmpdir%%";
        prefix="grp";
        
		passAllRows    = false;
		aggregateIgnored = false;
		aggregateIgnoredField     = null;

		int sizegroup= 0;
		int nrfields = 0;
		
		allocate( sizegroup, nrfields );
	}

	public Row getFields(Row r, String name, Row info)
	{
		// Loop over the aggregate names.
		// Keep the fields to group over and the aggregates
		Row row = new Row(r);
		
        if (!passAllRows)
        {
    		// Remove those that are not in group[] (or subject in case we want all rows...)
    		for (int i=r.size()-1;i>=0;i--)
    		{
                String fieldName = row.getValue(i).getName();
                
    			boolean found=false;
    			for (int j=0;j<groupField.length && !found;j++)
    			{
    				if (fieldName.equalsIgnoreCase(groupField[j])) found=true;
    			}
    			if (!found) r.removeValue(i);
    		}
        }
		
		// Re-add aggregates
		for (int i=0;i<subjectField.length;i++)
		{
			Value subj = row.searchValue(subjectField[i]);
			if (subj!=null)
			{
				// System.out.println("found subject #"+i+" --> "+subj);
				String value_name = aggregateField[i];
				int value_type = Value.VALUE_TYPE_NONE;
                int length = -1;
                int precision = -1;
                
				switch(aggregateType[i])
				{
					case TYPE_GROUP_SUM            : 
					case TYPE_GROUP_AVERAGE        :
					case TYPE_GROUP_COUNT_ALL      : value_type = Value.VALUE_TYPE_NUMBER; break;
					case TYPE_GROUP_MIN            : 
					case TYPE_GROUP_MAX            : value_type = subj.getType(); break;
                    case TYPE_GROUP_CONCAT_COMMA   : value_type = Value.VALUE_TYPE_STRING; break;
					default: break;
				}
                
                if (aggregateType[i]!=TYPE_GROUP_COUNT_ALL)
                {
                    length = subj.getLength();
                    precision = subj.getPrecision();
                }
                
				if (value_type != Value.VALUE_TYPE_NONE)
				{
					Value v = new Value(value_name, value_type);
					v.setOrigin(name);
                    v.setLength(length, precision);
					r.addValue(v);
				}
			}
		}

		return row;
	}

	public String getXML()
	{
		String retval="";

		retval+="      "+XMLHandler.addTagValue("all_rows",  passAllRows);
		retval+="      "+XMLHandler.addTagValue("ignore_aggregate",  aggregateIgnored);
		retval+="      "+XMLHandler.addTagValue("field_ignore", aggregateIgnoredField);
        retval+="      "+XMLHandler.addTagValue("directory", directory);
        retval+="      "+XMLHandler.addTagValue("prefix",    prefix);

		retval+="      <group>"+Const.CR;
		for (int i=0;i<groupField.length;i++)
		{
			retval+="        <field>"+Const.CR;
			retval+="          "+XMLHandler.addTagValue("name", groupField[i]);
			retval+="          </field>"+Const.CR;
		}
		retval+="        </group>"+Const.CR;

		retval+="      <fields>"+Const.CR;
		for (int i=0;i<subjectField.length;i++)
		{
			retval+="        <field>"+Const.CR;
			retval+="          "+XMLHandler.addTagValue("aggregate", aggregateField[i]);
			retval+="          "+XMLHandler.addTagValue("subject", subjectField[i]);
			retval+="          "+XMLHandler.addTagValue("type", getTypeDesc(aggregateType[i]));
			retval+="          </field>"+Const.CR;
		}
		retval+="        </fields>"+Const.CR;

		return retval;
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			passAllRows         = rep.getStepAttributeBoolean(id_step, "all_rows");
			aggregateIgnored = rep.getStepAttributeBoolean(id_step, "ignore_aggregate");
			aggregateIgnoredField     = rep.getStepAttributeString (id_step, "field_ignore");
            directory        =      rep.getStepAttributeString (id_step, "directory");
            prefix           =      rep.getStepAttributeString (id_step, "prefix");

			int groupsize = rep.countNrStepAttributes(id_step, "group_name");
			int nrvalues  = rep.countNrStepAttributes(id_step, "aggregate_name");
			
			allocate(groupsize, nrvalues);
			
			for (int i=0;i<groupsize;i++)
			{
				groupField[i] = rep.getStepAttributeString(id_step, i, "group_name");
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				aggregateField[i] = rep.getStepAttributeString(id_step, i, "aggregate_name");
				subjectField[i]   = rep.getStepAttributeString(id_step, i, "aggregate_subject");
				aggregateType[i]      = getType( rep.getStepAttributeString(id_step, i, "aggregate_type") );
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
			rep.saveStepAttribute(id_transformation, id_step, "all_rows", passAllRows);
			rep.saveStepAttribute(id_transformation, id_step, "ignore_aggregate",  aggregateIgnored);
			rep.saveStepAttribute(id_transformation, id_step, "field_ignore",  aggregateIgnoredField);
            rep.saveStepAttribute(id_transformation, id_step, "directory",       directory);
            rep.saveStepAttribute(id_transformation, id_step, "prefix",          prefix);

			for (int i=0;i<groupField.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "group_name",       groupField[i]);
			}
	
			for (int i=0;i<subjectField.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "aggregate_name",    aggregateField[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "aggregate_subject", subjectField[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "aggregate_type",    getTypeDesc(aggregateType[i]));
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	
	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;

		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new GroupByDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new GroupBy(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new GroupByData();
	}

    /**
     * @return Returns the directory.
     */
    public String getDirectory()
    {
        return directory;
    }

    /**
     * @param directory The directory to set.
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }

    /**
     * @return Returns the prefix.
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

}
