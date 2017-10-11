/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
