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


package org.pentaho.di.trans.steps.append;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Sven Boden
 * @since 3-june-2007
 */
public class AppendData extends BaseStepData implements StepDataInterface {
  public boolean processHead;
  public boolean processTail;
  public boolean firstTail;
  public RowSet headRowSet;
  public RowSet tailRowSet;
  public RowMetaInterface outputRowMeta;

  /**
   * Default constructor.
   */
  public AppendData() {
    super();
  }
}
