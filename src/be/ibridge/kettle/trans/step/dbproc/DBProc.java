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
 
package be.ibridge.kettle.trans.step.dbproc;

import java.util.ArrayList;

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
    
    private void runProc(Row row) throws KettleException
    {
        int i;
        Row add;

        if (first)
        {
            first=false;
            data.argnrs=new int[meta.getArgument().length];
            data.addnrs=new ArrayList();
            for (i=0;i<meta.getArgument().length;i++)
            {
                if (!meta.getArgumentDirection()[i].equalsIgnoreCase("OUT")) // IN or INOUT //$NON-NLS-1$
                {
                    data.argnrs[i]=row.searchValueIndex(meta.getArgument()[i]);
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
                
                if (meta.getArgumentDirection()[i].equalsIgnoreCase("OUT") || meta.getArgumentDirection()[i].equalsIgnoreCase("INOUT")) // OUT or INOUT //$NON-NLS-1$
                {
                    data.addnrs.add(new Integer(data.argnrs[i])); // Given the logic above, ONLY the INOUT args have a valid index (meaning >=0)
                }
            }
            data.db.setProcLookup(meta.getProcedure(), meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), 
                                  meta.getResultName(), meta.getResultType());
        }

        data.db.setProcValues(row, data.argnrs, meta.getArgumentDirection(), !Const.isEmpty(meta.getResultName())); 

        add=data.db.callProcedure(meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), meta.getResultName(), meta.getResultType());
        // We are only expecting the OUT and INOUT arguments here.
        // The INOUT values need to replace the value with the same name in the row.
        //
        for (i=0;i<add.size();i++)
        {
            if (Const.isEmpty(meta.getArgument()))
            {
                int idx = ((Integer)data.addnrs.get(i)).intValue();
                if (idx<0)
                {
                    row.addValue( add.getValue(i) ); // new for OUT
                }
                else
                {
                    row.setValue(idx, add.getValue(i) ); // replace for INOUT
                }
            }
            else
            {
                if (i>0)
                {
                    int idx = ((Integer)data.addnrs.get(i)).intValue();
                    if (idx<0)
                    {
                        row.addValue( add.getValue(i) ); // new for OUT
                    }
                    else
                    {
                        row.setValue(idx, add.getValue(i) ); // replace for INOUT
                    }
                }
                else
                {
                    row.addValue( add.getValue(i) ); // the function return value
                }
            }
        }
    }
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(DBProcMeta)smi;
        data=(DBProcData)sdi;
        
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
            runProc(r); // add new values to the row in rowset[0].
            putRow(r);  // copy row to output rowset(s);
                
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
        	   putError(r, 1, errorMessage, null, "DBPROCO01");
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
