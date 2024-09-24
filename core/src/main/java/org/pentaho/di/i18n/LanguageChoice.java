/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
  private static final String STRING_DEFAULT_LOCALE = "LocaleDefault";

  private static LanguageChoice choice;

  private Locale defaultLocale;

  private LanguageChoice() {
    try {
      loadSettings();
    } catch ( IOException e ) {
      // Can't load settings: set the default
      defaultLocale = Const.DEFAULT_LOCALE;

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
  }

  public void saveSettings() {
    try {
      Properties properties = new Properties();
      properties.setProperty( STRING_DEFAULT_LOCALE, defaultLocale.toString() );
      properties.store( new FileOutputStream( getSettingsFilename() ), "Language Choice" );
    } catch ( IOException e ) {
      // Ignore
    }
  }

  public String getSettingsFilename() {
    return Const.getKettleDirectory() + Const.FILE_SEPARATOR + ".languageChoice";
  }
}
