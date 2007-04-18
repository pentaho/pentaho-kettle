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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.hash.ByteArrayHashIndex;
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
	private final static int CREATION_METHOD_AUTOINC  = 1;
    private final static int CREATION_METHOD_SEQUENCE = 2;
	private final static int CREATION_METHOD_TABLEMAX = 3;
	
	private int techKeyCreation;	
	
	private DimensionLookupMeta meta;	
	private DimensionLookupData data;
	
	public DimensionLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
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
		if (meta.getDatabaseMeta().supportsAutoinc() && 
			DimensionLookupMeta.CREATION_METHOD_AUTOINC.equals(keyCreation) )
		{
		    setTechKeyCreation(CREATION_METHOD_AUTOINC);
		}
		else if (meta.getDatabaseMeta().supportsSequences() && 
		  	     DimensionLookupMeta.CREATION_METHOD_SEQUENCE.equals(keyCreation) )
		{
		    setTechKeyCreation(CREATION_METHOD_SEQUENCE);
		}
		else
		{
			setTechKeyCreation(CREATION_METHOD_TABLEMAX);
		}		
	}	
	
	private synchronized void lookupValues(Row row) throws KettleException
	{
		Row lu = new Row();
		Row add;		
		Value technicalKey;
		Value valueVersion;
		Value valueDate    = null;
		Value valueDateFrom = null;
		Value valueDateTo   = null;
		
		if (first)
		{
			first=false;
			determineTechKeyCreation();
			if (getCopy()==0) data.db.checkDimZero(meta.getSchemaName(), meta.getTableName(), meta.getKeyField(), meta.getVersionField(), meta.isAutoIncrement());
			
			data.db.setDimLookup(meta.getSchemaName(),
                                 meta.getTableName(), 
								 meta.getKeyLookup(), 
				                 meta.getKeyField(), 
				                 meta.getVersionField(), 
				                 meta.getFieldLookup(), 
                                 meta.getFieldStream(),
				                 meta.getDateFrom(), 
				                 meta.getDateTo(),
                                 meta.getCacheSize()>=0
				                );
			
			// Lookup values
			data.keynrs = new int[meta.getKeyStream().length];
			for (int i=0;i<meta.getKeyStream().length;i++)
			{
				//logDetailed("Lookup values key["+i+"] --> "+key[i]+", row==null?"+(row==null));
				data.keynrs[i]=row.searchValueIndex(meta.getKeyStream()[i]);
				if (data.keynrs[i]<0) // couldn't find field!
				{
					throw new KettleStepException(Messages.getString("DimensionLookup.Exception.KeyFieldNotFound",meta.getKeyStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

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

			data.notFoundTk = new Value(meta.getKeyField(), (long)meta.getDatabaseMeta().getNotFoundTK(meta.isAutoIncrement()));
			if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0) data.notFoundTk.setName(meta.getKeyRename());

			if (meta.getDateField()!=null && data.datefieldnr>=0)
			{
				data.valueDateNow = row.getValue(data.datefieldnr);
			}
			else
			{
				Calendar cal=Calendar.getInstance();
				data.valueDateNow = new Value("MIN", new Date(cal.getTimeInMillis())); // System date... //$NON-NLS-1$
			}
            
            if (meta.getCacheSize()>=0)
            {
                data.keyMeta = new Row();
                for (int i=0;i<data.keynrs.length;i++)
                {
                    Value key = new Value(row.getValue(data.keynrs[i]));
                    data.keyMeta.addValue(key.Clone());
                }
                
                data.cache = new ByteArrayHashIndex(meta.getCacheSize()>0 ? meta.getCacheSize() : 5000, data.keyMeta);
            }
		}

		if (meta.getDateField()!=null && data.datefieldnr>=0)
		{
			data.valueDateNow = row.getValue(data.datefieldnr);
		}
		
		for (int i=0;i<meta.getKeyStream().length;i++)
		{
			try
			{
				lu.addValue( row.getValue(data.keynrs[i]) );
			}
			catch(Exception e)
			{
				throw new KettleStepException(Messages.getString("DimensionLookup.Exception.ErrorDetectedInGettingKey",i+"",data.keynrs[i]+"/"+row.size(),row.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		if (data.datefieldnr>=0)	valueDate = row.getValue(data.datefieldnr);
		else valueDate = data.valueDateNow;
		
		if (log.isDebug()) logDebug(Messages.getString("DimensionLookup.Log.LookupRow")+lu.toString()+" valueDate="+valueDate.toString()); //$NON-NLS-1$ //$NON-NLS-2$
		
        // Do the lookup and see if we can find anything in the database.
        // But before that, let's see if we can find anything in the cache
        //
        add=null;
        if (meta.getCacheSize()>=0)
        {
            add=getFromCache(lu, valueDate);
        }
        
        if (add==null)
        {
            data.db.setDimValues(lu, valueDate );
            add=data.db.getLookup();
            linesInput++;
            
            if (add!=null && meta.getCacheSize()>=0)
            {
                addToCache(lu, add);
            }
        }
		
		/* Handle "update = false" first for performance reasons
		 */
		if (!meta.isUpdate())
		{
			if (add==null)
			{
				add=new Row();
				add.addValue(data.notFoundTk);
				Value v;
				for (int i=0;i<meta.getFieldStream().length;i++)
				{
					if (meta.getFieldStream()[i]!=null)
					{
						if (meta.getFieldStream()[i]!=null) // Rename the field?
							  v=new Value(meta.getFieldStream()[i], meta.getFieldUpdate()[i]);
						else  v=new Value(meta.getFieldLookup()[i], meta.getFieldUpdate()[i]); // Nope, take the default name
						v.setNull();
						add.addValue(v);
					}
				}
                if (meta.getCacheSize()>=0) // need -oo to +oo as well...
                {
                    add.addValue(data.min_date.Clone());
                    add.addValue(data.min_date.Clone());
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
				if (log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.NoDimensionEntryFound")+lu+")"); //$NON-NLS-1$ //$NON-NLS-2$
				//logDetailed("Entry not found: add value!");
				// Date range: ]-oo,+oo[ 
				valueDateFrom = new Value("MIN", meta.getMinDate()); //$NON-NLS-1$
				valueDateTo   = new Value("MAX", meta.getMaxDate()); //$NON-NLS-1$
				valueVersion = new Value(meta.getVersionField(), 1L);     // Versions start at 1.
				
				// get a new value from the sequence choosen.
				boolean autoinc=false;
				technicalKey = null;
				switch ( getTechKeyCreation() )
				{
				    case CREATION_METHOD_TABLEMAX:
						// What's the next value for the technical key?
						technicalKey=new Value(meta.getKeyField(), 0L); // value to accept new key...
						data.db.getNextValue(getTransMeta().getCounters(), meta.getSchemaName(), meta.getTableName(), technicalKey);
                        break;
				    case CREATION_METHOD_AUTOINC:
						autoinc=true;
						technicalKey=new Value(meta.getKeyField(), 0L); // value to accept new key...
						break;
				    case CREATION_METHOD_SEQUENCE:						
						technicalKey=data.db.getNextSequenceValue(meta.getSchemaName(), meta.getSequenceName(), meta.getKeyField());
						if (technicalKey!=null && log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.FoundNextSequence")+technicalKey.toString()); //$NON-NLS-1$
						break;					
				}	               

				/*
				 *   INSERT INTO table(version, datefrom, dateto, fieldlookup)
				 *   VALUES(valueVersion, valueDateFrom, valueDateTo, row.fieldnrs)
				 *   ;
				 */
				
				data.db.dimInsert(row, meta.getSchemaName(), meta.getTableName(), 
								  true, 
								  autoinc?null:meta.getKeyField(),   // In case of auto increment, don't insert the key, let the database do it.
								  autoinc, 
								  technicalKey, 
								  meta.getVersionField(), valueVersion, 
								  meta.getDateFrom(), valueDateFrom, 
								  meta.getDateTo(), valueDateTo, 
								  meta.getFieldLookup(), data.fieldnrs,
								  meta.getKeyStream(), 
								  meta.getKeyLookup(), data.keynrs
								);
								
				linesOutput++;
				add=new Row();
				if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0) technicalKey.setName(meta.getKeyRename());
				add.addValue(technicalKey);
                
                // See if we need to store this record in the cache as well...
                if (meta.getCacheSize()>=0)
                {
                    Row values = getCacheValues(row, technicalKey, valueVersion, valueDateFrom, valueDateTo);
                    
                    // put it in the cache...
                    addToCache(lu, values);
                }
                
				if (log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.AddedDimensionEntry")+add.toString()); //$NON-NLS-1$
			}
			else  // The entry was found: do we need to insert, update or both?
			{
				if (log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.DimensionEntryFound")+add); //$NON-NLS-1$
                
				// What's the key?  The first value of the return row
				technicalKey     = add.getValue(0);
				valueVersion = add.getValue(1); 
                
				// Date range: ]-oo,+oo[ 
				valueDateFrom = new Value("MIN", meta.getMinDate()); //$NON-NLS-1$
				valueDateTo   = new Value("MAX", meta.getMaxDate()); //$NON-NLS-1$

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
					  
					  logRowlevel(Messages.getString("DimensionLookup.Log.ComparingValues",""+v1,""+v2,String.valueOf(cmp),String.valueOf(identical),String.valueOf(insert),String.valueOf(punch))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				}
				
				if (!insert)  // Just an update of row at key = valueKey
				{
					if (!identical)
					{
						if (log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.UpdateRowWithValues")+row); //$NON-NLS-1$
						/*
						 * UPDATE d_customer
						 * SET    fieldlookup[] = row.getValue(fieldnrs)
						 * WHERE  returnkey = dimkey
						 */
						data.db.dimUpdate(row, meta.getSchemaName(), meta.getTableName(), meta.getFieldLookup(), data.fieldnrs, meta.getKeyField(), technicalKey);
						linesUpdated++;
                        
                        // We need to capture this change in the cache as well...
                        if (meta.getCacheSize()>=0)
                        {
                            Row values = getCacheValues(row, technicalKey, valueVersion, valueDateFrom, valueDateTo);
                            addToCache(lu, values);
                        }
					}
					else
					{
						if (log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.SkipLine")); //$NON-NLS-1$
						// Don't do anything, everything is file in de dimension.
						linesSkipped++;
					}
				}
				else
				{
					if (log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.InsertNewVersion")+technicalKey.toString()); //$NON-NLS-1$
					
					valueDateFrom = data.valueDateNow;
					valueDateTo   = new Value("MAX", meta.getMaxDate()); //$NON-NLS-1$

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
						technicalKey=data.db.getNextSequenceValue(meta.getSchemaName(), meta.getSequenceName(), meta.getKeyField());
						if (technicalKey!=null && log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.FoundNextSequence2")+technicalKey.toString()); //$NON-NLS-1$
					}
					else
					// Use our own sequence here...
					{
						// What's the next value for the technical key?
						technicalKey=new Value(meta.getKeyField(), 0L); // value to accept new key...
						data.db.getNextValue(getTransMeta().getCounters(), meta.getSchemaName(), meta.getTableName(), technicalKey);
					}

					data.db.dimInsert( row, meta.getSchemaName(), meta.getTableName(), 
									   false,
									   meta.getKeyField(), autoinc, technicalKey, 
									   meta.getVersionField(), valueVersion, 
									   meta.getDateFrom(), valueDateFrom, 
									   meta.getDateTo(), valueDateTo, 
									   meta.getFieldLookup(), data.fieldnrs, 
									   meta.getKeyStream(), meta.getKeyLookup(), data.keynrs
						         );
					linesOutput++;
                    
                    // We need to capture this change in the cache as well...
                    if (meta.getCacheSize()>=0)
                    {
                        Row values = getCacheValues(row, technicalKey, valueVersion, valueDateFrom, valueDateTo);
                        addToCache(lu, values);
                    }
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
					data.db.dimPunchThrough( row, meta.getSchemaName(), meta.getTableName(), meta.getFieldUpdate(), 
											 meta.getFieldLookup(), data.fieldnrs, 
											 meta.getKeyStream(), meta.getKeyLookup(), data.keynrs
									 	);
					linesUpdated++;
					 
				}
				
				add=new Row();
				if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0) technicalKey.setName(meta.getKeyRename());
				add.addValue(technicalKey);
				if (log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.TechnicalKey")+technicalKey); //$NON-NLS-1$
			}
		}
		
		if (log.isRowLevel()) logRowlevel(Messages.getString("DimensionLookup.Log.AddValuesToRow")+add); //$NON-NLS-1$
        
        int size = add.size();
        if (meta.getCacheSize()>=0 && !meta.isUpdate()) // don't return date from-to fields.
        {
            size-=2; 
        }
		for (int i=0;i<size;i++)
		{
			row.addValue( add.getValue(i) );
		}

		//
		// Finaly, check the date range!
		Value date;
		if (data.datefieldnr>=0) date = row.getValue(data.datefieldnr);
		else				date = new Value("date", new Date()); // system date //$NON-NLS-1$
		
		if (data.min_date.compare(date)>0) data.min_date.setValue( date.getDate() ); 
		if (data.max_date.compare(date)<0) data.max_date.setValue( date.getDate() ); 
	}
	
    /**
     * Keys:
     *   - natural key fields
     * Values:
     *   - Technical key
     *   - lookup fields / extra fields (allows us to compare or retrieve)
     *   - Date_from
     *   - Date_to
     *   
     * @param row The input row
     * @param technicalKey the technical key value
     * @param valueDateFrom the start of valid date range
     * @param valueDateTo the end of the valid date range
     * @return the values to store in the cache as a row.
     */
    private Row getCacheValues(Row row, Value technicalKey, Value valueVersion, Value valueDateFrom, Value valueDateTo)
    {
        Row values = new Row();
        values.addValue(technicalKey);
        values.addValue(valueVersion);
        for (int i=0;i<data.fieldnrs.length;i++)
        {
            Value v = row.getValue(data.fieldnrs[i]);
            values.addValue(v);
        }
        values.addValue(valueDateFrom);
        values.addValue(valueDateTo);
        
        return values;
    }

    /**
     * Adds a row to the cache
     * In case we are doing updates, we need to store the complete rows from the database.
     * These are the values we need to store
     * 
     * Key:
     *   - natural key fields
     * Value:
     *   - Technical key
     *   - lookup fields / extra fields (allows us to compare or retrieve)
     *   - Date_from
     *   - Date_to
     * 
     * @param keyValues
     * @param returnValues
     */
	private void addToCache(Row keyValues, Row returnValues)
    {
        if (data.valueMeta==null)
        {
            data.valueMeta = returnValues.Clone();
        }
        
        // store it in the cache if needed.
        data.cache.putAgain(Row.extractData(keyValues), Row.extractData(returnValues));
        
        // check if the size is not too big...
        // Allow for a buffer overrun of 20% and then remove those 20% in one go.
        // Just to keep performance in track.
        //
        int tenPercent = meta.getCacheSize()/10;
        if (meta.getCacheSize()>0 && data.cache.size()>meta.getCacheSize()+tenPercent)
        {
            // Which cache entries do we delete here?
            // We delete those with the lowest technical key...
            // Those would arguably be the "oldest" dimension entries.
            // Oh well... Nothing is going to be perfect here...
            // 
            // Getting the lowest 20% requires some kind of sorting algorithm and I'm not sure we want to do that.
            // Sorting is slow and even in the best case situation we need to do 2 passes over the cache entries...
            //
            // Perhaps we should get 20% random values and delete everything below the lowest but one TK.
            //
            List keys = data.cache.getKeys();
            int sizeBefore = keys.size();
            List samples = new ArrayList();
            
            // Take 10 sample technical keys....
            int stepsize=keys.size()/5;
            if (stepsize<1) stepsize=1; //make shure we have no endless loop
            for (int i=0;i<keys.size();i+=stepsize)
            {
                byte[] key = (byte[]) keys.get(i);
                byte[] value = data.cache.get(key);
                if (value!=null)
                {
                    Row values = Row.getRow(value, data.valueMeta);
                    Long tk = new Long(values.getValue(0).getInteger());
                    samples.add(tk);
                }
            }
            // Sort these 5 elements...
            Collections.sort(samples);
            
            // What is the smallest?
            // Take the second, not the fist in the list, otherwise we would be removing a single entry = not good.
            data.smallestCacheKey = ((Long) samples.get(1)).longValue();
            
            // Remove anything in the cache <= smallest.
            // This makes it almost single pass...
            // This algorithm is not 100% correct, but I guess it beats sorting the whole cache all the time.
            //
            for (int i=0;i<keys.size();i++)
            {
                byte[] key = (byte[]) keys.get(i);
                byte[] value = data.cache.get(key);
                if (value!=null)
                {
                    Row values = Row.getRow(value, data.valueMeta);
                    long tk = new Long(values.getValue(0).getInteger()).longValue();
                    if (tk<=data.smallestCacheKey)
                    {
                        data.cache.remove(key); // this one has to go.
                    }
                }
            }
            
            int sizeAfter = data.cache.size();
            logDetailed("Reduced the lookup cache from "+sizeBefore+" to "+sizeAfter+" rows.");
        }
        
        if (log.isRowLevel()) logRowlevel("Cache store: key="+keyValues+"    values="+returnValues);
    }

    private Row getFromCache(Row keyValues, Value dateValue)
    {
        byte[] value = data.cache.get(Row.extractData(keyValues));
        if (value!=null) 
        {
            Row row = Row.getRow(value, data.valueMeta);
            
            // See if the dateValue is between the from and to date ranges...
            // The last 2 values are from and to
            long time = dateValue.getInteger();
            long from = row.getValue(row.size()-2).getInteger(); 
            long to   = row.getValue(row.size()-1).getInteger(); 
            if (time>=from && time<to) // sanity check to see if we have the right version
            {
                if (log.isRowLevel()) logRowlevel("Cache hit: key="+keyValues+"  values="+row);
                return row;
            }
        }
        return null;
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
			
            if (checkFeedback(linesRead)) logBasic(Messages.getString("DimensionLookup.Log.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			logError(Messages.getString("DimensionLookup.Log.StepCanNotContinueForErrors", e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e)); //$NON-NLS-1$ //$NON-NLS-2$
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
			data.min_date = new Value("start_date", meta.getMinDate()); //$NON-NLS-1$
			data.max_date = new Value("end_date",   meta.getMaxDate()); //$NON-NLS-1$

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
				
				logBasic(Messages.getString("DimensionLookup.Log.ConnectedToDB")); //$NON-NLS-1$
				data.db.setCommit(meta.getCommitSize());
				
				return true;
			}
			catch(KettleException ke)
			{
				logError(Messages.getString("DimensionLookup.Log.ErrorOccurredInProcessing")+ke.getMessage()); //$NON-NLS-1$
			}
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DimensionLookupMeta)smi;
	    data = (DimensionLookupData)sdi;
	    
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
            logError(Messages.getString("DimensionLookup.Log.ErrorOccurredInProcessing")+e.getMessage()); //$NON-NLS-1$
        }
        
	    data.db.disconnect();
	    
	    super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("DimensionLookup.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("DimensionLookup.Log.UnexpectedError", e.toString())); //$NON-NLS-1$ //$NON-NLS-2$
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
}
