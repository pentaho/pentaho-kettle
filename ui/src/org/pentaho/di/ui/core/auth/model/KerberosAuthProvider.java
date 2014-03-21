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

import java.util.List;

import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;

public class KerberosAuthProvider extends BasicAuthProvider {
  private boolean useKeytab;
  private String keytabFile;

  public KerberosAuthProvider( BindingFactory bf ) {
    super( bf );
  }

  public boolean isUseKeytab() {

    return this.useKeytab;

  }

  public void setUseKeytab( boolean useKeytab ) {

    this.useKeytab = useKeytab;

  }

  public String getKeytabFile() {

    return this.keytabFile;

  }

  public void setKeytabFile( String keytabFile ) {

    this.keytabFile = keytabFile;

  }

  public String getProviderDescription() {
    return "Kerberos";
  }

  @Override
  protected void addBindings( List<Binding> bindings, BindingFactory bf ) {

    super.addBindings( bindings, bf );

    Binding b = bf.createBinding( this, "keytabFile", "keytab", "value" );
    b.setBindingType( Binding.Type.BI_DIRECTIONAL );
    bindings.add( b );

    b = bf.createBinding( this, "useKeytab", "useKeytab", "checked" );
    b.setBindingType( Binding.Type.BI_DIRECTIONAL );
    bindings.add( b );

    b = bf.createBinding( "useKeytab", "checked", "keytab", "!disabled" );
    b.setBindingType( Binding.Type.ONE_WAY );
    bindings.add( b );

    b = bf.createBinding( "useKeytab", "checked", "browse", "!disabled" );
    b.setBindingType( Binding.Type.ONE_WAY );
    bindings.add( b );

    b = bf.createBinding( "useKeytab", "checked", "password", "disabled" );
    b.setBindingType( Binding.Type.ONE_WAY );
    bindings.add( b );

    b = bf.createBinding( "useKeytab", "checked", "principal", "disabled" );
    b.setBindingType( Binding.Type.ONE_WAY );
    bindings.add( b );

  }

}
