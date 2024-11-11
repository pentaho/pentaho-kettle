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


package org.pentaho.di.trans.steps.regexeval;

import java.util.regex.Pattern;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Runtime data for the RegexEval step.
 *
 * @author Samatar Hassan
 * @author Daniel Einspanjer
 * @since 27-03-2008
 */
public class RegexEvalData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface conversionRowMeta;
  public int indexOfFieldToEvaluate;
  public int indexOfResultField;

  public Pattern pattern;

  public int[] positions;

  public RegexEvalData() {
    super();

    indexOfFieldToEvaluate = -1;
    indexOfResultField = -1;
  }
}
