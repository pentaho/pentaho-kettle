/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Ronny Roeller.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.numberrange;

import java.util.List;
import org.pentaho.di.core.exception.KettleException;

/**
 * This class assigns numbers into ranges
 * 
 * @author ronny.roeller@fredhopper.com
 * 
 */
public class NumberRangeSet 
 {
	 
	 public static final String MULTI_VALUE_SEPARATOR = ",";

		/**
	 * List of all rules that have to be considered
	 */
	 	private List<NumberRangeRule> rules;
	 
	 	/**
	 * Value that is returned if no rule matches
	 */
	 	private String fallBackValue;
	 
	 	public NumberRangeSet(List<NumberRangeRule> rules, String fallBackValue) 
	    {
		 		this.rules = rules;
		 		this.fallBackValue = fallBackValue;
		 }
	 
	 	/**
	 * Evaluates a value against all rules
	 */
	 	protected String evaluateDouble(double value) 
		{
			StringBuffer result = new StringBuffer();
	
				// Execute all rules
				for (NumberRangeRule rule : rules) 
				{
					String ruleResult = rule.evaluate(value);
		
						// If rule matched -> add value to the result
						if (ruleResult != null) 
						{
							// Add value separator if multiple values are available
								if (result.length() > 0)
									result.append(getMultiValueSeparator());
			
								result.append(ruleResult);
						}
				}
	
				return result.toString();
		}
	 
	 	/**
	 * Returns separator that is added if a value matches multiple ranges.
	 */
	 	public static String getMultiValueSeparator() 
		{
			return MULTI_VALUE_SEPARATOR;
		}
	 
	 	/**
	 * Evaluates a value against all rules. Return empty value if input is not
	 * numeric.
	 */
	 	public String evaluate(String strValue) throws KettleException
		{
			if (strValue != null) 
			{
				// Try to parse value to double
				try 
				{
					double doubleValue = Double.parseDouble(strValue);
					return evaluate(doubleValue);
				} 
				catch (Exception e) 
				{
					throw new KettleException(e);
				}
			}
			return fallBackValue;
		}
	 	
	 	/**
	 	 * Evaluates a value against all rules. Return empty value if input is not
		 * numeric.
		 */
		 	public String evaluate(Double value) throws KettleException
			{
				String result = evaluateDouble(value);
			    if (!"".equals(result))	return result;
			    
				return fallBackValue;
			}
	 
	 }