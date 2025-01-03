/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by bmorrise on 3/15/17.
 */
public class RunConfigurationDialog extends Dialog
  implements org.pentaho.di.engine.configuration.api.RunConfigurationDialog {

  private static Class<?> PKG = RunConfigurationDialog.class;

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

  private List<String> existingNames;
  private String originalName;

  private RunConfigurationService executionConfigurationManager;

  public RunConfigurationDialog( Shell parent, RunConfigurationService executionConfigurationManager,
                                 RunConfiguration runConfiguration, List<String> existingNames ) {

    super( parent, SWT.NONE );
    this.props = PropsUI.getInstance();
    this.executionConfigurationManager = executionConfigurationManager;
    this.runConfiguration = runConfiguration;
    if ( runConfiguration != null ) {
      this.runConfigurationMap.put( runConfiguration.getType(), runConfiguration );
      originalName = runConfiguration.getName();
    }
    this.existingNames = existingNames;
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
    shell.setMinimumSize( shell.getSize() );
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
    runConfiguration.getUI().attach( this );
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
      runConfiguration.setName( runConfiguration.getName().trim() );
      if ( !runConfiguration.getName().equals( originalName ) ) {
        existingNames.remove( originalName );
        if ( existingNames.stream().anyMatch( n -> n.equalsIgnoreCase( runConfiguration.getName().trim() ) ) ) {
          String title = BaseMessages.getString( PKG, "RunConfigurationDialog.RunConfigurationNameExists.Title" );
          String message =
            BaseMessages.getString( PKG, "RunConfigurationDialog.RunConfigurationNameExists",
              runConfiguration.getName() );
          String okButton = BaseMessages.getString( PKG, "System.Button.OK" );
          MessageDialog dialog =
            new MessageDialog( shell, title, null, message, MessageDialog.ERROR, new String[] { okButton }, 0 );

          dialog.open();
          return;
        }
      }
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

  @Override public Text getName() {
    return wName;
  }

  @Override public Button getOKButton() {
    return wOK;
  }

  @Override public Group getGroup() {
    return gOptions;
  }
}
