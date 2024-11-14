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


package org.pentaho.di.lineage;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * This class contains value lineage information.<br>
 * That means that we will have information on where and how a certain value is originating, being manipulated etc.<br>
 *
 * @author matt
 *
 */
public class ValueLineage {
  private TransMeta transMeta;
  private ValueMetaInterface valueMeta;

  private List<StepMeta> sourceSteps;

  /**
   * Create a new ValueLineage object with an empty set of source steps.
   *
   * @param valueMeta
   */
  public ValueLineage( TransMeta transMeta, ValueMetaInterface valueMeta ) {
    this.transMeta = transMeta;
    this.valueMeta = valueMeta;
    this.sourceSteps = new ArrayList<StepMeta>();
  }

  /**
   * @return the transMeta
   */
  public TransMeta getTransMeta() {
    return transMeta;
  }

  /**
   * @param transMeta
   *          the transMeta to set
   */
  public void setTransMeta( TransMeta transMeta ) {
    this.transMeta = transMeta;
  }

  /**
   * @return the valueMeta
   */
  public ValueMetaInterface getValueMeta() {
    return valueMeta;
  }

  /**
   * @param valueMeta
   *          the valueMeta to set
   */
  public void setValueMeta( ValueMetaInterface valueMeta ) {
    this.valueMeta = valueMeta;
  }

  /**
   * @return the sourceSteps
   */
  public List<StepMeta> getSourceSteps() {
    return sourceSteps;
  }

  /**
   * @param sourceSteps
   *          the sourceSteps to set
   */
  public void setSourceSteps( List<StepMeta> sourceSteps ) {
    this.sourceSteps = sourceSteps;
  }

}
