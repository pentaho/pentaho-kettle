/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class GlobalMessages extends AbstractMessageHandler {
  protected static Class<?> PKG = GlobalMessages.class;

  protected static final String SYSTEM_BUNDLE_PACKAGE = PKG.getPackage().getName();

  protected static final String BUNDLE_NAME = "messages.messages";

  protected static final Map<String, ResourceBundle> locales = Collections
    .synchronizedMap( new HashMap<String, ResourceBundle>() );

  protected static final LogChannelInterface log = new LogChannel( "i18n" );

  public static final String[] localeCodes = {
    "en_US", "nl_NL", "zh_CN", "zh_TW", "es_ES", "fr_FR", "de_DE", "pt_BR", "pt_PT", "es_AR", "no_NO", "it_IT", "ja_JP",
    "ko_KR" };

  public static final String[] localeDescr = {
    "English (US)", "Nederlands", "Simplified Chinese", "Traditional Chinese", "Espa\u00F1ol (Spain)", "Fran\u00E7ais", "Deutsch",
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

  protected static String buildBundleName( String packageName ) {
    return packageName + "." + BUNDLE_NAME;
  }

  /**
   * Retrieve a resource bundle of the default or fail-over locale.
   *
   * @param packageName The package to search in
   * @return The resource bundle
   * @throws MissingResourceException in case both resource bundles couldn't be found.
   */
  public static ResourceBundle getBundle( String packageName ) throws MissingResourceException {
    return GlobalMessageUtil.getBundle( packageName, PKG );
  }

  /**
   * Returns the localized string for the given {@code key} and {@code parameters} in a bundle defined by the the
   * concatenation of {@code packageName} and {@link #BUNDLE_NAME}, using the {@link GlobalMessages} class loader.
   *
   * @param packageName the package containing the localized messages
   * @param key         the message key being looked up
   * @param parameters  parameters within the looked up message
   * @return the localized string for the given {@code key} and {@code parameters} in a bundle defined by the the
   * concatenation of {@code packageName} and {@link #BUNDLE_NAME}, using the {@link GlobalMessages} class loader.
   */
  protected String calculateString( String packageName, String key, Object[] parameters ) {
    return calculateString( packageName, key, parameters, PKG );
  }

  /**
   * Returns the localized string for the given {@code key} and {@code parameters} in a bundle defined by the the
   * concatenation of {@code packageName} and {@link #BUNDLE_NAME}, using the provided {@code resourceClass}'s class
   * loader.
   *
   * @param packageName   the package containing the localized messages
   * @param key           the message key being looked up
   * @param parameters    parameters within the looked up message
   * @param resourceClass the class whose class loader is used to getch the resource bundle
   * @return the localized string for the given {@code key} and {@code parameters} in a bundle defined by the the
   * concatenation of {@code packageName} and {@link #BUNDLE_NAME}, using the provided {@code resourceClass}'s class
   * loader.
   */
  protected String calculateString( String packageName, String key, Object[] parameters, Class<?> resourceClass ) {
    final String[] pkgNames = new String[] { packageName, SYSTEM_BUNDLE_PACKAGE };
    return calculateString( pkgNames, key, parameters, resourceClass );
  }

  /**
   * Returns the localized string for the given {@code key} and {@code parameters} in a bundle defined by the the
   * concatenation of the package names defined in {@code packageName} and {@link #BUNDLE_NAME} (the first valid
   * combination of {@code packageName} + {@link #BUNDLE_NAME} wins), sing the provided {@code resourceClass}'s class
   * loader.
   *
   * @param pkgNames      an array of packages potentially containing the localized messages the first one found to
   *                      contain the messages is the one that is used to localize the message
   * @param key           the message key being looked up
   * @param parameters    parameters within the looked up message
   * @param resourceClass the class whose class loader is used to getch the resource bundle
   * @return the localized string for the given {@code key} and {@code parameters} in a bundle defined by the the
   * concatenation of the package names defined in {@code packageName} and {@link #BUNDLE_NAME} (the first valid
   * combination of {@code packageName} + {@link #BUNDLE_NAME} wins), sing the provided {@code resourceClass}'s class
   * loader
   */
  protected String calculateString( String[] pkgNames, String key, Object[] parameters, Class<?> resourceClass ) {
    return GlobalMessageUtil.calculateString( pkgNames, key, parameters, resourceClass, BUNDLE_NAME );
  }

  /**
   * Returns a {@link ResourceBundle} for the given {@link Locale} and {@code packagePath}, using the {@link
   * GlobalMessages} class loader.
   *
   * @param locale      the {@link Locale} for which the {@link ResourceBundle} is being retrieved
   * @param packagePath the full path to the localized message file without the {@code .properties} extension
   * @return a {@link ResourceBundle} for the given {@link Locale} and {@code packagePath}, using the {@link
   * GlobalMessages} class loader
   * @throws MissingResourceException when the {@link ResourceBundle} cannot be found
   */
  @VisibleForTesting
  static ResourceBundle getBundle( final Locale locale, final String packagePath ) throws MissingResourceException {
    return GlobalMessageUtil.getBundle( locale, packagePath, PKG );
  }

  @Override
  public String getString( String key ) {
    Object[] parameters = null;
    return calculateString( SYSTEM_BUNDLE_PACKAGE, key, parameters );
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
