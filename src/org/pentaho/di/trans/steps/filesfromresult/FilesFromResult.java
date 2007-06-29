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

package org.pentaho.di.trans.steps.filesfromresult;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
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
 * Reads results from a previous transformation in a Job
 * 
 * @author Matt
 * @since 2-jun-2003
 */

public class FilesFromResult extends BaseStep implements StepInterface
{
	private FilesFromResultMeta meta;

	private FilesFromResultData data;

	public FilesFromResult(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);

		meta = (FilesFromResultMeta) getStepMeta().getStepMetaInterface();
		data = (FilesFromResultData) stepDataInterface;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (data.resultFilesList == null || linesRead >= data.resultFilesList.size())
		{
			setOutputDone();
			return false;
		}

		ResultFile resultFile = (ResultFile) data.resultFilesList.get((int) linesRead);
		RowMetaAndData r = resultFile.getRow();
		data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
		smi.getFields(data.outputRowMeta, getStepname(), null, null);
		linesRead++;

		putRow(data.outputRowMeta, r.getData()); // copy row to possible alternate
										// rowset(s).

		if (checkFeedback(linesRead))
			logBasic(Messages.getString("FilesFromResult.Log.LineNumber") + linesRead); //$NON-NLS-1$

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (FilesFromResultMeta) smi;
		data = (FilesFromResultData) sdi;

		if (super.init(smi, sdi))
		{
			Result result = getTransMeta().getPreviousResult();

			if (result != null)
			{
				data.resultFilesList = result.getResultFilesList();
			} else
			{
				data.resultFilesList = null;
			}

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
			logBasic(Messages.getString("FilesFromResult.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped())
				;
		} catch (Exception e)
		{
			logError(Messages.getString("FilesFromResult.Log.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
