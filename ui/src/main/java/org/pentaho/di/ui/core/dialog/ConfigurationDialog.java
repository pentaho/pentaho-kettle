/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.util.HelpUtils;

public abstract class ConfigurationDialog extends Dialog {

  protected AbstractMeta abstractMeta;
  protected ExecutionConfiguration configuration;
  protected TableView wVariables;
  protected boolean retval;
  protected Shell shell;
  protected PropsUI props;
  protected Button wGatherMetrics;
  protected Label wlLogLevel;
  protected Group gDetails;
  protected CCombo wLogLevel;
  protected Button wSafeMode;
  protected Button wClearLog;
  protected int margin = Const.MARGIN;
  protected Composite composite;
  protected Composite cContainer;
  protected Composite cRunConfiguration;
  protected CCombo wRunConfiguration;
  protected ScrolledComposite scContainer;

  private TableView wParams;
  private Display display;
  private Shell parent;
  private Button wOK;
  private Button wCancel;
  protected FormData fdDetails;
  private FormData fd_tabFolder;
  private CTabFolder tabFolder;
  private Button alwaysShowOption;

  public ConfigurationDialog( Shell parent, ExecutionConfiguration configuration, AbstractMeta meta ) {
    super( parent );
    this.parent = parent;
    this.configuration = configuration;
    this.abstractMeta = meta;

    // Fill the parameters, maybe do this in another place?
    Map<String, String> params = configuration.getParams();
    params.clear();
    String[] paramNames = meta.listParameters();
    for ( String name : paramNames ) {
      params.put( name, "" );
    }

    props = PropsUI.getInstance();
  }

  protected void getInfoVariables() {
    Map<String, String> map = new HashMap<String, String>();
    int nrNonEmptyVariables = wVariables.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyVariables; i++ ) {
      TableItem tableItem = wVariables.getNonEmpty( i );
      String varName = tableItem.getText( 1 );
      String varValue = tableItem.getText( 2 );

      if ( !Utils.isEmpty( varName ) ) {
        map.put( varName, varValue );
      }
    }
    configuration.setVariables( map );
  }

  /**
   * Get the parameters from the dialog.
   */
  protected void getInfoParameters() {
    Map<String, String> map = new HashMap<String, String>();
    int nrNonEmptyVariables = wParams.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyVariables; i++ ) {
      TableItem tableItem = wParams.getNonEmpty( i );
      String paramName = tableItem.getText( 1 );
      String defaultValue = tableItem.getText( 2 );
      String paramValue = tableItem.getText( 3 );

      if ( Utils.isEmpty( paramValue ) ) {
        paramValue = Const.NVL( defaultValue, "" );
      }

      map.put( paramName, paramValue );
    }
    configuration.setParams( map );
  }

  protected void ok() {
    abstractMeta.setAlwaysShowRunOptions( alwaysShowOption.getSelection() );
    abstractMeta.setShowDialog( alwaysShowOption.getSelection() );
    if ( Const.isOSX() ) {
      // OSX bug workaround.
      wVariables.applyOSXChanges();
      wParams.applyOSXChanges();
    }
    getInfo();
    retval = true;
    dispose();
  }

  private void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  protected void cancel() {
    dispose();
  }

  public abstract void getInfo();

  protected void getParamsData() {
    wParams.clearAll( false );
    ArrayList<String> paramNames = new ArrayList<String>( configuration.getParams().keySet() );
    Collections.sort( paramNames );

    for ( int i = 0; i < paramNames.size(); i++ ) {
      String paramName = paramNames.get( i );
      String paramValue = configuration.getParams().get( paramName );
      String defaultValue;
      try {
        defaultValue = abstractMeta.getParameterDefault( paramName );
      } catch ( UnknownParamException e ) {
        defaultValue = "";
      }

      String description;
      try {
        description = abstractMeta.getParameterDescription( paramName );
      } catch ( UnknownParamException e ) {
        description = "";
      }

      TableItem tableItem = new TableItem( wParams.table, SWT.NONE );
      tableItem.setText( 1, paramName );
      tableItem.setText( 2, Const.NVL( defaultValue, "" ) );
      tableItem.setText( 3, Const.NVL( paramValue, "" ) );
      tableItem.setText( 4, Const.NVL( description, "" ) );
    }
    wParams.removeEmptyRows();
    wParams.setRowNums();
    wParams.optWidth( true );
  }

  /**
   * @param configuration
   *          the configuration to set
   */
  public void setConfiguration( ExecutionConfiguration configuration ) {
    this.configuration = configuration;
  }

  protected void mainLayout( Class<?> PKG, String prefix, Image img ) {
    display = parent.getDisplay();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX );
    props.setLook( shell );
    shell.setImage( img );
    shell.setLayout( new FormLayout() );
    shell.setText( BaseMessages.getString( PKG, prefix + ".Shell.Title" ) );

    scContainer = new ScrolledComposite( shell, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL );
    scContainer.setLayout( new FormLayout() );
    FormData fd = new FormData();
    fd.top = new FormAttachment( 0, Const.FORM_MARGIN );
    fd.bottom = new FormAttachment( 100, -Const.FORM_MARGIN );
    fd.left = new FormAttachment( 0, Const.FORM_MARGIN );
    fd.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    scContainer.setLayoutData( fd );
    scContainer.setExpandHorizontal( true );
    scContainer.setExpandVertical( true );
    cContainer = new Composite( scContainer, SWT.NONE );
    scContainer.setContent( cContainer );
    cContainer.setLayout( new FormLayout() );
    cContainer.setBackground( shell.getBackground() );
    cContainer.setParent( scContainer );
  }

  protected void optionsSectionLayout( Class<?> PKG, String prefix ) {
    gDetails = new Group( cContainer, SWT.SHADOW_ETCHED_IN );
    gDetails.setText( BaseMessages.getString( PKG, prefix + ".DetailsGroup.Label" ) );
    props.setLook( gDetails );

    // The layout
    gDetails.setLayout( new FormLayout() );
    fdDetails = new FormData();
    fdDetails.top = new FormAttachment( cRunConfiguration, Const.FORM_MARGIN );
    fdDetails.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    fdDetails.left = new FormAttachment( 0, Const.FORM_MARGIN );
    gDetails.setBackground( shell.getBackground() ); // the default looks ugly
    gDetails.setLayoutData( fdDetails );

    optionsSectionControls();
  }

  protected void parametersSectionLayout( Class<?> PKG, String prefix ) {

    tabFolder = new CTabFolder( cContainer, SWT.BORDER );
    props.setLook( tabFolder, Props.WIDGET_STYLE_TAB );
    fd_tabFolder = new FormData();
    fd_tabFolder.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    fd_tabFolder.left = new FormAttachment( 0, Const.FORM_MARGIN );
    fd_tabFolder.top = new FormAttachment( gDetails, Const.FORM_MARGIN );
    fd_tabFolder.bottom = new FormAttachment( gDetails, 370 );
    tabFolder.setLayoutData( fd_tabFolder );

    // Parameters
    CTabItem tbtmParameters = new CTabItem( tabFolder, SWT.NONE );
    tbtmParameters.setText( BaseMessages.getString( PKG, prefix + ".Params.Label" ) );

    ScrolledComposite paramScrollContainer = new ScrolledComposite( tabFolder, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL );
    paramScrollContainer.setLayout( new FormLayout() );
    paramScrollContainer.setExpandHorizontal( true );
    paramScrollContainer.setExpandVertical( true );
    paramScrollContainer.setMinSize( 200, 200 );

    Composite parametersComposite = new Composite( paramScrollContainer, SWT.NONE );
    props.setLook( parametersComposite );

    parametersComposite.setLayout( new FormLayout() );
    parametersComposite.setParent( paramScrollContainer );
    paramScrollContainer.setContent( parametersComposite );
    tbtmParameters.setControl( paramScrollContainer );

    ColumnInfo[] cParams = {
      new ColumnInfo( BaseMessages.getString( PKG, prefix + ".ParamsColumn.Argument" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true, 126 ), // Stepname
      new ColumnInfo( BaseMessages.getString( PKG, prefix + ".ParamsColumn.Default" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true, 138 ), // Preview size
      new ColumnInfo( BaseMessages.getString( PKG, prefix + ".ParamsColumn.Value" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false, 142 ), // Preview size
      new ColumnInfo( BaseMessages.getString( PKG, prefix + ".ParamsColumn.Description" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true, 181 ), // Preview size
    };

    String[] namedParams = abstractMeta.listParameters();
    int nrParams = namedParams.length;
    wParams =
        new TableView( abstractMeta, parametersComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cParams,
            nrParams, false, null, props, false );
    FormData fdParams = new FormData();
    fdParams.top = new FormAttachment( 0, Const.FORM_MARGIN );
    fdParams.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    fdParams.bottom = new FormAttachment( 100, -55 );
    fdParams.left = new FormAttachment( 0, Const.FORM_MARGIN );
    wParams.setLayoutData( fdParams );

    tabFolder.setSelection( 0 );

    Composite argsButtonComposite = new Composite( parametersComposite, SWT.NONE );
    GridLayout argsButtonLayout = new GridLayout(  );
    argsButtonLayout.numColumns = 1;
    argsButtonComposite.setLayout( argsButtonLayout );


    Button argsButton = new Button( argsButtonComposite, SWT.NONE );
    FormData fd_argsButton = new FormData();
    fd_argsButton.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    fd_argsButton.top = new FormAttachment( wParams, Const.FORM_MARGIN );
    fd_argsButton.bottom = new FormAttachment( 100, -Const.FORM_MARGIN );
    argsButtonComposite.setLayoutData( fd_argsButton );
    argsButtonComposite.setBackground( shell.getBackground() );
    argsButton.setLayoutData( argsButtonComposite );
    GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_END );
    argsButton.setText( BaseMessages.getString( PKG, prefix + ".Arguments.Label" ) );
    argsButton.setLayoutData( gridData );
    argsButton.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        new ArgumentsDialog( shell, configuration, abstractMeta );
      }
    } );

    // Variables
    CTabItem tbtmVariables = new CTabItem( tabFolder, SWT.NONE );
    tbtmVariables.setText( BaseMessages.getString( PKG, prefix + ".Variables.Label" ) );

    Composite variablesComposite = new Composite( tabFolder, SWT.NONE );
    props.setLook( variablesComposite );
    variablesComposite.setLayout( new FormLayout() );
    tbtmVariables.setControl( variablesComposite );

    ColumnInfo[] cVariables = {
      new ColumnInfo( BaseMessages.getString( PKG, prefix + ".VariablesColumn.Argument" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false, 287 ), // Stepname
      new ColumnInfo( BaseMessages.getString( PKG, prefix + ".VariablesColumn.Value" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false, 300 ), // Preview size
    };

    int nrVariables = configuration.getVariables() != null ? configuration.getVariables().size() : 0;
    wVariables =
        new TableView( abstractMeta, variablesComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cVariables,
            nrVariables, false, null, props, false );

    FormData fdVariables = new FormData();
    fdVariables.top = new FormAttachment( 0, Const.FORM_MARGIN );
    fdVariables.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    fdVariables.bottom = new FormAttachment( 100, -Const.FORM_MARGIN );
    fdVariables.left = new FormAttachment( 0, Const.FORM_MARGIN );

    wVariables.setLayoutData( fdVariables );
  }

  protected void buttonsSectionLayout( Class<?> PKG, String prefix, final String docTitle, final String docUrl,
      final String docHeader ) {

    // Bottom buttons and separator

    alwaysShowOption = new Button( cContainer, SWT.CHECK );
    props.setLook( alwaysShowOption );
    alwaysShowOption.setSelection( abstractMeta.isAlwaysShowRunOptions() );
    alwaysShowOption.setToolTipText( BaseMessages.getString( PKG, prefix + ".alwaysShowOption" ) );
    FormData fd_alwaysShowOption = new FormData();
    fd_alwaysShowOption.left = new FormAttachment( 0, Const.FORM_MARGIN );
    fd_alwaysShowOption.top = new FormAttachment( tabFolder, Const.FORM_MARGIN );
    alwaysShowOption.setLayoutData( fd_alwaysShowOption );
    alwaysShowOption.setText( BaseMessages.getString( PKG, prefix + ".AlwaysOption.Value" ) );

    wCancel = new Button( cContainer, SWT.PUSH );
    FormData fd_wCancel = new FormData();
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    wCancel.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        cancel();
      }
    } );

    wOK = new Button( cContainer, SWT.PUSH );
    FormData fd_wOK = new FormData();
    fd_wOK.top = new FormAttachment( wCancel, 0, SWT.TOP );
    fd_wOK.right = new FormAttachment( wCancel, -Const.FORM_MARGIN );
    wOK.setLayoutData( fd_wOK );
    wOK.setText( BaseMessages.getString( PKG, prefix + ".Button.Launch" ) );
    wOK.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        ok();
      }
    } );

    Button btnHelp = new Button( cContainer, SWT.NONE );
    btnHelp.setImage( GUIResource.getInstance().getImageHelpWeb() );
    btnHelp.setText( BaseMessages.getString( PKG, "System.Button.Help" ) );
    btnHelp.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.Help" ) );
    FormData fd_btnHelp = new FormData();
    fd_btnHelp.left = new FormAttachment( 0, Const.FORM_MARGIN );
    btnHelp.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent evt ) {
        HelpUtils.openHelpDialog( parent.getShell(), docTitle, docUrl, docHeader );
      }
    } );

    Label separator = new Label( cContainer, SWT.SEPARATOR | SWT.HORIZONTAL );
    if ( Const.isLinux() ) {
      fd_wCancel.top = new FormAttachment( separator, 10 );
    } else {
      fd_wCancel.top = new FormAttachment( separator, 15 );
    }
    if ( Const.isLinux() ) {
      fd_btnHelp.top = new FormAttachment( separator, 10 );
    } else {
      fd_btnHelp.top = new FormAttachment( separator, 15 );
    }
    fd_wCancel.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    wCancel.setLayoutData( fd_wCancel );
    btnHelp.setLayoutData( fd_btnHelp );
    FormData fd_separator = new FormData();
    fd_separator.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    fd_separator.left = new FormAttachment( 0, Const.FORM_MARGIN );
    fd_separator.top = new FormAttachment( alwaysShowOption, Const.FORM_MARGIN );
    separator.setLayoutData( fd_separator );
  }

  protected void openDialog() {
    shell.pack();
    scContainer.setMinSize( cContainer.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    // Set the focus on the OK button
    wOK.setFocus();

    Rectangle shellBounds = getParent().getBounds();
    Point dialogSize = shell.getSize();

    shell.setLocation( shellBounds.x + ( shellBounds.width - dialogSize.x ) / 2, shellBounds.y
        + ( shellBounds.height - dialogSize.y ) / 2 );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  protected void runConfigurationSectionLayout( Class<?> PKG, String prefix ) {
    cRunConfiguration = new Composite( cContainer, SWT.NONE );
    cRunConfiguration.setLayout( new FormLayout() );
    props.setLook( cRunConfiguration );
    FormData fdLocal = new FormData();
    fdLocal.top = new FormAttachment( 0, Const.FORM_MARGIN );
    fdLocal.right = new FormAttachment( 100, -Const.FORM_MARGIN );
    fdLocal.left = new FormAttachment( 0, Const.FORM_MARGIN );

    cRunConfiguration.setBackground( shell.getBackground() ); // the default looks ugly
    cRunConfiguration.setLayoutData( fdLocal );

    Label wlRunConfiguration = new Label( cRunConfiguration, SWT.LEFT );
    props.setLook( wlRunConfiguration );
    wlRunConfiguration.setText( "Run configuration:" );
    FormData fdlRunConfiguration = new FormData();
    fdlRunConfiguration.top = new FormAttachment( 0 );
    fdlRunConfiguration.left = new FormAttachment( 0 );
    wlRunConfiguration.setLayoutData( fdlRunConfiguration );

    wRunConfiguration = new CCombo( cRunConfiguration, SWT.BORDER );
    props.setLook( wRunConfiguration );
    FormData fdRunConfiguration = new FormData();
    fdRunConfiguration.width = 200;
    fdRunConfiguration.top = new FormAttachment( wlRunConfiguration, Const.FORM_MARGIN );
    fdRunConfiguration.left = new FormAttachment( 0 );
    wRunConfiguration.setLayoutData( fdRunConfiguration );
  }

  protected abstract void optionsSectionControls();
}
