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


import java.io.File;
import java.io.IOException;


public class KettleVFSTest {

  /**
   * Test to validate that startsWitScheme() returns true if the fileName starts with
   * known protocol like zip: jar: then it returns true else returns false
   * @param fileName
   */
  private static final String PROVIDER_PATTERN_SCHEME = "^[\\w\\d]+://(.*)";

  @Test
  public void testStartsWithScheme() {
    String fileName = "zip:file:///SavedLinkedres.zip!Calculate median and percentiles using the group by steps.ktr";
    assertTrue( KettleVFS.startsWithScheme( fileName ) );

    fileName = "SavedLinkedres.zip!Calculate median and percentiles using the group by steps.ktr";
    assertFalse( KettleVFS.startsWithScheme( fileName ) );
  }


  @Test
  public void testCheckForSchemeSuccess() throws IOException {
    String[] schemes = {"hdfs"};
    String vfsFilename = "hdfs://hsbcmaster:8020/tmp/acltest/";

    boolean testVfsFilename = KettleVFS.checkForSchemeProvider( schemes, true, vfsFilename, null, null );
    assertFalse( testVfsFilename );

  }

  @Test
  public void testCheckForSchemeFail() throws IOException {
    String[] schemes = {"file"};
    String vfsFilename = "hdfs://hsbcmaster:8020/tmp/acltest/";

    boolean testVfsFilename = KettleVFS.checkForSchemeProvider( schemes, true, vfsFilename, null, null );
    assertTrue( testVfsFilename );

  }

  @Test
  public void testCheckForSchemeIfRelativePath() throws IOException {
    String[] schemes = {"file"};
    String vfsFilename = "\"/tmp/acltest/\"";

    boolean testVfsFilename = KettleVFS.checkForSchemeProvider( schemes, true, vfsFilename, null, null );
    assertTrue( testVfsFilename );

  }

  @Test
  public void testCheckForSchemeIfBlank() throws IOException {
    String[] schemes = {"file"};
    String vfsFilename = " ";

    boolean testVfsFilename = KettleVFS.checkForSchemeProvider( schemes, true, vfsFilename, null, null );
    assertTrue( testVfsFilename );

  }

  @Test
  public void testCheckForSchemeIfNull() throws IOException {
    String[] schemes = {"file"};
    String vfsFilename = null;

    boolean testVfsFilename = KettleVFS.checkForSchemeProvider( schemes, true, vfsFilename, null, null );
    assertTrue( testVfsFilename );

  }

  @Test
  public void testNormalizePathWithFile() {
    String vfsFilename = "\\\\tmp/acltest.txt";

    String testNormalizePath = KettleVFS.normalizePath( vfsFilename, true );
    assertTrue( testNormalizePath.startsWith( "file:/" )  );
  }

  @Test
  public void testNormalizePath() {
    String vfsFilename = "tmp/acltest";

    String testNormalizePath = KettleVFS.normalizePath( vfsFilename, true );
    assertEquals( new File( vfsFilename ).getAbsolutePath(), testNormalizePath );
  }

  @Test
  public void testHasScheme() {
    String vfsFilename = "hdfs://hsbcmaster:8020/tmp/acltest/";

    boolean testVfsFilename = KettleVFS.hasScheme( vfsFilename, PROVIDER_PATTERN_SCHEME );
    assertTrue( testVfsFilename );

  }

  @Test
  public void testHasSchemeWithSpaces() {
    String vfsFilename = "/tmp/This is a text file4551613284841905296.txt";
    String vfsFilenameWithScheme = "hdfs://tmp/This is a text file4551613284841905296.txt";

    boolean testVfsFilename = KettleVFS.hasScheme( vfsFilename, PROVIDER_PATTERN_SCHEME );
    assertFalse( testVfsFilename );

    boolean testVfsFilenameWithScheme = KettleVFS.hasScheme( vfsFilenameWithScheme, PROVIDER_PATTERN_SCHEME );
    assertTrue( testVfsFilenameWithScheme );

  }

  @Test
  public void testHasSchemeWithVariables() {
    String vfsFilename = "${input_file}";
    String vfsFilenameWithScheme = "hdfs://${input_file}";

    boolean testVfsFilename = KettleVFS.hasScheme( vfsFilename, PROVIDER_PATTERN_SCHEME );
    assertFalse( testVfsFilename );

    boolean testVfsFilenameWithScheme = KettleVFS.hasScheme( vfsFilenameWithScheme, PROVIDER_PATTERN_SCHEME );
    assertTrue( testVfsFilenameWithScheme );

  }

  @Test
  public void testHasSchemeWithJSON() {
    String vfsFilename = "{\"textMessage\":{\"textMessage\":\" textMessage"
      + "textMessage \",\"textMessage\":{textMessage}}}";
    String vfsFilenameWithScheme = "hdfs://" + vfsFilename;

    boolean testVfsFilename = KettleVFS.hasScheme( vfsFilename, PROVIDER_PATTERN_SCHEME );
    assertFalse( testVfsFilename );

    boolean testVfsFilenameWithScheme = KettleVFS.hasScheme( vfsFilenameWithScheme, PROVIDER_PATTERN_SCHEME );
    assertTrue( testVfsFilenameWithScheme );

  }

}
