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
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

public class GlobalMessageUtil {

  private static final Logger log = LoggerFactory.getLogger( GlobalMessageUtil.class );

  /**
   * Used when the preferred locale (as defined by the user) is not available.
   */
  public static final Locale FAILOVER_LOCALE = Locale.US;

  protected static final LanguageChoice langChoice = LanguageChoice.getInstance();

  protected static final ThreadLocal<Locale> threadLocales = new ThreadLocal();

  public static String formatErrorMessage( String key, String msg ) {
    String s2 = key.substring( 0, key.indexOf( '.' ) + "ERROR_0000".length() + 1 );
    return BaseMessages.getString( "MESSUTIL.ERROR_FORMAT_MASK", s2, msg );
  }

  private static String decorateMissingKey( final String key ) {
    final StringBuilder keyBuilder = new StringBuilder();
    keyBuilder.append( '!' ).append( key ).append( '!' );
    return keyBuilder.toString();
  }

  public static String getString( ResourceBundle bundle, String key ) throws MissingResourceException {
    return MessageFormat.format( bundle.getString( key ), new Object[] {} );
  }

  public static String getErrorString( ResourceBundle bundle, String key ) {
    return formatErrorMessage( key, getString( bundle, key ) );
  }

  public static String getString( ResourceBundle bundle, String key, String param1 ) {
    try {
      Object[] args = { param1 };
      return MessageFormat.format( bundle.getString( key ), args );
    } catch ( Exception e ) {
      return decorateMissingKey( key );
    }
  }

  public static String getErrorString( ResourceBundle bundle, String key, String param1 ) {
    return formatErrorMessage( key, getString( bundle, key, param1 ) );
  }

  public static String getString( ResourceBundle bundle, String key, String param1, String param2 ) {
    try {
      Object[] args = { param1, param2 };
      return MessageFormat.format( bundle.getString( key ), args );
    } catch ( Exception e ) {
      return decorateMissingKey( key );
    }
  }

  public static String getErrorString( ResourceBundle bundle, String key, String param1, String param2 ) {
    return formatErrorMessage( key, getString( bundle, key, param1, param2 ) );
  }

  public static String getString( ResourceBundle bundle, String key, String param1, String param2, String param3 ) {
    try {
      Object[] args = { param1, param2, param3 };
      return MessageFormat.format( bundle.getString( key ), args );
    } catch ( Exception e ) {
      return decorateMissingKey( key );
    }
  }

  public static String getErrorString( ResourceBundle bundle, String key, String param1, String param2,
                                       String param3 ) {
    return formatErrorMessage( key, getString( bundle, key, param1, param2, param3 ) );
  }

  public static String getString( ResourceBundle bundle, String key, String param1, String param2, String param3,
                                  String param4 ) {
    try {
      Object[] args = { param1, param2, param3, param4 };
      return MessageFormat.format( bundle.getString( key ), args );
    } catch ( Exception e ) {
      return decorateMissingKey( key );
    }
  }

  public static String getString( ResourceBundle bundle, String key, String param1, String param2, String param3,
                                  String param4, String param5 ) {
    try {
      Object[] args = { param1, param2, param3, param4, param5 };
      return MessageFormat.format( bundle.getString( key ), args );
    } catch ( Exception e ) {
      return decorateMissingKey( key );
    }
  }

  public static String getString( ResourceBundle bundle, String key, String param1, String param2, String param3,
                                  String param4, String param5, String param6 ) {
    try {
      Object[] args = { param1, param2, param3, param4, param5, param6 };
      return MessageFormat.format( bundle.getString( key ), args );
    } catch ( Exception e ) {
      return decorateMissingKey( key );
    }
  }

  public static String getErrorString( ResourceBundle bundle, String key, String param1, String param2,
                                       String param3, String param4 ) {
    return formatErrorMessage( key, getString( bundle, key, param1, param2, param3, param4 ) );
  }

  public static void setLocale( Locale newLocale ) {
    threadLocales.set( newLocale );
  }

  public static Locale getLocale() {
    Locale rtn = threadLocales.get();
    if ( rtn != null ) {
      return rtn;
    }

    setLocale( langChoice.getDefaultLocale() );
    return langChoice.getDefaultLocale();
  }

  /**
   * Returns a {@link LinkedHashSet} of {@link Locale}s for consideration when localizing text. The
   * {@link LinkedHashSet} contains the user selected preferred {@link Locale}, the failover {@link Locale}
   * ({@link Locale#ENGLISH}) and the {@link Locale#ROOT}.
   *
   * @return Returns a {@link LinkedHashSet} of {@link Locale}s for consideration when translating text
   */
  public static LinkedHashSet<Locale> getActiveLocales() {
    // Use a LinkedHashSet to maintain order
    final LinkedHashSet<Locale> activeLocales = new LinkedHashSet<>();
    // Example: messages_fr_FR.properties
    activeLocales.add( langChoice.getDefaultLocale() );
    // Example: messages_en_US.properties
    activeLocales.add( FAILOVER_LOCALE );
    // Example: messages.properties
    activeLocales.add( Locale.ROOT );
    return activeLocales;
  }

  /**
   * Calls {@link #calculateString(String[], String, Object[], Class, String, boolean)} with the {@code
   * logNotFoundError} parameter set to {@code true} to ensure proper error logging when the localized string cannot be
   * found.
   */

  public static String calculateString( final String[] pkgNames, final String key, final Object[] parameters,
                                        final Class<?> resourceClass, final String bundleName ) {
    return calculateString( pkgNames, key, parameters, resourceClass, bundleName, false, false );
  }

  public static String calculateString( final String[] pkgNames, final String key, Object[] parameters,
                                        final Class<?> resourceClass, final String bundleName,
                                        final boolean fallbackOnRoot ) {
    return calculateString( pkgNames, key, parameters, resourceClass, bundleName, false, fallbackOnRoot );
  }

  /**
   * Returns the localized string for the given {@code key} and {@code parameters} in a bundle defined by the the
   * concatenation of the package names defined in {@code packageName} and @code bundleName} (the first valid
   * combination of {@code packageName} + {@code bundleName} wins), sing the provided {@code resourceClass}'s class
   * loader.
   *
   * @param pkgNames         an array of packages potentially containing the localized messages the first one found to
   *                         contain the messages is the one that is used to localize the message
   * @param key              the message key being looked up
   * @param parameters       parameters within the looked up message
   * @param resourceClass    the class whose class loader is used to getch the resource bundle
   * @param bundleName       the name of the message bundle
   * @param logNotFoundError determines whether an error is logged when the localized string cannot be found - it can be
   *                         used to suppress the log in cases where it is known that various combinations of parameters
   *                         will be tried to fetch the message, to avoid unnecessary error logging.
   * @param fallbackOnRoot   if true, and a {@link ResourceBundle} cannot be found for a given {@link Locale},
   *                         falls back on the ROOT {@link Locale}
   * @return the localized string for the given {@code key} and {@code parameters} in a bundle defined by the the
   * concatenation of the package names defined in {@code packageName} and @code bundleName} (the first valid
   * combination of {@code packageName} + @code bundleName} wins), sing the provided {@code resourceClass}'s class
   * loader
   */
  public static String calculateString( final String[] pkgNames, final String key, final Object[] parameters,
                                        final Class<?> resourceClass, final String bundleName,
                                        final boolean logNotFoundError, final boolean fallbackOnRoot ) {

    final Set<Locale> activeLocales = getActiveLocales();
    for ( final Locale locale : activeLocales ) {
      final String string =
        calculateString( pkgNames, locale, key, parameters, resourceClass, bundleName, fallbackOnRoot );
      if ( !isMissingKey( string ) ) {
        return string;
      }
    }
    if ( logNotFoundError ) {
      final StringBuilder msg = new StringBuilder();
      msg.append( "Message not found in the preferred and failover locale: key=[" ).append( key ).append(
        "], package=" ).append( Arrays.asList( pkgNames ) );
      log.error( Const.getStackTracker( new KettleException( msg.toString() ) ) );
    }
    return decorateMissingKey( key );
  }

  private static String calculateString( final String[] pkgNames, final Locale locale, final String key,
                                         final Object[] parameters, final Class<?> resourceClass,
                                         final String bundleName, final boolean fallbackOnRoot ) {
    for ( final String packageName : pkgNames ) {
      try {
        return calculateString( packageName, locale, key, parameters, resourceClass, bundleName, fallbackOnRoot );
      } catch ( final MissingResourceException e ) {
        continue;
      }
    }
    return null;
  }

  static String calculateString( final String packageName, final Locale locale, final String key, Object[] parameters,
                                 final Class<?> resourceClass, final String bundleName ) {
    return calculateString( packageName, locale, key, parameters, resourceClass, bundleName, true );
  }

  @VisibleForTesting
  static String calculateString( final String packageName, final Locale locale, final String key,
                                 final Object[] parameters, final Class<?> resourceClass, final String bundleName,
                                 final boolean fallbackOnRoot ) throws MissingResourceException {
    try {
      ResourceBundle bundle = getBundle( locale, packageName + "." + bundleName, resourceClass, fallbackOnRoot );
      String unformattedString = bundle.getString( key );
      String string = MessageFormat.format( unformattedString, parameters );
      return string;
    } catch ( IllegalArgumentException e ) {
      final StringBuilder msg = new StringBuilder();
      msg.append( "Format problem with key=[" ).append( key ).append( "], locale=[" ).append( locale ).append(
        "], package=" ).append( packageName ).append( " : " ).append( e.toString() );
      log.error( msg.toString() );
      log.error( Const.getStackTracker( e ) );
      throw new MissingResourceException( msg.toString(), packageName, key );
    }
  }

  /**
   * Retrieve a resource bundle of the default or fail-over locales.
   *
   * @param packagePath   The package to search in
   * @param resourceClass the class to use to resolve the bundle
   * @return The resource bundle
   * @throws MissingResourceException in case both resource bundles couldn't be found.
   */
  public static ResourceBundle getBundle( final String packagePath, final Class<?> resourceClass )
    throws MissingResourceException {
    final Set<Locale> activeLocales = getActiveLocales();
    for ( final Locale locale : activeLocales ) {
      try {
        return getBundle( locale, packagePath, resourceClass );
      } catch ( MissingResourceException e ) {
        final StringBuilder msg = new StringBuilder();
        msg.append( "Unable to find properties file for package '" ).append( packagePath ).append( "' and class '" )
          .append( resourceClass.getName() ).append( "' in the available locales: " ).append( locale );
        // nothing to do, an exception will be thrown if no bundle is found
        log.warn( msg.toString() );
      }
    }
    final StringBuilder msg = new StringBuilder();
    msg.append( "Unable to find properties file for package '" ).append( packagePath ).append( "' and class '" )
      .append( resourceClass.getName() ).append( "' in the available locales: " ).append(
      Arrays.asList( activeLocales ) );
    throw new MissingResourceException( msg.toString(), resourceClass.getName(),
      packagePath );
  }

  public static ResourceBundle getBundle( Locale locale, String packagePath, Class<?> resourceClass ) {
    return getBundle( locale, packagePath, resourceClass, true );
  }

  /**
   * Returns a {@link ResourceBundle} corresponding to the given {@link Locale} package and resource class. Falls-back
   * on the ROOT {@link Locale}, if the {@code fallbackOnRoot} flag is true and the requested Locale is not available.
   *
   * @param locale         the {@link Locale} for which the {@link ResourceBundle} is being requested
   * @param packagePath
   * @param resourceClass
   * @param fallbackOnRoot if true, and a {@link ResourceBundle} cannot be found for the requested {@link Locale}, falls
   *                       back on the ROOT {@link Locale}
   * @return a {@link ResourceBundle} corresponding to the given {@link Locale} package and resource class
   */
  public static ResourceBundle getBundle( final Locale locale, final String packagePath, final Class<?> resourceClass,
                                          final boolean fallbackOnRoot ) {
    final GlobalMessageControl control = new GlobalMessageControl( fallbackOnRoot );
    final String resourceName = control.toResourceName( control.toBundleName( packagePath, locale ), "properties" );

    ResourceBundle bundle;
    try {
      bundle = ResourceBundle.getBundle( packagePath, locale, resourceClass.getClassLoader(),
        new GlobalMessageControl( fallbackOnRoot ) );
    } catch ( final MissingResourceException e ) {
      final StringBuilder msg = new StringBuilder();
      msg.append( "Unable to find properties file '" ).append( resourceName ).append( "': " ).append( e.toString() );
      throw new MissingResourceException( msg.toString(), resourceClass.getName(), packagePath );
    }
    return bundle;
  }

  /**
   * Returns a string corresponding to the locale (Example: "en", "en_US").
   *
   * @param locale The {@link Locale} whose string representation it being returned
   * @return a string corresponding to the locale (Example: "en", "en_US").
   */
  protected static String getLocaleString( Locale locale ) {
    final StringBuilder localeString = new StringBuilder();
    if ( locale != null && !StringUtils.isBlank( locale.getLanguage() ) ) {
      if ( !StringUtils.isBlank( locale.getCountry() ) ) {
        // force language to be lower case and country to be upper case
        localeString.append( locale.getLanguage().toLowerCase() ).append( '_' ).append(
          locale.getCountry().toUpperCase() );
      } else {
        // force language to be lower case
        localeString.append( locale.getLanguage().toLowerCase() );
      }
    }
    return localeString.toString();
  }

  /**
   * Returns true if the given {@code string} is null or is in the format of a missing key: !key!.
   *
   * @param string
   * @return true if the given {@code string} is null or is in the format of a missing key: !key!.
   */
  protected static boolean isMissingKey( final String string ) {
    return string == null || ( string.trim().startsWith( "!" ) && string.trim().endsWith( "!" )
      && !string.trim().equals( "!" ) );
  }

  /**
   * A custom {@link ResourceBundle.Control} implementation that provides the desired fall-back mechanism.
   */
  static class GlobalMessageControl extends ResourceBundle.Control {

    private boolean fallbackOnRoot;

    GlobalMessageControl( final boolean fallbackOnRoot ) {
      this.fallbackOnRoot = fallbackOnRoot;
    }

    @Override
    public Locale getFallbackLocale( final String baseName, final Locale locale ) {
      // we have our own fall-back mechanism
      return null;
    }

    @Override
    public List<Locale> getCandidateLocales( final String baseName, final Locale locale ) {
      // we have our own fall-back mechanism
      final List<Locale> locales = super.getCandidateLocales( baseName, locale );
      // remove the root locale, as we want to handle it ourselves, unless the locale itself is root
      if ( !fallbackOnRoot && !locale.equals( Locale.ROOT ) ) {
        locales.remove( Locale.ROOT );
      }
      return locales;
    }

    @Override
    public ResourceBundle newBundle( final String baseName, final Locale locale, final String format,
                                     final ClassLoader loader, final boolean reload )
      throws IllegalAccessException, InstantiationException, IOException {
      final String resourceName = toResourceName( toBundleName( baseName, locale ), "properties" );
      ResourceBundle bundle;
      InputStream stream = null;
      if ( reload ) {
        final URL url = loader.getResource( resourceName );
        if ( url != null ) {
          final URLConnection connection = url.openConnection();
          if ( connection != null ) {
            connection.setUseCaches( false );
            stream = connection.getInputStream();
          }
        }
      } else {
        stream = loader.getResourceAsStream( resourceName );
      }
      if ( stream == null ) {
        // Retry with the system class loader, just in case we are dealing with a messy plug-in.
        stream = ClassLoader.getSystemResourceAsStream( resourceName );
      }
      if ( stream != null ) {
        try {
          // use UTF-8 encoding
          bundle = new PropertyResourceBundle( new InputStreamReader( stream, StandardCharsets.UTF_8.name() ) );
        } finally {
          stream.close();
        }
      } else {
        final StringBuilder msg = new StringBuilder();
        msg.append( "Unable to find properties file '" ).append( resourceName ).append( "'" );
        throw new MissingResourceException( msg.toString(), loader.getClass().getName(), baseName );
      }
      return bundle;
    }
  }
}
