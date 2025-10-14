/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.s3common;

import java.io.File;

public class TestCleanupUtil {
  private TestCleanupUtil() { }

  public static void cleanUpLogsDir() {
    File logsDir = new File( "logs" );
    if ( logsDir.exists() && logsDir.isDirectory() ) {
      File[] files = logsDir.listFiles();
      if ( files != null ) {
        for ( File f : files ) {
          f.delete();
        }
      }
      logsDir.delete();
    }
  }
}
