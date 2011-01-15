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

package org.pentaho.di.trans.steps.prioritizestreams;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/*
 * Created on 30-06-2008
 *
 */


public class PrioritizeStreamsMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = PrioritizeStreamsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** by which steps to display? */
    private String  stepName[];
    
	public PrioritizeStreamsMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode, databases);
	}


	public Object clone()
	{	
        PrioritizeStreamsMeta retval = (PrioritizeStreamsMeta) super.clone();

        int nrfields = stepName.length;

        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.stepName[i] = stepName[i];
        }
		return retval;
	}
	   public void allocate(int nrfields)
	    {
	        stepName = new String[nrfields]; 
	    }
    /**
     * @return Returns the stepName.
     */
    public String[] getStepName()
    {
        return stepName;
    }
    /**
     * @param stepName The stepName to set.
     */
    public void setStepName(String[] stepName)
    {
        this.stepName = stepName;
    }
    public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: nothing changes to rowMeta
    }
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
	throws KettleXMLException
	{
	  try
	    {
		  Node steps = XMLHandler.getSubNode(stepnode, "steps");
          int nrsteps = XMLHandler.countNodes(steps, "step");

          allocate(nrsteps);

          for (int i = 0; i < nrsteps; i++)
          {
              Node fnode = XMLHandler.getSubNodeByNr(steps, "step", i);
              stepName[i] = XMLHandler.getTagValue(fnode, "name");
          }
	    }
      catch (Exception e)
      {
          throw new KettleXMLException("Unable to load step info from XML", e);
      }
	}
   public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        
        retval.append("    <steps>" + Const.CR);
        for (int i = 0; i < stepName.length; i++)
        {
            retval.append("      <step>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", stepName[i]));
            retval.append("        </step>" + Const.CR);
        }
        retval.append("      </steps>" + Const.CR);

        return retval.toString();
    }
	public void setDefault()
	{
        int nrsteps = 0;

        allocate(nrsteps);

        for (int i = 0; i < nrsteps; i++)
        {
            stepName[i] = "step" + i;
        }
	}


	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
	        try
	        {

	            int nrsteps = rep.countNrStepAttributes(id_step, "step_name");

	            allocate(nrsteps);

	            for (int i = 0; i < nrsteps; i++)
	            {
	                stepName[i] = rep.getStepAttributeString(id_step, i, "step_name");
	            }
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unexpected error reading step information from the repository", e);
	        }
	    }
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
	throws KettleException
	{
        try
        {
	            for (int i = 0; i < stepName.length; i++)
	            {
	                rep.saveStepAttribute(id_transformation, id_step, i, "step_name", stepName[i]);
	            }
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
	        }
	    }
		
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "PrioritizeStreamsMeta.CheckResult.NotReceivingFields"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
            if (stepName.length > 0)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "PrioritizeStreamsMeta.CheckResult.AllStepsFound"), stepMeta);
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "PrioritizeStreamsMeta.CheckResult.NoStepsEntered"), stepMeta);
                remarks.add(cr);
            }

		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "PrioritizeStreamsMeta.CheckResult.StepRecevingData2"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "PrioritizeStreamsMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new PrioritizeStreams(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new PrioritizeStreamsData();
	}

}
