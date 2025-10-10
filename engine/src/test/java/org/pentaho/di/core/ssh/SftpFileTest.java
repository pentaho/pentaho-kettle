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

package org.pentaho.di.core.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.junit.Test;

/**
 * Unit tests for SftpFile class.
 * Tests constructor, getters, and edge cases.
 */
public class SftpFileTest {

  @Test
  public void testConstructorAndGetters() {
    String name = "testfile.txt";
    boolean isDirectory = false;
    long size = 1024;
    Instant modified = Instant.ofEpochSecond( 1609459200 ); // 2021-01-01 00:00:00 UTC

    SftpFile file = new SftpFile( name, isDirectory, size, modified );

    assertEquals( name, file.getName() );
    assertEquals( isDirectory, file.isDirectory() );
    assertEquals( size, file.getSize() );
    assertEquals( modified, file.getModified() );
  }

  @Test
  public void testDirectoryFile() {
    SftpFile dir = new SftpFile( "mydir", true, 0, Instant.now() );

    assertTrue( dir.isDirectory() );
    assertEquals( "mydir", dir.getName() );
  }

  @Test
  public void testRegularFile() {
    SftpFile file = new SftpFile( "data.csv", false, 5000, Instant.now() );

    assertFalse( file.isDirectory() );
    assertEquals( "data.csv", file.getName() );
    assertEquals( 5000, file.getSize() );
  }

  @Test
  public void testZeroSizeFile() {
    SftpFile emptyFile = new SftpFile( "empty.txt", false, 0, Instant.now() );

    assertEquals( 0, emptyFile.getSize() );
    assertFalse( emptyFile.isDirectory() );
  }

  @Test
  public void testLargeFile() {
    long largeSize = 10_000_000_000L; // 10 GB
    SftpFile largeFile = new SftpFile( "large.iso", false, largeSize, Instant.now() );

    assertEquals( largeSize, largeFile.getSize() );
  }

  @Test
  public void testEpochTimestamp() {
    SftpFile file = new SftpFile( "old.txt", false, 100, Instant.EPOCH );

    assertEquals( Instant.EPOCH, file.getModified() );
  }

  @Test
  public void testRecentTimestamp() {
    Instant now = Instant.now();
    SftpFile file = new SftpFile( "new.txt", false, 200, now );

    assertEquals( now, file.getModified() );
  }

  @Test
  public void testFileWithSpecialCharactersInName() {
    String specialName = "file with spaces & special-chars_123.txt";
    SftpFile file = new SftpFile( specialName, false, 512, Instant.now() );

    assertEquals( specialName, file.getName() );
  }

  @Test
  public void testFileWithPathInName() {
    // Some SFTP implementations may include path separators
    String pathName = "subdir/file.txt";
    SftpFile file = new SftpFile( pathName, false, 256, Instant.now() );

    assertEquals( pathName, file.getName() );
  }

  @Test
  public void testDotFiles() {
    SftpFile dotFile = new SftpFile( ".hidden", false, 100, Instant.now() );
    SftpFile currentDir = new SftpFile( ".", true, 0, Instant.now() );
    SftpFile parentDir = new SftpFile( "..", true, 0, Instant.now() );

    assertEquals( ".hidden", dotFile.getName() );
    assertFalse( dotFile.isDirectory() );

    assertEquals( ".", currentDir.getName() );
    assertTrue( currentDir.isDirectory() );

    assertEquals( "..", parentDir.getName() );
    assertTrue( parentDir.isDirectory() );
  }
}
