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
	
		debug="Start of lookupValues";
			
		if (first)
		{
			debug="first run, initialize";
			first=false;
			
			data.dblup.setLookup(meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition(), meta.getUpdateLookup(), null, null);
			data.dbupd.prepareUpdate(meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition(), meta.getUpdateLookup());
			
			debug="first run, lookup values, field positions, etc.";
			// lookup the values!
			logDetailed("Checking row: "+row.toString());
			data.keynrs  = new int[meta.getKeyStream().length];
			data.keynrs2 = new int[meta.getKeyStream().length];
			for (int i=0;i<meta.getKeyStream().length;i++)
			{
				data.keynrs[i]=row.searchValueIndex(meta.getKeyStream()[i]);
				if (data.keynrs[i]<0 &&  // couldn't find field!
                    !"IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) &&   // No field needed!
				    !"IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i])  // No field needed!
                   )
				{
					throw new KettleStepException("Field ["+meta.getKeyStream()[i]+"] is required and couldn't be found!");
				}
				data.keynrs2[i]=row.searchValueIndex(meta.getKeyStream2()[i]);
				if (data.keynrs2[i]<0 &&  // couldn't find field!
				    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed!
				   )
				{
					throw new KettleStepException("Field ["+meta.getKeyStream2()[i]+"] is required and couldn't be found!");
				}
				
				logDebug("Field ["+meta.getKeyStream()[i]+"] has nr. "+data.keynrs[i]);
			}
			// Cache the position of the compare fields in Row row
			//
			debug="first run, lookup compare fields, positions, etc.";
			data.valuenrs = new int[meta.getUpdateLookup().length];
			for (int i=0;i<meta.getUpdateLookup().length;i++)
			{
				data.valuenrs[i]=row.searchValueIndex(meta.getUpdateStream()[i]);
				if (data.valuenrs[i]<0)  // couldn't find field!
				{
					throw new KettleStepException("Field ["+meta.getUpdateStream()[i]+"] is required and couldn't be found!");
				}
				logDebug("Field ["+meta.getUpdateStream()[i]+"] has nr. "+data.valuenrs[i]);
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
		
		debug="setValues()";
		data.dblup.setValuesLookup(lu);
		
		logDebug("Values set for lookup: "+lu.toString()+", input row: "+row);
		debug="getLookup()";
		add=data.dblup.getLookup();  // Got back the complete row!
		linesInput++;
		
		if (add==null) 
		{
			/* nothing was found: throw error!
			 */
            if (!meta.isErrorIgnored())
            {
                throw new KettleDatabaseException("Entry to update with following key could not be found: "+lu);
            }
            else
            {
                log.logDetailed(toString(), "WARNING: key could not be found for update: "+lu);
                if (meta.getIgnoreFlagField()!=null && meta.getIgnoreFlagField().length()>0) // add flag field!
                {
                    row.addValue(new Value(meta.getIgnoreFlagField(), false));
                }
            }
		}
		else
		{
			logRowlevel("Found row: !"+add.toString());
			/* Row was found:
			 *  
			 * UPDATE row or do nothing?
			 *
			 */
			debug="compare for update";
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
				logRowlevel("Update row with: !"+lu.toString());
				debug="setValuesUpdate()";
				data.dbupd.setValuesUpdate(lu);
				debug="updateRow()";
				data.dbupd.updateRow();
				linesUpdated++;
			}
			else
			{
				linesSkipped++;
			}
            
            if (meta.getIgnoreFlagField()!=null && meta.getIgnoreFlagField().length()>0) // add flag field!
            {
                row.addValue(new Value(meta.getIgnoreFlagField(), false));
            }
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(UpdateMeta)smi;
		data=(UpdateData)sdi;

		Row r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		    
		try
		{
			lookupValues(r); // add new values to the row in rowset[0].
			putRow(r);       // copy row to output rowset(s);
			
			if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesRead);
		}
		catch(KettleException e)
		{
			logError("Error in step, asking everyone to stop because of:"+e.getMessage());
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(UpdateMeta)smi;
		data=(UpdateData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.dblup=new Database(meta.getDatabase());
			data.dbupd=new Database(meta.getDatabase());
			try 
			{
				data.dblup.connect();
				data.dbupd.connect();
				
				logBasic("Connected to database...");
				
				data.dbupd.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleException ke)
			{
				logError("An error occurred, processing will be stopped: "+ke.getMessage());
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
            if (!data.dbupd.isAutoCommit()) data.dbupd.commit();
            data.dbupd.closeUpdate();
        }
        catch(KettleDatabaseException e)
        {
            log.logError(toString(), "Unable to commit Update connection ["+data.dbupd+"] :"+e.toString());
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
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
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
