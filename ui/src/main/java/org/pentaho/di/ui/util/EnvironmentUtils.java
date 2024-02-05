/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 - 2024 by Hitachi Vantara : http://www.pentaho.com
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

import com.cronutils.utils.VisibleForTesting;
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
  private static final EnvironmentUtils instance = new EnvironmentUtils();

  private static final Pattern EDGE_PATTERN = Pattern.compile( "Edge?/(\\d+)" );
  private static final Pattern SAFARI_PATTERN = Pattern.compile( "AppleWebKit/(\\d+)" );

  private static final String SUPPORTED_DISTRIBUTION_NAME = "ubuntu";
  public static final String UBUNTU_BROWSER = "Midori";
  public static final String MAC_BROWSER = "Safari";
  public static final String WINDOWS_BROWSER = "Edge";
  private final LogChannelInterface log = new LogChannel( this );

  @VisibleForTesting
  EnvironmentUtils() {}

  public static EnvironmentUtils getInstance() {
    return instance;
  }

  /**
   * Checks the available browser to see if it is an unsupported one.
   *
   * @return 'true' if in a unSupported browser environment 'false' otherwise.
   */
  public synchronized boolean isUnsupportedBrowserEnvironment() {
    String environment = getEnvironmentName();
    if ( environment.contains( "linux" ) ) {
      return false;
    }

    final String userAgent = getUserAgent();
    if ( userAgent == null ) {
      return true;
    }

    if ( environment.contains( "windows" ) ) {
      Matcher edgeMatcher = EDGE_PATTERN.matcher( userAgent );
      int edgeMinVersion = getSupportedVersion( "min.windows.browser.supported" );

      return !edgeMatcher.find() || checkUserAgent( edgeMatcher, edgeMinVersion );
    }

    return checkUserAgent( SAFARI_PATTERN.matcher( userAgent ), getSupportedVersion( "min.mac.browser.supported" ) );
  }

  private boolean checkUserAgent( Matcher matcher, int version ) {
    return matcher.find() && Integer.parseInt( matcher.group( 1 ) ) < version;
  }

  /**
   * Ask for user Agent of the available browser.
   *
   * @return a string that contains the user agent of the browser.
   */
  @VisibleForTesting
  String getUserAgent() {
    Browser browser;

    try {
      browser = new Browser( new Shell(), SWT.NONE );
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

    return  ( path == null || !path.contains("webkit") ) && osName.contains( SUPPORTED_DISTRIBUTION_NAME );
  }

  /**
   * Ask for the path in the system for the webkit library.
   *
   * @return a string that contains the path or 'null' if not found.
   */
  @VisibleForTesting
  String getWebkitPath() {
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

  @VisibleForTesting
  String getOsName() {
    return System.getProperty( "os.name" ).toLowerCase();
  }

  /**
   * Gets the supported version of the required Property.
   *
   * @param property a string with the required property.
   * @return the value of the requiredProperty.
   */
  @VisibleForTesting
  int getSupportedVersion( String property ) {
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
   * @return a string that contains the distribution name or an empty string if it could not find the name.
   */
  private String getLinuxDistribution() {
    Process command;

    try {
      command = executeLSBCommand();
    } catch ( IOException e ) {
      log.logError( "Could not execute command", e );
      return "";
    }

    BufferedReader in = getBufferedReaderFromProcess( command );
    try {
      return in.readLine();
    } catch ( IOException e ) {
      log.logError( "Could not read the distribution name", e );
      return "";
    }
  }

  @VisibleForTesting
  Process executeLSBCommand() throws IOException {
    return Runtime.getRuntime().exec( "lsb_release -d" );
  }

  @VisibleForTesting
  BufferedReader getBufferedReaderFromProcess( Process process ) {
    return new BufferedReader( new InputStreamReader( process.getInputStream() ) );
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

    String edgeUserAgent = WINDOWS_BROWSER.substring( 0, 3 ); // Edg
    if ( userAgent.contains( edgeUserAgent ) ) {
      return  WINDOWS_BROWSER;
    } else if ( userAgent.contains( UBUNTU_BROWSER ) ) {
      return  UBUNTU_BROWSER;
    } else if ( userAgent.contains( MAC_BROWSER ) ) {
      return MAC_BROWSER;
    }

    return "";
  }
}
