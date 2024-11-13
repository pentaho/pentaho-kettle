/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.closure;

import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 18-Sep-2007
 */
public class ClosureGeneratorData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public int parentIndex;
  public int childIndex;
  public boolean reading;
  public ValueMetaInterface parentValueMeta;
  public ValueMetaInterface childValueMeta;
  public Map<Object, Object> map;
  public Map<Object, Long> parents;
  public Object topLevel;

  public ClosureGeneratorData() {
    super();
  }
}
