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

package org.pentaho.di.trans.steps.abort;

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

/**
 * Meta data for the abort step.
 *  
 * @author sboden
 */
public class AbortMeta  extends BaseStepMeta implements StepMetaInterface {
	private static Class<?> PKG = AbortMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/**
	 * Threshold to abort.
	 */
	private String rowThreshold;
	
	/**
	 * Message to put in log when aborting.
	 */
	private String message;
	
	/**
	 * Always log rows.
	 */
	private boolean alwaysLogRows;
	
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: no values are added to the row in the step
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        // See if we have input streams leading to this step!
        if (input.length == 0) {
            CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "AbortMeta.CheckResult.NoInputReceivedError"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        return new Abort(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    public StepDataInterface getStepData() {
        return new AbortData();
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
        readData(stepnode);        
    }

    public void setDefault() 
    {
    	rowThreshold  = "0";
    	message       = "";    	
    	alwaysLogRows = true;
    }
     
	public String getXML()
	{
        StringBuilder retval = new StringBuilder(200);

        retval.append("      ").append(XMLHandler.addTagValue("row_threshold", rowThreshold)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("message", message)); //$NON-NLS-1$ //$NON-NLS-2$
	    retval.append("      ").append(XMLHandler.addTagValue("always_log_rows", alwaysLogRows)); //$NON-NLS-1$ //$NON-NLS-2$
	    
	    return retval.toString();
	}

    private void readData(Node stepnode)
	    throws KettleXMLException
    {
    	try 
    	{
    	    rowThreshold  = XMLHandler.getTagValue(stepnode, "row_threshold"); //$NON-NLS-1$
    	    message       = XMLHandler.getTagValue(stepnode, "message"); //$NON-NLS-1$
    		alwaysLogRows = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "always_log_rows")); //$NON-NLS-1$ //$NON-NLS-2$
    	}
	    catch(Exception e)
	    {
 		    throw new KettleXMLException(BaseMessages.getString(PKG, "AbortMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
	    }    	
    }

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
	    try
	    {
	    	rowThreshold  = rep.getStepAttributeString(id_step, "row_threshold"); //$NON-NLS-1$
	    	message       = rep.getStepAttributeString(id_step, "message"); //$NON-NLS-1$
		    alwaysLogRows = rep.getStepAttributeBoolean(id_step, "always_log_rows"); //$NON-NLS-1$
	    }
	    catch(Exception e)
	    {
 		    throw new KettleException(BaseMessages.getString(PKG, "AbortMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
	    }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
	    throws KettleException
    {
	    try
	    {
	    	rep.saveStepAttribute(id_transformation, id_step, "row_threshold",   rowThreshold); //$NON-NLS-1$
	    	rep.saveStepAttribute(id_transformation, id_step, "message",         message); //$NON-NLS-1$
  	 	    rep.saveStepAttribute(id_transformation, id_step, "always_log_rows", alwaysLogRows); //$NON-NLS-1$
	    }
	    catch(Exception e)
	    {
		    throw new KettleException(BaseMessages.getString(PKG, "AbortMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
	    }
    }
    
	public String getMessage() 
	{
		return message;
	}

	public void setMessage(String message) 
	{
		this.message = message;
	}

	public String getRowThreshold() 
	{
		return rowThreshold;
	}

	public void setRowThreshold(String rowThreshold) 
	{
		this.rowThreshold = rowThreshold;
	}

	public boolean isAlwaysLogRows() 
	{
		return alwaysLogRows;
	}

	public void setAlwaysLogRows(boolean alwaysLogRows) 
	{
		this.alwaysLogRows = alwaysLogRows;
	}
}