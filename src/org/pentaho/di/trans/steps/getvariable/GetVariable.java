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
 
package org.pentaho.di.trans.steps.getvariable;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Get information from the System or the supervising transformation.
 * 
 * @author Matt 
 * @since 4-aug-2003
 */
public class GetVariable extends BaseStep implements StepInterface
{
	private GetVariableMeta meta;
	private GetVariableData data;
    
	public GetVariable(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
        setName(stepMeta.getName());
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] rowData;
		
		if (data.readsRows)
		{
			rowData = getRow();
			if (rowData==null)
			{
				setOutputDone();
				return false;
			}
		}
		else
		{
			rowData=new Object[0];
			linesRead++;
		}
		
		// initialize 
		if (first && rowData!=null)
		{
			first=false;
            
            // Make output metadata
            data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		}
		
        // Add the variables to the row...
		//
		// TODO: Maybe keep the Object[] for speed, although this step will always
		//       be used in "small" amounts.
        // 
		Object extraData[] = new Object[meta.getFieldName().length];
        for (int i=0;i<meta.getFieldName().length;i++)
        {
            String newValue = environmentSubstitute(meta.getVariableString()[i]);
            if ( log.isDetailed() )
                logDetailed("field ["+meta.getFieldName()[i]+"] has value ["+newValue+"]");
            extraData[i] = newValue;
        }
        
        rowData = RowDataUtil.addRowData(rowData, extraData);
        
        putRow(data.outputRowMeta, rowData);		     
					
        if (!data.readsRows) // Just one row and then stop!
        {
            setOutputDone();
            return false;
        }
        
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GetVariableMeta)smi;
		data=(GetVariableData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
			data.readsRows = false;
			StepMeta previous[] = getTransMeta().getPrevSteps(getStepMeta()); 
			if (previous!=null && previous.length>0)
			{
				data.readsRows = true;
			}
			
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error : "+e.toString());
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}