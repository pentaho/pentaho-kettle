/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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


/*
 * Created on 27-06-2008
 *
 */

public class CloneRowMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = CloneRowMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** nr of clone rows */
	private String nrclones;
	
	/** Flag: add clone flag */
	
	private boolean addcloneflag;
	
	/** clone flag field*/
	private String cloneflagfield;
	
	private boolean nrcloneinfield;
	
	private String nrclonefield;
	
	private boolean addclonenum;
	private String clonenumfield;
	
	
	
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
        retval.append("    " + XMLHandler.addTagValue("nrcloneinfield",   nrcloneinfield));
        retval.append("    " + XMLHandler.addTagValue("nrclonefield", nrclonefield));
        
        
        retval.append("    " + XMLHandler.addTagValue("addclonenum",   addclonenum));
        retval.append("    " + XMLHandler.addTagValue("clonenumfield", clonenumfield));
        
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

	
	public boolean isNrCloneInField()
	{
		return nrcloneinfield;
	}
	public void setNrCloneInField(boolean nrcloneinfield)
	{
		this.nrcloneinfield=nrcloneinfield;
	}
	public boolean isAddCloneNum()
	{
		return addclonenum;
	}
	public void setAddCloneNum(boolean addclonenum)
	{
		this.addclonenum=addclonenum;
	}
	public String getCloneNumField()
	{
		return clonenumfield;
	}
	public void setCloneNumField(String clonenumfield)
	{
		this.clonenumfield=clonenumfield;
	}
	public String getNrCloneField()
	{
		return nrclonefield;
	}
	public void setNrCloneField(String nrclonefield)
	{
		this.nrclonefield=nrclonefield;
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
			nrcloneinfield = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "nrcloneinfield"));
			nrclonefield = XMLHandler.getTagValue(stepnode, "nrclonefield");
			addclonenum = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addclonenum"));
			clonenumfield = XMLHandler.getTagValue(stepnode, "clonenumfield");
			
			
		}
	    catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "CloneRowMeta.Exception.UnableToReadStepInfo"), e);
        }
	}

	public void setDefault()
	{
		nrclones="0";
		cloneflagfield=null;
		nrclonefield=null;
		nrcloneinfield=false;
		addcloneflag=false;
		addclonenum=false;
		clonenumfield=null;
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try{
			nrclones = rep.getStepAttributeString(id_step, "nrclones");
			addcloneflag =  rep.getStepAttributeBoolean(id_step, "addcloneflag");
			cloneflagfield = rep.getStepAttributeString(id_step, "cloneflagfield");
			nrcloneinfield =  rep.getStepAttributeBoolean(id_step, "nrcloneinfield");
			nrclonefield = rep.getStepAttributeString(id_step, "nrclonefield");
			addclonenum =  rep.getStepAttributeBoolean(id_step, "addclonenum");
			
			clonenumfield = rep.getStepAttributeString(id_step, "clonenumfield");
			
		}
		 catch (Exception e)
	     {
	        throw new KettleException(BaseMessages.getString(PKG, "CloneRowMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
	     }
	}
	
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "nrclones", nrclones);
			rep.saveStepAttribute(id_transformation, id_step, "addcloneflag",    addcloneflag);
			rep.saveStepAttribute(id_transformation, id_step, "cloneflagfield", cloneflagfield);	
			rep.saveStepAttribute(id_transformation, id_step, "nrcloneinfield",    nrcloneinfield);
			rep.saveStepAttribute(id_transformation, id_step, "nrclonefield", nrclonefield);
			rep.saveStepAttribute(id_transformation, id_step, "addclonenum",    addclonenum);
			
			rep.saveStepAttribute(id_transformation, id_step, "clonenumfield", clonenumfield);
			
		}
		catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "CloneRowMeta.Exception.UnexpectedErrorSavingStepInfo"), e); //$NON-NLS-1$
        }
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		 // Output field (boolean) ?
		if(addcloneflag)
		{
			String realfieldValue=space.environmentSubstitute(cloneflagfield);
			 if (!Const.isEmpty(realfieldValue))
		     {
				 ValueMetaInterface v = new ValueMeta(realfieldValue, ValueMeta.TYPE_BOOLEAN);
				 v.setOrigin(origin);
				 rowMeta.addValueMeta(v);
		     }
		}
		// Output clone row number
		if(addclonenum)
		{
			String realfieldValue=space.environmentSubstitute(clonenumfield);
			 if (!Const.isEmpty(realfieldValue))
		     {
				 ValueMetaInterface v = new ValueMeta(realfieldValue, ValueMeta.TYPE_INTEGER);
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
            error_message = BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.NrClonesdMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.NrClonesOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
        }
		remarks.add(cr);
		
		if(addcloneflag)
		{
			if (Const.isEmpty(cloneflagfield))
	        {
	            error_message = BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.CloneFlagFieldMissing"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
	        }
	        else
	        {
	            error_message = BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.CloneFlagFieldOk"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
	        }
			remarks.add(cr);
		}
		if(addclonenum)
		{
			if (Const.isEmpty(clonenumfield))
	        {
	            error_message = BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.CloneNumFieldMissing"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
	        }
	        else
	        {
	            error_message = BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.CloneNumFieldOk"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
	        }
			remarks.add(cr);
		}
		if(nrcloneinfield)
		{
			if (Const.isEmpty(nrclonefield))
	        {
	            error_message = BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.NrCloneFieldMissing"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
	        }
	        else
	        {
	            error_message = BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.NrCloneFieldOk"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
	        }
			remarks.add(cr);
		}
	
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
		}
		remarks.add(cr);
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "CloneRowMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
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
