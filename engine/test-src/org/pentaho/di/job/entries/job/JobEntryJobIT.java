/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.job;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.www.SlaveServerJobStatus;

import java.nio.file.Files;
import java.nio.file.Path;


public class JobEntryJobIT extends JobEntryJob {

  private final String JOB_ENTRY_JOB_NAME = "JobEntryJobName";
  private final String LOG_FILE_NAME = "LogFileName";
  private final String REMOTE_SLAVE_SERVER_NAME = "RemoteSlaveServerName";
  private final String JOB_META_NAME = "JobMetaName";
  private final String REPLY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          + "<webresult><result>OK</result><id>0</id></webresult>";
  private final String LOG = "Log";

  private Path file;

  @BeforeClass
  public static void setUpBeforeClass() {
    KettleLogStore.init();
  }

  @After
  public void tearDown() throws Exception {
    // Wait for LogChannelFileWriter to close outputstream
    Thread.sleep( 1000 );
    FileUtils.forceDelete( file.toFile() );
  }

  @Test
  public void testLogfileWritesFromRemote() throws Exception {
    JobEntryJob job = Mockito.spy( new JobEntryJob( JOB_ENTRY_JOB_NAME ) );
    Mockito.doCallRealMethod().when( job ).execute( Matchers.any( Result.class ), Matchers.anyInt() );

    Job parentJob = Mockito.mock( Job.class );
    JobMeta parentJobMeta = Mockito.mock( JobMeta.class );
    JobMeta jobMeta = Mockito.mock( JobMeta.class );
    SlaveServer slaveServer = Mockito.mock( SlaveServer.class );
    LogChannelInterface log = Mockito.mock( LogChannelInterface.class );
    SlaveServerJobStatus status = Mockito.mock( SlaveServerJobStatus.class );

    Mockito.when( parentJob.getLogLevel() ).thenReturn( LogLevel.BASIC );
    Mockito.when( parentJobMeta.getRepositoryDirectory() ).thenReturn( null );
    Mockito.when( jobMeta.getRepositoryDirectory() ).thenReturn( Mockito.mock( RepositoryDirectoryInterface.class ) );
    Mockito.when( jobMeta.getName() ).thenReturn( JOB_META_NAME );
    Mockito.when( parentJob.getJobMeta() ).thenReturn( parentJobMeta );
    Mockito.when( parentJobMeta.findSlaveServer( REMOTE_SLAVE_SERVER_NAME ) ).thenReturn( slaveServer );
    Mockito.when( slaveServer.getLogChannel() ).thenReturn( log );
    Mockito.when( log.getLogLevel() ).thenReturn( LogLevel.BASIC );
    Mockito.when( slaveServer.sendXML( Matchers.anyString(), Matchers.anyString() ) ).thenReturn( REPLY );
    Mockito.when( slaveServer.execService( Matchers.anyString() ) ).thenReturn( REPLY );
    Mockito.when( slaveServer.getJobStatus( Matchers.anyString(), Matchers.anyString(), Matchers.anyInt() ) ).thenReturn( status );
    Mockito.when( status.getResult() ).thenReturn( Mockito.mock( Result.class ) );
    Mockito.when( status.getLoggingString() ).thenReturn( LOG );

    file = Files.createTempFile( "file", "" );

    Mockito.doReturn( LOG_FILE_NAME ).when( job ).getLogFilename();
    Mockito.doReturn( file.toString() ).when( job ).environmentSubstitute( LOG_FILE_NAME );
    Mockito.doReturn( REMOTE_SLAVE_SERVER_NAME ).when( job ).environmentSubstitute( REMOTE_SLAVE_SERVER_NAME );
    Mockito.doReturn( jobMeta ).when( job ).getJobMeta( Matchers.any( Repository.class ), Matchers.any( VariableSpace.class ) );
    Mockito.doNothing().when( job ).copyVariablesFrom( Matchers.any( VariableSpace.class ) );
    Mockito.doNothing().when( job ).setParentVariableSpace( Matchers.any( VariableSpace.class ) );

    job.setLogfile = true;
    job.createParentFolder = false;
    job.logFileLevel = LogLevel.BASIC;
    job.execPerRow = false;
    job.paramsFromPrevious = false;
    job.argFromPrevious = false;
    job.waitingToFinish = true;
    job.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    job.setRemoteSlaveServerName( REMOTE_SLAVE_SERVER_NAME );
    job.setParentJob( parentJob );

    job.execute( new Result(), 0 );
    String result = FileUtils.readFileToString( file.toFile() );
    Assert.assertTrue( result.contains( LOG ) );
  }
}
