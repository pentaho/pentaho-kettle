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

package org.pentaho.di.trans.steps.univariatestats;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Holds temporary data and has routines for computing derived statistics.
 *
 * @author Mark Hall (mhall{[at]}pentaho.org)
 * @version 1.0
 */
public class UnivariateStatsData extends BaseStepData implements StepDataInterface {

  // this class contains intermediate results,
  // info about the input format, derived output
  // format etc.

  // the input data format
  protected RowMetaInterface m_inputRowMeta;

  // the output data format
  protected RowMetaInterface m_outputRowMeta;

  /**
   * contains the FieldIndexs - one for each UnivariateStatsMetaFunction
   */
  protected FieldIndex[] m_indexes;

  /**
   * Creates a new <code>UnivariateStatsData</code> instance.
   */
  public UnivariateStatsData() {
    super();
  }

  /**
   * Set the FieldIndexes
   *
   * @param fis
   *          a <code>FieldIndex[]</code> value
   */
  public void setFieldIndexes( FieldIndex[] fis ) {
    m_indexes = fis;
  }

  /**
   * Get the fieldIndexes
   *
   * @return a <code>FieldIndex[]</code> value
   */
  public FieldIndex[] getFieldIndexes() {
    return m_indexes;
  }

  /**
   * Get the meta data for the input format
   *
   * @return a <code>RowMetaInterface</code> value
   */
  public RowMetaInterface getInputRowMeta() {
    return m_inputRowMeta;
  }

  /**
   * Save the meta data for the input format. (I'm not sure that this is really needed)
   *
   * @param rmi
   *          a <code>RowMetaInterface</code> value
   */
  public void setInputRowMeta( RowMetaInterface rmi ) {
    m_inputRowMeta = rmi;
  }

  /**
   * Get the meta data for the output format
   *
   * @return a <code>RowMetaInterface</code> value
   */
  public RowMetaInterface getOutputRowMeta() {
    return m_outputRowMeta;
  }

  /**
   * Set the meta data for the output format
   *
   * @param rmi
   *          a <code>RowMetaInterface</code> value
   */
  public void setOutputRowMeta( RowMetaInterface rmi ) {
    m_outputRowMeta = rmi;
  }
}
