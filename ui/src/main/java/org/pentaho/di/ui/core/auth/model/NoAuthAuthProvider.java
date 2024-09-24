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

public class NoAuthAuthProvider extends AbstractAuthProvider {
  public NoAuthAuthProvider( BindingFactory bf ) {
    super( bf );
  }

  public String getPrincipal() {
    return null;
  }

  public String getProviderDescription() {
    return "NoAuth";
  }

  @Override
  protected void addBindings( List<Binding> bindings, BindingFactory bf ) {

  }

  @Override
  public void bind() {

  }
}
