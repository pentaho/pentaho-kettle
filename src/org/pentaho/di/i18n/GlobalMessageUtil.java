/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.di.i18n;


import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class GlobalMessageUtil {


  public static String formatErrorMessage(String key, String msg) {
    String s2 = key.substring(0, key.indexOf('.')+"ERROR_0000".length()+1); //$NON-NLS-1$ //$NON-NLS-2$
    return BaseMessages.getString("MESSUTIL.ERROR_FORMAT_MASK", s2, msg); //$NON-NLS-1$
  }

  public static String getString(ResourceBundle bundle, String key) throws MissingResourceException 
  {
    // try {
      return MessageFormat.format(bundle.getString(key), new Object[] {});
    // } catch (Exception e) {
    //   return '!' + key + '!';
    //}
  }

  public static String getErrorString(ResourceBundle bundle, String key) {
    return formatErrorMessage(key, getString(bundle, key));
  }

  public static String getString(ResourceBundle bundle, String key, String param1) {
    try {
      Object[] args = {param1};
      return MessageFormat.format(bundle.getString(key), args);      
    } catch (Exception e) {
      return '!' + key + '!';
    }
  }

  public static String getErrorString(ResourceBundle bundle, String key, String param1) {
    return formatErrorMessage(key, getString(bundle, key, param1));
  }

  public static String getString(ResourceBundle bundle, String key, String param1, String param2) {
    try {
      Object[] args = {param1, param2};
      return MessageFormat.format(bundle.getString(key), args);      
    } catch (Exception e) {
      return '!' + key + '!';
    }
  }
  
  public static String getErrorString(ResourceBundle bundle, String key, String param1, String param2) {
    return formatErrorMessage(key, getString(bundle, key, param1, param2));
  }

  public static String getString(ResourceBundle bundle, String key, String param1, String param2, String param3) {
    try {
      Object[] args = {param1, param2, param3};
      return MessageFormat.format(bundle.getString(key), args);      
    } catch (Exception e) {
      return '!' + key + '!';
    }
  }
  
  public static String getErrorString(ResourceBundle bundle, String key, String param1, String param2, String param3) {
    return formatErrorMessage(key, getString(bundle, key, param1, param2, param3));
  }

  public static String getString(ResourceBundle bundle, String key, String param1, String param2, String param3, String param4) {
    try {
      Object[] args = {param1, param2, param3, param4};
      return MessageFormat.format(bundle.getString(key), args);      
    } catch (Exception e) {
      return '!' + key + '!';
    }
  }
  
  public static String getString(ResourceBundle bundle, String key, String param1, String param2, String param3, String param4,String param5) {
	    try {
	      Object[] args = {param1, param2, param3, param4,param5};
	      return MessageFormat.format(bundle.getString(key), args);      
	    } catch (Exception e) {
	      return '!' + key + '!';
	    }
	  }
  
  public static String getString(ResourceBundle bundle, String key, String param1, String param2, String param3, String param4,String param5,String param6) {
	    try {
	      Object[] args = {param1, param2, param3, param4,param5,param6};
	      return MessageFormat.format(bundle.getString(key), args);      
	    } catch (Exception e) {
	      return '!' + key + '!';
	    }
	  }

  public static String getErrorString(ResourceBundle bundle, String key, String param1, String param2, String param3, String param4) {
    return formatErrorMessage(key, getString(bundle, key, param1, param2, param3, param4));
  }
  
  /*
  public static String formatMessage(String pattern) {
      try {
        Object[] args = {};
        return MessageFormat.format(pattern, args);      
      } catch (Exception e) {
        return '!' + pattern + '!';
      }
    }

  public static String formatMessage(String pattern, String param1) {
    try {
      Object[] args = {param1};
      return MessageFormat.format(pattern, args);      
    } catch (Exception e) {
      return '!' + pattern + '!';
    }
  }
  
  public static String formatMessage(String pattern, String param1, String param2) {
    try {
      Object[] args = {param1, param2};
      return MessageFormat.format(pattern, args);      
    } catch (Exception e) {
      return '!' + pattern + '!';
    }
    
  }
  
  public static String formatMessage(String pattern, String param1, String param2, String param3) {
    try {
      Object[] args = {param1, param2, param3};
      return MessageFormat.format(pattern, args);      
    } catch (Exception e) {
      return '!' + pattern + '!';
    }
  }

  public static String formatMessage(String pattern, String param1, String param2, String param3, String param4) {
    try {
      Object[] args = {param1, param2, param3, param4};
      return MessageFormat.format(pattern, args);      
    } catch (Exception e) {
      return '!' + pattern + '!';
    }
  }
  */
}