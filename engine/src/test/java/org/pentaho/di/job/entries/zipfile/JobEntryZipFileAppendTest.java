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
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
  public void appendingToVfsTargetPreservesExistingArchiveEntries() throws Exception {
    File directory = Files.createTempDirectory( "JobEntryZipFileVfsAppendTest" ).toFile();
    File existingSource = writeFile( directory, "old.txt", "old content" );
    File newSource = writeFile( directory, "new.txt", "new content" );
    String archiveName = "ram://JobEntryZipFileAppendTest/" + UUID.randomUUID() + "/archive.zip";
    JobEntryZipFile entry = new JobEntryZipFile();

    try {
      JobMeta jobMeta = new JobMeta();
      Job parentJob = new Job( null, jobMeta );
      entry.setParentJobMeta( jobMeta );
      createParentFolder( archiveName, entry );

      assertTrue( entry.processRowFile( parentJob, new Result(), archiveName, null, null,
        existingSource.getAbsolutePath(), null, false ) );

      entry.ifZipFileExists = 1;
      assertTrue( entry.processRowFile( parentJob, new Result(), archiveName, null, null,
        newSource.getAbsolutePath(), null, false ) );

      assertEquals( new HashSet<>( Arrays.asList( existingSource.getName(), newSource.getName() ) ),
        getZipEntryNames( archiveName, entry ) );
    } finally {
      deleteVfsFile( archiveName, entry );
      Files.deleteIfExists( existingSource.toPath() );
      Files.deleteIfExists( newSource.toPath() );
      Files.deleteIfExists( directory.toPath() );
    }
  }

  @Test
  public void appendingSameEntryNameFailsWithoutReplacingTheArchive() throws Exception {
    File directory = Files.createTempDirectory( "JobEntryZipFileDuplicateAppendTest" ).toFile();
    File existingDirectory = new File( directory, "existing" );
    File newDirectory = new File( directory, "new" );
    assertTrue( existingDirectory.mkdir() );
    assertTrue( newDirectory.mkdir() );
    File existingSource = writeFile( existingDirectory, "same.txt", "old content" );
    File newSource = writeFile( newDirectory, "same.txt", "new content" );
    File archive = new File( directory, "archive.zip" );

    try {
      JobEntryZipFile entry = new JobEntryZipFile();
      JobMeta jobMeta = new JobMeta();
      Job parentJob = new Job( null, jobMeta );
      entry.setParentJobMeta( jobMeta );

      assertTrue( entry.processRowFile( parentJob, new Result(), archive.getAbsolutePath(), null, null,
        existingSource.getAbsolutePath(), null, false ) );

      entry.ifZipFileExists = 1;
      assertFalse( entry.processRowFile( parentJob, new Result(), archive.getAbsolutePath(), null, null,
        newSource.getAbsolutePath(), null, false ) );

      assertEquals( 1, getZipEntryCount( archive, "same.txt" ) );
      try ( ZipFile zipFile = new ZipFile( archive ) ) {
        assertEquals( "old content", new String( zipFile.getInputStream( zipFile.getEntry( "same.txt" ) )
          .readAllBytes(),
          StandardCharsets.UTF_8 ) );
      }
    } finally {
      Files.deleteIfExists( archive.toPath() );
      Files.deleteIfExists( existingSource.toPath() );
      Files.deleteIfExists( newSource.toPath() );
      Files.deleteIfExists( existingDirectory.toPath() );
      Files.deleteIfExists( newDirectory.toPath() );
      Files.deleteIfExists( directory.toPath() );
    }
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
    try ( InputStream archiveInput = Files.newInputStream( archive.toPath() ) ) {
      return getZipEntryNames( archiveInput );
    }
  }

  private int getZipEntryCount( File archive, String entryName ) throws IOException {
    int count = 0;
    try ( InputStream archiveInput = Files.newInputStream( archive.toPath() );
          ZipInputStream input = new ZipInputStream( archiveInput ) ) {
      ZipEntry entry;
      while ( ( entry = input.getNextEntry() ) != null ) {
        if ( entryName.equals( entry.getName() ) ) {
          count++;
        }
      }
    }
    return count;
  }

  private Set<String> getZipEntryNames( String archiveName, JobEntryZipFile entry ) throws Exception {
    try ( org.apache.commons.vfs2.FileObject archive = KettleVFS.getInstance( DefaultBowl.getInstance() )
      .getFileObject( archiveName, entry );
          InputStream archiveInput = KettleVFS.getInputStream( archive ) ) {
      return getZipEntryNames( archiveInput );
    }
  }

  private Set<String> getZipEntryNames( InputStream archiveInput ) throws IOException {
    Set<String> entryNames = new HashSet<>();
    try ( ZipInputStream input = new ZipInputStream( archiveInput ) ) {
      ZipEntry entry;
      while ( ( entry = input.getNextEntry() ) != null ) {
        entryNames.add( entry.getName() );
      }
    }
    return entryNames;
  }

  private void createParentFolder( String archiveName, JobEntryZipFile entry ) throws Exception {
    try ( org.apache.commons.vfs2.FileObject archive = KettleVFS.getInstance( DefaultBowl.getInstance() )
      .getFileObject( archiveName, entry );
          org.apache.commons.vfs2.FileObject parent = archive.getParent() ) {
      parent.createFolder();
    }
  }

  private void deleteVfsFile( String archiveName, JobEntryZipFile entry ) throws Exception {
    try ( org.apache.commons.vfs2.FileObject archive = KettleVFS.getInstance( DefaultBowl.getInstance() )
      .getFileObject( archiveName, entry );
          org.apache.commons.vfs2.FileObject parent = archive.getParent() ) {
      if ( archive.exists() ) {
        archive.delete();
      }
      if ( parent != null && parent.exists() ) {
        parent.delete();
      }
    }
  }
}