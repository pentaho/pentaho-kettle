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

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class NamedProvider extends XulEventSourceAdapter implements NamedModelObject<AuthProvider> {

  String name = null;

  AuthProvider provider = null;

  public NamedProvider( String name, AuthProvider provider ) {

    this.name = name;
    this.provider = provider;

  }

  public String getName() {
    return this.name;
  }

  public void setName( String name ) {
    String prev = this.name;
    this.name = name;
    firePropertyChange( "name", prev, this.name );
  }

  public void setItem( AuthProvider object ) {

    this.provider = object;
    try {
      provider.fireBindingsChanged();
    } catch ( Exception e ) {
      LogChannel.GENERAL.logError( "Binding event error while attempting to select provider.", e );
    }
  }

  public AuthProvider getItem() {
    return this.provider;

  }

  public String toString() {
    return this.name;
  }
}
