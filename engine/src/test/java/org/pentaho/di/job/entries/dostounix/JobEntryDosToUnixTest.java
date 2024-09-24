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
package org.pentaho.di.job.entries.dostounix;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryDosToUnixTest extends JobEntryLoadSaveTestSupport<JobEntryDosToUnix> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryDosToUnix> getJobEntryClass() {
    return JobEntryDosToUnix.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "nr_errors_less_than",
        "success_condition",
        "resultfilenames" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "nr_errors_less_than", "getNrErrorsLessThan",
        "success_condition", "getSuccessCondition",
        "resultfilenames", "getResultFilenames" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "nr_errors_less_than", "setNrErrorsLessThan",
        "success_condition", "setSuccessCondition",
        "resultfilenames", "setResultFilenames" );
  }
}
