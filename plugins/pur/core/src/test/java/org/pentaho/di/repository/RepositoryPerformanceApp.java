/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository;

import org.junit.Ignore;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.di.repository.pur.PurRepositoryLocation;
import org.pentaho.di.repository.pur.PurRepositoryMeta;
import org.pentaho.di.trans.TransMeta;

/**
 * This test class is not meant to be run automated. It provides two simple purposes: 1. to bulk load a PUR repository
 * 2. to give simple stats on the performance of the load.
 * 
 * Simple pass a repository URL either on the command line as an argument, or set it in the code, and run the main
 * method. The repository should be empty on initial execution, and you cannot run this utility against the same repo
 * twice without first emptying the repo, as the execution will throw duplicate entry exceptions.
 * 
 * The class can be improved by passing a load parameter as well.
 * 
 * @author GMoran
 * 
 */

@Ignore
public class RepositoryPerformanceApp extends RepositoryTestBase implements java.io.Serializable {
  static final long serialVersionUID = -5389269822527972858L; /* EESOURCE: UPDATE SERIALVERUID */

  public RepositoryPerformanceApp( String url ) {
    super( true );
    setRepositoryLocation( url );
  }

  private static int lightLoadMax = 5;
  private static int moderateLoadMax = 50;
  private static int heavyLoadMax = 500;
  private static int contentLoadMax = 5;

  private static String testFolder = "test_directory";

  private String repositoryLocation = null;

  public static void main( String[] args ) {

    String url = "http://localhost:9080/pentaho-di";
    if ( args.length > 0 ) {
      url = args[0];
    }
    RepositoryPerformanceApp test = new RepositoryPerformanceApp( url );
    try {
      test.setUp();
      test.startupRepository();
      test.testLightLoad();
    } catch ( Exception e ) {
      e.printStackTrace();
    }

  }

  protected void startupRepository() throws Exception {
    // PentahoLicenseVerifier.setStreamOpener(new TestLicenseStream("pdi-ee=true")); //$NON-NLS-1$
    KettleEnvironment.init();

    repositoryMeta = new PurRepositoryMeta();
    repositoryMeta.setName( "JackRabbit" );
    repositoryMeta.setDescription( "JackRabbit test repository" );
    ( (PurRepositoryMeta) repositoryMeta ).setRepositoryLocation( new PurRepositoryLocation( repositoryLocation ) );
    userInfo = new UserInfo( EXP_LOGIN, "password", EXP_USERNAME, "Apache Tomcat user", true );
    repository = new PurRepository();

    repository.init( repositoryMeta );
    repository.connect( EXP_LOGIN, "password" );
  }

  public void testLightLoad() throws Exception {
    testLoad( lightLoadMax );
  }

  public void testModerateLoad() throws Exception {
    testLoad( moderateLoadMax );
  }

  public void testHeavyLoad() throws Exception {
    testLoad( heavyLoadMax );
  }

  private Long testLoad( int max ) throws Exception {

    Long timeInMillis = System.currentTimeMillis();
    RepositoryDirectoryInterface rootDir = loadStartDirectory();

    for ( int i = 0; i < max; i++ ) {

      RepositoryDirectoryInterface childDir =
          repository.createRepositoryDirectory( rootDir, testFolder.concat( String.valueOf( i ) ) );
      createContent( contentLoadMax, childDir );
      createDirectories( contentLoadMax, childDir );
    }
    Long endTimeInMillis = System.currentTimeMillis();
    Long exec = endTimeInMillis - timeInMillis;

    System.out.println( "Execution time in seconds: ".concat( String.valueOf( exec * 0.001 ) ).concat( "s" ) );
    System.out.println( "Created ".concat( String.valueOf( max * ( ( contentLoadMax * 2 ) + 1 ) ) ).concat(
        " primary PDI objects. " ) );

    return exec;
  }

  private void createContent( int loadMax, RepositoryDirectoryInterface createHere ) throws Exception {
    for ( int ix = 0; ix < loadMax; ix++ ) {
      TransMeta transMeta =
          createTransMeta( createHere.getName().concat( EXP_DBMETA_NAME.concat( String.valueOf( ix ) ) ) );
      transMeta.setRepositoryDirectory( createHere );
      try {
        repository.save( transMeta, VERSION_COMMENT_V1.concat( String.valueOf( ix ) ), null );
      } catch ( Exception e ) {
        // nothing to do
      }

      JobMeta jobMeta =
          createJobMeta( "JOB_".concat( createHere.getName() ).concat( EXP_DBMETA_NAME.concat( String.valueOf( ix ) ) ) );
      jobMeta.setRepositoryDirectory( createHere );
      try {
        repository.save( jobMeta, VERSION_COMMENT_V1.concat( String.valueOf( ix ) ), null );
      } catch ( Exception e ) {
        // nothing to do
      }
    }
  }

  private void createDirectories( int loadMax, RepositoryDirectoryInterface createHere ) throws Exception {
    for ( int ix = 0; ix < loadMax; ix++ ) {
      repository.createRepositoryDirectory( createHere, testFolder.concat( String.valueOf( ix ) ) );
    }
  }

  public void setRepositoryLocation( String repositoryLocation ) {
    this.repositoryLocation = repositoryLocation;
  }

  @Override
  protected RepositoryDirectoryInterface loadStartDirectory() throws Exception {
    RepositoryDirectoryInterface dir = super.loadStartDirectory();
    return dir.findDirectory( "home/joe" );
  }

  @Override
  protected void delete( ObjectId id ) {
    // nothing to do
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

}
