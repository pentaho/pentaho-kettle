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
 
package org.pentaho.di.trans.steps.validator;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Calculate new field values using pre-defined functions. 
 * 
 * @author Matt
 * @since 8-sep-2005
 */
public class Validator extends BaseStep implements StepInterface
{
    public class FieldIndexes
    {
        public int indexName;
        public int indexA;
        public int indexB;
        public int indexC;
    };    

	private ValidatorMeta meta;
	private ValidatorData data;

	public Validator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(ValidatorMeta)smi;
		data=(ValidatorData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first=false;
            
            data.fieldIndexes = new int[meta.getValidatorField().length];
            
            // Calculate the indexes of the values and arguments in the target data or temporary data
            // We do this in advance to save time later on.
            //
            for (int i=0;i<meta.getValidatorField().length;i++)
            {
                ValidatorField field = meta.getValidatorField()[i];
                
                if (!Const.isEmpty(field.getName())) 
                {
                    data.fieldIndexes[i] = getInputRowMeta().indexOfValue(field.getName());
                    if (data.fieldIndexes[i]<0)
                    {
                        // Nope: throw an exception
                        throw new KettleStepException("Unable to find the specified fieldname '"+field.getName()+"' for validation#"+(i+1));
                    }
                }
                else
                {
                    throw new KettleStepException("There is no name specified for validator field #"+(i+1));
                }
            }
        }

        if (log.isRowLevel()) log.logRowlevel(toString(), "Read row #"+linesRead+" : "+getInputRowMeta().getString(r));

        try {
        	validateFields(getInputRowMeta(), r);		
        	putRow(getInputRowMeta(), r);     // copy row to possible alternate rowset(s).
        }
        catch(KettleValidatorException e) {
        	if (getStepMeta().isDoingErrorHandling()) {
        		putError(getInputRowMeta(), r, 1, e.getMessage(), e.getFieldname(), e.getCodeDesc());
        	}
        	else {
        		throw new KettleException(e.getMessage(), e);
        	}
        }

        if (log.isRowLevel()) log.logRowlevel(toString(), "Wrote row #"+linesWritten+" : "+r);        
        if (checkFeedback(linesRead)) logBasic("Linenr "+linesRead);

		return true;
	}

	/**
	 * @param inputRowMeta the input row metadata
	 * @param r the input row (data)
	 * @throws KettleValidatorException in case there is a validation error, details are stored in the exception.
	 */
    private void validateFields(RowMetaInterface inputRowMeta, Object[] r) throws KettleValidatorException, KettleValueException
    {
        for (int i=0;i<meta.getValidatorField().length;i++)
        {
            ValidatorField field = meta.getValidatorField()[i];
            
            int valueIndex = data.fieldIndexes[i];
            ValueMetaInterface validatorMeta = data.constantsMeta[i];
            
            ValueMetaInterface valueMeta = inputRowMeta.getValueMeta(valueIndex);
            Object valueData = r[valueIndex];

            // Check for null
            //
            boolean isNull = valueMeta.isNull(valueData);
            if (!field.isNullAllowed() && isNull) {
            	throw new KettleValidatorException(KettleValidatorException.ERROR_NULL_VALUE_NOT_ALLOWED, Messages.getString("Validator.Exception.NullNotAllowed", field.getName(), inputRowMeta.getString(r)), field.getName());
            }
            
            // Check the data type!
            //
            if (field.isDataTypeVerified() && field.getDataType()!=ValueMetaInterface.TYPE_NONE) {
            	
            	// Same data type?
            	//
            	if (field.getDataType() != valueMeta.getType()) {
                	throw new KettleValidatorException(KettleValidatorException.ERROR_UNEXPECTED_DATA_TYPE, Messages.getString("Validator.Exception.UnexpectedDataType", field.getName(), valueMeta.toStringMeta(), validatorMeta.toStringMeta()), field.getName());
            	}
            }
            
            // Check various things if the value is not null..
            //
            if (!isNull) {
            	
            	String stringValue = valueMeta.getString(valueData);
            	
            	// Minimum length
            	//
            	if (field.getMinimumLength()>=0 && stringValue.length()<field.getMinimumLength() ) {
                	throw new KettleValidatorException(KettleValidatorException.ERROR_SHORTER_THAN_MINIMUM_LENGTH, Messages.getString("Validator.Exception.ShorterThanMininumLength", field.getName(), valueMeta.getString(valueData), Integer.toString(stringValue.length()), Integer.toString(field.getMinimumLength())), field.getName());
            	}
            	
            	// Maximum length
            	//
            	if (field.getMaximumLength()>=0 && stringValue.length()>field.getMaximumLength() ) {
                	throw new KettleValidatorException(KettleValidatorException.ERROR_LONGER_THAN_MAXIMUM_LENGTH, Messages.getString("Validator.Exception.LongerThanMaximumLength", field.getName(), valueMeta.getString(valueData), Integer.toString(stringValue.length()), Integer.toString(field.getMaximumLength())), field.getName());
            	}
            	
            	// Minimal value
            	//
            	if (data.minimumValue[i]!=null && valueMeta.compare(valueData, validatorMeta, data.minimumValue[i])<0) {
                	throw new KettleValidatorException(KettleValidatorException.ERROR_LOWER_THAN_ALLOWED_MINIMUM, Messages.getString("Validator.Exception.LowerThanMinimumValue", field.getName(), valueMeta.getString(valueData), data.constantsMeta[i].getString(data.minimumValue[i])), field.getName());
            	}

            	// Maximum value
            	//
            	if (data.maximumValue[i]!=null && valueMeta.compare(valueData, validatorMeta, data.maximumValue[i])>0) {
                	throw new KettleValidatorException(KettleValidatorException.ERROR_HIGHER_THAN_ALLOWED_MAXIMUM, Messages.getString("Validator.Exception.HigherThanMaximumValue", field.getName(), valueMeta.getString(valueData), data.constantsMeta[i].getString(data.maximumValue[i])), field.getName());
            	}
            	
            	// In list?
            	//
            	boolean found = data.listValues[i].length==0;
            	for (Object object : data.listValues[i]) {
                	if (object!=null && data.listValues[i]!=null && valueMeta.compare(valueData, validatorMeta, object)==0) {
                    	found=true;
                	}
            	}
            	if (!found) {
            		throw new KettleValidatorException(KettleValidatorException.ERROR_VALUE_NOT_IN_LIST, Messages.getString("Validator.Exception.NotInList", field.getName(), valueMeta.getString(valueData)), field.getName());
            	}
            }
        }
    }

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ValidatorMeta)smi;
		data=(ValidatorData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.constantsMeta = new ValueMetaInterface[meta.getValidatorField().length];
			data.minimumValue = new Object[meta.getValidatorField().length];
			data.maximumValue = new Object[meta.getValidatorField().length];
			data.listValues = new Object[meta.getValidatorField().length][];

			for (int i=0;i<data.constantsMeta.length;i++) {
				ValidatorField field = meta.getValidatorField()[i];
				data.constantsMeta[i] = new ValueMeta(field.getName(), field.getDataType());
				data.constantsMeta[i].setConversionMask(field.getConversionMask());
				data.constantsMeta[i].setDecimalSymbol(field.getDecimalSymbol());
				data.constantsMeta[i].setGroupingSymbol(field.getGroupingSymbol());
				
				ValueMetaInterface stringMeta = data.constantsMeta[i].clone();
				stringMeta.setType(ValueMetaInterface.TYPE_STRING);
				
				try {
					data.minimumValue[i] = Const.isEmpty(field.getMinimumValue()) ? null : data.constantsMeta[i].convertData(stringMeta, field.getMinimumValue());
					data.maximumValue[i] = Const.isEmpty(field.getMaximumValue()) ? null : data.constantsMeta[i].convertData(stringMeta, field.getMaximumValue());
					int listSize = field.getAllowedValues()!=null ? field.getAllowedValues().length : 0;
					data.listValues[i] = new Object[listSize];
					for (int s=0;s<listSize;s++) {
						data.listValues[i][s] = Const.isEmpty(field.getAllowedValues()[s]) ? null : data.constantsMeta[i].convertData(stringMeta, field.getAllowedValues()[s]);
					}
				} catch (KettleValueException e) {
					if (field.getDataType()==ValueMetaInterface.TYPE_NONE) {
						logError(Messages.getString("Validator.Exception.SpecifyDataType"), e);
					}
					else {
						logError(Messages.getString("Validator.Exception.DataConversionErrorEncountered"), e);
					}
					return false;
				}
			}
			
		    return true;
		}
		return false;
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}