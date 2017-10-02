/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.job.entries.empty.JobEntryEmpty;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
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

public class JobMetaTest {
  private JobMeta jm;
  private JobEntryEmpty je1;
  private JobEntryEmpty je2;
  private JobEntryEmpty je4;

  @Before
  public void setUp() {
    jm = new JobMeta();

    je1 = new JobEntryEmpty();
    je1.setName( "je1" );
    JobEntryCopy copy1 = new JobEntryCopy( je1 );

    je2 = new JobEntryEmpty();
    je2.setName( "je2" );
    JobEntryCopy copy2 = new JobEntryCopy( je2 );
    JobHopMeta hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

    JobEntryEmpty je3 = new JobEntryEmpty();
    je3.setName( "je3" );
    copy2 = new JobEntryCopy( je3 );
    hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

    je4 = new JobEntryEmpty();
    je4.setName( "je4" );
    copy1 = new JobEntryCopy( je3 );
    copy2 = new JobEntryCopy( je4 );
    hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

  }

  @Test
  public void testPathExist() throws KettleXMLException, IOException, URISyntaxException {
    Assert.assertTrue( jm.isPathExist( je1, je4 ) );
  }

  @Test
  public void testPathNotExist() throws KettleXMLException, IOException, URISyntaxException {
    Assert.assertFalse( jm.isPathExist( je2, je4 ) );
  }

  @Test
  public void testContentChangeListener() throws Exception {
    ContentChangedListener listener = Mockito.mock( ContentChangedListener.class );
    jm.addContentChangedListener( listener );

    jm.setChanged();
    jm.setChanged( true );

    Mockito.verify( listener, Mockito.times( 2 ) ).contentChanged( Mockito.same( jm ) );

    jm.clearChanged();
    jm.setChanged( false );

    Mockito.verify( listener, Mockito.times( 2 ) ).contentSafe( Mockito.same( jm ) );

    jm.removeContentChangedListener( listener );
    jm.setChanged();
    jm.setChanged( true );

    Mockito.verifyNoMoreInteractions( listener );
  }

  @Test
  public void testLookupRepositoryReferences() throws Exception {
    JobMeta jobMetaMock = Mockito.mock( JobMeta.class );
    Mockito.doCallRealMethod().when( jobMetaMock ).lookupRepositoryReferences( Mockito.any( Repository.class ) );
    Mockito.doCallRealMethod().when( jobMetaMock ).addJobEntry( Mockito.anyInt(), Mockito.any( JobEntryCopy.class ) );
    Mockito.doCallRealMethod().when( jobMetaMock ).clear();

    jobMetaMock.clear();

    JobEntryTrans jobEntryMock = Mockito.mock( JobEntryTrans.class );
    Mockito.when( jobEntryMock.hasRepositoryReferences() ).thenReturn( true );

    JobEntryTrans brokenJobEntryMock = Mockito.mock( JobEntryTrans.class );
    Mockito.when( brokenJobEntryMock.hasRepositoryReferences() ).thenReturn( true );
    Mockito.doThrow( Mockito.mock( IdNotFoundException.class ) ).when( brokenJobEntryMock ).lookupRepositoryReferences( Mockito.any(
        Repository.class ) );

    JobEntryCopy jobEntryCopy1 = Mockito.mock( JobEntryCopy.class );
    Mockito.when( jobEntryCopy1.getEntry() ).thenReturn( jobEntryMock );
    jobMetaMock.addJobEntry( 0, jobEntryCopy1 );

    JobEntryCopy jobEntryCopy2 = Mockito.mock( JobEntryCopy.class );
    Mockito.when( jobEntryCopy2.getEntry() ).thenReturn( brokenJobEntryMock );
    jobMetaMock.addJobEntry( 1, jobEntryCopy2 );

    JobEntryCopy jobEntryCopy3 = Mockito.mock( JobEntryCopy.class );
    Mockito.when( jobEntryCopy3.getEntry() ).thenReturn( jobEntryMock );
    jobMetaMock.addJobEntry( 2, jobEntryCopy3 );

    Repository repo = Mockito.mock( Repository.class );
    try {
      jobMetaMock.lookupRepositoryReferences( repo );
      Assert.fail( "no exception for broken entry" );
    } catch ( LookupReferencesException e ) {
      // ok
    }

    Mockito.verify( jobEntryMock, Mockito.times( 2 ) ).lookupRepositoryReferences( Mockito.any( Repository.class ) );
  }

  /**
   * Given job meta object.
   * <br/>
   * When the job is called to export resources,
   * then the existing current directory should be used as a context to locate resources.
   */
  @Test
  public void shouldUseExistingRepositoryDirectoryWhenExporting() throws KettleException {
    // prepare
    final JobMeta clone = Mockito.spy( new JobMeta() );
    RepositoryDirectoryInterface directory = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.when( directory.getPath() ).thenReturn( "directoryPath" );

    JobMeta jobMeta = new JobMeta(  ) {
      @Override
      public Object realClone( boolean doClear ) {
        return clone;
      }
    };
    jobMeta.setRepositoryDirectory( directory );
    jobMeta.setName( "jobName" );

    // run
    jobMeta.exportResources( null, new HashMap<String, ResourceDefinition>( 4 ), Mockito.mock( ResourceNamingInterface.class ),
      null, null );

    // assert
    Mockito.verify( clone ).setRepositoryDirectory( directory );
  }

  @Test
  public void shouldUseCoordinatesOfItsStepsAndNotesWhenCalculatingMinimumPoint() {
    JobMeta jobMeta = new JobMeta();
    Point jobEntryPoint = new Point( 500, 500 );
    Point notePadMetaPoint = new Point( 400, 400 );
    JobEntryCopy jobEntryCopy = Mockito.mock( JobEntryCopy.class );
    Mockito.when( jobEntryCopy.getLocation() ).thenReturn( jobEntryPoint );
    NotePadMeta notePadMeta = Mockito.mock( NotePadMeta.class );
    Mockito.when( notePadMeta.getLocation() ).thenReturn( notePadMetaPoint );

    // empty Job return 0 coordinate point
    Point point = jobMeta.getMinimum();
    Assert.assertEquals( 0, point.x );
    Assert.assertEquals( 0, point.y );

    // when Job contains a single step or note, then jobMeta should return coordinates of it, subtracting borders
    jobMeta.addJobEntry( 0, jobEntryCopy );
    Point actualStepPoint = jobMeta.getMinimum();
    Assert.assertEquals( jobEntryPoint.x - JobMeta.BORDER_INDENT, actualStepPoint.x );
    Assert.assertEquals( jobEntryPoint.y - JobMeta.BORDER_INDENT, actualStepPoint.y );

    // when Job contains step or notes, then jobMeta should return minimal coordinates of them, subtracting borders
    jobMeta.addNote( notePadMeta );
    Point stepPoint = jobMeta.getMinimum();
    Assert.assertEquals( notePadMetaPoint.x - JobMeta.BORDER_INDENT, stepPoint.x );
    Assert.assertEquals( notePadMetaPoint.y - JobMeta.BORDER_INDENT, stepPoint.y );
  }

  @Test
  public void testLoadXml() throws KettleException {
    final String directory = "/home/admin";
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


}
