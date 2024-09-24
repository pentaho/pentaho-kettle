/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.i18n;

public class PackageMessages {

  private final Class<?> packageClass;
  private final String prefix;

  public PackageMessages( final Class<?> packageClass ) {
    this( packageClass, null );
  }

  public PackageMessages( final Class<?> packageClass, String prefix ) {
    this.packageClass = packageClass;
    this.prefix = prefix == null ? packageClass.getSimpleName() + "." : prefix;
  }

  public String getString( final String key, final String... parameters ) {
    return getString( prefix, key, parameters );
  }

  public String getString( final String prefix, final String key, final String... parameters ) {
    return BaseMessages.getString( packageClass, prefix + key, parameters );
  }
}
