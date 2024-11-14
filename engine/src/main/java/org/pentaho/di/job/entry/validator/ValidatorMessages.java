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


package org.pentaho.di.job.entry.validator;

import org.pentaho.di.i18n.GlobalMessageUtil;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for getting formatted strings from validator resource bundle.
 *
 * @author mlowery
 */
public class ValidatorMessages {

  private static final String BUNDLE_NAME = "org.pentaho.di.job.entry.messages.validator";

  public static String getString( final String key, final Object... params ) {
    return getStringFromBundle( BUNDLE_NAME, key, params );
  }

  public static String getStringFromBundle( final String bundleName, final String key, final Object... params ) {
    ResourceBundle bundle = null;
    try {
      bundle = GlobalMessageUtil.getBundle( bundleName, ValidatorMessages.class );
    } catch ( MissingResourceException e ) {
      return "??? missing resource ???";
    } catch ( NullPointerException e ) {
      return "??? baseName null ???";
    }
    String unformattedString = null;
    try {
      unformattedString = bundle.getString( key );
    } catch ( Exception e ) {
      return "??? " + key + " ???";
    }
    String formattedString = MessageFormat.format( unformattedString, params );
    return formattedString;
  }

}
