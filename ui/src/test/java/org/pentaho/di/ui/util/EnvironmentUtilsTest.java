/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 - 2024 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.pentaho.di.ui.util.EnvironmentUtils.MAC_BROWSER;
import static org.pentaho.di.ui.util.EnvironmentUtils.UBUNTU_BROWSER;
import static org.pentaho.di.ui.util.EnvironmentUtils.WINDOWS_BROWSER;

import org.junit.Test;
import org.mockito.Mockito;
import java.io.BufferedReader;
import java.io.IOException;

public class EnvironmentUtilsTest {

  @Test
  public void testIsUnSupportedBrowserEnvironment_ubuntu() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.UBUNTU );
    assertTrue( mock.getMockedInstance().isUnsupportedBrowserEnvironment() );

    mock = new EnvironmentUtilsMock( Case.UBUNTU_WRONG );
    assertFalse( mock.getMockedInstance().isUnsupportedBrowserEnvironment() );
  }

  @Test
  public void testIsUnSupportedBrowserEnvironment_windows() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.WINDOWS );
    assertFalse( mock.getMockedInstance().isUnsupportedBrowserEnvironment() );

    mock = new EnvironmentUtilsMock( Case.WINDOWS_WRONG );
    assertTrue( mock.getMockedInstance().isUnsupportedBrowserEnvironment() );
  }

  @Test
  public void testIsUnSupportedBrowserEnvironment_mac() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.MAC_OS_X );
    assertFalse( mock.getMockedInstance().isUnsupportedBrowserEnvironment() );

    mock = new EnvironmentUtilsMock( Case.MAC_OS_X_WRONG );
    assertTrue( mock.getMockedInstance().isUnsupportedBrowserEnvironment() );
  }

  @Test
  public void testIsWebkitUnavailable_ubuntu() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.UBUNTU );
    assertFalse( mock.getMockedInstance().isWebkitUnavailable() );

    mock = new EnvironmentUtilsMock( Case.UBUNTU_WRONG );
    assertFalse( mock.getMockedInstance().isWebkitUnavailable() );
  }

  @Test
  public void testIsWebkitUnavailable_mac() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.MAC_OS_X );
    assertFalse( mock.getMockedInstance().isWebkitUnavailable() );
  }

  @Test
  public void testIsWebkitUnavailable_windows() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.WINDOWS );
    assertFalse( mock.getMockedInstance().isWebkitUnavailable() );
  }

  @Test
  public void testGetBrowserName_ubuntu() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.UBUNTU );
    assertEquals( UBUNTU_BROWSER, mock.getMockedInstance().getBrowserName() );
  }

  @Test
  public void testGetBrowserName_mac() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.MAC_OS_X );
    assertEquals( MAC_BROWSER, mock.getMockedInstance().getBrowserName() );

    mock = new EnvironmentUtilsMock( Case.MAC_OS_X_WRONG );
    assertEquals( MAC_BROWSER, mock.getMockedInstance().getBrowserName() );
  }

  @Test
  public void testGetBrowserName_windows() {
    EnvironmentUtilsMock mock = new EnvironmentUtilsMock( Case.WINDOWS );
    assertEquals( WINDOWS_BROWSER, mock.getMockedInstance().getBrowserName() );

    mock = new EnvironmentUtilsMock( Case.WINDOWS_WRONG );
    assertEquals("", mock.getMockedInstance().getBrowserName() );
  }

  class EnvironmentUtilsMock extends EnvironmentUtils {
    private final Case option;
    private static final String WEBKIT_PATH = "/path/mock/webkit";
    private static final String MAC_OS_X_NAME = "mac os x";
    private static final String UBUNTU_NAME = "ubuntu";
    private static final String UBUNTU_WRONG_NAME = "linux";
    private static final String WINDOWS_NAME = "windows";

    private static final String MIDORI_AGENT = "Mozilla/5.0 (X11; Linux) AppleWebKit/538.15 ("
      + "KHTML, like Gecko) Chrome/46.0.2490.86 Safari/538.15 Midori/0.5";
    private static final String SAFARI_7_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 "
      + "(KHTML, like Gecko) Version/7.0.3 Safari/7046A194A";
    private static final String SAFARI_9_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/601.75.14 "
            + "(KHTML, like Gecko) Version/9.3.2 Safari/537.75.14";
    private static final String IE_11_AGENT = "Mozilla/5.0 (compatible, MSIE 11, Windows NT 6.3; Trident/7.0;  rv:11.0) like Gecko";
    private static final String EDGE_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.2277.98";

    public EnvironmentUtilsMock( Case option ) {
      this.option = option;
    }

    public EnvironmentUtils getMockedInstance() {
      return this;
    }

    @Override
    String getWebkitPath() {
      switch ( option ) {
        case UBUNTU:
          return WEBKIT_PATH;
        case UBUNTU_WRONG:
          return "";
        default:
          return null;
      }
    }

    @Override
    String getOsName() {
      switch ( option ) {
        case UBUNTU_WRONG:
          return UBUNTU_WRONG_NAME;
        case UBUNTU:
          return UBUNTU_NAME;
        case MAC_OS_X:
        case MAC_OS_X_WRONG:
          return MAC_OS_X_NAME;
        case WINDOWS:
        case WINDOWS_WRONG:
          return WINDOWS_NAME;
        default:
          return null;
      }
    }

    @Override
    String getUserAgent() {
      switch ( option ) {
        case UBUNTU:
          return MIDORI_AGENT;
        case MAC_OS_X:
          return SAFARI_9_AGENT;
        case MAC_OS_X_WRONG:
          return SAFARI_7_AGENT;
        case WINDOWS:
          return EDGE_AGENT;
        case WINDOWS_WRONG:
          return IE_11_AGENT;
        default:
          return null;
      }
    }

    @Override
    int getSupportedVersion( String property ) {
      if ( property.contains( "min.mac.browser.supported" ) ) {
        return 601;
      } else if ( property.contains( "min.windows.browser.supported" ) ) {
        return 116;
      }

      return 0;
    }

    @Override
    Process executeLSBCommand() {
      return Mockito.mock( Process.class );
    }

    @Override
    BufferedReader getBufferedReaderFromProcess( Process p ) {
      BufferedReader bufferedReader = Mockito.mock( BufferedReader.class );
      try {
        switch ( option ) {
          case UBUNTU:
            when( bufferedReader.readLine() ).thenReturn( UBUNTU_NAME );
            break;
          case UBUNTU_WRONG:
            when( bufferedReader.readLine() ).thenReturn( UBUNTU_WRONG_NAME );
            break;
          default:
            when( bufferedReader.readLine() ).thenReturn( "" );
            break;
        }
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      return bufferedReader;
    }
  }

  enum Case {
    UBUNTU, UBUNTU_WRONG, MAC_OS_X, MAC_OS_X_WRONG, WINDOWS, WINDOWS_WRONG
  }

}
