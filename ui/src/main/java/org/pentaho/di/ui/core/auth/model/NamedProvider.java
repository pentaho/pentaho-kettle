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
