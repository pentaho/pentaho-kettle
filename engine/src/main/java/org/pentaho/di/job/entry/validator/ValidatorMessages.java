/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

  private static final String BUNDLE_NAME = "org.pentaho.di.job.entry.messages.validator";

  public static String getString( final String key, final Object... params ) {
    return getStringFromBundle( BUNDLE_NAME, key, params );
  }

  public static String getStringFromBundle( final String bundleName, final String key, final Object... params ) {
    ResourceBundle bundle = null;
    try {
      bundle = ResourceBundle.getBundle( bundleName, Locale.getDefault() );
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
