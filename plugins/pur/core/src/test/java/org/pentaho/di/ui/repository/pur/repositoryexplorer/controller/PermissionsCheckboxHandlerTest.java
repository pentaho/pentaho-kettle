/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.controller;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.ui.xul.components.XulCheckbox;

public class PermissionsCheckboxHandlerTest {
  private XulCheckbox readCheckbox;

  private XulCheckbox writeCheckbox;

  private XulCheckbox deleteCheckbox;

  private XulCheckbox manageCheckbox;

  private PermissionsCheckboxHandler permissionsCheckboxHandler;

  @Before
  public void setup() {
    readCheckbox = mock( XulCheckbox.class );
    writeCheckbox = mock( XulCheckbox.class );
    deleteCheckbox = mock( XulCheckbox.class );
    manageCheckbox = mock( XulCheckbox.class );
    permissionsCheckboxHandler =
        new PermissionsCheckboxHandler( readCheckbox, writeCheckbox, deleteCheckbox, manageCheckbox );
  }

  @Test
  public void testSetAllUncheckedUnchecksAll() {
    boolean checked = false;
    permissionsCheckboxHandler.setAllChecked( checked );
    verify( readCheckbox, times( 1 ) ).setChecked( checked );
    verify( writeCheckbox, times( 1 ) ).setChecked( checked );
    verify( deleteCheckbox, times( 1 ) ).setChecked( checked );
    verify( manageCheckbox, times( 1 ) ).setChecked( checked );
  }

  @Test
  public void testSetAllCheckedChecksAll() {
    boolean checked = true;
    permissionsCheckboxHandler.setAllChecked( checked );
    verify( readCheckbox, times( 1 ) ).setChecked( checked );
    verify( writeCheckbox, times( 1 ) ).setChecked( checked );
    verify( deleteCheckbox, times( 1 ) ).setChecked( checked );
    verify( manageCheckbox, times( 1 ) ).setChecked( checked );
  }

  @Test
  public void testSetAllDisabledDisablesAll() {
    boolean disabled = true;
    permissionsCheckboxHandler.setAllDisabled( disabled );
    verify( readCheckbox, times( 1 ) ).setDisabled( disabled );
    verify( writeCheckbox, times( 1 ) ).setDisabled( disabled );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( disabled );
    verify( manageCheckbox, times( 1 ) ).setDisabled( disabled );
  }

  @Test
  public void testSetAllEnabledEnablesAll() {
    boolean disabled = false;
    permissionsCheckboxHandler.setAllDisabled( disabled );
    verify( readCheckbox, times( 1 ) ).setDisabled( disabled );
    verify( writeCheckbox, times( 1 ) ).setDisabled( disabled );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( disabled );
    verify( manageCheckbox, times( 1 ) ).setDisabled( disabled );
  }

  @Test
  public void testProcessCheckboxesNoneCheckedEnableAppropriateTrue() {
    when( readCheckbox.isChecked() ).thenReturn( false );
    when( writeCheckbox.isChecked() ).thenReturn( false );
    when( deleteCheckbox.isChecked() ).thenReturn( false );
    when( manageCheckbox.isChecked() ).thenReturn( false );
    assertEquals( EnumSet.noneOf( RepositoryFilePermission.class ), permissionsCheckboxHandler.processCheckboxes( true ) );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
    verify( readCheckbox, times( 1 ) ).setDisabled( false );
  }

  @Test
  public void testProcessCheckboxesNoneCheckedEnableAppropriateFalse() {
    when( readCheckbox.isChecked() ).thenReturn( false );
    when( writeCheckbox.isChecked() ).thenReturn( false );
    when( deleteCheckbox.isChecked() ).thenReturn( false );
    when( manageCheckbox.isChecked() ).thenReturn( false );
    assertEquals( EnumSet.noneOf( RepositoryFilePermission.class ), permissionsCheckboxHandler.processCheckboxes() );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
    verify( readCheckbox, never() ).setDisabled( false );
  }

  @Test
  public void testProcessCheckboxesReadCheckedEnableAppropriateTrue() {
    when( readCheckbox.isChecked() ).thenReturn( true );
    when( writeCheckbox.isChecked() ).thenReturn( false );
    when( deleteCheckbox.isChecked() ).thenReturn( false );
    when( manageCheckbox.isChecked() ).thenReturn( false );
    assertEquals( EnumSet.of( RepositoryFilePermission.READ ), permissionsCheckboxHandler.processCheckboxes( true ) );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( false );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testProcessCheckboxesReadCheckedEnableAppropriateFalse() {
    when( readCheckbox.isChecked() ).thenReturn( true );
    when( writeCheckbox.isChecked() ).thenReturn( false );
    when( deleteCheckbox.isChecked() ).thenReturn( false );
    when( manageCheckbox.isChecked() ).thenReturn( false );
    assertEquals( EnumSet.of( RepositoryFilePermission.READ ), permissionsCheckboxHandler.processCheckboxes() );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testProcessCheckboxesWriteCheckedEnableAppropriateTrue() {
    when( readCheckbox.isChecked() ).thenReturn( false );
    when( writeCheckbox.isChecked() ).thenReturn( true );
    when( deleteCheckbox.isChecked() ).thenReturn( false );
    when( manageCheckbox.isChecked() ).thenReturn( false );
    assertEquals( EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.WRITE ),
        permissionsCheckboxHandler.processCheckboxes( true ) );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( false );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( false );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testProcessCheckboxesWriteCheckedEnableAppropriateFalse() {
    when( readCheckbox.isChecked() ).thenReturn( false );
    when( writeCheckbox.isChecked() ).thenReturn( true );
    when( deleteCheckbox.isChecked() ).thenReturn( false );
    when( manageCheckbox.isChecked() ).thenReturn( false );
    assertEquals( EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.WRITE ),
        permissionsCheckboxHandler.processCheckboxes() );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testProcessCheckboxesDeleteCheckedEnableAppropriateTrue() {
    when( readCheckbox.isChecked() ).thenReturn( false );
    when( writeCheckbox.isChecked() ).thenReturn( false );
    when( deleteCheckbox.isChecked() ).thenReturn( true );
    when( manageCheckbox.isChecked() ).thenReturn( false );
    assertEquals( EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.WRITE,
        RepositoryFilePermission.DELETE ), permissionsCheckboxHandler.processCheckboxes( true ) );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( false );
    verify( manageCheckbox, times( 1 ) ).setDisabled( false );
  }

  @Test
  public void testProcessCheckboxesDeleteCheckedEnableAppropriateFalse() {
    when( readCheckbox.isChecked() ).thenReturn( false );
    when( writeCheckbox.isChecked() ).thenReturn( false );
    when( deleteCheckbox.isChecked() ).thenReturn( true );
    when( manageCheckbox.isChecked() ).thenReturn( false );
    assertEquals( EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.WRITE,
        RepositoryFilePermission.DELETE ), permissionsCheckboxHandler.processCheckboxes() );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testProcessCheckboxesManageCheckedEnableAppropriateTrue() {
    when( readCheckbox.isChecked() ).thenReturn( false );
    when( writeCheckbox.isChecked() ).thenReturn( false );
    when( deleteCheckbox.isChecked() ).thenReturn( false );
    when( manageCheckbox.isChecked() ).thenReturn( true );
    assertEquals( EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.WRITE,
        RepositoryFilePermission.DELETE, RepositoryFilePermission.ACL_MANAGEMENT ), permissionsCheckboxHandler
        .processCheckboxes( true ) );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( false );
  }

  @Test
  public void testProcessCheckboxesManageCheckedEnableAppropriateFalse() {
    when( readCheckbox.isChecked() ).thenReturn( false );
    when( writeCheckbox.isChecked() ).thenReturn( false );
    when( deleteCheckbox.isChecked() ).thenReturn( false );
    when( manageCheckbox.isChecked() ).thenReturn( true );
    assertEquals( EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.WRITE,
        RepositoryFilePermission.DELETE, RepositoryFilePermission.ACL_MANAGEMENT ), permissionsCheckboxHandler
        .processCheckboxes() );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testUpdateCheckboxesNoPermissionsAppropriateTrue() {
    permissionsCheckboxHandler.updateCheckboxes( true, EnumSet.noneOf( RepositoryFilePermission.class ) );
    verify( readCheckbox, times( 1 ) ).setChecked( false );
    verify( writeCheckbox, times( 1 ) ).setChecked( false );
    verify( deleteCheckbox, times( 1 ) ).setChecked( false );
    verify( manageCheckbox, times( 1 ) ).setChecked( false );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
    verify( readCheckbox, times( 1 ) ).setDisabled( false );
  }

  @Test
  public void testUpdateCheckboxesNoPermissionsAppropriateFalse() {
    permissionsCheckboxHandler.updateCheckboxes( false, EnumSet.noneOf( RepositoryFilePermission.class ) );
    verify( readCheckbox, times( 1 ) ).setChecked( false );
    verify( writeCheckbox, times( 1 ) ).setChecked( false );
    verify( deleteCheckbox, times( 1 ) ).setChecked( false );
    verify( manageCheckbox, times( 1 ) ).setChecked( false );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
    verify( readCheckbox, never() ).setDisabled( false );
  }

  @Test
  public void testUpdateCheckboxesReadPermissionsAppropriateTrue() {
    permissionsCheckboxHandler.updateCheckboxes( true, EnumSet.of( RepositoryFilePermission.READ ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( false );
    verify( deleteCheckbox, times( 1 ) ).setChecked( false );
    verify( manageCheckbox, times( 1 ) ).setChecked( false );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( false );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testUpdateCheckboxesReadPermissionsAppropriateFalse() {
    permissionsCheckboxHandler.updateCheckboxes( false, EnumSet.of( RepositoryFilePermission.READ ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( false );
    verify( deleteCheckbox, times( 1 ) ).setChecked( false );
    verify( manageCheckbox, times( 1 ) ).setChecked( false );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testUpdateCheckboxesWritePermissionsAppropriateTrue() {
    permissionsCheckboxHandler.updateCheckboxes( true, EnumSet.of( RepositoryFilePermission.WRITE,
        RepositoryFilePermission.READ ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( true );
    verify( deleteCheckbox, times( 1 ) ).setChecked( false );
    verify( manageCheckbox, times( 1 ) ).setChecked( false );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( false );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( false );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testUpdateCheckboxesWritePermissionsAppropriateFalse() {
    permissionsCheckboxHandler.updateCheckboxes( false, EnumSet.of( RepositoryFilePermission.WRITE,
        RepositoryFilePermission.READ ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( true );
    verify( deleteCheckbox, times( 1 ) ).setChecked( false );
    verify( manageCheckbox, times( 1 ) ).setChecked( false );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testUpdateCheckboxesDeletePermissionsAppropriateTrue() {
    permissionsCheckboxHandler.updateCheckboxes( true, EnumSet.of( RepositoryFilePermission.DELETE,
        RepositoryFilePermission.WRITE, RepositoryFilePermission.READ ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( true );
    verify( deleteCheckbox, times( 1 ) ).setChecked( true );
    verify( manageCheckbox, times( 1 ) ).setChecked( false );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( false );
    verify( manageCheckbox, times( 1 ) ).setDisabled( false );
  }

  @Test
  public void testUpdateCheckboxesDeletePermissionsAppropriateFalse() {
    permissionsCheckboxHandler.updateCheckboxes( false, EnumSet.of( RepositoryFilePermission.DELETE,
        RepositoryFilePermission.WRITE, RepositoryFilePermission.READ ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( true );
    verify( deleteCheckbox, times( 1 ) ).setChecked( true );
    verify( manageCheckbox, times( 1 ) ).setChecked( false );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testUpdateCheckboxesManagePermissionsAppropriateTrue() {
    permissionsCheckboxHandler.updateCheckboxes( true, EnumSet.of( RepositoryFilePermission.ACL_MANAGEMENT,
        RepositoryFilePermission.DELETE, RepositoryFilePermission.WRITE, RepositoryFilePermission.READ ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( true );
    verify( deleteCheckbox, times( 1 ) ).setChecked( true );
    verify( manageCheckbox, times( 1 ) ).setChecked( true );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( false );
  }

  @Test
  public void testUpdateCheckboxesManagePermissionsAppropriateFalse() {
    permissionsCheckboxHandler.updateCheckboxes( false, EnumSet.of( RepositoryFilePermission.ACL_MANAGEMENT,
        RepositoryFilePermission.DELETE, RepositoryFilePermission.WRITE, RepositoryFilePermission.READ ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( true );
    verify( deleteCheckbox, times( 1 ) ).setChecked( true );
    verify( manageCheckbox, times( 1 ) ).setChecked( true );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }

  @Test
  public void testUpdateCheckboxesAllPermissionsAppropriateTrue() {
    permissionsCheckboxHandler.updateCheckboxes( true, EnumSet.of( RepositoryFilePermission.ALL ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( true );
    verify( deleteCheckbox, times( 1 ) ).setChecked( true );
    verify( manageCheckbox, times( 1 ) ).setChecked( true );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( false );
  }

  @Test
  public void testUpdateCheckboxesAllPermissionsAppropriateFalse() {
    permissionsCheckboxHandler.updateCheckboxes( false, EnumSet.of( RepositoryFilePermission.ALL ) );
    verify( readCheckbox, times( 1 ) ).setChecked( true );
    verify( writeCheckbox, times( 1 ) ).setChecked( true );
    verify( deleteCheckbox, times( 1 ) ).setChecked( true );
    verify( manageCheckbox, times( 1 ) ).setChecked( true );
    verify( readCheckbox, times( 1 ) ).setDisabled( true );
    verify( writeCheckbox, times( 1 ) ).setDisabled( true );
    verify( deleteCheckbox, times( 1 ) ).setDisabled( true );
    verify( manageCheckbox, times( 1 ) ).setDisabled( true );
  }
}
