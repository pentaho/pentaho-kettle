/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.i18n;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.util.Locale;

public class GlobalMessageUtilTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Test
  public void testGetLocaleString() {
    Assert.assertEquals( "", GlobalMessageUtil.getLocaleString( null ) );
    Assert.assertEquals( "", GlobalMessageUtil.getLocaleString( new Locale( "" ) ) );
    Assert.assertEquals( "en", GlobalMessageUtil.getLocaleString( Locale.ENGLISH ) );
    Assert.assertEquals( "en_US", GlobalMessageUtil.getLocaleString( Locale.US ) );
    Assert.assertEquals( "en", GlobalMessageUtil.getLocaleString( new Locale( "EN" ) ) );
    Assert.assertEquals( "en_US", GlobalMessageUtil.getLocaleString( new Locale( "EN", "us" ) ) );
  }

  @Test
  public void isMissingKey() {
    Assert.assertTrue( GlobalMessageUtil.isMissingKey( null ) );
    Assert.assertFalse( GlobalMessageUtil.isMissingKey( "" ) );
    Assert.assertFalse( GlobalMessageUtil.isMissingKey( " " ) );
    Assert.assertTrue( GlobalMessageUtil.isMissingKey( "!foo!" ) );
    Assert.assertTrue( GlobalMessageUtil.isMissingKey( "!foo! " ) );
    Assert.assertTrue( GlobalMessageUtil.isMissingKey( " !foo!" ) );
    Assert.assertFalse( GlobalMessageUtil.isMissingKey( "!foo" ) );
    Assert.assertFalse( GlobalMessageUtil.isMissingKey( "foo!" ) );
    Assert.assertFalse( GlobalMessageUtil.isMissingKey( "foo" ) );
    Assert.assertFalse( GlobalMessageUtil.isMissingKey( "!" ) );
    Assert.assertFalse( GlobalMessageUtil.isMissingKey( " !" ) );
  }

  @Test
  public void calculateString() {

    // "fr", "FR"
    Assert.assertEquals( "Une certaine valeur foo", GlobalMessageUtil.calculateString(
      GlobalMessages.SYSTEM_BUNDLE_PACKAGE, Locale.FRANCE, "someKey", new String[] { "foo" }, GlobalMessages.PKG,
      GlobalMessages.BUNDLE_NAME ) );

    // "fr" - should fall back on default bundle
    String str = GlobalMessageUtil.calculateString( GlobalMessages.SYSTEM_BUNDLE_PACKAGE,
      Locale.FRENCH, "someKey", new String[] { "foo" }, GlobalMessages.PKG, GlobalMessages.BUNDLE_NAME );
    Assert.assertEquals( "Some Value foo", str );

    // "jp"
    Assert.assertEquals( "何らかの値 foo", GlobalMessageUtil.calculateString( GlobalMessages.SYSTEM_BUNDLE_PACKAGE,
      Locale.JAPANESE, "someKey", new String[] { "foo" }, GlobalMessages.PKG, GlobalMessages.BUNDLE_NAME ) );

    // "jp", "JP" - should fall back on "jp"
    str = GlobalMessageUtil.calculateString( GlobalMessages.SYSTEM_BUNDLE_PACKAGE, Locale.JAPAN, "someKey", new String[]
      { "foo" }, GlobalMessages.PKG, GlobalMessages.BUNDLE_NAME );
    Assert.assertEquals( "何らかの値 foo", str );

    // try with multiple packages
    // make sure the selected language is used correctly
    LanguageChoice.getInstance().setDefaultLocale( Locale.FRANCE ); // "fr", "FR"
    Assert.assertEquals( "Une certaine valeur foo", GlobalMessageUtil.calculateString( new String[] { GlobalMessages
      .SYSTEM_BUNDLE_PACKAGE }, "someKey", new String[] { "foo" }, GlobalMessages.PKG, GlobalMessages.BUNDLE_NAME ) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.FRENCH ); // "fr" - fall back on "default" messages.properties
    Assert.assertEquals( "Some Value foo", GlobalMessageUtil.calculateString( new String[] { GlobalMessages
      .SYSTEM_BUNDLE_PACKAGE }, "someKey", new String[] { "foo" }, GlobalMessages.PKG, GlobalMessages.BUNDLE_NAME ) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.FRENCH ); // "fr" - fall back on foo/messages_fr.properties
    Assert.assertEquals( "Une certaine valeur foo", GlobalMessageUtil.calculateString( new String[] { GlobalMessages
        .SYSTEM_BUNDLE_PACKAGE, "org.pentaho.di.foo" }, "someKey", new String[] { "foo" }, GlobalMessages.PKG,
      GlobalMessages.BUNDLE_NAME ) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.JAPANESE ); // "jp"
    Assert.assertEquals( "何らかの値 foo", GlobalMessageUtil.calculateString( new String[] { GlobalMessages
      .SYSTEM_BUNDLE_PACKAGE }, "someKey", new String[] { "foo" }, GlobalMessages.PKG, GlobalMessages.BUNDLE_NAME ) );

    LanguageChoice.getInstance().setDefaultLocale( Locale.JAPAN ); // "jp", "JP" - fall back on "jp"
    Assert.assertEquals( "何らかの値 foo", GlobalMessageUtil.calculateString( new String[] { GlobalMessages
      .SYSTEM_BUNDLE_PACKAGE }, "someKey", new String[] { "foo" }, GlobalMessages.PKG, GlobalMessages.BUNDLE_NAME ) );
  }
}
