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


package org.pentaho.di.trans.steps.fieldsplitter;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class FieldSplitterData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface previousMeta;
  public RowMetaInterface outputMeta;
  public RowMetaInterface conversionMeta;
  public int fieldnr;
  public String delimiter;
  public String enclosure;

  public FieldSplitterData() {
    super();
  }

}
