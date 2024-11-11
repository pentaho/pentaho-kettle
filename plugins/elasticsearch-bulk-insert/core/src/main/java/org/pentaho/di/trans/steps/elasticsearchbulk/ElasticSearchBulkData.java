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


package org.pentaho.di.trans.steps.elasticsearchbulk;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/*
 * @author webdetails
 * @since 16-02-2011
 */
public class ElasticSearchBulkData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public int nextBufferRowIdx;

  public Object[][] inputRowBuffer;

  public ElasticSearchBulkData() {
    super();

    nextBufferRowIdx = 0;
  }

}
