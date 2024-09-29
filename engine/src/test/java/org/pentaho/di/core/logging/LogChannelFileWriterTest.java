/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.logging;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

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

    doAnswer( invocationOnMock -> {
      Thread.sleep( 2000 );
      return null; } )
            .when( outputStream ).close();

    writer.startLogging();

    Thread.sleep( 500 );

    verify( outputStream, atLeastOnce() ).write( any( byte[].class ) );
    verify( outputStream, atLeastOnce() ).flush();
    verify( outputStream, never() ).close();

    writer.stopLogging();

    verify( outputStream ).close();

  }
}
