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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;

public class LanguageChoice {
  private static final String STRING_FAILOVER_LOCALE = "LocaleFailover";
  private static final String STRING_DEFAULT_LOCALE = "LocaleDefault";

  private static LanguageChoice choice;

  private Locale defaultLocale;
  private Locale failoverLocale;

  private LanguageChoice() {
    try {
      loadSettings();
    } catch ( IOException e ) {
      // Can't load settings: set the default
      defaultLocale = Const.DEFAULT_LOCALE;
      failoverLocale = Locale.US;

      if ( defaultLocale.getLanguage().equals( Locale.GERMAN.getLanguage() ) ) {
        defaultLocale = Locale.US;
      }
    }
  }

  public static final LanguageChoice getInstance() {
    if ( choice != null ) {
      return choice;
    }

    choice = new LanguageChoice();

    return choice;
  }

  /**
   * @return Returns the defaultLocale.
   */
  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  /**
   * @param defaultLocale
   *          The defaultLocale to set.
   */
  public void setDefaultLocale( Locale defaultLocale ) {
    this.defaultLocale = defaultLocale;
  }

  /**
   * @return Returns the failoverLocale.
   */
  public Locale getFailoverLocale() {
    return failoverLocale;
  }

  /**
   * @param failoverLocale
   *          The failoverLocale to set.
   */
  public void setFailoverLocale( Locale failoverLocale ) {
    this.failoverLocale = failoverLocale;
  }

  private void loadSettings() throws IOException {
    Properties properties = new Properties();
    FileInputStream fis = new FileInputStream( getSettingsFilename() );
    try {
      properties.load( fis );
    } finally {
      try {
        fis.close();
      } catch ( IOException ignored ) {
        // Ignore closure exceptions
      }
    }

    String defaultLocaleStr = properties.getProperty( STRING_DEFAULT_LOCALE, Const.DEFAULT_LOCALE.toString() );
    defaultLocale = EnvUtil.createLocale( defaultLocaleStr );

    String failoverLocaleStr = properties.getProperty( STRING_FAILOVER_LOCALE, "en_US" );
    failoverLocale = EnvUtil.createLocale( failoverLocaleStr );
  }

  public void saveSettings() {
    try {
      Properties properties = new Properties();
      properties.setProperty( STRING_DEFAULT_LOCALE, defaultLocale.toString() );
      properties.setProperty( STRING_FAILOVER_LOCALE, failoverLocale.toString() );
      properties.store( new FileOutputStream( getSettingsFilename() ), "Language Choice" );
    } catch ( IOException e ) {
      // Ignore
    }
  }

  public String getSettingsFilename() {
    return Const.getKettleDirectory() + Const.FILE_SEPARATOR + ".languageChoice";
  }
}
