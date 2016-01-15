/*!
* Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
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

package org.pentaho.di.repository.pur;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;

import java.util.ArrayList;
import java.util.List;

public class PurRepositoryUnitTest {
  private VariableSpace mockedVariableSpace;
  private HasDatabasesInterface mockedHasDbInterface;

  @Before
  public void init() {
    System.setProperty( Const.KETTLE_TRANS_LOG_TABLE, "KETTLE_STEP_LOG_DB_VALUE" );
    mockedVariableSpace = Mockito.mock( VariableSpace.class );
    mockedHasDbInterface = Mockito.mock( HasDatabasesInterface.class );
  }

  @Test
  public void testGetObjectInformationGetsAclByFileId() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = Mockito.mock( IUnifiedRepository.class );
    RepositoryConnectResult result = Mockito.mock( RepositoryConnectResult.class );
    Mockito.when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = Mockito.mock( IRepositoryConnector.class );
    Mockito.when( connector.connect( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = Mockito.mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    // purRepository.setTest( mockRepo );
    ObjectId objectId = Mockito.mock( ObjectId.class );
    RepositoryFile mockFile = Mockito.mock( RepositoryFile.class );
    RepositoryFile mockRootFolder = Mockito.mock( RepositoryFile.class );
    RepositoryObjectType repositoryObjectType = RepositoryObjectType.TRANSFORMATION;
    RepositoryFileTree mockRepositoryTree = Mockito.mock( RepositoryFileTree.class );
    String testId = "TEST_ID";
    String testFileId = "TEST_FILE_ID";
    Mockito.when( objectId.getId() ).thenReturn( testId );
    Mockito.when( mockRepo.getFileById( testId ) ).thenReturn( mockFile );
    Mockito.when( mockFile.getPath() ).thenReturn( "/home/testuser/path.ktr" );
    Mockito.when( mockFile.getId() ).thenReturn( testFileId );
    Mockito.when( mockRepo.getTree( Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean() ) ).thenReturn( mockRepositoryTree );
    Mockito.when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    Mockito.when( mockRootFolder.getId() ).thenReturn( "/" );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    purRepository.getObjectInformation( objectId, repositoryObjectType );
    Mockito.verify( mockRepo ).getAcl( testFileId );
  }

  @Test
  public void onlyGlobalVariablesOfLogTablesSetToNull() {
    PurRepositoryExporter purRepoExporter = new PurRepositoryExporter( Mockito.mock( PurRepository.class ) );
    String hardcodedString = "hardcoded";
    String globalParam = "${" + Const.KETTLE_TRANS_LOG_TABLE + "}";

    StepLogTable stepLogTable = StepLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface );
    stepLogTable.setConnectionName( hardcodedString );
    stepLogTable.setSchemaName( hardcodedString );
    stepLogTable.setTimeoutInDays( hardcodedString );
    stepLogTable.setTableName( globalParam );

    JobEntryLogTable jobEntryLogTable = JobEntryLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface );
    jobEntryLogTable.setConnectionName( hardcodedString );
    jobEntryLogTable.setSchemaName( hardcodedString );
    jobEntryLogTable.setTimeoutInDays( hardcodedString );
    jobEntryLogTable.setTableName( globalParam );

    List<LogTableInterface> logTables = new ArrayList<>();
    logTables.add( jobEntryLogTable );
    logTables.add( stepLogTable );

    purRepoExporter.setGlobalVariablesOfLogTablesNull( logTables );

    for ( LogTableInterface logTable : logTables ) {
      Assert.assertEquals( logTable.getConnectionName(), hardcodedString );
      Assert.assertEquals( logTable.getSchemaName(), hardcodedString );
      Assert.assertEquals( logTable.getTimeoutInDays(), hardcodedString );
      Assert.assertEquals( logTable.getTableName(), null );
    }
  }
}
