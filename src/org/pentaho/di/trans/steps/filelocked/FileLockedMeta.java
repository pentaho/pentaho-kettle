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

package org.pentaho.di.trans.steps.filelocked;

import org.w3c.dom.Node;
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

/**
 * Check if a file is locked
 *  *  
 * @author Samatar
 * @since 03-Juin-2009
 *
 */

public class FileLockedMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = FileLockedMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private boolean addresultfilenames;
	
    /** dynamic filename */
    private String       filenamefield;
    
    /** function result: new value name */
    private String       resultfieldname;
    
    public FileLockedMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the filenamefield.
     */
    public String getDynamicFilenameField()
    {
        return filenamefield;
    }

    /**
     * @param filenamefield The filenamefield to set.
     */
    public void setDynamicFilenameField(String filenamefield)
    {
        this.filenamefield = filenamefield;
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
    
    
    public boolean addResultFilenames()
    {
    	return addresultfilenames;
    }
    
    public void setaddResultFilenames(boolean addresultfilenames)
    {
    	this.addresultfilenames=addresultfilenames;
    }
    
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode, databases);
	}
 
 

    public Object clone()
    {
        FileLockedMeta retval = (FileLockedMeta) super.clone();
       
        return retval;
    }

    public void setDefault()
    {
        resultfieldname = "result"; //$NON-NLS-1$
        addresultfilenames=false;
    }
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{    
        if (!Const.isEmpty(resultfieldname))
        {
            ValueMetaInterface v = new ValueMeta(resultfieldname, ValueMeta.TYPE_BOOLEAN);
            v.setOrigin(name);
            inputRowMeta.addValueMeta(v);
        }
    }
	

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    " + XMLHandler.addTagValue("filenamefield", filenamefield)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("addresultfilenames",       addresultfilenames));
        return retval.toString();
    }

    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
	throws KettleXMLException
	{
	    try
	    {
            filenamefield = XMLHandler.getTagValue(stepnode, "filenamefield"); //$NON-NLS-1$
            resultfieldname = XMLHandler.getTagValue(stepnode, "resultfieldname");
            addresultfilenames  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addresultfilenames"));   
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "FileLockedMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
        try
        {
            filenamefield = rep.getStepAttributeString(id_step, "filenamefield"); //$NON-NLS-1$
            resultfieldname = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
            addresultfilenames  = rep.getStepAttributeBoolean(id_step, "addresultfilenames");   
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "FileLockedMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "filenamefield", filenamefield); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "addresultfilenames",          addresultfilenames);
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "FileLockedMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }
    
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

      
        if (Const.isEmpty(resultfieldname))
        {
            error_message = BaseMessages.getString(PKG, "FileLockedMeta.CheckResult.ResultFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "FileLockedMeta.CheckResult.ResultFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
        if (Const.isEmpty(filenamefield))
        {
            error_message = BaseMessages.getString(PKG, "FileLockedMeta.CheckResult.FileFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "FileLockedMeta.CheckResult.FileFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FileLockedMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "FileLockedMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new FileLocked(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new FileLockedData();
    }

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
