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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.repository.pur.model.EERepositoryObject;
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryLock;
import org.pentaho.di.repository.pur.model.RepositoryObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectRecipient;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.repository.pur.services.IRevisionService;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

/**
 * 
 * @author tkafalas
 * 
 */
public class UIEEJobTest {
  private final static String LOCK_MESSAGE = "lockMessage";
  private final static String LOCK_NOTE = "lockNote";
  private final static String JOB_ID = "jobId";

  private ObjectId mockObjectId;
  private UIEEJob uiJob;
  private EERepositoryObject mockEERepositoryObject;
  private ILockService mockLockService;
  private RepositoryLock mockRepositoryLock;
  private UIRepositoryDirectory mockParent;
  private IRevisionService mockRevisionService;
  private IAclService mockAclService;
  private Repository mockRepository;

  @Before
  public void beforeTest() throws Exception {

    mockObjectId = mock( ObjectId.class );
    when( mockObjectId.getId() ).thenReturn( JOB_ID );
    mockEERepositoryObject = mock( EERepositoryObject.class );
    when( mockEERepositoryObject.getObjectId() ).thenReturn( mockObjectId );
    mockParent = mock( UIRepositoryDirectory.class );

    mockLockService = mock( ILockService.class );
    mockRevisionService = mock( IRevisionService.class );
    mockAclService = mock( IAclService.class );

    mockRepository = mock( Repository.class );
    when( mockRepository.hasService( ILockService.class ) ).thenReturn( true );
    when( mockRepository.getService( ILockService.class ) ).thenReturn( mockLockService );
    when( mockRepository.hasService( IRevisionService.class ) ).thenReturn( true );
    when( mockRepository.getService( IRevisionService.class ) ).thenReturn( mockRevisionService );
    when( mockRepository.hasService( IAclService.class ) ).thenReturn( true );
    when( mockRepository.getService( IAclService.class ) ).thenReturn( mockAclService );

    uiJob = new UIEEJob( mockEERepositoryObject, mockParent, mockRepository );

    mockRepositoryLock = mock( RepositoryLock.class );
  }

  @Test
  public void testGetImage() {
    String image = uiJob.getImage();
    assertNotNull( image );
    File f = new File( image );

    when( mockEERepositoryObject.getLock() ).thenReturn( mockRepositoryLock );

    String image2 = uiJob.getImage();
    assertNotNull( image2 );
    f = new File( image2 );
    assertNotEquals( image, image2 );
  }

  @Test
  public void testGetLockMessage() throws Exception {
    when( mockEERepositoryObject.getLockMessage() ).thenReturn( LOCK_MESSAGE );
    assertEquals( LOCK_MESSAGE, uiJob.getLockMessage() );
  }

  @Test
  public void testLock() throws Exception {
    when( mockLockService.lockJob( mockObjectId, LOCK_NOTE ) ).thenReturn( mockRepositoryLock );
    uiJob.lock( LOCK_NOTE );

    verify( mockEERepositoryObject ).setLock( mockRepositoryLock );
    verify( mockParent ).fireCollectionChanged();

    uiJob.unlock();
    verify( mockEERepositoryObject ).setLock( null );
    verify( mockParent, times( 2 ) ).fireCollectionChanged();
  }

  @Test
  public void testRevisions() throws Exception {
    final String revisionName = "revisionName";
    final String commitMessage = "commitMessage";

    ObjectRevision mockObjectRevision = mock( ObjectRevision.class );
    when( mockObjectRevision.getName() ).thenReturn( revisionName );
    List<ObjectRevision> mockRevisions = Arrays.asList( new ObjectRevision[] { mockObjectRevision } );
    when( mockRevisionService.getRevisions( any( ObjectId.class ) ) ).thenReturn( mockRevisions );

    uiJob.refreshRevisions();
    verify( mockRevisionService, times( 1 ) ).getRevisions( mockObjectId );
    UIRepositoryObjectRevisions revisions = uiJob.getRevisions();

    assertEquals( 1, revisions.size() );
    assertEquals( "revisionName", revisions.get( 0 ).getName() );
    verify( mockRevisionService, times( 1 ) ).getRevisions( mockObjectId );

    uiJob.restoreRevision( revisions.get( 0 ), commitMessage );

    verify( mockRevisionService ).restoreJob( mockObjectId, revisionName, commitMessage );
    verify( mockParent, times( 1 ) ).fireCollectionChanged();
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

    uiJob.clearAcl();

    UIRepositoryObjectAcls acls = new UIRepositoryObjectAcls();
    uiJob.getAcls( acls );

    verify( mockAclService ).getAcl( mockObjectId, false );
    assertEquals( owner, acls.getOwner().getName() );

    acls = new UIRepositoryObjectAcls();
    RepositoryObjectAcl repObjectAcl = new RepositoryObjectAcl( new RepositoryObjectRecipient( role, Type.ROLE ) );
    acls.setObjectAcl( new RepositoryObjectAcl( new RepositoryObjectRecipient( role, Type.ROLE ) ) );

    uiJob.setAcls( acls );
    verify( mockAclService ).setAcl( mockObjectId, repObjectAcl );

    when( mockAclService.getAcl( mockObjectId, false ) ).thenThrow( new KettleException( "" ) );
    uiJob.clearAcl();
    try {
      uiJob.getAcls( acls );
      fail( "Expected an exception" );
    } catch ( AccessDeniedException e ) {
      // Test Succeeded if here
    }

    doThrow( new KettleException( "" ) ).when( mockAclService ).setAcl( any( ObjectId.class ),
        any( RepositoryObjectAcl.class ) );
    try {
      uiJob.setAcls( acls );
      fail( "Expected an exception" );
    } catch ( AccessDeniedException e ) {
      // Test Succeeded if here
    }
  }

  @Test
  public void testAccess() throws Exception {
    when( mockAclService.hasAccess( mockObjectId, RepositoryFilePermission.READ ) ).thenReturn( true );
    when( mockAclService.hasAccess( mockObjectId, RepositoryFilePermission.WRITE ) ).thenReturn( false );

    assertTrue( uiJob.hasAccess( RepositoryFilePermission.READ ) );
    assertFalse( uiJob.hasAccess( RepositoryFilePermission.WRITE ) );
  }

  @Test
  public void testRename() throws Exception {
    final String newName = "newName";
    RepositoryDirectory repDir = mock( RepositoryDirectory.class );

    uiJob.renameJob( mockObjectId, repDir, newName );
    verify( mockRevisionService, times( 1 ) ).getRevisions( mockObjectId );
  }

  @Test
  public void testVersionFlags() throws Exception {
    assertFalse( uiJob.getVersioningEnabled() );
    when( mockEERepositoryObject.getVersioningEnabled() ).thenReturn( true );
    assertTrue( uiJob.getVersioningEnabled() );

    assertFalse( uiJob.getVersionCommentEnabled() );
    when( mockEERepositoryObject.getVersionCommentEnabled() ).thenReturn( true );
    assertTrue( uiJob.getVersionCommentEnabled() );
  }

  @Test( expected = IllegalStateException.class )
  public void testIllegalStateOnAclService() throws Exception {
    when( mockRepository.hasService( IAclService.class ) ).thenReturn( false );
    uiJob = new UIEEJob( mockEERepositoryObject, mockParent, mockRepository );
  }

  @Test( expected = IllegalStateException.class )
  public void testIllegalStateOnLockService() throws Exception {
    when( mockRepository.hasService( ILockService.class ) ).thenReturn( false );
    uiJob = new UIEEJob( mockEERepositoryObject, mockParent, mockRepository );
  }

  @Test( expected = IllegalStateException.class )
  public void testIllegalStateOnRevisionService() throws Exception {
    when( mockRepository.hasService( IRevisionService.class ) ).thenReturn( false );
    uiJob = new UIEEJob( mockEERepositoryObject, mockParent, mockRepository );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testIllegalArgumentOnConstructor() throws Exception {
    RepositoryElementMetaInterface badObject = mock( RepositoryElementMetaInterface.class );
    uiJob = new UIEEJob( badObject, mockParent, mockRepository );
  }

}
