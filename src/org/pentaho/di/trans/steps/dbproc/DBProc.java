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
 
package org.pentaho.di.trans.steps.dbproc;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Retrieves values from a database by calling database stored procedures or functions
 *  
 * @author Matt
 * @since 26-apr-2003
 *
 */

public class DBProc extends BaseStep implements StepInterface
{
	private DBProcMeta meta;
	private DBProcData data;
	
	public DBProc(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private Object[] runProc(Object[] row) throws KettleException
	{
		int addIndex;
		RowMetaAndData add;

        if (first)
		{
			data.argnrs=new int[meta.getArgument().length];
			for (int i=0;i<meta.getArgument().length;i++)
			{
				if (!meta.getArgumentDirection()[i].equalsIgnoreCase("OUT")) // IN or INOUT //$NON-NLS-1$
				{
					data.argnrs[i]=data.previousMeta.indexOfValue(meta.getArgument()[i]);
					if (data.argnrs[i]<0)
					{
						logError(Messages.getString("DBProc.Log.ErrorFindingField")+meta.getArgument()[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("DBProc.Exception.CouldnotFindField",meta.getArgument()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				else
				{
					data.argnrs[i]=-1;
				}
			}
			data.db.setProcLookup(meta.getProcedure(), meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), 
			                      meta.getResultName(), meta.getResultType());
			first=false;
		}

		data.db.setProcValues(data.previousMeta, row, data.argnrs, meta.getArgumentDirection(), !Const.isEmpty(meta.getResultName())); 

		add=data.db.callProcedure(meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), meta.getResultName(), meta.getResultType());

		// Function return?
		if (!Const.isEmpty(meta.getResultName())) {
			row=RowDataUtil.addValueData(row, add.getData()[0]); //first is the function return
			addIndex=1;
		} else {
			addIndex=0;
		}
        // We are only expecting the OUT and INOUT arguments here.
        // The INOUT values need to replace the value with the same name in the row.
        //
		for (int i = 0; i < data.argnrs.length; i++) {
			if (meta.getArgumentDirection()[i].equalsIgnoreCase("OUT")) {
				//add
				row=RowDataUtil.addValueData(row, add.getData()[addIndex]); 
				addIndex++;
			} else if (meta.getArgumentDirection()[i].equalsIgnoreCase("INOUT")) {
				//replace
				row[data.argnrs[i]]=add.getData()[addIndex];
				addIndex++;
			}
			// IN not taken
		}
		return row;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DBProcMeta)smi;
		data=(DBProcData)sdi;
		
		boolean sendToErrorRow=false;
		String errorMessage = null;

		Object[] r=getRow();       // Get row from input rowset & set row busy!
		//TODO: a procedure/function could also have no input at all
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
        if (first)
		{
			// get the RowMeta
			data.previousMeta = (RowMetaInterface) getInputRowMeta().clone();
			data.addOutMeta = new RowMeta();
			meta.getFields(data.addOutMeta, getStepname(), null);
        	//assemble the outputMeta
        	data.outputMeta=(RowMetaInterface)data.previousMeta.clone();
        	data.outputMeta.addRowMeta(data.addOutMeta);
			// do not reset first, needed in runProc();
		}
		    
		try
		{
			r=runProc(r); // add new values to the row in rowset[0].
			putRow(data.outputMeta, r);  // copy row to output rowset(s);
				
            if (checkFeedback(linesRead)) logBasic(Messages.getString("DBProc.LineNumber")+linesRead); //$NON-NLS-1$
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
			
				logError(Messages.getString("DBProc.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
	        }
			
			 if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(data.outputMeta, r, 1, errorMessage, null, "TOP001");
	         }
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DBProcMeta)smi;
		data=(DBProcData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(meta.getDatabase());
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

                if (!meta.isAutoCommit())
                {
                    logBasic(Messages.getString("DBProc.Log.AutoCommit")); //$NON-NLS-1$
                    data.db.setCommit(9999);
                }
				logBasic(Messages.getString("DBProc.Log.ConnectedToDB")); //$NON-NLS-1$
				
				return true;
			}
			catch(KettleException e)
			{
				logError(Messages.getString("DBProc.Log.DBException")+e.getMessage()); //$NON-NLS-1$
				data.db.disconnect();
			}
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DBProcMeta)smi;
	    data = (DBProcData)sdi;
	    
        try
        {
            if (!meta.isAutoCommit())
            {
                data.db.commit();
            }
        }
        catch(KettleDatabaseException e)
        {
            logError(Messages.getString("DBProc.Log.CommitError")+e.getMessage());
        }
	    data.db.disconnect();
	    
	    super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run()
	{
		logBasic(Messages.getString("DBProc.Log.StartingToRun")); //$NON-NLS-1$
		
		try
		{
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("DBProc.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
