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

package org.pentaho.di.job.entries.pgpencryptfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryPGPEncryptFilesLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryPGPEncryptFiles> {

  @Override
  protected Class<JobEntryPGPEncryptFiles> getJobEntryClass() {
    return JobEntryPGPEncryptFiles.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "GPGLocation", "arg_from_previous", "include_subfolders",
      "add_result_filesname", "destination_is_a_file", "create_destination_folder", "add_date",
      "add_time", "SpecifyFormat", "date_time_format", "nr_errors_less_than", "success_condition",
      "AddDateBeforeExtension", "DoNotKeepFolderStructure", "ifFileExists", "destinationFolder",
      "ifMovedFileExists", "moved_date_time_format", "create_move_to_folder", "add_moved_date",
      "add_moved_time", "SpecifyMoveFormat", "AddMovedDateBeforeExtension", "asciiMode",
      "action_type", "source_filefolder", "userid", "destination_filefolder", "wildcard" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    int count = new Random().nextInt( 50 ) + 1;

    validators.put( "action_type", new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator( JobEntryPGPEncryptFiles.actionTypeCodes.length ), count ) );
    validators.put( "source_filefolder", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );
    validators.put( "userid", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );
    validators.put( "destination_filefolder",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );
    validators.put( "wildcard", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );

    return validators;
  }
}
