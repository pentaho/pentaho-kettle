/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;

@SuppressWarnings( { "unchecked", "rawtypes" } )
public class ObjectListModel extends XulEventSourceAdapter {
  private AbstractModelList<NamedModelObject> modelObjects = new AbstractModelList<NamedModelObject>();

  private NamedModelObject selected = null;
  protected NamedModelObject selectedItem;
  protected Object value;

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
