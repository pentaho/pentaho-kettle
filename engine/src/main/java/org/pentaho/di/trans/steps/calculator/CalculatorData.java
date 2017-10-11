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

package org.pentaho.di.trans.steps.calculator;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Matt
 * @since 8-sep-2005
 */
public class CalculatorData extends BaseStepData implements StepDataInterface {
  private RowMetaInterface outputRowMeta;
  private RowMetaInterface calcRowMeta;

  private Calculator.FieldIndexes[] fieldIndexes;

  private int[] tempIndexes;

  private final Map<Integer, ValueMetaInterface> resultMetaMapping;

  public CalculatorData() {
    super();
    resultMetaMapping = new HashMap<Integer, ValueMetaInterface>();
  }

  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  public void setOutputRowMeta( RowMetaInterface outputRowMeta ) {
    this.outputRowMeta = outputRowMeta;
  }

  public RowMetaInterface getCalcRowMeta() {
    return calcRowMeta;
  }

  public void setCalcRowMeta( RowMetaInterface calcRowMeta ) {
    this.calcRowMeta = calcRowMeta;
  }

  public Calculator.FieldIndexes[] getFieldIndexes() {
    return fieldIndexes;
  }

  public void setFieldIndexes( Calculator.FieldIndexes[] fieldIndexes ) {
    this.fieldIndexes = fieldIndexes;
  }

  public int[] getTempIndexes() {
    return tempIndexes;
  }

  public void setTempIndexes( int[] tempIndexes ) {
    this.tempIndexes = tempIndexes;
  }

  public ValueMetaInterface getValueMetaFor( int resultType, String name ) throws KettlePluginException {
    // don't need any synchronization as data instance belongs only to one step instance
    ValueMetaInterface meta = resultMetaMapping.get( resultType );
    if ( meta == null ) {
      meta = ValueMetaFactory.createValueMeta( name, resultType );
      resultMetaMapping.put( resultType, meta );
    }
    return meta;
  }

  public void clearValuesMetaMapping() {
    resultMetaMapping.clear();
  }
}
