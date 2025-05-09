/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.trans.steps.salesforceinput;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.steps.salesforce.SOQLValuesHighlight;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnectionUtils;
import org.pentaho.di.trans.steps.salesforce.SalesforceStep;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepMeta;
import org.pentaho.di.trans.steps.salesforceinput.FieldDTO;
import org.pentaho.di.trans.steps.salesforceinput.FieldsResponse;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInput;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputField;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.ComponentSelectionListener;
import org.pentaho.di.ui.trans.steps.salesforce.SalesforceStepDialog;



public class SalesforceInputDialog extends SalesforceStepDialog {

  private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!

  private String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'.000'XXX";
  private String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

  private CTabFolder wTabFolder;

  private CTabItem wFileTab, wContentTab, wFieldsTab;

  private Composite wFileComp, wContentComp, wFieldsComp;

  private FormData fdTabFolder, fdFileComp, fdContentComp, fdFieldsComp, fdlInclURLField;

  private FormData fdInclURLField, fdlInclModuleField, fdlInclRownumField, fdlModule, fdModule;

  private FormData fdInclModuleField, fdlInclModule, fdlInclURL, fdInclURL, fdlLimit, fdLimit;

  private FormData fdlTimeOut, fdTimeOut, fdFields, fdUserName, fdURL, fdPassword, fdCondition;

  private Button wInclURL, wInclModule, wInclRownum, wUseCompression, wQueryAll;

  private FormData fdInclSQLField;

  private FormData fdInclTimestampField;

  private Label wlInclURL, wlInclURLField, wlInclModule, wlInclRownum, wlInclRownumField;

  private Label wlInclModuleField, wlLimit, wlTimeOut, wlCondition, wlModule, wlInclSQLField, wlInclSQL;

  private Group wConnectionGroup, wSettingsGroup;

  private Label wlInclTimestampField, wlInclTimestamp, wlUseCompression;
  private Label wlQueryAll, wlInclDeletionDateField, wlInclDeletionDate;

  private FormData fdlInclSQL, fdInclSQL, fdlInclSQLField, fdlInclDeletionDateField, fdlInclDeletionDate;

  private FormData fdlInclTimestamp, fdInclTimestamp, fdlInclTimestampField, fdInclDeletionDateField, fdDeletionDate;

  private Button wInclSQL;

  private TextVar wInclURLField, wInclModuleField, wInclRownumField, wInclSQLField, wInclDeletionDateField;

  private Button wInclTimestamp, wInclDeletionDate;

  private TextVar wInclTimestampField;

  private TableView wFields;

  private SalesforceInputMeta input;

  private LabelTextVar wUserName, wURL, wPassword;

  private StyledTextComp wCondition;

  private Label wlPosition;
  private FormData fdlPosition;

  private Button wspecifyQuery;
  private FormData fdspecifyQuery;
  private Label wlspecifyQuery;
  private FormData fdlspecifyQuery;

  private StyledTextComp wQuery;
  private FormData fdQuery;
  private Label wlQuery;
  private FormData fdlQuery;

  private TextVar wTimeOut, wLimit;

  private ComboVar wModule;

  private Group wAdditionalFields, wAdvancedGroup;

  private FormData fdAdditionalFields, fdAdvancedGroup;

  private Label wlRecordsFilter;
  private CCombo wRecordsFilter;
  private FormData fdlRecordsFilter;
  private FormData fdRecordsFilter;

  private Label wlReadFrom;
  private TextVar wReadFrom;
  private FormData fdlReadFrom, fdReadFrom;
  private Button open;

  private Label wlReadTo;
  private TextVar wReadTo;
  private FormData fdlReadTo, fdReadTo;
  private Button opento;

  private Button wTest;

  private FormData fdTest;
  private Listener lsTest;

  private boolean gotModule = false;

  private boolean getModulesListError = false; /* True if error getting modules list */

  private ColumnInfo[] colinf;

  public SalesforceInputDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, in, transMeta, sname );
    input = (SalesforceInputMeta) in;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };

    SelectionListener checkBoxModifyListener = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    };

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.DialogTitle" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, margin );
    fdlStepname.right = new FormAttachment( middle, -margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF FILE TAB ///
    // ////////////////////////
    wFileTab = new CTabItem( wTabFolder, SWT.NONE );
    wFileTab.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.File.Tab" ) );

    wFileComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFileComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wFileComp.setLayout( fileLayout );

    // ////////////////////////
    // START CONNECTION GROUP

    wConnectionGroup = new Group( wFileComp, SWT.SHADOW_ETCHED_IN );
    wConnectionGroup.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.ConnectionGroup.Label" ) );
    FormLayout fconnLayout = new FormLayout();
    fconnLayout.marginWidth = 3;
    fconnLayout.marginHeight = 3;
    wConnectionGroup.setLayout( fconnLayout );
    props.setLook( wConnectionGroup );

    // Webservice URL
    wURL = new LabelTextVar( transMeta, wConnectionGroup,
      BaseMessages.getString( PKG, "SalesforceInputDialog.URL.Label" ),
      BaseMessages.getString( PKG, "SalesforceInputDialog.URL.Tooltip" ) );
    props.setLook( wURL );
    wURL.addModifyListener( lsMod );
    fdURL = new FormData();
    fdURL.left = new FormAttachment( 0, 0 );
    fdURL.top = new FormAttachment( 0, margin );
    fdURL.right = new FormAttachment( 100, 0 );
    wURL.setLayoutData( fdURL );

    // UserName line
    wUserName = new LabelTextVar( transMeta, wConnectionGroup,
      BaseMessages.getString( PKG, "SalesforceInputDialog.User.Label" ),
      BaseMessages.getString( PKG, "SalesforceInputDialog.User.Tooltip" ) );
    props.setLook( wUserName );
    wUserName.addModifyListener( lsMod );
    fdUserName = new FormData();
    fdUserName.left = new FormAttachment( 0, 0 );
    fdUserName.top = new FormAttachment( wURL, margin );
    fdUserName.right = new FormAttachment( 100, 0 );
    wUserName.setLayoutData( fdUserName );

    // Password line
    wPassword = new LabelTextVar( transMeta, wConnectionGroup,
      BaseMessages.getString( PKG, "SalesforceInputDialog.Password.Label" ),
      BaseMessages.getString( PKG, "SalesforceInputDialog.Password.Tooltip" ), true );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    fdPassword = new FormData();
    fdPassword.left = new FormAttachment( 0, 0 );
    fdPassword.top = new FormAttachment( wUserName, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    // Test Salesforce connection button
    wTest = new Button( wConnectionGroup, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.TestConnection.Label" ) );
    props.setLook( wTest );
    fdTest = new FormData();
    wTest.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.TestConnection.Tooltip" ) );
    // fdTest.left = new FormAttachment(middle, 0);
    fdTest.top = new FormAttachment( wPassword, margin );
    fdTest.right = new FormAttachment( 100, 0 );
    wTest.setLayoutData( fdTest );

    FormData fdConnectionGroup = new FormData();
    fdConnectionGroup.left = new FormAttachment( 0, 0 );
    fdConnectionGroup.right = new FormAttachment( 100, 0 );
    fdConnectionGroup.top = new FormAttachment( 0, margin );
    wConnectionGroup.setLayoutData( fdConnectionGroup );

    // END CONNECTION GROUP
    // ////////////////////////

    // ////////////////////////
    // START SETTINGS GROUP

    wSettingsGroup = new Group( wFileComp, SWT.SHADOW_ETCHED_IN );
    wSettingsGroup.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.HttpAuthGroup.Label" ) );
    FormLayout fsettingsLayout = new FormLayout();
    fsettingsLayout.marginWidth = 3;
    fsettingsLayout.marginHeight = 3;
    wSettingsGroup.setLayout( fsettingsLayout );
    props.setLook( wSettingsGroup );

    wlspecifyQuery = new Label( wSettingsGroup, SWT.RIGHT );
    wlspecifyQuery.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.specifyQuery.Label" ) );
    props.setLook( wlspecifyQuery );
    fdlspecifyQuery = new FormData();
    fdlspecifyQuery.left = new FormAttachment( 0, 0 );
    fdlspecifyQuery.top = new FormAttachment( wConnectionGroup, 2 * margin );
    fdlspecifyQuery.right = new FormAttachment( middle, -margin );
    wlspecifyQuery.setLayoutData( fdlspecifyQuery );
    wspecifyQuery = new Button( wSettingsGroup, SWT.CHECK );
    props.setLook( wspecifyQuery );
    wspecifyQuery.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.specifyQuery.Tooltip" ) );
    fdspecifyQuery = new FormData();
    fdspecifyQuery.left = new FormAttachment( middle, 0 );
    fdspecifyQuery.top = new FormAttachment( wConnectionGroup, 2 * margin );
    wspecifyQuery.setLayoutData( fdspecifyQuery );
    wspecifyQuery.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setEnableQuery();
        input.setChanged();
      }
    } );

    // Module
    wlModule = new Label( wSettingsGroup, SWT.RIGHT );
    wlModule.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.Module.Label" ) );
    props.setLook( wlModule );
    fdlModule = new FormData();
    fdlModule.left = new FormAttachment( 0, 0 );
    fdlModule.top = new FormAttachment( wspecifyQuery, margin );
    fdlModule.right = new FormAttachment( middle, -margin );
    wlModule.setLayoutData( fdlModule );
    wModule = new ComboVar( transMeta, wSettingsGroup, SWT.BORDER | SWT.READ_ONLY );
    wModule.setEditable( true );
    props.setLook( wModule );
    wModule.addModifyListener( lsMod );
    fdModule = new FormData();
    fdModule.left = new FormAttachment( middle, margin );
    fdModule.top = new FormAttachment( wspecifyQuery, margin );
    fdModule.right = new FormAttachment( 100, -margin );
    wModule.setLayoutData( fdModule );
    wModule.addFocusListener( new FocusListener() {
      @Override
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
        getModulesListError = false;
      }

      @Override
      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        // check if the URL and login credentials passed and not just had error
        if ( Utils.isEmpty( wURL.getText() )
          || Utils.isEmpty( wUserName.getText() ) || Utils.isEmpty( wPassword.getText() )
          || ( getModulesListError ) ) {
          return;
        }

        getModulesList();
      }
    } );

    wlPosition = new Label( wSettingsGroup, SWT.NONE );
    props.setLook( wlPosition );
    fdlPosition = new FormData();
    fdlPosition.left = new FormAttachment( middle, 0 );
    fdlPosition.right = new FormAttachment( 100, 0 );
    fdlPosition.bottom = new FormAttachment( 100, -margin );
    wlPosition.setLayoutData( fdlPosition );

    // condition
    wlCondition = new Label( wSettingsGroup, SWT.RIGHT );
    wlCondition.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.Condition.Label" ) );
    props.setLook( wlCondition );
    FormData fdlCondition = new FormData();
    fdlCondition.left = new FormAttachment( 0, -margin );
    fdlCondition.top = new FormAttachment( wModule, margin );
    fdlCondition.right = new FormAttachment( middle, -margin );
    wlCondition.setLayoutData( fdlCondition );

    wCondition =
      new StyledTextComp( transMeta, wSettingsGroup, SWT.MULTI
        | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "" );
    wCondition.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.Condition.Tooltip" ) );
    props.setLook( wCondition, Props.WIDGET_STYLE_FIXED );
    wCondition.addModifyListener( lsMod );
    fdCondition = new FormData();
    fdCondition.left = new FormAttachment( middle, margin );
    fdCondition.top = new FormAttachment( wModule, margin );
    fdCondition.right = new FormAttachment( 100, -2 * margin );
    fdCondition.bottom = new FormAttachment( wlPosition, -margin );
    wCondition.setLayoutData( fdCondition );
    wCondition.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent arg0 ) {
        setQueryToolTip();
        setPosition();
      }
    } );

    wCondition.addKeyListener( new KeyAdapter() {
      @Override
      public void keyPressed( KeyEvent e ) {
        setPosition();
      }

      @Override
      public void keyReleased( KeyEvent e ) {
        setPosition();
      }
    } );
    wCondition.addFocusListener( new FocusAdapter() {
      @Override
      public void focusGained( FocusEvent e ) {
        setPosition();
      }

      @Override
      public void focusLost( FocusEvent e ) {
        setPosition();
      }
    } );
    wCondition.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseDoubleClick( MouseEvent e ) {
        setPosition();
      }

      @Override
      public void mouseDown( MouseEvent e ) {
        setPosition();
      }

      @Override
      public void mouseUp( MouseEvent e ) {
        setPosition();
      }
    } );

    // Text Higlighting
    wCondition.addLineStyleListener( new SOQLValuesHighlight() );

    // Query
    wlQuery = new Label( wSettingsGroup, SWT.RIGHT );
    wlQuery.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.Query.Label" ) );
    props.setLook( wlQuery );
    fdlQuery = new FormData();
    fdlQuery.left = new FormAttachment( 0, -margin );
    fdlQuery.top = new FormAttachment( wspecifyQuery, margin );
    fdlQuery.right = new FormAttachment( middle, -margin );
    wlQuery.setLayoutData( fdlQuery );

    wQuery =
      new StyledTextComp( transMeta, wSettingsGroup, SWT.MULTI
        | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "" );
    props.setLook( wQuery, Props.WIDGET_STYLE_FIXED );
    wQuery.addModifyListener( lsMod );
    fdQuery = new FormData();
    fdQuery.left = new FormAttachment( middle, 0 );
    fdQuery.top = new FormAttachment( wspecifyQuery, margin );
    fdQuery.right = new FormAttachment( 100, -2 * margin );
    fdQuery.bottom = new FormAttachment( wlPosition, -margin );
    wQuery.setLayoutData( fdQuery );
    wQuery.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent arg0 ) {
        setQueryToolTip();
      }
    } );

    wQuery.addKeyListener( new KeyAdapter() {
      @Override
      public void keyPressed( KeyEvent e ) {
        setPosition();
      }

      @Override
      public void keyReleased( KeyEvent e ) {
        setPosition();
      }
    } );
    wQuery.addFocusListener( new FocusAdapter() {
      @Override
      public void focusGained( FocusEvent e ) {
        setPosition();
      }

      @Override
      public void focusLost( FocusEvent e ) {
        setPosition();
      }
    } );
    wQuery.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseDoubleClick( MouseEvent e ) {
        setPosition();
      }

      @Override
      public void mouseDown( MouseEvent e ) {
        setPosition();
      }

      @Override
      public void mouseUp( MouseEvent e ) {
        setPosition();
      }
    } );

    // Text Higlighting
    wQuery.addLineStyleListener( new SOQLValuesHighlight() );

    FormData fdSettingsGroup = new FormData();
    fdSettingsGroup.left = new FormAttachment( 0, 0 );
    fdSettingsGroup.right = new FormAttachment( 100, 0 );
    fdSettingsGroup.bottom = new FormAttachment( 100, 0 );
    fdSettingsGroup.top = new FormAttachment( wConnectionGroup, margin );
    wSettingsGroup.setLayoutData( fdSettingsGroup );

    // END SETTINGS GROUP
    // ////////////////////////

    fdFileComp = new FormData();
    fdFileComp.left = new FormAttachment( 0, 0 );
    fdFileComp.top = new FormAttachment( 0, 0 );
    fdFileComp.right = new FormAttachment( 100, 0 );
    fdFileComp.bottom = new FormAttachment( 100, 0 );
    wFileComp.setLayoutData( fdFileComp );

    wFileComp.layout();
    wFileTab.setControl( wFileComp );

    // ///////////////////////////////////////////////////////////
    // / END OF FILE TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF CONTENT TAB///
    // /
    wContentTab = new CTabItem( wTabFolder, SWT.NONE );
    wContentTab.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.Content.Tab" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    wContentComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wContentComp );
    wContentComp.setLayout( contentLayout );

    // ///////////////////////////////
    // START OF Advanced GROUP //
    // ///////////////////////////////

    wAdvancedGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wAdvancedGroup );
    wAdvancedGroup.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.AdvancedGroup.Label" ) );

    FormLayout advancedgroupLayout = new FormLayout();
    advancedgroupLayout.marginWidth = 10;
    advancedgroupLayout.marginHeight = 10;
    wAdvancedGroup.setLayout( advancedgroupLayout );

    // RecordsFilter
    wlRecordsFilter = new Label( wAdvancedGroup, SWT.RIGHT );
    wlRecordsFilter.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.RecordsFilter.Label" ) );
    props.setLook( wlRecordsFilter );
    fdlRecordsFilter = new FormData();
    fdlRecordsFilter.left = new FormAttachment( 0, 0 );
    fdlRecordsFilter.right = new FormAttachment( middle, -margin );
    fdlRecordsFilter.top = new FormAttachment( 0, 2 * margin );
    wlRecordsFilter.setLayoutData( fdlRecordsFilter );

    wRecordsFilter = new CCombo( wAdvancedGroup, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wRecordsFilter );
    wRecordsFilter.addModifyListener( lsMod );
    fdRecordsFilter = new FormData();
    fdRecordsFilter.left = new FormAttachment( middle, 0 );
    fdRecordsFilter.top = new FormAttachment( 0, 2 * margin );
    fdRecordsFilter.right = new FormAttachment( 100, -margin );
    wRecordsFilter.setLayoutData( fdRecordsFilter );
    wRecordsFilter.setItems( SalesforceConnectionUtils.recordsFilterDesc );
    wRecordsFilter.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        updateRecordsFilter();
      }
    } );

    // Query All?
    wlQueryAll = new Label( wAdvancedGroup, SWT.RIGHT );
    wlQueryAll.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.QueryAll.Label" ) );
    props.setLook( wlQueryAll );
    FormData fdlQueryAll = new FormData();
    fdlQueryAll.left = new FormAttachment( 0, 0 );
    fdlQueryAll.top = new FormAttachment( wRecordsFilter, margin );
    fdlQueryAll.right = new FormAttachment( middle, -margin );
    wlQueryAll.setLayoutData( fdlQueryAll );
    wQueryAll = new Button( wAdvancedGroup, SWT.CHECK );
    wQueryAll.addSelectionListener( checkBoxModifyListener );
    props.setLook( wQueryAll );
    wQueryAll.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.QueryAll.Tooltip" ) );
    FormData fdQueryAll = new FormData();
    fdQueryAll.left = new FormAttachment( middle, 0 );
    fdQueryAll.top = new FormAttachment( wRecordsFilter, margin );
    wQueryAll.setLayoutData( fdQueryAll );
    wQueryAll.addSelectionListener( new ComponentSelectionListener( input ) );

    open = new Button( wAdvancedGroup, SWT.PUSH );
    open.setImage( GUIResource.getInstance().getImageCalendar() );
    open.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.OpenCalendar" ) );
    FormData fdlButton = new FormData();
    fdlButton.top = new FormAttachment( wQueryAll, margin );
    fdlButton.right = new FormAttachment( 100, 0 );
    open.setLayoutData( fdlButton );
    open.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        final Shell dialog = new Shell( shell, SWT.DIALOG_TRIM );
        dialog.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.SelectDate" ) );
        dialog.setImage( GUIResource.getInstance().getImageSpoon() );
        dialog.setLayout( new GridLayout( 3, false ) );

        final DateTime calendar = new DateTime( dialog, SWT.CALENDAR );
        final DateTime time = new DateTime( dialog, SWT.TIME | SWT.TIME );
        new Label( dialog, SWT.NONE );
        new Label( dialog, SWT.NONE );

        Button ok = new Button( dialog, SWT.PUSH );
        ok.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        ok.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        ok.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            wReadFrom.setText( calendar.getYear()
              + "-"
              + ( ( calendar.getMonth() + 1 ) < 10
              ? "0" + ( calendar.getMonth() + 1 ) : ( calendar.getMonth() + 1 ) ) + "-"
              + ( calendar.getDay() < 10 ? "0" + calendar.getDay() : calendar.getDay() ) + " "
              + ( time.getHours() < 10 ? "0" + time.getHours() : time.getHours() ) + ":"
              + ( time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes() ) + ":"
              + ( time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes() ) );

            dialog.close();
          }
        } );
        dialog.setDefaultButton( ok );
        dialog.pack();
        dialog.open();
      }
    } );

    wlReadFrom = new Label( wAdvancedGroup, SWT.RIGHT );
    wlReadFrom.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.ReadFrom.Label" ) );
    props.setLook( wlReadFrom );
    fdlReadFrom = new FormData();
    fdlReadFrom.left = new FormAttachment( 0, 0 );
    fdlReadFrom.top = new FormAttachment( wQueryAll, margin );
    fdlReadFrom.right = new FormAttachment( middle, -margin );
    wlReadFrom.setLayoutData( fdlReadFrom );
    wReadFrom = new TextVar( transMeta, wAdvancedGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wReadFrom.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.ReadFrom.Tooltip" ) );
    props.setLook( wReadFrom );
    wReadFrom.addModifyListener( lsMod );
    fdReadFrom = new FormData();
    fdReadFrom.left = new FormAttachment( middle, 0 );
    fdReadFrom.top = new FormAttachment( wQueryAll, margin );
    fdReadFrom.right = new FormAttachment( open, -margin );
    wReadFrom.setLayoutData( fdReadFrom );

    opento = new Button( wAdvancedGroup, SWT.PUSH );
    opento.setImage( GUIResource.getInstance().getImageCalendar() );
    opento.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.OpenCalendar" ) );
    FormData fdlButtonto = new FormData();
    fdlButtonto.top = new FormAttachment( wReadFrom, 2 * margin );
    fdlButtonto.right = new FormAttachment( 100, 0 );
    opento.setLayoutData( fdlButtonto );
    opento.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        final Shell dialogto = new Shell( shell, SWT.DIALOG_TRIM );
        dialogto.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.SelectDate" ) );
        dialogto.setImage( GUIResource.getInstance().getImageSpoon() );
        dialogto.setLayout( new GridLayout( 3, false ) );

        final DateTime calendarto = new DateTime( dialogto, SWT.CALENDAR | SWT.BORDER );
        final DateTime timeto = new DateTime( dialogto, SWT.TIME | SWT.TIME );
        new Label( dialogto, SWT.NONE );
        new Label( dialogto, SWT.NONE );
        Button okto = new Button( dialogto, SWT.PUSH );
        okto.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        okto.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        okto.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            wReadTo.setText( calendarto.getYear()
              + "-"
              + ( ( calendarto.getMonth() + 1 ) < 10 ? "0" + ( calendarto.getMonth() + 1 ) : ( calendarto
              .getMonth() + 1 ) ) + "-"
              + ( calendarto.getDay() < 10 ? "0" + calendarto.getDay() : calendarto.getDay() ) + " "
              + ( timeto.getHours() < 10 ? "0" + timeto.getHours() : timeto.getHours() ) + ":"
              + ( timeto.getMinutes() < 10 ? "0" + timeto.getMinutes() : timeto.getMinutes() ) + ":"
              + ( timeto.getSeconds() < 10 ? "0" + timeto.getSeconds() : timeto.getSeconds() ) );
            dialogto.close();
          }
        } );
        dialogto.setDefaultButton( okto );
        dialogto.pack();
        dialogto.open();
      }
    } );

    wlReadTo = new Label( wAdvancedGroup, SWT.RIGHT );
    wlReadTo.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.ReadTo.Label" ) );
    props.setLook( wlReadTo );
    fdlReadTo = new FormData();
    fdlReadTo.left = new FormAttachment( 0, 0 );
    fdlReadTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdlReadTo.right = new FormAttachment( middle, -margin );
    wlReadTo.setLayoutData( fdlReadTo );
    wReadTo = new TextVar( transMeta, wAdvancedGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wReadTo.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.ReadTo.Tooltip" ) );
    props.setLook( wReadTo );
    wReadTo.addModifyListener( lsMod );
    fdReadTo = new FormData();
    fdReadTo.left = new FormAttachment( middle, 0 );
    fdReadTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdReadTo.right = new FormAttachment( opento, -margin );
    wReadTo.setLayoutData( fdReadTo );

    fdAdvancedGroup = new FormData();
    fdAdvancedGroup.left = new FormAttachment( 0, margin );
    fdAdvancedGroup.top = new FormAttachment( 0, 2 * margin );
    fdAdvancedGroup.right = new FormAttachment( 100, -margin );
    wAdvancedGroup.setLayoutData( fdAdvancedGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Advanced GROUP
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////
    // START OF Additional Fields GROUP //
    // ///////////////////////////////

    wAdditionalFields = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wAdditionalFields );
    wAdditionalFields.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.wAdditionalFields.Label" ) );

    FormLayout AdditionalFieldsgroupLayout = new FormLayout();
    AdditionalFieldsgroupLayout.marginWidth = 10;
    AdditionalFieldsgroupLayout.marginHeight = 10;
    wAdditionalFields.setLayout( AdditionalFieldsgroupLayout );

    // Add Salesforce URL in the output stream ?
    wlInclURL = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclURL.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclURL.Label" ) );
    props.setLook( wlInclURL );
    fdlInclURL = new FormData();
    fdlInclURL.left = new FormAttachment( 0, 0 );
    fdlInclURL.top = new FormAttachment( wAdvancedGroup, margin );
    fdlInclURL.right = new FormAttachment( middle, -margin );
    wlInclURL.setLayoutData( fdlInclURL );
    wInclURL = new Button( wAdditionalFields, SWT.CHECK );
    props.setLook( wInclURL );
    wInclURL.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclURL.Tooltip" ) );
    fdInclURL = new FormData();
    fdInclURL.left = new FormAttachment( middle, 0 );
    fdInclURL.top = new FormAttachment( wAdvancedGroup, margin );
    wInclURL.setLayoutData( fdInclURL );
    wInclURL.addSelectionListener( checkBoxModifyListener );
    wInclURL.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setEnableInclTargetURL();
        input.setChanged();
      }
    } );

    wlInclURLField = new Label( wAdditionalFields, SWT.LEFT );
    wlInclURLField.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclURLField.Label" ) );
    props.setLook( wlInclURLField );
    fdlInclURLField = new FormData();
    fdlInclURLField.left = new FormAttachment( wInclURL, margin );
    fdlInclURLField.top = new FormAttachment( wAdvancedGroup, margin );
    wlInclURLField.setLayoutData( fdlInclURLField );
    wInclURLField = new TextVar( transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wlInclURLField );
    wInclURLField.addModifyListener( lsMod );
    fdInclURLField = new FormData();
    fdInclURLField.left = new FormAttachment( wlInclURLField, margin );
    fdInclURLField.top = new FormAttachment( wAdvancedGroup, margin );
    fdInclURLField.right = new FormAttachment( 100, 0 );
    wInclURLField.setLayoutData( fdInclURLField );

    // Add module in the output stream ?
    wlInclModule = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclModule.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclModule.Label" ) );
    props.setLook( wlInclModule );
    fdlInclModule = new FormData();
    fdlInclModule.left = new FormAttachment( 0, 0 );
    fdlInclModule.top = new FormAttachment( wInclURLField, margin );
    fdlInclModule.right = new FormAttachment( middle, -margin );
    wlInclModule.setLayoutData( fdlInclModule );
    wInclModule = new Button( wAdditionalFields, SWT.CHECK );
    props.setLook( wInclModule );
    wInclModule.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclModule.Tooltip" ) );
    fdModule = new FormData();
    fdModule.left = new FormAttachment( middle, 0 );
    fdModule.top = new FormAttachment( wInclURLField, margin );
    wInclModule.setLayoutData( fdModule );
    wInclModule.addSelectionListener( checkBoxModifyListener );

    wInclModule.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setEnableInclModule();
        input.setChanged();
      }
    } );

    wlInclModuleField = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclModuleField.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclModuleField.Label" ) );
    props.setLook( wlInclModuleField );
    fdlInclModuleField = new FormData();
    fdlInclModuleField.left = new FormAttachment( wInclModule, margin );
    fdlInclModuleField.top = new FormAttachment( wInclURLField, margin );
    wlInclModuleField.setLayoutData( fdlInclModuleField );
    wInclModuleField = new TextVar( transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclModuleField );
    wInclModuleField.addModifyListener( lsMod );
    fdInclModuleField = new FormData();
    fdInclModuleField.left = new FormAttachment( wlInclModuleField, margin );
    fdInclModuleField.top = new FormAttachment( wInclURLField, margin );
    fdInclModuleField.right = new FormAttachment( 100, 0 );
    wInclModuleField.setLayoutData( fdInclModuleField );

    // Add SQL in the output stream ?
    wlInclSQL = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclSQL.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclSQL.Label" ) );
    props.setLook( wlInclSQL );
    fdlInclSQL = new FormData();
    fdlInclSQL.left = new FormAttachment( 0, 0 );
    fdlInclSQL.top = new FormAttachment( wInclModuleField, margin );
    fdlInclSQL.right = new FormAttachment( middle, -margin );
    wlInclSQL.setLayoutData( fdlInclSQL );
    wInclSQL = new Button( wAdditionalFields, SWT.CHECK );
    props.setLook( wInclSQL );
    wInclSQL.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclSQL.Tooltip" ) );
    fdInclSQL = new FormData();
    fdInclSQL.left = new FormAttachment( middle, 0 );
    fdInclSQL.top = new FormAttachment( wInclModuleField, margin );
    wInclSQL.setLayoutData( fdInclSQL );
    wInclSQL.addSelectionListener( checkBoxModifyListener );
    wInclSQL.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setEnableInclSQL();
        input.setChanged();
      }
    } );

    wlInclSQLField = new Label( wAdditionalFields, SWT.LEFT );
    wlInclSQLField.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclSQLField.Label" ) );
    props.setLook( wlInclSQLField );
    fdlInclSQLField = new FormData();
    fdlInclSQLField.left = new FormAttachment( wInclSQL, margin );
    fdlInclSQLField.top = new FormAttachment( wInclModuleField, margin );
    wlInclSQLField.setLayoutData( fdlInclSQLField );
    wInclSQLField = new TextVar( transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wlInclSQLField );
    wInclSQLField.addModifyListener( lsMod );
    fdInclSQLField = new FormData();
    fdInclSQLField.left = new FormAttachment( wlInclSQLField, margin );
    fdInclSQLField.top = new FormAttachment( wInclModuleField, margin );
    fdInclSQLField.right = new FormAttachment( 100, 0 );
    wInclSQLField.setLayoutData( fdInclSQLField );

    // Add Timestamp in the output stream ?
    wlInclTimestamp = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclTimestamp.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclTimestamp.Label" ) );
    props.setLook( wlInclTimestamp );
    fdlInclTimestamp = new FormData();
    fdlInclTimestamp.left = new FormAttachment( 0, 0 );
    fdlInclTimestamp.top = new FormAttachment( wInclSQLField, margin );
    fdlInclTimestamp.right = new FormAttachment( middle, -margin );
    wlInclTimestamp.setLayoutData( fdlInclTimestamp );
    wInclTimestamp = new Button( wAdditionalFields, SWT.CHECK );
    props.setLook( wInclTimestamp );
    wInclTimestamp.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclTimestamp.Tooltip" ) );
    fdInclTimestamp = new FormData();
    fdInclTimestamp.left = new FormAttachment( middle, 0 );
    fdInclTimestamp.top = new FormAttachment( wInclSQLField, margin );
    wInclTimestamp.setLayoutData( fdInclTimestamp );
    wInclTimestamp.addSelectionListener( checkBoxModifyListener );
    wInclTimestamp.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setEnableInclTimestamp();
        input.setChanged();
      }
    } );

    wlInclTimestampField = new Label( wAdditionalFields, SWT.LEFT );
    wlInclTimestampField.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclTimestampField.Label" ) );
    props.setLook( wlInclTimestampField );
    fdlInclTimestampField = new FormData();
    fdlInclTimestampField.left = new FormAttachment( wInclTimestamp, margin );
    fdlInclTimestampField.top = new FormAttachment( wInclSQLField, margin );
    wlInclTimestampField.setLayoutData( fdlInclTimestampField );
    wInclTimestampField = new TextVar( transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wlInclTimestampField );
    wInclTimestampField.addModifyListener( lsMod );
    fdInclTimestampField = new FormData();
    fdInclTimestampField.left = new FormAttachment( wlInclTimestampField, margin );
    fdInclTimestampField.top = new FormAttachment( wInclSQLField, margin );
    fdInclTimestampField.right = new FormAttachment( 100, 0 );
    wInclTimestampField.setLayoutData( fdInclTimestampField );

    // Include Rownum in output stream?
    wlInclRownum = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclRownum.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclRownum.Label" ) );
    props.setLook( wlInclRownum );
    FormData fdlInclRownum = new FormData();
    fdlInclRownum.left = new FormAttachment( 0, 0 );
    fdlInclRownum.top = new FormAttachment( wInclTimestampField, margin );
    fdlInclRownum.right = new FormAttachment( middle, -margin );
    wlInclRownum.setLayoutData( fdlInclRownum );
    wInclRownum = new Button( wAdditionalFields, SWT.CHECK );
    props.setLook( wInclRownum );
    wInclRownum.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclRownum.Tooltip" ) );
    FormData fdRownum = new FormData();
    fdRownum.left = new FormAttachment( middle, 0 );
    fdRownum.top = new FormAttachment( wInclTimestampField, margin );
    wInclRownum.setLayoutData( fdRownum );
    wInclRownum.addSelectionListener( checkBoxModifyListener );

    wInclRownum.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setEnableInclRownum();
        input.setChanged();
      }
    } );

    wlInclRownumField = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclRownumField.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclRownumField.Label" ) );
    props.setLook( wlInclRownumField );
    fdlInclRownumField = new FormData();
    fdlInclRownumField.left = new FormAttachment( wInclRownum, margin );
    fdlInclRownumField.top = new FormAttachment( wInclTimestampField, margin );
    wlInclRownumField.setLayoutData( fdlInclRownumField );
    wInclRownumField = new TextVar( transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclRownumField );
    wInclRownumField.addModifyListener( lsMod );
    FormData fdInclRownumField = new FormData();
    fdInclRownumField.left = new FormAttachment( wlInclRownumField, margin );
    fdInclRownumField.top = new FormAttachment( wInclTimestampField, margin );
    fdInclRownumField.right = new FormAttachment( 100, 0 );
    wInclRownumField.setLayoutData( fdInclRownumField );

    // Include DeletionDate in output stream?
    wlInclDeletionDate = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclDeletionDate.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.InclDeletionDate.Label" ) );
    props.setLook( wlInclDeletionDate );
    fdlInclDeletionDate = new FormData();
    fdlInclDeletionDate.left = new FormAttachment( 0, 0 );
    fdlInclDeletionDate.top = new FormAttachment( wInclRownumField, margin );
    fdlInclDeletionDate.right = new FormAttachment( middle, -margin );
    wlInclDeletionDate.setLayoutData( fdlInclDeletionDate );
    wInclDeletionDate = new Button( wAdditionalFields, SWT.CHECK );
    props.setLook( wInclDeletionDate );
    wInclDeletionDate.setToolTipText( BaseMessages.getString(
      PKG, "SalesforceInputDialog.InclDeletionDate.Tooltip" ) );
    fdDeletionDate = new FormData();
    fdDeletionDate.left = new FormAttachment( middle, 0 );
    fdDeletionDate.top = new FormAttachment( wInclRownumField, margin );
    wInclDeletionDate.setLayoutData( fdDeletionDate );
    wInclDeletionDate.addSelectionListener( checkBoxModifyListener );

    wInclDeletionDate.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setEnableInclDeletionDate();
        input.setChanged();
      }
    } );

    wlInclDeletionDateField = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclDeletionDateField.setText( BaseMessages.getString(
      PKG, "SalesforceInputDialog.InclDeletionDateField.Label" ) );
    props.setLook( wlInclDeletionDateField );
    fdlInclDeletionDateField = new FormData();
    fdlInclDeletionDateField.left = new FormAttachment( wInclDeletionDate, margin );
    fdlInclDeletionDateField.top = new FormAttachment( wInclRownumField, margin );
    wlInclDeletionDateField.setLayoutData( fdlInclDeletionDateField );
    wInclDeletionDateField = new TextVar( transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclDeletionDateField );
    wInclDeletionDateField.addModifyListener( lsMod );
    fdInclDeletionDateField = new FormData();
    fdInclDeletionDateField.left = new FormAttachment( wlInclDeletionDateField, margin );
    fdInclDeletionDateField.top = new FormAttachment( wInclRownumField, margin );
    fdInclDeletionDateField.right = new FormAttachment( 100, 0 );
    wInclDeletionDateField.setLayoutData( fdInclDeletionDateField );

    fdAdditionalFields = new FormData();
    fdAdditionalFields.left = new FormAttachment( 0, margin );
    fdAdditionalFields.top = new FormAttachment( wAdvancedGroup, margin );
    fdAdditionalFields.right = new FormAttachment( 100, -margin );
    wAdditionalFields.setLayoutData( fdAdditionalFields );

    // ///////////////////////////////////////////////////////////
    // / END OF Additional Fields GROUP
    // ///////////////////////////////////////////////////////////

    // Timeout
    wlTimeOut = new Label( wContentComp, SWT.RIGHT );
    wlTimeOut.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.TimeOut.Label" ) );
    props.setLook( wlTimeOut );
    fdlTimeOut = new FormData();
    fdlTimeOut.left = new FormAttachment( 0, 0 );
    fdlTimeOut.top = new FormAttachment( wAdditionalFields, 2 * margin );
    fdlTimeOut.right = new FormAttachment( middle, -margin );
    wlTimeOut.setLayoutData( fdlTimeOut );
    wTimeOut = new TextVar( transMeta, wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTimeOut );
    wTimeOut.addModifyListener( lsMod );
    fdTimeOut = new FormData();
    fdTimeOut.left = new FormAttachment( middle, 0 );
    fdTimeOut.top = new FormAttachment( wAdditionalFields, 2 * margin );
    fdTimeOut.right = new FormAttachment( 100, 0 );
    wTimeOut.setLayoutData( fdTimeOut );

    // Use compression?
    wlUseCompression = new Label( wContentComp, SWT.RIGHT );
    wlUseCompression.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.UseCompression.Label" ) );
    props.setLook( wlUseCompression );
    FormData fdlUseCompression = new FormData();
    fdlUseCompression.left = new FormAttachment( 0, 0 );
    fdlUseCompression.top = new FormAttachment( wTimeOut, margin );
    fdlUseCompression.right = new FormAttachment( middle, -margin );
    wlUseCompression.setLayoutData( fdlUseCompression );
    wUseCompression = new Button( wContentComp, SWT.CHECK );
    wUseCompression.addSelectionListener( checkBoxModifyListener );
    props.setLook( wUseCompression );
    wUseCompression.setToolTipText( BaseMessages.getString( PKG, "SalesforceInputDialog.UseCompression.Tooltip" ) );
    FormData fdUseCompression = new FormData();
    fdUseCompression.left = new FormAttachment( middle, 0 );
    fdUseCompression.top = new FormAttachment( wTimeOut, margin );
    wUseCompression.setLayoutData( fdUseCompression );
    wUseCompression.addSelectionListener( new ComponentSelectionListener( input ) );

    // Limit rows
    wlLimit = new Label( wContentComp, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.Limit.Label" ) );
    props.setLook( wlLimit );
    fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.top = new FormAttachment( wUseCompression, margin );
    fdlLimit.right = new FormAttachment( middle, -margin );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new TextVar( transMeta, wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );
    fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.top = new FormAttachment( wUseCompression, margin );
    fdLimit.right = new FormAttachment( 100, 0 );
    wLimit.setLayoutData( fdLimit );

    fdContentComp = new FormData();
    fdContentComp.left = new FormAttachment( 0, 0 );
    fdContentComp.top = new FormAttachment( 0, 0 );
    fdContentComp.right = new FormAttachment( 100, 0 );
    fdContentComp.bottom = new FormAttachment( 100, 0 );
    wContentComp.setLayoutData( fdContentComp );

    wContentComp.layout();
    wContentTab.setControl( wContentComp );

    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT TAB
    // ///////////////////////////////////////////////////////////

    // Fields tab...
    //
    wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.Fields.Tab" ) );

    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = Const.FORM_MARGIN;
    fieldsLayout.marginHeight = Const.FORM_MARGIN;

    wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    wFieldsComp.setLayout( fieldsLayout );
    props.setLook( wFieldsComp );

    wGet = new Button( wFieldsComp, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.GetFields.Button" ) );
    fdGet = new FormData();
    fdGet.left = new FormAttachment( 50, 0 );
    fdGet.bottom = new FormAttachment( 100, 0 );
    wGet.setLayoutData( fdGet );

    final int FieldsRows = input.getInputFields().length;

    colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Name.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Field.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.IsIdLookup.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {
          BaseMessages.getString( PKG, "System.Combo.Yes" ),
          BaseMessages.getString( PKG, "System.Combo.No" ) }, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Type.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames(), true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Format.Column" ),
          ColumnInfo.COLUMN_TYPE_FORMAT, 3 ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Length.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Precision.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Currency.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Decimal.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Group.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.TrimType.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, SalesforceInputField.trimTypeDesc, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Repeat.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {
          BaseMessages.getString( PKG, "System.Combo.Yes" ),
          BaseMessages.getString( PKG, "System.Combo.No" ) }, true ),

      };

    colinf[ 0 ].setUsingVariables( true );
    colinf[ 0 ].setToolTip( BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Name.Column.Tooltip" ) );
    colinf[ 1 ].setUsingVariables( true );
    colinf[ 1 ].setToolTip( BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsTable.Field.Column.Tooltip" ) );
    colinf[ 2 ].setReadOnly( true );
    wFields =
      new TableView( transMeta, wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( wGet, -margin );
    wFields.setLayoutData( fdFields );

    fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment( 0, 0 );
    fdFieldsComp.top = new FormAttachment( 0, 0 );
    fdFieldsComp.right = new FormAttachment( 100, 0 );
    fdFieldsComp.bottom = new FormAttachment( 100, 0 );
    wFieldsComp.setLayoutData( fdFieldsComp );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.Button.PreviewRows" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder );

    // Add listeners
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsTest = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        test();
      }
    };
    lsGet = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        get();
        shell.setCursor( null );
        busy.dispose();
        input.setChanged();
      }
    };
    lsPreview = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );
    wTest.addListener( SWT.Selection, lsTest );
    wPreview.addListener( SWT.Selection, lsPreview );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wLimit.addSelectionListener( lsDef );
    wInclModuleField.addSelectionListener( lsDef );
    wInclURLField.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wTabFolder.setSelection( 0 );

    // Set the shell size, based upon previous time...
    setSize();
    getData( input );
    setEnableInclTargetURL();
    setEnableInclSQL();
    setEnableInclTimestamp();
    setEnableInclModule();
    setEnableInclRownum();
    setEnableInclDeletionDate();
    setEnableQuery();

    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void setEnableInclTargetURL() {
    wInclURLField.setEnabled( wInclURL.getSelection() );
    wlInclURLField.setEnabled( wInclURL.getSelection() );
  }

  private void setEnableQuery() {
    wQuery.setVisible( wspecifyQuery.getSelection() );
    wlCondition.setVisible( !wspecifyQuery.getSelection() );
    wCondition.setVisible( !wspecifyQuery.getSelection() );
    wlModule.setVisible( !wspecifyQuery.getSelection() );
    wModule.setVisible( !wspecifyQuery.getSelection() );

    if ( wspecifyQuery.getSelection() ) {
      if ( wInclModule.getSelection() ) {
        wInclModule.setSelection( false );
      }
      wRecordsFilter.setText( SalesforceConnectionUtils
        .getRecordsFilterDesc( SalesforceConnectionUtils.RECORDS_FILTER_ALL ) );
    }
    wlInclModule.setEnabled( !wspecifyQuery.getSelection() );
    wInclModule.setEnabled( !wspecifyQuery.getSelection() );
    setEnableInclModule();
    wlRecordsFilter.setEnabled( !wspecifyQuery.getSelection() );
    wRecordsFilter.setEnabled( !wspecifyQuery.getSelection() );
    updateRecordsFilter();
    enableCondition();
  }

  private void setEnableInclSQL() {
    wInclSQLField.setEnabled( wInclSQL.getSelection() );
    wlInclSQLField.setEnabled( wInclSQL.getSelection() );
  }

  private void setEnableInclTimestamp() {
    wInclTimestampField.setEnabled( wInclTimestamp.getSelection() );
    wlInclTimestampField.setEnabled( wInclTimestamp.getSelection() );
  }

  private void setEnableInclModule() {
    wInclModuleField.setEnabled( wInclModule.getSelection() && !wspecifyQuery.getSelection() );
    wlInclModuleField.setEnabled( wInclModule.getSelection() && !wspecifyQuery.getSelection() );
  }

  private void setEnableInclRownum() {
    wInclRownumField.setEnabled( wInclRownum.getSelection() );
    wlInclRownumField.setEnabled( wInclRownum.getSelection() );
  }

  private void setEnableInclDeletionDate() {
    wInclDeletionDateField.setEnabled( wInclDeletionDate.getSelection() );
    wlInclDeletionDateField.setEnabled( wInclDeletionDate.getSelection() );
  }

  private void get() {

    try {
      Trans trans = new Trans( transMeta, null );
      trans.rowsets = new ArrayList<>();

      getInfo( meta );
      // Clear Fields Grid
      wFields.removeAll();
      SalesforceInput step = (SalesforceInput) meta.getStep( stepMeta, meta.getStepData(), 0, transMeta, trans );
      step.setStepMetaInterface( meta );
      FieldsResponse fieldsResponse = step.getFields();

      for ( FieldDTO field : fieldsResponse.fieldDTOList ) {
        addFieldToTable(
          field.getName(), field.getField(), field.isIdlookup(), field.getType(), field.getLength(),
          field.getPrecision() );
      }

      if ( !fieldsResponse.fieldNames.isEmpty() ) {
        String[] fieldsName = fieldsResponse.fieldNames.toArray( new String[ fieldsResponse.fieldNames.size() ] );
        colinf[ 1 ].setComboValues( fieldsName );
      }
      wFields.removeEmptyRows();
      wFields.setRowNums();
      wFields.optWidth( true );
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogTitle" ), BaseMessages
        .getString( PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogMessage" ), e );
    } catch ( Exception e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogTitle" ), BaseMessages
        .getString( PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogMessage" ), e );
    }
  }

  void addFieldToTable( String fieldLabel, String fieldName, boolean fieldIdIsLookup, String fieldType,
                        String fieldLength, String fieldPrecision ) {
    TableItem item = new TableItem( wFields.table, SWT.NONE );
    item.setText( 1, fieldLabel );
    item.setText( 2, fieldName );
    item.setText( 3, fieldIdIsLookup ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString(
      PKG, "System.Combo.No" ) );

    // Try to get the Type
    if ( fieldType.equals( "boolean" ) ) {
      item.setText( 4, "Boolean" );
    } else if ( fieldType.equals( "date" ) ) {
      item.setText( 4, "Date" );
      item.setText( 5, DEFAULT_DATE_FORMAT );
    } else if ( fieldType.equals( "datetime" ) ) {
      item.setText( 4, "Date" );
      item.setText( 5, DEFAULT_DATE_TIME_FORMAT );
    } else if ( fieldType.equals( "double" ) ) {
      item.setText( 4, "Number" );
    } else if ( fieldType.equals( "int" ) ) {
      item.setText( 4, "Integer" );
    } else if ( fieldType.equals( "base64" ) ) {
      item.setText( 4, "Binary" );
    } else {
      item.setText( 4, "String" );
    }

    if ( fieldLength != null ) {
      item.setText( 6, fieldLength );
    }
    // Get precision
    if ( fieldPrecision != null ) {
      item.setText( 7, fieldPrecision );
    }
  }

  private void updateRecordsFilter() {
    boolean activeFilter =
      ( !wspecifyQuery.getSelection() && SalesforceConnectionUtils.getRecordsFilterByDesc( wRecordsFilter
        .getText() ) != SalesforceConnectionUtils.RECORDS_FILTER_ALL );

    wlReadFrom.setEnabled( activeFilter );
    wReadFrom.setEnabled( activeFilter );
    open.setEnabled( activeFilter );
    wlReadTo.setEnabled( activeFilter );
    wReadTo.setEnabled( activeFilter );
    opento.setEnabled( activeFilter );
    wlQueryAll.setEnabled( !activeFilter );
    wQueryAll.setEnabled( !activeFilter );
    enableCondition();
    boolean activateDeletionDate =
      SalesforceConnectionUtils.getRecordsFilterByDesc( wRecordsFilter.getText() )
        == SalesforceConnectionUtils.RECORDS_FILTER_DELETED;
    if ( !activateDeletionDate ) {
      wInclDeletionDate.setSelection( false );
    }
    wlInclDeletionDate.setEnabled( activateDeletionDate );
    wInclDeletionDate.setEnabled( activateDeletionDate );
    wlInclDeletionDateField.setEnabled( activateDeletionDate );
    wInclDeletionDateField.setEnabled( activateDeletionDate );
  }

  /**
   * Read the data from the TextFileInputMeta object and show it in this dialog.
   *
   * @param in The SalesforceInputMeta object to obtain the data from.
   */
  public void getData( SalesforceInputMeta in ) {
    wURL.setText( Const.NVL( in.getTargetURL(), "" ) );
    wUserName.setText( Const.NVL( in.getUsername(), "" ) );
    wPassword.setText( Const.NVL( in.getPassword(), "" ) );
    wModule.setText( Const.NVL( in.getModule(), "Account" ) );
    wCondition.setText( Const.NVL( in.getCondition(), "" ) );

    wspecifyQuery.setSelection( in.isSpecifyQuery() );
    wQuery.setText( Const.NVL( in.getQuery(), "" ) );
    wRecordsFilter.setText( SalesforceConnectionUtils.getRecordsFilterDesc( input.getRecordsFilter() ) );
    wInclURLField.setText( Const.NVL( in.getTargetURLField(), "" ) );
    wInclURL.setSelection( in.includeTargetURL() );

    wInclSQLField.setText( Const.NVL( in.getSQLField(), "" ) );
    wInclSQL.setSelection( in.includeSQL() );

    wInclTimestampField.setText( Const.NVL( in.getTimestampField(), "" ) );
    wInclTimestamp.setSelection( in.includeTimestamp() );
    wInclDeletionDateField.setText( Const.NVL( in.getDeletionDateField(), "" ) );
    wInclDeletionDate.setSelection( in.includeDeletionDate() );

    wInclModuleField.setText( Const.NVL( in.getModuleField(), "" ) );
    wInclModule.setSelection( in.includeModule() );

    wInclRownumField.setText( Const.NVL( in.getRowNumberField(), "" ) );
    wInclRownum.setSelection( in.includeRowNumber() );

    wTimeOut.setText( Const.NVL( in.getTimeout(), SalesforceConnectionUtils.DEFAULT_TIMEOUT ) );
    wUseCompression.setSelection( in.isCompression() );
    wQueryAll.setSelection( in.isQueryAll() );
    wLimit.setText( "" + in.getRowLimit() );

    wReadFrom.setText( Const.NVL( in.getReadFrom(), "" ) );
    wReadTo.setText( Const.NVL( in.getReadTo(), "" ) );

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "SalesforceInputDialog.Log.GettingFieldsInfo" ) );
    }
    for ( int i = 0; i < in.getInputFields().length; i++ ) {
      SalesforceInputField field = in.getInputFields()[ i ];

      if ( field != null ) {
        TableItem item = wFields.table.getItem( i );
        String name = field.getName();
        String path = field.getField();
        String isidlookup =
          field.isIdLookup() ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString(
            PKG, "System.Combo.No" );
        String type = field.getTypeDesc();
        String format = field.getFormat();
        String length = "" + field.getLength();
        String prec = "" + field.getPrecision();
        String curr = field.getCurrencySymbol();
        String group = field.getGroupSymbol();
        String decim = field.getDecimalSymbol();
        String trim = field.getTrimTypeDesc();
        String rep =
          field.isRepeated() ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString(
            PKG, "System.Combo.No" );

        if ( name != null ) {
          item.setText( 1, name );
        }
        if ( path != null ) {
          item.setText( 2, path );
        }
        if ( isidlookup != null ) {
          item.setText( 3, isidlookup );
        }
        if ( type != null ) {
          item.setText( 4, type );
        }
        if ( format != null ) {
          item.setText( 5, format );
        }
        if ( length != null && !"-1".equals( length ) ) {
          item.setText( 6, length );
        }
        if ( prec != null && !"-1".equals( prec ) ) {
          item.setText( 7, prec );
        }
        if ( curr != null ) {
          item.setText( 8, curr );
        }
        if ( decim != null ) {
          item.setText( 9, decim );
        }
        if ( group != null ) {
          item.setText( 10, group );
        }
        if ( trim != null ) {
          item.setText( 11, trim );
        }
        if ( rep != null ) {
          item.setText( 12, rep );
        }
      }
    }

    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText();
    try {
      getInfo( input );
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "SalesforceInputDialog.ErrorValidateData.DialogTitle" ),
        BaseMessages.getString( PKG, "SalesforceInputDialog.ErrorValidateData.DialogMessage" ), e );
    }
    dispose();
  }

  @Override
  protected void getInfo( SalesforceStepMeta in ) throws KettleException {
    SalesforceInputMeta meta = (SalesforceInputMeta) in;

    stepname = wStepname.getText(); // return value

    // copy info to SalesforceInputMeta class (input)
    meta.setTargetURL( Const.NVL( wURL.getText(), SalesforceConnectionUtils.TARGET_DEFAULT_URL ) );
    meta.setUsername( Const.NVL( wUserName.getText(), "" ) );
    meta.setPassword( Const.NVL( wPassword.getText(), "" ) );
    meta.setModule( Const.NVL( wModule.getText(), "Account" ) );
    meta.setCondition( Const.NVL( wCondition.getText(), "" ) );

    meta.setSpecifyQuery( wspecifyQuery.getSelection() );
    meta.setQuery( Const.NVL( wQuery.getText(), "" ) );
    meta.setCompression( wUseCompression.getSelection() );
    meta.setQueryAll( wQueryAll.getSelection() );
    meta.setTimeout( Const.NVL( wTimeOut.getText(), "0" ) );
    meta.setRowLimit( Const.NVL( wLimit.getText(), "0" ) );
    meta.setTargetURLField( Const.NVL( wInclURLField.getText(), "" ) );
    meta.setSQLField( Const.NVL( wInclSQLField.getText(), "" ) );
    meta.setTimestampField( Const.NVL( wInclTimestampField.getText(), "" ) );
    meta.setModuleField( Const.NVL( wInclModuleField.getText(), "" ) );
    meta.setRowNumberField( Const.NVL( wInclRownumField.getText(), "" ) );
    meta.setRecordsFilter( SalesforceConnectionUtils.getRecordsFilterByDesc( wRecordsFilter.getText() ) );
    meta.setIncludeTargetURL( wInclURL.getSelection() );
    meta.setIncludeSQL( wInclSQL.getSelection() );
    meta.setIncludeTimestamp( wInclTimestamp.getSelection() );
    meta.setIncludeModule( wInclModule.getSelection() );
    meta.setIncludeRowNumber( wInclRownum.getSelection() );
    meta.setReadFrom( wReadFrom.getText() );
    meta.setReadTo( wReadTo.getText() );
    meta.setDeletionDateField( Const.NVL( wInclDeletionDateField.getText(), "" ) );
    meta.setIncludeDeletionDate( wInclDeletionDate.getSelection() );
    int nrFields = wFields.nrNonEmpty();

    meta.allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      SalesforceInputField field = new SalesforceInputField();

      TableItem item = wFields.getNonEmpty( i );

      field.setName( item.getText( 1 ) );
      field.setField( item.getText( 2 ) );
      field.setIdLookup( BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( 3 ) ) );
      field.setType( ValueMetaFactory.getIdForValueMeta( item.getText( 4 ) ) );
      field.setFormat( item.getText( 5 ) );
      field.setLength( Const.toInt( item.getText( 6 ), -1 ) );
      field.setPrecision( Const.toInt( item.getText( 7 ), -1 ) );
      field.setCurrencySymbol( item.getText( 8 ) );
      field.setDecimalSymbol( item.getText( 9 ) );
      field.setGroupSymbol( item.getText( 10 ) );
      field.setTrimType( SalesforceInputField.getTrimTypeByDesc( item.getText( 11 ) ) );
      field.setRepeated( BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( 12 ) ) );

      //CHECKSTYLE:Indentation:OFF
      meta.getInputFields()[ i ] = field;
    }
  }

  // Preview the data
  private void preview() {
    try {
      SalesforceInputMeta oneMeta = new SalesforceInputMeta();
      getInfo( oneMeta );

      // check if the path is given

      TransMeta previewMeta =
        TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

      EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
        BaseMessages.getString( PKG, "SalesforceInputDialog.NumberRows.DialogTitle" ),
        BaseMessages.getString( PKG, "SalesforceInputDialog.NumberRows.DialogMessage" ) );
      int previewSize = numberDialog.open();
      if ( previewSize > 0 ) {
        TransPreviewProgressDialog progressDialog =
          new TransPreviewProgressDialog(
            shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
        progressDialog.open();

        if ( !progressDialog.isCancelled() ) {
          Trans trans = progressDialog.getTrans();
          String loggingText = progressDialog.getLoggingText();

          if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
            EnterTextDialog etd =
              new EnterTextDialog(
                shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
                .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
            etd.setReadOnly();
            etd.open();
          }

          PreviewRowsDialog prd =
            new PreviewRowsDialog(
              shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
              .getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
          prd.open();
        }
      }
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages
        .getString( PKG, "SalesforceInputDialog.ErrorPreviewingData.DialogTitle" ), BaseMessages.getString(
        PKG, "SalesforceInputDialog.ErrorPreviewingData.DialogMessage" ), e );
    }
  }

  private void getModulesList() {
    if ( !gotModule ) {
      String selectedField = wModule.getText();
      wModule.removeAll();

      try {
        Trans trans = new Trans( transMeta, null );
        trans.rowsets = new ArrayList<>();

        getInfo( meta );
        SalesforceStep step = (SalesforceStep) meta.getStep( stepMeta, meta.getStepData(), 0, transMeta, trans );
        step.setStepMetaInterface( meta );

        if ( !Utils.isEmpty( selectedField ) ) {
          wModule.setText( selectedField );
        }

        gotModule = true;
        getModulesListError = false;
        String[] modules = step.getModules( "true" );
        if ( modules != null && modules.length > 0 ) {
          // populate Combo
          wModule.setItems( modules );
        }

        gotModule = true;
        getModulesListError = false;
      } catch ( Exception e ) {
        new ErrorDialog( shell, BaseMessages.getString(
          PKG, "SalesforceInputDialog.ErrorRetrieveModules.DialogTitle" ), BaseMessages.getString(
          PKG, "SalesforceInputDialog.ErrorRetrieveData.ErrorRetrieveModules" ), e );
        getModulesListError = true;
      } finally {
        if ( !Utils.isEmpty( selectedField ) ) {
          wModule.setText( selectedField );
        }
      }
    }
  }

  protected void setQueryToolTip() {
    StyledTextComp control = wCondition;
    if ( wspecifyQuery.getSelection() ) {
      control = wQuery;
    }
    control.setToolTipText( transMeta.environmentSubstitute( control.getText() ) );
  }

  public void setPosition() {
    StyledTextComp control = wCondition;
    if ( wspecifyQuery.getSelection() ) {
      control = wQuery;
    }

    String scr = control.getText();
    int linenr = control.getLineAtOffset( control.getCaretOffset() ) + 1;
    int posnr = control.getCaretOffset();

    // Go back from position to last CR: how many positions?
    int colnr = 0;
    while ( posnr > 0 && scr.charAt( posnr - 1 ) != '\n' && scr.charAt( posnr - 1 ) != '\r' ) {
      posnr--;
      colnr++;
    }
    wlPosition.setText( BaseMessages.getString( PKG, "SalesforceInputDialog.Position.Label", "" + linenr, ""
      + colnr ) );

  }

  private void enableCondition() {
    boolean enableCondition =
      !wspecifyQuery.getSelection()
        && SalesforceConnectionUtils.getRecordsFilterByDesc( wRecordsFilter.getText() )
        == SalesforceConnectionUtils.RECORDS_FILTER_ALL;
    wlCondition.setVisible( enableCondition );
    wCondition.setVisible( enableCondition );
    wlPosition.setVisible( enableCondition );
  }
}
