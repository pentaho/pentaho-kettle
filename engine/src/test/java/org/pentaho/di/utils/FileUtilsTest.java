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

package org.pentaho.di.utils;

import java.io.File;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.FileUtil;

public class FileUtilsTest {

  @Test
  public void testCreateTempDir() {
    String tempDir = TestUtils.createTempDir();
    if ( tempDir != null ) {
      File fl = new File( tempDir );
      assertTrue( "Dir should be created", fl.exists() );
      try {
        fl.delete();
      } catch ( Exception ex ) {
        ex.printStackTrace();
      }
    }
  }

  @Test
  public void testCreateParentFolder() {
    String tempDir = TestUtils.createTempDir();
    String suff = tempDir.substring( tempDir.lastIndexOf( File.separator ) + 1 );
    tempDir += File.separator + suff + File.separator + suff;
    assertTrue( "Dir should be created", FileUtil.createParentFolder( getClass(), tempDir, true, new LogChannel(
      this ), null ) );
    File fl = new File( tempDir.substring( 0, tempDir.lastIndexOf( File.separator ) ) );
    assertTrue( "Dir should exist", fl.exists() );
    fl.delete();
    new File( tempDir ).delete();
  }

  @Test
  public void testIsFullyQualified() {
    assertTrue( FileUtil.isFullyQualified( "/test" ) );
    assertTrue( FileUtil.isFullyQualified( "\\test" ) );
  }
}
