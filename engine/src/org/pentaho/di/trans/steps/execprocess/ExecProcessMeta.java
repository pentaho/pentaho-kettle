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

package org.pentaho.di.trans.steps.execprocess;

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
 * Created on 03-11-2008
 * 
 */

public class ExecProcessMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = ExecProcessMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** dynamic process field name */
    private String       processfield;
    
    /** function result: new value name */
    private String       resultfieldname;
    
    /** function result: error fieldname */
    private String       errorfieldname;
    
    /** function result: exit value fieldname */
    private String       exitvaluefieldname;
    
    /** fail if the exit status is different from 0 **/
    private boolean      failwhennotsuccess;
    
    
    public ExecProcessMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the processfield.
     */
    public String getProcessField()
    {
        return processfield;
    }

    /**
     * @param processfield The processfield to set.
     */
    public void setProcessField(String processfield)
    {
        this.processfield = processfield;
    }

    /**
     * @return Returns the resultName.
     */
    public String getResultFieldName()
    {
        return resultfieldname;
    }

    /**
     * @param resultfieldname The resultfieldname to set.
     */
    public void setResultFieldName(String resultfieldname)
    {
        this.resultfieldname = resultfieldname;
    }
    /**
     * @return Returns the errorfieldname.
     */
    public String getErrorFieldName()
    {
        return errorfieldname;
    }

    /**
     * @param errorfieldname The errorfieldname to set.
     */
    public void setErrorFieldName(String errorfieldname)
    {
        this.errorfieldname = errorfieldname;
    }
    /**
     * @return Returns the exitvaluefieldname.
     */
    public String getExitValueFieldName()
    {
        return exitvaluefieldname;
    }

    /**
     * @param exitvaluefieldname The exitvaluefieldname to set.
     */
    public void setExitValueFieldName(String exitvaluefieldname)
    {
        this.exitvaluefieldname = exitvaluefieldname;
    }
    /**
     * @return Returns the failwhennotsuccess.
     */
    public boolean isFailWhenNotSuccess()
    {
        return failwhennotsuccess;
    }

    /**
     * @param failwhennotsuccess The failwhennotsuccess to set.
     */
    public void setFailWhentNoSuccess(boolean failwhennotsuccess)
    {
        this.failwhennotsuccess = failwhennotsuccess;
    }


	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode, databases);
	}
 

    public Object clone()
    {
        ExecProcessMeta retval = (ExecProcessMeta) super.clone();
       
        return retval;
    }

    public void setDefault()
    {
        resultfieldname = "Result output"; //$NON-NLS-1$
        errorfieldname="Error output";
        exitvaluefieldname="Exit value";
        failwhennotsuccess=false;
    }
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{    	
        // Output fields (String)
		String realOutputFieldname=space.environmentSubstitute(resultfieldname);
		 if (!Const.isEmpty(realOutputFieldname))
	     {
			 ValueMetaInterface v = new ValueMeta(realOutputFieldname, ValueMeta.TYPE_STRING);
			 v.setLength(100, -1);
			 v.setOrigin(name);
			 inputRowMeta.addValueMeta(v);
	     }
        String realerrofieldname=space.environmentSubstitute(errorfieldname);
        if (!Const.isEmpty(realerrofieldname))
        {
        	ValueMetaInterface v = new ValueMeta(realerrofieldname, ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
            v.setOrigin(name);
            inputRowMeta.addValueMeta(v);
        }
        String realexitvaluefieldname=space.environmentSubstitute(exitvaluefieldname);
        if (!Const.isEmpty(realexitvaluefieldname))
        {
        	ValueMetaInterface v = new ValueMeta(realexitvaluefieldname, ValueMeta.TYPE_INTEGER);
            v.setLength(ValueMeta.DEFAULT_INTEGER_LENGTH, 0);
            v.setOrigin(name);
            inputRowMeta.addValueMeta(v);
        }
    }
	

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    " + XMLHandler.addTagValue("processfield", processfield)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("errorfieldname", errorfieldname));
        retval.append("    " + XMLHandler.addTagValue("exitvaluefieldname", exitvaluefieldname));
        retval.append("    " + XMLHandler.addTagValue("failwhennotsuccess", failwhennotsuccess));
        return retval.toString();
    }

    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
	throws KettleXMLException
	{
    	try{
			processfield = XMLHandler.getTagValue(stepnode, "processfield"); //$NON-NLS-1$
            resultfieldname = XMLHandler.getTagValue(stepnode, "resultfieldname"); 
            errorfieldname = XMLHandler.getTagValue(stepnode, "errorfieldname"); 
            exitvaluefieldname = XMLHandler.getTagValue(stepnode, "exitvaluefieldname"); 
            failwhennotsuccess     = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "failwhennotsuccess"));
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "ExecProcessMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
        try
        {
        	processfield = rep.getStepAttributeString(id_step, "processfield"); //$NON-NLS-1$
            resultfieldname = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$  
            errorfieldname = rep.getStepAttributeString(id_step, "errorfieldname");    
            exitvaluefieldname = rep.getStepAttributeString(id_step, "exitvaluefieldname"); 
            failwhennotsuccess   =      rep.getStepAttributeBoolean(id_step, "failwhennotsuccess");
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "ExecProcessMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "processfield", processfield); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "errorfieldname", errorfieldname);
            rep.saveStepAttribute(id_transformation, id_step, "exitvaluefieldname", exitvaluefieldname);
            rep.saveStepAttribute(id_transformation, id_step, "failwhennotsuccess",           failwhennotsuccess);
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "ExecProcessMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

      
        if (Const.isEmpty(resultfieldname))
        {
            error_message = BaseMessages.getString(PKG, "ExecProcessMeta.CheckResult.ResultFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "ExecProcessMeta.CheckResult.ResultFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
        }
        remarks.add(cr);
        
        if (Const.isEmpty(processfield))
        {
            error_message = BaseMessages.getString(PKG, "ExecProcessMeta.CheckResult.ProcessFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "ExecProcessMeta.CheckResult.ProcessFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
        }
        remarks.add(cr);
        
        // See if we have input streams leading to this step!
        if (input.length > 0)
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecProcessMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
        else
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecProcessMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
         remarks.add(cr);

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new ExecProcess(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new ExecProcessData();
    }

    public boolean supportsErrorHandling()
    {
        return failwhennotsuccess;
    }
}
