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


package org.pentaho.di.www;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class RegisterPackageServletIT {

  private static final String ZIP_FILE_NAME = "jobExport.zip";
  private static final String ZIP_FILE_PATH = "src/it/resources/org/pentaho/di/www/" + ZIP_FILE_NAME;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  protected RegisterPackageServlet servlet;


  @Before
  public void setup() {
    servlet = new RegisterPackageServlet();
    servlet.setup( new TransformationMap(), new JobMap(), new SocketRepository( mock( LogChannelInterface.class ) ),
      new ArrayList<>() );
  }

  @Test
  public void copyRequestToDirectory() throws Exception {
    // Get the contents of the Zip file as an InputStream
    File fileZip = new File( ZIP_FILE_PATH );
    InputStream inputStream = new FileInputStream( fileZip );

    // The InputStream should not be null
    assertNotNull( inputStream );

    // Execute (save the file to the given folder)
    String archiveUrl = servlet.copyRequestToDirectory( inputStream, temporaryFolder.getRoot().getAbsolutePath() );

    // Check that a file got copied
    File copiedFile = new File( archiveUrl );
    assertTrue( copiedFile.exists() );
    assertTrue( copiedFile.isFile() );

    // And that it was copied into the expected folder
    File expectedCopiedFile = new File( temporaryFolder.getRoot(), copiedFile.getName() );
    assertTrue( expectedCopiedFile.exists() );
    assertTrue( expectedCopiedFile.isFile() );

    // Check that the copied file has the expected size
    assertEquals( Files.size( fileZip.toPath() ), Files.size( copiedFile.toPath() ) );
  }

  @Test
  public void extract() throws Exception {
    // Copy the test Zip file to a working temporary folder
    FileUtils.copyFileToDirectory( new File( ZIP_FILE_PATH ), temporaryFolder.getRoot() );
    // Check that it is there
    File fileZip = new File( temporaryFolder.getRoot(), ZIP_FILE_NAME );
    assertTrue( fileZip.exists() );

    // Execute (extract to the same folder where the file is)
    String zipBaseUrl = servlet.extract( fileZip.getAbsolutePath() );

    // Check that the returned folder is the same folder where the file is
    File dirZipBaseUrl = new File( zipBaseUrl );
    assertTrue( dirZipBaseUrl.exists() );
    assertEquals( fileZip.getParentFile().getAbsolutePath(), dirZipBaseUrl.getAbsolutePath() );

    // The folder should contain the original zip file, and the 5 files that should have been extracted
    // Check that the folder contains the right number of files
    File[] listOfFiles = dirZipBaseUrl.listFiles();
    assertNotNull( listOfFiles );
    assertEquals( 6, listOfFiles.length );

    // Let's check the real file names
    Set<String> expectedFilesNames = new HashSet<>(
      Arrays.asList( "AddYear.ktr", "CurrentDate.kjb", "DataRowsCustomer.ktr", "GrabData.kjb", ZIP_FILE_NAME,
        "__job_execution_configuration__.xml" ) );

    for ( File children : listOfFiles ) {
      expectedFilesNames.remove( children.getName() );
    }

    assertTrue( "Some files are missing: " + expectedFilesNames.toString(), expectedFilesNames.isEmpty() );
  }

  @Test
  public void extractTo() throws Exception {
    // Copy the test Zip file to a working temporary folder
    FileUtils.copyFileToDirectory( new File( ZIP_FILE_PATH ), temporaryFolder.getRoot() );
    // Check that it is there
    File fileZip = new File( temporaryFolder.getRoot(), ZIP_FILE_NAME );
    assertTrue( fileZip.exists() );

    // For this test, we need a new folder
    File outputFolder = temporaryFolder.newFolder();
    // Make sure it exists
    assertTrue( outputFolder.exists() );

    // Execute (extract to a different folder from where the file is)
    servlet.extract( fileZip.getAbsolutePath(), outputFolder.getAbsolutePath() );

    // Make sure that origin and destination folders are different
    assertNotEquals( fileZip.getParentFile().getAbsolutePath(), outputFolder.getAbsolutePath() );

    // The output folder should only contain the 5 files that should have been extracted
    // Check that the folder contains the right number of files
    File[] listOfFiles = outputFolder.listFiles();
    assertNotNull( listOfFiles );
    assertEquals( 5, listOfFiles.length );

    // Let's check the real file names
    Set<String> expectedFilesNames = new HashSet<>(
      Arrays.asList( "AddYear.ktr", "CurrentDate.kjb", "DataRowsCustomer.ktr", "GrabData.kjb",
        "__job_execution_configuration__.xml" ) );

    for ( File children : listOfFiles ) {
      expectedFilesNames.remove( children.getName() );
    }

    assertTrue( "Some files are missing: " + expectedFilesNames.toString(), expectedFilesNames.isEmpty() );
  }

  @Test
  public void deleteArchive() throws Exception {
    // We'll create a file to be deleted...
    File fileToDelete = temporaryFolder.newFile();
    // Check that it really exists before deleting it
    assertTrue( fileToDelete.exists() );

    // Execute (delete the file)
    servlet.deleteArchive( fileToDelete.getAbsolutePath() );

    // Now, it should not exist
    assertFalse( fileToDelete.exists() );
  }
}
