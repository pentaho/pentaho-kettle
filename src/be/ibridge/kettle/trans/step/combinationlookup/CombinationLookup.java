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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.TimedRow;
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
 * Manages or looks up information in a Type 1 or junk dimension.<p>
 * <p>
 	 1) Lookup combination field1..n in a dimension<p>
 	 2) If this combination exists, return technical key<p>
     3) If this combination doesn't exist, insert & return technical key<p>
 	 4) if replace is Y, remove all key fields from output.<p>
	 <p>
 * @author Matt
 * @since 22-jul-2003
 */
public class CombinationLookup extends BaseStep implements StepInterface
{
	private final static int CREATION_METHOD_AUTOINC  = 1;
    private final static int CREATION_METHOD_SEQUENCE = 2;
	private final static int CREATION_METHOD_TABLEMAX = 3;

	private int techKeyCreation;

	private CombinationLookupMeta meta;
	private CombinationLookupData data;

	public CombinationLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);

		meta=(CombinationLookupMeta)getStepMeta().getStepMetaInterface();
		data=(CombinationLookupData)stepDataInterface;
	}

	private void setTechKeyCreation(int method)
	{
		techKeyCreation = method;
	}

	private int getTechKeyCreation()
	{
		return techKeyCreation;
	}

	private void determineTechKeyCreation()
	{
		String keyCreation = meta.getTechKeyCreation();
		if (meta.getDatabase().supportsAutoinc() &&
			CombinationLookupMeta.CREATION_METHOD_AUTOINC.equals(keyCreation) )
		{
		    setTechKeyCreation(CREATION_METHOD_AUTOINC);
		}
		else if (meta.getDatabase().supportsSequences() &&
		  	     CombinationLookupMeta.CREATION_METHOD_SEQUENCE.equals(keyCreation) )
		{
		    setTechKeyCreation(CREATION_METHOD_SEQUENCE);
		}
		else
		{
			setTechKeyCreation(CREATION_METHOD_TABLEMAX);
		}
	}

	private Value lookupInCache(Row row)
	{
		// try to find the row in the cache...
		// It is using TimedRow.hashcode() & TimedRow.equals()
        //
		Value tk = (Value) data.cache.get(new TimedRow(row));
		return tk;
	}

	private void storeInCache(Row row, Value tk)
	{
		if (meta.getCacheSize() > 0)
		{
			// Do cache management if cache size is specified.
			// See if we have to limit the cache_size.
			if ( data.cache.size() > meta.getCacheSize() )
			{
				 long last_date=-1L;
				 Set set = data.cache.keySet();
				 Iterator it = set.iterator();
				 TimedRow smallest=null;
				 while (it.hasNext())
				 {
				 	TimedRow r=(TimedRow)it.next();
				 	long time = r.getLogtime();
				 	if (last_date<0 || time<last_date)
				 	{
				 		last_date=time;
				 		smallest=r;
				 	}
				 }
				 if (smallest!=null) data.cache.remove(smallest);
			}
		}

		data.cache.put(new TimedRow(row), tk);
	}

	private void lookupValues(Row row) throws KettleException
	{
		Value val_hash    = null;
		Value val_key     = null;

		if (first)
		{
			determineTechKeyCreation();

			first=false;

			// Lookup values
			data.keynrs    = new int[meta.getKeyField().length];
			for (int i=0;i<meta.getKeyField().length;i++)
			{
				data.keynrs[i]=row.searchValueIndex(meta.getKeyField()[i]);
				if (data.keynrs[i]<0) // couldn't find field!
				{
					throw new KettleStepException(Messages.getString("CombinationLookup.Exception.FieldNotFound",meta.getKeyField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
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

			data.db.setCombiLookup(meta.getSchemaName(), meta.getTablename(), meta.getKeyLookup(),
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
				switch ( getTechKeyCreation() )
				{
				    case CREATION_METHOD_TABLEMAX:
				    	//  Use our own counter: what's the next value for the technical key?
				        val_key=new Value(meta.getTechnicalKeyField(), 0.0); // value to accept new key...
				        data.db.getNextValue(getTransMeta().getCounters(), meta.getSchemaName(), meta.getTablename(), val_key);
                        break;
				    case CREATION_METHOD_AUTOINC:
				    	autoinc=true;
						val_key=new Value(meta.getTechnicalKeyField(), 0.0); // value to accept new key...
						break;
				    case CREATION_METHOD_SEQUENCE:
						val_key=data.db.getNextSequenceValue(meta.getSchemaName(), meta.getSequenceFrom(), meta.getTechnicalKeyField());
						if (val_key!=null && log.isRowLevel()) logRowlevel(Messages.getString("CombinationLookup.Log.FoundNextSequenceValue")+val_key.toString()); //$NON-NLS-1$
						break;
				}

				String tkFieldName = meta.getTechnicalKeyField();
				if (autoinc) tkFieldName=null;

				data.db.combiInsert( row, meta.getSchemaName(), meta.getTablename(), tkFieldName,
			                    autoinc,
			                    val_key,
						        meta.getKeyLookup(),
						        data.keynrs,
						        meta.useHash(),
						        meta.getHashField(),
						        val_hash
							   );

				linesOutput++;

                if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("CombinationLookup.Log.AddedDimensionEntry")+val_key); //$NON-NLS-1$

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

		 boolean sendToErrorRow=false;
		 String errorMessage = null;

		 
		try
		{
			lookupValues(r); // add new values to the row in rowset[0].
			putRow(r);       // copy row to output rowset(s);

            if (checkFeedback(linesRead)) logBasic(Messages.getString("CombinationLookup.Log.LineNumber")+linesRead); //$NON-NLS-1$
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
				logError(Messages.getString("CombinationLookup.Log.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(r, 1, errorMessage, null, "CLOOKUP001");
			}

		}

		return true;
	}

	public boolean init(StepMetaInterface sii, StepDataInterface sdi)
	{
		if (super.init(sii, sdi))
		{
			if (meta.getCacheSize()>0)
			{
				data.cache=new HashMap((int)(meta.getCacheSize()*1.5));
			}
			else
			{
				data.cache=new HashMap();
			}

			data.db=new Database(meta.getDatabase());
			try
			{
				data.db.connect(getPartitionID());
				logBasic(Messages.getString("CombinationLookup.Log.ConnectedToDB")); //$NON-NLS-1$
				data.db.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleDatabaseException dbe)
			{
				logError(Messages.getString("CombinationLookup.Log.UnableToConnectDB")+dbe.getMessage()); //$NON-NLS-1$
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (CombinationLookupMeta)smi;
	    data = (CombinationLookupData)sdi;

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
        }
        catch(KettleDatabaseException e)
        {
            logError(Messages.getString("CombinationLookup.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }

	    data.db.disconnect();

	    super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	//
	public void run()
	{
		logBasic(Messages.getString("CombinationLookup.Log.StartingToRun")); //$NON-NLS-1$

		try
		{
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("CombinationLookup.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
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