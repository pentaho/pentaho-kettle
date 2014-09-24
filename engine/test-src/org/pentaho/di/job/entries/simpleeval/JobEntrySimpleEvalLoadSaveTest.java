package org.pentaho.di.job.entries.simpleeval;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class JobEntrySimpleEvalLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntrySimpleEval> {

  @Override
  protected Class<JobEntrySimpleEval> getJobEntryClass() {
    return JobEntrySimpleEval.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return asList(
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
    return asList( "name", "description" );
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