/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.sql;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExecSQLMetaInjectionTest extends BaseMetadataInjectionTest<ExecSQLMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new ExecSQLMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SQL", () -> meta.getSql() );
    check( "EXECUTE_FOR_EACH_ROW", () -> meta.isExecutedEachInputRow() );
    check( "UPDATE_STATS_FIELD", () -> meta.getUpdateField() );
    check( "INSERT_STATS_FIELD", () -> meta.getInsertField() );
    check( "DELETE_STATS_FIELD", () -> meta.getDeleteField() );
    check( "READ_STATS_FIELD", () -> meta.getReadField() );
    check( "EXECUTE_AS_SINGLE_STATEMENT", () -> meta.isSingleStatement() );
    check( "REPLACE_VARIABLES", () -> meta.isReplaceVariables() );
    check( "QUOTE_STRINGS", () -> meta.isQuoteString() );
    check( "BIND_PARAMETERS", () -> meta.isParams() );
    check( "PARAMETER_NAME", () -> meta.getArguments()[ 0 ] );

    // skip connection name testing, so we can provide our own custom handling
    skipPropertyTest( "CONNECTIONNAME" );

    // mock the database connections
    final DatabaseMeta db1 = new DatabaseMeta();
    db1.setName( "my connection 1" );
    final DatabaseMeta db2 = new DatabaseMeta();
    db2.setName( "my connection 2" );
    final DatabaseMeta db3 = new DatabaseMeta();
    db3.setName( "my connection 3" );
    final List<SharedObjectInterface> mockDbs = Arrays.asList( new SharedObjectInterface[] { db1, db2, db3 } );

    final StepMeta parentStepMeta = Mockito.mock( StepMeta.class );
    final TransMeta parentTransMeta = Mockito.mock( TransMeta.class );

    Mockito.doReturn( mockDbs ).when( parentTransMeta ).getDatabases();
    Mockito.doReturn( parentTransMeta ).when( parentStepMeta ).getParentTransMeta();
    meta.setParentStepMeta( parentStepMeta );

    injector.setProperty( meta, "CONNECTIONNAME", setValue( new ValueMetaString( "my connection 2" ),
      "my connection 2" ), "my connection 2" );
    // verify we get back the correct connection
    assertEquals( db2, meta.getDatabaseMeta() );
  }
}
