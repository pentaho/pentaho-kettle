/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.www.SlaveServerJobStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

  private static Path FILE;

  @BeforeClass
  public static void setUpBeforeClass() {
    KettleLogStore.init();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    // Wait for LogChannelFileWriter to close outputstream
    Thread.sleep( 1000 );
    FileUtils.forceDelete( FILE.toFile() );
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

    FILE = Files.createTempFile( "file", "" );

    doReturn( LOG_FILE_NAME ).when( job ).getLogFilename();
    doReturn( FILE.toString() ).when( job ).environmentSubstitute( LOG_FILE_NAME );
    doReturn( REMOTE_SLAVE_SERVER_NAME ).when( job ).environmentSubstitute( REMOTE_SLAVE_SERVER_NAME );
    doReturn( jobMeta ).when( job ).getJobMeta( any( Repository.class ), any( VariableSpace.class ) );
    doNothing().when( job ).copyVariablesFrom( any( VariableSpace.class ) );
    doNothing().when( job ).setParentVariableSpace( any( VariableSpace.class ) );

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
    job.setParentJobMeta( parentJobMeta );

    job.execute( new Result(), 0 );
    String result = Files.lines( FILE ).collect( Collectors.joining( "" ) );
    assertTrue( result.contains( LOG ) );
  }

  @Test
  public void testPDI18776() throws KettleException, IOException {
    KettleEnvironment.init();
    String path = getClass().getResource( "Random_value.ktr" ).getPath();
    Variables variables = new Variables();
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    String childJobStep = trans.getSteps().get( 1 ).step.toString();
    assertTrue( childJobStep.contains( "Dummy" ) );
  }

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

}
