/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import org.junit.Test;

public class GlobalMessagesTest {
  /*
   * https://github.com/pentaho/pentaho-kettle/pull/620
   * Reading properties file without native2ascii. (use UTF8 characters) #620
   */
  private ResourceBundle res = null;

  /*
   * to validate that old-style escaped ISO-8859-1 files are read correctly as before
   */
  @Test
  public void testGetBundleOldASCII() throws Exception {
    res = GlobalMessages.getBundle( Locale.JAPAN, "org/pentaho/di/i18n/messages/test_ascii_messages" );
    assertEquals( "環境変数の選択", res.getString( "System.Dialog.SelectEnvironmentVar.Title" ) );

    res = GlobalMessages.getBundle( Locale.CHINA, "org/pentaho/di/i18n/messages/test_ascii_messages" );
    assertEquals( "选择一个环境变量", res.getString( "System.Dialog.SelectEnvironmentVar.Title" ) );

    res = GlobalMessages.getBundle( Locale.US, "org/pentaho/di/i18n/messages/test_ascii_messages" );
    assertEquals( "Select an Environment Variable", res.getString( "System.Dialog.SelectEnvironmentVar.Title" ) );
  }

  /*
   * to validate that new-style UTF8 files are read correctly
   */
  @Test
  public void testGetBundleNewUTF8() throws Exception {
    res = GlobalMessages.getBundle( Locale.JAPAN, "org/pentaho/di/i18n/messages/test_utf8_messages" );
    assertEquals( "環境変数の選択", res.getString( "System.Dialog.SelectEnvironmentVar.Title" ) );

    res = GlobalMessages.getBundle( Locale.CHINA, "org/pentaho/di/i18n/messages/test_utf8_messages" );
    assertEquals( "选择一个环境变量", res.getString( "System.Dialog.SelectEnvironmentVar.Title" ) );
  }

  /*
   * to validate the format of an unmatched string
   */
  @Test
  public void testUnmatchedString() {
    String messageId = UUID.randomUUID().toString();
    assertEquals( "!" + messageId + "!", GlobalMessages.getInstance().getString( messageId ) );
  }
}
