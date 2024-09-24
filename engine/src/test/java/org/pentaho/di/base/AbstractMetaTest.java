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
package org.pentaho.di.base;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.changed.ChangedFlagInterface;
import org.pentaho.di.core.changed.PDIObserver;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.core.listeners.CurrentDirectoryChangedListener;
import org.pentaho.di.core.listeners.FilenameChangedListener;
import org.pentaho.di.core.listeners.NameChangedListener;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.core.osgi.api.NamedClusterServiceOsgi;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.util.PentahoDefaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class AbstractMetaTest {
  private AbstractMeta meta;
  private ObjectId objectId;
  private Repository repo;

  @Mock private MetastoreLocatorOsgi mockMetastoreLocatorOsgi;

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( DatabasePluginType.getInstance() );
    PluginRegistry.init();
  }

  @Before
  public void setUp() throws Exception {
    meta = new AbstractMetaStub();
    objectId = mock( ObjectId.class );
    repo = mock( Repository.class );
  }

  @Test
  public void testGetParent() {
    assertNull( meta.getParent() );
  }

  @Test
  public void testGetSetObjectId() {
    assertNull( meta.getObjectId() );
    meta.setObjectId( objectId );
    assertEquals( objectId, meta.getObjectId() );
  }

  @Test
  public void testGetSetContainerObjectId() {
    assertNull( meta.getContainerObjectId() );
    meta.setCarteObjectId( "myObjectId" );
    assertEquals( "myObjectId", meta.getContainerObjectId() );
  }

  @Test
  public void testGetSetName() {
    assertNull( meta.getName() );
    meta.setName( "myName" );
    assertEquals( "myName", meta.getName() );
  }

  @Test
  public void testGetSetDescription() {
    assertNull( meta.getDescription() );
    meta.setDescription( "I am a meta" );
    assertEquals( "I am a meta", meta.getDescription() );
  }

  @Test
  public void testGetSetExtendedDescription() {
    assertNull( meta.getExtendedDescription() );
    meta.setExtendedDescription( "I am a meta" );
    assertEquals( "I am a meta", meta.getExtendedDescription() );
  }

  @Test
  public void testNameFromFilename() {
    assertNull( meta.getName() );
    assertNull( meta.getFilename() );
    meta.nameFromFilename();
    assertNull( meta.getName() );
    meta.setFilename( "/path/to/my/file 2.ktr" );
    meta.nameFromFilename();
    assertEquals( "file 2", meta.getName() );
  }

  @Test
  public void testGetSetFilename() {
    assertNull( meta.getFilename() );
    meta.setFilename( "myfile" );
    assertEquals( "myfile", meta.getFilename() );
  }

  @Test
  public void testGetSetRepositoryDirectory() {
    assertNull( meta.getRepositoryDirectory() );
    RepositoryDirectoryInterface dir = mock( RepositoryDirectoryInterface.class );
    meta.setRepositoryDirectory( dir );
    assertEquals( dir, meta.getRepositoryDirectory() );
  }

  @Test
  public void testGetSetRepository() {
    assertNull( meta.getRepository() );
    meta.setRepository( repo );
    assertEquals( repo, meta.getRepository() );
  }

  @Test
  public void testGetSetDatabase() {
    assertEquals( 0, meta.nrDatabases() );
    assertNull( meta.getDatabases() );
    assertFalse( meta.haveConnectionsChanged() );
    meta.clear();
    assertTrue( meta.getDatabases().isEmpty() );
    assertEquals( 0, meta.nrDatabases() );
    assertFalse( meta.haveConnectionsChanged() );
    DatabaseMeta db1 = mock( DatabaseMeta.class );
    when( db1.getName() ).thenReturn( "db1" );
    when( db1.getDisplayName() ).thenReturn( "db1" );
    meta.addDatabase( db1 );
    assertEquals( 1, meta.nrDatabases() );
    assertFalse( meta.getDatabases().isEmpty() );
    assertTrue( meta.haveConnectionsChanged() );
    DatabaseMeta db2 = mock( DatabaseMeta.class );
    when( db2.getName() ).thenReturn( "db2" );
    when( db2.getDisplayName() ).thenReturn( "db2" );
    meta.addDatabase( db2 );
    assertEquals( 2, meta.nrDatabases() );
    // Test replace
    meta.addDatabase( db1, true );
    assertEquals( 2, meta.nrDatabases() );
    meta.addOrReplaceDatabase( db1 );
    assertEquals( 2, meta.nrDatabases() );
    // Test duplicate
    meta.addDatabase( db2, false );
    assertEquals( 2, meta.nrDatabases() );
    DatabaseMeta db3 = mock( DatabaseMeta.class );
    when( db3.getName() ).thenReturn( "db3" );
    meta.addDatabase( db3, false );
    assertEquals( 3, meta.nrDatabases() );
    assertEquals( db1, meta.getDatabase( 0 ) );
    assertEquals( 0, meta.indexOfDatabase( db1 ) );
    assertEquals( db2, meta.getDatabase( 1 ) );
    assertEquals( 1, meta.indexOfDatabase( db2 ) );
    assertEquals( db3, meta.getDatabase( 2 ) );
    assertEquals( 2, meta.indexOfDatabase( db3 ) );
    DatabaseMeta db4 = mock( DatabaseMeta.class );
    meta.addDatabase( 3, db4 );
    assertEquals( 4, meta.nrDatabases() );
    assertEquals( db4, meta.getDatabase( 3 ) );
    assertEquals( 3, meta.indexOfDatabase( db4 ) );
    meta.removeDatabase( 3 );
    assertEquals( 3, meta.nrDatabases() );
    assertTrue( meta.haveConnectionsChanged() );
    meta.clearChangedDatabases();
    assertFalse( meta.haveConnectionsChanged() );

    List<DatabaseMeta> list = Arrays.asList( db2, db1 );
    meta.setDatabases( list );
    assertEquals( 2, meta.nrDatabases() );
    assertEquals( "db1", meta.getDatabaseNames()[0] );
    assertEquals( 0, meta.indexOfDatabase( db1 ) );
    meta.removeDatabase( -1 );
    assertEquals( 2, meta.nrDatabases() );
    meta.removeDatabase( 2 );
    assertEquals( 2, meta.nrDatabases() );
    assertEquals( db1, meta.findDatabase( "db1" ) );
    assertNull( meta.findDatabase( "" ) );
  }

  @Test( expected = KettlePluginException.class )
  public void testGetSetImportMetaStore() throws Exception {
    assertNull( meta.getMetaStore() );
    meta.importFromMetaStore();
    IMetaStore metastore = mock( IMetaStore.class );
    meta.setMetaStore( metastore );
    assertEquals( metastore, meta.getMetaStore() );
    meta.importFromMetaStore();
    IMetaStoreElementType elementType = mock( IMetaStoreElementType.class );
    when( metastore.getElementTypeByName(
      PentahoDefaults.NAMESPACE, PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME ) ).thenReturn( elementType );
    when( metastore.getElements( PentahoDefaults.NAMESPACE, elementType ) )
      .thenReturn( new ArrayList<>() );
    meta.importFromMetaStore();
    IMetaStoreElement element = mock( IMetaStoreElement.class );
    when( metastore.getElements( PentahoDefaults.NAMESPACE, elementType ) )
      .thenReturn( Collections.singletonList( element ) );
    meta.importFromMetaStore();
  }

  @Test
  public void testAddNameChangedListener() {
    meta.fireNameChangedListeners( "a", "a" );
    meta.fireNameChangedListeners( "a", "b" );
    meta.addNameChangedListener( null );
    meta.fireNameChangedListeners( "a", "b" );
    NameChangedListener listener = mock( NameChangedListener.class );
    meta.addNameChangedListener( listener );
    meta.fireNameChangedListeners( "b", "a" );
    verify( listener, times( 1 ) ).nameChanged( meta, "b", "a" );
    meta.removeNameChangedListener( null );
    meta.removeNameChangedListener( listener );
    meta.fireNameChangedListeners( "b", "a" );
    verifyNoMoreInteractions( listener );
  }

  @Test
  public void testAddFilenameChangedListener() {
    meta.fireFilenameChangedListeners( "a", "a" );
    meta.fireFilenameChangedListeners( "a", "b" );
    meta.addFilenameChangedListener( null );
    meta.fireFilenameChangedListeners( "a", "b" );
    FilenameChangedListener listener = mock( FilenameChangedListener.class );
    meta.addFilenameChangedListener( listener );
    meta.fireFilenameChangedListeners( "b", "a" );
    verify( listener, times( 1 ) ).filenameChanged( meta, "b", "a" );
    meta.removeFilenameChangedListener( null );
    meta.removeFilenameChangedListener( listener );
    meta.fireFilenameChangedListeners( "b", "a" );
    verifyNoMoreInteractions( listener );
  }

  @Test
  public void testAddRemoveFireContentChangedListener() {
    assertTrue( meta.getContentChangedListeners().isEmpty() );
    ContentChangedListener listener = mock( ContentChangedListener.class );
    meta.addContentChangedListener( listener );
    assertFalse( meta.getContentChangedListeners().isEmpty() );
    meta.fireContentChangedListeners();
    verify( listener, times( 1 ) ).contentChanged( any() );
    verify( listener, never() ).contentSafe( any() );
    meta.fireContentChangedListeners( true );
    verify( listener, times( 2 ) ).contentChanged( any() );
    verify( listener, never() ).contentSafe( any() );
    meta.fireContentChangedListeners( false );
    verify( listener, times( 2 ) ).contentChanged( any() );
    verify( listener, times( 1 ) ).contentSafe( any() );
    meta.removeContentChangedListener( listener );
    assertTrue( meta.getContentChangedListeners().isEmpty() );
  }

  @Test
  public void testAddCurrentDirectoryChangedListener() {
    meta.fireNameChangedListeners( "a", "a" );
    meta.fireNameChangedListeners( "a", "b" );
    meta.addCurrentDirectoryChangedListener( null );
    meta.fireCurrentDirectoryChanged( "a", "b" );
    CurrentDirectoryChangedListener listener = mock( CurrentDirectoryChangedListener.class );
    meta.addCurrentDirectoryChangedListener( listener );
    meta.fireCurrentDirectoryChanged( "b", "a" );
    verify( listener, times( 1 ) ).directoryChanged( meta, "b", "a" );
    meta.fireCurrentDirectoryChanged( "a", "a" );
    meta.removeCurrentDirectoryChangedListener( null );
    meta.removeCurrentDirectoryChangedListener( listener );
    meta.fireNameChangedListeners( "b", "a" );
    verifyNoMoreInteractions( listener );
  }

  @Test
  public void testAddOrReplaceSlaveServer() {
    // meta.addOrReplaceSlaveServer() right now will fail with an NPE
    assertNull( meta.getSlaveServers() );
    List<SlaveServer> slaveServers = new ArrayList<>();
    meta.setSlaveServers( slaveServers );
    assertNotNull( meta.getSlaveServers() );
    SlaveServer slaveServer = mock( SlaveServer.class );
    meta.addOrReplaceSlaveServer( slaveServer );
    assertFalse( meta.getSlaveServers().isEmpty() );
    meta.addOrReplaceSlaveServer( slaveServer );
    assertEquals( 1, meta.getSlaveServerNames().length );
    assertNull( meta.findSlaveServer( null ) );
    assertNull( meta.findSlaveServer( "" ) );
    when( slaveServer.getName() ).thenReturn( "ss1" );
    assertEquals( slaveServer, meta.findSlaveServer( "ss1" ) );
  }

  @Test
  public void testAddRemoveViewUndo() {
    // addUndo() right now will fail with an NPE
    assertEquals( 0, meta.getUndoSize() );
    meta.clearUndo();
    assertEquals( 0, meta.getUndoSize() );
    assertEquals( 0, meta.getMaxUndo() );
    meta.setMaxUndo( 3 );
    assertEquals( 3, meta.getMaxUndo() );
    // viewThisUndo() and viewPreviousUndo() have the same logic
    assertNull( meta.viewThisUndo() );
    assertNull( meta.viewPreviousUndo() );
    assertNull( meta.viewNextUndo() );
    assertNull( meta.previousUndo() );
    assertNull( meta.nextUndo() );
    StepMeta fromMeta = mock( StepMeta.class );
    StepMeta toMeta = mock( StepMeta.class );
    Object[] from = new Object[]{ fromMeta };
    Object[] to = new Object[]{ toMeta };
    int[] pos = new int[0];
    Point[] prev = new Point[0];
    Point[] curr = new Point[0];

    meta.addUndo( from, to, pos, prev, curr, AbstractMeta.TYPE_UNDO_NEW, false );
    assertNotNull( meta.viewThisUndo() );
    assertNotNull( meta.viewPreviousUndo() );
    assertNull( meta.viewNextUndo() );
    meta.addUndo( from, to, pos, prev, curr, AbstractMeta.TYPE_UNDO_CHANGE, false );
    assertNotNull( meta.viewThisUndo() );
    assertNotNull( meta.viewPreviousUndo() );
    assertNull( meta.viewNextUndo() );
    TransAction action = meta.previousUndo();
    assertNotNull( action );
    assertEquals( AbstractMeta.TYPE_UNDO_CHANGE, action.getType() );
    assertNotNull( meta.viewThisUndo() );
    assertNotNull( meta.viewPreviousUndo() );
    assertNotNull( meta.viewNextUndo() );
    meta.addUndo( from, to, pos, prev, curr, AbstractMeta.TYPE_UNDO_DELETE, false );
    meta.addUndo( from, to, pos, prev, curr, AbstractMeta.TYPE_UNDO_POSITION, false );
    assertNotNull( meta.previousUndo() );
    assertNotNull( meta.nextUndo() );
    meta.setMaxUndo( 1 );
    assertEquals( 1, meta.getUndoSize() );
    meta.addUndo( from, to, pos, prev, curr, AbstractMeta.TYPE_UNDO_NEW, false );
  }

  @Test
  public void testGetSetAttributes() {
    assertNull( meta.getAttributesMap() );
    Map<String, Map<String, String>> attributesMap = new HashMap<>();
    meta.setAttributesMap( attributesMap );
    assertNull( meta.getAttributes( "group1" ) );
    Map<String, String> group1Attributes = new HashMap<>();
    attributesMap.put( "group1", group1Attributes );
    assertEquals( group1Attributes, meta.getAttributes( "group1" ) );
    assertNull( meta.getAttribute( "group1", "attr1" ) );
    group1Attributes.put( "attr1", "value1" );
    assertEquals( "value1", meta.getAttribute( "group1", "attr1" ) );
    assertNull( meta.getAttribute( "group1", "attr2" ) );
    meta.setAttribute( "group1", "attr2", "value2" );
    assertEquals( "value2", meta.getAttribute( "group1", "attr2" ) );
    meta.setAttributes( "group2", null );
    assertNull( meta.getAttributes( "group2" ) );
    meta.setAttribute( "group2", "attr3", "value3" );
    assertNull( meta.getAttribute( "group3", "attr4" ) );
  }

  @Test
  public void testNotes() {
    assertNull( meta.getNotes() );
    // most note methods will NPE at this point, so call clear() to create an empty note list
    meta.clear();
    assertNotNull( meta.getNotes() );
    assertTrue( meta.getNotes().isEmpty() );
    // Can't get a note from an empty list (i.e. no indices)
    Exception e = null;
    try {
      assertNull( meta.getNote( 0 ) );
    } catch ( IndexOutOfBoundsException ioobe ) {
      e = ioobe;
    }
    assertNotNull( e );
    assertNull( meta.getNote( 20, 20 ) );
    NotePadMeta note1 = mock( NotePadMeta.class );
    meta.removeNote( 0 );
    assertFalse( meta.hasChanged() );
    meta.addNote( note1 );
    assertTrue( meta.hasChanged() );
    NotePadMeta note2 = mock( NotePadMeta.class );
    when( note2.getLocation() ).thenReturn( new Point( 0, 0 ) );
    when( note2.isSelected() ).thenReturn( true );
    meta.addNote( 1, note2 );
    assertEquals( note2, meta.getNote( 0, 0 ) );
    List<NotePadMeta> selectedNotes = meta.getSelectedNotes();
    assertNotNull( selectedNotes );
    assertEquals( 1, selectedNotes.size() );
    assertEquals( note2, selectedNotes.get( 0 ) );
    assertEquals( 1, meta.indexOfNote( note2 ) );
    meta.removeNote( 2 );
    assertEquals( 2, meta.nrNotes() );
    meta.removeNote( 1 );
    assertEquals( 1, meta.nrNotes() );
    assertTrue( meta.haveNotesChanged() );
    meta.clearChanged();
    assertFalse( meta.haveNotesChanged() );

    meta.addNote( 1, note2 );
    meta.lowerNote( 1 );
    assertTrue( meta.haveNotesChanged() );
    meta.clearChanged();
    assertFalse( meta.haveNotesChanged() );
    meta.raiseNote( 0 );
    assertTrue( meta.haveNotesChanged() );
    meta.clearChanged();
    assertFalse( meta.haveNotesChanged() );
    int[] indexes = meta.getNoteIndexes( Arrays.asList( note1, note2 ) );
    assertNotNull( indexes );
    assertEquals( 2, indexes.length );
  }


  @Test
  public void testCopyVariablesFrom() {
    assertNull( meta.getVariable( "var1" ) );
    VariableSpace vars = mock( VariableSpace.class );
    when( vars.getVariable( "var1" ) ).thenReturn( "x" );
    when( vars.listVariables() ).thenReturn( new String[]{ "var1" } );
    meta.copyVariablesFrom( vars );
    assertEquals( "x", meta.getVariable( "var1", "y" ) );
  }

  @Test
  public void testEnvironmentSubstitute() {
    // This is just a delegate method, verify it's called
    VariableSpace vars = mock( VariableSpace.class );
    // This method is reused by the stub to set the mock as the variables object
    meta.setInternalKettleVariables( vars );

    meta.environmentSubstitute( "${param}" );
    verify( vars, times( 1 ) ).environmentSubstitute( "${param}" );
    String[] params = new String[]{ "${param}" };
    meta.environmentSubstitute( params );
    verify( vars, times( 1 ) ).environmentSubstitute( params );
  }

  @Test
  public void testFieldSubstitute() throws Exception {
    // This is just a delegate method, verify it's called
    VariableSpace vars = mock( VariableSpace.class );
    // This method is reused by the stub to set the mock as the variables object
    meta.setInternalKettleVariables( vars );

    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    Object[] data = new Object[0];
    meta.fieldSubstitute( "?{param}", rowMeta, data );
    verify( vars, times( 1 ) ).fieldSubstitute( "?{param}", rowMeta, data );
  }

  @Test
  public void testGetSetParentVariableSpace() {
    assertNull( meta.getParentVariableSpace() );
    VariableSpace variableSpace = mock( VariableSpace.class );
    meta.setParentVariableSpace( variableSpace );
    assertEquals( variableSpace, meta.getParentVariableSpace() );
  }

  @Test
  public void testGetSetVariable() {
    assertNull( meta.getVariable( "var1" ) );
    assertEquals( "x", meta.getVariable( "var1", "x" ) );
    meta.setVariable( "var1", "y" );
    assertEquals( "y", meta.getVariable( "var1", "x" ) );
  }

  @Test
  public void testGetSetParameterValue() throws Exception {
    assertNull( meta.getParameterValue( "var1" ) );
    assertNull( meta.getParameterDefault( "var1" ) );
    assertNull( meta.getParameterDescription( "var1" ) );

    meta.setParameterValue( "var1", "y" );
    // Values for new parameters must be added by addParameterDefinition
    assertNull( meta.getParameterValue( "var1" ) );
    assertNull( meta.getParameterDefault( "var1" ) );
    assertNull( meta.getParameterDescription( "var1" ) );

    meta.addParameterDefinition( "var2", "z", "My Description" );
    assertEquals( "", meta.getParameterValue( "var2" ) );
    assertEquals( "z", meta.getParameterDefault( "var2" ) );
    assertEquals( "My Description", meta.getParameterDescription( "var2" ) );
    meta.setParameterValue( "var2", "y" );
    assertEquals( "y", meta.getParameterValue( "var2" ) );
    assertEquals( "z", meta.getParameterDefault( "var2" ) );

    String[] params = meta.listParameters();
    assertNotNull( params );

    // clearParameters() just clears their values, not their presence
    meta.clearParameters();
    assertEquals( "", meta.getParameterValue( "var2" ) );

    // eraseParameters() clears the list of parameters
    meta.eraseParameters();
    assertNull( meta.getParameterValue( "var1" ) );

    NamedParams newParams = new NamedParamsDefault();
    newParams.addParameterDefinition( "var3", "default", "description" );
    newParams.setParameterValue( "var3", "a" );
    newParams.addParameterDefinition( "emptyVar", "", "emptyDesc" );
    newParams.setParameterValue( "emptyVar", "" );
    meta.copyParametersFrom( newParams );
    meta.activateParameters();
    assertEquals( "default", meta.getParameterDefault( "var3" ) );
    assertEquals( "", meta.getParameterDefault( "emptyVar" ) );
  }

  @Test
  public void testGetSetLogLevel() {
    assertEquals( LogLevel.BASIC, meta.getLogLevel() );
    meta.setLogLevel( LogLevel.DEBUG );
    assertEquals( LogLevel.DEBUG, meta.getLogLevel() );
  }

  @Test
  public void testGetSetSharedObjectsFile() {
    assertNull( meta.getSharedObjectsFile() );
    meta.setSharedObjectsFile( "mySharedObjects" );
    assertEquals( "mySharedObjects", meta.getSharedObjectsFile() );
  }

  @Test
  public void testGetSetSharedObjects() {
    SharedObjects sharedObjects = mock( SharedObjects.class );
    meta.setSharedObjects( sharedObjects );
    assertEquals( sharedObjects, meta.getSharedObjects() );
    meta.setSharedObjects( null );
    AbstractMeta spyMeta = spy( meta );
    assertNotNull( spyMeta.getSharedObjects() );
  }

  @Test
  public void testGetSetCreatedDate() {
    assertNull( meta.getCreatedDate() );
    Date now = Calendar.getInstance().getTime();
    meta.setCreatedDate( now );
    assertEquals( now, meta.getCreatedDate() );
  }

  @Test
  public void testGetSetCreatedUser() {
    assertNull( meta.getCreatedUser() );
    meta.setCreatedUser( "joe" );
    assertEquals( "joe", meta.getCreatedUser() );
  }

  @Test
  public void testGetSetModifiedDate() {
    assertNull( meta.getModifiedDate() );
    Date now = Calendar.getInstance().getTime();
    meta.setModifiedDate( now );
    assertEquals( now, meta.getModifiedDate() );
  }

  @Test
  public void testGetSetModifiedUser() {
    assertNull( meta.getModifiedUser() );
    meta.setModifiedUser( "joe" );
    assertEquals( "joe", meta.getModifiedUser() );
  }

  @Test
  public void testAddDeleteModifyObserver() {
    PDIObserver observer = mock( PDIObserver.class );
    meta.addObserver( observer );
    Object event = new Object();
    meta.notifyObservers( event );
    // Changed flag isn't set, so this won't be called
    verify( observer, never() ).update( meta, event );
    meta.setChanged( true );
    meta.notifyObservers( event );
    verify( observer, times( 1 ) ).update( any( ChangedFlagInterface.class ), any() );
  }

  @Test
  public void testGetRegistrationDate() {
    assertNull( meta.getRegistrationDate() );
  }

  @Test
  public void testGetObjectNameCopyRevision() {
    assertNull( meta.getObjectName() );
    meta.setName( "x" );
    assertEquals( "x", meta.getObjectName() );
    assertNull( meta.getObjectCopy() );
    assertNull( meta.getObjectRevision() );
    ObjectRevision rev = mock( ObjectRevision.class );
    meta.setObjectRevision( rev );
    assertEquals( rev, meta.getObjectRevision() );
  }

  @Test
  public void testHasMissingPlugins() {
    assertFalse( meta.hasMissingPlugins() );
  }

  @Test
  public void testGetSetPrivateDatabases() {
    assertNull( meta.getPrivateDatabases() );
    Set<String> dbs = new HashSet<>();
    meta.setPrivateDatabases( dbs );
    assertEquals( dbs, meta.getPrivateDatabases() );
  }

  @Test
  public void testGetSetChannelLogTable() {
    assertNull( meta.getChannelLogTable() );
    ChannelLogTable table = mock( ChannelLogTable.class );
    meta.setChannelLogTable( table );
    assertEquals( table, meta.getChannelLogTable() );
  }

  @Test
  public void testGetEmbeddedMetaStore() {
    assertNotNull( meta.getEmbeddedMetaStore() );
  }

  @Test
  public void testGetBooleanValueOfVariable() {
    assertFalse( meta.getBooleanValueOfVariable( null, false ) );
    assertTrue( meta.getBooleanValueOfVariable( "", true ) );
    assertTrue( meta.getBooleanValueOfVariable( "true", true ) );
    assertFalse( meta.getBooleanValueOfVariable( "${myVar}", false ) );
    meta.setVariable( "myVar", "Y" );
    assertTrue( meta.getBooleanValueOfVariable( "${myVar}", false ) );
  }

  @Test
  public void testInitializeShareInjectVariables() {
    meta.initializeVariablesFrom( null );
    VariableSpace parent = mock( VariableSpace.class );
    when( parent.getVariable( "var1" ) ).thenReturn( "x" );
    when( parent.listVariables() ).thenReturn( new String[]{ "var1" } );
    meta.initializeVariablesFrom( parent );
    assertEquals( "x", meta.getVariable( "var1" ) );
    assertNotNull( meta.listVariables() );
    VariableSpace newVars = mock( VariableSpace.class );
    when( newVars.getVariable( "var2" ) ).thenReturn( "y" );
    meta.shareVariablesWith( newVars );
    assertEquals( "y", meta.getVariable( "var2" ) );
    Map<String, String> props = new HashMap<>();
    props.put( "var3", "a" );
    props.put( "var4", "b" );
    meta.shareVariablesWith( new Variables() );
    meta.injectVariables( props );
    // Need to "Activate" the injection, we can initialize from null
    meta.initializeVariablesFrom( null );
    assertEquals( "a", meta.getVariable( "var3" ) );
    assertEquals( "b", meta.getVariable( "var4" ) );
  }

  @Test
  public void testCanSave() {
    assertTrue( meta.canSave() );
  }

  @Test
  public void testHasChanged() {
    meta.clear();
    assertFalse( meta.hasChanged() );
    meta.setChanged( true );
    assertTrue( meta.hasChanged() );
  }

  @Test
  public void testShouldOverwrite() {
    assertTrue( meta.shouldOverwrite( null, null, null, null ) );
    Props.init( Props.TYPE_PROPERTIES_EMPTY );
    assertTrue( meta.shouldOverwrite( null, Props.getInstance(), "message", "remember" ) );

    Props.getInstance().setProperty( Props.STRING_ASK_ABOUT_REPLACING_DATABASES, "Y" );
    OverwritePrompter prompter = mock( OverwritePrompter.class );
    when( prompter.overwritePrompt( "message", "remember", Props.STRING_ASK_ABOUT_REPLACING_DATABASES ) )
      .thenReturn( false );
    assertFalse( meta.shouldOverwrite( prompter, Props.getInstance(), "message", "remember" ) );
  }

  @Test
  public void testGetSetNamedClusterServiceOsgi() {
    assertNull( meta.getNamedClusterServiceOsgi() );
    NamedClusterServiceOsgi mockNamedClusterOsgi = mock( NamedClusterServiceOsgi.class );
    meta.setNamedClusterServiceOsgi( mockNamedClusterOsgi );
    assertEquals( mockNamedClusterOsgi, meta.getNamedClusterServiceOsgi() );
  }

  @Test
  public void testGetNamedClusterEmbedManager() {
    assertNull( meta.getNamedClusterEmbedManager() );
    NamedClusterEmbedManager mockNamedClusterEmbedManager = mock( NamedClusterEmbedManager.class );
    meta.namedClusterEmbedManager = mockNamedClusterEmbedManager;
    assertEquals( mockNamedClusterEmbedManager, meta.getNamedClusterEmbedManager() );
  }

  @Test
  public void testEmbeddedMetastoreProviderKeyIsMemoized() {
    String keyValue = "keyValue";
    meta.setMetastoreLocatorOsgi( mockMetastoreLocatorOsgi );
    when( mockMetastoreLocatorOsgi.setEmbeddedMetastore( meta.getEmbeddedMetaStore() ) ).thenReturn( keyValue );

    assertEquals( keyValue, meta.getEmbeddedMetastoreProviderKey() );
    assertEquals( keyValue, meta.getEmbeddedMetastoreProviderKey() );
    verify( mockMetastoreLocatorOsgi, times( 1 ) )
      .setEmbeddedMetastore( meta.getEmbeddedMetaStore() );
  }


  @Test
  public void testGetSetMetastoreLocatorOsgi() {
    assertNull( meta.getMetastoreLocatorOsgi() );
    meta.setMetastoreLocatorOsgi( mockMetastoreLocatorOsgi );
    assertEquals( mockMetastoreLocatorOsgi, meta.getMetastoreLocatorOsgi() );
  }

  @Test
  public void testMultithreadHammeringOfListener() throws InterruptedException {

    CountDownLatch latch = new CountDownLatch( 3 );
    AbstractMetaListenerThread th1 = new AbstractMetaListenerThread( meta, 2000, latch ); // do 2k random add/delete/fire
    AbstractMetaListenerThread th2 = new AbstractMetaListenerThread( meta, 2000, latch ); // do 2k random add/delete/fire
    AbstractMetaListenerThread th3 = new AbstractMetaListenerThread( meta, 2000, latch ); // do 2k random add/delete/fire

    Thread t1 = new Thread( th1 );
    Thread t2 = new Thread( th2 );
    Thread t3 = new Thread( th3 );
    t1.start();
    t2.start();
    t3.start();
    assertTrue( "Test took longer than 5 minutes, deadlock?", latch.await( 300, TimeUnit.SECONDS ) ); // Will hang out waiting for each thread to complete...
    assertEquals( "No exceptions encountered", th1.message );
    assertEquals( "No exceptions encountered", th2.message );
    assertEquals( "No exceptions encountered", th3.message );
  }

  /**
   * Stub class for AbstractMeta. No need to test the abstract methods here, they should be done in unit tests for
   * proper child classes.
   */
  public static class AbstractMetaStub extends AbstractMeta {

    // Reuse this method to set a mock internal variable space
    @Override
    public void setInternalKettleVariables( VariableSpace var ) {
      this.variables = var;
    }

    @Override
    protected void setInternalFilenameKettleVariables( VariableSpace var ) {

    }

    @Override
    protected void setInternalNameKettleVariable( VariableSpace var ) {

    }

    @Override
    public String getXML() throws KettleException {
      return null;
    }

    @Override
    public String getFileType() {
      return null;
    }

    @Override
    public String[] getFilterNames() {
      return new String[0];
    }

    @Override
    public String[] getFilterExtensions() {
      return new String[0];
    }

    @Override
    public String getDefaultExtension() {
      return null;
    }

    @Override
    public void saveSharedObjects() throws KettleException {
    }

    @Override
    public String getLogChannelId() {
      return null;
    }

    @Override
    public LoggingObjectType getObjectType() {
      return null;
    }

    @Override
    public boolean isGatheringMetrics() {
      return false;
    }

    @Override
    public void setGatheringMetrics( boolean b ) {
    }

    @Override
    public void setForcingSeparateLogging( boolean b ) {
    }

    @Override
    public boolean isForcingSeparateLogging() {
      return false;
    }

    @Override
    public RepositoryObjectType getRepositoryElementType() {
      return null;
    }
  }



  private static class AbstractMetaListenerThread implements Runnable {
    AbstractMeta metaToWork;
    int times;
    CountDownLatch whenDone;
    String message;

    AbstractMetaListenerThread( AbstractMeta aMeta, int times, CountDownLatch latch ) {
      this.metaToWork = aMeta;
      this.times = times;
      this.whenDone = latch;
    }

    @Override public void run() {
      for ( int i = 0; i < times; i++ ) {
        int randomNum = ThreadLocalRandom.current().nextInt( 0, 3 );
        switch ( randomNum ) {
          case 0: {
            try {
              metaToWork.addFilenameChangedListener( mock( FilenameChangedListener.class ) );
            } catch ( Throwable ex ) {
              message = "Exception adding listener.";
            }
            break;
          }
          case 1: {
            try {
              metaToWork.removeFilenameChangedListener( mock( FilenameChangedListener.class ) );
            } catch ( Throwable ex ) {
              message = "Exception removing listener.";
            }
            break;
          }
          default: {
            try {
              metaToWork.fireFilenameChangedListeners( "oldName", "newName" );
            } catch ( Throwable ex ) {
              message = "Exception firing listeners.";
            }
            break;
          }
        }
      }
      if ( message == null ) {
        message = "No exceptions encountered";
      }
      whenDone.countDown(); // show success...
    }
  }

}
