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


package org.pentaho.di.trans.steps.janino;

import java.util.List;

import org.codehaus.janino.ExpressionEvaluator;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 8-sep-2005
 *
 */
public class JaninoData extends BaseStepData implements StepDataInterface {
  public static final int RETURN_TYPE_STRING = 0;
  public static final int RETURN_TYPE_NUMBER = 1;
  public static final int RETURN_TYPE_INTEGER = 2;
  public static final int RETURN_TYPE_LONG = 3;
  public static final int RETURN_TYPE_DATE = 4;
  public static final int RETURN_TYPE_BIGDECIMAL = 5;
  public static final int RETURN_TYPE_BYTE_ARRAY = 6;
  public static final int RETURN_TYPE_BOOLEAN = 7;

  public RowMetaInterface outputRowMeta;
  public ValueMetaInterface[] returnType;
  public int[] replaceIndex;

  public ExpressionEvaluator[] expressionEvaluators;
  public List<List<Integer>> argumentIndexes;

  public JaninoData() {
    super();
  }

}
