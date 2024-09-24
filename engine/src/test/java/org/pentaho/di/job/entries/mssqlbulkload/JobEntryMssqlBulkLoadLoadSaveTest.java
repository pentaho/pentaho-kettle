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

package org.pentaho.di.job.entries.mssqlbulkload;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryMssqlBulkLoadLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryMssqlBulkLoad> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryMssqlBulkLoad> getJobEntryClass() {
    return JobEntryMssqlBulkLoad.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "schemaname", "tablename", "filename", "dataFileType", "fieldTerminator",
      "lineterminated", "codePage", "specificCodePage", "formatFilename", "fireTriggers", "checkConstraints",
      "keepNulls", "keepIdentity", "tablock", "startFile", "endFile", "orderBy", "orderDirection", "maxErrors",
      "batchSize", "rowsPerBatch", "errorFilename", "addDatetime", "addFileToResult", "truncate", "database" } );
  }

}
