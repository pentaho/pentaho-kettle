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

/**
 * Contains one rule for a number range
 * 
 * @author ronny.roeller@fredhopper.com
 * 
 */
public class NumberRangeRule 
 {
	 
	 	/**
	 * Lower bound for which the rule matches (lowerBound <= x)
	 */
	 	private double lowerBound;
	 
	 	/**
	 * Upper bound for which the rule matches (x < upperBound)
	 */
	 	private double upperBound;
	 
	 	/**
	 * Value that is returned if the number to be tested is within the range
	 */
	 	private String value;
	 
	 	public NumberRangeRule(double lowerBound, double upperBound, String value) 
		{
					this.lowerBound = lowerBound;
					this.upperBound = upperBound;
					this.value = value;
				}
	 
	 	/**
	 * Evaluates if the current value is within the range. If so, it returns the
	 * value. Otherwise it returns null.
	 */
	 	public String evaluate(double compareValue) 
		{
					// Check if the value is within the range
						if ((compareValue >= lowerBound) && (compareValue < upperBound))
										return value;
					
						// Default value is null
						return null;
				}
	 
	 	public double getLowerBound() 
		{
					return lowerBound;
				}
	 
	 	public double getUpperBound() 
		{
					return upperBound;
				}
	 
	 	public String getValue() 
		{
					return value;
				}
	 
	 }