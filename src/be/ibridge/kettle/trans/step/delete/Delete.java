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
package be.ibridge.kettle.trans.step.delete;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
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
 * Delete data in a database table.
 * 
 * 
 * @author Tom
 * @since 28-March-2006
 */
public class Delete extends BaseStep implements StepInterface
{
	private DeleteMeta meta;
	private DeleteData data;
	
	public Delete(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void deleteValues(Row row) throws KettleException
	{
		Row lu;

		if (first)
		{
			first=false;
			
			data.dbupd.prepareDelete(meta.getSchemaName(), meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition());
			
			// lookup the values!
			if (log.isDetailed()) logDetailed(Messages.getString("Delete.Log.CheckingRow")+row.toString()); //$NON-NLS-1$
			data.keynrs  = new int[meta.getKeyStream().length];
			data.keynrs2 = new int[meta.getKeyStream().length];
			for (int i=0;i<meta.getKeyStream().length;i++)
			{
				data.keynrs[i]=row.searchValueIndex(meta.getKeyStream()[i]);
				if (data.keynrs[i]<0 &&  // couldn't find field!
                    !"IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) &&   // No field needed! //$NON-NLS-1$
				    !"IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i])  // No field needed! //$NON-NLS-1$
                   )
				{
					throw new KettleStepException(Messages.getString("Delete.Exception.FieldRequired",meta.getKeyStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				data.keynrs2[i]=row.searchValueIndex(meta.getKeyStream2()[i]);
				if (data.keynrs2[i]<0 &&  // couldn't find field!
				    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed! //$NON-NLS-1$
				   )
				{
					throw new KettleStepException(Messages.getString("Delete.Exception.FieldRequired",meta.getKeyStream2()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				if (log.isDebug()) logDebug(Messages.getString("Delete.Log.FieldInfo",meta.getKeyStream()[i])+data.keynrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		lu = new Row();
		for (int i=0;i<meta.getKeyStream().length;i++)
		{
			if (data.keynrs[i]>=0)
			{
				lu.addValue( row.getValue(data.keynrs[i]) );
			}
			if (data.keynrs2[i]>=0)
			{
				lu.addValue( row.getValue(data.keynrs2[i]) );
			}
		}
		
		data.dbupd.setValuesUpdate(lu);
		
		if (log.isDebug()) logDebug(Messages.getString("Delete.Log.SetValuesForDelete",lu.toString(),""+row)); //$NON-NLS-1$ //$NON-NLS-2$

		//data.dbupd.updateRow();
		data.dbupd.updateRow(data.batchMode); //  delete in batch mode
		linesUpdated++;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DeleteMeta)smi;
		data=(DeleteData)sdi;
		
		 boolean sendToErrorRow=false;
	     String errorMessage = null;

		Row r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
                        // process batched deletes
                        try {
                            data.dbupd.updateFinished(data.batchMode);
                        } catch(Exception dbe)
                        {
                            logError("Unexpected error committing the database connection: "+dbe.toString());
                            logError(Const.getStackTracker(dbe));
                            setErrors(1);
                            stopAll();
        		}
                    
			setOutputDone();
			return false;
		}
		    
		try
		{
			deleteValues(r); // add new values to the row in rowset[0].
			putRow(r);       // copy row to output rowset(s);
			
            if (checkFeedback(linesRead)) logBasic(Messages.getString("Delete.Log.LineNumber")+linesRead); //$NON-NLS-1$
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
				logError(Messages.getString("Delete.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(r, 1, errorMessage, null, "DEL001");
	         }
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DeleteMeta)smi;
		data=(DeleteData)sdi;
		
		if (super.init(smi, sdi))
		{
                        data.batchMode = meta.getCommitSize()>0 && meta.useBatchUpdate();
			data.dbupd=new Database(meta.getDatabaseMeta());
			try 
			{
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.dbupd.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.dbupd.connect(getPartitionID());
                }
				
				logBasic(Messages.getString("Delete.Log.ConnectedToDB")); //$NON-NLS-1$
				
				data.dbupd.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleException ke)
			{
				logError(Messages.getString("Delete.Log.ErrorOccurred")+ke.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{

		meta=(DeleteMeta)smi;
		data=(DeleteData)sdi;
		
        try
        {
            if (!data.dbupd.isAutoCommit())
            {
                if (getErrors()==0) 
                {
                    data.dbupd.commit();
                }
                else
                {
                    data.dbupd.rollback();
                }
            }
            data.dbupd.closeUpdate();
        }
        catch(KettleDatabaseException e)
        {
            log.logError(toString(), Messages.getString("Delete.Log.UnableToCommitUpdateConnection")+data.dbupd+"] :"+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            setErrors(1);
        }
        
		data.dbupd.disconnect();

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("Delete.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("Delete.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
