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

/**
 * @author Infobright Inc.
 */
public class WindowsJNILibraryUtil {

  /**
   * adds kettle's libext to java.library.path so that we can pick up 
   * infobright_jni library.
   */
  public static void fixJavaLibraryPath() {
    String curLibPath = System.getProperty("java.library.path");
    if (curLibPath != null) {
      String libextPath = null;
      String[] paths = curLibPath.split(";");
      for (String path : paths) {
        if (path.contains("libswt\\win32")) {
          libextPath = path.replace("libswt\\win32", "libext");
        }
      }
      if (libextPath != null) {
        System.setProperty("java.library.path", curLibPath + ";" + libextPath);
      }
    }
 }
}
