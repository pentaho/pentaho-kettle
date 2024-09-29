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
