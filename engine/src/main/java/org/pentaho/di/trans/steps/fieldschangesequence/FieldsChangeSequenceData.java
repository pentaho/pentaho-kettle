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

package org.pentaho.di.trans.steps.fieldschangesequence;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class FieldsChangeSequenceData extends BaseStepData implements StepDataInterface {

  public ValueMetaInterface[] fieldnrsMeta;
  public RowMetaInterface previousMeta;
  public RowMetaInterface outputRowMeta;

  public int[] fieldnrs;
  public Object[] previousValues;
  public int fieldnr;
  public long startAt;
  public long incrementBy;
  public long seq;
  public int nextIndexField;

  public FieldsChangeSequenceData() {
    super();
  }

}
