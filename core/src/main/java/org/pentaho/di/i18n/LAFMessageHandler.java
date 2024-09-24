/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.i18n;

import org.pentaho.di.laf.BasePropertyHandler;

/**
 * @author dhushon
 *
 */
public class LAFMessageHandler extends GlobalMessages {

  private static String replace = "org.pentaho.di";
  private static String replaceWith = null;
  private static int offset = -1;
  private static String replaceSysBundle = null;

  static {
    replaceWith = BasePropertyHandler.getProperty( "LAFpackage" );
  }

  // TODO: modify base class to include a mandatory accessor so that this singleton instantiation can be
  // TODO: better controlled
  public LAFMessageHandler() {
    super();
    reinit();
  }

  public static synchronized MessageHandler getInstance() {
    if ( GMinstance == null ) {
      GMinstance = new LAFMessageHandler();
    }
    return GMinstance;
  }

  protected void reinit() {
    replaceWith = BasePropertyHandler.getProperty( "LAFpackage" );
    replaceSysBundle = replacePackage( SYSTEM_BUNDLE_PACKAGE );
    offset = -1;
  }

  /**
   * replace the application packagename target with ours for proper resolution e.g. replace org.pentaho.di.* with
   * pointers to new package structure
   *
   * @param packageName
   * @return
   */
  private String replacePackage( String packageName ) {
    // we haven't yet discovered the offset for the trim
    if ( offset < 0 ) {
      offset = packageName.indexOf( replace );
      if ( offset >= 0 ) {
        offset = replace.length();
      }
    }
    if ( replaceWith != null ) {
      return new String( replaceWith + packageName.substring( offset ) );
    }
    return packageName;
  }

  @Override
  protected String calculateString( String packageName, String key, Object[] parameters, Class<?> resourceClass ) {
    if ( replaceWith != null ) {
      final String[] pkgNames = new String[] { replacePackage( packageName ), replaceSysBundle };
      final String string = super.calculateString( pkgNames, key, parameters, resourceClass );
      if ( !GlobalMessageUtil.isMissingKey( string ) ) {
        return string;
      }
    }

    final String[] pkgNames = new String[] { packageName, SYSTEM_BUNDLE_PACKAGE };
    return GlobalMessageUtil.calculateString( pkgNames, key, parameters, resourceClass, BUNDLE_NAME, false );
  }
}
