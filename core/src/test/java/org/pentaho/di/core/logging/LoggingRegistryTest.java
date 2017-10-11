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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LoggingRegistryTest {
  public static final String LOG_CHANEL_ID_PARENT = "parent-chanel-id";
  public static final String LOG_CHANEL_ID_CHILD = "child-chanel-id";
  public static final String STRING_DEFAULT = "<def>";

  @Test
  public void correctLogIdReturned_WhenLogObjectRegisteredAlready() {
    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    LoggingObject parent = new LoggingObject( new SimpleLoggingObject( "parent", LoggingObjectType.TRANS, null ) );
    parent.setLogChannelId( LOG_CHANEL_ID_PARENT );

    LoggingObject child = new LoggingObject( new SimpleLoggingObject( "child", LoggingObjectType.STEP, parent ) );
    child.setLogChannelId( LOG_CHANEL_ID_CHILD );

    loggingRegistry.getMap().put( STRING_DEFAULT, child );

    String logChanelId = loggingRegistry.registerLoggingSource( child );

    assertEquals( logChanelId, LOG_CHANEL_ID_CHILD );
  }

  @Test
  public void testRegisterFileWriter() {
    String id = "1";

    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    LogChannelFileWriterBuffer buffer = new LogChannelFileWriterBuffer( id );
    loggingRegistry.registerLogChannelFileWriterBuffer( buffer );

    assertNotNull( loggingRegistry.getLogChannelFileWriterBuffer( id ) );
  }

  @Test
  public void testFileWritersIds() {
    String id = "1";

    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    LogChannelFileWriterBuffer buffer = new LogChannelFileWriterBuffer( id );
    loggingRegistry.registerLogChannelFileWriterBuffer( buffer );

    assertNotNull( loggingRegistry.getLogChannelFileWriterBufferIds() );
  }

  @Test
  public void testRemoveFileWriter() {
    String id = "1";

    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    LogChannelFileWriterBuffer buffer = new LogChannelFileWriterBuffer( id );
    loggingRegistry.registerLogChannelFileWriterBuffer( buffer );

    loggingRegistry.removeLogChannelFileWriterBuffer( id );

    assertNull( loggingRegistry.getLogChannelFileWriterBuffer( id ) );
  }
}
