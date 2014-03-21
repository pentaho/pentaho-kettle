/*
 * !
 *  * This program is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  * Foundation.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this
 *  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  * or from the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Lesser General Public License for more details.
 *  *
 *  * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 *
 */

package org.pentaho.di.ui.core.auth.model;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
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
  public void bind() {

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
  public void fireBindingsChanged() {

    for ( Binding bind : elementBindings ) {
      try {
        bind.fireSourceChanged();
      } catch ( XulException e ) {
        e.printStackTrace();
      } catch ( InvocationTargetException e ) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public AuthProvider clone() {

    AuthProvider provider = null;

    try {
      provider = (AuthProvider) super.clone();
      provider.bind();
    } catch ( CloneNotSupportedException e ) {
      e.printStackTrace();
    }

    return provider;
  }

  public String toString() {
    return getProviderDescription();
  }

}
