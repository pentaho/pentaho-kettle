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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectRecipient;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectories;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjects;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

/**
 * 
 * @author tkafalas
 * 
 */
public class UIEERepositoryDirectoryTest {
  private static final String ID = "ID";
  private ObjectId mockObjectId;
  private RepositoryDirectory mockRepositoryDirectory;
  private UIRepositoryDirectory mockParent;
  private IAclService mockAclService;
  private Repository mockRepository;
  private PurRepository mockPurRepository;
  UIEERepositoryDirectory uiRepDir;
  UIEERepositoryDirectory uiPurRepDir;

  @Before
  public void beforeTest() throws Exception {
    mockObjectId = mock( ObjectId.class );
    when( mockObjectId.getId() ).thenReturn( ID );
    mockRepositoryDirectory = mock( RepositoryDirectory.class );
    when( mockRepositoryDirectory.getObjectId() ).thenReturn( mockObjectId );
    mockParent = mock( UIRepositoryDirectory.class );

    mockAclService = mock( IAclService.class );

    mockRepository = mock( Repository.class );
    when( mockRepository.hasService( IAclService.class ) ).thenReturn( true );
    when( mockRepository.getService( IAclService.class ) ).thenReturn( mockAclService );

    mockPurRepository = mock( PurRepository.class );
    when( mockPurRepository.hasService( IAclService.class ) ).thenReturn( true );
    when( mockPurRepository.getService( IAclService.class ) ).thenReturn( mockAclService );

    uiRepDir = new UIEERepositoryDirectory( mockRepositoryDirectory, mockParent, mockRepository );
    uiPurRepDir = new UIEERepositoryDirectory( mockRepositoryDirectory, mockParent, mockPurRepository );

  }

  @Test
  public void testAcls() throws Exception {
    final String owner = "owner";
    final String role = "role";
    ObjectRecipient mockObjectRecipient = mock( ObjectRecipient.class );
    when( mockObjectRecipient.getName() ).thenReturn( owner );
    ObjectAcl mockAcl = mock( ObjectAcl.class );
    when( mockAcl.getOwner() ).thenReturn( mockObjectRecipient );
    when( mockAclService.getAcl( mockObjectId, false ) ).thenReturn( mockAcl );

    uiRepDir.clearAcl();

    UIRepositoryObjectAcls acls = new UIRepositoryObjectAcls();
    uiRepDir.getAcls( acls );
    verify( mockAclService, times( 1 ) ).getAcl( mockObjectId, false );
    assertEquals( owner, acls.getOwner().getName() );

    acls = new UIRepositoryObjectAcls();
    uiRepDir.getAcls( acls, false );
    verify( mockAclService, times( 2 ) ).getAcl( mockObjectId, false );
    assertEquals( owner, acls.getOwner().getName() );

    acls = new UIRepositoryObjectAcls();
    RepositoryObjectAcl repObjectAcl = new RepositoryObjectAcl( new RepositoryObjectRecipient( role, Type.ROLE ) );
    acls.setObjectAcl( new RepositoryObjectAcl( new RepositoryObjectRecipient( role, Type.ROLE ) ) );

    uiRepDir.setAcls( acls );
    verify( mockAclService ).setAcl( mockObjectId, repObjectAcl );

    when( mockAclService.getAcl( mockObjectId, false ) ).thenThrow( new KettleException( "" ) );
    uiRepDir.clearAcl();
    try {
      uiRepDir.getAcls( acls );
      fail( "Expected an exception" );
    } catch ( AccessDeniedException e ) {
      // Test Succeeded if here
    }

    when( mockAclService.getAcl( mockObjectId, true ) ).thenThrow( new KettleException( "" ) );
    uiRepDir.clearAcl();
    try {
      uiRepDir.getAcls( acls, true );
      fail( "Expected an exception" );
    } catch ( AccessDeniedException e ) {
      // Test Succeeded if here
    }

    doThrow( new KettleException( "" ) ).when( mockAclService ).setAcl( any( ObjectId.class ), any( ObjectAcl.class ) );
    uiRepDir.clearAcl();
    try {
      uiRepDir.setAcls( acls );
      fail( "Expected an exception" );
    } catch ( AccessDeniedException e ) {
      // Test Succeeded if here
    }
  }

  @Test
  public void testDelete() throws Exception {
    UIRepositoryDirectories mockUIRepositoryDirectories = mock( UIRepositoryDirectories.class );
    when( mockUIRepositoryDirectories.contains( uiRepDir ) ).thenReturn( true );
    when( mockParent.getChildren() ).thenReturn( mockUIRepositoryDirectories );
    UIRepositoryObjects mockUIRepositoryObjects = mock( UIRepositoryObjects.class );
    when( mockUIRepositoryObjects.contains( uiRepDir ) ).thenReturn( true );
    when( mockParent.getRepositoryObjects() ).thenReturn( mockUIRepositoryObjects );

    uiRepDir.delete( false );
    verify( mockRepository ).deleteRepositoryDirectory( mockRepositoryDirectory );
    verify( mockUIRepositoryDirectories, times( 1 ) ).remove( uiRepDir );
    verify( mockUIRepositoryObjects, times( 1 ) ).remove( uiRepDir );
    verify( mockParent, times( 1 ) ).refresh();

    uiPurRepDir.delete( false );
    verify( mockPurRepository ).deleteRepositoryDirectory( mockRepositoryDirectory, false );
    verify( mockUIRepositoryDirectories, times( 2 ) ).remove( uiPurRepDir );
    verify( mockUIRepositoryObjects, times( 2 ) ).remove( uiPurRepDir );
    verify( mockParent, times( 2 ) ).refresh();
  }

  @Test
  public void testSetName() throws Exception {
    final String newDirName = "foo";
    when( mockRepositoryDirectory.getName() ).thenReturn( "dirName" );

    uiRepDir.setName( newDirName, true );
    verify( mockRepository ).renameRepositoryDirectory( mockRepositoryDirectory.getObjectId(), null, newDirName );

    uiPurRepDir.setName( newDirName, true );
    verify( mockPurRepository ).renameRepositoryDirectory( mockRepositoryDirectory.getObjectId(), null, newDirName,
        true );
  }

  @Test
  public void testAccess() throws Exception {
    when( mockAclService.hasAccess( mockObjectId, RepositoryFilePermission.READ ) ).thenReturn( true );
    when( mockAclService.hasAccess( mockObjectId, RepositoryFilePermission.WRITE ) ).thenReturn( false );

    assertTrue( uiPurRepDir.hasAccess( RepositoryFilePermission.READ ) );
    assertFalse( uiPurRepDir.hasAccess( RepositoryFilePermission.WRITE ) );
  }

  @Test
  public void testBadConstructor() throws Exception {
    when( mockRepository.hasService( IAclService.class ) ).thenReturn( false );
    try {
      new UIEERepositoryDirectory( mockRepositoryDirectory, mockParent, mockRepository );
      fail( "Expected an exception" );
    } catch ( IllegalStateException e ) {
      // Test Succeeded if here
    }
  }
}
