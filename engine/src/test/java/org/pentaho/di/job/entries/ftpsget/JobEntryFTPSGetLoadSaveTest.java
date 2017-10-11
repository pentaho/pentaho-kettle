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

package org.pentaho.di.job.entries.ftpsget;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

public class JobEntryFTPSGetLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryFTPSGet> {

  @Override
  protected Class<JobEntryFTPSGet> getJobEntryClass() {
    return JobEntryFTPSGet.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "port", "serverName", "userName", "password", "FTPSDirectory",
      "targetDirectory", "wildcard", "binaryMode", "timeout", "remove", "onlyGettingNewFiles",
      "activeConnection", "moveFiles", "moveToDirectory", "dateInFilename", "timeInFilename",
      "specifyFormat", "dateTimeFormat", "addDateBeforeExtension", "addToResult", "createMoveFolder",
      "proxy_host", "proxy_port", "proxy_username", "proxy_password", "ifFileExists", "limit",
      "success_condition", "connection_type" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( "connection_type", new IntLoadSaveValidator( FTPSConnection.connection_type_Code.length ) );
    validators.put( "ifFileExists", new IntLoadSaveValidator( JobEntryFTPSGet.FILE_EXISTS_ACTIONS.length ) );

    return validators;
  }

}
