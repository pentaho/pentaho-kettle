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


package org.pentaho.di.trans.steps.gettablenames;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class GetTableNamesData extends BaseDatabaseStepData implements StepDataInterface {
  public String realTableNameFieldName;
  public String realObjectTypeFieldName;
  public String realIsSystemObjectFieldName;
  public String realSQLCreationFieldName;
  public String realSchemaName;

  public RowMetaInterface outputRowMeta;
  public long rownr;
  public RowMetaInterface inputRowMeta;
  public int totalpreviousfields;
  public int indexOfSchemaField;

  public Object[] readrow;

  public GetTableNamesData() {
    super();
    db = null;
    realTableNameFieldName = null;
    realObjectTypeFieldName = null;
    realIsSystemObjectFieldName = null;
    realSQLCreationFieldName = null;
    rownr = 0;
    realSchemaName = null;
    totalpreviousfields = 0;
    readrow = null;
    indexOfSchemaField = -1;
  }

}
