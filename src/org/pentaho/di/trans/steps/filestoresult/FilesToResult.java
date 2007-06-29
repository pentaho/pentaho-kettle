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

package org.pentaho.di.trans.steps.filestoresult;

import java.io.IOException;
import java.util.Iterator;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Writes filenames to a next job entry in a Job
 * 
 * @author matt
 * @since 26-may-2006
 */
public class FilesToResult extends BaseStep implements StepInterface
{
	private FilesToResultMeta meta;

	private FilesToResultData data;

	public FilesToResult(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta = (FilesToResultMeta) smi;
		data = (FilesToResultData) sdi;

		Object[] r = getRow(); // get row, set busy!
		if (r == null) // no more input to be expected...
		{
			Iterator it = data.filenames.iterator();
			while (it.hasNext())
			{
				addResultFile((ResultFile) it.next());
			}
			logBasic(Messages.getString("FilesToResult.Log.AddedNrOfFiles", String.valueOf(data.filenames
					.size())));
			setOutputDone();
			return false;
		}

		if (first)
		{
			first = false;

			for (int i = 0; i < r.length; i++)
				if (((ValueMeta) r[i]).getName().equals(meta.getFilenameField()))
				{
					data.filenameIndex = i;
					break;
				}

			if (data.filenameIndex < 0)
			{
				logError(Messages.getString("FilesToResult.Log.CouldNotFindField", meta.getFilenameField())); //$NON-NLS-1$ //$NON-NLS-2$
				setErrors(1);
				stopAll();
				return false;
			}
		}

		// OK, get the filename field from the row
		ValueMeta filenameValue = (ValueMeta) r[data.filenameIndex];

		String filename = filenameValue.getString(filenameValue);

		try
		{
			ResultFile resultFile = new ResultFile(meta.getFileType(), KettleVFS.getFileObject(filename),
					getTrans().getName(), getStepname());

			// Add all rows to rows buffer...
			data.filenames.add(resultFile);
		} catch (IOException e)
		{
			throw new KettleException(e);
		}

		// Copy to any possible next steps...
		data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, getStepname(), null, null);
		putRow(data.outputRowMeta, r); // copy row to possible alternate
		// rowset(s).

		if ((linesRead > 0) && (linesRead % Const.ROWS_UPDATE) == 0)
			logBasic(Messages.getString("FilesToResult.Log.LineNumber") + linesRead); //$NON-NLS-1$

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (FilesToResultMeta) smi;
		data = (FilesToResultData) sdi;

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
			logBasic(Messages.getString("FilesToResult.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped())
				;
		} catch (Exception e)
		{
			logError(Messages.getString("FilesToResult.Log.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
