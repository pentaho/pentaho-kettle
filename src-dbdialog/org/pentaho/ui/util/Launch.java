///////////////////////////////////////////////////////// 
// Bare Bones Browser Launch                           // 
// Version 1.5 (December 10, 2005)                     // 
// By Dem Pilafian                                     // 
// Supports: Mac OS X, GNU/Linux, Unix, Windows XP     // 
// Example Usage:                                      // 
// String url = "http://www.centerkey.com/";           // 
// BareBonesBrowserLaunch.openURL(url);                // 
// Public Domain Software -- Free to Use as You Like   // 
/////////////////////////////////////////////////////////
package org.pentaho.ui.util;

import java.lang.reflect.Method;

import org.pentaho.ui.database.Messages;

public class Launch {

  public enum Status {
    Success, Failed
  };

  public static Status openURL(String url) {

    Status r = Status.Success;
    String osName = System.getProperty("os.name"); //$NON-NLS-1$

    try {
      if (osName.startsWith("Mac OS")) { //$NON-NLS-1$
        Class <?> fileMgr = Class.forName("com.apple.eio.FileManager"); //$NON-NLS-1$
        Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class }); //$NON-NLS-1$
        openURL.invoke(null, new Object[] { url });
      } else if (osName.startsWith("Windows")){ //$NON-NLS-1$
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url); //$NON-NLS-1$
      } else { //assume Unix or Linux
        String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        String browser = null;
        for (int count = 0; count < browsers.length && browser == null; count++){
          if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0){ //$NON-NLS-1$
            browser = browsers[count];
          }
        }
        if (browser == null){
          throw new Exception(Messages.getString("Launch.ERROR_0001_BROWSER_NOT_FOUND")); //$NON-NLS-1$
        }else{
          Runtime.getRuntime().exec(new String[] { browser, url });
        }
      }
    } catch (Exception e) {
      r = Status.Failed;
    }
    return r;
  }

}
