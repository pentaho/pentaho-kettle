package org.pentaho.di.trans.steps.infobrightoutput;

import java.util.ArrayList;

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.BaseMessages;

/**
 * @author Infobright Inc.
 */
public abstract class AbstractMessages {
  protected ArrayList<String> packageNames = new ArrayList<String>();

  public void addPackage(Package packageObj) {
    packageNames.add(packageObj.getName());
  }
  
  public String get(String key, String ... params) {
    String res = null;
    String notFoundKey = "!" + key + "!";
    
    for (String pName : packageNames) {
      // Kettle will generate an exception message if there is a
      // failed message search. Since we are searching over multiple
      // packages, we don't want this message generated unless we
      // cannot find the message in any of the packages.
      int logLevel = LogWriter.getInstance().getLogLevel();
      LogWriter.getInstance().setLogLevel(0);
      try {
        res = BaseMessages.getString(pName, key);
      }
      finally {
        LogWriter.getInstance().setLogLevel(logLevel);
      }
      if (!res.equals(notFoundKey)) {
        return res;
      }
    }
    
    // This means we did not find the key, so let Kettle generate
    // its normal error.
    return BaseMessages.getString(packageNames.get(0), key);
  }  
}
