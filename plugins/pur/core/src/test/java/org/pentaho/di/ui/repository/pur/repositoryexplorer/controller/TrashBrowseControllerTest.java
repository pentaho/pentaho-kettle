/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repository.pur.services.ITrashService;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.MainController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.containers.XulDeck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tatsiana_Kasiankova
 * 
 */

@RunWith( org.mockito.junit.MockitoJUnitRunner.class )
public class TrashBrowseControllerTest {

  private TrashBrowseControllerSpy trBrController;
  @Mock UIRepositoryDirectory repoDirMock;
  @Mock ITrashService trashServiceMock;
  @Mock Repository repositoryMock;
  @Mock List<TrashBrowseController.UIDeletedObject> selectedTrashFileItemsMock;
  @Mock TrashBrowseController.UIDeletedObject uiDeleteObjectMock;
  @Mock TrashBrowseController.UIDeletedObject uiDirectoryObjectMock;
  @Mock RepositoryElementMetaInterface objectInDirectory;
  @Mock ObjectId objectIdMock;
  @Mock ObjectId objectIdMock2;
  @Mock TransMeta transMetaMock;
  @Mock XulDeck deckMock;
  @Mock RepositoryDirectoryInterface actualDirMock;
  @Mock RepositoryDirectoryInterface actualSubDirMock;
  @Mock List<RepositoryElementMetaInterface> repositoryObjectsMock;
  @Mock List<RepositoryDirectoryInterface> repositoryDirectoryInterfaces;
  @Mock MainController mainControllerMock;
  @Mock XulMessageBox messageBoxMock;
  @Mock Map<ObjectId, UIRepositoryDirectory> dirMapMock;

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    trBrController = new TrashBrowseControllerSpy();
  }

  @Test
  public void testShouldRefreshRepoDir_IfKettleExceptionOnRepoDirDeletion() throws Exception {
    KettleException ke = new KettleException( "TEST MESSAGE" );
    doThrow( ke ).when( repoDirMock ).delete();
    try {
      trBrController.deleteFolder( repoDirMock );
      fail( "Expected appearance KettleException: " + ke.getMessage() );
    } catch ( KettleException e ) {
      assertTrue( ke.getMessage().equals( e.getMessage() ) );
    }
    verify( repoDirMock, times( 1 ) ).refresh();
  }

  public class TrashBrowseControllerSpy extends TrashBrowseController {

    private static final long serialVersionUID = 1L;

    TrashBrowseControllerSpy() {
      super();
      this.repoDir = repoDirMock;
      this.selectedTrashFileItems = selectedTrashFileItemsMock;
      this.trashService = trashServiceMock;
      this.repository = repositoryMock;
      this.deck = deckMock;
      this.mainController = mainControllerMock;
      this.messageBox = messageBoxMock;
      this.dirMap = dirMapMock;
    }
  }

  @Test
  public void testUnDeleteNoFileSelected() throws Exception {
    when( selectedTrashFileItemsMock.toArray() ).thenReturn( new TrashBrowseController.UIDeletedObject[0] );
    expectedException.expect( RuntimeException.class );

    trBrController.undelete();
  }

  @Test
  public void testUnDeleteTransformation() throws Exception {
    testUnDelete( RepositoryObjectType.TRANSFORMATION.name(), true );

    verify( trashServiceMock, times( 1 ) ).undelete( anyList() );
    verify( transMetaMock, times( 1 ) ).clearChanged();
    verify( repositoryMock, times( 1 ) ).loadTransformation( objectIdMock, null );
    verify( deckMock, times( 1 ) ).setSelectedIndex( 1 );
  }

  @Test
  public void testUnDeleteJob() throws Exception {
    testUnDelete( RepositoryObjectType.JOB.name(), true );

    verify( trashServiceMock, times( 1 ) ).undelete( anyList() );
    verify( transMetaMock, never() ).clearChanged();
    verify( repositoryMock, never() ).loadTransformation( objectIdMock, null );
    verify( deckMock, times( 1 ) ).setSelectedIndex( 1 );
  }

  @Test
  public void testClosestUIRepositoryDirectory() throws Exception {
    testUnDelete( RepositoryObjectType.JOB.name(), false );

    verify( trashServiceMock, times( 1 ) ).undelete( anyList() );
    verify( transMetaMock, never() ).clearChanged();
    verify( repositoryMock, never() ).loadTransformation( objectIdMock, null );
    verify( repoDirMock, times( 1 ) ).refresh();
    verify( deckMock, times( 1 ) ).setSelectedIndex( 1 );
  }

  private void testUnDelete( String deletedObjectType, boolean directoryNull ) throws Exception {
    List<TrashBrowseController.UIDeletedObject> uiDeleteObjects = Arrays.asList( uiDeleteObjectMock );
    when( selectedTrashFileItemsMock.toArray() )
        .thenReturn( new TrashBrowseController.UIDeletedObject[] { uiDeleteObjectMock } );
    doReturn( "/home/admin" ).when( uiDeleteObjectMock ).getOriginalParentPath();
    doReturn( objectIdMock ).when( uiDeleteObjectMock ).getId();
    doReturn( deletedObjectType ).when( uiDeleteObjectMock ).getType();
    doReturn( uiDeleteObjects ).when( trashServiceMock ).getTrash();
    if ( directoryNull ) {
      doReturn( null ).when( repositoryMock ).findDirectory( "/home/admin" );
    } else {
      doReturn( actualSubDirMock ).when( repositoryMock ).findDirectory( "/home/admin" );
      doReturn( objectIdMock ).when( actualSubDirMock ).getObjectId();
      doReturn( null ).when( dirMapMock ).get( objectIdMock );
      doReturn( actualDirMock ).when( actualSubDirMock ).getParent();
      doReturn( objectIdMock2 ).when( actualDirMock ).getObjectId();
      doReturn( repoDirMock ).when( dirMapMock ).get( objectIdMock2 );

    }
    doReturn( transMetaMock ).when( repositoryMock ).loadTransformation( objectIdMock, null );

    trBrController.undelete();
  }

  @Test
  public void testUnDeleteDirectory() throws Exception {
    when( selectedTrashFileItemsMock.toArray() )
        .thenReturn( new TrashBrowseController.UIDeletedObject[] { uiDirectoryObjectMock } );
    doReturn( "/home/admin" ).when( uiDirectoryObjectMock ).getOriginalParentPath();
    doReturn( objectIdMock ).when( uiDirectoryObjectMock ).getId();
    doReturn( null ).when( uiDirectoryObjectMock ).getType();
    doReturn( Arrays.asList( uiDirectoryObjectMock ) ).when( trashServiceMock ).getTrash();
    doReturn( null ).when( repositoryMock ).findDirectory( "/home/admin" );
    doReturn( transMetaMock ).when( repositoryMock ).loadTransformation( objectIdMock, null );
    doReturn( "directory" ).when( uiDirectoryObjectMock ).getName();
    doReturn( actualDirMock ).when( repositoryMock ).findDirectory( "/home/admin/directory" );
    doReturn( null ).when( actualDirMock ).getChildren();
    doReturn( RepositoryObjectType.TRANSFORMATION ).when( objectInDirectory ).getObjectType();
    doReturn( objectIdMock ).when( objectInDirectory ).getObjectId();
    doReturn( Arrays.asList( objectInDirectory ) ).when( actualDirMock ).getRepositoryObjects();

    trBrController.undelete();

    verify( trashServiceMock, times( 1 ) ).undelete( anyList() );
    verify( transMetaMock, times( 1 ) ).clearChanged();
    verify( repositoryMock, times( 1 ) ).loadTransformation( objectIdMock, null );
    verify( repositoryMock, times( 1 ) ).findDirectory( "/home/admin/directory" );
    verify( actualDirMock, times( 1 ) ).getChildren();
    verify( deckMock, times( 1 ) ).setSelectedIndex( 1 );
  }

  @Test
  public void testUnDeleteDirectoryWSubDir() throws Exception {
    List<TrashBrowseController.UIDeletedObject> uiDeleteObjects = Arrays.asList( uiDirectoryObjectMock );
    when( selectedTrashFileItemsMock.toArray() )
        .thenReturn( new TrashBrowseController.UIDeletedObject[] { uiDirectoryObjectMock } );
    doReturn( "/home/admin" ).when( uiDirectoryObjectMock ).getOriginalParentPath();
    doReturn( objectIdMock ).when( uiDirectoryObjectMock ).getId();
    doReturn( null ).when( uiDirectoryObjectMock ).getType();
    doReturn( uiDeleteObjects ).when( trashServiceMock ).getTrash();
    doReturn( null ).when( repositoryMock ).findDirectory( "/home/admin" );
    doReturn( transMetaMock ).when( repositoryMock ).loadTransformation( objectIdMock, null );
    doReturn( "directory" ).when( uiDirectoryObjectMock ).getName();
    doReturn( actualDirMock ).when( repositoryMock ).findDirectory( "/home/admin/directory" );

    doReturn( Arrays.asList( actualSubDirMock ) ).when( actualDirMock ).getChildren();
    doReturn( null ).when( actualSubDirMock ).getChildren();
    doReturn( Arrays.asList( objectInDirectory ) ).when( actualSubDirMock ).getRepositoryObjects();
    doReturn( RepositoryObjectType.TRANSFORMATION ).when( objectInDirectory ).getObjectType();
    doReturn( objectIdMock ).when( objectInDirectory ).getObjectId();
    doReturn( new ArrayList<RepositoryElementMetaInterface>() ).when( actualDirMock ).getRepositoryObjects();

    trBrController.undelete();

    verify( trashServiceMock, times( 1 ) ).undelete( anyList() );
    verify( transMetaMock, times( 1 ) ).clearChanged();
    verify( repositoryMock, times( 1 ) ).loadTransformation( objectIdMock, null );
    verify( repositoryMock, times( 1 ) ).findDirectory( "/home/admin/directory" );
    verify( actualDirMock, times( 3 ) ).getChildren();
    verify( actualSubDirMock, times( 1 ) ).getChildren();
    verify( deckMock, times( 1 ) ).setSelectedIndex( 1 );
  }

  @Test
  public void testExceptionHandle() throws Exception {
    RuntimeException runtimeException = new RuntimeException( "Exception handle" );
    when( selectedTrashFileItemsMock.toArray() )
        .thenReturn( new TrashBrowseController.UIDeletedObject[] { uiDirectoryObjectMock } );
    doThrow( runtimeException ).when( trashServiceMock ).undelete( anyList() );
    doReturn( false ).when( mainControllerMock ).handleLostRepository( any( Throwable.class ) );

    trBrController.undelete();

    verify( messageBoxMock ).setTitle( "Error" );
    verify( messageBoxMock ).setAcceptLabel( "OK" );
    verify( messageBoxMock ).setMessage( contains( "Exception handle" ) );
    verify( messageBoxMock, times( 1 ) ).open();
    verify( deckMock, never() ).setSelectedIndex( 1 );
  }

  @Test
  public void testExceptionNotHandle() throws Exception {
    RuntimeException runtimeException = new RuntimeException( "Exception handle" );
    when( selectedTrashFileItemsMock.toArray() )
        .thenReturn( new TrashBrowseController.UIDeletedObject[] { uiDirectoryObjectMock } );
    doThrow( runtimeException ).when( trashServiceMock ).undelete( anyList() );
    doReturn( true ).when( mainControllerMock ).handleLostRepository( any( Throwable.class ) );

    trBrController.undelete();

    verify( messageBoxMock, never() ).setTitle( "Error" );
    verify( messageBoxMock, never() ).setAcceptLabel( "OK" );
    verify( messageBoxMock, never() ).setMessage( contains( "Exception handle" ) );
    verify( messageBoxMock, never() ).open();
    verify( deckMock, never() ).setSelectedIndex( 1 );
  }

  @Test
  public void testUnDeleteNotFoundDir() throws Exception {
    when( selectedTrashFileItemsMock.toArray() )
        .thenReturn( new TrashBrowseController.UIDeletedObject[] { uiDirectoryObjectMock } );
    doReturn( "/home/admin" ).when( uiDirectoryObjectMock ).getOriginalParentPath();
    doReturn( objectIdMock ).when( uiDirectoryObjectMock ).getId();
    doReturn( null ).when( uiDirectoryObjectMock ).getType();
    doReturn( Arrays.asList( uiDirectoryObjectMock ) ).when( trashServiceMock ).getTrash();
    doReturn( null ).when( repositoryMock ).findDirectory( "/home/admin" );
    doReturn( "directory" ).when( uiDirectoryObjectMock ).getName();
    doReturn( null ).when( repositoryMock ).findDirectory( "/home/admin/directory" );

    trBrController.undelete();

    verify( messageBoxMock ).setTitle( "Error" );
    verify( messageBoxMock ).setAcceptLabel( "OK" );
    verify( messageBoxMock )
        .setMessage( eq( "Unable to restore directory /home/admin/directory cause : Directory not found" ) );
    verify( messageBoxMock, times( 1 ) ).open();
    verify( deckMock, times( 1 ) ).setSelectedIndex( 1 );
  }
}
