/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.concatfields;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputData;

/*
 * ConcatFieldsData
 * @author jb
 * @since 2012-08-31
 *
 */
public class ConcatFieldsData extends TextFileOutputData implements StepDataInterface {
  public int posTargetField;
  public int[] remainingFieldsInputOutputMapping;
  public RowMetaInterface inputRowMetaModified; // the field precisions and lengths are altered! see
  // TextFileOutputMeta.getFields().
  public String stringSeparator;
  public String stringEnclosure;
  public String[] stringNullValue;
  public int targetFieldLengthFastDataDump; // for fast data dump (StringBuilder size)
  public int headerOffsetForSplitRows;

  public ConcatFieldsData() {
    super(); // allocate TextFileOutputData
  }

}
