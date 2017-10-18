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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LogChannelFileWriterBufferTest {

  @Test
  public void test() {
    String id = "1";
    String logMessage = "Log message";

    LogChannelFileWriterBuffer buffer = new LogChannelFileWriterBuffer( id );

    buffer.addEvent( new KettleLoggingEvent( logMessage, System.currentTimeMillis(), LogLevel.BASIC ) );

    String log = buffer.getBuffer().toString();
    assertTrue( log.contains( logMessage ) );
  }
}
