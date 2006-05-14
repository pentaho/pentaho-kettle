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
 
package be.ibridge.kettle.trans.step.rowsfromresult;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Reads results from a previous transformation in a Job
 * 
 * @author Matt
 * @since 2-jun-2003
 */

public class RowsFromResult extends BaseStep implements StepInterface
{
	private RowsFromResultMeta meta;
	private RowsFromResultData data;
	
	public RowsFromResult(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(RowsFromResultMeta)getStepMeta().getStepMetaInterface();
		data=(RowsFromResultData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (getTransMeta().getSourceRows()==null || data.pointer>=getTransMeta().getSourceRows().size())
		{
			setOutputDone();
			return false;
		}
		
		Row r=(Row)getTransMeta().getSourceRows().get((int)linesRead);
		linesRead++;
		
		putRow(r);     // copy row to possible alternate rowset(s).

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic(Messages.getString("RowsFromResult.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(RowsFromResultMeta)smi;
		data=(RowsFromResultData)sdi;
		
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
			logBasic(Messages.getString("RowsFromResult.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("RowsFromResult.Log.UnexpectedError")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
