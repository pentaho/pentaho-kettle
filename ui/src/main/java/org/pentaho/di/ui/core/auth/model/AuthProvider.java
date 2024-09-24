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

package org.pentaho.di.ui.core.auth.model;

import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingException;

import java.lang.reflect.InvocationTargetException;

public abstract interface AuthProvider extends Cloneable {
  public abstract String getPrincipal();

  public abstract void setPrincipal( String paramString );

  public abstract String getProviderDescription();

  public abstract String getOverlay();

  public void bind() throws BindingException, XulException, InvocationTargetException;

  public void unbind();

  public AuthProvider clone() throws CloneNotSupportedException;

  public void fireBindingsChanged() throws XulException, InvocationTargetException;
}
