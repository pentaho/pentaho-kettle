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
 
package org.pentaho.di.trans.steps.selectvalues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * Select, re-order, remove or change the meta-data of the fields in the inputstreams.
 * 
 * @author Matt
 * @since 5-apr-2003
 *
 */
public class SelectValues extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SelectValuesMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SelectValuesMeta meta;
	private SelectValuesData data;
	
	public SelectValues(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	/**
	   Only select the values that are still needed...<p>
	   Put the values in the right order...<p>
	   Change the meta-data information if needed...<p>
	   
	   @param row The row to manipulate
	   @return true if everything went well, false if we need to stop because of an error!	   
	*/
	private synchronized Object[] selectValues(RowMetaInterface rowMeta, Object[] rowData) throws KettleValueException
	{
		if (data.firstselect)
		{
			data.firstselect=false;

            // We need to create a new meta-data row to drive the output
            // We also want to know the indexes of the selected fields in the source row.
            //
			data.fieldnrs=new int[meta.getSelectName().length];
			for (int i=0;i<data.fieldnrs.length;i++) 
			{
				data.fieldnrs[i]=rowMeta.indexOfValue( meta.getSelectName()[i] );
				if (data.fieldnrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "SelectValues.Log.CouldNotFindField",meta.getSelectName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return null;
				}
			}
			
			// Check for doubles in the selected fields... AFTER renaming!!
			//
			int cnt[] = new int[meta.getSelectName().length];
			for (int i=0;i<meta.getSelectName().length;i++)
			{
				cnt[i]=0;
				for (int j=0;j<meta.getSelectName().length;j++)
				{
                    String one = Const.NVL( meta.getSelectRename()[i], meta.getSelectName()[i]);
                    String two = Const.NVL( meta.getSelectRename()[j], meta.getSelectName()[j]);
					if (one.equals(two)) cnt[i]++;
					
					if (cnt[i]>1)
					{
						logError(BaseMessages.getString(PKG, "SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice",one)); //$NON-NLS-1$ //$NON-NLS-2$
						setErrors(1);
						stopAll();
						return null;
					}
				}
			}
			
			// See if we need to include (and sort) the non-specified fields as well...
			//
			if (meta.isSelectingAndSortingUnspecifiedFields()) {
				// Select the unspecified fields.
				// Sort the fields
				// Add them after the specified fields...
				//
				List<String> extra = new ArrayList<String>();
				ArrayList<Integer> unspecifiedKeyNrs = new ArrayList<Integer>(); 
				for (int i=0;i<rowMeta.size();i++) {
					String fieldName = rowMeta.getValueMeta(i).getName();
					if (Const.indexOfString(fieldName, meta.getSelectName())<0) {
						extra.add(fieldName);
					}
				}
				Collections.sort(extra);
				for (String fieldName : extra) {
					int index = rowMeta.indexOfValue(fieldName);
					unspecifiedKeyNrs.add(index);
				}
				
				// Create the extra field list...
				//
				data.extraFieldnrs = new int[unspecifiedKeyNrs.size()];
				for (int i=0;i<data.extraFieldnrs.length;i++) data.extraFieldnrs[i] = unspecifiedKeyNrs.get(i);
			}
			else
			{
				data.extraFieldnrs = new int[] {};
			}
		}

        // Create a new output row
        Object[] outputData = new Object[data.selectRowMeta.size()];
        int outputIndex = 0;
        
		// Get the field values
        //
		for (int idx : data.fieldnrs)
		{
            // Normally this can't happen, except when streams are mixed with different
			// number of fields.
			// 
			if (idx<rowMeta.size())
			{
                ValueMetaInterface valueMeta = rowMeta.getValueMeta( idx );
                
			    // TODO: Clone might be a 'bit' expensive as it is only needed in case you want to copy a single field to 2 or more target fields.
                // And even then it is only required for the last n-1 target fields.
                // Perhaps we can consider the requirements for cloning at init(), store it in a boolean[] and just consider this at runtime
                //
                outputData[outputIndex++] = valueMeta.cloneValueData(rowData[idx]);
			}
			else
			{
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SelectValues.Log.MixingStreamWithDifferentFields")); //$NON-NLS-1$
			}			
		}
		
		// Do we need to drag the rest of the row also in there?
		//
		for (int idx : data.extraFieldnrs)
		{
			outputData[outputIndex++] = rowData[idx]; // always just a copy, can't be specified twice.
		}

		return outputData;
	}
	
	/**
	   
	   Remove the values that are no longer needed.<p>
	   
	   @param row The row to manipulate
	   @return true if everything went well, false if we need to stop because of an error!
	   
	*/
	private synchronized Object[] removeValues(RowMetaInterface rowMeta, Object[] rowData)
	{		
		if (data.firstdeselect)
		{
			data.firstdeselect=false;

			data.removenrs=new int[meta.getDeleteName().length];
			for (int i=0;i<data.removenrs.length;i++) 
			{
				data.removenrs[i]=rowMeta.indexOfValue(meta.getDeleteName()[i]);
				if (data.removenrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "SelectValues.Log.CouldNotFindField",meta.getDeleteName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return null;
				}
			}
			
			// Check for doubles in the selected fields...
			int cnt[] = new int[meta.getDeleteName().length];
			for (int i=0;i<meta.getDeleteName().length;i++)
			{
				cnt[i]=0;
				for (int j=0;j<meta.getDeleteName().length;j++)
				{
					if (meta.getDeleteName()[i].equals(meta.getDeleteName()[j])) cnt[i]++;
					
					if (cnt[i]>1)
					{
						logError(BaseMessages.getString(PKG, "SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice2",meta.getDeleteName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
						setErrors(1);
						stopAll();
						return null;
					}
				}
			}
			
			// Sort removenrs descending.  So that we can delete in ascending order...
            Arrays.sort(data.removenrs);
		}

		/*
		 *  Remove the field values
		 *  Take into account that field indexes change once you remove them!!!
		 *  Therefore removenrs is sorted in reverse on index...
		 */
        return RowDataUtil.removeItems(rowData, data.removenrs);
	}

	/**
	   
	   Change the meta-data of certain fields.<p>
	   This, we can do VERY fast.<p>
	   
	   @param row The row to manipulate
	   @return true if everything went well, false if we need to stop because of an error!
	 * @throws KettleValueException 
	   
	*/
	private synchronized Object[] metadataValues(RowMetaInterface rowMeta, Object[] rowData) throws KettleValueException
	{
		if (data.firstmetadata)
		{
			data.firstmetadata=false;

			data.metanrs=new int[meta.getMeta().length];
			for (int i=0;i<data.metanrs.length;i++) 
			{
				data.metanrs[i]=rowMeta.indexOfValue(meta.getMeta()[i].getName());
				if (data.metanrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "SelectValues.Log.CouldNotFindField",meta.getMeta()[i].getName())); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return null;
				}
			}
			
			// Check for doubles in the selected fields...
			int cnt[] = new int[meta.getMeta().length];
			for (int i=0;i<meta.getMeta().length;i++)
			{
				cnt[i]=0;
				for (int j=0;j<meta.getMeta().length;j++)
				{
					if (meta.getMeta()[i].getName().equals(meta.getMeta()[j].getName())) cnt[i]++;
					
					if (cnt[i]>1)
					{
						logError(BaseMessages.getString(PKG, "SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice2",meta.getMeta()[i].getName())); //$NON-NLS-1$ //$NON-NLS-2$
						setErrors(1);
						stopAll();
						return null;
					}
				}
			}
			
			// Also apply the metadata on the row meta to allow us to convert the data correctly, with the correct mask.
			//
			for (int i=0;i<data.metanrs.length;i++) 
			{
				SelectMetadataChange change = meta.getMeta()[i];
				ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.metanrs[i]);
				if (!Const.isEmpty(change.getConversionMask()))
				{
					valueMeta.setConversionMask(change.getConversionMask());
				}
				
				valueMeta.setDateFormatLenient(change.isDateFormatLenient());
				
				if (!Const.isEmpty(change.getEncoding()))
				{
				  valueMeta.setStringEncoding(change.getEncoding());
				}
				if (!Const.isEmpty(change.getDecimalSymbol()))
				{
					valueMeta.setDecimalSymbol(change.getDecimalSymbol());
				}
				if (!Const.isEmpty(change.getGroupingSymbol()))
				{
					valueMeta.setGroupingSymbol(change.getGroupingSymbol());
				}
				if (!Const.isEmpty(change.getCurrencySymbol()))
				{
					valueMeta.setCurrencySymbol(change.getCurrencySymbol());
				}
			}
		}

		//
		// Change the data too 
		//
		for (int i=0;i<data.metanrs.length;i++)
		{
			int index = data.metanrs[i];
			ValueMetaInterface fromMeta = rowMeta.getValueMeta(index);
            ValueMetaInterface toMeta   = data.metadataRowMeta.getValueMeta(index);
			
			// If we need to change from BINARY_STRING storage type to NORMAL...
			//
			if (fromMeta.isStorageBinaryString() && meta.getMeta()[i].getStorageType()==ValueMetaInterface.STORAGE_TYPE_NORMAL)
			{
				rowData[index] = fromMeta.convertBinaryStringToNativeType((byte[]) rowData[index]);
			}
			if (meta.getMeta()[i].getType()!=ValueMetaInterface.TYPE_NONE && fromMeta.getType()!=toMeta.getType())
            {
                switch(toMeta.getType())
                {
                case ValueMetaInterface.TYPE_STRING    : rowData[index] = fromMeta.getString(rowData[index]); break;
                case ValueMetaInterface.TYPE_NUMBER    : rowData[index] = fromMeta.getNumber(rowData[index]); break;
                case ValueMetaInterface.TYPE_INTEGER   : rowData[index] = fromMeta.getInteger(rowData[index]); break;
                case ValueMetaInterface.TYPE_DATE      : rowData[index] = fromMeta.getDate(rowData[index]); break;
                case ValueMetaInterface.TYPE_BIGNUMBER : rowData[index] = fromMeta.getBigNumber(rowData[index]); break;
                case ValueMetaInterface.TYPE_BOOLEAN   : rowData[index] = fromMeta.getBoolean(rowData[index]); break;
                case ValueMetaInterface.TYPE_BINARY    : rowData[index] = fromMeta.getBinary(rowData[index]); break;
                default: throw new KettleValueException("Unable to convert data type of value '"+fromMeta+"' to data type "+toMeta.getType());
                }
            }
		}

		return rowData;
	}

	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SelectValuesMeta)smi;
		data=(SelectValuesData)sdi;

		Object[] rowData=getRow();   // get row from rowset, wait for our turn, indicate busy!
		if (rowData==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "SelectValues.Log.GotRowFromPreviousStep")+getInputRowMeta().getString(rowData)); //$NON-NLS-1$

		if (first)
		{
			first = false;
			
			data.selectRowMeta = getInputRowMeta().clone();
			meta.getSelectFields(data.selectRowMeta, getStepname());
			data.deselectRowMeta = data.selectRowMeta.clone();
			meta.getDeleteFields(data.deselectRowMeta);
			data.metadataRowMeta = data.deselectRowMeta.clone();
			meta.getMetadataFields(data.metadataRowMeta, getStepname());
		}
		
		Object[] outputData = rowData;
		
        if (data.select)   outputData = selectValues(getInputRowMeta(), outputData);
		if (data.deselect) outputData = removeValues(data.selectRowMeta, outputData);
		if (data.metadata) outputData = metadataValues(data.deselectRowMeta, outputData);
		
		if (outputData==null) 
		{
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 

        // Send the row on its way
		putRow(data.metadataRowMeta, outputData);
        
		if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "SelectValues.Log.WroteRowToNextStep")+data.metadataRowMeta.getString(outputData)); //$NON-NLS-1$

        if (checkFeedback(getLinesRead())) logBasic(BaseMessages.getString(PKG, "SelectValues.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SelectValuesMeta)smi;
		data=(SelectValuesData)sdi;

		if (super.init(smi, sdi))
		{
			data.firstselect   = true;
			data.firstdeselect = true;
			data.firstmetadata = true;

			data.select=false;
			data.deselect=false;
			data.metadata=false;
			
			if (!Const.isEmpty(meta.getSelectName())) data.select   = true;
			if (!Const.isEmpty(meta.getDeleteName())) data.deselect = true;
			if (!Const.isEmpty(meta.getMeta())) data.metadata = true;
			
			boolean atLeastOne = data.select || data.deselect || data.metadata;
			if (!atLeastOne)
			{
				setErrors(1);
				logError(BaseMessages.getString(PKG, "SelectValues.Log.InputShouldContainData")); //$NON-NLS-1$
			}
			
			return atLeastOne; // One of those three has to work!
		}
		else
		{
			return false;
		}
	}
			
}