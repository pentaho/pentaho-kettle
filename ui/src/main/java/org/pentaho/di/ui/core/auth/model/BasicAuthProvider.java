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
  protected void addBindings( List<Binding> bindings, BindingFactory bf ) {

    Binding b = bf.createBinding( this, "password", "password", "value" );
    b.setBindingType( Binding.Type.BI_DIRECTIONAL );
    bindings.add( b );

  }

}
