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

package org.pentaho.di.trans.steps.rowstoresult;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
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
 * Writes results to a next transformation in a Job
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class RowsToResult extends BaseStep implements StepInterface
{
	private RowsToResultMeta meta;

	private RowsToResultData data;

	public RowsToResult(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta = (RowsToResultMeta) smi;
		data = (RowsToResultData) sdi;

		Object[] r = getRow(); // get row, set busy!
		if (r == null) // no more input to be expected...
		{
			getTransMeta().getResultRows().addAll(data.rows);

			setOutputDone();
			return false;
		}

		// Add all rows to rows buffer...
		data.rows.add( new RowMetaAndData(getInputRowMeta(), r) );
		data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		putRow(data.outputRowMeta, r); // copy row to possible alternate
										// rowset(s).

		if (checkFeedback(linesRead))
			logBasic(Messages.getString("RowsToResult.Log.LineNumber") + linesRead); //$NON-NLS-1$

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (RowsToResultMeta) smi;
		data = (RowsToResultData) sdi;

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
			logBasic(Messages.getString("RowsToResult.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped())
				;
		} catch (Exception e)
		{
			logError(Messages.getString("RowsToResult.Log.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		} finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
