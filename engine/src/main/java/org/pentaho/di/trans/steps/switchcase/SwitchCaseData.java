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
