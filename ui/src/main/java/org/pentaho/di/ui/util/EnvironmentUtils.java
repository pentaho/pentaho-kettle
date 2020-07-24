/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 - 2020 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.PropsUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentUtils {

  private static final EnvironmentUtils ENVIRONMENT_UTILS = new EnvironmentUtils( );
  private static final Pattern MSIE_PATTERN = Pattern.compile( "MSIE (\\d+)" );
  private static final Pattern SAFARI_PATTERN = Pattern.compile( "AppleWebKit\\/(\\d+)" );
  private static final String SUPPORTED_DISTRIBUTION_NAME = "ubuntu";
  public static final String UBUNTU_BROWSER = "Midori";
  public static final String MAC_BROWSER = "Safari";
  public static final String WINDOWS_BROWSER = "MSIE";
  private final LogChannelInterface log = new LogChannel( this );

  public static EnvironmentUtils getInstance( ) {
    return ENVIRONMENT_UTILS;
  }

  /**
   * Checks the available browser to see if it is an unsupported one.
   *
   * @return 'true' if in a unSupported browser environment 'false' otherwise.
   */
  public synchronized boolean isUnsupportedBrowserEnvironment() {
    if ( getEnvironmentName().contains( "linux" ) ) {
      return false;
    }
    final String userAgent = getUserAgent();
    if ( userAgent == null ) {
      return true;
    }
    return checkUserAgent( MSIE_PATTERN.matcher( userAgent ), getSupportedVersion( "min.windows.browser.supported" )  )
      || checkUserAgent( SAFARI_PATTERN.matcher( userAgent ), getSupportedVersion( "min.mac.browser.supported" )  );
  }

  private boolean checkUserAgent( Matcher matcher, int version ) {
    return  ( matcher.find() && Integer.parseInt( matcher.group( 1 ) ) < version );
  }

  /**
   * Ask for user Agent of the available browser.
   *
   * @return a string that contains the user agent of the browser.
   */
  protected String getUserAgent() {
    Browser browser;
    try {
      browser = new Browser(  new Shell(), SWT.NONE );
    } catch ( SWTError e ) {
      log.logError( "Could not open a browser", e );
      return  "";
    }
    String userAgent = browser.evaluate( "return window.navigator.userAgent;" ).toString();
    browser.close();
    return userAgent;
  }

  /**
   * Checks the existence of the webkit library on ubuntu.
   *
   * @return 'true' if the webkit library is not present in ubuntu, 'false' otherwise.
   */
  public synchronized boolean isWebkitUnavailable() {
    String path = getWebkitPath();
    String osName = getEnvironmentName();
    return  ( path == null || path.length() < 1 || !path.contains( "webkit" ) ) && osName.contains( SUPPORTED_DISTRIBUTION_NAME );
  }

  /**
   * Ask for the path in the system for the webkit library.
   *
   * @return a string that contains the path or 'null' if not found.
   */
  protected String getWebkitPath() {
    return System.getenv( "LIBWEBKITGTK" );
  }

  /**
   * Ask for the Operating system name.
   *
   * @return a string that contains the current Operating System.
   */
  private String getEnvironmentName() {
    String osName = getOsName();
    if ( osName.contentEquals( "linux" ) ) {
      return osName + " " + getLinuxDistribution().toLowerCase();
    }
    return osName;
  }

  protected String getOsName() {
    return System.getProperty( "os.name" ).toLowerCase();
  }

  /**
   * Gets the supported version of the required Property.
   *
   * @param property a string with the required property.
   * @return the value of the requiredProperty.
   */
  protected int getSupportedVersion( String property ) {
    return PropsUI.getInstance().getSupportedVersion( property );
  }

  /**
   * Ask if the browsing environment checks are disabled.
   *
   * @return 'true' if disabled 'false' otherwise.
   */
  public boolean isBrowserEnvironmentCheckDisabled() {
    return PropsUI.getInstance().isBrowserEnvironmentCheckDisabled();
  }

  /**
   * Ask for the running linux distribution.
   *
   * @return a string that contains the distribution name or a empty string if it could not find the name.
   */
  private String getLinuxDistribution() {
    Process p = null;
    try {
      p = ExecuteCommand( "lsb_release -d" );
    } catch ( IOException e ) {
      log.logError( "Could not execute command", e );
      return "";
    }
    BufferedReader in = getBufferedReaderFromProcess( p );
    try {
      return in.readLine();
    } catch ( IOException e ) {
      log.logError( "Could not read the distribution name", e );
      return "";
    }
  }

  protected Process ExecuteCommand( String command ) throws IOException {
    return Runtime.getRuntime().exec( command );
  }

  protected BufferedReader getBufferedReaderFromProcess( Process p ) {
    return new BufferedReader( new InputStreamReader( p.getInputStream() ) );
  }

  /**
   * Ask for the browser name.
   *
   * @return a String that contains the browser name.
   */
  public synchronized String getBrowserName() {
    final String userAgent = getUserAgent();
    if ( userAgent == null ) {
      return "";
    }
    if ( userAgent.contains( WINDOWS_BROWSER ) ) {
      return  WINDOWS_BROWSER;
    } else if ( userAgent.contains( UBUNTU_BROWSER ) ) {
      return  UBUNTU_BROWSER;
    } else if ( userAgent.contains( MAC_BROWSER ) ) {
      return MAC_BROWSER;
    }
    return "";
  }

}
