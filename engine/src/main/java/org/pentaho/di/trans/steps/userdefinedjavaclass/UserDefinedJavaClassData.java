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

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class UserDefinedJavaClassData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;
  public Map<String, String> parameterMap;
  public Map<String, String> infoMap;
  public Map<String, String> targetMap;

  public UserDefinedJavaClassData() {
    super();
  }
}
