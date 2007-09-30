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
 
package be.ibridge.kettle.trans.step.databasejoin;

import java.sql.ResultSet;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Use values from input streams to joins with values in a database.
 * Freehand SQL can be used to do this.
 * 
 * @author Matt
 * @since 26-apr-2003
 */
public class DatabaseJoin extends BaseStep implements StepInterface
{
	private DatabaseJoinMeta meta;
	private DatabaseJoinData data;
	
	public DatabaseJoin(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void lookupValues(Row row)
		throws KettleException
	{
		if (first)
		{
			first=false;

			if (log.isDetailed()) logDetailed(Messages.getString("DatabaseJoin.Log.CheckingRow")+row.toString()); //$NON-NLS-1$
			data.keynrs = new int[meta.getParameterField().length];
			
			for (int i=0;i<meta.getParameterField().length;i++)
			{
				data.keynrs[i]=row.searchValueIndex(meta.getParameterField()[i]);
				if (data.keynrs[i]<0)
				{
					throw new KettleStepException(Messages.getString("DatabaseJoin.Exception.FieldNotFound",meta.getParameterField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		
		// Construct the parameters row...
		Row param = new Row();
		for (int i=0;i<data.keynrs.length;i++)
		{
			param.addValue( row.getValue(data.keynrs[i]));
		}
		
		// Set the values on the prepared statement (for faster exec.)
		ResultSet rs = data.db.openQuery(data.pstmt, param);
		
		// Get a row from the database...
		Row add = data.db.getRow(rs);
		linesInput++;
		
		int counter = 0;
		while (add!=null && (meta.getRowLimit()==0 || counter<meta.getRowLimit()))
		{
			counter++;

			Row newrow = new Row(row);
			newrow.addRow(add);
			putRow(newrow);
			
			if (log.isRowLevel()) logRowlevel(Messages.getString("DatabaseJoin.Log.PutoutRow")+add); //$NON-NLS-1$
			
			// Get a new row
			if (meta.getRowLimit()==0 || counter<meta.getRowLimit()) 
			{
				add = data.db.getRow(rs);
				linesInput++;
			}
		}
		
		// Nothing found? Perhaps we have to put something out after all?
		if (counter==0 && meta.isOuterJoin())
		{
			if (data.notfound==null)
			{
				data.notfound = new Row(data.db.getReturnRow());
				// Set all values to NULL
				for (int i=0;i<data.notfound.size();i++) data.notfound.getValue(i).setNull();
			}
			Row newrow = new Row(row);
			newrow.addRow(data.notfound);
			putRow(newrow);
		}
		
		data.db.closeQuery(rs);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DatabaseJoinMeta)smi;
		data=(DatabaseJoinData)sdi;
		
		boolean sendToErrorRow=false;
		String errorMessage = null;


		Row r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		  
		try
		{
			lookupValues(r); // add new values to the row in rowset[0].
			
            if (checkFeedback(linesRead)) logBasic(Messages.getString("DatabaseJoin.Log.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{

			if (getStepMeta().isDoingErrorHandling())
			{
		          sendToErrorRow = true;
		          errorMessage = e.toString();
			}
			else
			{

				logError(Messages.getString("DatabaseJoin.Log.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(r, 1, errorMessage, null, "DBJOIN001");
			}

		}		
			
		return true;
	}
    
    /** Stop the running query */
    public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(DatabaseJoinMeta)smi;
        data=(DatabaseJoinData)sdi;

        if (data.db!=null)
        {
            data.db.cancelStatement(data.pstmt);
        }
    }

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DatabaseJoinMeta)smi;
		data=(DatabaseJoinData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(meta.getDatabaseMeta());
			try
			{
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }
				
				logBasic(Messages.getString("DatabaseJoin.Log.ConnectedToDB")); //$NON-NLS-1$
	
				// Prepare the SQL statement
				data.pstmt = data.db.prepareSQL(meta.getSql());
				data.db.setQueryLimit(meta.getRowLimit());
				
				return true;
			}
			catch(KettleException e)
			{
				logError(Messages.getString("DatabaseJoin.Log.DatabaseError")+e.getMessage()); //$NON-NLS-1$
				data.db.disconnect();
			}
		}
		
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DatabaseJoinMeta)smi;
	    data = (DatabaseJoinData)sdi;
	    
	    data.db.disconnect();
	    
	    super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		logBasic(Messages.getString("DatabaseJoin.Log.StartingToRun")); //$NON-NLS-1$
		
		try
		{
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("DatabaseJoin.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
