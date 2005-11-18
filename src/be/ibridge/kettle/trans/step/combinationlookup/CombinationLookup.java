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
 
package be.ibridge.kettle.trans.step.combinationlookup;

import java.util.Hashtable;

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
 * Manages or loooks up information in a junk dimension.<p>
 * <p> 
	 1) Lookup combination field1..n in a dimension<p>
	 2) If this combination exists, return technical key<p>
	 3) If this combination doesn't exist, insert & return technical key<p>
	 4) if replace is Y, remove all key fields from output.<p>
	 <p>
 * @author Matt
 * @since 22-jul-2003
 * 
 */
public class CombinationLookup extends BaseStep implements StepInterface
{
	private CombinationLookupMeta meta;	
	private CombinationLookupData data;
	
	public CombinationLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(CombinationLookupMeta)getStepMeta().getStepMetaInterface();
		data=(CombinationLookupData)stepDataInterface;
	}
	
	private Value lookupInCache(Row row)
	{
		// try to find the row in the cache...
		// It looks using Row.hashcode() & Row.equals()
		Value tk = (Value) data.cache.get(row);
		return tk;
	}
	
	private void storeInCache(Row row, Value tk)
	{
		data.cache.put(row, tk);
	}
	
	private void lookupValues(Row row)
		throws KettleException
	{
		Value val_hash    = null;
		Value val_key     = null;
		
		if (first)
		{
			first=false;
			
			// Lookup values
			data.keynrs    = new int[meta.getKeyField().length];
			for (int i=0;i<meta.getKeyField().length;i++)
			{
				data.keynrs[i]=row.searchValueIndex(meta.getKeyField()[i]);
				if (data.keynrs[i]<0) // couldn't find field!
				{
					throw new KettleStepException("Field ["+meta.getKeyField()[i]+"] couldn't be found!");
				} 
			}
			
			// Sort lookup values in reverse so we can delete from back to front!
			if (meta.replaceFields())
			{
				int x,y;
				int size=meta.getKeyField().length;
				int nr1, nr2;
				
				for (x=0;x<size;x++)
				{
					for (y=0;y<size-1;y++)
					{
						nr1 = data.keynrs[y];
						nr2 = data.keynrs[y+1];
						
						if (nr2>nr1) // reverse sort: swap values...
						{
							int nr_dummy             = data.keynrs[y];
							String key_dummy         = meta.getKeyField()[y];
							String keylookup_dummy   = meta.getKeyLookup()[y];
							data.keynrs[y]           = data.keynrs[y+1];
							meta.getKeyField()[y]    = meta.getKeyField()[y+1];
							meta.getKeyLookup()[y]   = meta.getKeyLookup()[y+1];
							data.keynrs[y+1]         = nr_dummy;
							meta.getKeyField()[y+1]  = key_dummy;
							meta.getKeyLookup()[y+1] = keylookup_dummy;
						}
					}
				}
			}

			data.db.setCombiLookup(meta.getTablename(), meta.getKeyLookup(), 
								   meta.getTechnicalKeyField(), meta.useHash(), 
								   meta.getHashField() );
		}
		
		Row lu = new Row();
		for (int i=0;i<meta.getKeyField().length;i++)
		{
			lu.addValue( row.getValue(data.keynrs[i]) ); // KEYi = ?
		}

		if (meta.useHash())
		{
			val_hash = new Value(meta.getHashField(), (long)lu.hashCode());
			lu.clear(); 
			lu.addValue(val_hash);
		}
		else
		{
			lu.clear(); 
		}
		
		for (int i=0;i<meta.getKeyField().length;i++)
		{
			Value parval = row.getValue(data.keynrs[i]);
			lu.addValue( parval ); // KEYi = ?
			lu.addValue( parval ); // KEYi IS NULL or ? IS NULL
		}
		
		// Before doing the actual lookup in the database, see if it's not in the cache...
		val_key = lookupInCache(lu);
		if (val_key==null)
		{
			data.db.setValuesLookup(lu);
			Row add=data.db.getLookup();
			linesInput++;
			
			if (add==null) // The dimension entry was not found, we need to add it!
			{
				// First try to use an AUTOINCREMENT field
				boolean autoinc=false;
				if (meta.getDatabase().supportsAutoinc() && meta.isUseAutoinc())
				{
					autoinc=true;
					val_key=new Value(meta.getTechnicalKeyField(), 0.0); // value to accept new key...
				}
				else
				// Try to get the value by looking at a SEQUENCE (oracle mostly)
				if (meta.getDatabase().supportsSequences() && meta.getSequenceFrom()!=null && meta.getSequenceFrom().length()>0)
				{
					val_key=data.db.getNextSequenceValue(meta.getSequenceFrom(), meta.getTechnicalKeyField());
					if (val_key!=null) logRowlevel("Found next sequence value: "+val_key.toString());
				}
				else
				// Use our own sequence here...
				{
					// What's the next value for the technical key?
					val_key=new Value(meta.getTechnicalKeyField(), 0.0); // value to accept new key...
					data.db.getNextValue(getTransMeta(), meta.getTablename(), val_key);
				}
	
				data.db.combiInsert( row, meta.getTablename(), meta.getSequenceFrom()==null?null:meta.getTechnicalKeyField(),
			                    autoinc,
			                    val_key,
						        meta.getKeyLookup(),
						        data.keynrs,
						        meta.useHash(),
						        meta.getHashField(),
						        val_hash
							   );
				
				linesOutput++;
				
				log.logRowlevel(toString(), "added dimension entry with key="+val_key);
				
				// Also store it in our Hashtable...
				storeInCache(lu, val_key);
			}
			else
			{
				val_key = add.getValue(0); // Only one value possible here...
				storeInCache(lu, val_key);
			}
		}
		
		// See if we need to replace the fields with the technical key
		if (meta.replaceFields())
		{
			for (int i=0;i<data.keynrs.length;i++)
			{
				row.removeValue(data.keynrs[i]); // safe because reverse sorted on index nr.
			}
		}
		
		// Add the technical key...
		row.addValue( val_key );
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
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
			logError("Because of an error, this step can't continue: "+e.getMessage());
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		
		return true;
	}

	public boolean init(StepMetaInterface sii, StepDataInterface sdi)
	{
		if (super.init(sii, sdi))
		{
			data.cache=new Hashtable();

			data.db=new Database(meta.getDatabase());
			try
			{
				data.db.connect();
				logBasic("Connected to database...");
				data.db.setCommit(meta.getCommitSize());
			
				return true;
			}
			catch(KettleDatabaseException dbe)
			{
				logError("Unable to connect to database: "+dbe.getMessage());
			}
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (CombinationLookupMeta)smi;
	    data = (CombinationLookupData)sdi;
	    
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
			markStop();
		    logSummary();
		}
	}

	public String toString()
	{
		return this.getClass().getName();
	}

}
