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

package org.pentaho.di.trans.steps.stringcut;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 30 September 2008
 */
public class StringCutData extends BaseStepData implements StepDataInterface {

  public int[] inStreamNrs;

  public String[] outStreamNrs;

  public int[] cutFrom;

  public int[] cutTo;

  public RowMetaInterface outputRowMeta;

  public int inputFieldsNr;

  /**
   * Default constructor.
   */
  public StringCutData() {
    super();
    inputFieldsNr = 0;
  }
}
