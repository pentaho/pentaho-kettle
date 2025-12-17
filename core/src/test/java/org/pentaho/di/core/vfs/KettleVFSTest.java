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


package org.pentaho.di.core.vfs;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


import org.junit.Test;


import java.io.File;


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
  public void testCheckForSchemeSuccess() {
    String[] schemes = {"hdfs"};
    String vfsFilename = "hdfs://hsbcmaster:8020/tmp/acltest/";

    assertNotNull( KettleVFS.getScheme( schemes, vfsFilename ) );
  }

  @Test
  public void testCheckForSchemeFail() {
    String[] schemes = {"file"};
    String vfsFilename = "hdfs://hsbcmaster:8020/tmp/acltest/";

    assertNull( KettleVFS.getScheme( schemes, vfsFilename ) );
  }

  @Test
  public void testCheckForSchemeIfRelativePath() {
    String[] schemes = {"file"};
    String vfsFilename = "\"/tmp/acltest/\"";

    assertNull( KettleVFS.getScheme( schemes, vfsFilename ) );
  }

  @Test
  public void testCheckForSchemeIfBlank() {
    String[] schemes = {"file"};
    String vfsFilename = " ";

    assertNull( KettleVFS.getScheme( schemes, vfsFilename ) );
  }

  @Test
  public void testNormalizePathWithFile() {
    String vfsFilename = "\\\\tmp/acltest.txt";

    String testNormalizePath = KettleVFS.normalizePath( vfsFilename, "someScheme" );
    assertTrue( testNormalizePath.startsWith( "file:/" )  );
  }

  @Test
  public void testNormalizePath() {
    String vfsFilename = "tmp/acltest";

    String testNormalizePath = KettleVFS.normalizePath( vfsFilename, null );
    assertEquals( new File( vfsFilename ).getAbsolutePath(), testNormalizePath );
  }

  @Test
  public void testHasScheme() {
    String vfsFilename = "hdfs://hsbcmaster:8020/tmp/acltest/";

    boolean testVfsFilename = KettleVFS.hasSchemePattern( vfsFilename, PROVIDER_PATTERN_SCHEME );
    assertTrue( testVfsFilename );

  }

  @Test
  public void testHasSchemeWithSpaces() {
    String vfsFilename = "/tmp/This is a text file4551613284841905296.txt";
    String vfsFilenameWithScheme = "hdfs://tmp/This is a text file4551613284841905296.txt";

    boolean testVfsFilename = KettleVFS.hasSchemePattern( vfsFilename, PROVIDER_PATTERN_SCHEME );
    assertFalse( testVfsFilename );

    boolean testVfsFilenameWithScheme = KettleVFS.hasSchemePattern( vfsFilenameWithScheme, PROVIDER_PATTERN_SCHEME );
    assertTrue( testVfsFilenameWithScheme );

  }

  @Test
  public void testHasSchemeWithVariables() {
    String vfsFilename = "${input_file}";
    String vfsFilenameWithScheme = "hdfs://${input_file}";

    boolean testVfsFilename = KettleVFS.hasSchemePattern( vfsFilename, PROVIDER_PATTERN_SCHEME );
    assertFalse( testVfsFilename );

    boolean testVfsFilenameWithScheme = KettleVFS.hasSchemePattern( vfsFilenameWithScheme, PROVIDER_PATTERN_SCHEME );
    assertTrue( testVfsFilenameWithScheme );

  }

  @Test
  public void testHasSchemeWithJSON() {
    String vfsFilename = "{\"textMessage\":{\"textMessage\":\" textMessage"
      + "textMessage \",\"textMessage\":{textMessage}}}";
    String vfsFilenameWithScheme = "hdfs://" + vfsFilename;

    boolean testVfsFilename = KettleVFS.hasSchemePattern( vfsFilename, PROVIDER_PATTERN_SCHEME );
    assertFalse( testVfsFilename );

    boolean testVfsFilenameWithScheme = KettleVFS.hasSchemePattern( vfsFilenameWithScheme, PROVIDER_PATTERN_SCHEME );
    assertTrue( testVfsFilenameWithScheme );

  }

  @Test
  public void testNormalizeFilePath_NullOrEmpty() {
    assertNull( KettleVFS.normalizeFilePath( null ) );
    assertEquals( "", KettleVFS.normalizeFilePath( "" ) );
    assertEquals( "   ", KettleVFS.normalizeFilePath( "   " ) );
  }

  @Test
  public void testNormalizeFilePath_WithScheme() {
    // Paths with schemes should be returned as-is
    String hdfsPath = "hdfs://namenode:8020/user/data/file.txt";
    assertEquals( hdfsPath, KettleVFS.normalizeFilePath( hdfsPath ) );

    String s3Path = "s3://bucket/folder/file.txt";
    assertEquals( s3Path, KettleVFS.normalizeFilePath( s3Path ) );

    // file:// paths should be preserved
    String filePath = "file:///tmp/test.ktr";
    assertEquals( filePath, KettleVFS.normalizeFilePath( filePath ) );

    // file:// paths with URL-encoded characters should be preserved
    String encodedFilePath = "file:///tmp/my%20folder/test.ktr";
    assertEquals( encodedFilePath, KettleVFS.normalizeFilePath( encodedFilePath ) );
  }

  @Test
  public void testNormalizeFilePath_WithVariables() {
    // Paths with unresolved variables should be returned as-is
    String pathWithVar = "${Internal.Entry.Current.Directory}/Output.ktr";
    assertEquals( pathWithVar, KettleVFS.normalizeFilePath( pathWithVar ) );
  }

  @Test
  public void testNormalizeFilePath_LocalPathGetsFileScheme() {
    // Test that local paths get file:// prefix
    String localPath = "/tmp/test/file.ktr";
    String result = KettleVFS.normalizeFilePath( localPath );
    assertTrue( result.startsWith( "file://" ) );
    assertTrue( result.endsWith( "file.ktr" ) );
  }

}
