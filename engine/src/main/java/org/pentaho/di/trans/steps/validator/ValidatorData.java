/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
public class ValidatorData extends BaseStepData implements StepDataInterface {
  public int[] fieldIndexes;

  public ValueMetaInterface[] constantsMeta;
  public String[] minimumValueAsString;
  public String[] maximumValueAsString;
  public int[] fieldsMinimumLengthAsInt;
  public int[] fieldsMaximumLengthAsInt;
  public Object[][] listValues;

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

  public ValidatorData() {
    super();
  }
}
