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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;


/**
 * Calculate new field values using pre-defined functions. 
 * 
 * @author Matt
 * @since 8-sep-2005
 */
public class Validator extends BaseStep implements StepInterface
{
	private static Class<?> PKG = ValidatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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

			data.fieldIndexes = new int[meta.getValidations().size()];

			// Calculate the indexes of the values and arguments in the target data or temporary data
            // We do this in advance to save time later on.
            //
            for (int i=0;i<meta.getValidations().size();i++)
            {
                Validation field = meta.getValidations().get(i);
                
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

        if (log.isRowLevel()) logRowlevel("Read row #"+getLinesRead()+" : "+getInputRowMeta().getString(r));

        try {
        	List<KettleValidatorException> exceptions = validateFields(getInputRowMeta(), r);
        	if (exceptions.size()>0) {
        		if (getStepMeta().isDoingErrorHandling()) {
            		if (meta.isConcatenatingErrors()) {
            			StringBuffer messages=new StringBuffer();
            			StringBuffer fields = new StringBuffer();
            			StringBuffer codes = new StringBuffer();
            			boolean notFirst=false;
	            		for (KettleValidatorException e: exceptions) {
	            			if (notFirst) {
	            				messages.append(meta.getConcatenationSeparator());
	            				fields.append(meta.getConcatenationSeparator());
	            				codes.append(meta.getConcatenationSeparator());
	            			} else {
	            				notFirst=true;
	            			}
            				messages.append(e.getMessage());
            				fields.append(e.getFieldname());
            				codes.append(e.getCodeDesc());
	            		}
	            		putError(getInputRowMeta(), r, exceptions.size(), messages.toString(), fields.toString(), codes.toString());
            		} else {
	            		for (KettleValidatorException e: exceptions) {
	                		putError(getInputRowMeta(), r, 1, e.getMessage(), e.getFieldname(), e.getCodeDesc());
	            		}
            		}
            	} else {
            		KettleValidatorException e = exceptions.get(0);
            		throw new KettleException(e.getMessage(), e);
            	}
        	} else {
        		putRow(getInputRowMeta(), r);     // copy row to possible alternate rowset(s).
        	}
        }
        catch(KettleValidatorException e) {
        	if (getStepMeta().isDoingErrorHandling()) {
        		putError(getInputRowMeta(), r, 1, e.getMessage(), e.getFieldname(), e.getCodeDesc());
        	}
        	else {
        		throw new KettleException(e.getMessage(), e);
        	}
        }

        if (log.isRowLevel()) logRowlevel("Wrote row #"+getLinesWritten()+" : "+getInputRowMeta().getString(r));        
        if (checkFeedback(getLinesRead())) logBasic("Linenr "+getLinesRead());

		return true;
	}

	private void readSourceValuesFromInfoSteps() throws KettleStepException {
        for (int i=0;i<meta.getValidations().size();i++)
        {
            Validation field = meta.getValidations().get(i);
            List<StreamInterface> streams = meta.getStepIOMeta().getInfoStreams();
            
            // If we need to source the allowed values data from a different step, we do this here as well
            //
            if (field.isSourcingValues()) {
            	if (streams.get(i).getStepMeta()==null) {
            		throw new KettleStepException("There is no valid source step specified for the allowed values of validation ["+field.getName()+"]");
            	}
            	if (Const.isEmpty(field.getSourcingField())) {
            		throw new KettleStepException("There is no valid source field specified for the allowed values of validation ["+field.getName()+"]");
            	}
            	
            	// Still here : OK, read the data from the specified step...
            	// The data is stored in data.listValues[i] and data.constantsMeta
            	//
            	RowSet allowedRowSet = findInputRowSet(streams.get(i).getStepname()); 
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
    private List<KettleValidatorException> validateFields(RowMetaInterface inputRowMeta, Object[] r) throws KettleValidatorException, KettleValueException
    {
    	List<KettleValidatorException> exceptions = new ArrayList<KettleValidatorException>();
    	
        for (int i=0;i<meta.getValidations().size();i++)
        {
            Validation field = meta.getValidations().get(i);
            
            int valueIndex = data.fieldIndexes[i];
            ValueMetaInterface validatorMeta = data.constantsMeta[i];
            
            ValueMetaInterface valueMeta = inputRowMeta.getValueMeta(valueIndex);
            Object valueData = r[valueIndex];

            // Check for null
            //
            boolean isNull = valueMeta.isNull(valueData);
            if (!field.isNullAllowed() && isNull) {
            	KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_NULL_VALUE_NOT_ALLOWED, BaseMessages.getString(PKG, "Validator.Exception.NullNotAllowed", field.getFieldName(), inputRowMeta.getString(r)), field.getFieldName());
            	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
            }
            
            if (field.isOnlyNullAllowed() && !isNull) {
            	KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_ONLY_NULL_VALUE_ALLOWED, BaseMessages.getString(PKG, "Validator.Exception.OnlyNullAllowed", field.getFieldName(), inputRowMeta.getString(r)), field.getFieldName());
            	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
            }
            
            // Check the data type!
            //
            if (field.isDataTypeVerified() && field.getDataType()!=ValueMetaInterface.TYPE_NONE) {
            	
            	// Same data type?
            	//
            	if (field.getDataType() != valueMeta.getType()) {
            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_UNEXPECTED_DATA_TYPE, BaseMessages.getString(PKG, "Validator.Exception.UnexpectedDataType", field.getFieldName(), valueMeta.toStringMeta(), validatorMeta.toStringMeta()), field.getFieldName());
                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
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
            		field.isSourcingValues() ||
            		!Const.isEmpty(field.getStartString()) ||
            		!Const.isEmpty(field.getEndString()) ||
            		!Const.isEmpty(field.getStartStringNotAllowed()) ||
            		!Const.isEmpty(field.getEndStringNotAllowed()) ||
            		field.isOnlyNumericAllowed() ||
            		data.patternExpected[i]!=null ||
            		data.patternDisallowed[i]!=null
            		) {
            	
	            	String stringValue = valueMeta.getString(valueData);
	            	
	            	// Minimum length
	            	//
	            	if (field.getMinimumLength()>=0 && stringValue.length()<field.getMinimumLength() ) {
	            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_SHORTER_THAN_MINIMUM_LENGTH, BaseMessages.getString(PKG, "Validator.Exception.ShorterThanMininumLength", field.getFieldName(), valueMeta.getString(valueData), Integer.toString(stringValue.length()), Integer.toString(field.getMinimumLength())), field.getFieldName());
	                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	}
	            	
	            	// Maximum length
	            	//
	            	if (field.getMaximumLength()>=0 && stringValue.length()>field.getMaximumLength() ) {
	            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_LONGER_THAN_MAXIMUM_LENGTH, BaseMessages.getString(PKG, "Validator.Exception.LongerThanMaximumLength", field.getFieldName(), valueMeta.getString(valueData), Integer.toString(stringValue.length()), Integer.toString(field.getMaximumLength())), field.getFieldName());
	                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	}
	            	
	            	// Minimal value
	            	//
	            	if (data.minimumValue[i]!=null && valueMeta.compare(valueData, validatorMeta, data.minimumValue[i])<0) {
	            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_LOWER_THAN_ALLOWED_MINIMUM, BaseMessages.getString(PKG, "Validator.Exception.LowerThanMinimumValue", field.getFieldName(), valueMeta.getString(valueData), data.constantsMeta[i].getString(data.minimumValue[i])), field.getFieldName());
	                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	}
	
	            	// Maximum value
	            	//
	            	if (data.maximumValue[i]!=null && valueMeta.compare(valueData, validatorMeta, data.maximumValue[i])>0) {
	            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_HIGHER_THAN_ALLOWED_MAXIMUM, BaseMessages.getString(PKG, "Validator.Exception.HigherThanMaximumValue", field.getFieldName(), valueMeta.getString(valueData), data.constantsMeta[i].getString(data.maximumValue[i])), field.getFieldName());
	                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	}
	            	
	            	// In list?
	            	//
	            	if (field.isSourcingValues() || data.listValues[i].length > 0) {
	            	    boolean found = false;
	            	    for (Object object : data.listValues[i]) {
	            	        if (object!=null && data.listValues[i]!=null && valueMeta.compare(valueData, validatorMeta, object)==0) {
	            	            found=true;
	            	        }
	            	    }
	            	    if (!found) {
	            	        KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_VALUE_NOT_IN_LIST, BaseMessages.getString(PKG, "Validator.Exception.NotInList", field.getFieldName(), valueMeta.getString(valueData)), field.getFieldName());
	            	        if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	    }
	            	}

	            	// Numeric data or strings with only 
		            if (field.isOnlyNumericAllowed()) {
		            	if (valueMeta.isNumeric() || !containsOnlyDigits(valueMeta.getString(valueData)) ) {
		            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_NON_NUMERIC_DATA, BaseMessages.getString(PKG, "Validator.Exception.NonNumericDataNotAllowed", field.getFieldName(), valueMeta.toStringMeta()), field.getFieldName());
		                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
		            	}
		            }
	            	
	            	// Does not start with string value
	            	//
	            	if (!Const.isEmpty(field.getStartString()) && !stringValue.startsWith(field.getStartString())) {
	            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_DOES_NOT_START_WITH_STRING, BaseMessages.getString(PKG, "Validator.Exception.DoesNotStartWithString", field.getFieldName(), valueMeta.getString(valueData), field.getStartString()), field.getFieldName());
	                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	}
	
	            	// Ends with string value
	            	//
	            	if (!Const.isEmpty(field.getEndString()) && !stringValue.endsWith(field.getEndString())) {
	            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_DOES_NOT_END_WITH_STRING, BaseMessages.getString(PKG, "Validator.Exception.DoesNotStartWithString", field.getFieldName(), valueMeta.getString(valueData), field.getEndString()), field.getFieldName());
	                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	}
	
	            	// Starts with string value
	            	//
	            	if (!Const.isEmpty(field.getStartStringNotAllowed()) && stringValue.startsWith(field.getStartStringNotAllowed())) {
	            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_STARTS_WITH_STRING, BaseMessages.getString(PKG, "Validator.Exception.StartsWithString", field.getFieldName(), valueMeta.getString(valueData), field.getStartStringNotAllowed()), field.getFieldName());
	                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	}
	
	            	// Ends with string value
	            	//
	            	if (!Const.isEmpty(field.getEndStringNotAllowed()) && stringValue.endsWith(field.getEndStringNotAllowed())) {
	            		KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_ENDS_WITH_STRING, BaseMessages.getString(PKG, "Validator.Exception.EndsWithString", field.getFieldName(), valueMeta.getString(valueData), field.getEndStringNotAllowed()), field.getFieldName());
	                	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            	}

	            	// Matching regular expression allowed?
	            	//
	            	if (data.patternExpected[i]!=null) {
	            		Matcher matcher = data.patternExpected[i].matcher(stringValue);
	            		if (!matcher.matches()) {
	            			KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_MATCHING_REGULAR_EXPRESSION_EXPECTED, BaseMessages.getString(PKG, "Validator.Exception.MatchingRegExpExpected", field.getFieldName(), valueMeta.getString(valueData), field.getRegularExpression()), field.getFieldName());
	                    	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            		}
	            	}

	            	// Matching regular expression NOT allowed?
	            	//
	            	if (data.patternDisallowed[i]!=null) {
	            		Matcher matcher = data.patternDisallowed[i].matcher(stringValue);
	            		if (matcher.matches()) {
	            			KettleValidatorException exception = new KettleValidatorException(field, KettleValidatorException.ERROR_MATCHING_REGULAR_EXPRESSION_NOT_ALLOWED, BaseMessages.getString(PKG, "Validator.Exception.MatchingRegExpNotAllowed", field.getFieldName(), valueMeta.getString(valueData), field.getRegularExpressionNotAllowed()), field.getFieldName());
	                    	if (meta.isValidatingAll()) exceptions.add(exception); else throw exception;
	            		}
	            	}

            	}
            }
        }
        
        return exceptions;
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
			data.constantsMeta = new ValueMetaInterface[meta.getValidations().size()];
			data.minimumValue = new Object[meta.getValidations().size()];
			data.maximumValue = new Object[meta.getValidations().size()];
			data.listValues = new Object[meta.getValidations().size()][];
			data.patternExpected = new Pattern[meta.getValidations().size()];
			data.patternDisallowed = new Pattern[meta.getValidations().size()];

			for (int i=0;i<meta.getValidations().size();i++) {
				Validation field = meta.getValidations().get(i);
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
						logError(BaseMessages.getString(PKG, "Validator.Exception.SpecifyDataType"), e);
					}
					else {
						logError(BaseMessages.getString(PKG, "Validator.Exception.DataConversionErrorEncountered"), e);
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

}