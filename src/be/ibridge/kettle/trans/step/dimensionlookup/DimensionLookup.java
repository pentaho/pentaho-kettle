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

package be.ibridge.kettle.trans.step.dimensionlookup;

import java.util.Calendar;
import java.util.Date;

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
 * Manages a slowly chaning dimension (lookup or update)
 * 
 * @author Matt
 * @since 14-may-2003
 *
 */

public class DimensionLookup extends BaseStep implements StepInterface
{
	private DimensionLookupMeta meta;	
	private DimensionLookupData data;
	
	public DimensionLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void lookupValues(Row row)
		throws KettleException
	{
		Row lu = new Row();
		Row add;		
		Value technicalKey;
		Value val_version;
		Value val_date    = null;
		Value val_datfrom = null;
		Value val_datto   = null;
		
		debug = "Start of lookupValues()";
		
		if (first)
		{
			debug = "init of lookupValues()";
			first=false;
			if (getCopy()==0) data.db.checkDimZero(meta.getTableName(), meta.getKeyField(), meta.getVersionField(), meta.isAutoIncrement());
			
			debug = "first: setDimLookup()";
			data.db.setDimLookup(meta.getTableName(), 
								 meta.getKeyLookup(), 
				                 meta.getKeyField(), 
				                 meta.getVersionField(), 
				                 meta.getFieldLookup(), 
                                 meta.getFieldStream(),
				                 meta.getDateFrom(), 
				                 meta.getDateTo()
				                );
			
			// Lookup values
			debug = "first: lookup key nrs";
			data.keynrs = new int[meta.getKeyStream().length];
			for (int i=0;i<meta.getKeyStream().length;i++)
			{
				//logDetailed("Lookup values key["+i+"] --> "+key[i]+", row==null?"+(row==null));
				data.keynrs[i]=row.searchValueIndex(meta.getKeyStream()[i]);
				if (data.keynrs[i]<0) // couldn't find field!
				{
					throw new KettleStepException("Key field ["+meta.getKeyStream()[i]+"] couldn't be found!");
				} 
			}

			debug = "first: lookup value nrs";
			// Return values
			if (meta.isUpdate())
			{
				data.fieldnrs = new int[meta.getFieldStream().length];
				for (int i=0;meta.getFieldStream()!=null && i<meta.getFieldStream().length;i++)
				{
					data.fieldnrs[i]=row.searchValueIndex(meta.getFieldStream()[i]);
				}
			}

			if (meta.getDateField()!=null && meta.getDateField().length()>0)
			{ 
				data.datefieldnr = row.searchValueIndex(meta.getDateField());
			}
			else 
			{
				data.datefieldnr=-1;
			} 

			debug = "first: notfound field";
			
			meta.setNotFound( new Value(meta.getKeyField(), (double)meta.getDatabaseMeta().getNotFoundTK(meta.isAutoIncrement())) );
			if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0) meta.getNotFound().setName(meta.getKeyRename());

			if (meta.getDateField()!=null && data.datefieldnr>=0)
			{
				data.val_datnow = row.getValue(data.datefieldnr);
			}
			else
			{
				Calendar cal=Calendar.getInstance();
				data.val_datnow = new Value("MIN", new Date(cal.getTimeInMillis())); // System date...
			}

			debug = "first: end";
		}

		if (meta.getDateField()!=null && data.datefieldnr>=0)
		{
			data.val_datnow = row.getValue(data.datefieldnr);
		}
		
		for (int i=0;i<meta.getKeyStream().length;i++)
		{
			try
			{
				lu.addValue( row.getValue(data.keynrs[i]) );
			}
			catch(Exception e)
			{
				throw new KettleStepException("Error detected getting key #"+i+", pos="+data.keynrs[i]+"/"+row.size()+" from row: "+row.toString());
			}
		}
		if (data.datefieldnr>=0)	val_date = row.getValue(data.datefieldnr);
		else val_date = data.val_datnow;
		
		logDebug("lookup row : "+lu.toString()+" val_date="+val_date.toString());
		
		debug = "setDimValues()";
		data.db.setDimValues(lu, val_date );
		debug = "getLookup()";
		add=data.db.getLookup();
		
		/* Handle "update = false" first for performance reasons
		 */
		if (!meta.isUpdate())
		{
			debug = "lookup";
			if (add==null)
			{
				debug = "lookup: nothing found";
				add=new Row();
				add.addValue(meta.getNotFound());
				Value v;
				for (int i=0;i<meta.getFieldStream().length;i++)
				{
					debug = "lookup: fieldstream #"+i;
					if (meta.getFieldStream()[i]!=null)
					{
						debug = "lookup: new value #"+i;
						if (meta.getFieldStream()[i]!=null) // Rename the field?
							  v=new Value(meta.getFieldStream()[i], meta.getFieldUpdate()[i]);
						else  v=new Value(meta.getFieldLookup()[i], meta.getFieldUpdate()[i]); // Nope, take the default name
						v.setNull();
						add.addValue(v);
					}
				}
			}
			else
			{
				// We found the return values in row "add".
				// Throw away the version nr...
				add.removeValue(1);
				
				// Rename the key field if needed.  Do it directly in the row...
				if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0) add.getValue(0).setName(meta.getKeyRename());
			}
		}
		else  // Insert - update algorithm for slowly changing dimensions
		{
			if (add==null) // The dimension entry was not found, we need to add it!
			{
				debug = "insert";
				logRowlevel("No dimension entry found: INSERT (lookup= "+lu+")");
				//logDetailed("Entry not found: add value!");
				// Date range: ]-oo,+oo[ 
				val_datfrom = new Value("MIN", meta.getMinDate());
				val_datto   = new Value("MAX", meta.getMaxDate());
				val_version = new Value(meta.getVersionField(), 1L);     // Versions start at 1.
				
				// get a new value from the sequence choosen.
				boolean autoinc=false;
                
                // First try to use an AUTOINCREMENT field
				if (meta.getDatabaseMeta().supportsAutoinc() && meta.isAutoIncrement())
				{
					autoinc=true;
					technicalKey=new Value(meta.getKeyField(), 0L); // value to accept new key...
				}
				else
				// Try to get the value by looking at a SEQUENCE (oracle mostly)
				if (meta.getDatabaseMeta().supportsSequences() && meta.getSequenceName()!=null && meta.getSequenceName().length()>0)
				{
					technicalKey=data.db.getNextSequenceValue(meta.getSequenceName(), meta.getKeyField());
					if (technicalKey!=null) logRowlevel("Found next sequence value: "+technicalKey.toString());
				}
				else
				// Use our own sequence here...
				{
					// What's the next value for the technical key?
					technicalKey=new Value(meta.getKeyField(), 0L); // value to accept new key...
					data.db.getNextValue(getTransMeta(), meta.getTableName(), technicalKey);
				}

				/*
				 *   INSERT INTO table(version, datefrom, dateto, fieldlookup)
				 *   VALUES(val_version, val_datfrom, val_datto, row.fieldnrs)
				 *   ;
				 */
				
				debug = "insert row";
				data.db.dimInsert(row, meta.getTableName(), 
								  true, 
								  autoinc?null:meta.getKeyField(),   // In case of auto increment, don't insert the key, let the database do it.
								  autoinc, 
								  technicalKey, 
								  meta.getVersionField(), val_version, 
								  meta.getDateFrom(), val_datfrom, 
								  meta.getDateTo(), val_datto, 
								  meta.getFieldLookup(), data.fieldnrs,
								  meta.getKeyStream(), 
								  meta.getKeyLookup(), data.keynrs
								);
								
				linesOutput++;
				add=new Row();
				if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0) technicalKey.setName(meta.getKeyRename());
				add.addValue(technicalKey);
				logRowlevel("added dimension entry with key="+add.toString());
			}
			else  // The entry was found: do we need to insert, update or both?
			{
				logRowlevel("Dimension entry found : "+add);
				debug = "update";
                
				// What's the key?  The first value of the return row
				technicalKey     = add.getValue(0);
				val_version = add.getValue(1); 
                
				// Date range: ]-oo,+oo[ 
				val_datfrom = new Value("MIN", meta.getMinDate());
				val_datto   = new Value("MAX", meta.getMaxDate());

				// The other values, we compare with
				int cmp;
				
				// If everything is the same: don't do anything
				// If one of the fields is different: insert or update
				// If all changed fields have update = Y, update
				// If one of the changed fields has update = N, insert

				boolean insert=false;
				boolean identical=true;
				boolean punch=false;
				Value v1, v2;
				
				for (int i=0;i<meta.getFieldStream().length;i++)
				{
					v1  = row.getValue(data.fieldnrs[i]);
					v2  = add.getValue(i+2);
					cmp = v1.compare(v2);
					  
					  // Not the same and update = 'N' --> insert
					  if (cmp!=0) identical=false;
                      
                      // Field flagged for insert: insert
					  if (cmp!=0 && meta.getFieldUpdate()[i]==DimensionLookupMeta.TYPE_UPDATE_DIM_INSERT)
					  { 
					  	insert=true;
					  }
                      
                      // Field flagged for punchthrough
					  if (cmp!=0 && meta.getFieldUpdate()[i]==DimensionLookupMeta.TYPE_UPDATE_DIM_PUNCHTHROUGH) 
                      {
                            punch=true;
                      }
					  
					  logRowlevel("Comparing "+v1+" and "+v2+" (cmp="+cmp+") --> identical="+identical+", insert="+insert+", punch="+punch);
				}
				
				if (!insert)  // Just an update of row at key = val_key
				{
					if (!identical)
					{
						logRowlevel("UPDATE row with values: "+row);
						/*
						 * UPDATE d_customer
						 * SET    fieldlookup[] = row.getValue(fieldnrs)
						 * WHERE  returnkey = dimkey
						 */
						debug = "update row";
						data.db.dimUpdate(row, meta.getTableName(), meta.getFieldLookup(), data.fieldnrs, meta.getKeyField(), technicalKey);
						linesUpdated++;
					}
					else
					{
						logRowlevel("SKIP line: identical!");
						// Don't do anything, everything is file in de dimension.
						linesSkipped++;
					}
				}
				else
				{
					logRowlevel("INSERT new version: val_key="+technicalKey.toString());
					
					val_datfrom = data.val_datnow;
					val_datto   = new Value("MAX", meta.getMaxDate());

					boolean autoinc=false;					
					// First try to use an AUTOINCREMENT field
					if (meta.getDatabaseMeta().supportsAutoinc() && meta.isAutoIncrement())
					{
						autoinc=true;
						technicalKey=new Value(meta.getKeyField(), 0.0); // value to accept new key...
					}
					else
					// Try to get the value by looking at a SEQUENCE (oracle mostly)
					if (meta.getDatabaseMeta().supportsSequences() && meta.getSequenceName()!=null && meta.getSequenceName().length()>0)
					{
						technicalKey=data.db.getNextSequenceValue(meta.getSequenceName(), meta.getKeyField());
						if (technicalKey!=null) logRowlevel("Found next sequence value: "+technicalKey.toString());
					}
					else
					// Use our own sequence here...
					{
						// What's the next value for the technical key?
						technicalKey=new Value(meta.getKeyField(), 0L); // value to accept new key...
						data.db.getNextValue(getTransMeta(), meta.getTableName(), technicalKey);
					}

					data.db.dimInsert( row, meta.getTableName(), 
									   false,
									   meta.getKeyField(), autoinc, technicalKey, 
									   meta.getVersionField(), val_version, 
									   meta.getDateFrom(), val_datfrom, 
									   meta.getDateTo(), val_datto, 
									   meta.getFieldLookup(), data.fieldnrs, 
									   meta.getKeyStream(), meta.getKeyLookup(), data.keynrs
						         );
					linesOutput++;
				}
				if (punch) // On of the fields we have to punch through has changed!
				{
					/*
					 * This means we have to update all versions:
					 * 
					 * UPDATE dim SET punchf1 = val1, punchf2 = val2, ...
					 * WHERE  fieldlookup[] = ?
					 * ;
					 * 
					 * --> update ALL versions in the dimension table.
					 */
					data.db.dimPunchThrough( row, meta.getTableName(), meta.getFieldUpdate(), 
											 meta.getFieldLookup(), data.fieldnrs, 
											 meta.getKeyStream(), meta.getKeyLookup(), data.keynrs
									 	);
					linesUpdated++;
					 
				}
				
				add=new Row();
				if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0) technicalKey.setName(meta.getKeyRename());
				add.addValue(technicalKey);
                logRowlevel("Technical key = "+technicalKey);
			}
		}
		
		debug = "add values to row";
        logRowlevel("Values to add to row: "+add);
		for (int i=0;i<add.size();i++)
		{
			row.addValue( add.getValue(i) );
		}

		debug = "get date from date field, datefieldnr="+data.datefieldnr;
		//
		// Finaly, check the date range!
		Value date;
		if (data.datefieldnr>=0) date = row.getValue(data.datefieldnr);
		else				date = new Value("date", new Date()); // system date
		
		debug = "check min date (min_date==null? "+(data.min_date==null)+", date==null? "+(date==null)+"), datefieldnr="+data.datefieldnr;
		if (data.min_date.compare(date)>0) data.min_date.setValue( date.getDate() ); 
		debug = "check max date";
		if (data.max_date.compare(date)<0) data.max_date.setValue( date.getDate() ); 
	
		debug = "end of lookupValues()";
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DimensionLookupMeta)smi;
		data=(DimensionLookupData)sdi;

		Row r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();  // signal end to receiver(s)
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
		meta=(DimensionLookupMeta)smi;
		data=(DimensionLookupData)sdi;

		if (super.init(smi, sdi))
		{
			data.min_date = new Value("start_date", meta.getMinDate());
			data.max_date = new Value("end_date",   meta.getMaxDate());

			data.db=new Database(meta.getDatabaseMeta());
			try
			{
				data.db.connect();
				
				logBasic("Connected to database...");
				data.db.setCommit(meta.getCommitSize());
				
				return true;
			}
			catch(KettleException ke)
			{
				logError("An error occurred, processing will be stopped: "+ke.getMessage());
			}
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DimensionLookupMeta)smi;
	    data = (DimensionLookupData)sdi;
	    
	    data.db.disconnect();
	    
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
}
