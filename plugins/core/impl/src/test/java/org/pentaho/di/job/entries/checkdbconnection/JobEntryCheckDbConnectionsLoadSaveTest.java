/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.checkdbconnection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.ClassRule;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.DatabaseMetaLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryCheckDbConnectionsLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCheckDbConnections> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
