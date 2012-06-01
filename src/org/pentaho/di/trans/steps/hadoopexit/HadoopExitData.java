/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.hadoopexit;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class HadoopExitData extends BaseStepData implements StepDataInterface {
  private RowMetaInterface outputRowMeta = null;

  private int inKeyOrdinal = -1;
  private int inValueOrdinal = -1;
  
  private final static int outKeyOrdinal = 0;
  private final static int outValueOrdinal = 1;
  
  public HadoopExitData() {
    super();
  }

  public void init(RowMetaInterface rowMeta, HadoopExitMeta stepMeta, VariableSpace space) throws KettleException {
    if (rowMeta != null) {
      outputRowMeta = rowMeta.clone();
      stepMeta.getFields(outputRowMeta, stepMeta.getName(), null, null, space);
      
      setInKeyOrdinal(rowMeta.indexOfValue(stepMeta.getOutKeyFieldname()));
      setInValueOrdinal(rowMeta.indexOfValue(stepMeta.getOutValueFieldname()));
    }
  }

  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  public void setInKeyOrdinal(int inKeyOrdinal) {
    this.inKeyOrdinal = inKeyOrdinal;
  }

  public int getInKeyOrdinal() {
    return inKeyOrdinal;
  }

  public void setInValueOrdinal(int inValueOrdinal) {
    this.inValueOrdinal = inValueOrdinal;
  }

  public int getInValueOrdinal() {
    return inValueOrdinal;
  }

  public static int getOutKeyOrdinal() {
    return outKeyOrdinal;
  }

  public static int getOutValueOrdinal() {
    return outValueOrdinal;
  }
}
