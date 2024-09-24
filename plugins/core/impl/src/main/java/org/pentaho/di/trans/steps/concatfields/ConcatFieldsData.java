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
