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
package org.pentaho.di.ui.spoon;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Static class wrapping Spoon's BaseMessages class and Look and Feel bundle. 
 * 
 * This wrapper is required for XUL portions of Spoon as the XUL system internationalizes 
 * via ResourceBundles.
 * 
 * @author nbaker
 */
public class XulSpoonResourceBundle extends ResourceBundle{
  private static Class<?> BASE_PKG = Spoon.class;
  private Class<?> PKG;
  
  public XulSpoonResourceBundle(final Class<?> PKG){
    this.PKG = PKG;
  }
  
  public XulSpoonResourceBundle(){
    this.PKG = BASE_PKG;
  }
  
  // If the requested key isn't found in the configured bundle, we fallback to the LAF bundle.
  private static ResourceBundle lafBundle;
  static{
    URL url = null;
    try{
      url = new File(".").toURI().toURL();
    } catch(MalformedURLException ex){}
    URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{url});
    
    lafBundle = ResourceBundle.getBundle("ui/laf", Locale.getDefault(), classLoader);
  }
  
  
  @Override
  public Enumeration<String> getKeys() {
    return null;
  }

  @Override
  protected Object handleGetObject(String key) {
    String result = BaseMessages.getString(PKG, key);
    
    // If not found check LAF bundle.
    if(result.indexOf('!') == 0 && result.lastIndexOf('!') == result.length() - 1){
      result = lafBundle.getString(key);
    }
    return result;
  }
  
}
