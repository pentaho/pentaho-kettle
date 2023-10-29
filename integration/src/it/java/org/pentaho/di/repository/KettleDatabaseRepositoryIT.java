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

package org.pentaho.di.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryCreationHelper;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;

public class KettleDatabaseRepositoryIT extends TestCase {

  protected KettleDatabaseRepositoryMeta repositoryMeta;
  protected KettleDatabaseRepository repository;
  protected RepositoryDirectoryInterface tree;

  public void testDatabaseRepository() throws Exception {

    KettleEnvironment.init();
    String filename = File.createTempFile( "kdrtest", "" ).getAbsolutePath();
    System.out.println( "Using file '" + filename + "' as a H2 database repository" );

    try {
      DatabaseMeta databaseMeta = new DatabaseMeta( "H2Repo", "H2", "JDBC", null, filename, null, null, null );
      repositoryMeta =
        new KettleDatabaseRepositoryMeta( "KettleDatabaseRepository", "H2Repo", "H2 Repository", databaseMeta );
      repository = new KettleDatabaseRepository();
      repository.init( repositoryMeta );
      repository.connectionDelegate.connect( true, true );
      KettleDatabaseRepositoryCreationHelper helper = new KettleDatabaseRepositoryCreationHelper( repository );
      helper.createRepositorySchema( null, false, new ArrayList<String>(), false );
      repository.disconnect();

      // Test connecting...
      //
      repository.connect( "admin", "admin" );
      assertTrue( repository.isConnected() );
      
      // Test database save
      DatabaseMeta dataBaseForSave = new DatabaseMeta("H2Test", "H2", "JDBC", null, filename, null, null, null );   
      repository.save( dataBaseForSave, "User creates new database" );
      //load new database from repository by generated id on save step
      DatabaseMeta loadedDataBase = repository.loadDatabaseMeta( dataBaseForSave.getObjectId(), "User creates new database" );
      
      assertEquals("Database object before save and after load form database is diffenert", dataBaseForSave, loadedDataBase );
      
      // Test loading the directory tree
      tree = repository.loadRepositoryDirectoryTree();
      assertNotNull( tree );

      // Test directory creation
      //
      RepositoryDirectoryInterface fooDirectory = repository.createRepositoryDirectory( tree, "foo" );
      RepositoryDirectoryInterface barDirectory = repository.createRepositoryDirectory( fooDirectory, "bar" );
      RepositoryDirectoryInterface samplesDirectory =
        repository.createRepositoryDirectory( fooDirectory, "samples" );

      // Test directory path lookup
      RepositoryDirectoryInterface checkBar = tree.findDirectory( "/foo/bar" );
      assertNotNull( checkBar );
      assertTrue( checkBar.equals( barDirectory ) );

      // Save all the transformations samples.
      //
      verifyTransformationSamples( samplesDirectory );
      verifyJobSamples( samplesDirectory );

      // Verify metastore functionality
      //
      IMetaStore metaStore = repository.getRepositoryMetaStore();
      KettleMetaStoreTestBase testBase = new KettleMetaStoreTestBase();
      testBase.testFunctionality( metaStore );

      // Finally test disconnecting
      repository.disconnect();
      assertFalse( repository.isConnected() );

    } catch ( Exception e ) {
      e.printStackTrace();
      throw new KettleException( "Error during database repository unit testing", e );
    } finally {
      // Remove the H2 database file
      //
      assertTrue( new File( filename + ".h2.db" ).delete() );
      assertTrue( new File( filename + ".trace.db" ).delete() );
    }
  }

  protected void verifyTransformationSamples( RepositoryDirectoryInterface samplesDirectory ) throws Exception {
    File transSamplesFolder = new File( "samples/transformations/" );
    String[] files = transSamplesFolder.list( new FilenameFilter() {
      public boolean accept( File dir, String name ) {
        return name.endsWith( ".ktr" ) && !name.contains( "HL7" );
      }
    } );
    Arrays.sort( files );

    for ( String file : files ) {
      String transFilename = transSamplesFolder.getAbsolutePath() + "/" + file;
      System.out.println( "Storing/Loading/validating transformation '" + transFilename + "'" );

      // Load the TransMeta object...
      //
      TransMeta transMeta = new TransMeta( transFilename );
      if ( Utils.isEmpty( transMeta.getName() ) ) {
        transMeta.setName( Const.createName( file ) );
      }

      // Save it in the repository in the samples folder
      //
      transMeta.setRepositoryDirectory( samplesDirectory );
      repository.save( transMeta, "unit testing", null, true );
      assertNotNull( transMeta.getObjectId() );

      // Load it back up again...
      //
      TransMeta repTransMeta = repository.loadTransformation( transMeta.getObjectId(), null );
      String oneXml = repTransMeta.getXML();

      // Save & load it again
      //
      repository.save( transMeta, "unit testing", null, true );
      repTransMeta = repository.loadTransformation( transMeta.getObjectId(), null );
      String twoXml = repTransMeta.getXML();

      // The XML needs to be identical after loading
      //
      // storeFile(oneXml, "/tmp/one.ktr");
      // storeFile(twoXml, "/tmp/two.ktr");

      assertEquals( oneXml, twoXml );
    }

    // Verify the number of stored files, see if we can find them all again.
    //
    System.out.println( "Stored "
      + files.length + " transformation samples in folder " + samplesDirectory.getPath() );
    String[] transformationNames = repository.getTransformationNames( samplesDirectory.getObjectId(), false );
    assertEquals( files.length, transformationNames.length );
  }

  protected void verifyJobSamples( RepositoryDirectoryInterface samplesDirectory ) throws Exception {
    FileObject jobSamplesFolder = KettleVFS.getFileObject( "samples/jobs/" );
    FileObject[] files = jobSamplesFolder.findFiles( new FileSelector() {

      @Override
      public boolean traverseDescendents( FileSelectInfo arg0 ) throws Exception {
        return true;
      }

      @Override
      public boolean includeFile( FileSelectInfo info ) throws Exception {
        return info.getFile().getName().getExtension().equalsIgnoreCase( "kjb" );
      }
    } );

    List<FileObject> filesList = Arrays.asList( files );
    Collections.sort( filesList, new Comparator<FileObject>() {
      @Override
      public int compare( FileObject o1, FileObject o2 ) {
        return o1.getName().getPath().compareTo( o2.getName().getPath() );
      }
    } );

    // test the storage of jobMeta attributes in the Kettle DB Repo
    if ( filesList.size() > 0 ) {
      FileObject file = filesList.get( 0 );
      String jobFilename = file.getName().getPath();
      System.out.println( "Storing/Loading/validating job attributes" );

      // Load the JobMeta object...
      //
      JobMeta jobMeta = new JobMeta( jobFilename, repository );
      // set some attributes
      jobMeta.setAttribute( "group", "key", "value" );
      jobMeta.setAttribute( "test-group", "test-key-1", "test-value" );
      jobMeta.setAttribute( "test-group", "test-key-2", "test-value" );
      jobMeta.setAttribute( "test-group", "test-key-3", "test-value-3" );

      // Save it in the repository in the samples folder
      //
      jobMeta.setRepositoryDirectory( samplesDirectory );
      repository.save( jobMeta, "unit testing" );
      assertNotNull( jobMeta.getObjectId() );

      // Load it back up again...
      //
      JobMeta repJobMeta = repository.loadJob( jobMeta.getObjectId(), null );
      String value = repJobMeta.getAttribute( "group", "key" );
      String value1 = repJobMeta.getAttribute( "test-group", "test-key-1" );
      String value2 = repJobMeta.getAttribute( "test-group", "test-key-2" );
      String value3 = repJobMeta.getAttribute( "test-group", "test-key-3" );
      assertEquals( "value", value );
      assertEquals( "test-value", value1 );
      assertEquals( "test-value", value2 );
      assertEquals( "test-value-3", value3 );
    }
    
    for ( FileObject file : filesList ) {
      String jobFilename = file.getName().getPath();
      System.out.println( "Storing/Loading/validating job '" + jobFilename + "'" );

      // Load the JobMeta object...
      //
      JobMeta jobMeta = new JobMeta( jobFilename, repository );
      if ( Utils.isEmpty( jobMeta.getName() ) ) {
        jobMeta.setName( Const.createName( file.getName().getBaseName() ) );
      }

      // Save it in the repository in the samples folder
      //
      jobMeta.setRepositoryDirectory( samplesDirectory );
      repository.save( jobMeta, "unit testing" );
      assertNotNull( jobMeta.getObjectId() );

      // Load it back up again...
      //
      JobMeta repJobMeta = repository.loadJob( jobMeta.getObjectId(), null );
      String oneXml = repJobMeta.getXML();

      // Save & load it again
      //
      repository.save( jobMeta, "unit testing" );
      repJobMeta = repository.loadJob( jobMeta.getObjectId(), null );
      String twoXml = repJobMeta.getXML();

      // The XML needs to be identical after loading
      //
      // storeFile(oneXml, "/tmp/one.ktr");
      // storeFile(twoXml, "/tmp/two.ktr");
      //
      assertEquals( oneXml, twoXml );
    }

    // Verify the number of stored files, see if we can find them all again.
    //
    System.out.println( "Stored " + files.length + " job samples in folder " + samplesDirectory.getPath() );
    String[] jobNames = repository.getJobNames( samplesDirectory.getObjectId(), false );
    assertEquals( files.length, jobNames.length );
  }

  protected void storeFile( String xml, String filename ) throws Exception {
    File file = new File( filename );
    FileOutputStream fos = new FileOutputStream( file );

    fos.write( XMLHandler.getXMLHeader( Const.XML_ENCODING ).getBytes( Const.XML_ENCODING ) );
    fos.write( xml.getBytes( Const.XML_ENCODING ) );
    fos.close();

  }

}
