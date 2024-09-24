/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.evaluatetablecontent;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.InfobrightDatabaseMeta;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * tests fix for PDI-1044
 * Job entry: Evaluate rows number in a table:
 * PDI Server logs with error from Quartz even though the job finishes successfully.
 */
public class JobEntryEvalTableContentTest {
  private static final Map<Class<?>, String> dbMap = new HashMap<>();
  JobEntryEvalTableContent entry;

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  public static class DBMockIface extends BaseDatabaseMeta {

    @Override
    public Object clone() {
      return super.clone();
    }

    @Override
    public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                      boolean addFieldName, boolean addCr ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getDriverClass() {
      return "org.pentaho.di.job.entries.evaluatetablecontent.MockDriver";
    }

    @Override
    public String getURL( String hostname, String port, String databaseName ) {
      return "";
    }

    @Override
    public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                         String pk, boolean semicolon ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk,
                                            boolean useAutoinc, String pk, boolean semicolon ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String[] getUsedLibraries() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int[] getAccessTypeList() {
      // TODO Auto-generated method stub
      return null;
    }

  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
    dbMap.put( DatabaseInterface.class, DBMockIface.class.getName() );
    dbMap.put( InfobrightDatabaseMeta.class, InfobrightDatabaseMeta.class.getName() );

    PluginRegistry preg = PluginRegistry.getInstance();

    PluginInterface mockDbPlugin = mock( PluginInterface.class );
    when( mockDbPlugin.matches( anyString() ) ).thenReturn( true );
    when( mockDbPlugin.isNativePlugin() ).thenReturn( true );
    when( mockDbPlugin.getMainType() ).thenAnswer( (Answer<Class<?>>) invocation -> DatabaseInterface.class );

    when( mockDbPlugin.getPluginType() ).thenAnswer(
      (Answer<Class<? extends PluginTypeInterface>>) invocation -> DatabasePluginType.class );

    when( mockDbPlugin.getIds() ).thenReturn( new String[] { "Oracle", "mock-db-id" } );
    when( mockDbPlugin.getName() ).thenReturn( "mock-db-name" );
    when( mockDbPlugin.getClassMap() ).thenReturn( dbMap );

    preg.registerPlugin( DatabasePluginType.class, mockDbPlugin );
  }

  @AfterClass
  public static void tearDownAfterClass() {
    KettleClientEnvironment.reset();
  }

  @Before
  public void setUp() throws Exception {
    MockDriver.registerInstance();
    Job job = new Job( null, new JobMeta() );
    entry = new JobEntryEvalTableContent();

    job.getJobMeta().addJobEntry( new JobEntryCopy( entry ) );
    entry.setParentJob( job );

    job.setStopped( false );

    DatabaseMeta dbMeta = new DatabaseMeta();
    dbMeta.setDatabaseType( "mock-db" );

    entry.setDatabase( dbMeta );
    // set KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES to default, in case overwritten at any point.
    entry.setVariable( Const.KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES, "N" );
  }

  @After
  public void tearDown() throws Exception {
    MockDriver.deregeisterInstances();
  }

  @Test
  public void testNrErrorsFailureNewBehavior() {
    entry.setLimit( "1" );
    entry.setSuccessCondition( JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_EQUAL );
    entry.setTablename( "table" );

    Result res = entry.execute( new Result(), 0 );

    assertFalse( "Eval number of rows should fail", res.getResult() );
    assertEquals(
      "No errors should be reported in result object accoding to the new behavior", 0, res.getNrErrors() );
  }

  @Test
  public void testNrErrorsFailureOldBehavior() {
    entry.setLimit( "1" );
    entry.setSuccessCondition( JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_EQUAL );
    entry.setTablename( "table" );

    entry.setVariable( Const.KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES, "Y" );

    Result res = entry.execute( new Result(), 0 );

    assertFalse( "Eval number of rows should fail", res.getResult() );
    assertEquals(
      "An error should be reported in result object accoding to the old behavior", 1, res.getNrErrors() );
  }

  @Test
  public void testNrErrorsSuccess() {


    entry.setLimit( "5" );
    entry.setSuccessCondition( JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_EQUAL );
    entry.setTablename( "table" );

    Result res = entry.execute( new Result(), 0 );

    assertTrue( "Eval number of rows should be suceeded", res.getResult() );
    assertEquals( "Apparently there should no error", 0, res.getNrErrors() );

    // that should work regardless of old/new behavior flag
    entry.setVariable( Const.KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES, "Y" );

    res = entry.execute( new Result(), 0 );

    assertTrue( "Eval number of rows should be suceeded", res.getResult() );
    assertEquals( "Apparently there should no error", 0, res.getNrErrors() );
  }

  @Test
  public void testNrErrorsNoCustomSql() {
    entry.setLimit( "5" );
    entry.setSuccessCondition( JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_EQUAL );
    entry.setUseCustomSQL( true );
    entry.setCustomSQL( null );

    Result res = entry.execute( new Result(), 0 );

    assertFalse( "Eval number of rows should fail", res.getResult() );
    assertEquals( "Apparently there should be an error", 1, res.getNrErrors() );

    // that should work regardless of old/new behavior flag
    entry.setVariable( Const.KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES, "Y" );

    res = entry.execute( new Result(), 0 );

    assertFalse( "Eval number of rows should fail", res.getResult() );
    assertEquals( "Apparently there should be an error", 1, res.getNrErrors() );
  }
}
