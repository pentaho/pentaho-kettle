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
 
package org.pentaho.di.trans.steps.streamlookup;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Collections;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.hash.ByteArrayHashIndex;
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
 * Looks up information by first reading data into a hash table (in memory)
 * 
 * TODO: add warning with conflicting types OR modify the lookup values to the input row type. (this is harder to do as currently we don't know the types)
 * 
 * @author Matt
 * @since  26-apr-2003
 */
public class StreamLookup extends BaseStep implements StepInterface
{
	private static Class<?> PKG = StreamLookupMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private StreamLookupMeta meta;
	private StreamLookupData data;


	public StreamLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private void handleNullIf()
	{
	    data.nullIf = new Object[meta.getValue().length];
	    
		for (int i=0;i<meta.getValue().length;i++)
		{
			if (meta.getValueDefaultType()[i]<0)
			{
				//logError("unknown default value type: "+dtype+" for value "+value[i]+", default to type: String!");
				meta.getValueDefaultType()[i]=ValueMetaInterface.TYPE_STRING;
			}
			data.nullIf[i]=null;
			switch(meta.getValueDefaultType()[i])
			{
			case ValueMetaInterface.TYPE_STRING: 
				if (Const.isEmpty(meta.getValueDefault()[i]))
                {
                    data.nullIf[i] = null;
                }
                else
                {
                    data.nullIf[i]=meta.getValueDefault()[i];
                }
				break;
			case ValueMetaInterface.TYPE_DATE  :
				try { data.nullIf[i] = DateFormat.getInstance().parse(meta.getValueDefault()[i]); }
				catch(Exception e) { }
				break;
			case ValueMetaInterface.TYPE_NUMBER: 
				try { data.nullIf[i] = new Double( Double.parseDouble(meta.getValueDefault()[i]) ); }
				catch(Exception e) { }
				break;
			case ValueMetaInterface.TYPE_INTEGER: 
				try { data.nullIf[i] = new Long( Long.parseLong(meta.getValueDefault()[i]) ); }
				catch(Exception e) { }
				break;
			case ValueMetaInterface.TYPE_BOOLEAN: 
				if ("TRUE".equalsIgnoreCase(meta.getValueDefault()[i]) || //$NON-NLS-1$
				    "Y".equalsIgnoreCase(meta.getValueDefault()[i]) )  //$NON-NLS-1$
				    data.nullIf[i] = Boolean.TRUE; 
				else
				    data.nullIf[i] = Boolean.FALSE; 
				;
				break;
			case ValueMetaInterface.TYPE_BIGNUMBER: 
				try { data.nullIf[i] = new BigDecimal(meta.getValueDefault()[i]); }
				catch(Exception e) { }
				break;
			default: 
				// if a default value is given and no conversion is implemented throw an error
				if (meta.getValueDefault()[i] != null && meta.getValueDefault()[i].trim().length()>0 ) {
					throw new RuntimeException(BaseMessages.getString(PKG, "StreamLookup.Exception.ConversionNotImplemented") +" " + ValueMeta.getTypeDesc(meta.getValueDefaultType()[i]));
				} else {
					// no default value given: just set it to null
					data.nullIf[i] = null;
					break;
				}				
			}
		}
	}

	private boolean readLookupValues() throws KettleException
	{
		data.infoStream = meta.getStepIOMeta().getInfoStreams().get(0);
		if (data.infoStream.getStepMeta()==null)
		{
			logError(BaseMessages.getString(PKG, "StreamLookup.Log.NoLookupStepSpecified")); //$NON-NLS-1$
			return false;
		}
		if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "StreamLookup.Log.ReadingFromStream")+data.infoStream.getStepname()+"]"); //$NON-NLS-1$ //$NON-NLS-2$

        int[] keyNrs = new int[meta.getKeylookup().length];
        int[] valueNrs = new int[meta.getValue().length];
        boolean firstRun = true;
        
        // Which row set do we read from?
        //
        RowSet rowSet = findInputRowSet(data.infoStream.getStepname());
        Object[] rowData=getRowFrom(rowSet); // rows are originating from "lookup_from"
		while (rowData!=null)
		{
            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "StreamLookup.Log.ReadLookupRow")+rowSet.getRowMeta().getString(rowData)); //$NON-NLS-1$

            if (firstRun)
            {
                firstRun=false;
                data.hasLookupRows=true;
                
                data.infoMeta = rowSet.getRowMeta().clone();
                data.keyMeta = new RowMeta();
                data.valueMeta = new RowMeta();
            
                // Look up the keys in the source rows
                for (int i=0;i<meta.getKeylookup().length;i++)
                {
                    keyNrs[i] = rowSet.getRowMeta().indexOfValue(meta.getKeylookup()[i]);
                    if (keyNrs[i]<0)
                    {
                        throw new KettleStepException(BaseMessages.getString(PKG, "StreamLookup.Exception.UnableToFindField",meta.getKeylookup()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    data.keyMeta.addValueMeta( rowSet.getRowMeta().getValueMeta( keyNrs[i] ));
                }
                // Save the data types of the keys to optionally convert input rows later on...
                if (data.keyTypes==null)
                {
                    data.keyTypes=data.keyMeta.clone();
                }
    			// set the meta data for the keys also to STORAGE_TYPE_NORMAL, otherwise it will conflict later on
                // for the data is is already set to STORAGE_TYPE_NORMAL in StreamLookupMeta.getFields()
    			// all values in the cache are of this storage type (see convertToNormalStorageType below)
    			// position here after keyTypes are stored (needed below for correct convertToNormalStorageType)
                for (int i=0;i<keyNrs.length;i++)
                {
                	data.keyMeta.getValueMeta(i).setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
                }                
            			
    			for (int v=0;v<meta.getValue().length;v++)
    			{
    			    valueNrs[v] = rowSet.getRowMeta().indexOfValue( meta.getValue()[v] );
                    if (valueNrs[v]<0)
    				{
                        throw new KettleStepException(BaseMessages.getString(PKG, "StreamLookup.Exception.UnableToFindField",meta.getValue()[v])); //$NON-NLS-1$ //$NON-NLS-2$
    				}
                    data.valueMeta.addValueMeta( rowSet.getRowMeta().getValueMeta(valueNrs[v]) );
    			}
            }
            
            Object[] keyData = new Object[keyNrs.length];
            for (int i=0;i<keyNrs.length;i++)
            {
            	ValueMetaInterface keyMeta = data.keyTypes.getValueMeta(i);
                keyData[i] = keyMeta.convertToNormalStorageType( rowData[ keyNrs[i] ] ); // Make sure only normal storage goes in
                keyMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL); // now we need to change keyMeta/keyTypes also to normal
            }

            Object[] valueData = new Object[valueNrs.length];
            for (int i=0;i<valueNrs.length;i++)
            {
            	ValueMetaInterface valueMeta = data.valueMeta.getValueMeta(i);
                valueData[i] = valueMeta.convertToNormalStorageType( rowData[ valueNrs[i] ] ); // make sure only normal storage goes in
            }

            addToCache(data.keyMeta, keyData, data.valueMeta, valueData);
			
			rowData=getRowFrom(rowSet);
		}
		
		return true;
	}


    private Object[] lookupValues(RowMetaInterface rowMeta, Object[] row) throws KettleException
	{
		// See if we need to stop.
		if (isStopped()) return null;
		
		if ( data.lookupColumnIndex == null ) 
		{
			String names[] = data.lookupMeta.getFieldNames();
			data.lookupColumnIndex = new int[names.length];
			
			for( int i=0; i<names.length; i++ ) 
			{
				data.lookupColumnIndex[i] = rowMeta.indexOfValue(names[i]);
				if ( data.lookupColumnIndex[i] < 0 ) 
				{
					// we should not get here
					throw new KettleStepException( "The lookup column '"+names[i]+"' could not be found" );
				}
			}
		}
		
		// Copy value references to lookup table.
		//
        Object[] lu = new Object[data.keynrs.length];
        for (int i=0;i<data.keynrs.length;i++) {
        	// If the input is binary storage data, we convert it to normal storage.
        	//
        	if (data.convertKeysToNative[i]) {
        		lu[i] = data.lookupMeta.getValueMeta(i).convertBinaryStringToNativeType((byte[])row[data.keynrs[i]]);
        	} else {
        		lu[i] = row[data.keynrs[i]];
        	}
        }

        // Handle conflicting types (Number-Integer-String conversion to lookup type in hashtable)
        if (data.keyTypes!=null)
        {
            for (int i=0;i<data.lookupMeta.size();i++)
            {
                ValueMetaInterface inputValue  = data.lookupMeta.getValueMeta(i);
                ValueMetaInterface lookupValue = data.keyTypes.getValueMeta(i);
                if (inputValue.getType()!=lookupValue.getType())
                {
                    try
                    {
                    	// Change the input value to match the lookup value
                    	//
                        lu[i] = lookupValue.convertDataCompatible(inputValue, lu[i]);
                    }
                    catch (KettleValueException e)
                    {
                        throw new KettleStepException("Error converting data while looking up value", e);
                    }
                }
            }
        }
        
        Object[] add = null;
        
		if (data.hasLookupRows) 
		{
			try
			{
				if (meta.getKeystream().length>0)
				{
					add=getFromCache(data.keyTypes, lu);
				}
				else
				{
					// Just take the first element in the hashtable...
					throw new KettleStepException(BaseMessages.getString(PKG, "StreamLookup.Log.GotRowWithoutKeys")); //$NON-NLS-1$
				}
			}
			catch(Exception e)
			{
				throw new KettleStepException(e);
			}
		}
		
		if (add==null) // nothing was found, unknown code: add the specified default value...
		{
			add=data.nullIf;
		} 
		
        return RowDataUtil.addRowData(row, rowMeta.size(), add);
	}
	
    private void addToCache(RowMetaInterface keyMeta, Object[] keyData, RowMetaInterface valueMeta, Object[] valueData) throws KettleValueException
    {
        if (meta.isMemoryPreservationActive())
        {
            if (meta.isUsingSortedList())
            {
                KeyValue keyValue = new KeyValue(keyData, valueData);
                int idx = Collections.binarySearch(data.list, keyValue, data.comparator);
                if (idx<0)
                {
                    int index = -idx-1; // this is the insertion point
                    data.list.add(index, keyValue); // insert to keep sorted.
                }
                else
                {
                    data.list.set(idx, keyValue); // Overwrite to simulate Hashtable behaviour
                }
            }
            else
            {
                if (meta.isUsingIntegerPair())
                {
                	if (!data.metadataVerifiedIntegerPair) {
                		data.metadataVerifiedIntegerPair=true;
                		if (keyMeta.size()!=1 || valueMeta.size()!=1 || !keyMeta.getValueMeta(0).isInteger() || !valueMeta.getValueMeta(0).isInteger()) {

                			throw new KettleValueException(BaseMessages.getString(PKG, "StreamLookup.Exception.CanNotUseIntegerPairAlgorithm"));
                		}
                	}
                	
                    Long key = keyMeta.getInteger(keyData, 0);
                    Long value = valueMeta.getInteger(valueData, 0);
                    data.longIndex.put(key, value);
                }
                else
                {
                    if (data.hashIndex==null) 
                    { 
                        data.hashIndex = new ByteArrayHashIndex(keyMeta);
                    }
                    data.hashIndex.put(RowMeta.extractData(keyMeta, keyData), RowMeta.extractData(valueMeta, valueData));
                }
            }
        }
        else
        {
            // We can't just put Object[] in the map
            // The compare function is not in it.
            // We need to wrap in and use that.
            // Let's use RowMetaAndData for this one.
            //
            data.look.put(new RowMetaAndData(keyMeta, keyData), valueData);
        }
    }
    
	private Object[] getFromCache(RowMetaInterface keyMeta, Object[] keyData) throws KettleValueException
    {
        if (meta.isMemoryPreservationActive())
        {
            if (meta.isUsingSortedList())
            {
                KeyValue keyValue = new KeyValue(keyData, null);
                int idx = Collections.binarySearch(data.list, keyValue, data.comparator);
                if (idx<0) return null; // nothing found
                
                keyValue = (KeyValue)data.list.get(idx);
                return keyValue.getValue();
            }
            else
            {
                if (meta.isUsingIntegerPair())
                {
                    Long value = data.longIndex.get( keyMeta.getInteger(keyData, 0) );
                    if (value==null) return null;
                    return new Object[] { value, };
                }
                else
                {
                	try
                	{
	                    byte[] value = data.hashIndex.get(RowMeta.extractData(keyMeta, keyData));
	                    if (value==null) return null;
	                    return RowMeta.getRow(data.valueMeta, value);
                	}
                	catch(Exception e) {
                		logError("Oops", e);
                		throw new RuntimeException(e);
                	}
                }
            }
        }
        else
        {
            return (Object[])data.look.get(new RowMetaAndData(keyMeta, keyData));
        }
    }
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
	    meta = (StreamLookupMeta)smi;
	    data = (StreamLookupData)sdi;
	    
	    if (data.readLookupValues)
	    {
	        data.readLookupValues = false;
	        
			if (! readLookupValues()) // Read values in lookup table (look)
			{
				logError(BaseMessages.getString(PKG, "StreamLookup.Log.UnableToReadDataFromLookupStream")); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				return false;
			}
			
			// At this point, all the values in the cache are of normal storage data type...
			// We should reflect this in the metadata...
			//
			if(data.keyMeta!=null) { //null when no rows coming from lookup stream
				for (ValueMetaInterface valueMeta : data.keyMeta.getValueMetaList()) {
					valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
				}
			}
			if(data.valueMeta!=null) { //null when no rows coming from lookup stream
				for (ValueMetaInterface valueMeta : data.valueMeta.getValueMetaList()) {
					valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
				}
			}
	    }
	    
		Object[] r=getRow();      // Get row from input rowset & set row busy!
		if (r==null)         // no more input to be expected...
		{
			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "StreamLookup.Log.StoppedProcessingWithEmpty",getLinesRead()+"")); //$NON-NLS-1$ //$NON-NLS-2$
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first=false;
            
            // read the lookup values!
            data.keynrs = new int[meta.getKeystream().length];
            data.lookupMeta = new RowMeta();
            data.convertKeysToNative =  new boolean[meta.getKeystream().length];
            
            for (int i=0;i<meta.getKeystream().length;i++)
            {
                // Find the keynr in the row (only once)
                data.keynrs[i]=getInputRowMeta().indexOfValue(meta.getKeystream()[i]);
                if (data.keynrs[i]<0)
                {
                    throw new KettleStepException(BaseMessages.getString(PKG, "StreamLookup.Log.FieldNotFound",meta.getKeystream()[i],""+getInputRowMeta().getString(r))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                else
                {
                    if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "StreamLookup.Log.FieldInfo",meta.getKeystream()[i],""+data.keynrs[i])); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                
                data.lookupMeta.addValueMeta( getInputRowMeta().getValueMeta(data.keynrs[i]).clone() );
                
                // If we have binary storage data coming in, we convert it to normal data storage.
                // The storage in the lookup data store is also normal data storage. TODO: enforce normal data storage??
                //
                data.convertKeysToNative[i] = getInputRowMeta().getValueMeta(data.keynrs[i]).isStorageBinaryString();
            }
            
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.infoMeta }, null, this);
            
            // Handle the NULL values (not found...)
            handleNullIf();
        }
		
		Object[] outputRow = lookupValues(getInputRowMeta(), r); // Do the actual lookup in the hastable.
		if (outputRow==null)
		{
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		
		putRow(data.outputRowMeta, outputRow);       // copy row to output rowset(s);
			
        if (checkFeedback(getLinesRead())) 
        {
        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "StreamLookup.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
        }
			
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (StreamLookupMeta)smi;
	    data = (StreamLookupData)sdi;
	    
	    if (super.init(smi, sdi))
	    {
	        data.readLookupValues = true;
	        
	        return true;
	    }
	    return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}