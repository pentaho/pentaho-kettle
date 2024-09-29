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

package org.pentaho.di.ui.core.auth.controller;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.auth.model.AuthProvider;
import org.pentaho.di.ui.core.auth.model.BasicAuthProvider;
import org.pentaho.di.ui.core.auth.model.KerberosAuthProvider;
import org.pentaho.di.ui.core.auth.model.NamedModelObject;
import org.pentaho.di.ui.core.auth.model.NamedProvider;
import org.pentaho.di.ui.core.auth.model.NoAuthAuthProvider;
import org.pentaho.di.ui.core.auth.model.ObjectListModel;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulFileDialog;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

@SuppressWarnings( { "unchecked" } )
public class AuthProviderController extends AbstractXulEventHandler {

  protected BindingConvertor<NamedModelObject<NamedProvider>, String> selectedItemsNameBinding =
      new SelectedToStringConvertor();
  protected BindingConvertor<NamedModelObject<NamedProvider>, Object> selectedItemsItemBinding =
      new SelectedToItemConvertor();
  protected BindingConvertor<Collection<NamedModelObject<NamedProvider>>, Boolean> itemCountBinding =
      new RowCountToBooleanConvertor<NamedModelObject<NamedProvider>>();

  private XulDialog xulDialog;
  private BindingFactory bf;
  private ObjectListModel model = new ObjectListModel();

  AuthProvider activeProvider = null;

  private static LogChannelInterface log;

  ResourceBundle resourceBundle;

  public AuthProviderController() {

    log = new LogChannel( "AuthProviderController" );
    setName( "handler" );

  }

  public void setBindingFactory( BindingFactory bf ) {
    this.bf = bf;
  }

  public BindingFactory getBindingFactory() {
    return bf;
  }

  public void init() {

    xulDialog = ( (XulDialog) getXulDomContainer().getDocumentRoot().getRootElement() );

    if ( bf != null ) {
      bind();
    }

  }

  public void setResourceBundle( ResourceBundle res ) {

    resourceBundle = res;

  }

  public void open() {

    if ( xulDialog != null ) {
      xulDialog.show();
    }

  }

  /**
   * This will change to pull providers from the auth persistencemanager
   * 
   * @return Collection<AuthProvider>
   */
  public Collection<AuthProvider> getPossibleTypes() {
    ArrayList<AuthProvider> types = new ArrayList<AuthProvider>();

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
    // the combobox. Only use it to select the proper overlay, then
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

        try {

          current.bind();

        } catch ( Exception e ) {
          log.logError( resourceBundle.getString( "error.on_bind" ), e );
        }

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

      // Manage enabling/disabling layout based on item availability in the main authProvider list.
      bf.createBinding( model.getModelObjects(), "children", "remove_button", "disabled", itemCountBinding )
          .fireSourceChanged();
      bf.createBinding( model.getModelObjects(), "children", "name", "disabled", itemCountBinding ).fireSourceChanged();
      bf.createBinding( model.getModelObjects(), "children", "method_list", "disabled", itemCountBinding )
          .fireSourceChanged();

      // Manage enabling/disabling layout based on selection in the main authProvider list.
      bf.createBinding( "auth_list", "selectedItem", "name", "!disabled", BindingConvertor.object2Boolean() )
          .fireSourceChanged();
      bf.createBinding( "auth_list", "selectedItem", "method_list", "!disabled", BindingConvertor.object2Boolean() )
          .fireSourceChanged();
      bf.createBinding( "auth_list", "selectedItem", "remove_button", "!disabled", BindingConvertor.object2Boolean() )
          .fireSourceChanged();

      bf.setBindingType( Binding.Type.BI_DIRECTIONAL );

      // When an authorization entry is selected, select entry in model
      bf.createBinding( "auth_list", "selectedItem", this.model, "selectedItem" );

      // Syncs elements in the model and lists them in the authorization entry list
      Binding listBinding = this.bf.createBinding( this.model.getModelObjects(), "children", "auth_list", "elements" );
      listBinding.fireSourceChanged();

      // Update the entry name textbox when a new entry is selected in the authorization entry list
      bf.createBinding( this.model, "selectedItem", "name", "value", selectedItemsNameBinding ).fireSourceChanged();

      // Change the overlay when the user changes the "Method" in the method combobox
      bf.createBinding( this, "newOverlay", "method_list", "selectedItem" ).fireSourceChanged();

      // Update the method combobox with the appropriate selection for the selected authorization entry
      bf.createBinding( this.model, "selectedItem", "method_list", "selectedItem", selectedItemsItemBinding )
          .fireSourceChanged();

      // Because the programmatic selection of the item in the combobox does not fire events, we need
      // to bind the main authProvider selection to the changing of the overlay
      bf.createBinding( this.model, "selectedItem", this, "newOverlay", selectedItemsItemBinding );

    } catch ( XulException e ) {
      log.logError( resourceBundle.getString( "error.on_bind" ), e );
    } catch ( InvocationTargetException e ) {
      log.logError( resourceBundle.getString( "error.on_execution" ), e );
    }
  }

  public void onAccept() {

    // save model via PersistenceManager here ...

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

    String name = resourceBundle.getString( "uniquename.provider" );
    int index = 0;
    boolean good = false;

    String potentialName = null;

    while ( !good ) {
      potentialName = name.concat( Integer.toString( ++index ) );
      boolean found = false;
      for ( NamedModelObject<NamedProvider> o : model.getModelObjects() ) {
        if ( o.getName().equalsIgnoreCase( potentialName ) ) {
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
      log.logError( resourceBundle.getString( "error.file_browse" ), e );
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

  private class SelectedToItemConvertor extends BindingConvertor<NamedModelObject<NamedProvider>, Object> {
    private SelectedToItemConvertor() {
    }

    public Object sourceToTarget( NamedModelObject<NamedProvider> value ) {
      if ( value == null ) {
        return null;
      }
      return value.getItem();
    }

    public NamedModelObject<NamedProvider> targetToSource( Object value ) {
      if ( model.getSelectedItem() != null ) {

        AuthProvider provider = (AuthProvider) value;
        AuthProvider selectedProvider = (AuthProvider) model.getSelectedItem().getItem();

        AuthProvider providerToUse = null;
        if ( selectedProvider.getOverlay().equals( provider.getOverlay() ) ) {
          providerToUse = selectedProvider;
        } else {
          // Clone the provider... the one passed in is the provider used in the method
          // combobox... we don't want that instance in the main list.
          try {

            providerToUse = provider.clone();
            providerToUse.bind();

          } catch ( Exception e ) {
            log.logError( resourceBundle.getString( "error.new_provider" ), e );
          }
        }
        model.setItem( model.getSelectedItem(), providerToUse );

      }
      return model.getSelectedItem();
    }
  }

  private class SelectedToStringConvertor extends BindingConvertor<NamedModelObject<NamedProvider>, String> {
    private SelectedToStringConvertor() {
    }

    public String sourceToTarget( NamedModelObject<NamedProvider> value ) {
      if ( value == null ) {
        return "";
      }
      return value.getName();
    }

    public NamedModelObject<NamedProvider> targetToSource( String value ) {
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
   * 
   * @return ObjectListModel
   */
  ObjectListModel getModel() {
    return model;
  }
}
