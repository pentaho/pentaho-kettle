/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * This class evaluates strings and extracts a data type.
 * It allows you to criteria after which the analysis should be completed.
 *  
 * @author matt
 *
 */
public class StringEvaluator {

	private Set<String> values;
	private List<StringEvaluationResult> evaluationResults;
	private int maxLength;
	private int count;
	private boolean tryTrimming;
	
	private ValueMetaInterface stringMeta;
	
	public StringEvaluator(boolean tryTrimming) {
		this.tryTrimming = tryTrimming;
		
		values = new HashSet<String>();
		evaluationResults = new ArrayList<StringEvaluationResult>();
		count=0;
		
		stringMeta = new ValueMeta("string", ValueMetaInterface.TYPE_STRING);
		populateConversionMetaList();
	}
	
	public void evaluateString(String value) {
		count++;

		if (!values.contains(value)) {
			values.add(value);
			
			if(value != null) {
  			evaluateLength(value);
  			
  			challengeConversions(value);
			}
		}
	}
	
	private void challengeConversions(String value) {
		List<StringEvaluationResult> all = new ArrayList<StringEvaluationResult>(evaluationResults);
		for (StringEvaluationResult cmm : all) {
			if (cmm.getConversionMeta().isBoolean()) {
				// Boolean conversion never fails.
				// If it's a Y, N, true, false it's a boolean otherwise it ain't.
				//
				String string;
				if (tryTrimming) {
					string=Const.trim(value);
				} else {
					string=value;
				}
				if (!("Y".equalsIgnoreCase(string) || "N".equalsIgnoreCase(string) || "TRUE".equalsIgnoreCase(string) || "FALSE".equalsIgnoreCase(string))) {
					evaluationResults.remove(cmm);
				}				
			} else {
				try {
					if (cmm.getConversionMeta().isNumeric()) {
						boolean stop=false;
						int nrDots=0;
						int nrCommas=0;
						for (char c : value.toCharArray()) {
							if (!Character.isDigit(c) && c!='.' && c!=',' && !Character.isSpaceChar(c)) {
								evaluationResults.remove(cmm);
								stop=true;
								break;
							}
							
							// If the value contains a decimal or grouping symbol or some sort, it's not an integer
							//
							if ((c=='.' && cmm.getConversionMeta().isInteger()) || 
							    (c==',' && cmm.getConversionMeta().isInteger())) {
								evaluationResults.remove(cmm);
								stop=true;
								break;
							}
							if (c=='.') nrDots++;
							if (c==',') nrCommas++;
						}
						
						if (nrDots>1 && nrCommas>1) {
							evaluationResults.remove(cmm);
							stop=true;
						}
						
						if (stop) {
							continue;
						}

					}
					ValueMetaInterface meta = stringMeta.clone();
					meta.setConversionMetadata(cmm.getConversionMeta());
					meta.setTrimType(cmm.getConversionMeta().getTrimType());
					Object object = meta.convertDataUsingConversionMetaData(value);
					
					if (cmm.getConversionMeta().isNumeric() || cmm.getConversionMeta().isDate()) {
						// Convert back to a string to see if we lost any precision!
						//
						if (!cmm.getConversionMeta().isNull(object)) {
							String compare = cmm.getConversionMeta().getString(object);
							switch(cmm.getConversionMeta().getTrimType()) {
							case ValueMetaInterface.TRIM_TYPE_NONE:
								if (!compare.equalsIgnoreCase(value)) {
									evaluationResults.remove(cmm);
									continue;
								}
								break;
							case ValueMetaInterface.TRIM_TYPE_LEFT:
								if (!compare.equalsIgnoreCase(Const.ltrim(value))) {
									evaluationResults.remove(cmm);
									continue;
								}
								break;
							case ValueMetaInterface.TRIM_TYPE_RIGHT:
								if (!compare.equalsIgnoreCase(Const.rtrim(value))) {
									evaluationResults.remove(cmm);
									continue;
								}
								break;
							case ValueMetaInterface.TRIM_TYPE_BOTH:
								if (!compare.equalsIgnoreCase(Const.trim(value))) {
									evaluationResults.remove(cmm);
									continue;
								}
								break;
							}
						}
					}
					
					// Still here?  Evaluate the data...
					// Keep track of null values, min, max, etc.
					//
					if (cmm.getConversionMeta().isNull(object)) {
						cmm.incrementNrNull();
					}

					if (cmm.getMin()==null || cmm.getConversionMeta().compare(cmm.getMin(), object)>0) {
						cmm.setMin(object);
					}
					if (cmm.getMax()==null || cmm.getConversionMeta().compare(cmm.getMax(), object)<0) {
						cmm.setMax(object);
					}
					
				} catch(KettleValueException e) {
					// This one doesn't work, remove it from the list!
					//
					evaluationResults.remove(cmm);
				}
			}
		}
	}

	private void evaluateLength(String value) {
		if (value.length()>maxLength) {
			maxLength=value.length();
		}
	}
	
	private boolean containsInteger() {
		for (StringEvaluationResult result : evaluationResults) {
			if (result.getConversionMeta().isInteger()) return true;
		}
		return false;
	}

	private boolean containsNumber() {
		for (StringEvaluationResult result : evaluationResults) {
			if (result.getConversionMeta().isNumber()) return true;
		}
		return false;
	}

	private boolean containsDate() {
		for (StringEvaluationResult result : evaluationResults) {
			if (result.getConversionMeta().isDate()) return true;
		}
		return false;
	}

	public StringEvaluationResult getAdvicedResult() {
		if (evaluationResults.isEmpty()) {
			ValueMetaInterface adviced = new ValueMeta("adviced", ValueMetaInterface.TYPE_STRING);
			adviced.setLength(maxLength);
			int nrNulls = 0;
			String min=null;
			String max=null;
			for (String string : values) {
			  if(string != null) {
    			if (min==null || min.compareTo(string)>0) min=string;
    			if (max==null || max.compareTo(string)<0) max=string;
			  } else {
			    nrNulls++;
			  }
			}
			
			StringEvaluationResult result = new StringEvaluationResult(adviced);
			result.setNrNull(nrNulls);
			result.setMin(min);
			result.setMax(max);
			return result;
			
		} else {
			// If there are Numbers and Integers, pick the integers...
			//
			if (containsInteger() && containsNumber()) {
				for (Iterator<StringEvaluationResult> iterator = evaluationResults.iterator(); iterator.hasNext();) {
					StringEvaluationResult result = iterator.next();
					if (result.getConversionMeta().isNumber()) {
						iterator.remove();
					}
				}
			}
			// If there are Dates and Integers, pick the dates...
			//
			if (containsInteger() && containsDate()) {
				for (Iterator<StringEvaluationResult> iterator = evaluationResults.iterator(); iterator.hasNext();) {
					StringEvaluationResult result = iterator.next();
					if (result.getConversionMeta().isInteger()) {
						iterator.remove();
					}
				}
			}
			
			// Select the one with the shortest format mask.
			//
			int minIndex=0;
			int minLength=Integer.MAX_VALUE;
			for (int i=0;i<evaluationResults.size();i++) {
				StringEvaluationResult result = evaluationResults.get(i);
				ValueMetaInterface conversionMeta = result.getConversionMeta();
				if (!Const.isEmpty(conversionMeta.getConversionMask()) && !conversionMeta.isBoolean()) {
					int maskLength = conversionMeta.getConversionMask().length();
					if (minLength>maskLength) {
						minLength=maskLength;
						minIndex=i;
					}
				}
			}
			
			return evaluationResults.get(minIndex);
		}
		
	}

	private void populateConversionMetaList() {
		
		int[] trimTypes;
		if (tryTrimming) {
			trimTypes=new int[] { ValueMetaInterface.TRIM_TYPE_NONE, ValueMetaInterface.TRIM_TYPE_BOTH, };  
		} else {
			trimTypes=new int[] { ValueMetaInterface.TRIM_TYPE_NONE, };
		}
		
		for (int trimType : trimTypes) {
			for (String format : Const.getDateFormats()) {
				ValueMetaInterface conversionMeta = new ValueMeta("date", ValueMetaInterface.TYPE_DATE);
				conversionMeta.setConversionMask(format);
				conversionMeta.setTrimType(trimType);
				conversionMeta.setDateFormatLenient(false);
				evaluationResults.add(new StringEvaluationResult(conversionMeta));
			}
	
			final String[] numberFormats = new String[] {
				"#,###,###.#",
				"#.#",
				" #.#",
				"#",
				"#.0",
				"#.00",
				"#.000",
				"#.0000",
				"#.00000",
				"#.000000",
                " #.0#",
			};
			
			for (String format : numberFormats) {
				ValueMetaInterface conversionMeta = new ValueMeta("number-us", ValueMetaInterface.TYPE_NUMBER);
				conversionMeta.setConversionMask(format);
				conversionMeta.setTrimType(trimType);
				conversionMeta.setDecimalSymbol(".");
				conversionMeta.setGroupingSymbol(",");
				conversionMeta.setLength(-1);
				conversionMeta.setPrecision(-1);
				evaluationResults.add(new StringEvaluationResult(conversionMeta));
				
				conversionMeta = new ValueMeta("number-eu", ValueMetaInterface.TYPE_NUMBER);
				conversionMeta.setConversionMask(format);
				conversionMeta.setTrimType(trimType);
				conversionMeta.setDecimalSymbol(",");
				conversionMeta.setGroupingSymbol(".");
				conversionMeta.setLength(-1);
				conversionMeta.setPrecision(-1);
				evaluationResults.add(new StringEvaluationResult(conversionMeta));
			}
				
			// Integer
			//
			ValueMetaInterface conversionMeta = new ValueMeta("integer", ValueMetaInterface.TYPE_INTEGER);
			conversionMeta.setConversionMask("#");
			conversionMeta.setLength(15);
			evaluationResults.add(new StringEvaluationResult(conversionMeta));

			conversionMeta = new ValueMeta("integer", ValueMetaInterface.TYPE_INTEGER);
			conversionMeta.setConversionMask(" #");
			conversionMeta.setLength(15);
			evaluationResults.add(new StringEvaluationResult(conversionMeta));

	         // Add support for left zero padded integers
            //
            for (int i=1;i<=15;i++) {

              String mask = " ";
              for (int x=0;x<i;x++) mask+="0";
              mask+=";-";
              for (int x=0;x<i;x++) mask+="0";
              
              conversionMeta = new ValueMeta("integer-zero-padded-"+i, ValueMetaInterface.TYPE_INTEGER);
              conversionMeta.setConversionMask(mask);
              conversionMeta.setLength(i);
              evaluationResults.add(new StringEvaluationResult(conversionMeta));
            }

			// Boolean
			//
			conversionMeta = new ValueMeta("boolean", ValueMetaInterface.TYPE_BOOLEAN);
			evaluationResults.add(new StringEvaluationResult(conversionMeta));		
		}
	}



	/**
	 * @return The distinct set of string values
	 */
	public Set<String> getValues() {
		return values;
	}
	
	/**
	 * @return The list of string evaluation results
	 */
	public List<StringEvaluationResult> getStringEvaluationResults() {
		return evaluationResults;
	}
	
	/**
	 * @return the number of values analyzed
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return The maximum string length encountered
	 */
	public int getMaxLength() {
		return maxLength;
	}
}
