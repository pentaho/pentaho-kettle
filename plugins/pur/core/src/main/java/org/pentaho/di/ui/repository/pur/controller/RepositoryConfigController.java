/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.pur.PurRepositoryLocation;
import org.pentaho.di.repository.pur.PurRepositoryMeta;
import org.pentaho.di.ui.repository.pur.IRepositoryConfigDialogCallback;
import org.pentaho.di.ui.repository.pur.PurRepositoryDialog;
import org.pentaho.di.ui.repository.pur.model.RepositoryConfigModel;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class RepositoryConfigController extends AbstractXulEventHandler implements java.io.Serializable {

  private static final long serialVersionUID = 1882563488501980590L; /* EESOURCE: UPDATE SERIALVERUID */

  public String getName() {
    return "repositoryConfigController"; //$NON-NLS-1$
  }

  public static final String PLUGIN_PROPERTIES_FILE = "plugins/pdi-pur-plugin/plugin.properties"; //$NON-NLS-1$
  public static final String DEFAULT_URL = "default-url"; //$NON-NLS-1$
  private XulDialog repositoryConfigDialog;
  private XulTextbox url;
  private XulTextbox name;
  private XulTextbox id;
  private XulCheckbox modificationComments;
  private XulButton okButton;
  private RepositoryConfigModel model;
  private BindingFactory bf;
  private IRepositoryConfigDialogCallback callback;
  private RepositoryMeta repositoryMeta;
  private ResourceBundle messages;
  private XulMessageBox messageBox;

  public RepositoryConfigController() {

  }

  public void init() throws ControllerInitializationException {
    bf = new DefaultBindingFactory();
    bf.setDocument( this.getXulDomContainer().getDocumentRoot() );
    try {
      messageBox = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$
    } catch ( Throwable th ) {
      throw new ControllerInitializationException( th );
    }
    model = new RepositoryConfigModel();
    if ( bf != null ) {
      createBindings();
    }
    initializeModel();
  }

  private void createBindings() {
    repositoryConfigDialog = (XulDialog) document.getElementById( "repository-config-dialog" );//$NON-NLS-1$
    url = (XulTextbox) document.getElementById( "repository-url" );//$NON-NLS-1$
    name = (XulTextbox) document.getElementById( "repository-name" );//$NON-NLS-1$
    id = (XulTextbox) document.getElementById( "repository-id" );//$NON-NLS-1$
    modificationComments = (XulCheckbox) document.getElementById( "repository-modification-comments" );//$NON-NLS-1$
    okButton = (XulButton) document.getElementById( "repository-config-dialog_accept" ); //$NON-NLS-1$
    bf.setBindingType( Type.BI_DIRECTIONAL );
    bf.createBinding( model, "url", url, "value" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( model, "name", name, "value" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( model, "id", id, "value" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( model, "modificationComments", modificationComments, "checked" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType( Type.ONE_WAY );
    bf.createBinding( model, "valid", okButton, "!disabled" );//$NON-NLS-1$ //$NON-NLS-2$
  }

  public void ok() {
    if ( repositoryMeta instanceof PurRepositoryMeta ) {
      repositoryMeta.setName( model.getName() );
      repositoryMeta.setDescription( model.getId() );
      // remove trailing slash
      String url = model.getUrl();
      String urlTrim = url.endsWith( "/" ) ? url.substring( 0, url.length() - 1 ) : url;
      PurRepositoryLocation location = new PurRepositoryLocation( urlTrim );
      ( (PurRepositoryMeta) repositoryMeta ).setRepositoryLocation( location );
      ( (PurRepositoryMeta) repositoryMeta ).setVersionCommentMandatory( model.isModificationComments() );
      getCallback().onSuccess( ( (PurRepositoryMeta) repositoryMeta ) );
    } else {
      getCallback().onError(
          new IllegalStateException( BaseMessages.getString( PurRepositoryDialog.class,
              "RepositoryConfigDialog.ERROR_0001_NotAnInstanceOfPurRepositoryMeta" ) ) ); //$NON-NLS-1$
    }
  }

  public void cancel() {
    if ( !repositoryConfigDialog.isHidden() ) {
      repositoryConfigDialog.hide();
      getCallback().onCancel();
    }
  }

  public void test() {
    // build the url handling whether or not the model's url ends wirth a slash
    final String url =
        model.getUrl() + ( model.getUrl().endsWith( "/" ) ? "" : "/" ) + "webservices/unifiedRepository?wsdl"; //$NON-NLS-1$
    Service service;
    try {
      service = Service.create( new URL( url ), new QName( "http://www.pentaho.org/ws/1.0", "unifiedRepository" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      if ( service != null ) {
        IUnifiedRepositoryJaxwsWebService repoWebService = service.getPort( IUnifiedRepositoryJaxwsWebService.class );
        if ( repoWebService != null ) {
          messageBox.setTitle( BaseMessages.getString( PurRepositoryDialog.class, "Dialog.Success" ) );//$NON-NLS-1$
          messageBox.setAcceptLabel( BaseMessages.getString( PurRepositoryDialog.class, "Dialog.Ok" ) );//$NON-NLS-1$
          messageBox.setMessage( BaseMessages.getString( PurRepositoryDialog.class,
              "RepositoryConfigDialog.RepositoryUrlTestPassed" ) );//$NON-NLS-1$
          messageBox.open();

        } else {
          messageBox.setTitle( BaseMessages.getString( PurRepositoryDialog.class, "Dialog.Error" ) );//$NON-NLS-1$
          messageBox.setAcceptLabel( BaseMessages.getString( PurRepositoryDialog.class, "Dialog.Ok" ) );//$NON-NLS-1$
          messageBox.setMessage( BaseMessages.getString( PurRepositoryDialog.class,
              "RepositoryConfigDialog.RepositoryUrlTestFailed" ) );//$NON-NLS-1$
          messageBox.open();
        }
      } else {
        messageBox.setTitle( BaseMessages.getString( PurRepositoryDialog.class, "Dialog.Error" ) );//$NON-NLS-1$
        messageBox.setAcceptLabel( BaseMessages.getString( PurRepositoryDialog.class, "Dialog.Ok" ) );//$NON-NLS-1$
        messageBox.setMessage( BaseMessages.getString( PurRepositoryDialog.class,
            "RepositoryConfigDialog.RepositoryUrlTestFailed" ) );//$NON-NLS-1$
        messageBox.open();

      }
    } catch ( Exception e ) {
      messageBox.setTitle( BaseMessages.getString( PurRepositoryDialog.class, "Dialog.Error" ) );//$NON-NLS-1$
      messageBox.setAcceptLabel( BaseMessages.getString( PurRepositoryDialog.class, "Dialog.Ok" ) );//$NON-NLS-1$
      messageBox.setMessage( BaseMessages.getString( PurRepositoryDialog.class,
          "RepositoryConfigDialog.RepositoryUrlTestFailedMessage", e.getLocalizedMessage() ) );//$NON-NLS-1$
      messageBox.open();
    }
  }

  private String getDefaultUrl() {
    String returnValue = ""; //$NON-NLS-1$
    FileInputStream fis = null;
    Properties properties = null;
    try {
      File file = new File( PLUGIN_PROPERTIES_FILE );
      fis = new FileInputStream( file );
    } catch ( IOException e1 ) {
      return returnValue;
    }
    if ( null != fis ) {
      properties = new Properties();
      try {
        properties.load( fis );
      } catch ( IOException e ) {
        return returnValue;
      }
    }
    if ( properties != null ) {
      returnValue = properties.getProperty( DEFAULT_URL, "" );//$NON-NLS-1$
    }
    return returnValue;
  }

  public IRepositoryConfigDialogCallback getCallback() {
    return callback;
  }

  public void setCallback( IRepositoryConfigDialogCallback callback ) {
    this.callback = callback;
  }

  public void setRepositoryMeta( RepositoryMeta repositoryMeta ) {
    this.repositoryMeta = repositoryMeta;
  }

  public void updateModificationComments() {
    model.setModificationComments( modificationComments.isChecked() );
  }

  private void initializeModel() {
    PurRepositoryMeta purRepositoryMeta = null;
    if ( repositoryMeta != null && repositoryMeta instanceof PurRepositoryMeta ) {
      purRepositoryMeta = (PurRepositoryMeta) repositoryMeta;
      model.setName( purRepositoryMeta.getName() );
      model.setId( purRepositoryMeta.getDescription() );
      PurRepositoryLocation location = purRepositoryMeta.getRepositoryLocation();
      if ( location != null ) {
        model.setUrl( location.getUrl() );
      } else {
        model.setUrl( getDefaultUrl() );
      }
    } else {
      model.setModificationComments( true );
    }
  }

  public ResourceBundle getMessages() {
    return messages;
  }

  public void setMessages( ResourceBundle messages ) {
    this.messages = messages;
  }
}
