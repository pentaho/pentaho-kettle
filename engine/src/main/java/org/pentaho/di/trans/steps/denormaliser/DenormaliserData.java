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
