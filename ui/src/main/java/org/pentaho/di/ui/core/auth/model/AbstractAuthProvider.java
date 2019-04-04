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
