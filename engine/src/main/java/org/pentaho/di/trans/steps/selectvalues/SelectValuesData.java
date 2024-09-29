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


package org.pentaho.di.trans.steps.selectvalues;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class SelectValuesData extends BaseStepData implements StepDataInterface {
  public int[] fieldnrs;
  public int[] extraFieldnrs;
  public int[] removenrs;
  public int[] metanrs;

  public boolean firstselect;
  public boolean firstdeselect;
  public boolean firstmetadata;

  public RowMetaInterface selectRowMeta;
  public RowMetaInterface deselectRowMeta;
  public RowMetaInterface metadataRowMeta;

  public RowMetaInterface outputRowMeta;

  // The MODE, default = select...
  public boolean select; // "normal" selection of fields.
  public boolean deselect; // de-select mode
  public boolean metadata; // change meta-data (rename & change length/precision)
}
