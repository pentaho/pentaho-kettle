/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Static class wrapping Spoon's BaseMessages class and Look and Feel bundle.
 *
 * This wrapper is required for XUL portions of Spoon as the XUL system internationalizes via ResourceBundles.
 *
 * @author nbaker
 */
public class XulSpoonResourceBundle extends ResourceBundle {
  private static Class<?> BASE_PKG = Spoon.class;
  private Class<?> PKG;

  public XulSpoonResourceBundle( final Class<?> PKG ) {
    this.PKG = PKG;
  }

  public XulSpoonResourceBundle() {
    this.PKG = BASE_PKG;
  }

  // If the requested key isn't found in the configured bundle, we fallback to the LAF bundle.
  private static ResourceBundle lafBundle;
  static {
    URL url = null;
    try {
      url = new File( "." ).toURI().toURL();
    } catch ( MalformedURLException ex ) {
      // Ignore errors
    }
    URLClassLoader classLoader = URLClassLoader.newInstance( new URL[] { url } );

    if ( Const.isRunningOnWebspoonMode() ) {
      lafBundle = ResourceBundle.getBundle( "ui/laf", Locale.getDefault() );
    } else {
      lafBundle = ResourceBundle.getBundle( "ui/laf", Locale.getDefault(), classLoader );
    }
  }

  @Override
  public Enumeration<String> getKeys() {
    return null;
  }

  @Override
  protected Object handleGetObject( String key ) {
    String result = BaseMessages.getString( PKG, key );

    // If not found check LAF bundle.
    if ( result.indexOf( '!' ) == 0 && result.lastIndexOf( '!' ) == result.length() - 1 ) {
      result = lafBundle.getString( key );
    }
    return result;
  }

}
