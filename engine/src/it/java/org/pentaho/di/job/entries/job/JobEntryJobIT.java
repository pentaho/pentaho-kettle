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

package org.pentaho.di.job.entries.job;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
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
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    JobEntryJob job = spy( new JobEntryJob( JOB_ENTRY_JOB_NAME ) );
    doCallRealMethod().when( job ).execute( any( Result.class ), anyInt() );

    Job parentJob = mock( Job.class );
    JobMeta parentJobMeta = mock( JobMeta.class );
    JobMeta jobMeta = mock( JobMeta.class );
    SlaveServer slaveServer = mock( SlaveServer.class );
    LogChannelInterface log = mock( LogChannelInterface.class );
    SlaveServerJobStatus status = mock( SlaveServerJobStatus.class );

    when( parentJob.getLogLevel() ).thenReturn( LogLevel.BASIC );
    when( parentJobMeta.getRepositoryDirectory() ).thenReturn( null );
    when( jobMeta.getRepositoryDirectory() ).thenReturn( mock( RepositoryDirectoryInterface.class ) );
    when( jobMeta.getName() ).thenReturn( JOB_META_NAME );
    when( parentJob.getJobMeta() ).thenReturn( parentJobMeta );
    when( parentJobMeta.findSlaveServer( REMOTE_SLAVE_SERVER_NAME ) ).thenReturn( slaveServer );
    when( slaveServer.getLogChannel() ).thenReturn( log );
    when( log.getLogLevel() ).thenReturn( LogLevel.BASIC );
    when( slaveServer.sendXML( anyString(), anyString() ) ).thenReturn( REPLY );
    when( slaveServer.execService( anyString() ) ).thenReturn( REPLY );
    when( slaveServer.getJobStatus( anyString(), anyString(), anyInt() ) ).thenReturn( status );
    when( status.getResult() ).thenReturn( mock( Result.class ) );
    when( status.getLoggingString() ).thenReturn( LOG );

    file = Files.createTempFile( "file", "" );

    doReturn( LOG_FILE_NAME ).when( job ).getLogFilename();
    doReturn( file.toString() ).when( job ).environmentSubstitute( LOG_FILE_NAME );
    doReturn( REMOTE_SLAVE_SERVER_NAME ).when( job ).environmentSubstitute( REMOTE_SLAVE_SERVER_NAME );
    doReturn( jobMeta ).when( job ).getJobMeta( any( Repository.class ), any( VariableSpace.class ) );
    doNothing().when( job ).copyVariablesFrom( anyObject() );
    doNothing().when( job ).setParentVariableSpace( anyObject() );

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
    String result = Files.lines( file ).collect( Collectors.joining( "" ) );
    assertTrue( result.contains( LOG ) );
  }
}
