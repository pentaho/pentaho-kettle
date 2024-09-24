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
