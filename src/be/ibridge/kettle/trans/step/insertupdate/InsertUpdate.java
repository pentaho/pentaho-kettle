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
 
package be.ibridge.kettle.trans.step.insertupdate;

import java.util.ArrayList;
import java.util.List;

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
 * Performs a lookup in a database table.  If the key doesn't exist it inserts values 
 * into the table, otherwise it performs an update of the changed values.
 * If nothing changed, do nothing.
 *  
 * @author Matt
 * @since 26-apr-2003
 */
public class InsertUpdate extends BaseStep implements StepInterface
{
	private InsertUpdateMeta meta;
	private InsertUpdateData data;
	
	public InsertUpdate(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
			
			data.db.setLookup(meta.getSchemaName(), meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition(), meta.getUpdateLookup(), null, null);
            
            Row ins = new Row();
            // Insert the update fields: just names.  Type doesn't matter!
            for (int i=0;i<meta.getUpdateLookup().length;i++) 
            {
                if (ins.searchValueIndex(meta.getUpdateLookup()[i])<0) // Don't add twice!
                {
                    ins.addValue( new Value(meta.getUpdateLookup()[i]) );
                }
            }
            data.db.prepareInsert(ins, meta.getSchemaName(), meta.getTableName());
            
            if (!meta.isUpdateBypassed())
            {
            	List updateColumns = new ArrayList();
            	for(int i=0;i<meta.getUpdate().length;i++) {
            		if(meta.getUpdate()[i].booleanValue()) {
            			updateColumns.add(meta.getUpdateLookup()[i]);
            		}
            	}
            	data.db.prepareUpdate(meta.getSchemaName(), meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition(), (String[])updateColumns.toArray(new String[]{}));
            }
			
			// lookup the values!
			if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.CheckingRow")+row.toString()); //$NON-NLS-1$
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
					throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getKeyStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				data.keynrs2[i]=row.searchValueIndex(meta.getKeyStream2()[i]);
				if (data.keynrs2[i]<0 &&  // couldn't find field!
				    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed! //$NON-NLS-1$
				   )
				{
					throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getKeyStream2()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.FieldHasDataNumbers",meta.getKeyStream()[i])+data.keynrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// Cache the position of the compare fields in Row row
			//
			data.valuenrs = new int[meta.getUpdateLookup().length];
			for (int i=0;i<meta.getUpdateLookup().length;i++)
			{
				data.valuenrs[i]=row.searchValueIndex(meta.getUpdateStream()[i]);
				if (data.valuenrs[i]<0)  // couldn't find field!
				{
					throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getUpdateStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.FieldHasDataNumbers",meta.getUpdateStream()[i])+data.valuenrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
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
		
		data.db.setValuesLookup(lu);
		
		if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.ValuesSetForLookup")+lu.toString()); //$NON-NLS-1$
		add=data.db.getLookup();  // Got back the complete row!
		linesInput++;
		
		if (add==null) 
		{
			/* nothing was found:
			 *  
			 * INSERT ROW
			 *
			 */
			if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.InsertRow")+row.toString()); //$NON-NLS-1$

			// The values to insert are those in the update section (all fields should be specified)
            // For the others, we have no definite mapping!
            //
            Row ins = new Row();
            for (int i=0;i<data.valuenrs.length;i++)
            {
                ins.addValue(row.getValue(data.valuenrs[i]));
            }
            
            // Set the values on the prepared statement...
			data.db.setValuesInsert(ins);
            
			// Insert the row
            data.db.insertRow();
            
			linesOutput++;
		}
		else
		{
			if (!meta.isUpdateBypassed())
			{
				if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.FoundRowForUpdate")+row.toString()); //$NON-NLS-1$
				
				/* Row was found:
				 *  
				 * UPDATE row or do nothing?
				 *
				 */
				boolean update = false;
				int j = 0;
				for (int i=0;i<data.valuenrs.length;i++)
				{
					if(meta.getUpdate()[i].booleanValue()) {
						Value rowvalue = row.getValue(data.valuenrs[i]);
						lu.addValue(j++, rowvalue);
						Value retvalue = add.getValue(i);
						if (!retvalue.equals(rowvalue)) // Take table value as the driver.
						{
							update=true;
						}
					}
				}
				if (update)
				{
					if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.UpdateRow")+lu.toString()); //$NON-NLS-1$
					data.db.setValuesUpdate(lu);
					data.db.updateRow();
					linesUpdated++;
				}
				else
				{
					linesSkipped++;
				}
			}
			else
			{
				if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.UpdateBypassed")+row.toString()); //$NON-NLS-1$
				linesSkipped++;
			}
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(InsertUpdateMeta)smi;
		data=(InsertUpdateData)sdi;
		
		 boolean sendToErrorRow=false;
	     String errorMessage = null;

		Row r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)          // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		    
		try
		{
			lookupValues(r); // add new values to the row in rowset[0].
			putRow(r);       // copy row to output rowset(s);
			
			if (checkFeedback(linesRead)) logBasic(Messages.getString("InsertUpdate.Log.LineNumber")+linesRead); //$NON-NLS-1$
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
				logError(Messages.getString("InsertUpdate.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			 if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(r, 1, errorMessage, null, "ISU001");
	         }
		}
			
		return true;
	}
	
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(InsertUpdateMeta)smi;
		data=(InsertUpdateData)sdi;
		
		if (super.init(smi, sdi))
		{
		    try
		    {
				data.db=new Database(meta.getDatabaseMeta());
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }
				data.db.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleException ke)
			{
				logError(Messages.getString("InsertUpdate.Log.ErrorOccurredDuringStepInitialize")+ke.getMessage()); //$NON-NLS-1$
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (InsertUpdateMeta)smi;
	    data = (InsertUpdateData)sdi;
	
        try
        {
            if (!data.db.isAutoCommit())
            {
                if (getErrors()==0)
                {
                    data.db.commit();
                }
                else
                {
                    data.db.rollback();
                }
            }
            data.db.closeUpdate();
            data.db.closeInsert();
        }
        catch(KettleDatabaseException e)
        {
            log.logError(toString(), Messages.getString("InsertUpdate.Log.UnableToCommitConnection")+e.toString()); //$NON-NLS-1$
            setErrors(1);
        }

		data.db.disconnect();

	    super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("InsertUpdate.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("InsertUpdate.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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