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

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingException;
import org.pentaho.ui.xul.binding.BindingFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAuthProvider extends XulEventSourceAdapter
    implements AuthProvider {

  private String principal;
  BindingFactory bf;
  private java.util.List<Binding> elementBindings = new ArrayList<Binding>();

  public AbstractAuthProvider( BindingFactory bf ) {
    this.bf = bf;
  }

  public String getPrincipal() {
    return this.principal;
  }

  public void setPrincipal( String principal ) {
    this.principal = principal;
  }

  public String getOverlay() {
    return "org/pentaho/di/ui/core/auth/xul/".concat( getProviderDescription().toLowerCase() ).concat( ".xul" );
  }

  @Override
  public void bind() throws BindingException, XulException, InvocationTargetException {

    unbind();
    elementBindings = new ArrayList<Binding>();

    Binding b = bf.createBinding( this, "principal", "principal", "value" );
    b.setBindingType( Binding.Type.BI_DIRECTIONAL );
    elementBindings.add( b );

    addBindings( elementBindings, bf );
    fireBindingsChanged();

  }

  protected abstract void addBindings( List<Binding> bindings, BindingFactory bf );

  @Override
  public void unbind() {

    for ( Binding bind : elementBindings ) {
      bind.destroyBindings();
    }
    elementBindings.clear();

  }

  @Override
  public void fireBindingsChanged() throws XulException, InvocationTargetException {

    for ( Binding bind : elementBindings ) {
      bind.fireSourceChanged();
    }
  }

  @Override
  public AuthProvider clone() throws CloneNotSupportedException {
    return (AuthProvider) super.clone();
  }

  public String toString() {
    return getProviderDescription();
  }

}
