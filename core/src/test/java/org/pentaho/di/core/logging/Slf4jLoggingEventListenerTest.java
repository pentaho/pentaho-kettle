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

package org.pentaho.di.core.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryObjectType;
import org.slf4j.Logger;

import java.util.function.Function;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.logging.LogLevel.BASIC;
import static org.pentaho.di.core.logging.LogLevel.ERROR;


@RunWith ( MockitoJUnitRunner.class )
public class Slf4jLoggingEventListenerTest {

  @Mock private Logger transLogger, jobLogger, diLogger;
  @Mock private KettleLoggingEvent logEvent;
  @Mock private LoggingObjectInterface loggingObject;
  @Mock private LogMessage message;
  @Mock private Function<String, LoggingObjectInterface> logObjProvider;
  @Mock private RepositoryDirectory repositoryDirectory;

  private String logChannelId = "logChannelId";
  private String msgText = "message";
  private String messageSub = "subject";
  private String testPath = "/test/path";
  private LogLevel logLevel = BASIC;


  private Slf4jLoggingEventListener listener = new Slf4jLoggingEventListener();

  @Before
  public void before() {
    listener.transLogger = transLogger;
    listener.jobLogger = jobLogger;
    listener.diLogger = diLogger;
    listener.logObjProvider = logObjProvider;
    when( logEvent.getMessage() ).thenReturn( message );
    when( message.getLogChannelId() ).thenReturn( logChannelId );
    when( message.getLevel() ).thenReturn( logLevel );
    when( message.getMessage() ).thenReturn( msgText );
    when( message.getSubject() ).thenReturn( messageSub );
  }

  @Test
  public void testAddLogEventNoRegisteredLogObject() {
    listener.eventAdded( logEvent );
    verify( diLogger ).info( messageSub + " " + msgText );

    when( message.getLevel() ).thenReturn( ERROR );
    listener.eventAdded( logEvent );
    verify( diLogger ).error( messageSub + " " + msgText );
    verifyNoInteractions( transLogger );
    verifyNoInteractions( jobLogger );
  }

  @Test
  public void testAddLogEventTrans() {
    when( logObjProvider.apply( logChannelId ) ).thenReturn( loggingObject );
    when( loggingObject.getObjectType() ).thenReturn( LoggingObjectType.TRANS );
    when( loggingObject.getFilename() ).thenReturn( "filename" );
    when( message.getLevel() ).thenReturn( LogLevel.BASIC );
    listener.eventAdded( logEvent );


    verify( transLogger ).info( "[filename]  " + msgText );
    when( message.getLevel() ).thenReturn( LogLevel.ERROR );
    listener.eventAdded( logEvent );
    verify( transLogger ).error( "[filename]  " + msgText );
    verifyNoInteractions( diLogger );
    verifyNoInteractions( jobLogger );
  }

  @Test
  public void testAddLogEventJob() {
    when( logObjProvider.apply( logChannelId ) ).thenReturn( loggingObject );
    when( loggingObject.getObjectType() ).thenReturn( LoggingObjectType.JOB );
    when( loggingObject.getFilename() ).thenReturn( "filename" );
    when( message.getLevel() ).thenReturn( LogLevel.BASIC );
    listener.eventAdded( logEvent );


    verify( jobLogger ).info( "[filename]  " + msgText );

    when( message.getLevel() ).thenReturn( LogLevel.ERROR );
    listener.eventAdded( logEvent );
    verify( jobLogger ).error( "[filename]  " + msgText );
    verifyNoInteractions( diLogger );
    verifyNoInteractions( transLogger );
  }

  @Test
  public void testJobWithAndWithoutFilename() {
    when( logObjProvider.apply( logChannelId ) ).thenReturn( loggingObject );
    when( loggingObject.getObjectType() ).thenReturn( LoggingObjectType.JOB );
    when( loggingObject.getObjectName() ).thenReturn( "TestJob" );
    when( loggingObject.getFilename() ).thenReturn( "filename" );
    when( loggingObject.getRepositoryDirectory() ).thenReturn( repositoryDirectory );
    when( repositoryDirectory.getPath() ).thenReturn( "/" );
    when( message.getLevel() ).thenReturn( LogLevel.BASIC );
    listener.eventAdded( logEvent );
    verify( jobLogger ).info( "[filename]  " + msgText );

    when( repositoryDirectory.getPath() ).thenReturn( testPath );
    listener.eventAdded( logEvent );
    verify( jobLogger ).info( "[" + testPath + "/filename]  " + msgText );

    when( loggingObject.getFilename() ).thenReturn( null );
    listener.eventAdded( logEvent );
    verify( jobLogger ).info( "[" + testPath + "/TestJob"
      + RepositoryObjectType.JOB.getExtension() + "]  " + msgText );
  }

  @Test
  public void testTransWithAndWithoutFilename() {
    when( logObjProvider.apply( logChannelId ) ).thenReturn( loggingObject );
    when( loggingObject.getObjectType() ).thenReturn( LoggingObjectType.TRANS );
    when( loggingObject.getObjectName() ).thenReturn( "TestTrans" );
    when( loggingObject.getFilename() ).thenReturn( "filename" );
    when( loggingObject.getRepositoryDirectory() ).thenReturn( repositoryDirectory );
    when( repositoryDirectory.getPath() ).thenReturn( "/" );
    when( message.getLevel() ).thenReturn( LogLevel.BASIC );
    listener.eventAdded( logEvent );
    verify( transLogger ).info( "[filename]  " + msgText );

    when( repositoryDirectory.getPath() ).thenReturn( testPath );
    listener.eventAdded( logEvent );
    verify( transLogger ).info( "[" + testPath + "/filename]  " + msgText );

    when( loggingObject.getFilename() ).thenReturn( null );
    listener.eventAdded( logEvent );
    verify( transLogger ).info( "[" + testPath + "/TestTrans"
      + RepositoryObjectType.TRANSFORMATION.getExtension() + "]  " + msgText );
  }
}
