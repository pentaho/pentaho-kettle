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

package org.pentaho.di.trans.steps.clonerow;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 27-06-2008
 *
 */
public class CloneRowData extends BaseStepData implements StepDataInterface {

  public long nrclones;
  public RowMetaInterface outputRowMeta;
  public int indexOfNrCloneField;
  public boolean addInfosToRow;
  public int NrPrevFields;

  public CloneRowData() {
    super();
    nrclones = 0;
    indexOfNrCloneField = -1;
    addInfosToRow = false;
    NrPrevFields = 0;
  }

}
