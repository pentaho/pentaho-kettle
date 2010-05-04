/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.infobrightoutput;

import java.util.ArrayList;

import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogLevel;
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
      LogLevel logLevel = DefaultLogLevel.getLogLevel();
      DefaultLogLevel.setLogLevel(LogLevel.NOTHING);
      try {
        res = BaseMessages.getString(pName, key);
      }
      finally {
        DefaultLogLevel.setLogLevel(logLevel);
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
