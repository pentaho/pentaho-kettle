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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import org.junit.Test;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.pur.model.ObjectAce;
import org.pentaho.di.repository.pur.model.RepositoryObjectAce;
import org.pentaho.di.repository.pur.model.RepositoryObjectRecipient;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

/**
 * 
 * @author tkafalas
 * 
 */
public class UIRepositoryObjectAclTest {
  private final String RECIPIENT1 = "Elmer Fudd";
  private final String RECIPIENT2 = "Bugs Bunny";

  @Test
  public void testGetPermissionSet() {
    UIRepositoryObjectAcl uiAcl = new UIRepositoryObjectAcl( createObjectAce() );

    EnumSet<RepositoryFilePermission> permissions = uiAcl.getPermissionSet();

    assertNotNull( permissions );
    assertEquals( 1, permissions.size() );
    assertTrue( permissions.contains( RepositoryFilePermission.ALL ) );
  }

  @Test
  public void testSetPermission() {
    UIRepositoryObjectAcl uiAcl = new UIRepositoryObjectAcl( createObjectAce() );

    uiAcl.setPermissionSet( RepositoryFilePermission.READ, RepositoryFilePermission.WRITE );

    EnumSet<RepositoryFilePermission> permissions = uiAcl.getPermissionSet();

    assertNotNull( permissions );
    assertEquals( 2, permissions.size() );
    assertTrue( permissions.contains( RepositoryFilePermission.READ ) );
    assertTrue( permissions.contains( RepositoryFilePermission.WRITE ) );

    uiAcl.setPermissionSet( EnumSet.of( RepositoryFilePermission.DELETE, RepositoryFilePermission.WRITE ) );
    permissions = uiAcl.getPermissionSet();

    assertNotNull( permissions );
    assertEquals( 2, permissions.size() );
    assertTrue( permissions.contains( RepositoryFilePermission.DELETE ) );
    assertTrue( permissions.contains( RepositoryFilePermission.WRITE ) );

    uiAcl.addPermission( RepositoryFilePermission.READ );
    permissions = uiAcl.getPermissionSet();
    assertEquals( permissions, EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.DELETE,
        RepositoryFilePermission.WRITE ) );

    uiAcl.removePermission( RepositoryFilePermission.READ );
    permissions = uiAcl.getPermissionSet();
    assertEquals( permissions, EnumSet.of( RepositoryFilePermission.DELETE, RepositoryFilePermission.WRITE ) );

  }

  @Test
  public void testEquals() {
    UIRepositoryObjectAcl uiAcl1 = new UIRepositoryObjectAcl( createObjectAce() );
    UIRepositoryObjectAcl uiAcl2 =
        new UIRepositoryObjectAcl( new RepositoryObjectAce( new RepositoryObjectRecipient( RECIPIENT1,
            ObjectRecipient.Type.USER ), EnumSet.of( RepositoryFilePermission.ALL ) ) );
    assertTrue( uiAcl1.equals( uiAcl2 ) );

    uiAcl2 =
        new UIRepositoryObjectAcl( new RepositoryObjectAce( new RepositoryObjectRecipient( RECIPIENT1,
            ObjectRecipient.Type.SYSTEM_ROLE ), EnumSet.of( RepositoryFilePermission.ALL ) ) );
    assertFalse( uiAcl1.equals( uiAcl2 ) );

    uiAcl2 =
        new UIRepositoryObjectAcl( new RepositoryObjectAce( new RepositoryObjectRecipient( RECIPIENT1,
            ObjectRecipient.Type.USER ), EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.ALL ) ) );
    assertFalse( uiAcl1.equals( uiAcl2 ) );

    uiAcl2 =
        new UIRepositoryObjectAcl( new RepositoryObjectAce( new RepositoryObjectRecipient( RECIPIENT2,
            ObjectRecipient.Type.USER ), EnumSet.of( RepositoryFilePermission.ALL ) ) );
    assertFalse( uiAcl1.equals( uiAcl2 ) );

    assertFalse( uiAcl1.equals( null ) );
  }

  @Test
  public void testRecipient() {
    UIRepositoryObjectAcl uiAcl = new UIRepositoryObjectAcl( createObjectAce() );
    assertEquals( RECIPIENT1, uiAcl.getRecipientName() );

    uiAcl.setRecipientName( RECIPIENT2 );
    assertEquals( RECIPIENT2, uiAcl.getRecipientName() );
  }

  @Test
  public void testRecipientType() {
    UIRepositoryObjectAcl uiAcl = new UIRepositoryObjectAcl( createObjectAce() );
    assertEquals( ObjectRecipient.Type.USER, uiAcl.getRecipientType() );

    uiAcl.setRecipientType( ObjectRecipient.Type.ROLE );
    assertEquals( ObjectRecipient.Type.ROLE, uiAcl.getRecipientType() );
  }

  @Test
  public void testToString() {
    UIRepositoryObjectAcl uiAcl = new UIRepositoryObjectAcl( createObjectAce() );
    String s = uiAcl.toString();
    assertNotNull( s );
    assertTrue( s.contains( RECIPIENT1 ) );
  }

  private ObjectAce createObjectAce() {
    ObjectRecipient objectRecipient = new RepositoryObjectRecipient( RECIPIENT1, ObjectRecipient.Type.USER );
    ObjectAce objectAce = new RepositoryObjectAce( objectRecipient, EnumSet.of( RepositoryFilePermission.ALL ) );

    return objectAce;
  }

}
