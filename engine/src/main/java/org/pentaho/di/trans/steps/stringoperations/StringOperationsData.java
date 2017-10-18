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

package org.pentaho.di.trans.steps.stringoperations;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Apply certain operations too string.
 *
 * @author Samatar Hassan
 * @since 02 April 2009
 */
public class StringOperationsData extends BaseStepData implements StepDataInterface {

  public int[] inStreamNrs; // string infields

  public String[] outStreamNrs;

  /** Runtime trim operators */
  public int[] trimOperators;

  /** Runtime trim operators */
  public int[] lowerUpperOperators;

  public int[] padType;

  public String[] padChar;

  public int[] padLen;

  public int[] initCap;

  public int[] maskHTML;

  public int[] digits;

  public int[] removeSpecialCharacters;

  public RowMetaInterface outputRowMeta;

  public int inputFieldsNr;

  public int nrFieldsInStream;

  /**
   * Default constructor.
   */
  public StringOperationsData() {
    super();
    this.inputFieldsNr = 0;
    this.nrFieldsInStream = 0;
  }
}
