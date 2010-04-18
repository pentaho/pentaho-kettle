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

import java.util.regex.Pattern;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 11-jan-2007
 *
 */
public class ValidatorData extends BaseStepData implements StepDataInterface
{
    public int[] fieldIndexes;
    
    public ValueMetaInterface[] constantsMeta;
    public String[] minimumValueAsString;
    public String[] maximumValueAsString;
    public int[]    fieldsMinimumLengthAsInt;
    public int[]    fieldsMaximumLengthAsInt;
    public Object[] listValues[];

	public Pattern[] patternExpected;
	
	public Pattern[] patternDisallowed;
	
	public String[] errorCode;
	public String[] errorDescription; 
	public String[] conversionMask;
	public String[] decimalSymbol;
	public String[] groupingSymbol;
	public String[] maximumLength;
	public String[] minimumLength;
	public Object[] maximumValue;
	public Object[] minimumValue;
	public String[] startString;
	public String[] endString;
	public String[] startStringNotAllowed;
	public String[] endStringNotAllowed;
	public String[] regularExpression;
	public String[] regularExpressionNotAllowed;


	/**
	 * 
	 */
	public ValidatorData()
	{
		super();
	}
}
