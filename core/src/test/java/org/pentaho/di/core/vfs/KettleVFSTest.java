/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.vfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleFileException;

import java.io.File;

public class KettleVFSTest {

  /**
   * Test to validate that startsWitScheme() returns true if the fileName starts with
   * known protocol like zip: jar: then it returns true else returns false
   * @param fileName
   */
  @Test
  public void testStartsWithScheme() {
    String fileName = "zip:file:///SavedLinkedres.zip!Calculate median and percentiles using the group by steps.ktr";
    assertTrue( KettleVFS.startsWithScheme( fileName ) );

    fileName = "SavedLinkedres.zip!Calculate median and percentiles using the group by steps.ktr";
    assertFalse( KettleVFS.startsWithScheme( fileName ) );
  }


  @Test
  public void testCheckForSchemeSuccess() throws KettleFileException {
    String vfsFilename = "hdfs://hsbcmaster.labs.eag.hitachivantara.com:8020/tmp/acltest/";

    boolean test = "hdfs".equals( KettleVFS.getScheme( vfsFilename ) );
    assertTrue( test );
  }

  @Test( expected = KettleFileException.class )
  public void testCheckForSchemeFailWithException() throws KettleFileException {
    String invalidVfsFilename = "{\"textMessage\":{\"textMessage\":\" textMessage"
      + "textMessage \",\"textMessage\":{textMessage}}}";

    KettleVFS.getScheme( invalidVfsFilename );
  }

  @Test
  public void testCheckForSchemeFail() throws KettleFileException {
    String vfsFilename = "hdfs://hsbcmaster.labs.eag.hitachivantara.com:8020/tmp/acltest/";

    boolean test = "file".equals( KettleVFS.getScheme( vfsFilename ) );
    assertFalse( test );
  }

  @Test
  public void testIfSchemeIsRelativePath() {
    String vfsFilename = "file://hsbcmaster.labs.eag.hitachivantara.com:8020/tmp/acltest/";

    boolean testVfsFilename =  KettleVFS.isRelativePath( vfsFilename );
    assertFalse( testVfsFilename );
  }

  @Test
  public void testIfRelativePathIsRelativePath() {
    String relativePathVfsFilename = "/tmp/acltest/";

    boolean testRelativePathVfsFilename =  KettleVFS.isRelativePath( relativePathVfsFilename );
    assertTrue( testRelativePathVfsFilename );
  }

  @Test
  public void testIfBlankIsRelativePath() {
    String blankVfsFilename =  "";

    boolean testBlankVfsFilename =  KettleVFS.isRelativePath( blankVfsFilename );
    assertTrue( testBlankVfsFilename );
  }

  @Test
  public void testIfNullIsRelativePath() {
    String nullVfsFilename = null;

    boolean testNullVfsFilename = KettleVFS.isRelativePath( nullVfsFilename );
    assertTrue( testNullVfsFilename );
  }

  @Test
  public void testNormalizePathWithFile() {
    String vfsFilename = "\\\\tmp/acltest.txt";

    String testNormalizePath = KettleVFS.normalizePath( vfsFilename );
    assertTrue( testNormalizePath.startsWith( "file:/" )  );
  }

  @Test
  public void testNormalizePath() {
    String vfsFilename = "tmp/acltest";

    String testNormalizePath = KettleVFS.normalizePath( vfsFilename );
    assertEquals( new File( vfsFilename ).getAbsolutePath(), testNormalizePath );
  }

}
