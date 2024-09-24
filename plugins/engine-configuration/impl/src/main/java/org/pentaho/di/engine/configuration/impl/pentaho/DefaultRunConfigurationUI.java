/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.pentaho;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.configuration.api.RunConfigurationDialog;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.function.Supplier;

/**
 * Created by bmorrise on 8/22/17.
 */
public class DefaultRunConfigurationUI implements RunConfigurationUI {

  private static final String CLUSTERED = "Clustered";
  private static Class<?> PKG = DefaultRunConfigurationUI.class;

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;
  private PropsUI props = PropsUI.getInstance();
  private DefaultRunConfiguration defaultRunConfiguration;
  private RunConfigurationDialog runConfigurationDialog;

  private CCombo wcSlaveServer;

  public DefaultRunConfigurationUI( DefaultRunConfiguration defaultRunConfiguration ) {
    this.defaultRunConfiguration = defaultRunConfiguration;
  }

  @Override public void attach( RunConfigurationDialog runConfigurationDialog ) {
    this.runConfigurationDialog = runConfigurationDialog;

    FormLayout gformLayout = new FormLayout();
    gformLayout.marginWidth = 10;
    gformLayout.marginHeight = 10;
    runConfigurationDialog.getGroup().setLayout( gformLayout );

    Composite wTarget = new Composite( runConfigurationDialog.getGroup(), SWT.NONE );
    wTarget.setLayout( new FormLayout() );
    props.setLook( wTarget );

    Button wbLocal = new Button( wTarget, SWT.RADIO );
    props.setLook( wbLocal );
    wbLocal.setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Label.Local" ) );
    wbLocal.setSelection( defaultRunConfiguration.isLocal() );
    FormData fdbLocal = new FormData();
    fdbLocal.top = new FormAttachment( 0 );
    fdbLocal.left = new FormAttachment( 0 );
    wbLocal.setLayoutData( fdbLocal );

    Button wbPentaho = new Button( wTarget, SWT.RADIO );
    props.setLook( wbPentaho );
    boolean connectedToPentahoServer =
      spoonSupplier.get().getRepository() != null && spoonSupplier.get().getRepository().getRepositoryMeta().getId()
        .equals( "PentahoEnterpriseRepository" );
    wbPentaho.setVisible( connectedToPentahoServer );
    if ( connectedToPentahoServer ) {
      wbPentaho.setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Label.Pentaho" ) );
      wbPentaho.setSelection( defaultRunConfiguration.isPentaho() );
      FormData fdbPentaho = new FormData();
      fdbPentaho.top = new FormAttachment( wbLocal, 10 );
      fdbPentaho.left = new FormAttachment( 0 );
      wbPentaho.setLayoutData( fdbPentaho );
    }

    Button wbRemote = new Button( wTarget, SWT.RADIO );
    props.setLook( wbRemote );
    wbRemote.setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Label.Remote" ) );
    wbRemote.setSelection( defaultRunConfiguration.isRemote() || defaultRunConfiguration.isClustered() );
    FormData fdbRemote = new FormData();
    fdbRemote.top = new FormAttachment( wbPentaho, 10 );
    fdbRemote.left = new FormAttachment( 0 );
    wbRemote.setLayoutData( fdbRemote );

    FormData fdTarget = new FormData();
    fdTarget.left = new FormAttachment( 0 );
    fdTarget.top = new FormAttachment( 0 );
    wTarget.setLayoutData( fdTarget );

    Label vSpacer = new Label( runConfigurationDialog.getGroup(), SWT.VERTICAL | SWT.SEPARATOR );
    FormData fdvSpacer = new FormData();
    fdvSpacer.width = 1;
    fdvSpacer.left = new FormAttachment( wTarget, 30 );
    fdvSpacer.top = new FormAttachment( 0, 0 );
    fdvSpacer.bottom = new FormAttachment( 100, 0 );
    vSpacer.setLayoutData( fdvSpacer );

    Composite wcLocal = new Composite( runConfigurationDialog.getGroup(), SWT.NONE );
    props.setLook( wcLocal );
    wcLocal.setLayout( new GridLayout() );

    Text wlLocal = new Text( wcLocal, SWT.MULTI | SWT.WRAP );
    wlLocal.setEditable( false );
    props.setLook( wlLocal );
    wlLocal.setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Text.Local" ) );
    GridData gdlLocal = new GridData( GridData.FILL_HORIZONTAL );
    gdlLocal.widthHint = 200;
    wlLocal.setLayoutData( gdlLocal );

    FormData fdcLocal = new FormData();
    fdcLocal.left = new FormAttachment( vSpacer, 10 );
    fdcLocal.top = new FormAttachment( 0 );
    fdcLocal.right = new FormAttachment( 100 );
    fdcLocal.bottom = new FormAttachment( 100 );
    wcLocal.setLayoutData( fdcLocal );

    Composite wcPentaho = new Composite( runConfigurationDialog.getGroup(), SWT.NONE );
    props.setLook( wcPentaho );
    wcPentaho.setLayout( new GridLayout() );

    Text wlPentaho = new Text( wcPentaho, SWT.MULTI | SWT.WRAP | SWT.LEFT );
    wlPentaho.setEditable( false );
    props.setLook( wlPentaho );
    wlPentaho.setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Text.Pentaho" ) );
    GridData gdlPentaho = new GridData( GridData.FILL_HORIZONTAL );
    gdlPentaho.widthHint = 200;
    wlPentaho.setLayoutData( gdlPentaho );

    FormData fdcPentaho = new FormData();
    fdcPentaho.left = new FormAttachment( vSpacer, 10 );
    fdcPentaho.top = new FormAttachment( 0 );
    fdcPentaho.right = new FormAttachment( 100 );
    fdcPentaho.bottom = new FormAttachment( 100 );
    wcPentaho.setLayoutData( fdcPentaho );

    Composite wcRemote = new Composite( runConfigurationDialog.getGroup(), SWT.NONE );
    props.setLook( wcRemote );
    wcRemote.setLayout( new FormLayout() );

    Label wlRemote = new Label( wcRemote, SWT.LEFT );
    props.setLook( wlRemote );
    wlRemote.setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Label.Location" ) );
    FormData fdlRemote = new FormData();
    fdlRemote.left = new FormAttachment( 0 );
    fdlRemote.top = new FormAttachment( 0 );
    wlRemote.setLayoutData( fdlRemote );

    wcSlaveServer = new CCombo( wcRemote, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    props.setLook( wcSlaveServer );
    FormData fdSlaveServer = new FormData();
    fdSlaveServer.width = 150;
    fdSlaveServer.top = new FormAttachment( wlRemote, 5 );
    fdSlaveServer.left = new FormAttachment( 0 );
    wcSlaveServer.setLayoutData( fdSlaveServer );

    Button wbSendResources = new Button( wcRemote, SWT.CHECK );
    wbSendResources.setSelection( defaultRunConfiguration.isSendResources() );
    wbSendResources
      .setVisible( !Utils.isEmpty( defaultRunConfiguration.getServer() ) && !defaultRunConfiguration.isClustered() );
    props.setLook( wbSendResources );
    wbSendResources.setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Button.SendResources" ) );
    FormData fdbSendResources = new FormData();
    fdbSendResources.top = new FormAttachment( wcSlaveServer, 10 );
    fdbSendResources.left = new FormAttachment( 0 );
    wbSendResources.setLayoutData( fdbSendResources );
    wbSendResources.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        defaultRunConfiguration.setSendResources( wbSendResources.getSelection() );
      }
    } );

    Button wbLogRemoteExecutionLocally = new Button( wcRemote, SWT.CHECK );
    wbLogRemoteExecutionLocally.setSelection( defaultRunConfiguration.isLogRemoteExecutionLocally() );
    wbLogRemoteExecutionLocally.setVisible( defaultRunConfiguration.isClustered() );
    props.setLook( wbLogRemoteExecutionLocally );
    wbLogRemoteExecutionLocally
      .setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Checkbox.LogRemoteExecutionLocally" ) );
    FormData fdbLogRemoteExecutionLocally = new FormData();
    fdbLogRemoteExecutionLocally.top = new FormAttachment( wcSlaveServer, 10 );
    fdbLogRemoteExecutionLocally.left = new FormAttachment( 0 );
    wbLogRemoteExecutionLocally.setLayoutData( fdbLogRemoteExecutionLocally );
    wbLogRemoteExecutionLocally.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        defaultRunConfiguration.setLogRemoteExecutionLocally( wbLogRemoteExecutionLocally.getSelection() );
      }
    } );

    Button wbShowTransformations = new Button( wcRemote, SWT.CHECK );
    wbShowTransformations.setSelection( defaultRunConfiguration.isShowTransformations() );
    wbShowTransformations.setVisible( defaultRunConfiguration.isClustered() );
    props.setLook( wbShowTransformations );
    wbShowTransformations
      .setText( BaseMessages.getString( PKG, "DefaultRunConfigurationDialog.Checkbox.ShowTransformation" ) );
    FormData fdbShowTransformations = new FormData();
    fdbShowTransformations.top = new FormAttachment( wbLogRemoteExecutionLocally, 10 );
    fdbShowTransformations.left = new FormAttachment( 0 );
    wbShowTransformations.setLayoutData( fdbShowTransformations );
    wbShowTransformations.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        defaultRunConfiguration.setShowTransformations( wbShowTransformations.getSelection() );
      }
    } );

    FormData fdcRemote = new FormData();
    fdcRemote.left = new FormAttachment( vSpacer, 20 );
    fdcRemote.top = new FormAttachment( 0 );
    fdcRemote.right = new FormAttachment( 100 );
    fdcRemote.bottom = new FormAttachment( 100 );
    wcRemote.setLayoutData( fdcRemote );

    AbstractMeta meta = (AbstractMeta) spoonSupplier.get().getActiveMeta();

    if ( meta instanceof TransMeta && !Utils.isEmpty( ( (TransMeta) meta ).getClusterSchemas() ) ) {
      wcSlaveServer.add( CLUSTERED );
    }

    for ( int i = 0; i < meta.getSlaveServers().size(); i++ ) {
      SlaveServer slaveServer = meta.getSlaveServers().get( i );
      wcSlaveServer.add( slaveServer.toString() );
    }

    if ( !Utils.isEmpty( defaultRunConfiguration.getServer() ) ) {
      wcSlaveServer.setText( defaultRunConfiguration.getServer() );
    }

    wcLocal.setVisible( defaultRunConfiguration.isLocal() );
    wcPentaho.setVisible( defaultRunConfiguration.isPentaho() );
    wcRemote.setVisible( defaultRunConfiguration.isRemote() || defaultRunConfiguration.isClustered() );

    wcSlaveServer.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        if ( wcSlaveServer.getText().equals( CLUSTERED ) ) {
          defaultRunConfiguration.setClustered( true );
          defaultRunConfiguration.setLocal( false );
          defaultRunConfiguration.setRemote( false );
          wbLogRemoteExecutionLocally.setVisible( true );
          wbShowTransformations.setVisible( true );
          wbSendResources.setVisible( false );
        } else {
          defaultRunConfiguration.setRemote( true );
          defaultRunConfiguration.setLocal( false );
          defaultRunConfiguration.setClustered( false );
          defaultRunConfiguration.setServer( wcSlaveServer.getText() );
          wbLogRemoteExecutionLocally.setVisible( false );
          wbShowTransformations.setVisible( false );
          wbSendResources.setVisible( true );
        }
        checkOKEnabled();
      }
    } );

    wbLocal.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        wcLocal.setVisible( wbLocal.getSelection() );
        wcPentaho.setVisible( wbPentaho.getSelection() );
        wcRemote.setVisible( wbRemote.getSelection() );
        defaultRunConfiguration.setLocal( true );
        defaultRunConfiguration.setPentaho( false );
        defaultRunConfiguration.setRemote( false );
        defaultRunConfiguration.setClustered( false );
        checkOKEnabled();
      }
    } );

    wbPentaho.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        wcLocal.setVisible( wbLocal.getSelection() );
        wcPentaho.setVisible( wbPentaho.getSelection() );
        wcRemote.setVisible( wbRemote.getSelection() );
        defaultRunConfiguration.setLocal( false );
        defaultRunConfiguration.setPentaho( true );
        defaultRunConfiguration.setRemote( false );
        defaultRunConfiguration.setClustered( false );
        checkOKEnabled();
      }
    } );

    wbRemote.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        wcLocal.setVisible( wbLocal.getSelection() );
        wcPentaho.setVisible( wbPentaho.getSelection() );
        wcRemote.setVisible( wbRemote.getSelection() );
        defaultRunConfiguration.setLocal( false );
        defaultRunConfiguration.setPentaho( false );
        if ( Utils.isEmpty( wcSlaveServer.getText() ) ) {
          if ( meta instanceof TransMeta && !Utils.isEmpty( ( (TransMeta) meta ).getClusterSchemas() ) ) {
            wcSlaveServer.setText( CLUSTERED );
          } else if ( meta.getSlaveServers().size() > 0 ) {
            wcSlaveServer.setText( meta.getSlaveServers().get( 0 ).getName() );
          }
        }
        if ( !wcSlaveServer.getText().equals( CLUSTERED ) ) {
          defaultRunConfiguration.setRemote( true );
          defaultRunConfiguration.setClustered( false );
          wbSendResources.setVisible( true );
          wbShowTransformations.setVisible( false );
          wbLogRemoteExecutionLocally.setVisible( false );
        } else {
          defaultRunConfiguration.setClustered( true );
          defaultRunConfiguration.setRemote( false );
          wbSendResources.setVisible( false );
          wbShowTransformations.setVisible( true );
          wbLogRemoteExecutionLocally.setVisible( true );
        }
        checkOKEnabled();
        if ( !Utils.isEmpty( wcSlaveServer.getText() ) ) {
          defaultRunConfiguration.setServer( wcSlaveServer.getText() );
        }
      }
    } );

    if ( defaultRunConfiguration.isClustered() ) {
      wcSlaveServer.setText( CLUSTERED );
      wbSendResources.setVisible( false );
      wbShowTransformations.setVisible( true );
      wbLogRemoteExecutionLocally.setVisible( true );
    }
  }

  public void checkOKEnabled() {
    if ( ( defaultRunConfiguration.isRemote() && Utils.isEmpty( wcSlaveServer.getText() ) ) || Utils
      .isEmpty( runConfigurationDialog.getName().getText() ) ) {
      runConfigurationDialog.getOKButton().setEnabled( false );
    } else {
      runConfigurationDialog.getOKButton().setEnabled( true );
    }
  }

}
