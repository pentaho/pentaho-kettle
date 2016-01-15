/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.BaseLogTable;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.HasDatabasesInterface;

import java.util.ArrayList;

public class JobTest {
  private static final String STRING_DEFAULT = "<def>";
  private Job mockedJob;
  private Database mockedDataBase;
  private VariableSpace mockedVariableSpace;
  private HasDatabasesInterface hasDatabasesInterface;

  @Before
  public void init() {
    mockedDataBase = Mockito.mock( Database.class );
    mockedJob = Mockito.mock( Job.class );
    mockedVariableSpace = Mockito.mock( VariableSpace.class );
    hasDatabasesInterface = Mockito.mock( HasDatabasesInterface.class );

    Mockito.when( mockedJob.createDatabase( Matchers.any( DatabaseMeta.class ) ) ).thenReturn( mockedDataBase );
  }

  @Test
  public void recordsCleanUpMethodIsCalled_JobEntryLogTable() throws Exception {

    JobEntryLogTable jobEntryLogTable = JobEntryLogTable.getDefault( mockedVariableSpace, hasDatabasesInterface );
    setAllTableParamsDefault( jobEntryLogTable );

    JobMeta jobMeta = new JobMeta(  );
    jobMeta.setJobEntryLogTable( jobEntryLogTable );

    Mockito.when( mockedJob.getJobMeta() ).thenReturn( jobMeta );
    Mockito.doCallRealMethod().when( mockedJob ).writeJobEntryLogInformation();

    mockedJob.writeJobEntryLogInformation();

    Mockito.verify( mockedDataBase ).cleanupLogRecords( jobEntryLogTable );
  }

  @Test
  public void recordsCleanUpMethodIsCalled_JobLogTable() throws Exception {
    JobLogTable jobLogTable = JobLogTable.getDefault( mockedVariableSpace, hasDatabasesInterface );
    setAllTableParamsDefault( jobLogTable );

    Mockito.doCallRealMethod().when( mockedJob ).writeLogTableInformation( jobLogTable, LogStatus.END );

    mockedJob.writeLogTableInformation( jobLogTable, LogStatus.END );

    Mockito.verify( mockedDataBase ).cleanupLogRecords( jobLogTable );
  }

  public void setAllTableParamsDefault( BaseLogTable table ) {
    table.setSchemaName( STRING_DEFAULT );
    table.setConnectionName( STRING_DEFAULT );
    table.setTimeoutInDays( STRING_DEFAULT );
    table.setTableName( STRING_DEFAULT );
    table.setFields( new ArrayList<LogTableField>() );
  }

}
