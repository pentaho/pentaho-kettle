package org.pentaho.di.trans.steps.sql;

import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Execute one or more SQL statements in a script, one time or parameterised
 * (for every row)
 * 
 * @author Matt
 * @since 10-sep-2005
 */
public class ExecSQL extends BaseStep implements StepInterface
{
	private ExecSQLMeta meta;

	private ExecSQLData data;

	public ExecSQL(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
			Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public static final RowMetaAndData getResultRow(Result result, String upd, String ins, String del, String read)
	{
		RowMetaAndData resultRow = new RowMetaAndData();

		if (upd != null && upd.length() > 0)
		{
			ValueMeta meta = new ValueMeta(upd, ValueMetaInterface.TYPE_INTEGER);
			meta.setLength(9);
			resultRow.addValue(meta, new Long(result.getNrLinesUpdated()));
		}

		if (ins != null && ins.length() > 0)
		{
			ValueMeta meta = new ValueMeta(ins, ValueMetaInterface.TYPE_INTEGER);
			meta.setLength(9);
			resultRow.addValue(meta, new Long(result.getNrLinesOutput()));
		}

		if (del != null && del.length() > 0)
		{
			ValueMeta meta = new ValueMeta(del, ValueMetaInterface.TYPE_INTEGER);
			meta.setLength(9);
			resultRow.addValue(meta, new Long(result.getNrLinesDeleted()));
		}

		if (read != null && read.length() > 0)
		{
			ValueMeta meta = new ValueMeta(read, ValueMetaInterface.TYPE_INTEGER);
			meta.setLength(9);
			resultRow.addValue(meta, new Long(result.getNrLinesRead()));
		}

		return resultRow;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta = (ExecSQLMeta) smi;
		data = (ExecSQLData) sdi;

		if (!meta.isExecutedEachInputRow())
		{
			RowMetaAndData resultRow = getResultRow(data.result, meta.getUpdateField(), meta.getInsertField(), meta.getDeleteField(), meta.getReadField());
			putRow(resultRow.getRowMeta(), resultRow.getData());
			setOutputDone(); // Stop processing, this is all we do!
			return false;
		}

		Object[] row = getRow();
		if (row == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		StringBuffer sql = new StringBuffer(meta.getSql());
		
		if (first) // we just got started
		{
			first = false;
			
			data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			// Find the indexes of the arguments
			data.argumentIndexes = new int[meta.getArguments().length];
			for (int i = 0; i < meta.getArguments().length; i++)
			{
				data.argumentIndexes[i] = this.getInputRowMeta().indexOfValue(meta.getArguments()[i]);
				if (data.argumentIndexes[i] < 0)
				{
					logError(Messages.getString("ExecSQL.Log.ErrorFindingField") + meta.getArguments()[i] + "]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(Messages.getString("ExecSQL.Exception.CouldNotFindField", meta.getArguments()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			// Find the locations of the question marks in the String...
			// We replace the question marks with the values...
			// We ignore quotes etc. to make inserts easier...
			data.markerPositions = new ArrayList<Integer>();
			int len = sql.length();
			int pos = len - 1;
			while (pos >= 0)
			{
				if (sql.charAt(pos) == '?')
					data.markerPositions.add(new Integer(pos)); // save the
				// marker
				// position
				pos--;
			}
		}

		// Replace the values in the SQL string...
		for (int i = 0; i < data.markerPositions.size(); i++)
		{
			ValueMetaAndData value = (ValueMetaAndData)row[data.argumentIndexes[data.markerPositions.size() - i - 1]];
			int pos = ((Integer) data.markerPositions.get(i)).intValue();
			sql.replace(pos, pos + 1, value.getValueData().toString()); // replace the '?'
			// with the String
			// in the row.
		}

		if (log.isRowLevel())
			logRowlevel(Messages.getString("ExecSQL.Log.ExecutingSQLScript") + Const.CR + sql); //$NON-NLS-1$
		data.result = data.db.execStatements(sql.toString());

		RowMetaAndData add = getResultRow(data.result, meta.getUpdateField(), meta.getInsertField(), meta.getDeleteField(), meta.getReadField());
		
		row = RowDataUtil.addRowData(row, getInputRowMeta().size(), add.getData());

		if (!data.db.isAutoCommit()) {
			data.db.commit();
		}
		
		putRow(data.outputRowMeta,row); // send it out!

		if (checkFeedback(linesWritten))
			logBasic(Messages.getString("ExecSQL.Log.LineNumber") + linesWritten); //$NON-NLS-1$

		return true;
	}
	

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (ExecSQLMeta) smi;
		data = (ExecSQLData) sdi;

		logBasic(Messages.getString("ExecSQL.Log.FinishingReadingQuery")); //$NON-NLS-1$

		data.db.disconnect();

		super.dispose(smi, sdi);
	}

	/** Stop the running query */
	public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta = (ExecSQLMeta) smi;
		data = (ExecSQLData) sdi;

		if (data.db != null)
			data.db.cancelQuery();
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (ExecSQLMeta) smi;
		data = (ExecSQLData) sdi;

		if (super.init(smi, sdi))
		{
			data.db = new Database(meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);

			// Connect to the database
			try
			{
				if (getTransMeta().isUsingUniqueConnections())
				{
					synchronized (getTrans())
					{
						data.db.connect(getTrans().getThreadName(), getPartitionID());
					}
				} else
				{
					data.db.connect(getPartitionID());
				}

				if (log.isDetailed())
					logDetailed(Messages.getString("ExecSQL.Log.ConnectedToDB")); //$NON-NLS-1$

				// If the SQL needs to be executed once, this is a starting step
				// somewhere.
				if (!meta.isExecutedEachInputRow())
				{
					data.result = data.db.execStatements(meta.getSql());
				}
				return true;
			} catch (KettleException e)
			{
				logError(Messages.getString("ExecSQL.Log.ErrorOccurred") + e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
			}
		}

		return false;
	}

	//
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("ExecSQL.Log.StartingToRun")); //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data))
				;
		} catch (Exception e)
		{
			logError(Messages.getString("ExecSQL.Log.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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