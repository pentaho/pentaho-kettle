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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
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

		Object[] r;
		
        if (first)
        {
            first=false;
            
            readSourceValuesFromInfoSteps();
            
	        // Read the row AFTER the info rows
	        // That way the info-rowsets are out of the way
	        //
			r=getRow();    // get row, set busy!
			if (r==null)  // no more input to be expected...
			{
				setOutputDone();
				return false;
			}

			data.fieldIndexes = new int[meta.getValidations().length];

			// Calculate the indexes of the values and arguments in the target data or temporary data
            // We do this in advance to save time later on.
            //
            for (int i=0;i<meta.getValidations().length;i++)
            {
                Validation field = meta.getValidations()[i];
                
                if (!Const.isEmpty(field.getFieldName())) 
                {
                    data.fieldIndexes[i] = getInputRowMeta().indexOfValue(field.getFieldName());
                    if (data.fieldIndexes[i]<0)
                    {
                        // Nope: throw an exception
                        throw new KettleStepException("Unable to find the specified fieldname '"+field.getFieldName()+"' for validation#"+(i+1));
                    }
                }
                else
                {
                    throw new KettleStepException("There is no name specified for validator field #"+(i+1));
                }
            }
        }
        else
        {
	        // Read the row AFTER the info rows
	        // That way the info-rowsets are out of the way
	        //
			r=getRow();    // get row, set busy!
			if (r==null)  // no more input to be expected...
			{
				setOutputDone();
				return false;
			}
        }

        if (log.isRowLevel()) log.logRowlevel(toString(), "Read row #"+getLinesRead()+" : "+getInputRowMeta().getString(r));

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

        if (log.isRowLevel()) log.logRowlevel(toString(), "Wrote row #"+getLinesWritten()+" : "+getInputRowMeta().getString(r));        
        if (checkFeedback(getLinesRead())) logBasic("Linenr "+getLinesRead());

		return true;
	}

	private void readSourceValuesFromInfoSteps() throws KettleStepException {
        for (int i=0;i<meta.getValidations().length;i++)
        {
            Validation field = meta.getValidations()[i];
            // If we need to source the allowed values data from a different step, we do this here as well
            //
            if (field.isSourcingValues()) {
            	if (field.getSourcingStep()==null) {
            		throw new KettleStepException("There is no valid source step specified for the allowed values of validation ["+field.getName()+"]");
            	}
            	if (Const.isEmpty(field.getSourcingField())) {
            		throw new KettleStepException("There is no valid source field specified for the allowed values of validation ["+field.getName()+"]");
            	}
            	
            	// Still here : OK, read the data from the specified step...
            	// The data is stored in data.listValues[i] and data.constantsMeta
            	//
            	RowSet allowedRowSet = findInputRowSet(field.getSourcingStep().getName()); 
            	int fieldIndex=-1;
            	List<Object> allowedValues = new ArrayList<Object>();
            	Object[] allowedRowData = getRowFrom(allowedRowSet);
            	while (allowedRowData!=null) {
            		RowMetaInterface allowedRowMeta = allowedRowSet.getRowMeta();
            		if (fieldIndex<0) {
            			fieldIndex=allowedRowMeta.indexOfValue(field.getSourcingField());
            			if (fieldIndex<0) {
            				throw new KettleStepException("Source field ["+field.getSourcingField()+"] is not found in the source row data");
            			}
            			data.constantsMeta[i] = allowedRowMeta.getValueMeta(fieldIndex);
            		}
            		Object allowedValue = allowedRowData[fieldIndex];
            		if (allowedValue!=null) {
            			allowedValues.add(allowedValue);
            		}

            		// Grab another row too...
            		//
                	allowedRowData = getRowFrom(allowedRowSet);
            	}
            	// Set the list values in the data block...
            	//
            	data.listValues[i] = allowedValues.toArray(new Object[allowedValues.size()]);
            }
        }
	}

	/**
	 * @param inputRowMeta the input row metadata
	 * @param r the input row (data)
	 * @throws KettleValidatorException in case there is a validation error, details are stored in the exception.
	 */
    private void validateFields(RowMetaInterface inputRowMeta, Object[] r) throws KettleValidatorException, KettleValueException
    {
        for (int i=0;i<meta.getValidations().length;i++)
        {
            Validation field = meta.getValidations()[i];
            
            int valueIndex = data.fieldIndexes[i];
            ValueMetaInterface validatorMeta = data.constantsMeta[i];
            
            ValueMetaInterface valueMeta = inputRowMeta.getValueMeta(valueIndex);
            Object valueData = r[valueIndex];

            // Check for null
            //
            boolean isNull = valueMeta.isNull(valueData);
            if (!field.isNullAllowed() && isNull) {
            	throw new KettleValidatorException(field, KettleValidatorException.ERROR_NULL_VALUE_NOT_ALLOWED, Messages.getString("Validator.Exception.NullNotAllowed", field.getFieldName(), inputRowMeta.getString(r)), field.getFieldName());
            }
            
            if (field.isOnlyNullAllowed() && !isNull) {
            	throw new KettleValidatorException(field, KettleValidatorException.ERROR_ONLY_NULL_VALUE_ALLOWED, Messages.getString("Validator.Exception.OnlyNullAllowed", field.getFieldName(), inputRowMeta.getString(r)), field.getFieldName());
            }
            
            // Check if the field is either numeric, a date or a string containing only digits...
            //
            if (field.isOnlyNumericAllowed()) {
            }
            
            // Check the data type!
            //
            if (field.isDataTypeVerified() && field.getDataType()!=ValueMetaInterface.TYPE_NONE) {
            	
            	// Same data type?
            	//
            	if (field.getDataType() != valueMeta.getType()) {
                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_UNEXPECTED_DATA_TYPE, Messages.getString("Validator.Exception.UnexpectedDataType", field.getFieldName(), valueMeta.toStringMeta(), validatorMeta.toStringMeta()), field.getFieldName());
            	}
            }
            
            // Check various things if the value is not null..
            //
            if ( !isNull) {
            	 
            	if (field.getMinimumLength()>=0 || 
            		field.getMaximumLength()>=0 || 
            		data.minimumValue[i]!=null || 
            		data.maximumValue[i]!=null || 
            		data.listValues[i].length>0 ||
            		!Const.isEmpty(field.getStartString()) ||
            		!Const.isEmpty(field.getEndString()) ||
            		!Const.isEmpty(field.getStartStringNotAllowed()) ||
            		!Const.isEmpty(field.getStartStringNotAllowed()) ||
            		field.isOnlyNumericAllowed() ||
            		data.patternExpected[i]!=null ||
            		data.patternDisallowed[i]!=null
            		) {
            	
	            	String stringValue = valueMeta.getString(valueData);
	            	
	            	// Minimum length
	            	//
	            	if (field.getMinimumLength()>=0 && stringValue.length()<field.getMinimumLength() ) {
	                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_SHORTER_THAN_MINIMUM_LENGTH, Messages.getString("Validator.Exception.ShorterThanMininumLength", field.getFieldName(), valueMeta.getString(valueData), Integer.toString(stringValue.length()), Integer.toString(field.getMinimumLength())), field.getFieldName());
	            	}
	            	
	            	// Maximum length
	            	//
	            	if (field.getMaximumLength()>=0 && stringValue.length()>field.getMaximumLength() ) {
	                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_LONGER_THAN_MAXIMUM_LENGTH, Messages.getString("Validator.Exception.LongerThanMaximumLength", field.getFieldName(), valueMeta.getString(valueData), Integer.toString(stringValue.length()), Integer.toString(field.getMaximumLength())), field.getFieldName());
	            	}
	            	
	            	// Minimal value
	            	//
	            	if (data.minimumValue[i]!=null && valueMeta.compare(valueData, validatorMeta, data.minimumValue[i])<0) {
	                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_LOWER_THAN_ALLOWED_MINIMUM, Messages.getString("Validator.Exception.LowerThanMinimumValue", field.getFieldName(), valueMeta.getString(valueData), data.constantsMeta[i].getString(data.minimumValue[i])), field.getFieldName());
	            	}
	
	            	// Maximum value
	            	//
	            	if (data.maximumValue[i]!=null && valueMeta.compare(valueData, validatorMeta, data.maximumValue[i])>0) {
	                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_HIGHER_THAN_ALLOWED_MAXIMUM, Messages.getString("Validator.Exception.HigherThanMaximumValue", field.getFieldName(), valueMeta.getString(valueData), data.constantsMeta[i].getString(data.maximumValue[i])), field.getFieldName());
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
	            		throw new KettleValidatorException(field, KettleValidatorException.ERROR_VALUE_NOT_IN_LIST, Messages.getString("Validator.Exception.NotInList", field.getFieldName(), valueMeta.getString(valueData)), field.getFieldName());
	            	}
	            	
	            	// Numeric data or strings with only 
		            if (field.isOnlyNumericAllowed()) {
		            	if (valueMeta.isNumeric() || !containsOnlyDigits(valueMeta.getString(valueData)) ) {
		            		throw new KettleValidatorException(field, KettleValidatorException.ERROR_NON_NUMERIC_DATA, Messages.getString("Validator.Exception.NonNumericDataNotAllowed", field.getFieldName(), valueMeta.toStringMeta()), field.getFieldName());
		            	}
		            }
	            	
	            	// Does not start with string value
	            	//
	            	if (!Const.isEmpty(field.getStartString()) && !stringValue.startsWith(field.getStartString())) {
	                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_DOES_NOT_START_WITH_STRING, Messages.getString("Validator.Exception.DoesNotStartWithString", field.getFieldName(), valueMeta.getString(valueData), field.getStartString()), field.getFieldName());
	            	}
	
	            	// Ends with string value
	            	//
	            	if (!Const.isEmpty(field.getEndString()) && !stringValue.endsWith(field.getEndString())) {
	                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_DOES_NOT_END_WITH_STRING, Messages.getString("Validator.Exception.DoesNotStartWithString", field.getFieldName(), valueMeta.getString(valueData), field.getEndString()), field.getFieldName());
	            	}
	
	            	// Starts with string value
	            	//
	            	if (!Const.isEmpty(field.getStartStringNotAllowed()) && stringValue.startsWith(field.getStartStringNotAllowed())) {
	                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_STARTS_WITH_STRING, Messages.getString("Validator.Exception.StartsWithString", field.getFieldName(), valueMeta.getString(valueData), field.getStartStringNotAllowed()), field.getFieldName());
	            	}
	
	            	// Ends with string value
	            	//
	            	if (!Const.isEmpty(field.getEndStringNotAllowed()) && !stringValue.endsWith(field.getEndStringNotAllowed())) {
	                	throw new KettleValidatorException(field, KettleValidatorException.ERROR_ENDS_WITH_STRING, Messages.getString("Validator.Exception.EndsWithString", field.getFieldName(), valueMeta.getString(valueData), field.getEndStringNotAllowed()), field.getFieldName());
	            	}

	            	// Matching regular expression allowed?
	            	//
	            	if (data.patternExpected[i]!=null) {
	            		Matcher matcher = data.patternExpected[i].matcher(stringValue);
	            		if (!matcher.matches()) {
	            			throw new KettleValidatorException(field, KettleValidatorException.ERROR_MATCHING_REGULAR_EXPRESSION_EXPECTED, Messages.getString("Validator.Exception.MatchingRegExpExpected", field.getFieldName(), valueMeta.getString(valueData), field.getRegularExpression()), field.getFieldName());
	            		}
	            	}

	            	// Matching regular expression NOT allowed?
	            	//
	            	if (data.patternDisallowed[i]!=null) {
	            		Matcher matcher = data.patternDisallowed[i].matcher(stringValue);
	            		if (matcher.matches()) {
	            			throw new KettleValidatorException(field, KettleValidatorException.ERROR_MATCHING_REGULAR_EXPRESSION_NOT_ALLOWED, Messages.getString("Validator.Exception.MatchingRegExpNotAllowed", field.getFieldName(), valueMeta.getString(valueData), field.getRegularExpressionNotAllowed()), field.getFieldName());
	            		}
	            	}

            	}
            }
        }
    }

	private boolean containsOnlyDigits(String string) {
		for (char c : string.toCharArray()) {
			if (c<'0' || c>'9') return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ValidatorMeta)smi;
		data=(ValidatorData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.constantsMeta = new ValueMetaInterface[meta.getValidations().length];
			data.minimumValue = new Object[meta.getValidations().length];
			data.maximumValue = new Object[meta.getValidations().length];
			data.listValues = new Object[meta.getValidations().length][];
			data.patternExpected = new Pattern[meta.getValidations().length];
			data.patternDisallowed = new Pattern[meta.getValidations().length];

			for (int i=0;i<meta.getValidations().length;i++) {
				Validation field = meta.getValidations()[i];
				data.constantsMeta[i] = new ValueMeta(field.getFieldName(), field.getDataType());
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
				
				if (!Const.isEmpty(field.getRegularExpression())) {
					data.patternExpected[i] = Pattern.compile(field.getRegularExpression());
				}
				if (!Const.isEmpty(field.getRegularExpressionNotAllowed())) {
					data.patternDisallowed[i] = Pattern.compile(field.getRegularExpressionNotAllowed());
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