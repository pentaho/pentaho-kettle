/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.ui.repo.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.pentaho.di.ui.repo.service.BrowserAuthenticationService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BrowserAuthDialogHelperTest {

  @Test
  public void returnsTrueImmediatelyWhenNoAuthInProgress() {
    Shell shell = mock( Shell.class );
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    when( authService.isAuthInProgress() ).thenReturn( false );

    boolean result = BrowserAuthDialogHelper.confirmCancelExistingLoginIfNeeded( shell, authService );

    assertTrue( result );
    verify( authService, never() ).cancelCurrentAuth();
  }

  @Test
  public void returnsTrueAndCancelsWhenUserConfirmsYes() {
    Shell shell = mock( Shell.class );
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    when( authService.isAuthInProgress() ).thenReturn( true );

    try ( MockedConstruction<MessageBox> mocked = mockConstruction( MessageBox.class,
      ( mb, context ) -> when( mb.open() ).thenReturn( SWT.YES ) ) ) {

      boolean result = BrowserAuthDialogHelper.confirmCancelExistingLoginIfNeeded( shell, authService );

      assertTrue( result );
      verify( authService ).cancelCurrentAuth();
    }
  }

  @Test
  public void returnsFalseWhenUserDeclinesNo() {
    Shell shell = mock( Shell.class );
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    when( authService.isAuthInProgress() ).thenReturn( true );

    try ( MockedConstruction<MessageBox> mocked = mockConstruction( MessageBox.class,
      ( mb, context ) -> when( mb.open() ).thenReturn( SWT.NO ) ) ) {

      boolean result = BrowserAuthDialogHelper.confirmCancelExistingLoginIfNeeded( shell, authService );

      assertFalse( result );
      verify( authService, never() ).cancelCurrentAuth();
    }
  }

  @Test
  public void doesNotCancelAuthWhenNoAuthInProgress() {
    Shell shell = mock( Shell.class );
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    when( authService.isAuthInProgress() ).thenReturn( false );

    BrowserAuthDialogHelper.confirmCancelExistingLoginIfNeeded( shell, authService );

    verify( authService, never() ).cancelCurrentAuth();
  }

  @Test
  public void setsMessageBoxTextAndMessageWhenAuthInProgress() {
    Shell shell = mock( Shell.class );
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    when( authService.isAuthInProgress() ).thenReturn( true );

    try ( MockedConstruction<MessageBox> mocked = mockConstruction( MessageBox.class,
      ( mb, context ) -> when( mb.open() ).thenReturn( SWT.NO ) ) ) {

      BrowserAuthDialogHelper.confirmCancelExistingLoginIfNeeded( shell, authService );

      MessageBox mb = mocked.constructed().get( 0 );
      verify( mb ).setText( org.mockito.ArgumentMatchers.anyString() );
      verify( mb ).setMessage( org.mockito.ArgumentMatchers.anyString() );
    }
  }

  @Test
  public void returnsFalseWhenUserClosesDialogWithoutSelectingYes() {
    Shell shell = mock( Shell.class );
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    when( authService.isAuthInProgress() ).thenReturn( true );

    try ( MockedConstruction<MessageBox> mocked = mockConstruction( MessageBox.class,
      ( mb, context ) -> when( mb.open() ).thenReturn( SWT.CANCEL ) ) ) {

      boolean result = BrowserAuthDialogHelper.confirmCancelExistingLoginIfNeeded( shell, authService );

      assertFalse( result );
      verify( authService, never() ).cancelCurrentAuth();
    }
  }
}

