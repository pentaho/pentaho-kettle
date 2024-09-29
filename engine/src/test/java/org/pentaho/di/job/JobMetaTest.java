/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.exception.IdNotFoundException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.exception.LookupReferencesException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.core.listeners.CurrentDirectoryChangedListener;
import org.pentaho.di.job.entries.empty.JobEntryEmpty;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class JobMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String JOB_META_NAME = "jobName";

  private JobMeta jobMeta;
  private RepositoryDirectoryInterface directoryJob;
  private ContentChangedListener listener;
  private ObjectRevision objectRevision;

  @Before
  public void setUp() {
    jobMeta = new JobMeta();
    // prepare
    directoryJob = mock( RepositoryDirectoryInterface.class );
    when( directoryJob.getPath() ).thenReturn( "directoryPath" );
    listener = mock( ContentChangedListener.class );
    objectRevision = mock( ObjectRevision.class );
    when( objectRevision.getName() ).thenReturn( "revisionName" );
    jobMeta.addContentChangedListener( listener );
    jobMeta.setRepositoryDirectory( directoryJob );
    jobMeta.setName( JOB_META_NAME );
    jobMeta.setObjectRevision( objectRevision );
  }

  /**
   * PDI-18655 - Variables.initializeVariablesFrom susceptible to NullPointerException
   *
   * @throws KettleException
   */
  @Test
  public void testJobMetaInitialization() throws KettleException {
    System.getProperties().put( "custom_property_boolean", true );
    System.getProperties().put( "custom_property_string", "string" );
    JobMeta jobMeta = new JobMeta();
    assertNotNull( jobMeta );
  }

  @Test
  public void testPathExist() throws KettleXMLException, IOException, URISyntaxException {
    assertTrue( testPath( "je1-je4" ) );
  }

  @Test
  public void testPathNotExist() throws KettleXMLException, IOException, URISyntaxException {
    assertFalse( testPath( "je2-je4" ) );
  }

  private boolean testPath( String branch ) {
    JobEntryEmpty je1 = new JobEntryEmpty();
    je1.setName( "je1" );

    JobEntryEmpty je2 = new JobEntryEmpty();
    je2.setName( "je2" );

    JobHopMeta hop = new JobHopMeta( new JobEntryCopy( je1 ), new JobEntryCopy( je2 ) );
    jobMeta.addJobHop( hop );

    JobEntryEmpty je3 = new JobEntryEmpty();
    je3.setName( "je3" );
    hop = new JobHopMeta( new JobEntryCopy( je1 ), new JobEntryCopy( je3 ) );
    jobMeta.addJobHop( hop );

    JobEntryEmpty je4 = new JobEntryEmpty();
    je4.setName( "je4" );
    hop = new JobHopMeta( new JobEntryCopy( je3 ), new JobEntryCopy( je4 ) );
    jobMeta.addJobHop( hop );

    if ( branch.equals( "je1-je4" ) ) {
      return jobMeta.isPathExist( je1, je4 );
    } else if ( branch.equals( "je2-je4" ) ) {
      return jobMeta.isPathExist( je2, je4 );
    } else {
      return false;
    }
  }

  @Test
  public void testContentChangeListener() throws Exception {
    jobMeta.setChanged();
    jobMeta.setChanged( true );

    verify( listener, times( 2 ) ).contentChanged( same( jobMeta ) );

    jobMeta.clearChanged();
    jobMeta.setChanged( false );

    verify( listener, times( 2 ) ).contentSafe( same( jobMeta ) );

    jobMeta.removeContentChangedListener( listener );
    jobMeta.setChanged();
    jobMeta.setChanged( true );

    verifyNoMoreInteractions( listener );
  }

  @Test
  public void testLookupRepositoryReferences() throws Exception {
    jobMeta.clear();

    JobEntryTrans jobEntryMock = mock( JobEntryTrans.class );
    when( jobEntryMock.hasRepositoryReferences() ).thenReturn( true );

    JobEntryTrans brokenJobEntryMock = mock( JobEntryTrans.class );
    when( brokenJobEntryMock.hasRepositoryReferences() ).thenReturn( true );
    doThrow( mock( IdNotFoundException.class ) ).when( brokenJobEntryMock ).lookupRepositoryReferences( any(
        Repository.class ) );

    JobEntryCopy jobEntryCopy1 = mock( JobEntryCopy.class );
    when( jobEntryCopy1.getEntry() ).thenReturn( jobEntryMock );
    jobMeta.addJobEntry( 0, jobEntryCopy1 );

    JobEntryCopy jobEntryCopy2 = mock( JobEntryCopy.class );
    when( jobEntryCopy2.getEntry() ).thenReturn( brokenJobEntryMock );
    jobMeta.addJobEntry( 1, jobEntryCopy2 );

    JobEntryCopy jobEntryCopy3 = mock( JobEntryCopy.class );
    when( jobEntryCopy3.getEntry() ).thenReturn( jobEntryMock );
    jobMeta.addJobEntry( 2, jobEntryCopy3 );

    try {
      jobMeta.lookupRepositoryReferences( mock( Repository.class ) );
      fail( "no exception for broken entry" );
    } catch ( LookupReferencesException e ) {
      // ok
    }
    verify( jobEntryMock, times( 2 ) ).lookupRepositoryReferences( any( Repository.class ) );
  }

  /**
   * Given job meta object. <br/>
   * When the job is called to export resources, then the existing current directory should be used as a context to
   * locate resources.
   */
  @Test
  public void shouldUseExistingRepositoryDirectoryWhenExporting() throws KettleException {
    final JobMeta jobMetaSpy = spy( jobMeta );
    JobMeta jobMeta = new JobMeta() {
      @Override
      public Object realClone( boolean doClear ) {
        return jobMetaSpy;
      }
    };
    jobMeta.setRepositoryDirectory( directoryJob );
    jobMeta.setName( JOB_META_NAME );
    jobMeta.exportResources( null, new HashMap<String, ResourceDefinition>( 4 ), mock( ResourceNamingInterface.class ),
        null, null );

    // assert
    verify( jobMetaSpy ).setRepositoryDirectory( directoryJob );
  }

  @Test
  public void shouldUseCoordinatesOfItsStepsAndNotesWhenCalculatingMinimumPoint() {
    Point jobEntryPoint = new Point( 500, 500 );
    Point notePadMetaPoint = new Point( 400, 400 );
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );
    when( jobEntryCopy.getLocation() ).thenReturn( jobEntryPoint );
    NotePadMeta notePadMeta = mock( NotePadMeta.class );
    when( notePadMeta.getLocation() ).thenReturn( notePadMetaPoint );

    // empty Job return 0 coordinate point
    Point point = jobMeta.getMinimum();
    assertEquals( 0, point.x );
    assertEquals( 0, point.y );

    // when Job contains a single step or note, then jobMeta should return coordinates of it, subtracting borders
    jobMeta.addJobEntry( 0, jobEntryCopy );
    Point actualStepPoint = jobMeta.getMinimum();
    assertEquals( jobEntryPoint.x - JobMeta.BORDER_INDENT, actualStepPoint.x );
    assertEquals( jobEntryPoint.y - JobMeta.BORDER_INDENT, actualStepPoint.y );

    // when Job contains step or notes, then jobMeta should return minimal coordinates of them, subtracting borders
    jobMeta.addNote( notePadMeta );
    Point stepPoint = jobMeta.getMinimum();
    assertEquals( notePadMetaPoint.x - JobMeta.BORDER_INDENT, stepPoint.x );
    assertEquals( notePadMetaPoint.y - JobMeta.BORDER_INDENT, stepPoint.y );
  }

  @Test
  public void testEquals_oneNameNull() {
    assertFalse( testEquals( null, null, null, null ) );
  }

  @Test
  public void testEquals_secondNameNull() {
    jobMeta.setName( null );
    assertFalse( testEquals( JOB_META_NAME, null, null, null ) );
  }

  @Test
  public void testEquals_sameNameOtherDir() {
    RepositoryDirectoryInterface otherDirectory = mock( RepositoryDirectoryInterface.class );
    when( otherDirectory.getPath() ).thenReturn( "otherDirectoryPath" );
    assertFalse( testEquals( JOB_META_NAME, otherDirectory, null, null ) );
  }

  @Test
  public void testEquals_sameNameSameDirNullRev() {
    assertFalse( testEquals( JOB_META_NAME, directoryJob, null, null ) );
  }

  @Test
  public void testEquals_sameNameSameDirDiffRev() {
    ObjectRevision otherRevision = mock( ObjectRevision.class );
    when( otherRevision.getName() ).thenReturn( "otherRevision" );
    assertFalse( testEquals( JOB_META_NAME, directoryJob, otherRevision, null ) );
  }

  @Test
  public void testEquals_sameNameSameDirSameRev() {
    assertTrue( testEquals( JOB_META_NAME, directoryJob, objectRevision, null ) );
  }

  @Test
  public void testEquals_sameNameSameDirSameRevFilename() {
    assertFalse( testEquals( JOB_META_NAME, directoryJob, objectRevision, "Filename" ) );
  }

  @Test
  public void testEquals_sameFilename() {
    String newFilename = "Filename";
    jobMeta.setFilename( newFilename );
    assertFalse( testEquals( null, null, null, newFilename ) );
  }

  @Test
  public void testEquals_difFilenameSameName() {
    jobMeta.setFilename( "Filename" );
    assertFalse( testEquals( JOB_META_NAME, null, null, "OtherFileName" ) );
  }

  @Test
  public void testEquals_sameFilenameSameName() {
    String newFilename = "Filename";
    jobMeta.setFilename( newFilename );
    assertTrue( testEquals( JOB_META_NAME, null, null, newFilename ) );
  }

  @Test
  public void testEquals_sameFilenameDifName() {
    String newFilename = "Filename";
    jobMeta.setFilename( newFilename );
    assertFalse( testEquals( "OtherName", null, null, newFilename ) );
  }

  private boolean testEquals( String name, RepositoryDirectoryInterface repDirectory, ObjectRevision revision,
      String filename ) {
    JobMeta jobMeta2 = new JobMeta();
    jobMeta2.setName( name );
    jobMeta2.setRepositoryDirectory( repDirectory );
    jobMeta2.setObjectRevision( revision );
    jobMeta2.setFilename( filename );
    return jobMeta.equals( jobMeta2 );
  }

  @Test
  public void testLoadXml() throws KettleException {
    String directory = "/home/admin";
    Node jobNode = Mockito.mock( Node.class );
    NodeList nodeList = new NodeList() {
      Node node = Mockito.mock( Node.class );

      {
        Mockito.when( node.getNodeName() ).thenReturn( "directory" );
        Node child = Mockito.mock( Node.class );
        Mockito.when( node.getFirstChild() ).thenReturn( child );
        Mockito.when( child.getNodeValue() ).thenReturn( directory );
      }

      @Override public Node item( int index ) {
        return node;
      }

      @Override public int getLength() {
        return 1;
      }
    };

    Mockito.when( jobNode.getChildNodes() ).thenReturn( nodeList );

    Repository rep = Mockito.mock( Repository.class );
    RepositoryDirectory repDirectory =
      new RepositoryDirectory( new RepositoryDirectory( new RepositoryDirectory(), "home" ), "admin" );
    Mockito.when( rep.findDirectory( Mockito.eq( directory ) ) ).thenReturn( repDirectory );
    JobMeta meta = new JobMeta();

    meta.loadXML( jobNode, null, rep, Mockito.mock( IMetaStore.class ), false,
      Mockito.mock( OverwritePrompter.class ) );
    Job job = new Job( rep, meta );
    job.setInternalKettleVariables( null );

    Assert.assertEquals( repDirectory.getPath(), job.getVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY ) );
  }

  @Test
  public void testAddRemoveJobEntryCopySetUnsetParent() throws Exception {
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );
    jobMeta.addJobEntry( jobEntryCopy );
    jobMeta.removeJobEntry( 0 );
    verify( jobEntryCopy, times( 1 ) ).setParentJobMeta( jobMeta );
    verify( jobEntryCopy, times( 1 ) ).setParentJobMeta( null );
  }

  @Test
  public void testFireCurrentDirChanged() throws Exception {
    String pathBefore = "/path/before", pathAfter = "path/after";
    RepositoryDirectoryInterface repoDirOrig = mock( RepositoryDirectoryInterface.class );
    when( repoDirOrig.getPath() ).thenReturn( pathBefore );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );
    when( repoDir.getPath() ).thenReturn( pathAfter );

    jobMeta.setRepository( mock( Repository.class ) );
    jobMeta.setRepositoryDirectory( repoDirOrig );

    CurrentDirectoryChangedListener listener = mock( CurrentDirectoryChangedListener.class );
    jobMeta.addCurrentDirectoryChangedListener( listener );
    jobMeta.setRepositoryDirectory( repoDir );

    verify( listener, times( 1 ) ).directoryChanged( jobMeta, pathBefore, pathAfter );
  }

  @Test
  public void testHasLoop_simpleLoop() throws Exception {
    //main->2->3->main
    JobMeta jobMetaSpy = spy( jobMeta );
    JobEntryCopy jobEntryCopyMain = createJobEntryCopy( "mainStep" );
    JobEntryCopy jobEntryCopy2 = createJobEntryCopy( "step2" );
    JobEntryCopy jobEntryCopy3 = createJobEntryCopy( "step3" );
    when( jobMetaSpy.findNrPrevJobEntries( jobEntryCopyMain ) ).thenReturn( 1 );
    when( jobMetaSpy.findPrevJobEntry( jobEntryCopyMain, 0 ) ).thenReturn( jobEntryCopy2 );
    when( jobMetaSpy.findNrPrevJobEntries( jobEntryCopy2 ) ).thenReturn( 1 );
    when( jobMetaSpy.findPrevJobEntry( jobEntryCopy2, 0 ) ).thenReturn( jobEntryCopy3 );
    when( jobMetaSpy.findNrPrevJobEntries( jobEntryCopy3 ) ).thenReturn( 1 );
    when( jobMetaSpy.findPrevJobEntry( jobEntryCopy3, 0 ) ).thenReturn( jobEntryCopyMain );
    assertTrue( jobMetaSpy.hasLoop( jobEntryCopyMain ) );
  }

  @Test
  public void testHasLoop_loopInPrevSteps() throws Exception {
    //main->2->3->4->3
    JobMeta jobMetaSpy = spy( jobMeta );
    JobEntryCopy jobEntryCopyMain = createJobEntryCopy( "mainStep" );
    JobEntryCopy jobEntryCopy2 = createJobEntryCopy( "step2" );
    JobEntryCopy jobEntryCopy3 = createJobEntryCopy( "step3" );
    JobEntryCopy jobEntryCopy4 = createJobEntryCopy( "step4" );
    when( jobMetaSpy.findNrPrevJobEntries( jobEntryCopyMain ) ).thenReturn( 1 );
    when( jobMetaSpy.findPrevJobEntry( jobEntryCopyMain, 0 ) ).thenReturn( jobEntryCopy2 );
    when( jobMetaSpy.findNrPrevJobEntries( jobEntryCopy2 ) ).thenReturn( 1 );
    when( jobMetaSpy.findPrevJobEntry( jobEntryCopy2, 0 ) ).thenReturn( jobEntryCopy3 );
    when( jobMetaSpy.findNrPrevJobEntries( jobEntryCopy3 ) ).thenReturn( 1 );
    when( jobMetaSpy.findPrevJobEntry( jobEntryCopy3, 0 ) ).thenReturn( jobEntryCopy4 );
    when( jobMetaSpy.findNrPrevJobEntries( jobEntryCopy4 ) ).thenReturn( 1 );
    when( jobMetaSpy.findPrevJobEntry( jobEntryCopy4, 0 ) ).thenReturn( jobEntryCopy3 );
    //check no StackOverflow error
    assertFalse( jobMetaSpy.hasLoop( jobEntryCopyMain ) );
  }

  private JobEntryCopy createJobEntryCopy( String name ) {
    JobEntryInterface jobEntry = mock( JobEntryInterface.class );
    JobEntryCopy jobEntryCopy = new JobEntryCopy( jobEntry );
    when( jobEntryCopy.getName() ).thenReturn( name );
    jobEntryCopy.setNr( 0 );
    return jobEntryCopy;
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithFilename( ) {
    JobMeta jobMetaTest = new JobMeta(  );
    jobMetaTest.setFilename( "hasFilename" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobMetaTest.setInternalEntryCurrentDirectory();

    assertEquals( "file:///C:/SomeFilenameDirectory", jobMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );

  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithRepository( ) {
    JobMeta jobMetaTest = new JobMeta(  );
    RepositoryDirectoryInterface path = mock( RepositoryDirectoryInterface.class );

    when( path.getPath() ).thenReturn( "aPath" );
    jobMetaTest.setRepository( mock( Repository.class ) );
    jobMetaTest.setRepositoryDirectory( path );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobMetaTest.setInternalEntryCurrentDirectory();

    assertEquals( "/SomeRepDirectory", jobMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithoutFilenameOrRepository( ) {
    JobMeta jobMetaTest = new JobMeta(  );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobMetaTest.setInternalEntryCurrentDirectory();

    assertEquals( "Original value defined at run execution", jobMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY )  );
  }

  @Test
  public void testUpdateCurrentDirWithFilename( ) {
    JobMeta jobMetaTest = new JobMeta(  );
    jobMetaTest.setFilename( "hasFilename" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobMetaTest.updateCurrentDir();

    assertEquals( "file:///C:/SomeFilenameDirectory", jobMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );

  }

  @Test
  public void testUpdateCurrentDirWithRepository( ) {
    JobMeta jobMetaTest = new JobMeta(  );
    RepositoryDirectoryInterface path = mock( RepositoryDirectoryInterface.class );

    when( path.getPath() ).thenReturn( "aPath" );
    jobMetaTest.setRepository( mock( Repository.class ) );
    jobMetaTest.setRepositoryDirectory( path );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobMetaTest.updateCurrentDir();

    assertEquals( "/SomeRepDirectory", jobMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void testUpdateCurrentDirWithoutFilenameOrRepository( ) {
    JobMeta jobMetaTest = new JobMeta(  );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobMetaTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobMetaTest.updateCurrentDir();

    assertEquals( "Original value defined at run execution", jobMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY )  );
  }

  @Test
  public void isGatheringMetricsTest() {
    JobMeta jobMetaTest = new JobMeta();
    jobMetaTest.setGatheringMetrics( true );
    assertTrue( jobMetaTest.isGatheringMetrics() );
    jobMetaTest.setGatheringMetrics( false );
    assertFalse( jobMetaTest.isGatheringMetrics() );
  }

}
