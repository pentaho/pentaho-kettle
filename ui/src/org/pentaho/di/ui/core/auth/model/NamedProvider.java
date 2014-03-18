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

public class NamedProvider extends XulEventSourceAdapter implements NamedModelObject<AuthProvider> {

  String name = null;

  AuthProvider provider = null;

  public NamedProvider( String name, AuthProvider provider ) {

    this.name = name;
    this.provider = provider;

  }

  public String getName()
  {
    return this.name;
  }

  public void setName( String name )
  {
    String prev = this.name;
    this.name = name;
    firePropertyChange( "name", prev, this.name );
  }

  public void setItem( AuthProvider object )
  {

    this.provider = object;
    provider.fireBindingsChanged();
  }

  public AuthProvider getItem()
  {
    return this.provider;

  }

  public String toString()
  {
    return this.name;
  }
}
