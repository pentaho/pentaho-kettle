/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.ui.job.entries.ping;

import org.junit.Test;
import org.pentaho.di.ui.core.widget.TextVar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobEntryPingDialogTest {

  @Test
  public void testAcceptsPositiveIntegers() {
    for ( String value : new String[] { "1", "020", "   22   ", "2147483647" } ) {
      assertTrue( value, JobEntryPingDialog.isValidNumericInput( value ) );
    }
  }

  @Test
  public void testAcceptsAllSubstitutionSyntaxes() {
    for ( String value : new String[] {
      "${ping}", "%%ping%%", "$[31,32]", " ${unresolved} ", "%%unresolved%%",
      "1${digit}", "1%%digit%%", "$[31]${digit}", "${first}%%second%%", "${java.version}"
    } ) {
      assertTrue( value, JobEntryPingDialog.isValidNumericInput( value ) );
    }
  }

  @Test
  public void testRejectsInvalidLiteralsAndIncompleteExpressions() {
    for ( String value : new String[] {
      null, "", "   ", "abc", "0", "-2", "5.05", "2147483648", "3,400",
      "$", "${", "${ping", "${}", "%", "%%", "%%ping", "%%%%", "$[", "$[31", "$[]", "?{ping}"
    } ) {
      assertFalse( value, JobEntryPingDialog.isValidNumericInput( value ) );
    }
  }

  @Test
  public void testInvalidInputIsNotCleared() {
    JobEntryPingDialog dialog = mock( JobEntryPingDialog.class );
    TextVar field = mock( TextVar.class );
    when( field.isEnabled() ).thenReturn( true );
    when( field.getText() ).thenReturn( "abc" );
    doCallRealMethod().when( dialog ).validateNumericInput( field, "JobPing.TimeOut.Label" );

    assertFalse( dialog.validateNumericInput( field, "JobPing.TimeOut.Label" ) );

    verify( dialog ).displayInfoMessage( anyString() );
    verify( field ).setFocus();
    verify( field, never() ).setText( anyString() );
  }

  @Test
  public void testDisabledFieldIsNotValidated() {
    JobEntryPingDialog dialog = mock( JobEntryPingDialog.class );
    TextVar field = mock( TextVar.class );
    when( field.isEnabled() ).thenReturn( false );
    doCallRealMethod().when( dialog ).validateNumericInput( field, "JobPing.NrPackets.Label" );

    assertTrue( dialog.validateNumericInput( field, "JobPing.NrPackets.Label" ) );

    verify( field, never() ).getText();
    verify( dialog, never() ).displayInfoMessage( anyString() );
  }

  @Test
  public void testVariableInputIsAcceptedWithoutResolvingOrChangingIt() {
    JobEntryPingDialog dialog = mock( JobEntryPingDialog.class );
    TextVar field = mock( TextVar.class );
    when( field.isEnabled() ).thenReturn( true );
    doCallRealMethod().when( dialog ).validateNumericInput( field, "JobPing.TimeOut.Label" );

    for ( String value : new String[] { "${unresolved}", "%%unresolved%%", "$[31,32]" } ) {
      when( field.getText() ).thenReturn( value );
      assertTrue( dialog.validateNumericInput( field, "JobPing.TimeOut.Label" ) );
    }

    verify( dialog, never() ).displayInfoMessage( anyString() );
    verify( field, never() ).setText( anyString() );
    verify( field, never() ).setFocus();
  }
}