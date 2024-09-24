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
