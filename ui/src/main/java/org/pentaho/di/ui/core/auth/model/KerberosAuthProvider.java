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
