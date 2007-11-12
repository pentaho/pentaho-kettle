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


package org.pentaho.di.trans.steps.joinrows;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
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
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
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
			directory  = XMLHandler.getTagValue(stepnode, "directory"); //$NON-NLS-1$
			prefix     = XMLHandler.getTagValue(stepnode, "prefix"); //$NON-NLS-1$
			cacheSize = Const.toInt( XMLHandler.getTagValue(stepnode, "cache_size"), -1); //$NON-NLS-1$
			
			mainStepname = XMLHandler.getTagValue(stepnode, "main"); //$NON-NLS-1$

			Node compare = XMLHandler.getSubNode(stepnode, "compare"); //$NON-NLS-1$
			Node condnode = XMLHandler.getSubNode(compare, "condition"); //$NON-NLS-1$
	
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
			throw new KettleXMLException(Messages.getString("JoinRowsMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		directory = "%%java.io.tmpdir%%";; //$NON-NLS-1$
		prefix     = "out"; //$NON-NLS-1$
		cacheSize = 500;

		mainStepname = null;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);

		retval.append("      ").append(XMLHandler.addTagValue("directory",  directory)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("prefix",     prefix)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("cache_size", cacheSize)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("      ").append(XMLHandler.addTagValue("main", getLookupStepname())); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    <compare>").append(Const.CR); //$NON-NLS-1$
		
		if (condition!=null)
		{
			retval.append(condition.getXML());
		}
		
		retval.append("    </compare>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();	
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			directory        =      rep.getStepAttributeString (id_step, "directory"); //$NON-NLS-1$
			prefix           =      rep.getStepAttributeString (id_step, "prefix"); //$NON-NLS-1$
			cacheSize        = (int)rep.getStepAttributeInteger(id_step, "cache_size"); //$NON-NLS-1$
	
			mainStepname     =  rep.getStepAttributeString (id_step, "main"); //$NON-NLS-1$

			long id_condition = rep.getStepAttributeInteger(id_step, 0, "id_condition"); //$NON-NLS-1$
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
			throw new KettleException(Messages.getString("JoinRowsMeta.Exception.UnexpectedErrorInReadStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "directory",       directory); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "prefix",          prefix); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "cache_size",      cacheSize); //$NON-NLS-1$
	
			rep.saveStepAttribute(id_transformation, id_step, "main",  getLookupStepname()); //$NON-NLS-1$
			
			if (condition!=null) 
			{
				condition.saveRep(rep);
				rep.saveStepAttribute(id_transformation, id_step, "id_condition",  condition.getID()); //$NON-NLS-1$
                rep.insertTransStepCondition(id_transformation, id_step, condition.getID());
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("JoinRowsMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: nothing changes to rowMeta
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("JoinRowsMeta.CheckResult.StepReceivingDatas",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			// Check the sort directory
            String realDirectory = transMeta.environmentSubstitute(directory); 
			File f = new File( realDirectory );
			if (f.exists())
			{
				if (f.isDirectory())
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "["+realDirectory+Messages.getString("JoinRowsMeta.CheckResult.DirectoryExists"), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "["+realDirectory+Messages.getString("JoinRowsMeta.CheckResult.DirectoryExistsButNotValid"), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);
				}
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JoinRowsMeta.CheckResult.DirectoryDoesNotExist",realDirectory), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JoinRowsMeta.CheckResult.CouldNotFindFieldsFromPreviousSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("JoinRowsMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JoinRowsMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
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

	/**
	 * @param steps optionally search the info step in a list of steps
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps)
	{
		mainStep = StepMeta.findStep(steps, mainStepname);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new JoinRows(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new JoinRowsData();
	}

    public boolean excludeFromRowLayoutVerification()
    {
        return true;
    }
    
    
}
