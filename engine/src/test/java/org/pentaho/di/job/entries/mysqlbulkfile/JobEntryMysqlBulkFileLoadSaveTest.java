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

package org.pentaho.di.job.entries.mysqlbulkfile;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryMysqlBulkFileLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryMysqlBulkFile> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryMysqlBulkFile> getJobEntryClass() {
    return JobEntryMysqlBulkFile.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "schemaname", "tablename", "filename", "separator", "enclosed",
      "optionEnclosed", "lineterminated", "limitlines", "listColumn", "highPriority", "outdumpvalue",
      "iffileexists", "addFileToResult", "database" } );
  }

}
