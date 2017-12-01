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

package org.pentaho.di.trans.steps.replacestring;

import java.util.regex.Pattern;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 28 September 2008
 */
public class ReplaceStringData extends BaseStepData implements StepDataInterface {

  public int[] inStreamNrs;

  public String[] outStreamNrs;

  public int[] useRegEx;

  public String[] replaceString;

  public String[] replaceByString;

  public boolean[] setEmptyString;

  public int[] replaceFieldIndex;

  public int[] wholeWord;

  public int[] caseSensitive;

  public int[] isUnicode;

  public String realChangeField;

  public String[] valueChange;

  public String finalvalueChange;

  public RowMetaInterface outputRowMeta;

  public int inputFieldsNr;

  public Pattern[] patterns;

  public int numFields;

  /**
   * Default constructor.
   */
  public ReplaceStringData() {
    super();
    realChangeField = null;
    valueChange = null;
    finalvalueChange = null;
    inputFieldsNr = 0;
  }
}
