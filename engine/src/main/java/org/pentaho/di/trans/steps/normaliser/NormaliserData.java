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

package org.pentaho.di.trans.steps.normaliser;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class NormaliserData extends BaseStepData implements StepDataInterface {
  public List<String> type_occ;
  public int maxlen;
  public List<Integer> copy_fieldnrs;
  Map<String, List<Integer>> typeToFieldIndex;

  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public NormaliserData() {
    super();

    type_occ = null;
  }

}
