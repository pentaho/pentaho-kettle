/*!
* Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.pentaho.di.core.logging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.HasDatabasesInterface;

public class LogTableTest {
  private static String PARAM_START_SYMBOL = "${";
  private static String PARAM_END_SYMBOL = "}";
  private static String GLOBAL_PARAM = PARAM_START_SYMBOL + Const.KETTLE_STEP_LOG_DB + PARAM_END_SYMBOL;
  private static String USER_PARAM = PARAM_START_SYMBOL + "param-content" + PARAM_END_SYMBOL;
  private static String HARDCODED_VALUE = "hardcoded";
  private VariableSpace mockedVariableSpace;
  private HasDatabasesInterface mockedHasDbInterface;

  @Before
  public void init() {
    System.setProperty( Const.KETTLE_STEP_LOG_DB, "KETTLE_STEP_LOG_DB_VALUE" );
    System.setProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "true" );
    mockedVariableSpace = mock( VariableSpace.class );
    mockedHasDbInterface = mock( HasDatabasesInterface.class );
  }
  @Test
  public void hardcodedFieldsNotChanged() {
    tableFieldsChangedCorrectlyAfterNullingGlobalParams( HARDCODED_VALUE, HARDCODED_VALUE );
  }

  @Test
  public void userParamsFieldsNotChanged() {
    tableFieldsChangedCorrectlyAfterNullingGlobalParams( USER_PARAM, USER_PARAM );
  }

  @Test
  public void globalParamsFieldsAreNulled() {
    tableFieldsChangedCorrectlyAfterNullingGlobalParams( GLOBAL_PARAM, null );
  }

  @Test
  public void globalParamsFieldsAreNotNulled() {
    System.setProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "false" );
    tableFieldsChangedCorrectlyAfterNullingGlobalParams( GLOBAL_PARAM, GLOBAL_PARAM );
  }

  public void tableFieldsChangedCorrectlyAfterNullingGlobalParams( String valueForAllFields,
                                                                   String expectedAfterNullingGlobalParams ) {

    PerformanceLogTable performanceLogTable = getPerformanceLogTableWithAllEqFields( valueForAllFields );
    performanceLogTable.setAllGlobalParametersToNull();
    commonTableFieldsValueChecker( performanceLogTable, expectedAfterNullingGlobalParams );
    assertEquals( performanceLogTable.getLogInterval(), expectedAfterNullingGlobalParams );

    JobLogTable jobLogTable = getJobLogTableWithAllEqFields( valueForAllFields );
    jobLogTable.setAllGlobalParametersToNull();
    commonTableFieldsValueChecker( jobLogTable, expectedAfterNullingGlobalParams );
    assertEquals( jobLogTable.getLogInterval(), expectedAfterNullingGlobalParams );
    assertEquals( jobLogTable.getLogSizeLimit(), expectedAfterNullingGlobalParams );

    TransLogTable transLogTable = getTransLogTableWithAllEqFields( valueForAllFields );
    transLogTable.setAllGlobalParametersToNull();
    commonTableFieldsValueChecker( transLogTable, expectedAfterNullingGlobalParams );
    assertEquals( transLogTable.getLogInterval(), expectedAfterNullingGlobalParams );
    assertEquals( transLogTable.getLogSizeLimit(), expectedAfterNullingGlobalParams );
  }

  private PerformanceLogTable getPerformanceLogTableWithAllEqFields( String fieldsValue ) {
    PerformanceLogTable performanceLogTable =
      PerformanceLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface );
    initCommonTableFields( performanceLogTable, fieldsValue );
    performanceLogTable.setLogInterval( fieldsValue );

    return performanceLogTable;
  }

  private JobLogTable getJobLogTableWithAllEqFields( String fieldsValue ) {
    JobLogTable jobLogTable = JobLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface );
    initCommonTableFields( jobLogTable, fieldsValue  );
    jobLogTable.setLogSizeLimit( fieldsValue );
    jobLogTable.setLogInterval( fieldsValue );

    return jobLogTable;
  }

  private TransLogTable getTransLogTableWithAllEqFields( String fieldsValue ) {
    TransLogTable transLogTable = TransLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface, null );
    initCommonTableFields( transLogTable, fieldsValue );
    transLogTable.setLogInterval( fieldsValue );
    transLogTable.setLogSizeLimit( fieldsValue );

    return transLogTable;
  }

  private void initCommonTableFields( BaseLogTable logTable, String value ) {
    logTable.setTableName( value );
    logTable.setConnectionName( value );
    logTable.setSchemaName( value );
    logTable.setTimeoutInDays( value );
  }

  private void commonTableFieldsValueChecker( BaseLogTable logTable, String expectedForAllFields ) {
    assertEquals( logTable.getTableName(), expectedForAllFields );
    assertEquals( logTable.getConnectionName(), expectedForAllFields );
    assertEquals( logTable.getSchemaName(), expectedForAllFields );
    assertEquals( logTable.getTimeoutInDays(), expectedForAllFields );
  }
}
