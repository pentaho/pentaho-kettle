/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.dragdrop;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.FileProvider;
import org.pentaho.di.plugins.fileopensave.controllers.FileController;
import org.pentaho.di.plugins.fileopensave.providers.ProviderService;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalDirectory;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentFile;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryDirectory;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryFile;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryObjectId;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSDirectory;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;
import org.pentaho.di.plugins.fileopensave.util.Util;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.ui.spoon.Spoon;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class ElementTest {
  private static final String slash = "\\" + java.io.File.separator;
  private static final String NAME = "filename";
  private static final EntityType TYPE = EntityType.LOCAL_FILE;
  private static final String PATH = "/tmp/" + NAME;
  private static final String PATH_PARENT = "/tmp";
  private static final String LOCAL_PROVIDER = "local";
  private static final String LOCAL_ROOT = "Local";
  private static final String RECENT_PROVIDER = "recents";
  private static final String RECENT_ROOT = "Recents";
  private static final String REPOSITORY_PROVIDER = RepositoryFileProvider.TYPE;
  private static final String REPOSITORY_NAME = RepositoryFileProvider.NAME;
  private static final String NAMED_CLUSTER_PROVIDER = "clusters";
  private static final String NAMED_CLUSTER_NAME = "Hadoop Clusters";
  private static final String DOMAIN = "domain";
  private static final String CONNECTION = "connection";
  private static final String VFS_PROVIDER = VFSFileProvider.TYPE;
  private static final String VFS_NAME = VFSFileProvider.NAME;
  private static final String DUMMY_STRING = "dummyString";
  private static final String OBJECT_ID = "objectid";
  private VariableSpace space = new Variables();

  Element element1;

  @Before
  public void setUp() throws Exception {
    element1 = new Element( NAME, TYPE, PATH, LOCAL_PROVIDER );
  }

  @Test
  public void testEquals() {
    Element element2 = new Element( NAME, TYPE, PATH, LOCAL_PROVIDER );
    assertEquals( element1, element2 );
    element2 = new Element( "diffname", TYPE, "/tmp/diffname", LOCAL_PROVIDER );
    assertNotEquals( element1, element2 );
    element2 = new Element( NAME, TYPE, PATH, "diffProvider" );
    assertNotEquals( element1, element2 );
    element2 = new Element( NAME, EntityType.REPOSITORY_FILE, PATH, LOCAL_PROVIDER );
    // Changing the file type does not effect equals because in a map, if the path and provider are the same then
    // the files would live in the same physical space.
    assertEquals( element1, element2 );
  }

  @Test
  public void getAndSetName() {
    assertEquals( TYPE, element1.getEntityType() );
    element1.setEntityType( EntityType.TEST_FILE );
    assertEquals( EntityType.TEST_FILE, element1.getEntityType() );
  }

  @Test
  public void getAndSetPath() {
    assertEquals( PATH, element1.getPath() );
    element1.setPath( DUMMY_STRING );
    assertEquals( DUMMY_STRING, element1.getPath() );
  }

  @Test
  public void getProvider() {
    assertEquals( LOCAL_PROVIDER, element1.getProvider() );
    Element element2 = new Element( NAME, TYPE, PATH, DUMMY_STRING );
    assertEquals( DUMMY_STRING, element2.getProvider() );
  }

  @Test
  public void getRepositoryName() {
    assertEquals( "", element1.getRepositoryName() );
    Element element2 = new Element( NAME, TYPE, PATH, LOCAL_PROVIDER, DUMMY_STRING );
    assertEquals( DUMMY_STRING, element2.getRepositoryName() );
  }

  @Test
  public void getAndSetDomain() {
    assertEquals( "", element1.getDomain() );
    element1.setDomain( DUMMY_STRING );
    assertEquals( DUMMY_STRING, element1.getDomain() );
  }

  @Test
  public void getConnection() {
    assertEquals( "", element1.getConnection() );
    Element element2 = new Element( NAME, TYPE, PATH, LOCAL_PROVIDER, DOMAIN, DUMMY_STRING );
    assertEquals( DUMMY_STRING, element2.getConnection() );
  }

  @Test
  public void testHashCode() {
    Element element2 = new Element( NAME, TYPE, PATH, LOCAL_PROVIDER );
    assertEquals( element1.hashCode(), element2.hashCode() );
    element2 = new Element( "diffname", TYPE, "/tmp/diffname", LOCAL_PROVIDER );
    assertNotEquals( element1.hashCode(), element2.hashCode() );
    element2 = new Element( NAME, TYPE, PATH, "diffProvider" );
    assertNotEquals( element1.hashCode(), element2.hashCode() );
    element2 = new Element( NAME, EntityType.REPOSITORY_FILE, PATH, LOCAL_PROVIDER );
    // Changing the file type does not effect equals because in a map, if the path and provider are the same then
    // the files would live in the same physical space.
    assertEquals( element1.hashCode(), element2.hashCode() );
  }

  @Test
  public void getAndSetEntityType() {
    assertEquals( TYPE, element1.getEntityType() );
    element1.setEntityType( EntityType.TREE );
    assertEquals( EntityType.TREE, element1.getEntityType() );
  }

  @Test
  public void testToString() {
    assertEquals( TYPE.name() + "   path: " + PATH + "  name: " + NAME, element1.toString() );
  }

  @Test
  public void testLocalFileConversion() {
    //Local file
    Element element = new Element( NAME, TYPE, adjustSlashes( PATH ), LOCAL_PROVIDER );
    File file = element.convertToFile( space );
    assertTrue( file instanceof LocalFile );
    assertEquals( TYPE, file.getEntityType() );
    assertEquals( NAME, file.getName() );
    assertEquals( adjustSlashes( PATH ), file.getPath() );
    assertEquals( adjustSlashes( PATH_PARENT ), file.getParent() );
    assertEquals( LOCAL_ROOT, file.getRoot() );
    assertEquals( element, new Element( file ) );
    assertEquals( EntityType.LOCAL_DIRECTORY, element.calcParentEntityType() );
  }

  @Test
  public void testLocalDirectoryConversion() {
    //Local Directory
    Element element = new Element( NAME, EntityType.LOCAL_DIRECTORY, adjustSlashes( PATH ), LOCAL_PROVIDER );
    File file = element.convertToFile( space );
    assertTrue( file instanceof LocalDirectory );
    assertEquals( EntityType.LOCAL_DIRECTORY, file.getEntityType() );
    assertEquals( NAME, file.getName() );
    assertEquals( adjustSlashes( PATH ), file.getPath() );
    assertEquals( adjustSlashes( PATH_PARENT ), file.getParent() );
    assertEquals( LOCAL_ROOT, file.getRoot() );
    assertEquals( element, new Element( file ) );
    assertEquals( EntityType.LOCAL_DIRECTORY, element.calcParentEntityType() );
  }

  @Test
  public void testRecentFileConversion() {
    //Recent file - LocalFile
    Element element = new Element( NAME, EntityType.RECENT_FILE, adjustSlashes( PATH ), RECENT_PROVIDER );
    checkRecentFileConversion( element, adjustSlashes( PATH ), false );
    assertEquals( EntityType.LOCAL_FILE, element.convertRecent().getEntityType() );

    //Recent file - RepositoryFile
    //Repository Files are still stored as RepositoryFiles in the recents instead of a RecentFile like other connections
    element = new Element( NAME, EntityType.REPOSITORY_FILE, PATH, RECENT_PROVIDER, REPOSITORY_NAME );
    File file = element.convertToFile( space );
    checkRecentFileConversion( element, PATH, true );
    //Repository recents actually come in

    //Recent file - VFSFile
    String path = "pvfs://" + CONNECTION + PATH;
    element = new Element( NAME, EntityType.RECENT_FILE, path, RECENT_PROVIDER, DOMAIN, CONNECTION );
    checkRecentFileConversion( element, path, false );
    assertEquals( EntityType.VFS_FILE, element.convertRecent().getEntityType() );

    //Recent file - NamedClusterFile
    path = "hc://mycluster/tmp/filename";
    element = new Element( NAME, EntityType.RECENT_FILE, path, RECENT_PROVIDER );
    checkRecentFileConversion( element, path, false );
    assertEquals( EntityType.NAMED_CLUSTER_FILE, element.convertRecent().getEntityType() );
  }

  @Test
  public void testRepositoryDirectoryConversion() throws Exception {
    //Repository Directory
    setupRepositoryMocks();
    Element element =
      new Element( NAME, EntityType.REPOSITORY_DIRECTORY, PATH, REPOSITORY_PROVIDER, null );
    File file = element.convertToFile( space );
    assertTrue( file instanceof RepositoryDirectory );
    assertEquals( EntityType.REPOSITORY_DIRECTORY, file.getEntityType() );
    assertEquals( NAME, file.getName() );
    assertEquals( PATH, file.getPath() );
    assertEquals( PATH_PARENT, file.getParent() );
    assertEquals( REPOSITORY_NAME, file.getRoot() );
    assertEquals( element, new Element( file ) );
    assertEquals( EntityType.REPOSITORY_DIRECTORY, element.calcParentEntityType() );
  }

  @Test
  public void testRepositoryFileConversion() throws Exception {
    //Repository File
    setupRepositoryMocks();
    Element element =
      new Element( NAME, EntityType.REPOSITORY_FILE, PATH, REPOSITORY_PROVIDER, null );
    File file = element.convertToFile( space );
    assertTrue( file instanceof RepositoryFile );
    assertEquals( EntityType.REPOSITORY_FILE, file.getEntityType() );
    assertEquals( NAME, file.getName() );
    assertEquals( PATH, file.getPath() );
    assertEquals( PATH_PARENT, file.getParent() );
    assertEquals( element, new Element( file ) );
    assertEquals( EntityType.REPOSITORY_DIRECTORY, element.calcParentEntityType() );
  }

  @Test
  public void testVfsDirectoryConversion() throws Exception {
    //VFS Directory
    String path = "pvfs://" + CONNECTION + PATH;
    Element element =
      new Element( NAME, EntityType.VFS_DIRECTORY, path, VFS_PROVIDER, DOMAIN, CONNECTION );
    File file = element.convertToFile( space );
    assertTrue( file instanceof VFSDirectory );
    assertEquals( EntityType.VFS_DIRECTORY, file.getEntityType() );
    assertEquals( NAME, file.getName() );
    assertEquals( path, file.getPath() );
    assertEquals( Util.getFolder( path ), file.getParent() );
    assertEquals( VFS_NAME, file.getRoot() );
    assertEquals( element, new Element( file ) );
    assertEquals( EntityType.VFS_DIRECTORY, element.calcParentEntityType() );
  }

  @Test
  public void testVfsFileConversion() throws Exception {
    //VFS Directory
    String path = "pvfs://" + CONNECTION + PATH;
    Element element =
      new Element( NAME, EntityType.VFS_FILE, path, VFS_PROVIDER, DOMAIN, CONNECTION );
    File file = element.convertToFile( space );
    assertTrue( file instanceof VFSFile );
    assertEquals( EntityType.VFS_FILE, file.getEntityType() );
    assertEquals( NAME, file.getName() );
    assertEquals( path, file.getPath() );
    assertEquals( Util.getFolder( path ), file.getParent() );
    assertEquals( VFS_NAME, file.getRoot() );
    assertEquals( element, new Element( file ) );
    assertEquals( EntityType.VFS_DIRECTORY, element.calcParentEntityType() );
  }

  @Test
  public void testNamedClusterDirectoryConversion() throws Exception {
    //NamedCluster Directory
    String path = "hc://myCluster" + CONNECTION;
    setupNamedClusterMocks( path, EntityType.NAMED_CLUSTER_DIRECTORY );
    Element element =
      new Element( NAME, EntityType.NAMED_CLUSTER_DIRECTORY, path, NAMED_CLUSTER_PROVIDER, DOMAIN, CONNECTION );
    File file = element.convertToFile( space );
    assertEquals( EntityType.NAMED_CLUSTER_DIRECTORY, file.getEntityType() );
    assertEquals( NAME, file.getName() );
    assertEquals( path, file.getPath() );
    assertEquals( Util.getFolder( path ), file.getParent() );
    assertEquals( NAMED_CLUSTER_NAME, file.getRoot() );
    assertEquals( element, new Element( file ) );
    assertEquals( EntityType.NAMED_CLUSTER_DIRECTORY, element.calcParentEntityType() );
  }

  @Test
  public void testNamedClusterFileConversion() throws Exception {
    //NamedCluster Directory
    String path = "hc://myCluster" + CONNECTION;
    setupNamedClusterMocks( path, EntityType.NAMED_CLUSTER_FILE );
    Element element =
      new Element( NAME, EntityType.NAMED_CLUSTER_FILE, path, NAMED_CLUSTER_PROVIDER, DOMAIN, CONNECTION );
    File file = element.convertToFile( space );
    assertEquals( EntityType.NAMED_CLUSTER_FILE, file.getEntityType() );
    assertEquals( NAME, file.getName() );
    assertEquals( path, file.getPath() );
    assertEquals( Util.getFolder( path ), file.getParent() );
    assertEquals( NAMED_CLUSTER_NAME, file.getRoot() );
    assertEquals( element, new Element( file ) );
    assertEquals( EntityType.NAMED_CLUSTER_DIRECTORY, element.calcParentEntityType() );
  }

  @Test
  public void convertToFileObject() throws Exception {
    Element element = new Element( NAME, TYPE, adjustSlashes( PATH ), LOCAL_PROVIDER );
    FileObject fileObject = element.convertToFileObject( space );
    assertEquals( NAME, fileObject.getName().getBaseName() );
    assertEquals( adjustSlashes( PATH ), fileObject.getPath().toString() );
  }

  private void setupNamedClusterMocks( String path, EntityType entityType ) {
    //We need to get the FileController out of the Element object and use it to get the ProviderService. Both of these
    // instance variables are private so we will get them using TestUtils.  Then we can
    //add a mocked service for NamedClusters since the none of these classes is visible by this project. The mocked
    //ProviderService can then deliver a mocked File object as if it came from the NamedClusterProvider.
    FileController fileController = (FileController) TestUtils.reflectValue( element1, "FILE_CONTROLLER" );
    ProviderService providerService = (ProviderService) TestUtils.reflectValue( fileController, "providerService" );

    File mockedNCDirectory = mock( File.class );
    when( mockedNCDirectory.getName() ).thenReturn( NAME );
    when( mockedNCDirectory.getPath() ).thenReturn( path );
    when( mockedNCDirectory.getParent() ).thenReturn( Util.getFolder( path ) );
    when( mockedNCDirectory.getRoot() ).thenReturn( NAMED_CLUSTER_NAME );
    when( mockedNCDirectory.getProvider() ).thenReturn( NAMED_CLUSTER_PROVIDER );
    when( mockedNCDirectory.getEntityType() ).thenReturn( entityType );

    FileProvider mockNCProvider = mock( FileProvider.class );
    when( mockNCProvider.getType() ).thenReturn( "clusters" );
    when( mockNCProvider.isAvailable() ).thenReturn( true );
    when( mockNCProvider.getFile( path, entityType.isDirectory() ) ).thenReturn( mockedNCDirectory );

    providerService.add( mockNCProvider );
  }

  private void setupRepositoryMocks() throws Exception {
    Spoon spoonInstance = mock( Spoon.class );
    Element.spoonInstance = spoonInstance;

    RepositoryDirectoryInterface rdiPath = mock( RepositoryDirectoryInterface.class );
    when( rdiPath.getName() ).thenReturn( NAME );
    when( rdiPath.getObjectId() ).thenReturn( new RepositoryObjectId( OBJECT_ID ) );
    when( rdiPath.isVisible() ).thenReturn( true );
    Repository rep = mock( Repository.class );
    TestUtils.reflectSetValue( spoonInstance, "rep", rep ); //inject the instance variable in the mock
    when( rep.findDirectory( PATH ) ).thenReturn( rdiPath );
  }

  private String adjustSlashes( String target ) {
    target = target.replaceAll( "/", slash );
    if ( target.startsWith( "\\" ) ) {
      target = "c:" + target;
    }
    return target;
  }

  private void checkRecentFileConversion( Element element, String path, boolean isRepository ) {
    VariableSpace space = new Variables();
    File file = element.convertToFile( space );
    if ( isRepository ) {
      assertTrue( file instanceof RepositoryFile );
      assertEquals( EntityType.REPOSITORY_FILE, file.getEntityType() );
    } else {
      assertTrue( file instanceof RecentFile );
      assertEquals( EntityType.RECENT_FILE, file.getEntityType() );
    }
    assertEquals( NAME, file.getName() );
    assertEquals( path, file.getPath() );
    assertEquals( Util.getFolder( path ), file.getParent() );
    assertEquals( RECENT_ROOT, file.getRoot() );
    if ( !isRepository ) {
      // Cannot check equality on repo items because the repo name will be "recents" on the original element but
      // "repository" when creating a File object.  This inconsistancy is normal behavior and requires structural changes
      // to fix.
      assertEquals( element, new Element( file ) );
    }
  }
}