 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.clonerow;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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
 * Created on 27-06-2008
 *
 */

public class CloneRowMeta extends BaseStepMeta implements StepMetaInterface
{
	
	/** nr of clone rows */
	private String nrclones;
	
	/** Flag: add clone flag */
	
	private boolean addcloneflag;
	
	/** clone flag field*/
	private String cloneflagfield;
	
	public CloneRowMeta()
	{
		super(); // allocate BaseStepMeta
	}
   public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        retval.append("    " + XMLHandler.addTagValue("nrclones", nrclones));
        retval.append("    " + XMLHandler.addTagValue("addcloneflag",   addcloneflag));
        retval.append("    " + XMLHandler.addTagValue("cloneflagfield", cloneflagfield));
        return retval.toString();
    }
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	public String getNrClones()
	{
		return nrclones;
	}
	
	public void setNrClones(String nrclones)
	{
		this.nrclones=nrclones;
	}
	
	public boolean isAddCloneFlag()
	{
		return addcloneflag;
	}
	public void setAddCloneFlag(boolean addcloneflag)
	{
		this.addcloneflag=addcloneflag;
	}
	public String getCloneFlagField()
	{
		return cloneflagfield;
	}
	public void setCloneFlagField(String cloneflagfield)
	{
		this.cloneflagfield=cloneflagfield;
	}
	private void readData(Node stepnode) throws KettleXMLException
	{
		try{
			nrclones = XMLHandler.getTagValue(stepnode, "nrclones");
			addcloneflag = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addcloneflag"));
			cloneflagfield = XMLHandler.getTagValue(stepnode, "cloneflagfield");
		}
	    catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("CloneRowMeta.Exception.UnableToReadStepInfo"), e);
        }
	}

	public void setDefault()
	{
		nrclones="0";
		cloneflagfield=null;
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try{
			nrclones = rep.getStepAttributeString(id_step, "nrclones");
			addcloneflag =  rep.getStepAttributeBoolean(id_step, "addcloneflag");
			cloneflagfield = rep.getStepAttributeString(id_step, "cloneflagfield");
		}
		 catch (Exception e)
	     {
	        throw new KettleException(Messages.getString("CloneRowMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
	     }
	}
	
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "nrclones", nrclones);
			rep.saveStepAttribute(id_transformation, id_step, "addcloneflag",    addcloneflag);
			rep.saveStepAttribute(id_transformation, id_step, "cloneflagfield", cloneflagfield);	
		}
		catch (Exception e)
        {
            throw new KettleException(Messages.getString("CloneRowMeta.Exception.UnexpectedErrorSavingStepInfo"), e); //$NON-NLS-1$
        }
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		 // Output field (boolean) ?
		if(addcloneflag)
		{
			 if (!Const.isEmpty(cloneflagfield))
		     {
				 ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(cloneflagfield), ValueMeta.TYPE_BOOLEAN);
				 v.setOrigin(origin);
				 rowMeta.addValueMeta(v);
		     }
		}
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message="";
		
		if (Const.isEmpty(nrclones))
        {
            error_message = Messages.getString("CloneRowMeta.CheckResult.NrClonesdMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
        }
        else
        {
            error_message = Messages.getString("CloneRowMeta.CheckResult.NrClonesOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
        }
		remarks.add(cr);
		
		if(addcloneflag)
		{
			if (Const.isEmpty(cloneflagfield))
	        {
	            error_message = Messages.getString("CloneRowMeta.CheckResult.CloneFlagFieldMissing"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
	        }
	        else
	        {
	            error_message = Messages.getString("CloneRowMeta.CheckResult.CloneFlagFieldOk"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
	        }
			remarks.add(cr);
		}
		
	
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, Messages.getString("CloneRowMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("CloneRowMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
		}
		remarks.add(cr);
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("CloneRowMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("CloneRowMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
		}
		remarks.add(cr);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new CloneRow(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new CloneRowData();
	}


}
