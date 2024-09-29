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


package org.pentaho.di.trans.steps.setvalueconstant;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class SetValueConstantData extends BaseStepData implements StepDataInterface {

  private RowMetaInterface outputRowMeta;
  private RowMetaInterface convertRowMeta;

  private String[] realReplaceByValues;
  private int[] fieldnrs;
  private int fieldnr;

  SetValueConstantData() {
    super();
  }

  RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  void setOutputRowMeta( RowMetaInterface outputRowMeta ) {
    this.outputRowMeta = outputRowMeta;
  }

  RowMetaInterface getConvertRowMeta() {
    return convertRowMeta;
  }

  void setConvertRowMeta( RowMetaInterface convertRowMeta ) {
    this.convertRowMeta = convertRowMeta;
  }

  String[] getRealReplaceByValues() {
    return realReplaceByValues;
  }

  void setRealReplaceByValues( String[] realReplaceByValues ) {
    this.realReplaceByValues = realReplaceByValues;
  }

  int[] getFieldnrs() {
    return fieldnrs;
  }

  void setFieldnrs( int[] fieldnrs ) {
    this.fieldnrs = fieldnrs;
  }

  int getFieldnr() {
    return fieldnr;
  }

  void setFieldnr( int fieldnr ) {
    this.fieldnr = fieldnr;
  }
}
