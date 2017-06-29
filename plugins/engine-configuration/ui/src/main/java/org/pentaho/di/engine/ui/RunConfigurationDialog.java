/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.engine.ui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.api.RunOption;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 3/15/17.
 */
public class RunConfigurationDialog extends Dialog {

  private static Class<?> PKG = RunConfigurationDialog.class;
  public static final String CLUSTERED = "Clustered";

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  private Shell shell;

  private PropsUI props;

  private Label wlName;
  private Text wName;

  private Label wlDescription;
  private Text wDescription;

  private Label wlEngine;
  private CCombo wEngine;

  private Group gOptions;

  private Button wCancel;

  private Button wOK;

  private RunConfiguration runConfiguration;
  private RunConfiguration savedRunConfiguration;
  private Map<String, RunConfiguration> runConfigurationMap = new HashMap<>();

  private RunConfigurationService executionConfigurationManager;

  public RunConfigurationDialog( Shell parent, RunConfigurationService executionConfigurationManager,
                                 RunConfiguration runConfiguration ) {
    super( parent, SWT.NONE );
    this.props = PropsUI.getInstance();
    this.executionConfigurationManager = executionConfigurationManager;
    this.runConfiguration = runConfiguration;
    if ( runConfiguration != null ) {
      this.runConfigurationMap.put( runConfiguration.getType(), runConfiguration );
    }
  }

  public RunConfiguration open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Title" ) );
    shell.setImage( getImage() );

    Composite wSettings = new Composite( shell, SWT.SHADOW_NONE );
    props.setLook( wSettings );

    FormLayout specLayout = new FormLayout();
    specLayout.marginWidth = 0;
    specLayout.marginHeight = 0;
    wSettings.setLayout( specLayout );

    wlName = new Label( wSettings, SWT.RIGHT );
    wlName.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Label.Name" ) );
    props.setLook( wlName );
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.top = new FormAttachment( 0, 0 );
    wlName.setLayoutData( fdlName );

    wName = new Text( wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    FormData fdName = new FormData();
    fdName.left = new FormAttachment( 0, 0 );
    fdName.top = new FormAttachment( wlName, 5 );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );
    wName.addModifyListener( modifyEvent -> {
      runConfiguration.setName( wName.getText() );
      if ( Utils.isEmpty( runConfiguration.getName() ) ) {
        wOK.setEnabled( false );
      } else {
        wOK.setEnabled( true );
      }
    } );

    wlDescription = new Label( wSettings, SWT.RIGHT );
    wlDescription.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Label.Description" ) );
    props.setLook( wlDescription );
    FormData fdlDescription = new FormData();
    fdlDescription.left = new FormAttachment( 0, 0 );
    fdlDescription.top = new FormAttachment( wName, 10 );
    wlDescription.setLayoutData( fdlDescription );

    wDescription = new Text( wSettings, SWT.MULTI | SWT.LEFT | SWT.BORDER );
    props.setLook( wDescription );
    FormData fdDescription = new FormData();
    fdDescription.height = 40;
    fdDescription.left = new FormAttachment( 0, 0 );
    fdDescription.top = new FormAttachment( wlDescription, 5 );
    fdDescription.right = new FormAttachment( 100, 0 );
    wDescription.setLayoutData( fdDescription );
    wDescription.addModifyListener( modifyEvent -> runConfiguration.setDescription( wDescription.getText() ) );

    wlEngine = new Label( wSettings, SWT.RIGHT );
    wlEngine.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Label.Engine" ) );
    props.setLook( wlEngine );
    FormData fdlEngine = new FormData();
    fdlEngine.left = new FormAttachment( 0, 0 );
    fdlEngine.top = new FormAttachment( wDescription, 10 );
    wlEngine.setLayoutData( fdlEngine );

    wEngine = new CCombo( wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wEngine.setItems( executionConfigurationManager.getTypes() );
    wEngine.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        String engine = wEngine.getText();
        if ( !engine.equals( runConfiguration.getType() ) ) {
          updateOptions( engine );
        }
      }
    } );
    wEngine.select( 0 );
    props.setLook( wEngine );
    FormData fdEngine = new FormData();
    fdEngine.width = 150;
    fdEngine.left = new FormAttachment( 0, 0 );
    fdEngine.top = new FormAttachment( wlEngine, 5 );
    wEngine.setLayoutData( fdEngine );

    gOptions = new Group( wSettings, SWT.SHADOW_ETCHED_IN );
    gOptions.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Group.Settings" ) );
    props.setLook( gOptions );

    FormLayout gformLayout = new FormLayout();
    gformLayout.marginWidth = 10;
    gformLayout.marginHeight = 10;
    gOptions.setLayout( gformLayout );

    FormData fdOptions = new FormData();
    fdOptions.top = new FormAttachment( wEngine, 15 );
    fdOptions.right = new FormAttachment( 100 );
    fdOptions.left = new FormAttachment( 0 );
    fdOptions.height = 140;
    gOptions.setLayoutData( fdOptions );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormData();
    fdCancel.right = new FormAttachment( 100, 0 );
    fdCancel.bottom = new FormAttachment( 100, 0 );
    wCancel.setLayoutData( fdCancel );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( wCancel, -5 );
    fdOk.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOk );

    Label hSpacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdhSpacer = new FormData();
    fdhSpacer.height = 1;
    fdhSpacer.left = new FormAttachment( 0, 0 );
    fdhSpacer.bottom = new FormAttachment( wCancel, -15 );
    fdhSpacer.right = new FormAttachment( 100, 0 );
    hSpacer.setLayoutData( fdhSpacer );

    FormData fdSettings = new FormData();
    fdSettings.left = new FormAttachment( 0 );
    fdSettings.top = new FormAttachment( 0 );
    fdSettings.right = new FormAttachment( 100 );
    fdSettings.bottom = new FormAttachment( hSpacer, -15 );
    wSettings.setLayoutData( fdSettings );

    setValues();

    Listener lsCancel = e -> cancel();
    Listener lsOK = e -> ok();

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    BaseStepDialog.setSize( shell, 450, 300 );
    shell.setMinimumSize( 450, 300 );
    wName.setSelection( 0, wName.getText().length() );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    return savedRunConfiguration;
  }

  private Image getImage() {
    return SwtSvgImageUtil
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "images/run_tree.svg", ConstUI.ICON_SIZE,
        ConstUI.ICON_SIZE );
  }

  private void setValues() {
    if ( runConfiguration == null ) {
      runConfiguration = executionConfigurationManager.getRunConfigurationByType( DefaultRunConfiguration.TYPE );
      runConfigurationMap.put( DefaultRunConfiguration.TYPE, runConfiguration );
    }

    clearOptions();
    if ( runConfiguration.getType().equals( DefaultRunConfiguration.TYPE ) ) {
      showDefault();
    } else {
      showDynamic();
    }
    gOptions.layout();
    shell.pack();

    wEngine.setText( StringUtils.capitalize( runConfiguration.getType() ) );
    wName.setText( Const.NVL( runConfiguration.getName(), "" ) );
    wDescription.setText( Const.NVL( runConfiguration.getDescription(), "" ) );
  }

  private void clearOptions() {
    for ( Control control : gOptions.getChildren() ) {
      control.dispose();
    }
  }

  private void showDefault() {
    DefaultRunConfiguration defaultRunConfiguration = (DefaultRunConfiguration) runConfiguration;

    Composite wTarget = new Composite( gOptions, SWT.NONE );
    wTarget.setLayout( new FormLayout() );
    props.setLook( wTarget );

    Button wbLocal = new Button( wTarget, SWT.RADIO );
    props.setLook( wbLocal );
    wbLocal.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Label.Local" ) );
    wbLocal.setSelection( defaultRunConfiguration.isLocal() );
    FormData fdbLocal = new FormData();
    fdbLocal.top = new FormAttachment( 0 );
    fdbLocal.left = new FormAttachment( 0 );
    wbLocal.setLayoutData( fdbLocal );

    Button wbRemote = new Button( wTarget, SWT.RADIO );
    props.setLook( wbRemote );
    wbRemote.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Label.Remote" ) );
    wbRemote.setSelection( defaultRunConfiguration.isRemote() || defaultRunConfiguration.isClustered() );
    FormData fdbRemote = new FormData();
    fdbRemote.top = new FormAttachment( wbLocal, 5 );
    fdbRemote.left = new FormAttachment( 0 );
    wbRemote.setLayoutData( fdbRemote );

    FormData fdTarget = new FormData();
    fdTarget.left = new FormAttachment( 0 );
    fdTarget.top = new FormAttachment( 0 );
    wTarget.setLayoutData( fdTarget );

    Label vSpacer = new Label( gOptions, SWT.VERTICAL | SWT.SEPARATOR );
    FormData fdvSpacer = new FormData();
    fdvSpacer.width = 1;
    fdvSpacer.left = new FormAttachment( wTarget, 30 );
    fdvSpacer.top = new FormAttachment( 0, 0 );
    fdvSpacer.bottom = new FormAttachment( 100, 0 );
    vSpacer.setLayoutData( fdvSpacer );

    Composite wcLocal = new Composite( gOptions, SWT.NONE );
    props.setLook( wcLocal );
    wcLocal.setLayout( new GridLayout() );

    Text wlLocal = new Text( wcLocal, SWT.MULTI | SWT.WRAP );
    props.setLook( wlLocal );
    wlLocal.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Text.Local" ) );
    GridData gdlLocal = new GridData( GridData.FILL_HORIZONTAL );
    gdlLocal.widthHint = 200;
    wlLocal.setLayoutData( gdlLocal );

    FormData fdcLocal = new FormData();
    fdcLocal.left = new FormAttachment( vSpacer, 30 );
    fdcLocal.top = new FormAttachment( 0 );
    fdcLocal.right = new FormAttachment( 100 );
    fdcLocal.bottom = new FormAttachment( 100 );
    wcLocal.setLayoutData( fdcLocal );

    Composite wcRemote = new Composite( gOptions, SWT.NONE );
    props.setLook( wcRemote );
    wcRemote.setLayout( new FormLayout() );

    Label wlRemote = new Label( wcRemote, SWT.LEFT );
    props.setLook( wlRemote );
    wlRemote.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Label.Location" ) );
    FormData fdlRemote = new FormData();
    fdlRemote.left = new FormAttachment( 0 );
    fdlRemote.top = new FormAttachment( 0 );
    wlRemote.setLayoutData( fdlRemote );

    CCombo wcSlaveServer = new CCombo( wcRemote, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
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
    wbSendResources.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Button.SendResources" ) );
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
      .setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Checkbox.LogRemoteExecutionLocally" ) );
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
      .setText( BaseMessages.getString( PKG, "RunConfigurationDialog.Checkbox.ShowTransformation" ) );
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
    fdcRemote.left = new FormAttachment( vSpacer, 30 );
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
        checkOKEnabled( defaultRunConfiguration, wcSlaveServer );
      }
    } );

    wbLocal.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        wcLocal.setVisible( wbLocal.getSelection() );
        wcRemote.setVisible( wbRemote.getSelection() );
        defaultRunConfiguration.setLocal( wbLocal.getSelection() );
        defaultRunConfiguration.setRemote( false );
        defaultRunConfiguration.setClustered( false );
        checkOKEnabled( defaultRunConfiguration, wcSlaveServer );
      }
    } );

    wbRemote.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        wcLocal.setVisible( wbLocal.getSelection() );
        wcRemote.setVisible( wbRemote.getSelection() );
        defaultRunConfiguration.setLocal( false );
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
        checkOKEnabled( defaultRunConfiguration, wcSlaveServer );
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

  private void checkOKEnabled( DefaultRunConfiguration defaultRunConfiguration, CCombo wcSlaveServer ) {
    if ( ( defaultRunConfiguration.isRemote() && Utils.isEmpty( wcSlaveServer.getText() ) ) || Utils
      .isEmpty( wName.getText() ) ) {
      wOK.setEnabled( false );
    } else {
      wOK.setEnabled( true );
    }
  }

  private void showDynamic() {
    Control lastControl = null;

    for ( Field field : runConfiguration.getClass().getDeclaredFields() ) {
      RunOption runOption = field.getAnnotation( RunOption.class );
      if ( runOption != null ) {
        Label optionLabel = new Label( gOptions, SWT.LEFT );
        props.setLook( optionLabel );
        optionLabel.setText( runOption.label() );
        FormData fdlOption = new FormData();
        fdlOption.left = new FormAttachment( 0 );
        fdlOption.top = lastControl != null ? new FormAttachment( lastControl, 10 ) : new FormAttachment( 0 );
        optionLabel.setLayoutData( fdlOption );

        String value = invokeGetter( runConfiguration, field.getName(), runOption.value() );
        Text optionText = new Text( gOptions, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( optionText );
        optionText.setText( value );
        FormData fdOption = new FormData();
        fdOption.left = new FormAttachment( 0 );
        fdOption.top = new FormAttachment( optionLabel, 5 );
        fdOption.right = new FormAttachment( 100 );
        optionText.setLayoutData( fdOption );

        invokeSetter( runConfiguration, field.getName(), optionText.getText() );

        optionText.addModifyListener( modifyEvent -> {
          if ( Utils.isEmpty( optionText.getText() ) ) {
            wOK.setEnabled( false );
          } else {
            wOK.setEnabled( true );
          }
          invokeSetter( runConfiguration, field.getName(), optionText.getText() );
        } );

        lastControl = optionText;
      }
    }
  }

  private void invokeSetter( Object object, String fieldName, String value ) {
    try {
      Method method =
        object.getClass().getMethod( "set" + StringUtils.capitalize( fieldName ), String.class );
      if ( method != null ) {
        method.invoke( object, value );
      }
    } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
      // Ignore exception
    }
  }

  private String invokeGetter( Object object, String fieldName, String defaultValue ) {
    try {
      Method method =
        object.getClass().getMethod( "get" + StringUtils.capitalize( fieldName ) );
      if ( method != null ) {
        String fieldValue = (String) method.invoke( runConfiguration );
        String value = fieldValue != null ? fieldValue : "";
        if ( Utils.isEmpty( value ) && !Utils.isEmpty( defaultValue ) ) {
          return defaultValue;
        }
        return value;
      }
    } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
      // Ignore exception
    }
    return "";
  }

  private void updateOptions( String type ) {
    RunConfiguration newRunConfiguration = runConfigurationMap.get( type );
    if ( newRunConfiguration == null ) {
      newRunConfiguration = executionConfigurationManager.getRunConfigurationByType( type );
      newRunConfiguration.setName( runConfiguration.getName() );
      newRunConfiguration.setDescription( runConfiguration.getDescription() );
      runConfigurationMap.put( type, newRunConfiguration );
    }
    runConfiguration = newRunConfiguration;
    setValues();
  }

  private void cancel() {
    shell.dispose();
  }

  private void ok() {
    if ( validated() ) {
      save();
      shell.dispose();
    }
  }

  private void save() {
    savedRunConfiguration = runConfiguration;
  }

  private boolean validated() {
    if ( StringUtils.containsAny( wName.getText(), "%\"\\/:[]*|\t\r\n" ) ) {
      MessageBox messageBox = new MessageBox( shell, SWT.ERROR );
      messageBox.setMessage( BaseMessages.getString( PKG, "RunConfiguration.InvalidChars.Message" ) );
      messageBox.setText( BaseMessages.getString( PKG, "RunConfiguration.InvalidChars.Title" ) );
      messageBox.open();
      return false;
    }

    return true;
  }

}
