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
package org.pentaho.di.job.entry.validator;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for getting formatted strings from validator resource bundle.
 *
 * @author mlowery
 */
public class ValidatorMessages {

  private static final String BUNDLE_NAME = "org.pentaho.di.job.entry.messages.validator"; //$NON-NLS-1$

  public static String getString(final String key, final Object... params) {
    return getStringFromBundle(BUNDLE_NAME, key, params);
  }

  public static String getStringFromBundle(final String bundleName, final String key, final Object... params) {
    ResourceBundle bundle = null;
    try {
      bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
    } catch (MissingResourceException e) {
      return "??? missing resource ???"; //$NON-NLS-1$
    } catch (NullPointerException e) {
      return "??? baseName null ???"; //$NON-NLS-1$
    }
    String unformattedString = null;
    try {
      unformattedString = bundle.getString(key);
    } catch (Exception e) {
      return "??? " + key + " ???"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    String formattedString = MessageFormat.format(unformattedString, params);
    return formattedString;
  }

}
