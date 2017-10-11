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

package org.pentaho.di.job.entries.checkdbconnection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.DatabaseMetaLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryCheckDbConnectionsLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCheckDbConnections> {

  @Override
  protected Class<JobEntryCheckDbConnections> getJobEntryClass() {
    return JobEntryCheckDbConnections.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "connections", "waitfors", "waittimes" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    int entries = new Random().nextInt( 10 ) + 1;
    validators.put( "connections", new ArrayLoadSaveValidator<DatabaseMeta>(
      new DatabaseMetaLoadSaveValidator(), entries ) );
    validators.put( "waitfors", new ArrayLoadSaveValidator<String>(
      new StringLoadSaveValidator(), entries ) );
    validators.put( "waittimes", new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator( JobEntryCheckDbConnections.unitTimeCode.length ), entries ) );
    return validators;
  }
}
