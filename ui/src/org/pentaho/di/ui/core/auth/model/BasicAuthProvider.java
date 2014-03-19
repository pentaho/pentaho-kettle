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

import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;

import java.util.List;

public class BasicAuthProvider extends AbstractAuthProvider {

  private String password;

  public BasicAuthProvider( BindingFactory bf ) {
    super( bf );
  }

  public String getPassword() {

    return this.password;

  }

  public void setPassword( String password ) {

    this.password = password;

  }

  public String getProviderDescription() {

    return "Basic";

  }

  @Override
  protected void addBindings( List<Binding> bindings,  BindingFactory bf ) {

      Binding b = bf.createBinding(  this, "password", "password", "value" );
      b.setBindingType( Binding.Type.BI_DIRECTIONAL );
      bindings.add( b );

  }

}
