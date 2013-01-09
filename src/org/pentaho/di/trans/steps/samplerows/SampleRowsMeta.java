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

package org.pentaho.di.trans.steps.samplerows;

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
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/*
 * Created on 02-jun-2008
 *
 */

public class SampleRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = SampleRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String linesrange;
	private String linenumfield;
	public static String DEFAULT_RANGE="1";
	
	public SampleRowsMeta()
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
		Object retval = super.clone();
		return retval;
	}
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{   
        if (!Const.isEmpty(linenumfield))
        {
        	
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(linenumfield), ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			inputRowMeta.addValueMeta(v);
        }
	 }
		
	 private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
		throws KettleXMLException
		{
		try{
			linesrange = XMLHandler.getTagValue(stepnode, "linesrange");
			linenumfield = XMLHandler.getTagValue(stepnode, "linenumfield");
		}
	    catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "SampleRowsMeta.Exception.UnableToReadStepInfo"), e);
        }
	}
	public String getLinesRange()
	{
		return this.linesrange;
	}
	public void setLinesRange(String linesrange)
	{
		this.linesrange=linesrange;
	}
	public String getLineNumberField()
	{
		return this.linenumfield;
	}
	public void setLineNumberField(String linenumfield)
	{
		this.linenumfield=linenumfield;
	}
	
	public void setDefault()
	{
		linesrange=DEFAULT_RANGE;
		linenumfield=null;
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try{
			linesrange = rep.getStepAttributeString(id_step, "linesrange");
			linenumfield = rep.getStepAttributeString(id_step, "linenumfield");
			
		}
		 catch (Exception e)
	     {
	        throw new KettleException(BaseMessages.getString(PKG, "SampleRowsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
	     }
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "linesrange", linesrange);
			rep.saveStepAttribute(id_transformation, id_step, "linenumfield", linenumfield);
			
			
		}
		catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "SampleRowsMeta.Exception.UnexpectedErrorSavingStepInfo"), e); //$NON-NLS-1$
        }
	}
	 public String getXML()
	    {
	        StringBuffer retval = new StringBuffer();
	        retval.append("    " + XMLHandler.addTagValue("linesrange", linesrange));
	        retval.append("    " + XMLHandler.addTagValue("linenumfield", linenumfield));
	        
	        return retval.toString();
	    }
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		
		if (Const.isEmpty(linesrange))
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SampleRowsMeta.CheckResult.LinesRangeMissing"), stepinfo);
        else
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK,  BaseMessages.getString(PKG, "SampleRowsMeta.CheckResult.LinesRangeOk"), stepinfo);
		remarks.add(cr);
		
		if (prev==null || prev.size()==0)
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "SampleRowsMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SampleRowsMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
		remarks.add(cr);
		
		
		// See if we have input streams leading to this step!
		if (input.length>0)
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SampleRowsMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SampleRowsMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
		remarks.add(cr);
	}
	

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new SampleRows(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new SampleRowsData();
	}

}
