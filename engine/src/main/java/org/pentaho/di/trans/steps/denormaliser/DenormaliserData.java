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

package org.pentaho.di.trans.steps.denormaliser;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Data structure used by Denormaliser during processing
 *
 * @author Matt
 * @since 19-jan-2006
 *
 */
public class DenormaliserData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;

  public Object[] previous;

  public int[] groupnrs;
  public Integer[] fieldNrs;

  public Object[] targetResult;

  public int keyFieldNr;

  public Map<String, List<Integer>> keyValue;

  public int[] removeNrs;

  public int[] fieldNameIndex;

  public long[] counters;

  public Object[] sum;

  public RowMetaInterface inputRowMeta;

  public DenormaliserData() {
    super();

    previous = null;
    keyValue = new Hashtable<String, List<Integer>>();
  }

}
