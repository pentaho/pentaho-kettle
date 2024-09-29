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
