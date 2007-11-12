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

package org.pentaho.di.trans.steps.append;

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

/**
 * @author Sven Boden
 * @since  3-june-2007
 */
public class AppendMeta extends BaseStepMeta implements StepMetaInterface
{
	private String   headStepName;
	private StepMeta headStepMeta;

	private String   tailStepName;  
	private StepMeta tailStepMeta;

	
    public AppendMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	/**
     * @return Returns the hopname to be used as tail
     */
    public String getTailStepName()
    {
		if (tailStepMeta!=null && 
	        tailStepMeta.getName()!=null &&
	        tailStepMeta.getName().length()>0
		   ) 
			return tailStepMeta.getName();
		return null;
   }
 
	/**
     * @return Returns the hopname to be used as head
     */
    public String getHeadStepName()
    {
		if (headStepMeta!=null && 
	        headStepMeta.getName()!=null &&
	        headStepMeta.getName().length()>0
		   ) 
			return headStepMeta.getName();
		return null;
    }
    
    /**
     * @param tailStepname The tailStepname to set.
     */
    public void setTailStepName(String tailStepname)
    {
        this.tailStepName = tailStepname;
    }
    
    /**
     * @param headStepname The headStepname to set.
     */
    public void setHeadStepName(String headStepname)
    {
        this.headStepName = headStepname;
    }
    
    /**
     * @return Returns the tailStep.
     */
    public StepMeta getTailStepMeta()
    {
        return tailStepMeta;
    }
    
    /**
     * @return Returns the headStep.
     */
    public StepMeta getHeadStepMeta()
    {
        return headStepMeta;
    }
    
    /**
     * @param tailStep The tailStep to set.
     */
    public void setTailStepMeta(StepMeta tailStep)
    {
        this.tailStepMeta = tailStep;
    }
	
    /**
     * @param headStep The headStep to set.
     */
    public void setHeadStepMeta(StepMeta headStep)
    {
        this.headStepMeta = headStep;
    }
	
	public Object clone()
	{
		AppendMeta retval = (AppendMeta)super.clone();

        return retval;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

		retval.append(XMLHandler.addTagValue("head_name", getHeadStepName()));	//$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("tail_name", getTailStepName()));	//$NON-NLS-1$

		return retval.toString();
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{ 
			headStepName = XMLHandler.getTagValue(stepnode, "head_name");  //$NON-NLS-1$
			tailStepName = XMLHandler.getTagValue(stepnode, "tail_name");  //$NON-NLS-1$			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("AppendMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
	}
    
    public String[] getInfoSteps()
    {
        return new String[] { headStepName, tailStepName }; 
    }

    /**
     * @param infoSteps The info-step(s) to set
     */
    public void setInfoSteps(StepMeta[] infoSteps)
    {
        if (infoSteps!=null && infoSteps.length==2)
        {
            headStepMeta = infoSteps[0];
            tailStepMeta = infoSteps[1];
        }
    }

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			headStepName =  rep.getStepAttributeString (id_step, "head_name");  //$NON-NLS-1$
			tailStepName =  rep.getStepAttributeString (id_step, "tail_name");  //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("AppendMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "head_name", getHeadStepName()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "tail_name", getTailStepName()); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("AppendMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	
	public void searchInfoAndTargetSteps(List<StepMeta> steps)
	{
		headStepMeta = StepMeta.findStep(steps, headStepName);
		tailStepMeta = StepMeta.findStep(steps, tailStepName);
	}

	public boolean chosesTargetSteps()
	{
	    return false;
	}

	public String[] getTargetSteps()
	{
	    return null;
	}
    
    public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        // We don't have any input fields here in "r" as they are all info fields.
        // So we just take the info fields.
        //
        if (info!=null)
        {
            for (int i=0;i<info.length;i++)
            r.mergeRowMeta(info[i]);
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info)
	{
		CheckResult cr;
		
		if (getHeadStepName()!=null && getTailStepName()!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("AppendMeta.CheckResult.SourceStepsOK"), stepMeta);
			remarks.add(cr);
		}
		else if (getHeadStepName()==null && getTailStepName()==null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("AppendMeta.CheckResult.SourceStepsMissing"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("AppendMeta.CheckResult.OneSourceStepMissing"), stepMeta);
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new Append(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new AppendData();
	}
}