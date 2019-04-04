/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entries.msaccessbulkload;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.msaccessbulkload.JobEntryMSAccessBulkLoad;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the Microsoft Access Bulk Load job entry settings.
 *
 * @author Samatar Hassan
 * @since 24-07-2008
 */
@PluginDialog( id = "MS_ACCESS_BULK_LOAD", image = "ui/images/deprecated.svg",
  pluginType = PluginDialog.PluginType.JOBENTRY )
public class JobEntryMSAccessBulkLoadDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryMSAccessBulkLoadDialog.class; // for i18n purposes, needed by Translator2!!

  private static final String[] FILETYPES = new String[] { BaseMessages.getString(
    PKG, "JobEntryMSAccessBulkLoad.Filetype.All" ) };

  private Label wlName;
  private Text wName;
  private FormData fdlName, fdName;

  private Label WSourceFileFoldername;
  private Button wbFileFoldername, wbaEntry, wbSourceFolder;
  private TextVar wSourceFileFoldername;
  private FormData fdlSourceFileFoldername, fdbSourceFileFoldername, fdbaEntry, fdSourceFileFoldername, fdbSourceFolder;

  private Label wlTargetDbname;
  private Button wbTargetDbname;
  private TextVar wTargetDbname;
  private FormData fdlTargetDbname, fdbTargetDbname, fdTargetDbname;

  private Label wlTablename;
  private TextVar wTablename;
  private FormData fdlTablename;
  private FormData fdTablename;

  private Label wlWildcard;
  private TextVar wWildcard;
  private FormData fdlWildcard;
  private FormData fdWildcard;

  private Label wlDelimiter;
  private TextVar wDelimiter;
  private FormData fdlDelimiter;
  private FormData fdDelimiter;

  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;

  private Label wlFields;

  private TableView wFields;

  private FormData fdlFields, fdFields;

  private Button wbDelete; // Delete
  private FormData fdbdDelete;
  private Button wbEdit; // Edit

  private CTabFolder wTabFolder;
  private Composite wGeneralComp, wAdvancedComp;
  private CTabItem wGeneralTab, wAdvancedTab;
  private FormData fdGeneralComp, fdAdvancedComp;
  private FormData fdTabFolder;

  private Group wSourceGroup;
  private FormData fdSourceGroup;

  private Group wTargetGroup;
  private FormData fdTargetGroup;

  private FormData fdbeSourceFileFolder;

  private Group wFileResult;
  private FormData fdFileResult;

  private Label wlSuccessCondition;
  private CCombo wSuccessCondition;
  private FormData fdlSuccessCondition, fdSuccessCondition;

  private Group wSuccessOn;
  private FormData fdSuccessOn;

  private Label wlNrErrorsLessThan;
  private TextVar wNrErrorsLessThan;
  private FormData fdlNrErrorsLessThan, fdNrErrorsLessThan;

  private Label wlAddFileToResult, wlincludeSubFolders;
  private Button wAddFileToResult, wincludeSubFolders;
  private FormData fdlAddFileToResult, fdAddFileToResult, fdlincludeSubFolders, fdincludeSubFolders;

  private JobEntryMSAccessBulkLoad jobEntry;
  private Shell shell;

  private Label wlPrevious;

  private Button wPrevious;

  private FormData fdlPrevious, fdPrevious;

  private SelectionAdapter lsDef;

  private boolean changed;

  public JobEntryMSAccessBulkLoadDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep,
    JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryMSAccessBulkLoad) jobEntryInt;

    if ( this.jobEntry.getName() == null ) {
      this.jobEntry.setName( BaseMessages.getString( JobEntryMSAccessBulkLoad.class, "JobEntryMSAccessBulkLoad.Name.Default" ) );
    }
  }

  public JobEntryInterface open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, props.getJobsDialogStyle() );
    props.setLook( shell );
    JobDialog.setShellImage( shell, jobEntry );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        jobEntry.setChanged();
      }
    };
    changed = jobEntry.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Name line
    wlName = new Label( shell, SWT.RIGHT );
    wlName.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Name.Label" ) );
    props.setLook( wlName );
    fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.right = new FormAttachment( middle, -margin );
    fdlName.top = new FormAttachment( 0, margin );
    wlName.setLayoutData( fdlName );
    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    fdName = new FormData();
    fdName.left = new FormAttachment( middle, 0 );
    fdName.top = new FormAttachment( 0, margin );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Tab.General.Label" ) );

    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF Source GROUP///
    // /
    wSourceGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wSourceGroup );
    wSourceGroup.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.SourceGroup.Group.Label" ) );
    FormLayout SourceGroupgroupLayout = new FormLayout();
    SourceGroupgroupLayout.marginWidth = 10;
    SourceGroupgroupLayout.marginHeight = 10;
    wSourceGroup.setLayout( SourceGroupgroupLayout );

    // previous
    wlPrevious = new Label( wSourceGroup, SWT.RIGHT );
    wlPrevious.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Previous.Label" ) );
    props.setLook( wlPrevious );
    fdlPrevious = new FormData();
    fdlPrevious.left = new FormAttachment( 0, 0 );
    fdlPrevious.top = new FormAttachment( wName, margin );
    fdlPrevious.right = new FormAttachment( middle, -margin );
    wlPrevious.setLayoutData( fdlPrevious );
    wPrevious = new Button( wSourceGroup, SWT.CHECK );
    props.setLook( wPrevious );
    wPrevious.setToolTipText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Previous.Tooltip" ) );
    fdPrevious = new FormData();
    fdPrevious.left = new FormAttachment( middle, 0 );
    fdPrevious.top = new FormAttachment( wName, margin );
    fdPrevious.right = new FormAttachment( 100, 0 );
    wPrevious.setLayoutData( fdPrevious );
    wPrevious.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {

        RefreshArgFromPrevious();

      }
    } );

    // FileFoldername line
    WSourceFileFoldername = new Label( wSourceGroup, SWT.RIGHT );
    WSourceFileFoldername.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.FileFoldername.Label" ) );
    props.setLook( WSourceFileFoldername );
    fdlSourceFileFoldername = new FormData();
    fdlSourceFileFoldername.left = new FormAttachment( 0, 0 );
    fdlSourceFileFoldername.top = new FormAttachment( wPrevious, margin );
    fdlSourceFileFoldername.right = new FormAttachment( middle, -margin );
    WSourceFileFoldername.setLayoutData( fdlSourceFileFoldername );

    // Browse Destination folders button ...
    wbSourceFolder = new Button( wSourceGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbSourceFolder );
    wbSourceFolder.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.BrowseFolders.Label" ) );
    fdbSourceFolder = new FormData();
    fdbSourceFolder.right = new FormAttachment( 100, 0 );
    fdbSourceFolder.top = new FormAttachment( wPrevious, margin );
    wbSourceFolder.setLayoutData( fdbSourceFolder );
    wbSourceFolder.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        DirectoryDialog ddialog = new DirectoryDialog( shell, SWT.OPEN );
        if ( wSourceFileFoldername.getText() != null ) {
          ddialog.setFilterPath( jobMeta.environmentSubstitute( wSourceFileFoldername.getText() ) );
        }

        // Calling open() will open and run the dialog.
        // It will return the selected directory, or
        // null if user cancels
        String dir = ddialog.open();
        if ( dir != null ) {
          // Set the text box to the new selection
          wSourceFileFoldername.setText( dir );
        }

      }
    } );

    // Browse source file button ...
    wbFileFoldername = new Button( wSourceGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbFileFoldername );
    wbFileFoldername.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.BrowseFiles.Label" ) );
    fdbSourceFileFoldername = new FormData();
    fdbSourceFileFoldername.right = new FormAttachment( wbSourceFolder, -margin );
    fdbSourceFileFoldername.top = new FormAttachment( wPrevious, margin );
    wbFileFoldername.setLayoutData( fdbSourceFileFoldername );

    wSourceFileFoldername = new TextVar( jobMeta, wSourceGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSourceFileFoldername );
    wSourceFileFoldername.addModifyListener( lsMod );
    fdSourceFileFoldername = new FormData();
    fdSourceFileFoldername.left = new FormAttachment( middle, 0 );
    fdSourceFileFoldername.top = new FormAttachment( wPrevious, margin );
    fdSourceFileFoldername.right = new FormAttachment( wbFileFoldername, -margin );
    wSourceFileFoldername.setLayoutData( fdSourceFileFoldername );

    // Whenever something changes, set the tooltip to the expanded version:
    wSourceFileFoldername.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wSourceFileFoldername.setToolTipText( jobMeta.environmentSubstitute( wSourceFileFoldername.getText() ) );
      }
    } );

    wbFileFoldername.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        FileDialog dialog = new FileDialog( shell, SWT.OPEN );
        dialog.setFilterExtensions( new String[] { "*" } );
        if ( wSourceFileFoldername.getText() != null ) {
          dialog.setFileName( jobMeta.environmentSubstitute( wSourceFileFoldername.getText() ) );
        }
        dialog.setFilterNames( FILETYPES );
        if ( dialog.open() != null ) {
          wSourceFileFoldername.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
        }
      }
    } );

    // Include sub folders
    wlincludeSubFolders = new Label( wSourceGroup, SWT.RIGHT );
    wlincludeSubFolders
      .setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.includeSubFolders.Label" ) );
    props.setLook( wlincludeSubFolders );
    fdlincludeSubFolders = new FormData();
    fdlincludeSubFolders.left = new FormAttachment( 0, 0 );
    fdlincludeSubFolders.top = new FormAttachment( wSourceFileFoldername, margin );
    fdlincludeSubFolders.right = new FormAttachment( middle, -margin );
    wlincludeSubFolders.setLayoutData( fdlincludeSubFolders );
    wincludeSubFolders = new Button( wSourceGroup, SWT.CHECK );
    props.setLook( wincludeSubFolders );
    wincludeSubFolders.setToolTipText( BaseMessages.getString(
      PKG, "JobEntryMSAccessBulkLoad.includeSubFolders.Tooltip" ) );
    fdincludeSubFolders = new FormData();
    fdincludeSubFolders.left = new FormAttachment( middle, 0 );
    fdincludeSubFolders.top = new FormAttachment( wSourceFileFoldername, margin );
    fdincludeSubFolders.right = new FormAttachment( 100, 0 );
    wincludeSubFolders.setLayoutData( fdincludeSubFolders );
    wincludeSubFolders.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // Wildcard
    wlWildcard = new Label( wSourceGroup, SWT.RIGHT );
    wlWildcard.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Wildcard.Label" ) );
    props.setLook( wlWildcard );
    fdlWildcard = new FormData();
    fdlWildcard.left = new FormAttachment( 0, 0 );
    fdlWildcard.top = new FormAttachment( wincludeSubFolders, margin );
    fdlWildcard.right = new FormAttachment( middle, -margin );
    wlWildcard.setLayoutData( fdlWildcard );
    wWildcard = new TextVar( jobMeta, wSourceGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wWildcard );
    wWildcard.setToolTipText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Wildcard.Tooltip" ) );
    wWildcard.addModifyListener( lsMod );
    fdWildcard = new FormData();
    fdWildcard.left = new FormAttachment( middle, 0 );
    fdWildcard.top = new FormAttachment( wincludeSubFolders, margin );
    fdWildcard.right = new FormAttachment( wbFileFoldername, -margin );
    wWildcard.setLayoutData( fdWildcard );

    // Whenever something changes, set the tooltip to the expanded version:
    wWildcard.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wWildcard.setToolTipText( jobMeta.environmentSubstitute( wWildcard.getText() ) );
      }
    } );

    // Delimiter
    wlDelimiter = new Label( wSourceGroup, SWT.RIGHT );
    wlDelimiter.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Delimiter.Label" ) );
    props.setLook( wlDelimiter );
    fdlDelimiter = new FormData();
    fdlDelimiter.left = new FormAttachment( 0, 0 );
    fdlDelimiter.top = new FormAttachment( wWildcard, margin );
    fdlDelimiter.right = new FormAttachment( middle, -margin );
    wlDelimiter.setLayoutData( fdlDelimiter );
    wDelimiter = new TextVar( jobMeta, wSourceGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDelimiter );
    wDelimiter.setToolTipText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Delimiter.Tooltip" ) );
    wDelimiter.addModifyListener( lsMod );
    fdDelimiter = new FormData();
    fdDelimiter.left = new FormAttachment( middle, 0 );
    fdDelimiter.top = new FormAttachment( wWildcard, margin );
    fdDelimiter.right = new FormAttachment( wbFileFoldername, -margin );
    wDelimiter.setLayoutData( fdDelimiter );

    // Whenever something changes, set the tooltip to the expanded version:
    wDelimiter.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wDelimiter.setToolTipText( jobMeta.environmentSubstitute( wDelimiter.getText() ) );
      }
    } );

    fdSourceGroup = new FormData();
    fdSourceGroup.left = new FormAttachment( 0, margin );
    fdSourceGroup.top = new FormAttachment( wName, margin );
    fdSourceGroup.right = new FormAttachment( 100, -margin );
    wSourceGroup.setLayoutData( fdSourceGroup );
    // ///////////////////////////////////////////////////////////
    // / END OF Source GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Target GROUP///
    // /
    wTargetGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wTargetGroup );
    wTargetGroup.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.TargetGroup.Group.Label" ) );
    FormLayout TargetGroupgroupLayout = new FormLayout();
    TargetGroupgroupLayout.marginWidth = 10;
    TargetGroupgroupLayout.marginHeight = 10;
    wTargetGroup.setLayout( TargetGroupgroupLayout );

    // Target Db name line
    wlTargetDbname = new Label( wTargetGroup, SWT.RIGHT );
    wlTargetDbname.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.TargetDbname.Label" ) );
    props.setLook( wlTargetDbname );
    fdlTargetDbname = new FormData();
    fdlTargetDbname.left = new FormAttachment( 0, 0 );
    fdlTargetDbname.top = new FormAttachment( wSourceGroup, margin );
    fdlTargetDbname.right = new FormAttachment( middle, -margin );
    wlTargetDbname.setLayoutData( fdlTargetDbname );
    wbTargetDbname = new Button( wTargetGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTargetDbname );
    wbTargetDbname.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbTargetDbname = new FormData();
    fdbTargetDbname.right = new FormAttachment( 100, 0 );
    fdbTargetDbname.top = new FormAttachment( wSourceGroup, margin );
    wbTargetDbname.setLayoutData( fdbTargetDbname );
    wTargetDbname = new TextVar( jobMeta, wTargetGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTargetDbname );
    wTargetDbname.addModifyListener( lsMod );
    fdTargetDbname = new FormData();
    fdTargetDbname.left = new FormAttachment( middle, 0 );
    fdTargetDbname.top = new FormAttachment( wSourceGroup, margin );
    fdTargetDbname.right = new FormAttachment( wbTargetDbname, -margin );
    wTargetDbname.setLayoutData( fdTargetDbname );

    // Whenever something changes, set the tooltip to the expanded version:
    wTargetDbname.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wTargetDbname.setToolTipText( jobMeta.environmentSubstitute( wTargetDbname.getText() ) );
      }
    } );

    wbTargetDbname.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        FileDialog dialog = new FileDialog( shell, SWT.OPEN );
        dialog.setFilterExtensions( new String[] { "*" } );
        if ( wTargetDbname.getText() != null ) {
          dialog.setFileName( jobMeta.environmentSubstitute( wTargetDbname.getText() ) );
        }
        dialog.setFilterNames( FILETYPES );
        if ( dialog.open() != null ) {
          wTargetDbname.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
        }
      }
    } );

    // Tablename
    wlTablename = new Label( wTargetGroup, SWT.RIGHT );
    wlTablename.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Tablename.Label" ) );
    props.setLook( wlTablename );
    fdlTablename = new FormData();
    fdlTablename.left = new FormAttachment( 0, 0 );
    fdlTablename.top = new FormAttachment( wTargetDbname, margin );
    fdlTablename.right = new FormAttachment( middle, -margin );
    wlTablename.setLayoutData( fdlTablename );
    wTablename = new TextVar( jobMeta, wTargetGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTablename );
    wTablename.setToolTipText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Tablename.Tooltip" ) );
    wTablename.addModifyListener( lsMod );
    fdTablename = new FormData();
    fdTablename.left = new FormAttachment( middle, 0 );
    fdTablename.top = new FormAttachment( wTargetDbname, margin );
    fdTablename.right = new FormAttachment( wbTargetDbname, -margin );
    wTablename.setLayoutData( fdTablename );

    // Whenever something changes, set the tooltip to the expanded version:
    wTablename.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wTablename.setToolTipText( jobMeta.environmentSubstitute( wTablename.getText() ) );
      }
    } );

    fdTargetGroup = new FormData();
    fdTargetGroup.left = new FormAttachment( 0, margin );
    fdTargetGroup.top = new FormAttachment( wSourceGroup, margin );
    fdTargetGroup.right = new FormAttachment( 100, -margin );
    wTargetGroup.setLayoutData( fdTargetGroup );
    // ///////////////////////////////////////////////////////////
    // / END OF Target GROUP
    // ///////////////////////////////////////////////////////////

    // add button ...
    wbaEntry = new Button( wGeneralComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbaEntry );
    wbaEntry.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.FilenameAdd.Button" ) );
    wbaEntry.setToolTipText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.FilenameAdd.Button.Tooltip" ) );
    fdbaEntry = new FormData();
    fdbaEntry.left = new FormAttachment( 0, 0 );
    fdbaEntry.right = new FormAttachment( 100, -margin );
    fdbaEntry.top = new FormAttachment( wTargetGroup, margin );
    wbaEntry.setLayoutData( fdbaEntry );

    // Buttons to the right of the screen...
    wbDelete = new Button( wGeneralComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDelete );
    wbDelete.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.FilenameDelete.Button" ) );
    wbDelete.setToolTipText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.FilenameDelete.Tooltip" ) );
    fdbdDelete = new FormData();
    fdbdDelete.right = new FormAttachment( 100, 0 );
    fdbdDelete.top = new FormAttachment( wbaEntry, 10 * margin );
    wbDelete.setLayoutData( fdbdDelete );

    wbEdit = new Button( wGeneralComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbEdit );
    wbEdit.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.FilenameEdit.Button" ) );
    fdbeSourceFileFolder = new FormData();
    fdbeSourceFileFolder.right = new FormAttachment( 100, 0 );
    fdbeSourceFileFolder.left = new FormAttachment( wbDelete, 0, SWT.LEFT );
    fdbeSourceFileFolder.top = new FormAttachment( wbDelete, margin );
    wbEdit.setLayoutData( fdbeSourceFileFolder );

    wlFields = new Label( wGeneralComp, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Fields.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.right = new FormAttachment( middle, -margin );
    fdlFields.top = new FormAttachment( wbaEntry, margin );
    wlFields.setLayoutData( fdlFields );

    int rows =
      jobEntry.source_filefolder == null ? 1 : ( jobEntry.source_filefolder.length == 0
        ? 0 : jobEntry.source_filefolder.length );
    final int FieldsRows = rows;

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Fields.SourceFileFolder.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Fields.Wildcard.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Fields.FieldsDelimiter.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Fields.TargetDb.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Fields.TargetTable.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ), };

    colinf[0].setUsingVariables( true );
    colinf[0]
      .setToolTip( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Fields.SourceFileFolder.Tooltip" ) );
    colinf[1].setUsingVariables( true );
    colinf[1].setToolTip( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Fields.Wildcard.Tooltip" ) );
    colinf[2].setUsingVariables( true );
    colinf[2].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.FieldsDelimiter.Tooltip" ) );
    colinf[3].setUsingVariables( true );
    colinf[3].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.TargetDb.Tooltip" ) );
    colinf[4].setUsingVariables( true );
    colinf[4].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.TargetTable.Tooltip" ) );
    wFields =
      new TableView(
        jobMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( wbDelete, -margin );
    fdFields.bottom = new FormAttachment( 100, -margin );
    wFields.setLayoutData( fdFields );

    fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( 0, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );
    props.setLook( wGeneralComp );

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF ADVANCED TAB ///
    // ////////////////////////

    wAdvancedTab = new CTabItem( wTabFolder, SWT.NONE );
    wAdvancedTab.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Tab.Advanced.Label" ) );

    wAdvancedComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAdvancedComp );

    FormLayout advancedLayout = new FormLayout();
    advancedLayout.marginWidth = 3;
    advancedLayout.marginHeight = 3;
    wAdvancedComp.setLayout( generalLayout );

    // SuccessOngrouping?
    // ////////////////////////
    // START OF SUCCESS ON GROUP///
    // /
    wSuccessOn = new Group( wAdvancedComp, SWT.SHADOW_NONE );
    props.setLook( wSuccessOn );
    wSuccessOn.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.SuccessOn.Group.Label" ) );

    FormLayout successongroupLayout = new FormLayout();
    successongroupLayout.marginWidth = 10;
    successongroupLayout.marginHeight = 10;

    wSuccessOn.setLayout( successongroupLayout );

    // Success Condition
    wlSuccessCondition = new Label( wSuccessOn, SWT.RIGHT );
    wlSuccessCondition.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.SuccessCondition.Label" ) );
    props.setLook( wlSuccessCondition );
    fdlSuccessCondition = new FormData();
    fdlSuccessCondition.left = new FormAttachment( 0, 0 );
    fdlSuccessCondition.right = new FormAttachment( middle, 0 );
    fdlSuccessCondition.top = new FormAttachment( 0, margin );
    wlSuccessCondition.setLayoutData( fdlSuccessCondition );
    wSuccessCondition = new CCombo( wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wSuccessCondition
      .add( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.SuccessWhenAllWorksFine.Label" ) );
    wSuccessCondition.add( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.SuccessWhenAtLeat.Label" ) );
    wSuccessCondition.add( BaseMessages
      .getString( PKG, "JobEntryMSAccessBulkLoad.SuccessWhenErrorsLessThan.Label" ) );

    wSuccessCondition.select( 0 ); // +1: starts at -1

    props.setLook( wSuccessCondition );
    fdSuccessCondition = new FormData();
    fdSuccessCondition.left = new FormAttachment( middle, 0 );
    fdSuccessCondition.top = new FormAttachment( 0, margin );
    fdSuccessCondition.right = new FormAttachment( 100, 0 );
    wSuccessCondition.setLayoutData( fdSuccessCondition );
    wSuccessCondition.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeSuccessCondition();

      }
    } );

    // Success when number of errors less than
    wlNrErrorsLessThan = new Label( wSuccessOn, SWT.RIGHT );
    wlNrErrorsLessThan.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.NrErrorsLessThan.Label" ) );
    props.setLook( wlNrErrorsLessThan );
    fdlNrErrorsLessThan = new FormData();
    fdlNrErrorsLessThan.left = new FormAttachment( 0, 0 );
    fdlNrErrorsLessThan.top = new FormAttachment( wSuccessCondition, margin );
    fdlNrErrorsLessThan.right = new FormAttachment( middle, -margin );
    wlNrErrorsLessThan.setLayoutData( fdlNrErrorsLessThan );

    wNrErrorsLessThan =
      new TextVar( jobMeta, wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(
        PKG, "JobEntryMSAccessBulkLoad.NrErrorsLessThan.Tooltip" ) );
    props.setLook( wNrErrorsLessThan );
    wNrErrorsLessThan.addModifyListener( lsMod );
    fdNrErrorsLessThan = new FormData();
    fdNrErrorsLessThan.left = new FormAttachment( middle, 0 );
    fdNrErrorsLessThan.top = new FormAttachment( wSuccessCondition, margin );
    fdNrErrorsLessThan.right = new FormAttachment( 100, -margin );
    wNrErrorsLessThan.setLayoutData( fdNrErrorsLessThan );

    fdSuccessOn = new FormData();
    fdSuccessOn.left = new FormAttachment( 0, margin );
    fdSuccessOn.top = new FormAttachment( 0, margin );
    fdSuccessOn.right = new FormAttachment( 100, -margin );
    wSuccessOn.setLayoutData( fdSuccessOn );
    // ///////////////////////////////////////////////////////////
    // / END OF Success ON GROUP
    // ///////////////////////////////////////////////////////////

    // fileresult grouping?
    // ////////////////////////
    // START OF LOGGING GROUP///
    // /
    wFileResult = new Group( wAdvancedComp, SWT.SHADOW_NONE );
    props.setLook( wFileResult );
    wFileResult.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.FileResult.Group.Label" ) );
    FormLayout fileresultgroupLayout = new FormLayout();
    fileresultgroupLayout.marginWidth = 10;
    fileresultgroupLayout.marginHeight = 10;
    wFileResult.setLayout( fileresultgroupLayout );

    // Add file to result
    wlAddFileToResult = new Label( wFileResult, SWT.RIGHT );
    wlAddFileToResult.setText( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.AddFileToResult.Label" ) );
    props.setLook( wlAddFileToResult );
    fdlAddFileToResult = new FormData();
    fdlAddFileToResult.left = new FormAttachment( 0, 0 );
    fdlAddFileToResult.top = new FormAttachment( wSuccessOn, margin );
    fdlAddFileToResult.right = new FormAttachment( middle, -margin );
    wlAddFileToResult.setLayoutData( fdlAddFileToResult );
    wAddFileToResult = new Button( wFileResult, SWT.CHECK );
    props.setLook( wAddFileToResult );
    wAddFileToResult.setToolTipText( BaseMessages.getString(
      PKG, "JobEntryMSAccessBulkLoad.AddFileToResult.Tooltip" ) );
    fdAddFileToResult = new FormData();
    fdAddFileToResult.left = new FormAttachment( middle, 0 );
    fdAddFileToResult.top = new FormAttachment( wSuccessOn, margin );
    fdAddFileToResult.right = new FormAttachment( 100, 0 );
    wAddFileToResult.setLayoutData( fdAddFileToResult );
    wAddFileToResult.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    fdFileResult = new FormData();
    fdFileResult.left = new FormAttachment( 0, margin );
    fdFileResult.top = new FormAttachment( wSuccessOn, margin );
    fdFileResult.right = new FormAttachment( 100, -margin );
    wFileResult.setLayoutData( fdFileResult );
    // ///////////////////////////////////////////////////////////
    // / END OF FilesRsult GROUP
    // ///////////////////////////////////////////////////////////

    fdAdvancedComp = new FormData();
    fdAdvancedComp.left = new FormAttachment( 0, 0 );
    fdAdvancedComp.top = new FormAttachment( 0, 0 );
    fdAdvancedComp.right = new FormAttachment( 100, 0 );
    fdAdvancedComp.bottom = new FormAttachment( 100, 0 );
    wAdvancedComp.setLayoutData( fdGeneralComp );

    wAdvancedComp.layout();
    wAdvancedTab.setControl( wAdvancedComp );
    props.setLook( wAdvancedComp );

    // ///////////////////////////////////////////////////////////
    // / END OF ADVANCED TAB
    // ///////////////////////////////////////////////////////////

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wName, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    // Add the file to the list of files...
    SelectionAdapter selA = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        wFields.add( new String[] {
          wSourceFileFoldername.getText(), wWildcard.getText(), wDelimiter.getText(), wTargetDbname.getText(),
          wTablename.getText() } );
        wSourceFileFoldername.setText( "" );
        wWildcard.setText( "" );
        wDelimiter.setText( "" );
        wTargetDbname.setText( "" );
        wTablename.setText( "" );
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth( true );
      }
    };
    wbaEntry.addSelectionListener( selA );
    wSourceFileFoldername.addSelectionListener( selA );

    // Delete files from the list of files...
    wbDelete.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wFields.getSelectionIndices();
        wFields.remove( idx );
        wFields.removeEmptyRows();
        wFields.setRowNums();
      }
    } );

    // Edit the selected file & remove from the list...
    wbEdit.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int idx = wFields.getSelectionIndex();
        if ( idx >= 0 ) {
          String[] string = wFields.getItem( idx );
          wSourceFileFoldername.setText( string[0] );
          wWildcard.setText( string[1] );
          wDelimiter.setText( string[2] );
          wTargetDbname.setText( string[3] );
          wTablename.setText( string[4] );
          wFields.remove( idx );
        }
        wFields.removeEmptyRows();
        wFields.setRowNums();
      }
    } );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wName.addSelectionListener( lsDef );
    wSourceFileFoldername.addSelectionListener( lsDef );
    wTargetDbname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    activeSuccessCondition();
    RefreshArgFromPrevious();
    wTabFolder.setSelection( 0 );
    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  private void RefreshArgFromPrevious() {
    wlFields.setEnabled( !wPrevious.getSelection() );
    wFields.setEnabled( !wPrevious.getSelection() );
    wbSourceFolder.setEnabled( !wPrevious.getSelection() );
    wSourceFileFoldername.setEnabled( !wPrevious.getSelection() );
    wlTargetDbname.setEnabled( !wPrevious.getSelection() );
    wTargetDbname.setEnabled( !wPrevious.getSelection() );
    wTablename.setEnabled( !wPrevious.getSelection() );
    wlTablename.setEnabled( !wPrevious.getSelection() );
    wbTargetDbname.setEnabled( !wPrevious.getSelection() );
    wbEdit.setEnabled( !wPrevious.getSelection() );
    wincludeSubFolders.setEnabled( !wPrevious.getSelection() );
    wlincludeSubFolders.setEnabled( !wPrevious.getSelection() );
    wbDelete.setEnabled( !wPrevious.getSelection() );
    wbaEntry.setEnabled( !wPrevious.getSelection() );
    wbFileFoldername.setEnabled( !wPrevious.getSelection() );
    wlWildcard.setEnabled( !wPrevious.getSelection() );
    wWildcard.setEnabled( !wPrevious.getSelection() );
    wlDelimiter.setEnabled( !wPrevious.getSelection() );
    wDelimiter.setEnabled( !wPrevious.getSelection() );
    WSourceFileFoldername.setEnabled( !wPrevious.getSelection() );
  }

  public void dispose() {
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  private void activeSuccessCondition() {
    wlNrErrorsLessThan.setEnabled( wSuccessCondition.getSelectionIndex() != 0 );
    wNrErrorsLessThan.setEnabled( wSuccessCondition.getSelectionIndex() != 0 );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wName.setText( Const.NVL( jobEntry.getName(), "" ) );
    wAddFileToResult.setSelection( jobEntry.isAddResultFilename() );
    wincludeSubFolders.setSelection( jobEntry.isIncludeSubFoders() );
    wPrevious.setSelection( jobEntry.isArgsFromPrevious() );
    if ( jobEntry.source_filefolder != null ) {
      for ( int i = 0; i < jobEntry.source_filefolder.length; i++ ) {
        TableItem ti = wFields.table.getItem( i );
        if ( jobEntry.source_filefolder[i] != null ) {
          ti.setText( 1, jobEntry.source_filefolder[i] );
        }
        if ( jobEntry.source_wildcard[i] != null ) {
          ti.setText( 2, jobEntry.source_wildcard[i] );
        }
        if ( jobEntry.delimiter[i] != null ) {
          ti.setText( 3, jobEntry.delimiter[i] );
        }
        if ( jobEntry.target_Db[i] != null ) {
          ti.setText( 4, jobEntry.target_Db[i] );
        }
        if ( jobEntry.target_table[i] != null ) {
          ti.setText( 5, jobEntry.target_table[i] );
        }
      }
      wFields.setRowNums();
      wFields.optWidth( true );
    }
    if ( jobEntry.getLimit() != null ) {
      wNrErrorsLessThan.setText( jobEntry.getLimit() );
    } else {
      wNrErrorsLessThan.setText( "10" );
    }

    if ( jobEntry.getSuccessCondition() != null ) {
      if ( jobEntry.getSuccessCondition().equals( jobEntry.SUCCESS_IF_AT_LEAST ) ) {
        wSuccessCondition.select( 1 );
      } else if ( jobEntry.getSuccessCondition().equals( jobEntry.SUCCESS_IF_ERRORS_LESS ) ) {
        wSuccessCondition.select( 2 );
      } else {
        wSuccessCondition.select( 0 );
      }
    } else {
      wSuccessCondition.select( 0 );
    }

    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    jobEntry.setChanged( changed );
    jobEntry = null;
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wName.getText() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
      mb.open();
      return;
    }
    jobEntry.setName( wName.getText() );
    jobEntry.setAddResultFilenames( wAddFileToResult.getSelection() );
    jobEntry.setIncludeSubFoders( wincludeSubFolders.getSelection() );
    jobEntry.setArgsFromPrevious( wPrevious.getSelection() );

    int nritems = wFields.nrNonEmpty();
    int nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String arg = wFields.getNonEmpty( i ).getText( 1 );
      if ( arg != null && arg.length() != 0 ) {
        nr++;
      }
    }
    jobEntry.source_filefolder = new String[nr];
    jobEntry.source_wildcard = new String[nr];
    jobEntry.delimiter = new String[nr];
    jobEntry.target_Db = new String[nr];
    jobEntry.target_table = new String[nr];

    nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String source = wFields.getNonEmpty( i ).getText( 1 );
      String wild = wFields.getNonEmpty( i ).getText( 2 );
      String delimiter = wFields.getNonEmpty( i ).getText( 3 );
      String destdb = wFields.getNonEmpty( i ).getText( 4 );
      String desttable = wFields.getNonEmpty( i ).getText( 5 );
      if ( source != null && source.length() != 0 ) {
        jobEntry.source_filefolder[nr] = source;
        jobEntry.source_wildcard[nr] = wild;
        jobEntry.delimiter[nr] = delimiter;
        jobEntry.target_Db[nr] = destdb;
        jobEntry.target_table[nr] = desttable;
        nr++;
      }
    }
    jobEntry.setLimit( wNrErrorsLessThan.getText() );

    if ( wSuccessCondition.getSelectionIndex() == 1 ) {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_AT_LEAST );
    } else if ( wSuccessCondition.getSelectionIndex() == 2 ) {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_ERRORS_LESS );
    } else {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_NO_ERRORS );
    }

    dispose();
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }
}
