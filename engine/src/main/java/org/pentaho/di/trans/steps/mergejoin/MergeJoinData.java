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

package org.pentaho.di.trans.steps.mergejoin;

import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Biswapesh
 * @since 24-nov-2005
 *
 */

public class MergeJoinData extends BaseStepData implements StepDataInterface {
  public Object[] one, two;
  public RowMetaInterface oneMeta, twoMeta;
  public RowMetaInterface outputRowMeta; // just for speed: oneMeta+twoMeta
  public Object[] one_dummy, two_dummy;
  public List<Object[]> ones, twos;
  public Object[] one_next, two_next;
  public boolean one_optional, two_optional;
  public int[] keyNrs1;
  public int[] keyNrs2;

  public RowSet oneRowSet;
  public RowSet twoRowSet;

  /**
   * Default initializer
   */
  public MergeJoinData() {
    super();
    ones = null;
    twos = null;
    one_next = null;
    two_next = null;
    one_dummy = null;
    two_dummy = null;
    one_optional = false;
    two_optional = false;
    keyNrs1 = null;
    keyNrs2 = null;
  }

}
