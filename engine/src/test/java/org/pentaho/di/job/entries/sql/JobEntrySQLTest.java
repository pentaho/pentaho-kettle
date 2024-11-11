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


package org.pentaho.di.job.entries.sql;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntrySQLTest extends JobEntryLoadSaveTestSupport<JobEntrySQL> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntrySQL> getJobEntryClass() {
    return JobEntrySQL.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "sql",
        "useVariableSubstitution",
        "sqlfromfile",
        "sqlfilename",
        "sendOneStatement",
        "database" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "sql", "getSQL",
        "useVariableSubstitution", "getUseVariableSubstitution",
        "sqlfromfile", "getSQLFromFile",
        "sqlfilename", "getSQLFilename",
        "sendOneStatement", "isSendOneStatement",
        "database", "getDatabase" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "sql", "setSQL",
        "useVariableSubstitution", "setUseVariableSubstitution",
        "sqlfromfile", "setSQLFromFile",
        "sqlfilename", "setSQLFilename",
        "sendOneStatement", "setSendOneStatement",
        "database", "setDatabase" );
  }

}
