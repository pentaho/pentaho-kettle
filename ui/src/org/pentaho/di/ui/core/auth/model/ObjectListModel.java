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
import org.pentaho.ui.xul.util.AbstractModelList;

@SuppressWarnings( {"unchecked", "rawtypes"} )
public class ObjectListModel extends XulEventSourceAdapter {
  private AbstractModelList<NamedModelObject> modelObjects = new AbstractModelList<NamedModelObject>();

  private NamedModelObject selected = null;
  private NamedModelObject selectedItem;
  private Object value;

  public AbstractModelList<NamedModelObject> getModelObjects() {
    return this.modelObjects;
  }

  public NamedModelObject getSelectedItem() {
    return this.selected;
  }

  public void setSelectedItem( NamedModelObject selected ) {
    NamedModelObject prev = this.selected;

    this.selected = selected;
    firePropertyChange( "selectedItem", prev, selected );
  }

  public void add( NamedModelObject item ) {
    this.modelObjects.add( item );
  }

  public void setName( String value ) {

    if ( selected != null ) {
      selected.setName( value );
    }

  }

  public String getName() {

    if ( this.selected != null ) {
      return this.selected.getName();
    }
    return "";

  }

  public void setItem( NamedModelObject selectedItem, Object value ) {

    selectedItem.setItem( value );

  }
}
