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

package org.pentaho.di.trans.steps.loadsave.setter;

import java.lang.reflect.Method;

public class MethodSetter<T> implements Setter<T> {
  private final Method method;

  public MethodSetter( Method method ) {
    this.method = method;
  }

  @Override
  public void set( Object obj, T value ) {
    try {
      method.invoke( obj, value );
    } catch ( Exception e ) {
      throw new RuntimeException( "Error invoking " + method + " on " + obj, e );
    }
  }

}
