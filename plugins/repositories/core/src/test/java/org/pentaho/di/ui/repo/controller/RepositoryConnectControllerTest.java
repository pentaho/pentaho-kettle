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

package org.pentaho.di.ui.repo.controller;

import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by bmorrise on 5/3/16.
 * modified by amit kumar 8/sep/22
 */
@RunWith( MockitoJUnitRunner.class )
public class RepositoryConnectControllerTest {

  public static final String PLUGIN_NAME = "PLUGIN NAME";
  public static final String ID = "ID";
  public static final String PLUGIN_DESCRIPTION = "PLUGIN DESCRIPTION";
  public static final String DATABASE_NAME = "DATABASE NAME";
  public static final String REPOSITORY_NAME = "Repository Name";
  public static final String REPOSITORY_ID = "Repository ID";
  public static final String REPOSITORY_DESCRIPTION = "Repository Description";

  @Mock
  RepositoriesMeta repositoriesMeta;

  @Mock
  PluginRegistry pluginRegistry;

  @Mock
  RepositoryMeta repositoryMeta;

  @Mock
  PluginInterface pluginInterface;

  @Mock
  AbstractRepository repository;

  @Mock
  DatabaseMeta databaseMeta;

  @Mock
  PropsUI propsUI;

  @Mock
  Spoon spoon;

  private RepositoryConnectController controller;

  @BeforeClass
  public static void setUpClass() throws Exception {
    if ( !KettleEnvironment.isInitialized() ) {
      KettleEnvironment.init();
    }
  }

  @Before
  public void setUp() {
    controller = new RepositoryConnectController( pluginRegistry, () -> spoon, repositoriesMeta );

    when( pluginInterface.getName() ).thenReturn( PLUGIN_NAME );
    when( pluginInterface.getIds() ).thenReturn( new String[] { ID } );
    when( pluginInterface.getDescription() ).thenReturn( PLUGIN_DESCRIPTION );

    List<PluginInterface> plugins = new ArrayList<>();
    plugins.add( pluginInterface );

    when( pluginRegistry.getPlugins( RepositoryPluginType.class ) ).thenReturn( plugins );

    when( repositoryMeta.getId() ).thenReturn( ID );
    when( repositoryMeta.getName() ).thenReturn( PLUGIN_NAME );
  }

  @Test
  public void testGetPlugins() throws Exception {
    String plugins = controller.getPlugins();
    assertEquals( "[{\"name\":\"PLUGIN NAME\",\"description\":\"PLUGIN DESCRIPTION\",\"id\":\"ID\"}]", plugins );
  }

  @Test
  public void testGetSetParentShell() {
    assertNull( controller.getParentShell() );

    Shell mockShell = mock( Shell.class );
    controller.setParentShell( mockShell );
    assertNotNull( controller.getParentShell() );
    assertEquals( mockShell, controller.getParentShell() );
  }

  @Test
  public void testCreateRepository() throws Exception {
    String id = ID;
    Map<String, Object> items = new HashMap<>();

    when( pluginRegistry.loadClass( RepositoryPluginType.class, id, RepositoryMeta.class ) )
      .thenReturn( repositoryMeta );
    when( pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta.getId(), Repository.class ) )
      .thenReturn( repository );

    when( repository.test() ).thenReturn( true );

    RepositoryMeta result = controller.createRepository( id, items );

    assertNotEquals( null, result );

    when( repository.test() ).thenReturn( false );

    result = controller.createRepository( id, items );

    assertEquals( null, result );

    doThrow( new KettleException( "forced exception" ) ).when( repositoriesMeta ).writeData();

    result = controller.createRepository( id, items );
    assertEquals( null, result );
  }

  @Test
  public void testConnectToRepository() throws Exception {
    when( pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta.getId(), Repository.class ) )
      .thenReturn( repository );

    controller.setCurrentRepository( repositoryMeta );
    controller.connectToRepository();

    verify( repository ).init( repositoryMeta );
    verify( repository ).connect( null, null );
    verify( spoon, times( 1 ) ).clearRepositoryDirectory();
  }


  @Test
  public void testGetDatabases() throws Exception {
    when( repositoriesMeta.nrDatabases() ).thenReturn( 1 );
    when( repositoriesMeta.getDatabase( 0 ) ).thenReturn( databaseMeta );
    when( databaseMeta.getName() ).thenReturn( DATABASE_NAME );

    String databases = controller.getDatabases().toString();
    assertEquals( "[{\"name\":\"DATABASE NAME\"}]", databases );
  }


  @Test
  public void testDeleteRepository() throws Exception {
    int index = 1;
    when( repositoriesMeta.findRepository( REPOSITORY_NAME ) ).thenReturn( repositoryMeta );
    when( repositoriesMeta.indexOfRepository( repositoryMeta ) ).thenReturn( index );
    boolean result = controller.deleteRepository( REPOSITORY_NAME );

    assertEquals( true, result );
    verify( repositoriesMeta ).removeRepository( index );
    verify( repositoriesMeta ).writeData();
  }

  @Test
  public void testSetDefaultRepository() {
    boolean result = controller.setDefaultRepository( REPOSITORY_NAME );
    assertEquals( true, result );
  }

  @Test
  public void testAddDatabase() throws Exception {
    controller.addDatabase( databaseMeta );

    verify( repositoriesMeta ).addDatabase( databaseMeta );
    verify( repositoriesMeta ).writeData();
  }


  @Test
  public void testGetRepository() throws Exception {
    KettleFileRepositoryMeta kettleFileRepositoryMeta = new KettleFileRepositoryMeta();
    kettleFileRepositoryMeta.setId( REPOSITORY_ID );
    kettleFileRepositoryMeta.setDescription( REPOSITORY_DESCRIPTION );
    kettleFileRepositoryMeta.setName( REPOSITORY_NAME );

    when( repositoriesMeta.findRepository( REPOSITORY_NAME ) ).thenReturn( kettleFileRepositoryMeta );

    JSONObject output = controller.getRepository( REPOSITORY_NAME );
    String outputString = output.toJSONString();

    assertEquals( true, outputString.contains( REPOSITORY_ID ) );
    assertEquals( true, outputString.contains( REPOSITORY_DESCRIPTION ) );
    assertEquals( true, outputString.contains( REPOSITORY_NAME ) );
  }


  @Test
  public void testRepoSwitch() throws Exception {
    when( pluginRegistry.loadClass( RepositoryPluginType.class, REPOSITORY_ID, Repository.class ) ).thenReturn(
        repository );

    KettleFileRepositoryMeta kettleFileRepositoryMeta = new KettleFileRepositoryMeta();
    kettleFileRepositoryMeta.setId( REPOSITORY_ID );
    kettleFileRepositoryMeta.setDescription( REPOSITORY_DESCRIPTION );
    kettleFileRepositoryMeta.setName( REPOSITORY_NAME );

    controller.connectToRepository( kettleFileRepositoryMeta );

    verify( spoon ).closeAllJobsAndTransformations( true );

    when( spoon.getRepository() ).thenReturn( repository );
    controller.connectToRepository( kettleFileRepositoryMeta );

    verify( spoon ).closeRepository();
  }


  @Test
  public void testOnlySetConnectedOnConnect() throws Exception {
    when( pluginRegistry.loadClass( RepositoryPluginType.class, ID, Repository.class ) )
      .thenReturn( repository );
    when( pluginRegistry.loadClass( RepositoryPluginType.class, ID, RepositoryMeta.class ) )
      .thenReturn( repositoryMeta );

    when( repository.test() ).thenReturn( true );

    Map<String, Object> items = new HashMap<>();
    RepositoryMeta result = controller.createRepository( ID, items );
    controller.setCurrentRepository( repositoryMeta );

    assertNotEquals( null, result );
    assertNull( controller.getConnectedRepository() );

    controller.connectToRepository();
    assertNotNull( controller.getConnectedRepository() );
  }

  @Test
  public void testEditConnectedRepository() throws Exception {
    RepositoryMeta before = new TestRepositoryMeta( ID, "name1", PLUGIN_DESCRIPTION, "same" );

    doReturn( repository ).when( pluginRegistry ).loadClass( RepositoryPluginType.class, ID, Repository.class );
    when( repositoriesMeta.findRepository( nullable( String.class ) ) ).thenReturn( before );

    controller.setConnectedRepository( before );
    controller.setCurrentRepository( before );

    Map<String, Object> map = new HashMap<>();
    map.put( RepositoryConnectController.DISPLAY_NAME, "name2" );
    map.put( RepositoryConnectController.IS_DEFAULT, true );
    map.put( RepositoryConnectController.DESCRIPTION, PLUGIN_DESCRIPTION );

    controller.updateRepository( ID, map );
    assertEquals( "name2", controller.getConnectedRepository().getName() );
  }

  @Test
  public void testIsDatabaseWithNameExist() throws Exception {
    final DatabaseMeta databaseMeta1 = new DatabaseMeta();
    databaseMeta1.setName( "TestDB1" );
    controller.addDatabase( databaseMeta1 );
    final DatabaseMeta databaseMeta2 = new DatabaseMeta();
    databaseMeta2.setName( "TestDB2" );
    controller.addDatabase( databaseMeta2 );

    when( repositoriesMeta.nrDatabases() ).thenReturn( 2 );
    when( repositoriesMeta.getDatabase( 0 ) ).thenReturn( databaseMeta1 );
    when( repositoriesMeta.getDatabase( 1 ) ).thenReturn( databaseMeta2 );


    //existing databases
    assertFalse( controller.isDatabaseWithNameExist( databaseMeta1, false ) );
    databaseMeta2.setName( "TestDB1" );
    assertTrue( controller.isDatabaseWithNameExist( databaseMeta2, false ) );

    //new databases
    final DatabaseMeta databaseMeta3 = new DatabaseMeta();
    databaseMeta3.setName( "TestDB3" );
    assertFalse( controller.isDatabaseWithNameExist( databaseMeta3, true ) );
    databaseMeta3.setName( "TestDB1" );
    assertTrue( controller.isDatabaseWithNameExist( databaseMeta3, true ) );
  }

  private static class TestRepositoryMeta extends BaseRepositoryMeta implements RepositoryMeta {

    private String innerStuff;

    public TestRepositoryMeta( String id, String name, String description, String innerStuff ) {
      super( id, name, description, false );
      this.innerStuff = innerStuff;
    }

    @Override
    public RepositoryCapabilities getRepositoryCapabilities() {
      return null;
    }

    @Override
    public RepositoryMeta clone() {
      return new TestRepositoryMeta( getId(), getName(), getDescription(), innerStuff );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public JSONObject toJSONObject() {
      JSONObject obj = super.toJSONObject();
      obj.put( "extra", innerStuff );
      return obj;
    }
  }
}
