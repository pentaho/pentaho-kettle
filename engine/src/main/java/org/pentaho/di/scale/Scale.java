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


package org.pentaho.di.scale;

import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;

public class Scale {

  private static Class<?> PKG = Scale.class; // for i18n purposes, needed by Translator2!!

  public static void main( String[] args ) throws Exception {

    // startup Scale ( a.k.a. PDI minimal/headless environment )
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.SCALE );
    KettleEnvironment.init();
  }
}
