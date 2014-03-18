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

package org.pentaho.di.ui.core.auth.controller;

import org.pentaho.di.ui.core.auth.model.AuthProvider;
import org.pentaho.di.ui.core.auth.model.BasicAuthProvider;
import org.pentaho.di.ui.core.auth.model.KerberosAuthProvider;
import org.pentaho.di.ui.core.auth.model.NamedModelObject;
import org.pentaho.di.ui.core.auth.model.NamedProvider;
import org.pentaho.di.ui.core.auth.model.NoAuthAuthProvider;
import org.pentaho.di.ui.core.auth.model.ObjectListModel;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulFileDialog;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthProviderController extends AbstractXulEventHandler {

  private static String XUL_FILE = "org/pentaho/di/ui/core/auth/xul/authManager.xul";

  protected BindingConvertor<NamedModelObject, String> selectedItemsNameBinding = new SelectedToStringConvertor();
  protected BindingConvertor<NamedModelObject, Object> selectedItemsItemBinding = new SelectedToItemConvertor();
  protected BindingConvertor<Collection<NamedModelObject>, Boolean> itemCountBinding = new RowCountToBooleanConvertor();

  private XulDialog xulDialog;
  private BindingFactory bf;
  private XulLoader loader;
  private XulRunner runner;
  private ObjectListModel model = new ObjectListModel();

  AuthProvider activeProvider = null;

  public AuthProviderController( XulLoader loader, BindingFactory bindingFactory, XulRunner runner ) {
    this.bf = bindingFactory;
    this.loader = loader;
    this.runner = runner;

    setName( "handler" );

    init();
    bind();
  }

  private boolean init() {
    boolean success = true;

    try {

      setXulDomContainer( loader.loadXul( XUL_FILE, null ) );
      bf.setDocument( getXulDomContainer().getDocumentRoot() );
      getXulDomContainer().addEventHandler( this );
      runner.addContainer( getXulDomContainer() );
      xulDialog = ( (XulDialog) getXulDomContainer().getDocumentRoot().getRootElement() );
      runner.initialize();

    } catch ( XulException e ) {
      success = false;
    }

    return success;
  }

  public void open() {
    xulDialog.show();
  }

  public Collection<AuthProvider> getPossibleTypes() {
    ArrayList types = new ArrayList();

    types.add( new NoAuthAuthProvider( bf ) );
    types.add( new KerberosAuthProvider( bf ) );
    types.add( new BasicAuthProvider( bf ) );

    return types;
  }

  public void setNewOverlay( AuthProvider provider ) throws XulException {

    if ( provider == null ) {
      provider = new NoAuthAuthProvider( bf );
    }
    // Don't use this provider... it is the one that is created to populate
    // the combobox. Only use it to select the proper overly, then
    // bind the provider associated with the NamedObject selected
    // in the main authProvider list.

    if ( this.activeProvider != null ) {
      getXulDomContainer().removeOverlay( activeProvider.getOverlay() );
    }

    getXulDomContainer().loadOverlay( provider.getOverlay() );

    if ( model.getSelectedItem() != null ) {
      AuthProvider current = (AuthProvider) model.getSelectedItem().getItem();

      // Only bind the selected provider if it matches the overlay... the selected
      // provider may not have been updated and cloned yet.

      if ( current.getOverlay().equalsIgnoreCase( provider.getOverlay() ) ) {
        current.bind();
      }
    }

    this.activeProvider = provider;
  }

  public String getNewOverlay() {
    return "";
  }

  private void bind() {
    try {
      this.bf.setBindingType( Binding.Type.ONE_WAY );

      // Loads the authorization types into the "Method" combobox
      bf.createBinding( this, "possibleTypes", "method_list", "elements" ).fireSourceChanged();

      // When an authorization entry is selected, select entry in model
      bf.createBinding( "auth_list", "selectedItem", this.model, "selectedItem" );

      // Manage enabling/disabling layout based on item availability in the main authProvider list.
      bf.createBinding( model.getModelObjects(), "children", "remove_button", "disabled", itemCountBinding ).fireSourceChanged();
      bf.createBinding( model.getModelObjects(), "children", "name", "disabled", itemCountBinding ).fireSourceChanged();
      bf.createBinding( model.getModelObjects(), "children", "method_list", "disabled", itemCountBinding ).fireSourceChanged();

      // Manage enabling/disabling layout based on selection in the main authProvider list.
      bf.createBinding( "auth_list", "selectedItem", "name", "!disabled", BindingConvertor.object2Boolean() ).fireSourceChanged();
      bf.createBinding( "auth_list", "selectedItem", "method_list", "!disabled", BindingConvertor.object2Boolean() ).fireSourceChanged();
      bf.createBinding( "auth_list", "selectedItem", "remove_button", "!disabled", BindingConvertor.object2Boolean() ).fireSourceChanged();

      bf.setBindingType( Binding.Type.BI_DIRECTIONAL );

      // Syncs elements in the model and lists them in the authorization entry list
      Binding listBinding = this.bf.createBinding( this.model.getModelObjects(), "children", "auth_list", "elements" );
      listBinding.fireSourceChanged();

      // Update the entry name textbox when a new entry is selected in the authorization entry list
      bf.createBinding( this.model, "selectedItem", "name", "value", selectedItemsNameBinding ).fireSourceChanged();

      // Change the overlay when the user changes the "Method" in the method combobox
      bf.createBinding( this, "newOverlay", "method_list", "selectedItem" ).fireSourceChanged();

      // Update the method combobox with the appropriate selection for the selected authorization entry
      bf.createBinding( this.model, "selectedItem", "method_list", "selectedItem", selectedItemsItemBinding ).fireSourceChanged();

      // Because the programmatic selection of the item in the combobox does not fire events, we need
      // to bind the main authProvider selection to the changing of the overlay
      bf.createBinding( this.model, "selectedItem", this, "newOverlay", selectedItemsItemBinding );


    } catch ( XulException e ) {
      e.printStackTrace();
    } catch ( InvocationTargetException e ) {
      e.printStackTrace();
    }
  }

  public void onAccept() {
    this.xulDialog.hide();
  }

  public void onCancel() {
    this.xulDialog.hide();
  }

  public void addNew() {

    NamedProvider provider = new NamedProvider( generateUniqueName(), new NoAuthAuthProvider( bf ) );
    this.model.add( provider );
    this.model.setSelectedItem( provider );

  }

  private String generateUniqueName() {

    String name = "Provider";
    int index = 0;
    boolean good = false;

    String potentialName = null;

    while ( !good ){
      potentialName = name.concat( Integer.toString( ++index ) );
      boolean found = false;
      for(  NamedModelObject o : model.getModelObjects() ) {
        if ( o.getName().equalsIgnoreCase( potentialName ) ){
          found = true;
          break;
        }
      }
      good = !found;
    }
    return potentialName;

  }

  public void remove() {

    int index = this.model.getModelObjects().indexOf( this.model.getSelectedItem() );

    if ( index >= 1 ) {
      index -= 1;
    }

    this.model.getModelObjects().remove( this.model.getSelectedItem() );

    if ( !model.getModelObjects().isEmpty() ) {
      this.model.setSelectedItem( model.getModelObjects().get( index ) );
    } else {
      this.model.setSelectedItem( null );
    }

  }

  public void browse() {

    try {
      XulTextbox filename = (XulTextbox) document.getElementById( "keytab" );

      XulFileDialog dialog = (XulFileDialog) document.createElement( "filedialog" );
      XulFileDialog.RETURN_CODE retval = dialog.showOpenDialog();

      if ( retval == XulFileDialog.RETURN_CODE.OK ) {
        File file = (File) dialog.getFile();
        filename.setValue( file.getAbsolutePath() );
      }

    } catch ( XulException e ) {
      System.out.println( "Error creating file dialog" );
    }

  }

  public void addProviders( List<NamedProvider> providers ) {

    if ( providers == null || providers.isEmpty() ) {
      return;
    }

    for ( NamedProvider provider : providers ) {
      model.add( provider );
    }

    model.setSelectedItem( model.getModelObjects().get( 0 ) );

  }

  private class SelectedToItemConvertor extends BindingConvertor<NamedModelObject, Object> {
    private SelectedToItemConvertor() {
    }

    public Object sourceToTarget( NamedModelObject value ) {
      if ( value == null ) {
        return null;
      }
      return value.getItem();
    }

    public NamedModelObject targetToSource( Object value ) {
      if ( model.getSelectedItem() != null ) {

        AuthProvider provider = (AuthProvider) value;
        AuthProvider selectedProvider = (AuthProvider) model.getSelectedItem().getItem();

        AuthProvider providerToUse = null;
        if ( selectedProvider.getOverlay().equals( provider.getOverlay() ) ) {
          providerToUse = selectedProvider;
        } else {
          // Clone the provider... the one passed in is the provider used in the method
          // combobox... we don't want that instance in the main list.
          providerToUse = provider.clone();
        }
        model.setItem( model.getSelectedItem(), providerToUse );

      }
      return model.getSelectedItem();
    }
  }

  private class SelectedToStringConvertor extends BindingConvertor<NamedModelObject, String> {
    private SelectedToStringConvertor() {
    }

    public String sourceToTarget( NamedModelObject value ) {
      if ( value == null ) {
        return "";
      }
      return value.getName();
    }

    public NamedModelObject targetToSource( String value ) {
      if ( model.getSelectedItem() != null ) {
        model.setName( value );
      }
      return model.getSelectedItem();
    }
  }

  private class RowCountToBooleanConvertor<T> extends BindingConvertor<Collection<T>, Boolean> {

    @Override
    public Boolean sourceToTarget( Collection<T> value ) {

      return ( value == null ) || ( value.isEmpty() );

    }

    @Override
    public Collection<T> targetToSource( Boolean value ) {
      return null;
    }
  }

  /**
   * Exposed only for junit tests
   * @return ObjectListModel
   */
  ObjectListModel getModel(){
    return model;
  }
}
