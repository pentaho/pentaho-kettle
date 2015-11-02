/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnection;
import org.pentaho.ui.xul.containers.XulTree;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class ConnectionsControllerTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private ConnectionsController controller;
  private DatabaseDialog databaseDialog;
  private Repository repository;
  private XulTree connectionsTable;

  @Before
  public void setUp() throws Exception {
    // a tricky initialisation - first inject private fields
    controller = new ConnectionsController();
    connectionsTable = mock( XulTree.class );
    FieldUtils.writeDeclaredField( controller, "connectionsTable", connectionsTable, true );

    // and then spy the controller
    controller = spy( controller );

    databaseDialog = mock( DatabaseDialog.class );
    doReturn( databaseDialog ).when( controller ).getDatabaseDialog();
    doNothing().when( controller ).refreshConnectionList();
    doNothing().when( controller ).showAlreadyExistsMessage();

    repository = mock( Repository.class );
    controller.init( repository );
  }


  @Test
  public void createConnection_EmptyName() throws Exception {
    when( databaseDialog.open() ).thenReturn( "" );
    controller.createConnection();

    // repository was not accessed
    verify( repository, never() ).getDatabaseID( anyString() );
    verify( repository, never() ).save( any( DatabaseMeta.class ), anyString(), any( ProgressMonitorListener.class ) );
  }

  @Test
  public void createConnection_NameExists() throws Exception {
    final String dbName = "name";

    when( databaseDialog.open() ).thenReturn( dbName );
    when( repository.getDatabaseID( dbName ) ).thenReturn( new StringObjectId( "existing" ) );

    controller.createConnection();
    assertShowedAlreadyExistsMessage();
  }

  @Test
  public void createConnection_NewName() throws Exception {
    final String dbName = "name";

    when( databaseDialog.open() ).thenReturn( dbName );
    when( databaseDialog.getDatabaseMeta() ).thenReturn( new DatabaseMeta() );
    when( repository.getDatabaseID( dbName ) ).thenReturn( null );

    controller.createConnection();
    assertRepositorySavedDb();
  }

  @Test
  public void editConnection_EmptyName() throws Exception {
    final String dbName = "name";
    List<UIDatabaseConnection> selectedConnection = createSelectedConnectionList( dbName );
    when( connectionsTable.<UIDatabaseConnection>getSelectedItems() ).thenReturn( selectedConnection );

    when( repository.getDatabaseID( dbName ) ).thenReturn( new StringObjectId( "existing" ) );
    when( databaseDialog.open() ).thenReturn( "" );

    controller.editConnection();

    // repository.save() was not invoked
    verify( repository, never() ).save( any( DatabaseMeta.class ), anyString(), any( ProgressMonitorListener.class ) );
  }


  @Test
  public void editConnection_NameExists_Same() throws Exception {
    final String dbName = "name";
    List<UIDatabaseConnection> selectedConnection = createSelectedConnectionList( dbName );
    when( connectionsTable.<UIDatabaseConnection>getSelectedItems() ).thenReturn( selectedConnection );

    when( repository.getDatabaseID( dbName ) ).thenReturn( new StringObjectId( "existing" ) );
    when( databaseDialog.open() ).thenReturn( dbName );

    controller.editConnection();
    assertRepositorySavedDb();
  }

  @Test
  public void editConnection_NameDoesNotExist() throws Exception {
    final String dbName = "name";
    List<UIDatabaseConnection> selectedConnection = createSelectedConnectionList( dbName );
    when( connectionsTable.<UIDatabaseConnection>getSelectedItems() ).thenReturn( selectedConnection );

    when( repository.getDatabaseID( dbName ) ).thenReturn( new StringObjectId( "existing" ) );
    when( databaseDialog.open() ).thenReturn( "non-existing-name" );

    controller.editConnection();
    assertRepositorySavedDb();
  }

  @Test
  public void editConnection_NameExists_Different() throws Exception {
    final String dbName = "name";
    List<UIDatabaseConnection> selectedConnection = createSelectedConnectionList( dbName );
    when( connectionsTable.<UIDatabaseConnection>getSelectedItems() ).thenReturn( selectedConnection );

    final String anotherName = "anotherName";
    when( repository.getDatabaseID( dbName ) ).thenReturn( new StringObjectId( "existing" ) );
    when( repository.getDatabaseID( anotherName ) ).thenReturn( new StringObjectId( "another-existing" ) );
    when( databaseDialog.open() ).thenReturn( anotherName );

    controller.editConnection();
    assertShowedAlreadyExistsMessage();
  }

  private List<UIDatabaseConnection> createSelectedConnectionList( String selectedDbName ) {
    DatabaseMeta meta = new DatabaseMeta();
    meta.setName( selectedDbName );
    return singletonList( new UIDatabaseConnection( meta, repository ) );
  }


  private void assertShowedAlreadyExistsMessage() throws KettleException {
    // repository.save() was not invoked
    verify( repository, never() ).save( any( DatabaseMeta.class ), anyString(), any( ProgressMonitorListener.class ) );
    // instead the error dialog was shown
    verify( controller ).showAlreadyExistsMessage();
  }

  private void assertRepositorySavedDb() throws KettleException {
    // repository.save() was invoked
    verify( repository ).save( any( DatabaseMeta.class ), anyString(), any( ProgressMonitorListener.class ) );
  }
}
