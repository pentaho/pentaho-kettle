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
package be.ibridge.kettle.trans.step.update;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Update data in a database table, does NOT ever perform an insert.
 * 
 * @author Matt
 * @since 26-apr-2003
 */
public class Update extends BaseStep implements StepInterface
{
	private UpdateMeta meta;
	private UpdateData data;
	
	public Update(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void lookupValues(Row row) throws KettleException
	{
		Row lu;
		Row add;
	
		if (first)
		{
			first=false;
			
			data.dblup.setLookup(meta.getSchemaName(), meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition(), meta.getUpdateLookup(), null, null);
			data.dbupd.prepareUpdate(meta.getSchemaName(), meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition(), meta.getUpdateLookup());
			
			// lookup the values!
			if (log.isDetailed()) logDetailed(Messages.getString("Update.Log.CheckingRow")+row.toString()); //$NON-NLS-1$
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
					throw new KettleStepException(Messages.getString("Update.Exception.FieldRequired",meta.getKeyStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				data.keynrs2[i]=row.searchValueIndex(meta.getKeyStream2()[i]);
				if (data.keynrs2[i]<0 &&  // couldn't find field!
				    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed! //$NON-NLS-1$
				   )
				{
					throw new KettleStepException(Messages.getString("Update.Exception.FieldRequired",meta.getKeyStream2()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				if (log.isDebug()) logDebug(Messages.getString("Update.Log.FieldHasDataNumbers",meta.getKeyStream()[i])+""+data.keynrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// Cache the position of the compare fields in Row row
			//
			data.valuenrs = new int[meta.getUpdateLookup().length];
			for (int i=0;i<meta.getUpdateLookup().length;i++)
			{
				data.valuenrs[i]=row.searchValueIndex(meta.getUpdateStream()[i]);
				if (data.valuenrs[i]<0)  // couldn't find field!
				{
					throw new KettleStepException(Messages.getString("Update.Exception.FieldRequired",meta.getUpdateStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (log.isDebug()) logDebug(Messages.getString("Update.Log.FieldHasDataNumbers",meta.getUpdateStream()[i])+""+data.valuenrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
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
		
		data.dblup.setValuesLookup(lu);
		
		if (log.isDebug()) logDebug(Messages.getString("Update.Log.ValuesSetForLookup",lu.toString(),""+row)); //$NON-NLS-1$ //$NON-NLS-2$
		add=data.dblup.getLookup();  // Got back the complete row!
		linesInput++;
		
		if (add==null) 
		{
			/* nothing was found: throw error!
			 */
            if (!meta.isErrorIgnored())
            {
                if (getStepMeta().isDoingErrorHandling())
                {
                    row.setIgnore();
                    if (data.stringErrorKeyNotFound==null)
                    {
                        data.stringErrorKeyNotFound=Messages.getString("Update.Exception.KeyCouldNotFound")+lu;
                        data.stringFieldnames="";
                        for (int i=0;i<lu.size();i++) 
                        {
                            if (i>0) data.stringFieldnames+=", ";
                            data.stringFieldnames+=lu.getValue(i).getName();
                        }
                    }
                    putError(row, 1L, data.stringErrorKeyNotFound, data.stringFieldnames, "UPD001");
                }
                else
                {
                    throw new KettleDatabaseException(Messages.getString("Update.Exception.KeyCouldNotFound")+lu); //$NON-NLS-1$
                }
            }
            else
            {
                log.logDetailed(toString(), Messages.getString("Update.Log.KeyCouldNotFound")+lu); //$NON-NLS-1$
                if (meta.getIgnoreFlagField()!=null && meta.getIgnoreFlagField().length()>0) // add flag field!
                {
                    row.addValue(new Value(meta.getIgnoreFlagField(), false));
                }
            }
		}
		else
		{
			if (log.isRowLevel()) logRowlevel(Messages.getString("Update.Log.FoundRow")+add.toString()); //$NON-NLS-1$
			/* Row was found:
			 *  
			 * UPDATE row or do nothing?
			 *
			 */
			boolean update = false;
			for (int i=0;i<data.valuenrs.length;i++)
			{
				Value rowvalue = row.getValue(data.valuenrs[i]);
				lu.addValue(i, rowvalue);
				Value retvalue = add.getValue(i);
				if (!rowvalue.equals(retvalue))
				{
					update=true;
				}
			}
			if (update)
			{
				if (log.isRowLevel()) logRowlevel(Messages.getString("Update.Log.UpdateRow")+lu.toString()); //$NON-NLS-1$
				data.dbupd.setValuesUpdate(lu);
				data.dbupd.updateRow();
				linesUpdated++;
			}
			else
			{
				linesSkipped++;
			}
            
            if (meta.getIgnoreFlagField()!=null && meta.getIgnoreFlagField().length()>0) // add flag field!
            {
                row.addValue(new Value(meta.getIgnoreFlagField(), true));
            }
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(UpdateMeta)smi;
		data=(UpdateData)sdi;
		
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
			
            if (!r.isIgnored()) putRow(r); // copy row to output rowset(s);
			
            if (checkFeedback(linesRead)) logBasic(Messages.getString("Update.Log.LineNumber")+linesRead); //$NON-NLS-1$
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
				logError(Messages.getString("Update.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			 if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(r, 1, errorMessage, null, "UPD001");
	         }
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(UpdateMeta)smi;
		data=(UpdateData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.dblup=new Database(meta.getDatabaseMeta());
			data.dbupd=new Database(meta.getDatabaseMeta());
			try 
			{
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) 
                    { 
                        data.dblup.connect(getTrans().getThreadName(), getPartitionID());
                        data.dbupd.connect(getTrans().getThreadName(), getPartitionID());
                    }
                }
                else
                {
                    data.dblup.connect(getPartitionID());
                    data.dbupd.connect(getPartitionID());
                }
                
				logBasic(Messages.getString("Update.Log.ConnectedToDB")); //$NON-NLS-1$
				
				data.dbupd.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleException ke)
			{
				logError(Messages.getString("Update.Log.ErrorOccurred")+ke.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(UpdateMeta)smi;
		data=(UpdateData)sdi;
		
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
            log.logError(toString(), Messages.getString("Update.Log.UnableToCommitUpdateConnection")+data.dbupd+"] :"+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            setErrors(1);
        }
        
        
        
		data.dblup.disconnect();
		data.dbupd.disconnect();

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("Update.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("Update.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
