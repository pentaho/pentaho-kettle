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

package org.pentaho.di.core.logging;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.OutputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogChannelFileWriterTest {

  @Test
  public void test() throws Exception {
    String id = "1";
    String logMessage = "Log message";

    FileObject fileObject = mock( FileObject.class );
    FileContent fileContent = mock( FileContent.class );
    OutputStream outputStream = mock( OutputStream.class );

    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getOutputStream( anyBoolean() ) ).thenReturn( outputStream );

    LogChannelFileWriter writer = new LogChannelFileWriter( id, fileObject, false );

    LoggingRegistry.getInstance().getLogChannelFileWriterBuffer( id ).addEvent(
            new KettleLoggingEvent( logMessage, System.currentTimeMillis(), LogLevel.BASIC ) );

    writer.flush();

    ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass( byte[].class );
    verify( outputStream ).write( captor.capture() );

    String arguments = new String( captor.getValue() );
    assertTrue( arguments.contains( logMessage ) );
  }
}
