package org.pentaho.di.core.util;

import java.util.ArrayList;
import java.util.HashSet;
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

	private static String[] NUMBER_FORMATS = new String[] {
		"###############.##############",
		"#,###,###,###,###,###.############",
		"0000000000000",
		"000000000000",
		"00000000000",
		"0000000000",
		"000000000",
		"00000000",
		"0000000",
		"000000",
		"00000",
		"0000",
		"000",
		"00",
		"0",
		"#.0",
		"#.00",
		"#.000",
		"#.0000",
		"#.00000",
		"#.000000",
		"#.0000000",
		"#.00000000",
		"#.000000000",
		"#.0000000000",
		"#.00000000000",
		"00.0",
		"00.00",
		"00.000",
		"00.0000",
		"00.00000",
		"00.000000",
		"00.0000000",
		"00.00000000",
		"00.000000000",
		"00.0000000000",
		"00.00000000000",
		"000.0",
		"000.00",
		"000.000",
		"000.0000",
		"000.00000",
		"000.000000",
		"000.0000000",
		"000.00000000",
		"000.000000000",
		"000.0000000000",
		"000.00000000000",
		"0000.0",
		"0000.00",
		"0000.000",
		"0000.0000",
		"0000.00000",
		"0000.000000",
		"0000.0000000",
		"0000.00000000",
		"0000.000000000",
		"0000.0000000000",
		"0000.00000000000",
		"00000.0",
		"00000.00",
		"00000.000",
		"00000.0000",
		"00000.00000",
		"00000.000000",
		"00000.0000000",
		"00000.00000000",
		"00000.000000000",
		"00000.0000000000",
		"00000.00000000000",
		"000000.0",
		"000000.00",
		"000000.000",
		"000000.0000",
		"000000.00000",
		"000000.000000",
		"000000.0000000",
		"000000.00000000",
		"000000.000000000",
		"000000.0000000000",
		"000000.00000000000",
		
	};

	
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
			
			evaluateLength(value);
			
			challengeConversions(value);
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
	
			for (String format : NUMBER_FORMATS) {
				ValueMetaInterface conversionMeta = new ValueMeta("number-us", ValueMetaInterface.TYPE_NUMBER);
				conversionMeta.setConversionMask(format);
				conversionMeta.setTrimType(trimType);
				conversionMeta.setDecimalSymbol(".");
				conversionMeta.setGroupingSymbol(",");
				evaluationResults.add(new StringEvaluationResult(conversionMeta));
				
				conversionMeta = new ValueMeta("number-eu", ValueMetaInterface.TYPE_NUMBER);
				conversionMeta.setConversionMask(format);
				conversionMeta.setTrimType(trimType);
				conversionMeta.setDecimalSymbol(",");
				conversionMeta.setGroupingSymbol(".");
				evaluationResults.add(new StringEvaluationResult(conversionMeta));
			}
	
			// Integer
			//
			ValueMetaInterface conversionMeta = new ValueMeta("integer", ValueMetaInterface.TYPE_INTEGER);
			conversionMeta.setConversionMask("#");
			evaluationResults.add(new StringEvaluationResult(conversionMeta));

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
