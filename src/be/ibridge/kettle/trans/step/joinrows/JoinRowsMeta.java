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


package be.ibridge.kettle.trans.step.joinrows;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Condition;
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
 
public class JoinRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	private String  directory;
	private String  prefix;
	private int     cacheSize;
	
	/**Which step is providing the lookup data?*/
	private StepMeta mainStep;
	
	/**Which step is providing the lookup data?*/
	private String   mainStepname;
	
	/**Optional condition to limit the join (where clause)*/
	private Condition condition;

	/**
	 * @return Returns the lookupFromStep.
	 */
	public StepMeta getMainStep()
	{
		return mainStep;
	}
	
	/**
	 * @param lookupFromStep The lookupFromStep to set.
	 */
	public void setMainStep(StepMeta lookupFromStep)
	{
		this.mainStep = lookupFromStep;
	}
	
	/**
	 * @return Returns the lookupFromStepname.
	 */
	public String getMainStepname()
	{
		return mainStepname;
	}
	
	/**
	 * @param lookupFromStepname The lookupFromStepname to set.
	 */
	public void setMainStepname(String lookupFromStepname)
	{
		this.mainStepname = lookupFromStepname;
	}

	/**
     * @param cacheSize The cacheSize to set.
     */
    public void setCacheSize(int cacheSize)
    {
        this.cacheSize = cacheSize;
    }
    
    /**
     * @return Returns the cacheSize.
     */
    public int getCacheSize()
    {
        return cacheSize;
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
    
    /**
     * @return Returns the condition.
     */
    public Condition getCondition()
    {
        return condition;
    }
    
    /**
     * @param condition The condition to set.
     */
    public void setCondition(Condition condition)
    {
        this.condition = condition;
    }
    
	public JoinRowsMeta()
	{
		super(); // allocate BaseStepMeta
		condition = new Condition();
	}
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		JoinRowsMeta retval = (JoinRowsMeta)super.clone();

		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			directory  = XMLHandler.getTagValue(stepnode, "directory");
			prefix     = XMLHandler.getTagValue(stepnode, "prefix");
			cacheSize = Const.toInt( XMLHandler.getTagValue(stepnode, "cache_size"), -1);
			
			mainStepname = XMLHandler.getTagValue(stepnode, "main");

			Node compare = XMLHandler.getSubNode(stepnode, "compare");
			Node condnode = XMLHandler.getSubNode(compare, "condition");
	
			// The new situation...
			if (condnode!=null)
			{
				condition = new Condition(condnode);
			}
			else
			{
				condition = new Condition();
			}

		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read step information from XML", e);
		}
	}

	public void setDefault()
	{
		directory = "%%java.io.tmpdir%%";;
		prefix     = "out";
		cacheSize = 500;

		mainStepname = null;
	}

	public String getXML()
	{
		String retval="";

		retval+="      "+XMLHandler.addTagValue("directory",  directory);
		retval+="      "+XMLHandler.addTagValue("prefix",     prefix);
		retval+="      "+XMLHandler.addTagValue("cache_size", cacheSize);
		
		retval+="      "+XMLHandler.addTagValue("main", getLookupStepname());

		retval+="    <compare>"+Const.CR;
		
		if (condition!=null)
		{
			retval+=condition.getXML();
		}
		
		retval+="    </compare>"+Const.CR;

		return retval;	
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			directory        =      rep.getStepAttributeString (id_step, "directory");
			prefix           =      rep.getStepAttributeString (id_step, "prefix");
			cacheSize        = (int)rep.getStepAttributeInteger(id_step, "cache_size");
	
			mainStepname     =  rep.getStepAttributeString (id_step, "main");

			long id_condition = rep.getStepAttributeInteger(id_step, 0, "id_condition");
			if (id_condition>0)
			{
				condition = new Condition(rep, id_condition);
			}
			else
			{
				condition = new Condition();
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
			rep.saveStepAttribute(id_transformation, id_step, "cache_size",      cacheSize);
	
			rep.saveStepAttribute(id_transformation, id_step, "main",  getLookupStepname());
			
			if (condition!=null) 
			{
				condition.saveRep(rep);
				rep.saveStepAttribute(id_transformation, id_step, "id_condition",  condition.getID());
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
			
			// Check the sort directory
			File f = new File(directory);
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

	public String getLookupStepname()
	{
		if (mainStep!=null && 
			mainStep.getName()!=null &&
			mainStep.getName().length()>0
		   ) 
			return mainStep.getName();
		return null;
	}

	public void searchInfoAndTargetSteps(ArrayList steps)
	{
		mainStep = TransMeta.findStep(steps, mainStepname);
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new JoinRowsDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new JoinRows(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new JoinRowsData();
	}

}
