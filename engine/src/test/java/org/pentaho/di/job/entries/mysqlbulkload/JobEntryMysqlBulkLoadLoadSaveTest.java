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

package org.pentaho.di.job.entries.mysqlbulkload;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryMysqlBulkLoadLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryMysqlBulkLoad> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryMysqlBulkLoad> getJobEntryClass() {
    return JobEntryMysqlBulkLoad.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "schemaname", "tablename", "filename", "separator", "enclosed",
      "escaped", "linestarted", "lineterminated", "replacedata", "ignorelines", "listattribut",
      "localInfile", "prorityvalue", "addFileToResult", "database" } );
  }

}
