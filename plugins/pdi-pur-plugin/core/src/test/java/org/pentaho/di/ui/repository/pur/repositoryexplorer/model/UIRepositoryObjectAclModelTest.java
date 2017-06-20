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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.repository.pur.model.ObjectAce;
import org.pentaho.di.repository.pur.model.RepositoryObjectAce;
import org.pentaho.di.repository.pur.model.RepositoryObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectRecipient;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

/**
 * 
 * @author tkafalas
 * 
 */
public class UIRepositoryObjectAclModelTest {

  private final String RECIPIENT0 = "Looney Tunes";
  private final String RECIPIENT1 = "Elmer Fudd";
  private final String RECIPIENT2 = "Bug Bunny";
  private final String RECIPIENT3 = "Daffy Duck";
  private final String ROLE1 = "role1";
  private final String ROLE2 = "role2";
  private final String ROLE3 = "role3";
  private final String USER1 = "user1";
  private final String USER2 = "user2";
  private final String USER3 = "user3";

  UIRepositoryObjectAcls repositoryObjectAcls;
  RepositoryObjectAcl repObjectAcl;
  UIRepositoryObjectAcl userAcl1;
  UIRepositoryObjectAcl userAcl2;
  UIRepositoryObjectAcl userAcl3;
  UIRepositoryObjectAcl roleAcl1;
  UIRepositoryObjectAcl roleAcl2;
  UIRepositoryObjectAcl roleAcl3;
  List<UIRepositoryObjectAcl> originalUIAcls;
  UIRepositoryObjectAclModel repositoryObjectAclModel;
  List<String> defaultUserNameList;
  List<String> defaultRoleNameList;

  @Before
  public void beforeTest() {
    repositoryObjectAcls = new UIRepositoryObjectAcls();
    repObjectAcl = new RepositoryObjectAcl( new RepositoryObjectRecipient( RECIPIENT0, Type.ROLE ) );
    repositoryObjectAcls
        .setObjectAcl( new RepositoryObjectAcl( new RepositoryObjectRecipient( RECIPIENT0, Type.ROLE ) ) );

    userAcl1 = new UIRepositoryObjectAcl( createUserAce( RECIPIENT1 ) );
    userAcl2 = new UIRepositoryObjectAcl( createUserAce( RECIPIENT2 ) );
    userAcl3 = new UIRepositoryObjectAcl( createUserAce( RECIPIENT3 ) );
    repositoryObjectAclModel = new UIRepositoryObjectAclModel( repositoryObjectAcls );

    roleAcl1 = new UIRepositoryObjectAcl( createRoleAce( ROLE1 ) );
    roleAcl2 = new UIRepositoryObjectAcl( createRoleAce( ROLE2 ) );
    roleAcl3 = new UIRepositoryObjectAcl( createRoleAce( ROLE3 ) );
    defaultUserNameList = Arrays.asList( new String[] { USER1, USER2, USER3 } );
    defaultRoleNameList = Arrays.asList( new String[] { ROLE1, ROLE2, ROLE3 } );
  }

  @Test
  public void testGetAcls() {
    List<UIRepositoryObjectAcl> originalUIAcls = Arrays.asList( new UIRepositoryObjectAcl[] { userAcl1, userAcl2 } );
    repositoryObjectAcls.addAcls( originalUIAcls );
    repositoryObjectAclModel.assignRole( ROLE3 );

    assertEquals( userAcl2, repositoryObjectAclModel.getAcl( RECIPIENT2 ) );
    assertEquals( ROLE3, repositoryObjectAclModel.getAcl( ROLE3 ).getRecipientName() );
    assertNull( repositoryObjectAclModel.getAcl( "foo" ) );
  }

  @Test
  public void testSelectedAvailableRoles() {
    List<String> selectedAvailableRoles = Arrays.asList( new String[] { ROLE1, ROLE2 } );
    repositoryObjectAclModel.setSelectedAvailableRoles( selectedAvailableRoles );
    repositoryObjectAclModel.setSelectedAvailableRole( ROLE3 );

    assertStringListMatches( defaultRoleNameList, repositoryObjectAclModel.getSelectedAvailableRoles() );
  }

  @Test
  public void testSelectedAvailableUsers() {
    List<String> selectedAvailableUsers = Arrays.asList( new String[] { USER1, USER2 } );
    repositoryObjectAclModel.setSelectedAvailableUsers( selectedAvailableUsers );
    repositoryObjectAclModel.setSelectedAvailableUser( USER3 );

    assertStringListMatches( defaultUserNameList, repositoryObjectAclModel.getSelectedAvailableUsers() );
  }

  @Test
  public void testSelectedAssignedRoles() {
    List<UIRepositoryObjectAcl> assignedRoles = Arrays.asList( new UIRepositoryObjectAcl[] { roleAcl1, roleAcl2 } );
    repositoryObjectAclModel.setSelectedAssignedRoles( assignedRoles );

    assertListMatches( assignedRoles, repositoryObjectAclModel.getSelectedAssignedRoles() );
  }

  @Test
  public void testSelectedAssignedUsers() {
    List<UIRepositoryObjectAcl> assignedUsers = Arrays.asList( new UIRepositoryObjectAcl[] { roleAcl1, roleAcl2 } );
    repositoryObjectAclModel.setSelectedAssignedUsers( assignedUsers );
    repositoryObjectAclModel.setSelectedAssignedUser( roleAcl3 );

    assertListMatches( Arrays.asList( new UIRepositoryObjectAcl[] { roleAcl1, roleAcl2, roleAcl3 } ),
        repositoryObjectAclModel.getSelectedAssignedUsers() );
  }

  @Test
  public void testAvailableUsers() {
    repositoryObjectAclModel.setAvailableUserList( defaultUserNameList );

    assertStringListMatches( defaultUserNameList, repositoryObjectAclModel.getAvailableUserList() );
    assertEquals( USER1, repositoryObjectAclModel.getAvailableUser( 0 ) );
    assertEquals( 1, repositoryObjectAclModel.getAvailableUserIndex( USER2 ) );
    assertEquals( -1, repositoryObjectAclModel.getAvailableUserIndex( "foo" ) );
  }

  @Test
  public void testAvailableRoles() {
    repositoryObjectAclModel.setAvailableRoleList( defaultRoleNameList );

    assertStringListMatches( defaultRoleNameList, repositoryObjectAclModel.getAvailableRoleList() );
    assertEquals( ROLE1, repositoryObjectAclModel.getAvailableRole( 0 ) );
    assertEquals( 1, repositoryObjectAclModel.getAvailableRoleIndex( ROLE2 ) );
    assertEquals( -1, repositoryObjectAclModel.getAvailableRoleIndex( "foo" ) );
  }

  @Test
  public void testSelectedUsersAndRoles() {
    // Set USER1/ROLE1 so that they are selected acls. SetAclsList will retain this selected status by
    // removing them from available lists.
    UIRepositoryObjectAcl selectedUserAcl = new UIRepositoryObjectAcl( createUserAce( USER1 ) );
    UIRepositoryObjectAcl selectedRoleAcl = new UIRepositoryObjectAcl( createRoleAce( ROLE1 ) );
    UIRepositoryObjectAcl unselectedAcl = new UIRepositoryObjectAcl( createUserAce( "FOO" ) );

    repositoryObjectAcls.addAcl( selectedUserAcl );
    repositoryObjectAcls.addAcl( selectedRoleAcl );

    repositoryObjectAclModel.setAclsList( defaultUserNameList, defaultRoleNameList );
    assertStringListMatches( Arrays.asList( new String[] { USER2, USER3 } ), repositoryObjectAclModel
        .getAvailableUserList() );
    assertStringListMatches( Arrays.asList( new String[] { ROLE2, ROLE3 } ), repositoryObjectAclModel
        .getAvailableRoleList() );
    assertEquals( repositoryObjectAclModel.getSelectedAvailableUsers().get( 0 ), defaultUserNameList.get( 0 ) );
    assertEquals( repositoryObjectAclModel.getSelectedAvailableRoles().get( 0 ), defaultRoleNameList.get( 0 ) );
    assertNameToAclListMatches( Arrays.asList( new String[] { USER1, ROLE1 } ), repositoryObjectAclModel
        .getSelectedAcls().getAcls() );

    assertEquals( selectedUserAcl, repositoryObjectAclModel.getSelectedUser( 0 ) );
    assertEquals( 0, repositoryObjectAclModel.getSelectedUserIndex( selectedUserAcl ) );
    assertEquals( -1, repositoryObjectAclModel.getSelectedUserIndex( unselectedAcl ) );

    assertEquals( selectedRoleAcl, repositoryObjectAclModel.getSelectedRole( 0 ) );
    assertEquals( 0, repositoryObjectAclModel.getSelectedRoleIndex( selectedRoleAcl ) );
    assertEquals( -1, repositoryObjectAclModel.getSelectedRoleIndex( unselectedAcl ) );
  }

  @Test
  public void testAssignRoles() {
    UIRepositoryObjectAcl selectedRoleAcl = new UIRepositoryObjectAcl( createRoleAce( ROLE1 ) );
    repositoryObjectAcls.addAcl( selectedRoleAcl );
    repositoryObjectAclModel.setAclsList( null, defaultRoleNameList );

    List<Object> objectRoleList = Arrays.asList( new Object[] { ROLE2 } );
    repositoryObjectAclModel.assignRoles( objectRoleList );
    assertStringListMatches( Arrays.asList( new String[] { ROLE3 } ), repositoryObjectAclModel.getAvailableRoleList() );
    assertNameToAclListMatches( Arrays.asList( new String[] { ROLE2 } ), repositoryObjectAclModel
        .getSelectedAssignedRoles() );
    assertNameToAclListMatches( Arrays.asList( new String[] { ROLE2 } ), repositoryObjectAclModel.getAclsToAdd() );

    repositoryObjectAclModel.updateSelectedAcls();
    assertNameToAclListMatches( Arrays.asList( new String[] { ROLE1, ROLE2 } ), repositoryObjectAclModel
        .getSelectedAcls().getAcls() );
    // For some reason, updateSelectedAcls does not clear aclsToAdd. After the update ROLE2 is still present in
    // the aclsToAdd list. This probably is not an issue because the interface reloads. For now, I will clear
    // manually now so I can exercise some unassign code.
    repositoryObjectAclModel.getAclsToAdd().clear();

    // Unassign the pending ROLE2 and the pre-assigned ROLE1
    UIRepositoryObjectAcl role2Acl = repositoryObjectAclModel.getSelectedRole( 1 );
    repositoryObjectAclModel.unassign( Arrays.asList( new Object[] { role2Acl, selectedRoleAcl } ) );

    assertEquals( 0, repositoryObjectAclModel.getSelectedAssignedRoles().size() );
    assertStringListMatches( defaultRoleNameList, repositoryObjectAclModel.getAvailableRoleList() );

    repositoryObjectAclModel.updateSelectedAcls();
    assertEquals( 0, repositoryObjectAclModel.getSelectedAcls().getAcls().size() );
  }

  @Test
  public void testAssignUsers() {
    UIRepositoryObjectAcl selectedUserAcl = new UIRepositoryObjectAcl( createUserAce( USER1 ) );
    repositoryObjectAcls.addAcl( selectedUserAcl );
    repositoryObjectAclModel.setAclsList( defaultUserNameList, null );

    List<Object> objectUserList = Arrays.asList( new Object[] { USER2 } );
    repositoryObjectAclModel.assignUsers( objectUserList );
    assertStringListMatches( Arrays.asList( new String[] { USER3 } ), repositoryObjectAclModel.getAvailableUserList() );
    assertNameToAclListMatches( Arrays.asList( new String[] { USER2 } ), repositoryObjectAclModel
        .getSelectedAssignedUsers() );
    assertNameToAclListMatches( Arrays.asList( new String[] { USER2 } ), repositoryObjectAclModel.getAclsToAdd() );

    repositoryObjectAclModel.updateSelectedAcls();
    assertNameToAclListMatches( Arrays.asList( new String[] { USER1, USER2 } ), repositoryObjectAclModel
        .getSelectedAcls().getAcls() );
    // For some reason, updateSelectedAcls does not clear aclsToAdd. After the update USER2 is still present in
    // the aclsToAdd list. This probably is not an issue because the interface reloads. For now, I will clear
    // manually now so I can exercise some unassign code.
    repositoryObjectAclModel.getAclsToAdd().clear();

    // Unassign the pending USER2 and the pre-assigned USER1
    UIRepositoryObjectAcl user2Acl = repositoryObjectAclModel.getSelectedUser( 1 );
    repositoryObjectAclModel.unassign( Arrays.asList( new Object[] { user2Acl, selectedUserAcl } ) );

    assertEquals( 0, repositoryObjectAclModel.getSelectedAssignedUsers().size() );
    assertStringListMatches( defaultUserNameList, repositoryObjectAclModel.getAvailableUserList() );

    repositoryObjectAclModel.updateSelectedAcls();
    assertEquals( 0, repositoryObjectAclModel.getSelectedAcls().getAcls().size() );
  }

  @Test
  public void testUserAssignmentFlags() {
    assertFalse( repositoryObjectAclModel.isUserAssignmentPossible() );
    repositoryObjectAclModel.setUserAssignmentPossible( true );
    assertTrue( repositoryObjectAclModel.isUserAssignmentPossible() );

    assertFalse( repositoryObjectAclModel.isUserUnassignmentPossible() );
    repositoryObjectAclModel.setUserUnassignmentPossible( true );
    assertTrue( repositoryObjectAclModel.isUserUnassignmentPossible() );

    assertFalse( repositoryObjectAclModel.isRoleAssignmentPossible() );
    repositoryObjectAclModel.setRoleAssignmentPossible( true );
    assertTrue( repositoryObjectAclModel.isRoleAssignmentPossible() );

    assertFalse( repositoryObjectAclModel.isRoleUnassignmentPossible() );
    repositoryObjectAclModel.setRoleUnassignmentPossible( true );
    assertTrue( repositoryObjectAclModel.isRoleUnassignmentPossible() );
  }

  @Test
  public void testClear() {
    repositoryObjectAcls.addAcl( new UIRepositoryObjectAcl( createUserAce( USER1 ) ) );
    repositoryObjectAcls.addAcl( new UIRepositoryObjectAcl( createRoleAce( ROLE1 ) ) );
    repositoryObjectAclModel.setAclsList( defaultUserNameList, defaultRoleNameList );
    repositoryObjectAclModel.assignRoles( Arrays.asList( new Object[] { ROLE2 } ) );
    repositoryObjectAclModel.assignUsers( Arrays.asList( new Object[] { USER2 } ) );

    repositoryObjectAclModel.clear();
    assertEquals( 0, repositoryObjectAclModel.getAvailableUserList().size() );
    assertEquals( 0, repositoryObjectAclModel.getAvailableRoleList().size() );
    assertEquals( 0, repositoryObjectAclModel.getSelectedAssignedUsers().size() );
    assertEquals( 0, repositoryObjectAclModel.getSelectedAssignedRoles().size() );
    assertEquals( 0, repositoryObjectAclModel.getSelectedAvailableUsers().size() );
    assertEquals( 0, repositoryObjectAclModel.getSelectedAvailableRoles().size() );
    // Selected List is unchanged.
    assertEquals( 1, repositoryObjectAclModel.getSelectedUserList().size() );
    assertEquals( 1, repositoryObjectAclModel.getSelectedRoleList().size() );
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
      assertTrue( "Did not find " + knownUIAcl.getRecipientName(), found );
    }
  }

  private void assertStringListMatches( List<String> knownList, List<String> checkedList ) {
    assertEquals( "list sizes don't match", knownList.size(), checkedList.size() );
    boolean found;
    for ( String known : knownList ) {
      found = false;
      for ( String checked : checkedList ) {
        if ( known.equals( checked ) ) {
          found = true;
          break;
        }
      }
      assertTrue( "Did not find " + known, found );
    }
  }

  private void assertNameToAclListMatches( List<String> knownList, List<UIRepositoryObjectAcl> checkedList ) {
    assertEquals( "list sizes don't match", knownList.size(), checkedList.size() );
    boolean found;
    for ( String knownName : knownList ) {
      found = false;
      for ( UIRepositoryObjectAcl checkedAcl : checkedList ) {
        if ( knownName.equals( checkedAcl.getRecipientName() ) ) {
          found = true;
          break;
        }
      }
      assertTrue( "Did not find " + knownName, found );
    }
  }

  private ObjectAce createUserAce( String recipientName ) {
    ObjectRecipient objectRecipient = new RepositoryObjectRecipient( recipientName, ObjectRecipient.Type.USER );
    ObjectAce objectAce =
        new RepositoryObjectAce( objectRecipient, EnumSet.of( RepositoryFilePermission.READ,
            RepositoryFilePermission.WRITE ) );
    return objectAce;
  }

  private ObjectAce createRoleAce( String recipientName ) {
    ObjectRecipient objectRecipient = new RepositoryObjectRecipient( recipientName, ObjectRecipient.Type.ROLE );
    ObjectAce objectAce =
        new RepositoryObjectAce( objectRecipient, EnumSet.of( RepositoryFilePermission.READ,
            RepositoryFilePermission.WRITE ) );
    return objectAce;
  }
}
