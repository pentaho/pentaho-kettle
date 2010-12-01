 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.databaselookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.TimedRow;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Looks up values in a database using keys from input streams.
 * 
 * @author Matt
 * @since 26-apr-2003
 */
public class DatabaseLookup extends BaseStep implements StepInterface
{
	private static Class<?> PKG = DatabaseLookupMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DatabaseLookupMeta meta;
	private DatabaseLookupData data;

	public DatabaseLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/**
	 * Performs the lookup based on the meta-data and the input row.
	 * @param row The row to use as lookup data and the row to add the returned lookup fields to
	 * @return the resulting row after the lookup values where added
	 * @throws KettleException In case something goes wrong.
	 */
	private synchronized Object[] lookupValues(RowMetaInterface inputRowMeta, Object[] row) throws KettleException
	{
		Object[] outputRow = RowDataUtil.resizeArray(row, data.outputRowMeta.size());
                
        Object[] lookupRow = new Object[data.lookupMeta.size()];
        int lookupIndex=0;
        
        for (int i=0;i<meta.getStreamKeyField1().length;i++)
		{
			if (data.keynrs[i]>=0)
			{
                ValueMetaInterface input = inputRowMeta.getValueMeta(data.keynrs[i]);
				ValueMetaInterface value = data.lookupMeta.getValueMeta(lookupIndex);
                lookupRow[lookupIndex] = row[data.keynrs[i]];
                
				// Try to convert type if needed
				if (input.getType()!=value.getType())
                {
                    lookupRow[lookupIndex] = value.convertData(input, lookupRow[lookupIndex]);
                }
                lookupIndex++;
			}
			if (data.keynrs2[i]>=0)
			{
                ValueMetaInterface input = inputRowMeta.getValueMeta(data.keynrs2[i]);
                ValueMetaInterface value = data.lookupMeta.getValueMeta(lookupIndex);
                lookupRow[lookupIndex] = row[data.keynrs2[i]];
                
                // Try to convert type if needed
                if (input.getType()!=value.getType())
                {
                    lookupRow[lookupIndex] = value.convertData(input, lookupRow[lookupIndex]);
                }
                lookupIndex++;
			}
		}

        Object[] add = null;
        boolean cache_now=false;
        boolean cacheHit = false;

		// First, check if we looked up before
		if (meta.isCached())
        {
			add = getRowFromCache(data.lookupMeta, lookupRow);
			if (add!=null) 
			{
				cacheHit=true;
			}
        }
		else add=null; 

		if (add==null)
		{
			if ( !(meta.isCached() && meta.isLoadingAllDataInCache()) || data.hasDBCondition ) { // do not go to the database when all rows are in (exception LIKE operator)
				if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "DatabaseLookup.Log.AddedValuesToLookupRow1")+meta.getStreamKeyField1().length+BaseMessages.getString(PKG, "DatabaseLookup.Log.AddedValuesToLookupRow2")+data.lookupMeta.getString(lookupRow)); //$NON-NLS-1$ //$NON-NLS-2$

				data.db.setValuesLookup(data.lookupMeta, lookupRow);
				add = data.db.getLookup(meta.isFailingOnMultipleResults());
				cache_now=true;
			}
		}

		if (add==null) // nothing was found, unknown code: add default values
		{
			if (meta.isEatingRowOnLookupFailure())
			{
				return null;
			}
			if (getStepMeta().isDoingErrorHandling())
			{
                putError(getInputRowMeta(), row, 1L, "No lookup found", null, "DBL001");

                // return false else we would still be processed.
                return null;
			}
			
			if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "DatabaseLookup.Log.NoResultsFoundAfterLookup")); //$NON-NLS-1$
            
			add=new Object[data.returnMeta.size()];
			for (int i=0;i<meta.getReturnValueField().length;i++)
			{
				if (data.nullif[i]!=null)
				{
					add[i] = data.nullif[i];
				}
				else
				{
					add[i] = null;			
				}
			}
		}
        else
        {
        	if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "DatabaseLookup.Log.FoundResultsAfterLookup")+add); //$NON-NLS-1$

        	// Only verify the data types if the data comes from the DB, NOT when we have a cache hit
        	// In that case, we already know the data type is OK.
        	if (!cacheHit)
        	{
                incrementLinesInput();

	        	int types[] = meta.getReturnValueDefaultType();
	
	        	// The assumption here is that the types are in the same order
	        	// as the returned lookup row, but since we make the lookup row
	        	// that should not be a problem.
	            //
	            for (int i=0;i<types.length;i++)
	        	{  
	        		ValueMetaInterface returned = data.db.getReturnRowMeta().getValueMeta(i);
	                ValueMetaInterface expected = data.returnMeta.getValueMeta(i);
	                
	                if ( returned != null && types[i] > 0 && types[i] !=  returned.getType() )
	        		{
	        			// Set the type to the default return type
	        		    add[i] = expected.convertData(returned, add[i]);
	        		}
	        	}
        	}
        } 

		// Store in cache if we need to!
		// If we already loaded all data into the cache, storing more makes no sense.
		//
		if (meta.isCached() && cache_now && !meta.isLoadingAllDataInCache() && data.allEquals)
		{
			storeRowInCache(data.lookupMeta, lookupRow, add);
		}

		for (int i=0;i<data.returnMeta.size();i++)
		{
			outputRow[inputRowMeta.size()+i] = add[i];
		}

		return outputRow;
	}

	private void storeRowInCache(RowMetaInterface lookupMeta, Object[] lookupRow, Object[] add) {
		
		RowMetaAndData rowMetaAndData = new RowMetaAndData(lookupMeta, lookupRow);
// DEinspanjer 2009-02-01 XXX: I want to write a test case to prove this point before checking in.
//		/* Don't insert a row with a duplicate key into the cache. It doesn't seem
//		 * to serve a useful purpose and can potentially cause the step to return
//		 * different values over the life of the transformation (if the source DB rows change)
//		 * Additionally, if using the load all data feature, re-inserting would reverse the order
//		 * specified in the step.
//		 */
//		if (!data.look.containsKey(rowMetaAndData)) {
//		    data.look.put(rowMetaAndData, new TimedRow(add));
//		}
		data.look.put(rowMetaAndData, new TimedRow(add));

        // See if we have to limit the cache_size.
        // Sample 10% of the rows in the cache.
        // Remove everything below the second lowest date.
        // That should on average remove more than 10% of the entries
        // It's not exact science, but it will be faster than the old algorithm
        
		// DEinspanjer 2009-02-01: If you had previously set a cache size and then turned on load all, this
		// method would throw out entries if the previous cache size wasn't big enough.
        if (!meta.isLoadingAllDataInCache() && meta.getCacheSize()>0 && data.look.size()>meta.getCacheSize())
        {
            List<RowMetaAndData> keys = new ArrayList<RowMetaAndData>(data.look.keySet());
            List<Date> samples = new ArrayList<Date>();
            int incr = keys.size()/10;
            if (incr==0) incr=1;
            for (int k=0;k<keys.size();k+=incr)
            {
                RowMetaAndData key = (RowMetaAndData) keys.get(k);
                TimedRow timedRow = (TimedRow) data.look.get(key);
                samples.add(timedRow.getLogDate());
            }
            
            Collections.sort(samples);
            
            if (samples.size()>1)
            {
                Date smallest = (Date) samples.get(1);
                
                // Everything below the smallest date goes away...
                for (int k=0;k<keys.size();k++)
                {
                    RowMetaAndData key = (RowMetaAndData) keys.get(k);
                    TimedRow timedRow = (TimedRow) data.look.get(key);
                    
                    if (timedRow.getLogDate().compareTo(smallest)<0)
                    {
                        data.look.remove(key);
                    }
                }
            }
        }	
	}

	private Object[] getRowFromCache(RowMetaInterface lookupMeta, Object[] lookupRow) throws KettleException {
		if (data.allEquals) {
			// only do the hashtable lookup when all equals otherwise conditions >, <, <> will give wrong results
	        TimedRow timedRow = (TimedRow) data.look.get(new RowMetaAndData(data.lookupMeta, lookupRow));
	        if (timedRow!=null)
	        {
	            return timedRow.getRow();
	        }
		}
        else // special handling of conditions <,>, <> etc.
        {
    		if (!data.hasDBCondition)  //e.g. LIKE not handled by this routine, yet
    		{
    			// TODO: find an alternative way to look up the data based on the condition.
    			// Not all conditions are "=" so we are going to have to evaluate row by row
    			// A sorted list or index might be a good solution here...
    			// 
    			Enumeration<RowMetaAndData> keys = data.look.keys();
    			while (keys.hasMoreElements()) {
    				RowMetaAndData key = keys.nextElement();
    				// Now verify that the key is matching our conditions...
    				//
    				boolean match = true;
    				int lookupIndex=0;
					for (int i=0;i<data.conditions.length && match;i++) {
    					ValueMetaInterface cmpMeta = lookupMeta.getValueMeta(lookupIndex);
    					Object cmpData = lookupRow[lookupIndex];
    					ValueMetaInterface keyMeta = key.getValueMeta(i);
    					Object keyData = key.getData()[i];
    					
    					switch(data.conditions[i]) {
    					case DatabaseLookupMeta.CONDITION_EQ : match = (cmpMeta.compare(cmpData, keyMeta, keyData)==0); break;
    					case DatabaseLookupMeta.CONDITION_NE : match = (cmpMeta.compare(cmpData, keyMeta, keyData)!=0); break;
    					case DatabaseLookupMeta.CONDITION_LT : match = (cmpMeta.compare(cmpData, keyMeta, keyData)>0); break;
    					case DatabaseLookupMeta.CONDITION_LE : match = (cmpMeta.compare(cmpData, keyMeta, keyData)>=0); break;
    					case DatabaseLookupMeta.CONDITION_GT : match = (cmpMeta.compare(cmpData, keyMeta, keyData)<0); break;
    					case DatabaseLookupMeta.CONDITION_GE : match = (cmpMeta.compare(cmpData, keyMeta, keyData)<=0); break;
    					case DatabaseLookupMeta.CONDITION_IS_NULL: match = keyMeta.isNull(keyData); break;
    					case DatabaseLookupMeta.CONDITION_IS_NOT_NULL: match = !keyMeta.isNull(keyData); break;
    					case DatabaseLookupMeta.CONDITION_BETWEEN :
    						// Between key >= cmp && key <= cmp2
    						ValueMetaInterface cmpMeta2 = lookupMeta.getValueMeta(lookupIndex+1);
    						Object cmpData2 = lookupRow[lookupIndex+1];
    						match = (keyMeta.compare(keyData, cmpMeta, cmpData)>=0);
    						if (match) {
    							match = (keyMeta.compare(keyData, cmpMeta2, cmpData2)<=0);
    						}
    						lookupIndex++;
    						break;
    					// TODO: add LIKE operator (think of changing the hasDBCondition logic then)
    					default: 
    						match=false;
    						data.hasDBCondition=true; // avoid looping in here the next time, also safety when a new condition will be introduced
    					    break; 
    					
    					}
    					lookupIndex++;
    				}
					if (match) {
						TimedRow timedRow = data.look.get(key);
				        if (timedRow!=null)
				        {
				            return timedRow.getRow();
				        }
				        else
				        {
				        	// This should never occur
				        }
					}
    			}
    		}
       	}
        return null;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DatabaseLookupMeta)smi;
		data=(DatabaseLookupData)sdi;
		
		 boolean sendToErrorRow=false;
		 String errorMessage = null;

		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first=false;
            
            // create the output metadata
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

            if (meta.isCached())
            {
                if (meta.getCacheSize()>0)
                {
                    data.look=new Hashtable<RowMetaAndData, TimedRow>((int)(meta.getCacheSize()*1.5));
                }
                else
                {
                    data.look=new Hashtable<RowMetaAndData, TimedRow>();
                }
            }

            data.db.setLookup(environmentSubstitute(meta.getSchemaName()), 
            		          environmentSubstitute(meta.getTablename()), meta.getTableKeyField(), meta.getKeyCondition(), meta.getReturnValueField(), meta.getReturnValueNewName(), meta.getOrderByClause(), meta.isFailingOnMultipleResults());

            // lookup the values!
            if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "DatabaseLookup.Log.CheckingRow")+getInputRowMeta().getString(r)); //$NON-NLS-1$
            
            data.keynrs = new int[meta.getStreamKeyField1().length];
            data.keynrs2= new int[meta.getStreamKeyField1().length];

            for (int i=0;i<meta.getStreamKeyField1().length;i++)
            {
                data.keynrs[i]=getInputRowMeta().indexOfValue(meta.getStreamKeyField1()[i]);
                if (data.keynrs[i]<0 &&  // couldn't find field!
                    !"IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) &&   // No field needed! //$NON-NLS-1$
                    !"IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i])  // No field needed! //$NON-NLS-1$
                   )
                {
                    throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseLookup.ERROR0001.FieldRequired1.Exception")+meta.getStreamKeyField1()[i]+BaseMessages.getString(PKG, "DatabaseLookup.ERROR0001.FieldRequired2.Exception")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                data.keynrs2[i]=getInputRowMeta().indexOfValue(meta.getStreamKeyField2()[i]);
                if (data.keynrs2[i]<0 &&  // couldn't find field!
                    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed! //$NON-NLS-1$
                   )
                {
                    throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseLookup.ERROR0001.FieldRequired3.Exception")+meta.getStreamKeyField2()[i]+BaseMessages.getString(PKG, "DatabaseLookup.ERROR0001.FieldRequired4.Exception")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "DatabaseLookup.Log.FieldHasIndex1")+meta.getStreamKeyField1()[i]+BaseMessages.getString(PKG, "DatabaseLookup.Log.FieldHasIndex2")+data.keynrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
            }

            data.nullif = new Object[meta.getReturnValueField().length];

            for (int i=0;i<meta.getReturnValueField().length;i++)
            {
                ValueMetaInterface stringMeta = new ValueMeta("string", ValueMetaInterface.TYPE_STRING);
                ValueMetaInterface returnMeta = data.outputRowMeta.getValueMeta(i+getInputRowMeta().size());
                
                if (!Const.isEmpty(meta.getReturnValueDefault()[i]))
                {
                    data.nullif[i] = returnMeta.convertData(stringMeta, meta.getReturnValueDefault()[i]);
                }
                else
                {
                    data.nullif[i] = null;;
                }
            }

            // Determine the types...
            data.keytypes = new int[meta.getTableKeyField().length];
            String schemaTable = meta.getDatabaseMeta().getQuotedSchemaTableCombination(environmentSubstitute(meta.getSchemaName()), 
            		                                                                    environmentSubstitute(meta.getTablename()));
            RowMetaInterface fields = data.db.getTableFields(schemaTable);
            if (fields!=null)
            {
                // Fill in the types...
                for (int i=0;i<meta.getTableKeyField().length;i++)
                {
                    ValueMetaInterface key = fields.searchValueMeta(meta.getTableKeyField()[i]);
                    if (key!=null)
                    {
                        data.keytypes[i] = key.getType();
                    }
                    else
                    {
                        throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseLookup.ERROR0001.FieldRequired5.Exception")+meta.getTableKeyField()[i]+BaseMessages.getString(PKG, "DatabaseLookup.ERROR0001.FieldRequired6.Exception")); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            else
            {
                throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseLookup.ERROR0002.UnableToDetermineFieldsOfTable")+schemaTable+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            // Count the number of values in the lookup as well as the metadata to send along with it.
            //
            data.lookupMeta = new RowMeta();
            
            for (int i=0;i<meta.getStreamKeyField1().length;i++)
            {
                if (data.keynrs[i]>=0)
                {
                    ValueMetaInterface value = getInputRowMeta().getValueMeta(data.keynrs[i]).clone();
                    
                    // Try to convert type if needed in a clone, we don't want to
                    // change the type in the original row

                    value.setType(data.keytypes[i]);
                    data.lookupMeta.addValueMeta( value );
                }
                if (data.keynrs2[i]>=0)
                {
                    ValueMetaInterface value = getInputRowMeta().getValueMeta(data.keynrs2[i]).clone();

                    // Try to convert type if needed in a clone, we don't want to
                    // change the type in the original row
                    
                    value.setType(data.keytypes[i]);
                    data.lookupMeta.addValueMeta( value );
                }
            }
            
            // We also want to know the metadata of the return values beforehand (null handling)
            data.returnMeta = new RowMeta();
            
            for (int i=0;i<meta.getReturnValueField().length;i++)
            {
                ValueMetaInterface v = data.outputRowMeta.getValueMeta(getInputRowMeta().size()+i).clone();
                data.returnMeta.addValueMeta(v);
            }
            
            // If the user selected to load all data into the cache at startup, that's what we do now...
            //
            if (meta.isCached() && meta.isLoadingAllDataInCache()) {
            	loadAllTableDataIntoTheCache();
            }
            
        }

		if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "DatabaseLookup.Log.GotRowFromPreviousStep")+getInputRowMeta().getString(r)); //$NON-NLS-1$

		try
		{
            // add new lookup values to the row
            Object[] outputRow = lookupValues(getInputRowMeta(), r); 

            if (outputRow!=null)
            {
	            // copy row to output rowset(s);
				putRow(data.outputRowMeta, outputRow);
	            
				if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "DatabaseLookup.Log.WroteRowToNextStep")+getInputRowMeta().getString(r)); //$NON-NLS-1$
	            if (checkFeedback(getLinesRead())) logBasic("linenr "+getLinesRead()); //$NON-NLS-1$
            }
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
				logError(BaseMessages.getString(PKG, "DatabaseLookup.ERROR003.UnexpectedErrorDuringProcessing")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), r, 1, errorMessage, null, "DBLOOKUPD001");
			}

		}

		return true;
	}
    
    private void loadAllTableDataIntoTheCache() throws KettleException {
    	DatabaseMeta dbMeta = meta.getDatabaseMeta();
    	
    	try {
	    	// We only want to get the used table fields...
	    	//
	    	String sql = "SELECT ";
	    	
            for (int i=0;i<meta.getStreamKeyField1().length;i++)
            {
            	if (i>0) sql+=", ";
            	sql+=dbMeta.quoteField(meta.getTableKeyField()[i]);
            }

	    	// Also grab the return field...
	    	//
	    	for (int i=0;i<meta.getReturnValueField().length;i++) {
	    		sql+=", "+dbMeta.quoteField(meta.getReturnValueField()[i]);
	    	}
	    	// The schema/table
	    	//
	    	sql+=" FROM "+dbMeta.getQuotedSchemaTableCombination(environmentSubstitute(meta.getSchemaName()), environmentSubstitute(meta.getTablename()));
	    	
	    	// order by?
			if (meta.getOrderByClause()!=null && meta.getOrderByClause().length()!=0)
			{
				sql += " ORDER BY "+meta.getOrderByClause();
			}
	    	
	    	// Now that we have the SQL constructed, let's store the rows...
	    	//
	    	List<Object[]> rows = data.db.getRows(sql, 0);
	    	if (rows!=null && rows.size()>0) {
	    		RowMetaInterface returnRowMeta = data.db.getReturnRowMeta();
	    		// Copy the data into 2 parts: key and value...
	    		// 
	    		for (Object[] row : rows) {
	    			int index=0;
	    			RowMeta keyMeta = new RowMeta();
	    			Object[] keyData = new Object[meta.getStreamKeyField1().length];
	    			for (int i=0;i<meta.getStreamKeyField1().length;i++) {
	    				keyData[i] = row[index];
	    				keyMeta.addValueMeta(returnRowMeta.getValueMeta(index++));
	    			}
	    			// RowMeta valueMeta = new RowMeta();
	    			Object[] valueData = new Object[data.returnMeta.size()];
	    			for (int i=0;i<data.returnMeta.size();i++) {
	    				valueData[i] = row[index++];
	    				// valueMeta.addValueMeta(returnRowMeta.getValueMeta(index++));
	    			}
	    			// Store the data...
	    			//
	    			storeRowInCache(keyMeta, keyData, valueData);
	    			incrementLinesInput();
	    		}
	    	}
    	}
    	catch(Exception e) {
    		throw new KettleException(e);
    	}
	}

	/** Stop the running query */
    public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(DatabaseLookupMeta)smi;
        data=(DatabaseLookupData)sdi;

        if (data.db!=null && !data.isCanceled) 
        {
          synchronized(data.db) {
            data.db.cancelQuery();
          }
        	data.isCanceled = true;
        }
    }


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DatabaseLookupMeta)smi;
		data=(DatabaseLookupData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(this, meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
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
                                
                if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "DatabaseLookup.Log.ConnectedToDatabase")); //$NON-NLS-1$
                
                // See if all the lookup conditions are "equal"
                // This might speed up things in the case when we load all data in the cache
                //
                data.allEquals = true;
                data.hasDBCondition = false;
                data.conditions = new int[meta.getKeyCondition().length];
                for (int i=0;i<meta.getKeyCondition().length;i++) {
                	data.conditions[i] = Const.indexOfString(meta.getKeyCondition()[i], DatabaseLookupMeta.conditionStrings);
                	if (!("=".equals(meta.getKeyCondition()[i]))) {
                		data.allEquals = false;
                	}
                	if (data.conditions[i]==DatabaseLookupMeta.CONDITION_LIKE) {
                		data.hasDBCondition = true;
                	}
                }

				return true;
			}
			catch(Exception e)
			{
				logError(BaseMessages.getString(PKG, "DatabaseLookup.ERROR0004.UnexpectedErrorDuringInit")+e.toString()); //$NON-NLS-1$
				if (data.db!=null) {
                	data.db.disconnect();
				}
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DatabaseLookupMeta)smi;
	    data = (DatabaseLookupData)sdi;

	    if (data.db!=null) {
        	data.db.disconnect();
	    }
	    
        // Recover memory immediately, allow in-memory data to be garbage collected
        //
	    data.look = null;

	    super.dispose(smi, sdi);
	}

}