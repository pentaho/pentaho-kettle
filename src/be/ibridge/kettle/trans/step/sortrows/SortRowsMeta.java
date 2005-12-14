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


package be.ibridge.kettle.trans.step.sortrows;

import java.io.File;
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
 * Created on 02-jun-2003
 *
 */
 
public class SortRowsMeta extends BaseStepMeta implements StepMetaInterface
{
    /** order by which fields? */
	private String  fieldName[];
    /** false : descending, true=ascending */
	private boolean ascending[];
	
    /** Directory to store the temp files */
	private String  directory;
    /** Temp files prefix... */
	private String  prefix;

	public SortRowsMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
    /**
     * @return Returns the ascending.
     */
    public boolean[] getAscending()
    {
        return ascending;
    }
    
    /**
     * @param ascending The ascending to set.
     */
    public void setAscending(boolean[] ascending)
    {
        this.ascending = ascending;
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
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
		fieldName    = new String[nrfields]; // order by
		ascending = new boolean[nrfields];
	}

	public Object clone()
	{
		SortRowsMeta retval = (SortRowsMeta)super.clone();

		int nrfields = fieldName.length;

		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldName   [i] = fieldName[i];
			retval.ascending[i] = ascending[i]; 
		}
		
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			directory = XMLHandler.getTagValue(stepnode, "directory");
			prefix    = XMLHandler.getTagValue(stepnode, "prefix");
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields = XMLHandler.countNodes(fields, "field");
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				fieldName[i] = XMLHandler.getTagValue(fnode, "name");
				String asc   = XMLHandler.getTagValue(fnode, "ascending");
				if (asc.equalsIgnoreCase("Y")) 
				     ascending[i]=true; 
				else ascending[i]=false;
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{		
		directory = "%%java.io.tmpdir%%";
		prefix    = "out";
		
		int nrfields = 0;
		
		allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			fieldName[i]="field"+i;
		}
	}

	public String getXML()
	{
		String retval="";

		retval+="      "+XMLHandler.addTagValue("directory", directory);
		retval+="      "+XMLHandler.addTagValue("prefix",    prefix);
		
		retval+="    <fields>"+Const.CR;
		for (int i=0;i<fieldName.length;i++)
		{
			retval+="      <field>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name",      fieldName[i]);
			retval+="        "+XMLHandler.addTagValue("ascending", ascending[i]);
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
			directory        =      rep.getStepAttributeString (id_step, "directory");
			prefix           =      rep.getStepAttributeString (id_step, "prefix");
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i]    = rep.getStepAttributeString(id_step, i, "field_name"); 
				ascending[i] = rep.getStepAttributeBoolean(id_step, i, "field_ascending"); 
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
			rep.saveStepAttribute(id_transformation, id_step, "directory",       directory);
			rep.saveStepAttribute(id_transformation, id_step, "prefix",          prefix);
			
			for (int i=0;i<fieldName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_ascending", ascending[i]);
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
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepMeta);
			remarks.add(cr);
			
			String  error_message="";
			boolean error_found=false;
			
			// Starting from selected fields in ...
			for (int i=0;i< fieldName.length;i++)
			{
				int idx = prev.searchValueIndex(fieldName[i]);
				if (idx<0)
				{
					error_message+="\t\t"+fieldName[i]+Const.CR;
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message="Sort keys that were not found in input stream:"+Const.CR+Const.CR+error_message;

				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				if (fieldName.length>0)
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All sort keys are found in the input stream.", stepMeta);
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No sort keys are entered.", stepMeta);
					remarks.add(cr);
				}
			}
			
			// Check the sort directory
			File f = new File(Const.replEnv(directory));
			if (f.exists())
			{
				if (f.isDirectory())
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "["+directory+"] exists and is a directory", stepMeta);
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "["+directory+"] exists but is not a directory", stepMeta);
					remarks.add(cr);
				}
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Directory ["+directory+"] doesn't exist!", stepMeta);
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Couldn't find fields from previous steps, check the hops...!", stepMeta);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
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
		return new SortRowsDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SortRows(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SortRowsData();
	}
}
