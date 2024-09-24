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

package org.pentaho.di.trans.steps.switchcase;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class SwitchCaseData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;
  public KeyToRowSetMap outputMap;
  public ValueMetaInterface valueMeta;
  public final Set<RowSet> nullRowSetSet = new HashSet<RowSet>();
  public int fieldIndex;
  public ValueMetaInterface inputValueMeta;
  // we expect only one default set for now
  public final Set<RowSet> defaultRowSetSet = new HashSet<RowSet>( 1, 1 );
  public ValueMetaInterface stringValueMeta;

  public SwitchCaseData() {
    super();
  }
}
