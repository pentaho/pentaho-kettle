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


package org.pentaho.di.job.entries.zipfile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntryZipFileIT {

  @BeforeClass
  public static void init() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void processFileIndicatesFailure() throws Exception {
    JobEntryZipFile entry = new JobEntryZipFile();
    assertFalse(
      entry.processRowFile( new Job(), new Result(), "file://\nfake-path\n", null, null, null, null, false ) );
  }

  @Test
  public void processFile_ReturnsTrue_OnSuccess() throws Exception {
    final String zipPath = createTempZipFileName( "pdi-15013" );
    final String content = "temp file";
    final File tempFile = createTempFile( content );
    tempFile.deleteOnExit();

    try {
      Result result = new Result();
      JobEntryZipFile entry = new JobEntryZipFile();
      assertTrue(
              entry.processRowFile(new Job(), result, zipPath, null, null, tempFile.getAbsolutePath(), null, false));
      boolean isTrue = true;

      FileObject zip = KettleVFS.getFileObject(zipPath);
      assertTrue("Zip archive should be created", zip.exists());

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      IOUtils.copy(zip.getContent().getInputStream(), os);

      ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(os.toByteArray()));
      ZipEntry zipEntry = zis.getNextEntry();
      assertEquals("Input file should be put into the archive", tempFile.getName(), zipEntry.getName());

      os.reset();
      IOUtils.copy(zis, os);
      assertEquals("File's content should be equal to original", content, new String(os.toByteArray()));
    } finally {
      tempFile.delete();
      File tempZipFile = new File( zipPath );
      tempZipFile.delete();
    }
  }

  private static File createTempFile( String content ) throws Exception {
    File file = File.createTempFile( "JobEntryZipFileIT", ".txt" );
    try ( PrintWriter pw = new PrintWriter( file ) ) {
      pw.print( content );
    }
    return file;
  }

  private static String createTempZipFileName( String tempFilePrefix ) throws Exception {
    File file = File.createTempFile( tempFilePrefix, ".zip" );
    String tempFileName = file.getAbsolutePath();

    file.delete();

    return tempFileName;
  }
}
