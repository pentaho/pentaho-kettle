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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.io.InputStreamReader;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

public class GlobalMessages extends AbstractMessageHandler {
  private static String packageNM = GlobalMessages.class.getPackage().getName();

  protected static final ThreadLocal<Locale> threadLocales = new ThreadLocal<Locale>();

  protected static final LanguageChoice langChoice = LanguageChoice.getInstance();

  protected static final String SYSTEM_BUNDLE_PACKAGE = GlobalMessages.class.getPackage().getName();

  protected static final String BUNDLE_NAME = "messages.messages";

  protected static final Map<String, ResourceBundle> locales = Collections
    .synchronizedMap( new HashMap<String, ResourceBundle>() );

  protected static final LogChannelInterface log = new LogChannel( "i18n" );

  public static final String[] localeCodes = {
    "en_US", "nl_NL", "zh_CN", "es_ES", "fr_FR", "de_DE", "pt_BR", "pt_PT", "es_AR", "no_NO", "it_IT", "ja_JP",
    "ko_KR" };

  public static final String[] localeDescr = {
    "English (US)", "Nederlands", "Simplified Chinese", "Espa\u00F1ol (Spain)", "Fran\u00E7ais", "Deutsch",
    "Portuguese (Brazil)", "Portuguese (Portugal)", "Espa\u00F1ol (Argentina)", "Norwegian (Norway)",
    "Italian (Italy)", "Japanese (Japan)", "Korean (Korea)", };

  protected static GlobalMessages GMinstance = null;

  /**
   * TODO: extend from abstract class to ensure singleton status and migrate instantiation to class controlled private
   */
  public GlobalMessages() {
  }

  public static synchronized MessageHandler getInstance() {
    if ( GMinstance == null ) {
      GMinstance = new GlobalMessages();
    }
    return GMinstance;
  }

  protected static Map<String, ResourceBundle> getLocales() {
    return locales;
  }

  public static synchronized Locale getLocale() {
    Locale rtn = threadLocales.get();
    if ( rtn != null ) {
      return rtn;
    }

    setLocale( langChoice.getDefaultLocale() );
    return langChoice.getDefaultLocale();
  }

  public static synchronized void setLocale( Locale newLocale ) {
    threadLocales.set( newLocale );
  }

  protected static String getLocaleString( Locale locale ) {
    String locString = locale.toString();
    if ( locString.length() == 5 && locString.charAt( 2 ) == '_' ) { // Force upper-lowercase format
      locString = locString.substring( 0, 2 ).toLowerCase() + "_" + locString.substring( 3 ).toUpperCase();
      // System.out.println("locString="+locString);
    }
    return locString;
  }

  protected static String buildHashKey( Locale locale, String packageName ) {
    return packageName + "_" + getLocaleString( locale );
  }

  protected static String buildBundleName( String packageName ) {
    return packageName + "." + BUNDLE_NAME;
  }

  /**
   * Retrieve a resource bundle of the default or fail-over locale.
   *
   * @param packageName
   *          The package to search in
   * @return The resource bundle
   * @throws MissingResourceException
   *           in case both resource bundles couldn't be found.
   */
  public static ResourceBundle getBundle( String packageName ) throws MissingResourceException {
    return getBundle( packageName, GlobalMessages.getInstance().getClass() );
  }

  /**
   * Retrieve a resource bundle of the default or fail-over locale.
   *
   * @param packageName
   *          The package to search in
   * @param resourceClass
   *          the class to use to resolve the bundle
   * @return The resource bundle
   * @throws MissingResourceException
   *           in case both resource bundles couldn't be found.
   */
  public static ResourceBundle getBundle( String packageName, Class<?> resourceClass ) throws MissingResourceException {
    ResourceBundle bundle;
    try {
      // First try to load the bundle in the default locale
      //
      bundle = getBundle( LanguageChoice.getInstance().getDefaultLocale(), packageName, resourceClass );
      return bundle;
    } catch ( MissingResourceException e ) {
      try {
        // Now retry the fail-over locale (en_US etc)
        //
        bundle = getBundle( LanguageChoice.getInstance().getFailoverLocale(), packageName, resourceClass );
        return bundle;
      } catch ( MissingResourceException e2 ) {
        // If nothing usable could be found throw an exception...
        //
        throw new MissingResourceException( "Unable to find properties file in the default '"
          + LanguageChoice.getInstance().getDefaultLocale() + "' nor the failore locale '"
          + LanguageChoice.getInstance().getFailoverLocale() + "'", packageName, packageName );
      }
    }
  }

  public static ResourceBundle getBundle( Locale locale, String packageName ) throws MissingResourceException {
    return getBundle( locale, packageName, GlobalMessages.getInstance().getClass() );
  }

  public static ResourceBundle getBundle( Locale locale, String packageName, Class<?> resourceClass ) throws MissingResourceException {
    String filename = buildHashKey( locale, packageName );
    filename = "/" + filename.replace( '.', '/' ) + ".properties";
    InputStream inputStream = null;
    try {
      ResourceBundle bundle = locales.get( filename );
      if ( bundle == null ) {
        inputStream = resourceClass.getResourceAsStream( filename );
        if ( inputStream == null ) {
          // Retry with the system class loader, just in case we are dealing with a messy plug-in.
          //
          inputStream = ClassLoader.getSystemResourceAsStream( filename );
        }
        // Now get the bundle from the messages files input stream
        //
        if ( inputStream != null ) {
          bundle = new PropertyResourceBundle( new InputStreamReader( inputStream, "UTF-8" ) );
          locales.put( filename, bundle );
        } else {
          throw new MissingResourceException( "Unable to find properties file [" + filename + "]", locale
            .toString(), packageName );
        }
      }
      return bundle;
    } catch ( IOException e ) {
      throw new MissingResourceException(
        "Unable to find properties file [" + filename + "] : " + e.toString(), locale.toString(), packageName );
    } finally {
      if ( inputStream != null ) {
        try {
          inputStream.close();
        } catch ( Exception e ) {
          // ignore this
        }
      }
    }
  }

  protected String findString( String packageName, Locale locale, String key, Object[] parameters ) throws MissingResourceException {
    return findString( packageName, locale, key, parameters, GlobalMessages.getInstance().getClass() );
  }

  protected String findString( String packageName, Locale locale, String key, Object[] parameters,
    Class<?> resourceClass ) throws MissingResourceException {
    try {
      ResourceBundle bundle = getBundle( locale, packageName + "." + BUNDLE_NAME, resourceClass );
      String unformattedString = bundle.getString( key );
      String string = MessageFormat.format( unformattedString, parameters );
      return string;
    } catch ( IllegalArgumentException e ) {
      String message =
        "Format problem with key=["
          + key + "], locale=[" + locale + "], package=" + packageName + " : " + e.toString();
      log.logError( message );
      log.logError( Const.getStackTracker( e ) );
      throw new MissingResourceException( message, packageName, key );
    }
  }

  protected String calculateString( String packageName, String key, Object[] parameters ) {
    return calculateString( packageName, key, parameters, GlobalMessages.getInstance().getClass() );
  }

  protected String calculateString( String packageName, String key, Object[] parameters, Class<?> resourceClass ) {
    String string = null;

    // First try the standard locale, in the local package
    try {
      string = findString( packageName, langChoice.getDefaultLocale(), key, parameters, resourceClass );
    } catch ( MissingResourceException e ) { /* Ignore */
    }
    if ( string != null ) {
      return string;
    }

    // Then try to find it in the i18n package, in the system messages of the preferred language.
    try {
      string = findString( SYSTEM_BUNDLE_PACKAGE, langChoice.getDefaultLocale(), key, parameters, resourceClass );
    } catch ( MissingResourceException e ) { /* Ignore */
    }
    if ( string != null ) {
      return string;
    }

    // Then try the failover locale, in the local package
    try {
      string = findString( packageName, langChoice.getFailoverLocale(), key, parameters, resourceClass );
    } catch ( MissingResourceException e ) { /* Ignore */
    }
    if ( string != null ) {
      return string;
    }

    // Then try to find it in the i18n package, in the system messages of the failover language.
    try {
      string = findString( SYSTEM_BUNDLE_PACKAGE, langChoice.getFailoverLocale(), key, parameters, resourceClass );
    } catch ( MissingResourceException e ) { /* Ignore */
    }
    if ( string != null ) {
      return string;
    }

    string = "!" + key + "!";
    String message =
      "Message not found in the preferred and failover locale: key=[" + key + "], package=" + packageName;
    log.logDetailed( Const.getStackTracker( new KettleException( message ) ) );

    return string;
  }

  @Override
  public String getString( String key ) {
    Object[] parameters = null;
    return calculateString( packageNM, key, parameters );
  }

  @Override
  public String getString( String packageName, String key ) {
    Object[] parameters = new Object[] {};
    return calculateString( packageName, key, parameters );
  }

  @Override
  public String getString( String packageName, String key, String... parameters ) {
    return calculateString( packageName, key, parameters );
  }

  @Override
  public String getString( String packageName, String key, Class<?> resourceClass, String... parameters ) {
    return calculateString( packageName, key, parameters, resourceClass );
  }

}
