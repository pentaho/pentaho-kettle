/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.www;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.UUIDUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;


public class RegisterPackageServletIT {

  protected RegisterPackageServlet servlet;

  private static final String ZIP_FILE_NAME = "jobExport.zip";

  private static final String ZIP_FILE_PATH = "src/it/resources/org/pentaho/di/www/" + ZIP_FILE_NAME;


  @BeforeClass
  public static void beforeAll() throws Exception
  {

    if ( getBaseTempDir().exists() ) {
      FileUtils.cleanDirectory(getBaseTempDir());
    }

  }

  @Before
  public void setup() {

    servlet = new RegisterPackageServlet();
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    servlet.setup(new TransformationMap(),
            new JobMap(),
            new SocketRepository( logChannelInterface ) ,
            new ArrayList<>() );
  }


  @Test
  public void copyRequestToDirectory() throws  Exception {

    File fileZip = new File(ZIP_FILE_PATH);
    InputStream inputStream = new FileInputStream( fileZip );
    File outputDirectory = createRandomTempDir("output" );

    assertTrue( outputDirectory.exists() );
    assertTrue( outputDirectory.isDirectory() );

    String archiveUrl = servlet.copyRequestToDirectory( inputStream, outputDirectory.getAbsolutePath() );

    // Verify

    // check a file got copied
    File fileArchiveUrl = new File( archiveUrl );
    assertTrue( fileArchiveUrl.exists() );
    assertTrue( fileArchiveUrl.isFile() );

    //check in expected directory
    File expectedArchiveUrl = new File (outputDirectory, fileArchiveUrl.getName() );
    assertEquals( expectedArchiveUrl.getAbsolutePath(), fileArchiveUrl.getAbsolutePath() );

    // check bytes are the same
    long delta = 10000; // a small difference
    long diff = Math.abs( fileZip.getTotalSpace() - fileArchiveUrl.getTotalSpace() );
    assertTrue( diff < delta );

  }

  @Test
  public void extract() throws Exception {

    // Setup
    File fileExtractDir = createRandomTempDir("extract" );
    FileUtils.copyFileToDirectory( new File( ZIP_FILE_PATH ),  fileExtractDir );
    File fileZip = new File( fileExtractDir, ZIP_FILE_NAME );
    assertTrue ( fileZip.exists() );

    String zipBaseUrl = servlet.extract( fileZip.getAbsolutePath() );

    // Verify
    File dirZipBaseUrl = new File( zipBaseUrl );

    assertTrue ( dirZipBaseUrl.exists() );
    assertEquals ( fileZip.getParentFile().getAbsolutePath(),  dirZipBaseUrl.getAbsolutePath() );

    assertEquals ( 6, dirZipBaseUrl.listFiles().length);

    Set<String> expectedFilesNames = new HashSet<>( Arrays.asList(
        "AddYear.ktr",
        "CurrentDate.kjb",
        "DataRowsCustomer.ktr",
        "GrabData.kjb",
        ZIP_FILE_NAME,
        "__job_execution_configuration__.xml"
        )
      );

    for (File children : dirZipBaseUrl.listFiles() )
    {
      expectedFilesNames.remove( children.getName() );
    }

    assertTrue("Some files are missing: " + expectedFilesNames.toString(),  expectedFilesNames.isEmpty() );

  }

  @Test
  public void deleteArchive() throws Exception
  {
    // Setup
    File fileDeleteDir = createRandomTempDir("delete" );
    FileUtils.copyFileToDirectory( new File( ZIP_FILE_PATH ),  fileDeleteDir );

    File fileZip = new File( fileDeleteDir, ZIP_FILE_NAME );
    assertTrue ( fileZip.exists() );

    servlet.deleteArchive( fileZip.getAbsolutePath() );

    assertFalse( fileZip.exists() );

  }


  @AfterClass
  public static void afterAll() throws Exception
  {

    if ( getBaseTempDir().exists() ) {
      FileUtils.cleanDirectory(getBaseTempDir());
    }

  }

  /**
   * Create temporary unique directory for testing.
   * <p>
   * format:
   * &nbsp; {JAVA_IO_TMPDIR}/RegisterPackageServletIT/export_4a3cfb7b-c850-11e9-9042-fd32c1018b37</p>
   * @param directoryPrefix
   * @return
   * @throws IOException
   */
  protected File createRandomTempDir(String directoryPrefix) throws IOException
  {

    File tempDir = FileUtils.getFile( getBaseTempDir(), directoryPrefix + "_" + UUIDUtil.getUUIDAsString()  );
    if (! tempDir.mkdirs() )
    {
      throw new IOException( "could not create temporary directory: " + tempDir.getAbsolutePath() );
    }

    return tempDir;

  }

  /**
   * Get base temporary directory.
   * @return
   */
  protected static File getBaseTempDir()
  {
    return new File( System.getProperty( "java.io.tmpdir" ),  RegisterPackageServletIT.class.getSimpleName() );
  }


}
