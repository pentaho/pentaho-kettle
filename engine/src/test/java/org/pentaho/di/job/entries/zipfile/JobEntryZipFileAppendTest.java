/*
 * ! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.job.entries.zipfile;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JobEntryZipFileAppendTest {

  @BeforeClass
  public static void init() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void appendingPreservesExistingArchiveEntries() throws Exception {
    File directory = Files.createTempDirectory( "JobEntryZipFileAppendTest" ).toFile();
    File existingSource = writeFile( directory, "old.txt", "old content" );
    File newSource = writeFile( directory, "new.txt", "new content" );
    File archive = new File( directory, "archive.zip" );

    try {
      JobEntryZipFile entry = new JobEntryZipFile();
      JobMeta jobMeta = new JobMeta();
      Job parentJob = new Job( null, jobMeta );
      entry.setParentJobMeta( jobMeta );

      assertTrue( entry.processRowFile( parentJob, new Result(), archive.getAbsolutePath(), null, null,
        existingSource.getAbsolutePath(), null, false ) );

      entry.ifZipFileExists = 1;
      assertTrue( entry.processRowFile( parentJob, new Result(), archive.getAbsolutePath(), null, null,
        newSource.getAbsolutePath(), null, false ) );

      Set<String> expectedEntryNames = new HashSet<>();
      expectedEntryNames.add( existingSource.getName() );
      expectedEntryNames.add( newSource.getName() );
      assertEquals( expectedEntryNames, getZipEntryNames( archive ) );
    } finally {
      Files.deleteIfExists( archive.toPath() );
      Files.deleteIfExists( existingSource.toPath() );
      Files.deleteIfExists( newSource.toPath() );
      Files.deleteIfExists( directory.toPath() );
    }
  }

  @Test
  public void createsAppendTemporaryFileBesideTargetArchive() throws IOException {
    File directory = Files.createTempDirectory( "JobEntryZipFileTempTest" ).toFile();
    File archive = new File( directory, "archive.zip" );
    File temporaryArchive = JobEntryZipFile.createTemporaryZipFile( archive );

    try {
      assertEquals( archive.getAbsoluteFile().getParentFile(), temporaryArchive.getParentFile() );
    } finally {
      Files.deleteIfExists( temporaryArchive.toPath() );
      Files.deleteIfExists( directory.toPath() );
    }
  }

  @Test
  public void appendingAppliesCompressionRateToExistingEntries() throws Exception {
    File directory = Files.createTempDirectory( "JobEntryZipFileCompressionTest" ).toFile();
    byte[] content = new byte[ 8192 ];
    Arrays.fill( content, (byte) 'a' );
    File existingSource = writeFile( directory, "old.txt", content );
    File newSource = writeFile( directory, "new.txt", content );
    File archive = new File( directory, "archive.zip" );

    try {
      JobEntryZipFile entry = new JobEntryZipFile();
      JobMeta jobMeta = new JobMeta();
      Job parentJob = new Job( null, jobMeta );
      entry.setParentJobMeta( jobMeta );

      assertTrue( entry.processRowFile( parentJob, new Result(), archive.getAbsolutePath(), null, null,
        existingSource.getAbsolutePath(), null, false ) );

      entry.ifZipFileExists = 1;
      entry.compressionRate = 0;
      assertTrue( entry.processRowFile( parentJob, new Result(), archive.getAbsolutePath(), null, null,
        newSource.getAbsolutePath(), null, false ) );

      try ( ZipFile zipFile = new ZipFile( archive ) ) {
        assertTrue( zipFile.getEntry( existingSource.getName() ).getCompressedSize() > content.length / 2 );
        assertTrue( zipFile.getEntry( newSource.getName() ).getCompressedSize() > content.length / 2 );
      }
    } finally {
      Files.deleteIfExists( archive.toPath() );
      Files.deleteIfExists( existingSource.toPath() );
      Files.deleteIfExists( newSource.toPath() );
      Files.deleteIfExists( directory.toPath() );
    }
  }

  @Test
  public void appendOnlySupportsLocalTargets() {
    assertTrue( JobEntryZipFile.isLocalFile( "/tmp/archive.zip" ) );
    assertTrue( JobEntryZipFile.isLocalFile( "file:///tmp/archive.zip" ) );
    assertFalse( JobEntryZipFile.isLocalFile( "s3://bucket/archive.zip" ) );
    assertFalse( JobEntryZipFile.isLocalFile( "hdfs://namenode/archive.zip" ) );
  }

  private File writeFile( File directory, String name, String content ) throws IOException {
    return writeFile( directory, name, content.getBytes( StandardCharsets.UTF_8 ) );
  }

  private File writeFile( File directory, String name, byte[] content ) throws IOException {
    File file = new File( directory, name );
    Files.write( file.toPath(), content );
    return file;
  }

  private Set<String> getZipEntryNames( File archive ) throws IOException {
    Set<String> entryNames = new HashSet<>();
    try ( ZipInputStream input = new ZipInputStream( Files.newInputStream( archive.toPath() ) ) ) {
      ZipEntry entry;
      while ( ( entry = input.getNextEntry() ) != null ) {
        entryNames.add( entry.getName() );
      }
    }
    return entryNames;
  }
}