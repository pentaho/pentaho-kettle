/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.ui.util;

import java.lang.reflect.Method;

import org.pentaho.ui.database.Messages;

public class Launch {

  public enum Status {
    Success, Failed
  }

  public Status openURL( String url ) {

    Status r = Status.Success;
    String osName = System.getProperty( "os.name" );

    try {
      if ( osName.startsWith( "Mac OS" ) ) {
        Class<?> fileMgr = Class.forName( "com.apple.eio.FileManager" );
        Method openURL = fileMgr.getDeclaredMethod( "openURL", new Class<?>[] { String.class } );
        openURL.invoke( null, new Object[] { url } );
      } else if ( osName.startsWith( "Windows" ) ) {
        Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler " + url );
      } else { // assume Unix or Linux
        String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
        String browser = null;
        for ( int count = 0; count < browsers.length && browser == null; count++ ) {
          if ( Runtime.getRuntime().exec( new String[] { "which", browsers[count] } ).waitFor() == 0 ) {
            browser = browsers[count];
          }
        }
        if ( browser == null ) {
          throw new Exception( Messages.getString( "Launch.ERROR_0001_BROWSER_NOT_FOUND" ) );
        } else {
          Runtime.getRuntime().exec( new String[] { browser, url } );
        }
      }
    } catch ( Exception e ) {
      r = Status.Failed;
    }
    return r;
  }

}
