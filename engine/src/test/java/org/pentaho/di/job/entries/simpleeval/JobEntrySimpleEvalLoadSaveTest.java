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

package org.pentaho.di.job.entries.simpleeval;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.List;
import java.util.Map;

import java.util.Arrays;

public class JobEntrySimpleEvalLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntrySimpleEval> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();


  @Override
  protected Class<JobEntrySimpleEval> getJobEntryClass() {
    return JobEntrySimpleEval.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
      "fieldname",
      "variablename",
      "mask",
      "comparevalue",
      "minvalue",
      "maxvalue",
      "successwhenvarset"
    );
  }

  @Override
  protected List<String> listXmlAttributes() {
    return Arrays.asList( "name", "description" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
      "fieldname", "getFieldName",
      "variablename", "getVariableName",
      "comparevalue", "getCompareValue",
      "minvalue", "getMinValue",
      "maxvalue", "getMaxValue",
      "successwhenvarset", "isSuccessWhenVarSet"
    );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
      "fieldname", "setFieldName",
      "variablename", "setVariableName",
      "comparevalue", "setCompareValue",
      "minvalue", "setMinValue",
      "maxvalue", "setMaxValue",
      "successwhenvarset", "setSuccessWhenVarSet"
    );
  }
}
