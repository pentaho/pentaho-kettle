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

package org.pentaho.di.core.logging;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.OutputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@RunWith( MockitoJUnitRunner.class )
public class LogChannelFileWriterTest {

  String id = "1";
  String logMessage = "Log message";

  @Mock
  FileObject fileObject;
  @Mock
  FileContent fileContent;
  @Mock
  OutputStream outputStream;
  @Captor
  ArgumentCaptor<byte[]> captor;

  @Before
  public void setup() throws Exception {
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getOutputStream( anyBoolean() ) ).thenReturn( outputStream );
  }

  @Test
  public void test() throws Exception {

    LogChannelFileWriter writer = new LogChannelFileWriter( id, fileObject, false );

    LoggingRegistry.getInstance().getLogChannelFileWriterBuffer( id ).addEvent(
            new KettleLoggingEvent( logMessage, System.currentTimeMillis(), LogLevel.BASIC ) );

    writer.flush();

    verify( outputStream ).write( captor.capture() );

    String arguments = new String( captor.getValue() );
    assertTrue( arguments.contains( logMessage ) );
  }

  @Test
  public void testStartStopLogging() throws Exception {
    LogChannelFileWriter writer = new LogChannelFileWriter( id, fileObject, false );
    writer.startLogging();
    Thread.sleep( 2000 );

    verify( outputStream, atLeastOnce() ).write( any( byte[].class ) );
    verify( outputStream, atLeastOnce() ).flush();
    verify( outputStream, never() ).close();

    writer.stopLogging();

    Thread.sleep( 2000 );
    verify( outputStream ).close();
  }
}
