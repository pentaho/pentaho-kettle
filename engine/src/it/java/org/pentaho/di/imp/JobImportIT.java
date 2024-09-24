/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.imp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryImportFeedbackInterface;
import org.pentaho.di.repository.RepositoryImporter;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test for job files export.
 */
@RunWith( MockitoJUnitRunner.class )
public class JobImportIT {

  private static final String FILE_REPOSITORY_ID = "KettleFileRepository";
  private static final String FILE_REPOSITORY_NAME = "FileRep";
  private static final String FILE_REPOSITORY_DESC = "File repository";

  private static final String EXPORT_FILE_NAME = "job_export_file";
  private static final String EXPORT_DIR = "src/it/resources/";
  private static final String EXPORT_FILE = EXPORT_DIR + EXPORT_FILE_NAME;

  private static final String REPOSITORY_ROOT_DIR = "target/test_repo";

  @Mock
  private RepositoryImportFeedbackInterface feedbackInterface;

  private KettleFileRepository repository;
  private RepositoryImporter importer;


  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
    Props.init( Props.TYPE_PROPERTIES_SPOON );

    deleteFolder( new File( REPOSITORY_ROOT_DIR ) );

    KettleFileRepositoryMeta repositoryMeta = new KettleFileRepositoryMeta( FILE_REPOSITORY_ID, FILE_REPOSITORY_NAME,
      FILE_REPOSITORY_DESC, REPOSITORY_ROOT_DIR );
    repository = new KettleFileRepository();
    repository.init( repositoryMeta );
    repository.connect( null, null );
    importer = new RepositoryImporter( repository );

  }

  @After
  public void tearDown() {
    repository.disconnect();
  }

  /**
   * Import a file containing three jobs referencing each other.  Verify that the jobs import correctly, in particular
   * that the ${Internal.Entry.Current.Directory} variable is correctly set in Job1 and Job2
   */
  @Test
  public void currentDirectoryVariableSetCorrectly() throws KettleException {
    RepositoryDirectoryInterface repositoryDir = repository.loadRepositoryDirectoryTree();
    repositoryDir = repositoryDir.findDirectory( "/" );

    importer.importAll( feedbackInterface, "", new String[] { EXPORT_FILE }, repositoryDir, true, true, "" );

    JobMeta job1 = repository.loadJob( "/public/SupportPostgres/Job1", repositoryDir, null, null );

    assertNotNull( job1 );
    assertEquals( 3, job1.getJobCopies().size() );
    JobEntryCopy jobEntryCopy1 = job1.getJobCopies().stream().filter( j -> j.getName().equals( "Job" ) ).findFirst().get();
    assertNotNull( jobEntryCopy1 );
    JobEntryJob jobEntryJob1 = (JobEntryJob) jobEntryCopy1.getEntry();
    assertEquals( "${Internal.Entry.Current.Directory}/Subjob", jobEntryJob1.getDirectory() );
    assertEquals( "Job2", jobEntryJob1.getJobName() );

    //repositoryDir = repositoryDir.findDirectory( "/public/SupportPostgres/Subjob" );
    JobMeta job2 = repository.loadJob( "/public/SupportPostgres/Subjob/Job2", repositoryDir, null, null );

    assertNotNull( job2 );
    assertEquals( 3, job2.getJobCopies().size() );
    JobEntryCopy jobEntryCopy2 = job2.getJobCopies().stream().filter( j -> j.getName().equals( "Job" ) ).findFirst().get();
    assertNotNull( jobEntryCopy2 );
    JobEntryJob jobEntryJob2 = (JobEntryJob) jobEntryCopy2.getEntry();
    assertEquals( "${Internal.Entry.Current.Directory}", jobEntryJob2.getDirectory() );
    assertEquals( "Job3", jobEntryJob2.getJobName() );
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
}
