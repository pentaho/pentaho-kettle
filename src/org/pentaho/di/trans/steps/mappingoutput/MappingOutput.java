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
 
package org.pentaho.di.trans.steps.mappingoutput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
 */

public class MappingOutput extends BaseStep implements StepInterface
{
	private MappingOutputMeta meta;
	private MappingOutputData data;


	public MappingOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MappingOutputMeta)smi;
		data=(MappingOutputData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null) 
		{
            // No more input to be expected...
            // Tell the next steps.
            //
			if ( data.targetStep != null )
			{
				// Code hardening for bug #5054, data.mapping can be null when the
				// mapping step fails before any input row is generated. This way
				// the method "setConnectorStep()" of this step is not called leaving
				// data.mapping set to null.
			    data.targetStep.setOutputDone();
			}
			return false;
		}
		
        if (first)
        {
            first=false;
            
            data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null);
            
            // 
            // Wait until we know were to store the row...
            // However, don't wait forever, if we don't have a connection after 60 seconds: bail out! 
            //
            int totalsleep = 0;
            while (!isStopped() && data.targetStep==null)
            {
                try { totalsleep+=10; Thread.sleep(10); } catch(InterruptedException e) { stopAll(); }
                if (totalsleep>60000)
                {
                    throw new KettleException(Messages.getString("MappingOutput.Exception.UnableToConnectWithParentMapping", ""+(totalsleep/1000)));
                }
            }
        }
                
		data.targetStep.putRow(data.outputRowMeta, r);     // copy row to possible alternate rowset(s).

        if (checkFeedback(linesRead)) logBasic(Messages.getString("MappingOutput.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MappingOutputMeta)smi;
		data=(MappingOutputData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("MappingOutput.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("MappingOutput.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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

    public void setConnectorStep(StepInterface targetStep)
    {
        data.targetStep = targetStep;
        
        // OK, before we leave, let's see if there is a rowset that covers the path to this target step.
        // If not, we need to create a new RowSet and add it to the Input RowSets of the target step
        RowSet rowSet = new RowSet(getTransMeta().getSizeRowset());
        
        // This is always a single copy, but for source and target...
        rowSet.setThreadNameFromToCopy(getStepname(), 0, targetStep.getStepname(), 0);
        
        targetStep.getOutputRowSets().add(rowSet);
    }
}