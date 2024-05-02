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
package org.pentaho.ui.database;

import org.junit.Test;
import org.pentaho.di.i18n.LanguageChoice;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.reflect.Whitebox.setInternalState;

/**
 * Unit tests for Messages.
 */
public class MessagesTest {

  @Test
  public void testGetBundle() throws Exception {
    assertNotNull( Messages.getBundle() );
  }

  @Test
  public void testGetString() throws Exception {
    // These tests are meant for the en_US locale (or equivalent)
    assertEquals( "Database Connection", Messages.getString( "DatabaseDialog.Shell.title" ) );
    assertEquals( "!Not.A.Message!", Messages.getString( "Not.A.Message" ) );

    // 1 param
    assertEquals( "MyParam: JDBC options help", Messages.getString( "DatabaseDialog.JDBCOptions.Tab", "MyParam" ) );
    assertEquals( "!Not.A.Message!", Messages.getString( "Not.A.Message", "Unused1" ) );
    assertEquals( "!null!", Messages.getString( null, "Unused1" ) );

    // 2 params
    assertEquals( "MyParam: JDBC options help",
      Messages.getString( "DatabaseDialog.JDBCOptions.Tab", "MyParam", "Unused" ) );
    assertEquals( "!Not.A.Message!", Messages.getString( "Not.A.Message", "Unused1", "Unused2" ) );
    assertEquals( "!null!", Messages.getString( null, null, null ) );

    // 3 params
    assertEquals( "MyParam: JDBC options help",
      Messages.getString( "DatabaseDialog.JDBCOptions.Tab", "MyParam", "Unused2", "Unused3" ) );
    assertEquals( "!Not.A.Message!", Messages.getString( "Not.A.Message", "Unused1", "Unused2", "Unused3" ) );
    assertEquals( "!null!", Messages.getString( null, null, null, null ) );

    // 4 params
    assertEquals( "MyParam: JDBC options help",
      Messages.getString( "DatabaseDialog.JDBCOptions.Tab", "MyParam", "Unused2", "Unused3", "Unused4" ) );
    assertEquals( "!Not.A.Message!",
      Messages.getString( "Not.A.Message", "Unused1", "Unused2", "Unused3", "Unused4" ) );
    assertEquals( "!null!", Messages.getString( null, null, null, null, null ) );

  }

  @Test
  public void getStringLocalized() throws Exception {

    mockStatic( Messages.class );
    when( Messages.getString( anyString() ) ).thenCallRealMethod();
    when( Messages.getBundle() ).thenCallRealMethod();

    final String msgKey = "DatabaseDialog.column.Parameter";

    LanguageChoice.getInstance().setDefaultLocale( Locale.FRENCH );
    // force the recreation of the bundle after setting the preferred language
    setInternalState( Messages.class, "RESOURCE_BUNDLE", (ResourceBundle) null );
    assertEquals( "Parameter", Messages.getString( msgKey ) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.FRANCE );
    // force the recreation of the bundle after setting the preferred language
    setInternalState( Messages.class, "RESOURCE_BUNDLE", (ResourceBundle) null );
    assertEquals( "Parameter", Messages.getString( msgKey) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.ENGLISH );
    // force the recreation of the bundle after setting the preferred language
    setInternalState( Messages.class, "RESOURCE_BUNDLE", (ResourceBundle) null );
    assertEquals( "Parameter", Messages.getString( msgKey) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.US );
    // force the recreation of the bundle after setting the preferred language
    setInternalState( Messages.class, "RESOURCE_BUNDLE", (ResourceBundle) null );
    assertEquals( "Parameter", Messages.getString( msgKey) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.JAPANESE );
    // force the recreation of the bundle after setting the preferred language
    setInternalState( Messages.class, "RESOURCE_BUNDLE", (ResourceBundle) null );
    assertEquals( "パラメータ", Messages.getString( msgKey ) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.JAPAN );
    // force the recreation of the bundle after setting the preferred language
    setInternalState( Messages.class, "RESOURCE_BUNDLE", (ResourceBundle) null );
    assertEquals( "パラメータ", Messages.getString( msgKey ) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.SIMPLIFIED_CHINESE );
    // force the recreation of the bundle after setting the preferred language
    setInternalState( Messages.class, "RESOURCE_BUNDLE", (ResourceBundle) null );
    assertEquals( "命名参数", Messages.getString( msgKey ) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.CHINESE );
    // force the recreation of the bundle after setting the preferred language
    setInternalState( Messages.class, "RESOURCE_BUNDLE", (ResourceBundle) null );
    assertEquals( "Parameter", Messages.getString( msgKey ) );
  }
}
