/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job.entries.mssqlbulkload;

import java.util.Arrays;
import java.util.List;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

public class JobEntryMssqlBulkLoadLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryMssqlBulkLoad> {

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
