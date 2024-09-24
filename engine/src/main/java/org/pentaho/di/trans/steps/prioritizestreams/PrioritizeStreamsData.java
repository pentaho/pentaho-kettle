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

package org.pentaho.di.trans.steps.prioritizestreams;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class PrioritizeStreamsData extends BaseStepData implements StepDataInterface {

  public RowSet[] rowSets;
  public int stepnrs;
  public int stepnr;
  public RowSet currentRowSet;
  public RowMetaInterface outputRowMeta;

  public PrioritizeStreamsData() {
    super();
  }

}
