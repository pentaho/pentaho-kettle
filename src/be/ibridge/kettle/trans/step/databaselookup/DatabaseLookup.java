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
	
	private synchronized void lookupValues(Row row)
		throws KettleException
	{
		Row lu;
		Row add;
		boolean cache_now=false;		
		
		debug = "Start of lookupValues()";
		if (first)
		{
            first=false;
            
			debug = "first row: start";
			if (meta.isCached())
			{
				debug = "first row: cache allocate";
				if (meta.getCacheSize()>0)
				{
					data.look=new Hashtable((int)(meta.getCacheSize()*1.5));
				}
				else
				{
					data.look=new Hashtable();
				}
			}

			debug = "first row: set lookup statement";
			data.db.setLookup(meta.getTablename(), meta.getTableKeyField(), meta.getKeyCondition(), meta.getReturnValueField(), meta.getReturnValueNewName(), meta.getOrderByClause());
			// lookup the values!
			logDetailed("Checking row: "+row.toString());
			data.keynrs = new int[meta.getStreamKeyField1().length];
			data.keynrs2= new int[meta.getStreamKeyField1().length];
			
			debug = "first row: get key fieldnrs";
			for (int i=0;i<meta.getStreamKeyField1().length;i++)
			{
				data.keynrs[i]=row.searchValueIndex(meta.getStreamKeyField1()[i]);
				if (data.keynrs[i]<0 &&  // couldn't find field!
                    !"IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) &&   // No field needed!
				    !"IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i])  // No field needed!
                   )
				{
					throw new KettleStepException("Field ["+meta.getStreamKeyField1()[i]+"] is required and couldn't be found!");
				}
				data.keynrs2[i]=row.searchValueIndex(meta.getStreamKeyField2()[i]);
				if (data.keynrs2[i]<0 &&  // couldn't find field!
				    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed!
				   )
				{
					throw new KettleStepException("Field ["+meta.getStreamKeyField2()[i]+"] is required and couldn't be found!");
				}
				logDebug("Field ["+meta.getStreamKeyField1()[i]+"] has nr. "+data.keynrs[i]);
			}
			
			data.nullif = new Value[meta.getReturnValueField().length];

			debug = "first row: get value fieldnrs";
			for (int i=0;i<meta.getReturnValueField().length;i++)
			{
				data.nullif[i] = new Value(meta.getReturnValueNewName()[i], meta.getReturnValueDefaultType()[i]);
				if (meta.getReturnValueDefault()[i]!=null && meta.getReturnValueDefault()[i].length()>0 )
				{
					data.nullif[i].setValue(meta.getReturnValueDefault()[i]);
					data.nullif[i].setType(meta.getReturnValueDefaultType()[i]);
				}
				else
				{
					data.nullif[i].setNull();
				}
			}
			
			// Determine the types...
			data.keytypes = new int[meta.getTableKeyField().length];
			Row fields = data.db.getTableFields(meta.getTablename());
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
						throw new KettleStepException("Field ["+meta.getTableKeyField()[i]+"] couldn't be found in the table!");
					}
				}
			}
			else
			{
				throw new KettleStepException("Unable to determine the fields of table ["+meta.getTablename()+"]");
			}
		}
		
		lu = new Row();
		debug = "Adding values to lookup row";
		for (int i=0;i<meta.getStreamKeyField1().length;i++)
		{
			if (data.keynrs[i]>=0)
			{
				Value value = row.getValue(data.keynrs[i]);
				// Try to convert type if needed!
				if (value.getType()!=data.keytypes[i]) value.setType(data.keytypes[i]);
				lu.addValue( value );
			}
			if (data.keynrs2[i]>=0)
			{
				Value value = row.getValue(data.keynrs2[i]);
				// Try to convert type if needed!
				if (value.getType()!=data.keytypes[i]) value.setType(data.keytypes[i]);
				lu.addValue( value );
			}
		}
        
		// First, check if we looked up before
		if (meta.isCached()) add=(Row)data.look.get(lu);
		else add=null; 
		
		if (add==null)
		{
            logRowlevel("Added "+meta.getStreamKeyField1().length+" values to lookup row: "+lu);
            
			debug = "setValuesLookup()";
			data.db.setValuesLookup(lu);
			debug = "getLookup()";
			add=data.db.getLookup();
			cache_now=true;
		}
				
		debug = "add null values to result";
		if (add==null) // nothing was found, unknown code: add default values
		{
            logRowlevel("No result found after database lookup! (add defaults)");
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
            logRowlevel("Found result after database lookup: "+add);
        }

		debug = "Store result in cache";
		// Store in cache if we need to!
		if (meta.isCached() && cache_now)
		{
			add.setLogdate();
			data.look.put(lu, add);
			
			// See if we have to limit the cache_size.
			if (meta.getCacheSize()>0 && data.look.size()>meta.getCacheSize())
			{
				 long last_date=-1L;
				 Enumeration elem = data.look.elements();
				 Row smallest=null;
				 while (elem.hasMoreElements())
				 {
				 	Row r=(Row)elem.nextElement();
				 	long time = r.getLogtime();
				 	if (last_date<0 || time<last_date) 
				 	{
				 		last_date=time;
				 		smallest=r;
				 	} 
				 }
				 if (smallest!=null) data.look.remove(smallest);
			}
		} 
	
		debug = "Add values to resulting row...";
		for (int i=0;i<add.size();i++)
		{
			row.addValue( add.getValue(i) );
		}

		debug = "end of lookupValues()";
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

        logRowlevel("Got row from previous step: "+r);

		try
		{
			lookupValues(r); // add new values to the row in rowset[0].
			putRow(r);       // copy row to output rowset(s);
			
            logRowlevel("Wrote row to next step: "+r);
			if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesRead);
		}
		catch(KettleException e)
		{
			logError("Because of an error, this step can't continue: "+e.getMessage());
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
			
		return true;
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
				data.db.connect();
				logBasic("Connected to database...");
				
				return true;
			}
			catch(Exception e)
			{
				logError("An error cause this step to stop: "+e.toString());
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
		logBasic("Starting to run...");
		
		try
		{
			logBasic("Connected to database...");	
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("An error cause this step to stop: "+e.getMessage());
			logError(e.toString());
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
