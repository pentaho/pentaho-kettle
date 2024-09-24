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

package org.pentaho.di.trans.steps.tablecompare;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 19-11-2009
 *
 */
public class TableCompareData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;

  public int refSchemaIndex;
  public int refTableIndex;
  public int cmpSchemaIndex;
  public int cmpTableIndex;
  public int keyFieldsIndex;
  public int excludeFieldsIndex;

  public Database referenceDb;
  public Database compareDb;
  public RowMetaInterface errorRowMeta;

  public int keyDescIndex;
  public int valueReferenceIndex;
  public int valueCompareIndex;

  public TableCompareData() {
    super();
  }

}
