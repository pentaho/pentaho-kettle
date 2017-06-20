/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.repository.pur.model.ObjectAce;
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectAce;
import org.pentaho.di.repository.pur.model.RepositoryObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectRecipient;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

/**
 * 
 * @author tkafalas
 * 
 */
public class UIRepositoryObjectAclsTest {

  private final String RECIPIENT0 = "Looney Tunes";
  private final String RECIPIENT1 = "Elmer Fudd";
  private final String RECIPIENT2 = "Bug Bunny";
  private final String RECIPIENT3 = "Daffy Duck";
  UIRepositoryObjectAcls repositoryObjectAcls;
  RepositoryObjectAcl repObjectAcl;
  UIRepositoryObjectAcl objectAcl1;
  UIRepositoryObjectAcl objectAcl2;
  UIRepositoryObjectAcl objectAcl3;

  @Before
  public void beforeTest() {
    repositoryObjectAcls = new UIRepositoryObjectAcls();
    repObjectAcl = new RepositoryObjectAcl( new RepositoryObjectRecipient( RECIPIENT0, Type.ROLE ) );

    // The fact that the next line is needed to avoid an NPE on many of the methods, ( from doing an addAll(null) ),
    // might indicate a bug but I'm not comfortable fixing it at this time since something might depend
    // on UIRepositoryObjectAcls.getAcls() delivering null rather than an empty list.
    repositoryObjectAcls.setObjectAcl( repObjectAcl );

    objectAcl1 = new UIRepositoryObjectAcl( createObjectAce( RECIPIENT1 ) );
    objectAcl2 = new UIRepositoryObjectAcl( createObjectAce( RECIPIENT2 ) );
    objectAcl3 = new UIRepositoryObjectAcl( createObjectAce( RECIPIENT3 ) );
  }

  @Test
  public void testSetObjectAcl() {
    ObjectAcl objectAcl = repositoryObjectAcls.getObjectAcl();
    assertEquals( repObjectAcl, objectAcl );
  }

  @Test
  public void testSetAndGetAcls() {

    List<UIRepositoryObjectAcl> originalUIAcls = Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2 } );

    // Call the method being tested
    repositoryObjectAcls.setAcls( originalUIAcls );

    // Assert that the two acls are present
    assertListMatches( originalUIAcls, repositoryObjectAcls.getAcls() );

    assertEquals( objectAcl1, repositoryObjectAcls.getAcl( RECIPIENT1 ) );
    assertNull( repositoryObjectAcls.getAcl( "not there" ) );
  }

  @Test
  public void testAddAndRemoveAcls() {

    List<UIRepositoryObjectAcl> originalUIAcls =
        Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2, objectAcl3 } );

    // Call the method being tested
    repositoryObjectAcls.addAcls( originalUIAcls );

    // Assert that the two acls are present
    assertListMatches( originalUIAcls, repositoryObjectAcls.getAcls() );

    repositoryObjectAcls.removeAcls( Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl3 } ) );
    assertListMatches( Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl2 } ), repositoryObjectAcls.getAcls() );

    repositoryObjectAcls.addDefaultAcl( objectAcl1 );
    // The permissions in the acls (in the list also) will be set to READ only.
    assertEquals( EnumSet.of( RepositoryFilePermission.READ ), objectAcl1.getPermissionSet() );
    assertListMatches( Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2 } ), repositoryObjectAcls
        .getAcls() );

    repositoryObjectAcls.removeAcl( objectAcl1.getRecipientName() );
    repositoryObjectAcls.removeAcl( objectAcl2.getRecipientName() );
    assertEquals( 0, repositoryObjectAcls.getAcls().size() );

    repositoryObjectAcls.addDefaultAcls( originalUIAcls );
    // The permissions in the acls (in the list also) will be set to READ only.
    assertEquals( EnumSet.of( RepositoryFilePermission.READ ), objectAcl1.getPermissionSet() );
    assertListMatches( originalUIAcls, repositoryObjectAcls.getAcls() );

  }

  @Test
  public void testAddAndRemoveAcl() {

    List<UIRepositoryObjectAcl> originalUIAcls = Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2 } );

    // Call the method being tested
    repositoryObjectAcls.addAcl( objectAcl1 );
    repositoryObjectAcls.addAcl( objectAcl2 );

    // Assert that the two acls are present
    assertListMatches( originalUIAcls, repositoryObjectAcls.getAcls() );
    repositoryObjectAcls.removeAcl( RECIPIENT2 );
    assertListMatches( Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1 } ), repositoryObjectAcls.getAcls() );

    repositoryObjectAcls.removeAcl( RECIPIENT1 );
    assertEquals( 0, repositoryObjectAcls.getAcls().size() );
  }

  @Test
  public void testUpdateAcl() {

    List<UIRepositoryObjectAcl> originalUIAcls = Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2 } );
    repositoryObjectAcls.addAcls( originalUIAcls );

    objectAcl2.addPermission( RepositoryFilePermission.DELETE );

    repositoryObjectAcls.updateAcl( objectAcl2 );

    // Assert that the delete permissions is added
    for ( UIRepositoryObjectAcl uiAcl : repositoryObjectAcls.getAcls() ) {
      if ( objectAcl2.getRecipientName().equals( uiAcl.getRecipientName() ) ) {
        assertEquals( "Delete permission was not added", objectAcl2.getPermissionSet(), uiAcl.getPermissionSet() );
      }
    }
  }

  @Test
  public void testSetRemoveSelectedAcls() {

    List<UIRepositoryObjectAcl> originalUIAcls =
        Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2, objectAcl3 } );

    repositoryObjectAcls.addAcls( originalUIAcls );
    List<UIRepositoryObjectAcl> selectedAcls = Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl3 } );

    // Call the method being tested
    repositoryObjectAcls.setSelectedAclList( selectedAcls );
    assertListMatches( selectedAcls, repositoryObjectAcls.getSelectedAclList() );

    repositoryObjectAcls.removeSelectedAcls();
    assertListMatches( Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl2 } ), repositoryObjectAcls.getAcls() );

  }

  @Test
  public void testClear() {

    List<UIRepositoryObjectAcl> originalUIAcls =
        Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2, objectAcl3 } );

    repositoryObjectAcls.addAcls( originalUIAcls );
    repositoryObjectAcls.setRemoveEnabled( true );
    assertTrue( repositoryObjectAcls.isRemoveEnabled() );

    // Call the method being tested
    repositoryObjectAcls.clear();

    assertTrue( repositoryObjectAcls.getSelectedAclList().isEmpty() );
    assertFalse( repositoryObjectAcls.isRemoveEnabled() );
    assertFalse( repositoryObjectAcls.isModelDirty() );
    assertTrue( repositoryObjectAcls.isEntriesInheriting() );
  }

  @Test
  public void testBooleanFlags() {
    List<UIRepositoryObjectAcl> originalUIAcls =
        Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2, objectAcl3 } );

    UIRepositoryObjectAcls repositoryObjectAcls = new UIRepositoryObjectAcls();
    repositoryObjectAcls
        .setObjectAcl( new RepositoryObjectAcl( new RepositoryObjectRecipient( RECIPIENT1, Type.USER ) ) );

    assertFalse( repositoryObjectAcls.isModelDirty() );
    assertFalse( repositoryObjectAcls.isRemoveEnabled() );
    assertFalse( repositoryObjectAcls.hasManageAclAccess() );
    assertTrue( repositoryObjectAcls.isEntriesInheriting() );

    repositoryObjectAcls.addAcls( originalUIAcls );
    assertTrue( repositoryObjectAcls.isModelDirty() );
    assertTrue( repositoryObjectAcls.isEntriesInheriting() );

    assertFalse( repositoryObjectAcls.isRemoveEnabled() );
    repositoryObjectAcls.setRemoveEnabled( true );
    assertTrue( repositoryObjectAcls.isRemoveEnabled() );

    repositoryObjectAcls.setModelDirty( true );
    assertTrue( repositoryObjectAcls.isModelDirty() );
    assertTrue( repositoryObjectAcls.isRemoveEnabled() );
    assertTrue( repositoryObjectAcls.isEntriesInheriting() );

    repositoryObjectAcls.setModelDirty( false );
    assertFalse( repositoryObjectAcls.isModelDirty() );
    assertTrue( repositoryObjectAcls.isEntriesInheriting() );

    repositoryObjectAcls.setEntriesInheriting( true );
    assertFalse( repositoryObjectAcls.isModelDirty() );
    assertTrue( repositoryObjectAcls.isEntriesInheriting() );

    repositoryObjectAcls.setEntriesInheriting( false );
    assertTrue( repositoryObjectAcls.isModelDirty() );
    assertFalse( repositoryObjectAcls.isRemoveEnabled() );
    assertFalse( repositoryObjectAcls.isEntriesInheriting() );

    repositoryObjectAcls.setHasManageAclAccess( true );
    assertTrue( repositoryObjectAcls.hasManageAclAccess() );

  }

  @Test
  public void testGetOwner() {
    assertEquals( RECIPIENT0, repositoryObjectAcls.getOwner().getName() );
    repositoryObjectAcls = new UIRepositoryObjectAcls();
    assertNull( repositoryObjectAcls.getOwner() );
  }

  @Test
  public void testGetAceIndex() {

    List<UIRepositoryObjectAcl> originalUIAcls =
        Arrays.asList( new UIRepositoryObjectAcl[] { objectAcl1, objectAcl2, objectAcl3 } );

    repositoryObjectAcls.addAcls( originalUIAcls );
    int i = repositoryObjectAcls.getAceIndex( objectAcl2.getAce() );
    assertTrue( objectAcl2.equals( repositoryObjectAcls.getAcls().get( i ) ) );

  }

  /**
   * Assert that all items in list knownList exist in the checkedList
   * 
   * @param knownList
   * @param unknownList
   */
  private void assertListMatches( List<UIRepositoryObjectAcl> knownList, List<UIRepositoryObjectAcl> checkedList ) {
    assertEquals( "list sizes don't match", knownList.size(), checkedList.size() );
    boolean found;
    for ( UIRepositoryObjectAcl knownUIAcl : knownList ) {
      found = false;
      for ( UIRepositoryObjectAcl checkedAcl : checkedList ) {
        if ( knownUIAcl.equals( checkedAcl ) ) {
          found = true;
          break;
        }
      }
      assertTrue( "Did not find an Acl", found );
    }
  }

  private ObjectAce createObjectAce( String recipientName ) {
    ObjectRecipient objectRecipient = new RepositoryObjectRecipient( recipientName, ObjectRecipient.Type.USER );
    ObjectAce objectAce =
        new RepositoryObjectAce( objectRecipient, EnumSet.of( RepositoryFilePermission.READ,
            RepositoryFilePermission.WRITE ) );

    return objectAce;
  }

}
