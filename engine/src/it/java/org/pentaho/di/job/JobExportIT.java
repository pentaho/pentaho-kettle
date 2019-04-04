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

package org.pentaho.di.job;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertTrue;

/**
 * Integration test for job files export.
 */
public class JobExportIT {

  private static final String FILE_REPOSITORY_ID = "KettleFileRepository";
  private static final String FILE_REPOSITORY_NAME = "FileRep";
  private static final String FILE_REPOSITORY_DESC = "File repository";

  private static final String EXPORT_FILE_NAME = "sample_job_export.zip";
  private static final String EXPORT_DIR = "target/export/org/pentaho/di/job/out/";
  private static final String EXPORT_FILE = EXPORT_DIR + EXPORT_FILE_NAME;

  private static final String REPOSITORY_ROOT_DIR = "src/it/resources/org/pentaho/di/job/repo";
  private static final String REPOSITORY_DIR = "/folder";
  private static final String JOB_NAME = "sample_job";
  private static final String EXTRACT_DIR = EXPORT_DIR + File.separator + JOB_NAME;
  private static final String EXTRACTED_JOB_FILE = EXTRACT_DIR + File.separator + JOB_NAME + ".kjb";
  private static final String EXTRACTED_TRANS_FILE = EXTRACT_DIR + File.separator + "sample_trans.ktr";

  private KettleFileRepository repository;

  @BeforeClass
  public static void setUpOnce() throws KettleException {
    deleteFolder( new File( EXPORT_DIR ) );
    new File( EXPORT_DIR ).mkdirs();

    KettleEnvironment.init();
  }

  @AfterClass
  public static void tearDownOnce() {
    deleteFolder( new File( EXPORT_DIR ) );
  }

  @Before
  public void setUp() throws KettleException {
    KettleFileRepositoryMeta repositoryMeta = new KettleFileRepositoryMeta( FILE_REPOSITORY_ID, FILE_REPOSITORY_NAME,
      FILE_REPOSITORY_DESC, REPOSITORY_ROOT_DIR );
    repository = new KettleFileRepository();
    repository.init( repositoryMeta );
    repository.connect( null, null );
  }

  @After
  public void tearDown() {
    repository.disconnect();
  }

  /**
   * Given a Job located in non-root directory of a repository,
   * and referencing to a Transformation using 'Internal.Entry.Current.Directory' variable.
   * <br/>
   * When this Job is exported into a zip file,
   * then the referenced Transformation should be exported as well.
   */
  @Test
  public void shouldExportJobAndRelatedTransformationFile() throws IOException, MetaStoreException, KettleException {
    RepositoryDirectoryInterface repositoryDir = repository.loadRepositoryDirectoryTree();
    repositoryDir = repositoryDir.findDirectory( REPOSITORY_DIR );

    JobMeta jobMeta = repository.loadJob( JOB_NAME, repositoryDir, null, null );
    Job job = new Job( repository, jobMeta );

    ResourceUtil.serializeResourceExportInterface( EXPORT_FILE, job.getJobMeta(), job, repository, null );

    File zipFile = new File( EXPORT_FILE );
    assertTrue( zipFile.exists() );

    extractZip( zipFile, EXTRACT_DIR );

    // assert that either of files, job and transformation, have been exported.
    assertTrue( new File( EXTRACTED_JOB_FILE ).exists() );
    assertTrue( new File( EXTRACTED_TRANS_FILE ).exists() );
  }

  private static void deleteFolder( File folder ) {
    File[] files = folder.listFiles();
    if ( files != null ) { //some JVMs return null for empty dirs
      for ( File f : files ) {
        if ( f.isDirectory() ) {
          deleteFolder( f );
        } else {
          f.delete();
        }
      }
    }
    folder.delete();
  }

  private static void extractZip( File zipFile, String extractDir ) throws IOException {
    int BUFFER = 2048;
    ZipFile zip = new ZipFile( zipFile );

    new File( extractDir ).mkdir();
    Enumeration zipFileEntries = zip.entries();

    // Process each entry
    while ( zipFileEntries.hasMoreElements() ) {
      // grab a zip file entry
      ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
      String currentEntry = entry.getName();

      File destFile = new File( extractDir, currentEntry );
      File destinationParent = destFile.getParentFile();

      // create the parent directory structure if needed
      destinationParent.mkdirs();

      if ( !entry.isDirectory() ) {
        BufferedInputStream is = new BufferedInputStream( zip
          .getInputStream( entry ) );
        int currentByte;
        // establish buffer for writing file
        byte[] data = new byte[ BUFFER ];

        // write the current file to disk
        FileOutputStream fos = new FileOutputStream( destFile );
        BufferedOutputStream dest = new BufferedOutputStream( fos,
          BUFFER );

        // read and write until last byte is encountered
        while ( ( currentByte = is.read( data, 0, BUFFER ) ) != -1 ) {
          dest.write( data, 0, currentByte );
        }
        dest.flush();
        dest.close();
        is.close();
      }
    }
  }
}
