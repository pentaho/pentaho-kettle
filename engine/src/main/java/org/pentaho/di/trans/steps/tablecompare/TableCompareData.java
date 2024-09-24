/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
