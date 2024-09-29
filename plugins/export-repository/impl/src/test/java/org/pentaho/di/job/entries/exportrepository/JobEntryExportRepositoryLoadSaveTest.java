/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.exportrepository;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryExportRepositoryLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryExportRepository> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryExportRepository> getJobEntryClass() {
    return JobEntryExportRepository.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "repositoryname", "username", "password", "targetfilename",
      "ifFileExists", "exportType", "directory", "addDate", "addTime", "SpecifyFormat",
      "date_time_format", "createFolder", "newFolder", "addresultfilesname", "nrLimit",
      "successCondition" } );
  }

}
