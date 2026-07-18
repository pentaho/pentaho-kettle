/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
