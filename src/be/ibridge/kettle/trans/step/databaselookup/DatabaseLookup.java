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
 
package be.ibridge.kettle.trans.step.databaselookup;

import java.util.Enumeration;
import java.util.Hashtable;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.TimedRow;
import be.ibridge.kettle.core.database.Database;
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
 * Looks up values in a database using keys from input streams.
 * 
 * @author Matt
 * @since 26-apr-2003
 */
public class DatabaseLookup extends BaseStep implements StepInterface
{
	private DatabaseLookupMeta meta;
	private DatabaseLookupData data;

	public DatabaseLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/**
	 * Performs the lookup based on the meta-data and the input row.
	 * @param row The row to use as lookup data and the row to add the returned lookup fields to
	 * @return true if we can pass the rows to the next steps, false if we can't.
	 * @throws KettleException In case something goes wrong.
	 */
	private synchronized boolean lookupValues(Row row) throws KettleException
	{
		Row lu;
		Row add;
		boolean cache_now=false;		

		if (first)
		{
            first=false;

			if (meta.isCached())
			{
				if (meta.getCacheSize()>0)
				{
					data.look=new Hashtable((int)(meta.getCacheSize()*1.5));
				}
				else
				{
					data.look=new Hashtable();
				}
			}

			data.db.setLookup(meta.getSchemaName(), meta.getTablename(), meta.getTableKeyField(), meta.getKeyCondition(), meta.getReturnValueField(), meta.getReturnValueNewName(), meta.getOrderByClause(), meta.isFailingOnMultipleResults());

			// lookup the values!
			if (log.isDetailed()) logDetailed(Messages.getString("DatabaseLookup.Log.CheckingRow")+row.toString()); //$NON-NLS-1$
			data.keynrs = new int[meta.getStreamKeyField1().length];
			data.keynrs2= new int[meta.getStreamKeyField1().length];

			for (int i=0;i<meta.getStreamKeyField1().length;i++)
			{
				data.keynrs[i]=row.searchValueIndex(meta.getStreamKeyField1()[i]);
				if (data.keynrs[i]<0 &&  // couldn't find field!
                    !"IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) &&   // No field needed! //$NON-NLS-1$
				    !"IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i])  // No field needed! //$NON-NLS-1$
                   )
				{
					throw new KettleStepException(Messages.getString("DatabaseLookup.ERROR0001.FieldRequired1.Exception")+meta.getStreamKeyField1()[i]+Messages.getString("DatabaseLookup.ERROR0001.FieldRequired2.Exception")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				data.keynrs2[i]=row.searchValueIndex(meta.getStreamKeyField2()[i]);
				if (data.keynrs2[i]<0 &&  // couldn't find field!
				    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed! //$NON-NLS-1$
				   )
				{
					throw new KettleStepException(Messages.getString("DatabaseLookup.ERROR0001.FieldRequired3.Exception")+meta.getStreamKeyField2()[i]+Messages.getString("DatabaseLookup.ERROR0001.FieldRequired4.Exception")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (log.isDebug()) logDebug(Messages.getString("DatabaseLookup.Log.FieldHasIndex1")+meta.getStreamKeyField1()[i]+Messages.getString("DatabaseLookup.Log.FieldHasIndex2")+data.keynrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
			}

			data.nullif = new Value[meta.getReturnValueField().length];

			for (int i=0;i<meta.getReturnValueField().length;i++)
			{
				data.nullif[i] = new Value(meta.getReturnValueNewName()[i], meta.getReturnValueDefaultType()[i]);
				if (!Const.isEmpty(meta.getReturnValueDefault()[i]))
				{
                    data.nullif[i].setValue(meta.getReturnValueDefault()[i]);
					data.nullif[i].setType(meta.getReturnValueDefaultType()[i]);
				}
				else
				{
					data.nullif[i].setType(meta.getReturnValueDefaultType()[i]);
					data.nullif[i].setNull();
				}
			}

			// Determine the types...
			data.keytypes = new int[meta.getTableKeyField().length];
            String schemaTable = meta.getDatabaseMeta().getQuotedSchemaTableCombination(meta.getSchemaName(), meta.getTablename());
			Row fields = data.db.getTableFields(schemaTable);
			if (fields!=null)
			{
				// Fill in the types...
				for (int i=0;i<meta.getTableKeyField().length;i++)
				{
					Value key = fields.searchValue(meta.getTableKeyField()[i]);
					if (key!=null)
					{
						data.keytypes[i] = key.getType();
					}
					else
					{
						throw new KettleStepException(Messages.getString("DatabaseLookup.ERROR0001.FieldRequired5.Exception")+meta.getTableKeyField()[i]+Messages.getString("DatabaseLookup.ERROR0001.FieldRequired6.Exception")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			else
			{
				throw new KettleStepException(Messages.getString("DatabaseLookup.ERROR0002.UnableToDetermineFieldsOfTable")+schemaTable+"]"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		lu = new Row();
		for (int i=0;i<meta.getStreamKeyField1().length;i++)
		{
			if (data.keynrs[i]>=0)
			{
				Value value = row.getValue(data.keynrs[i]);
				// Try to convert type if needed in a clone, we don't want to
				// change the type in the original row
				Value clonedValue = value.Clone();

				if (clonedValue.getType()!=data.keytypes[i]) clonedValue.setType(data.keytypes[i]);
				lu.addValue( clonedValue );
			}
			if (data.keynrs2[i]>=0)
			{
				Value value = row.getValue(data.keynrs2[i]);

				// Try to convert type if needed in a clone, we don't want to
				// change the type in the original row
				Value clonedValue = value.Clone();

				if (clonedValue.getType()!=data.keytypes[i]) clonedValue.setType(data.keytypes[i]);
				lu.addValue( clonedValue );
			}
		}

		// First, check if we looked up before
		if (meta.isCached())
        {
            TimedRow timedRow = (TimedRow) data.look.get(lu);
            if (timedRow==null)
            {
                add=null;
            }
            else
            {
                add=timedRow.getRow();
            }
        }
		else add=null; 

		if (add==null)
		{
			if (log.isRowLevel()) logRowlevel(Messages.getString("DatabaseLookup.Log.AddedValuesToLookupRow1")+meta.getStreamKeyField1().length+Messages.getString("DatabaseLookup.Log.AddedValuesToLookupRow2")+lu); //$NON-NLS-1$ //$NON-NLS-2$

			data.db.setValuesLookup(lu);
			add = data.db.getLookup(meta.isFailingOnMultipleResults());
			cache_now=true;
		}

		if (add==null) // nothing was found, unknown code: add default values
		{
			if (meta.isEatingRowOnLookupFailure())
			{
				return false;
			}
			if (getStepMeta().isDoingErrorHandling())
			{
                putError(row, 1L, "No lookup found", null, "DBL001");
                row.setIgnore();
                
                // return false else we would still be processed.
                return false;
			}
			
			if (log.isRowLevel()) logRowlevel(Messages.getString("DatabaseLookup.Log.NoResultsFoundAfterLookup")); //$NON-NLS-1$
			add=new Row();
			for (int i=0;i<meta.getReturnValueField().length;i++)
			{
				if (data.nullif[i]!=null)
				{
					add.addValue(new Value(data.nullif[i]));
				}
				else
				{
					Value v = new Value(meta.getReturnValueNewName()[i], meta.getReturnValueDefaultType()[i]);
					v.setNull();
					add.addValue(v);			
				}
			}
		}
        else
        {
        	if (log.isRowLevel()) logRowlevel(Messages.getString("DatabaseLookup.Log.FoundResultsAfterLookup")+add); //$NON-NLS-1$

        	int types[] = meta.getReturnValueDefaultType();

        	// The assumption here is that the types are in the same order
        	// as the returned lookup row, but since we make the lookup row
        	// that should not be a problem.
        	for (int i=0;i<types.length;i++)
        	{  
        		Value value = add.getValue(i);
        		if ( value != null && types[i] > 0 && 
        			 types[i] !=  value.getType() )
        		{
        			// Set the type to the default return type
        		    value.setType(types[i]);
        		}
        	}        	
        } 

		// Store in cache if we need to!
		if (meta.isCached() && cache_now)
		{
			data.look.put(lu, new TimedRow(add));

			// See if we have to limit the cache_size.
			if (meta.getCacheSize()>0 && data.look.size()>meta.getCacheSize())
			{
				 long last_date=-1L;
				 Enumeration elem = data.look.elements();
				 TimedRow smallest=null;
				 while (elem.hasMoreElements())
				 {
				 	TimedRow r=(TimedRow)elem.nextElement();
				 	long time = r.getLogtime();
				 	if (last_date<0 || time<last_date) 
				 	{
				 		last_date=time;
				 		smallest=r;
				 	} 
				 }
				 if (smallest!=null) {
					 data.look.remove(smallest);
				 }
			}
		} 

		for (int i=0;i<add.size();i++)
		{
			row.addValue( add.getValue(i) );
		}

		return true;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DatabaseLookupMeta)smi;
		data=(DatabaseLookupData)sdi;

		Row r=getRow();       // Get row from input rowset & set row busy!

		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		if (log.isRowLevel()) logRowlevel(Messages.getString("DatabaseLookup.Log.GotRowFromPreviousStep")+r); //$NON-NLS-1$

		try
		{
			if (lookupValues(r))
			{
				// add new values to the row in rowset[0].
				putRow(r);       // copy row to output rowset(s);
				if (log.isRowLevel()) logRowlevel(Messages.getString("DatabaseLookup.Log.WroteRowToNextStep")+r); //$NON-NLS-1$
                if (checkFeedback(linesRead)) logBasic("linenr "+linesRead); //$NON-NLS-1$
			}
		}
		catch(KettleException e)
		{
			logError(Messages.getString("DatabaseLookup.ERROR003.UnexpectedErrorDuringProcessing")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}

		return true;
	}
    
    /** Stop the running query */
    public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(DatabaseLookupMeta)smi;
        data=(DatabaseLookupData)sdi;

        if (data.db!=null) data.db.cancelQuery();
    }


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DatabaseLookupMeta)smi;
		data=(DatabaseLookupData)sdi;

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
                
                data.db.setCommit(100); // we never get a commit, but it just turns off auto-commit.
                                
                logBasic(Messages.getString("DatabaseLookup.Log.ConnectedToDatabase")); //$NON-NLS-1$

				return true;
			}
			catch(Exception e)
			{
				logError(Messages.getString("DatabaseLookup.ERROR0004.UnexpectedErrorDuringInit")+e.toString()); //$NON-NLS-1$
				data.db.disconnect();
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DatabaseLookupMeta)smi;
	    data = (DatabaseLookupData)sdi;

	    data.db.disconnect();

	    super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run()
	{
		logBasic(Messages.getString("DatabaseLookup.Log.StartingToRun")); //$NON-NLS-1$

		try
		{
			logBasic(Messages.getString("DatabaseLookup.Log.ConnectedToDatabase2"));	 //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("DatabaseLookup.ERROR003.UnexpectedErrorDuringProcessing2")+e.getMessage()); //$NON-NLS-1$
			logError(e.toString());
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
