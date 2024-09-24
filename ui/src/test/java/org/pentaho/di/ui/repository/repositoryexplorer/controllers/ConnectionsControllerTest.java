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

package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnection;
import org.pentaho.ui.xul.containers.XulTree;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Andrey Khayrutdinov
 */
public class ConnectionsControllerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private ConnectionsController controller;
  private DatabaseDialog databaseDialog;
  private DatabaseMeta databaseMeta;
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
    databaseMeta = mock( DatabaseMeta.class );
    doReturn( databaseMeta ).when( databaseDialog ).getDatabaseMeta();
    doNothing().when( controller ).refreshConnectionList();
    doNothing().when( controller ).showAlreadyExistsMessage();

    repository = mock( Repository.class );
    controller.init( repository );
  }


  @Test
  public void createConnection_NullName() throws Exception {
    testEditConnectionGetsWrongName( null );
  }

  @Test
  public void createConnection_EmptyName() throws Exception {
    testEditConnectionGetsWrongName( "" );
  }

  @Test
  public void createConnection_BlankName() throws Exception {
    testCreateConnectionGetsWrongName( "  " );
  }

  private void testCreateConnectionGetsWrongName( String wrongName ) throws Exception {
    when( databaseDialog.open() ).thenReturn( wrongName );
    when( databaseMeta.getDatabaseName() ).thenReturn( wrongName );
    controller.createConnection();
    assertRepositoryWasNotAccessed();
  }

  private void assertRepositoryWasNotAccessed() throws KettleException {
    verify( repository, never() ).getDatabaseID( anyString() );
    assertSaveWasNotInvoked();
  }


  @Test
  public void createConnection_NameExists() throws Exception {
    final String dbName = "name";

    when( databaseDialog.open() ).thenReturn( dbName );
    when( databaseMeta.getDatabaseName() ).thenReturn( dbName );
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
  public void editConnection_NullName() throws Exception {
    testEditConnectionGetsWrongName( null );
  }

  @Test
  public void editConnection_EmptyName() throws Exception {
    testEditConnectionGetsWrongName( "" );
  }

  @Test
  public void editConnection_BlankName() throws Exception {
    testEditConnectionGetsWrongName( "  " );
  }

  private void testEditConnectionGetsWrongName( String wrongName ) throws Exception {
    final String dbName = "name";
    List<UIDatabaseConnection> selectedConnection = createSelectedConnectionList( dbName );
    when( connectionsTable.<UIDatabaseConnection>getSelectedItems() ).thenReturn( selectedConnection );

    when( repository.getDatabaseID( dbName ) ).thenReturn( new StringObjectId( "existing" ) );
    when( databaseDialog.open() ).thenReturn( wrongName );

    controller.editConnection();

    assertSaveWasNotInvoked();
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

  private void assertSaveWasNotInvoked() throws KettleException {
    verify( repository, never() ).save( any(), anyString(), any() );
  }

  private void assertShowedAlreadyExistsMessage() throws KettleException {
    assertSaveWasNotInvoked();
    // instead the error dialog was shown
    verify( controller ).showAlreadyExistsMessage();
  }

  private void assertRepositorySavedDb() throws KettleException {
    // repository.save() was invoked
    verify( repository ).save( any(), anyString(), any() );
  }

  @Test
  public void editConnection_NameExists_SameWithSpaces() throws Exception {
    final String dbName = " name";
    DatabaseMeta dbmeta = spy( new DatabaseMeta() );
    dbmeta.setName( dbName );
    List<UIDatabaseConnection> selectedConnection = singletonList( new UIDatabaseConnection( dbmeta, repository ) );

    when( connectionsTable.<UIDatabaseConnection>getSelectedItems() ).thenReturn( selectedConnection );

    when( repository.getDatabaseID( dbName ) ).thenReturn( new StringObjectId( "existing" ) );
    when( databaseDialog.open() ).thenReturn( dbName );

    controller.editConnection();
    verify( dbmeta ).setName( dbName.trim() );
  }
}
