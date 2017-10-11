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
    final String zipPath = "ram://pdi-15013.zip";
    final String content = "temp file";
    final File tempFile = createTempFile( content );
    tempFile.deleteOnExit();
    try {
      Result result = new Result();
      JobEntryZipFile entry = new JobEntryZipFile();
      assertTrue(
        entry.processRowFile( new Job(), result, zipPath, null, null, tempFile.getAbsolutePath(), null, false ) );
    } finally {
      tempFile.delete();
    }

    FileObject zip = KettleVFS.getFileObject( zipPath );
    assertTrue( "Zip archive should be created", zip.exists() );

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    IOUtils.copy( zip.getContent().getInputStream(), os );

    ZipInputStream zis = new ZipInputStream( new ByteArrayInputStream( os.toByteArray() ) );
    ZipEntry entry = zis.getNextEntry();
    assertEquals( "Input file should be put into the archive", tempFile.getName(), entry.getName() );

    os.reset();
    IOUtils.copy( zis, os );
    assertEquals( "File's content should be equal to original", content, new String( os.toByteArray() ) );
  }

  private static File createTempFile( String content ) throws Exception {
    File file = File.createTempFile( "JobEntryZipFileIT", ".txt" );
    try ( PrintWriter pw = new PrintWriter( file ) ) {
      pw.print( content );
    }
    return file;
  }
}
