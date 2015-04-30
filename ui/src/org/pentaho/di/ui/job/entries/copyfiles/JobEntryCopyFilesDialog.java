/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entries.copyfiles;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.copyfiles.JobEntryCopyFiles;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * This dialog allows you to edit the Copy Files job entry settings.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */

public class JobEntryCopyFilesDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryCopyFiles.class; // for i18n purposes, needed by Translator2!!

  protected static final String[] FILETYPES = new String[] { BaseMessages.getString(
    PKG, "JobCopyFiles.Filetype.All" ) };

  private Label wlName;
  protected Text wName;
  private FormData fdlName, fdName;

  private Label wlCopyEmptyFolders;
  protected Button wCopyEmptyFolders;
  private FormData fdlCopyEmptyFolders, fdCopyEmptyFolders;

  private Label wlOverwriteFiles;
  protected Button wOverwriteFiles;
  private FormData fdlOverwriteFiles, fdOverwriteFiles;

  private Label wlIncludeSubfolders;
  protected Button wIncludeSubfolders;
  private FormData fdlIncludeSubfolders, fdIncludeSubfolders;

  private Label wlRemoveSourceFiles;
  protected Button wRemoveSourceFiles;
  private FormData fdlRemoveSourceFiles, fdRemoveSourceFiles;

  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;

  protected JobEntryCopyFiles jobEntry;
  protected Shell shell;

  private SelectionAdapter lsDef;

  protected boolean changed;

  private Label wlPrevious;

  protected Button wPrevious;

  private FormData fdlPrevious, fdPrevious;

  private Label wlFields;

  protected TableView wFields;

  private FormData fdlFields, fdFields;

  private ToolItem deleteToolItem; // Delete

  private CTabFolder wTabFolder;
  // settings tab
  private Composite wSettingsComp;
  private CTabItem wSettingsTab;
  private FormData fdSettingsComp;
  private FormData fdTabFolder;
  // files tab
  private Composite wFilesComp;
  private CTabItem wFilesTab;
  private FormData fdFilesComp;
  
  // Add File to result
  private Label wlAddFileToResult;
  protected Button wAddFileToResult;
  private FormData fdlAddFileToResult, fdAddFileToResult;

  private Label wlCreateDestinationFolder;
  protected Button wCreateDestinationFolder;
  private FormData fdlCreateDestinationFolder, fdCreateDestinationFolder;

  private Label wlDestinationIsAFile;
  protected Button wDestinationIsAFile;
  private FormData fdlDestinationIsAFile, fdDestinationIsAFile;

  public JobEntryCopyFilesDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryCopyFiles) jobEntryInt;

    if ( this.jobEntry.getName() == null ) {
      this.jobEntry.setName( BaseMessages.getString( PKG, "JobCopyFiles.Name.Default" ) );
    }
  }

  protected void initUI() {
    Shell parent = getParent();

    shell = new Shell( parent, props.getJobsDialogStyle() );
    props.setLook( shell );
    Button helpButton = JobDialog.setShellImage( shell, jobEntry );

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
    shell.setText( BaseMessages.getString( PKG, "JobCopyFiles.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Filename line
    wlName = new Label( shell, SWT.LEFT );
    wlName.setText( BaseMessages.getString( PKG, "JobCopyFiles.Name.Label" ) );
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
    fdName.left = new FormAttachment( 0, 0 );
    fdName.top = new FormAttachment( wlName, margin );
    fdName.right = new FormAttachment( 40, 0 );
    wName.setLayoutData( fdName );
    Label wlIcon = new Label( shell, SWT.RIGHT );
    wlIcon.setImage( getImage() );
    props.setLook( wlIcon );
    FormData fdlIcon = new FormData();
    fdlIcon.top = new FormAttachment( 0, margin * 3 );
    fdlIcon.right = new FormAttachment( 100, -margin );
    wlIcon.setLayoutData( fdlIcon );
    
    Label lTopSeparator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdTopSeparator = new FormData();
    fdTopSeparator.top = new FormAttachment( wName, margin * 3 );
    fdTopSeparator.left = new FormAttachment( 0, 0 );
    fdTopSeparator.right = new FormAttachment( 100, 0 );
    lTopSeparator.setLayoutData( fdTopSeparator );    
    
    
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( lTopSeparator, margin * 3 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -60 );
    wTabFolder.setLayoutData( fdTabFolder );
    
    // ///////////////////////////////////////////////////////////
    // / START OF FILES TAB
    // ///////////////////////////////////////////////////////////
    
    wFilesTab = new CTabItem( wTabFolder, SWT.NONE );
//    wFilesTab.setText( BaseMessages.getString( PKG, "JobCopyFiles.Tab.Files.Label" ) );
    wFilesTab.setText( "Files" );

    wFilesComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFilesComp );
    
    FormLayout filesLayout = new FormLayout();
    filesLayout.marginWidth = 3;
    filesLayout.marginHeight = 3;
    wFilesComp.setLayout( filesLayout );
    
    fdFilesComp = new FormData();
    fdFilesComp.left = new FormAttachment( 0, 0 );
    fdFilesComp.top = new FormAttachment( 0, 0 );
    fdFilesComp.right = new FormAttachment( 100, 0 );
    fdFilesComp.bottom = new FormAttachment( 100, 0 );
    wFilesComp.setLayoutData( fdFilesComp );

    wFilesComp.layout();
    wFilesTab.setControl( wFilesComp );
    
    // ///////////////////////////////////////////////////////////
    // / END OF FILES TAB
    // ///////////////////////////////////////////////////////////    
    
    // ////////////////////////
    // START OF SETTINGS TAB ///
    // ////////////////////////

    wSettingsTab = new CTabItem( wTabFolder, SWT.NONE );
    wSettingsTab.setText( BaseMessages.getString( PKG, "JobCopyFiles.Settings.Label" ) );

    wSettingsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSettingsComp );

    FormLayout settingsLayout = new FormLayout();
    settingsLayout.marginWidth = 3;
    settingsLayout.marginHeight = 3;
    wSettingsComp.setLayout( settingsLayout );
    
    wlIncludeSubfolders = new Label( wSettingsComp, SWT.RIGHT );
    wlIncludeSubfolders.setText( BaseMessages.getString( PKG, "JobCopyFiles.IncludeSubfolders.Label" ) );
    props.setLook( wlIncludeSubfolders );
    fdlIncludeSubfolders = new FormData();
    fdlIncludeSubfolders.left = new FormAttachment( 0, 0 );
    fdlIncludeSubfolders.top = new FormAttachment( wName, margin );
    fdlIncludeSubfolders.right = new FormAttachment( middle, -margin );
    wlIncludeSubfolders.setLayoutData( fdlIncludeSubfolders );
    wIncludeSubfolders = new Button( wSettingsComp, SWT.CHECK );
    props.setLook( wIncludeSubfolders );
    wIncludeSubfolders.setToolTipText( BaseMessages.getString( PKG, "JobCopyFiles.IncludeSubfolders.Tooltip" ) );
    fdIncludeSubfolders = new FormData();
    fdIncludeSubfolders.left = new FormAttachment( middle, 0 );
    fdIncludeSubfolders.top = new FormAttachment( wName, margin );
    fdIncludeSubfolders.right = new FormAttachment( 100, 0 );
    wIncludeSubfolders.setLayoutData( fdIncludeSubfolders );
    wIncludeSubfolders.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
        CheckIncludeSubFolders();
      }
    } );

    // Destination is a file?
    wlDestinationIsAFile = new Label( wSettingsComp, SWT.RIGHT );
    wlDestinationIsAFile.setText( BaseMessages.getString( PKG, "JobCopyFiles.DestinationIsAFile.Label" ) );
    props.setLook( wlDestinationIsAFile );
    fdlDestinationIsAFile = new FormData();
    fdlDestinationIsAFile.left = new FormAttachment( 0, 0 );
    fdlDestinationIsAFile.top = new FormAttachment( wIncludeSubfolders, margin );
    fdlDestinationIsAFile.right = new FormAttachment( middle, -margin );
    wlDestinationIsAFile.setLayoutData( fdlDestinationIsAFile );
    wDestinationIsAFile = new Button( wSettingsComp, SWT.CHECK );
    props.setLook( wDestinationIsAFile );
    wDestinationIsAFile.setToolTipText( BaseMessages.getString( PKG, "JobCopyFiles.DestinationIsAFile.Tooltip" ) );
    fdDestinationIsAFile = new FormData();
    fdDestinationIsAFile.left = new FormAttachment( middle, 0 );
    fdDestinationIsAFile.top = new FormAttachment( wIncludeSubfolders, margin );
    fdDestinationIsAFile.right = new FormAttachment( 100, 0 );
    wDestinationIsAFile.setLayoutData( fdDestinationIsAFile );
    wDestinationIsAFile.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // Copy empty folders
    wlCopyEmptyFolders = new Label( wSettingsComp, SWT.RIGHT );
    wlCopyEmptyFolders.setText( BaseMessages.getString( PKG, "JobCopyFiles.CopyEmptyFolders.Label" ) );
    props.setLook( wlCopyEmptyFolders );
    fdlCopyEmptyFolders = new FormData();
    fdlCopyEmptyFolders.left = new FormAttachment( 0, 0 );
    fdlCopyEmptyFolders.top = new FormAttachment( wDestinationIsAFile, margin );
    fdlCopyEmptyFolders.right = new FormAttachment( middle, -margin );
    wlCopyEmptyFolders.setLayoutData( fdlCopyEmptyFolders );
    wCopyEmptyFolders = new Button( wSettingsComp, SWT.CHECK );
    props.setLook( wCopyEmptyFolders );
    wCopyEmptyFolders.setToolTipText( BaseMessages.getString( PKG, "JobCopyFiles.CopyEmptyFolders.Tooltip" ) );
    fdCopyEmptyFolders = new FormData();
    fdCopyEmptyFolders.left = new FormAttachment( middle, 0 );
    fdCopyEmptyFolders.top = new FormAttachment( wDestinationIsAFile, margin );
    fdCopyEmptyFolders.right = new FormAttachment( 100, 0 );
    wCopyEmptyFolders.setLayoutData( fdCopyEmptyFolders );
    wCopyEmptyFolders.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // Create destination folder/parent folder
    wlCreateDestinationFolder = new Label( wSettingsComp, SWT.RIGHT );
    wlCreateDestinationFolder
      .setText( BaseMessages.getString( PKG, "JobCopyFiles.CreateDestinationFolder.Label" ) );
    props.setLook( wlCreateDestinationFolder );
    fdlCreateDestinationFolder = new FormData();
    fdlCreateDestinationFolder.left = new FormAttachment( 0, 0 );
    fdlCreateDestinationFolder.top = new FormAttachment( wCopyEmptyFolders, margin );
    fdlCreateDestinationFolder.right = new FormAttachment( middle, -margin );
    wlCreateDestinationFolder.setLayoutData( fdlCreateDestinationFolder );
    wCreateDestinationFolder = new Button( wSettingsComp, SWT.CHECK );
    props.setLook( wCreateDestinationFolder );
    wCreateDestinationFolder.setToolTipText( BaseMessages.getString(
      PKG, "JobCopyFiles.CreateDestinationFolder.Tooltip" ) );
    fdCreateDestinationFolder = new FormData();
    fdCreateDestinationFolder.left = new FormAttachment( middle, 0 );
    fdCreateDestinationFolder.top = new FormAttachment( wCopyEmptyFolders, margin );
    fdCreateDestinationFolder.right = new FormAttachment( 100, 0 );
    wCreateDestinationFolder.setLayoutData( fdCreateDestinationFolder );
    wCreateDestinationFolder.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // OverwriteFiles Option
    wlOverwriteFiles = new Label( wSettingsComp, SWT.RIGHT );
    wlOverwriteFiles.setText( BaseMessages.getString( PKG, "JobCopyFiles.OverwriteFiles.Label" ) );
    props.setLook( wlOverwriteFiles );
    fdlOverwriteFiles = new FormData();
    fdlOverwriteFiles.left = new FormAttachment( 0, 0 );
    fdlOverwriteFiles.top = new FormAttachment( wCreateDestinationFolder, margin );
    fdlOverwriteFiles.right = new FormAttachment( middle, -margin );
    wlOverwriteFiles.setLayoutData( fdlOverwriteFiles );
    wOverwriteFiles = new Button( wSettingsComp, SWT.CHECK );
    props.setLook( wOverwriteFiles );
    wOverwriteFiles.setToolTipText( BaseMessages.getString( PKG, "JobCopyFiles.OverwriteFiles.Tooltip" ) );
    fdOverwriteFiles = new FormData();
    fdOverwriteFiles.left = new FormAttachment( middle, 0 );
    fdOverwriteFiles.top = new FormAttachment( wCreateDestinationFolder, margin );
    fdOverwriteFiles.right = new FormAttachment( 100, 0 );
    wOverwriteFiles.setLayoutData( fdOverwriteFiles );
    wOverwriteFiles.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // Remove source files option
    wlRemoveSourceFiles = new Label( wSettingsComp, SWT.RIGHT );
    wlRemoveSourceFiles.setText( BaseMessages.getString( PKG, "JobCopyFiles.RemoveSourceFiles.Label" ) );
    props.setLook( wlRemoveSourceFiles );
    fdlRemoveSourceFiles = new FormData();
    fdlRemoveSourceFiles.left = new FormAttachment( 0, 0 );
    fdlRemoveSourceFiles.top = new FormAttachment( wOverwriteFiles, margin );
    fdlRemoveSourceFiles.right = new FormAttachment( middle, -margin );
    wlRemoveSourceFiles.setLayoutData( fdlRemoveSourceFiles );
    wRemoveSourceFiles = new Button( wSettingsComp, SWT.CHECK );
    props.setLook( wRemoveSourceFiles );
    wRemoveSourceFiles.setToolTipText( BaseMessages.getString( PKG, "JobCopyFiles.RemoveSourceFiles.Tooltip" ) );
    fdRemoveSourceFiles = new FormData();
    fdRemoveSourceFiles.left = new FormAttachment( middle, 0 );
    fdRemoveSourceFiles.top = new FormAttachment( wOverwriteFiles, margin );
    fdRemoveSourceFiles.right = new FormAttachment( 100, 0 );
    wRemoveSourceFiles.setLayoutData( fdRemoveSourceFiles );
    wRemoveSourceFiles.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    wlPrevious = new Label( wSettingsComp, SWT.RIGHT );
    wlPrevious.setText( BaseMessages.getString( PKG, "JobCopyFiles.Previous.Label" ) );
    props.setLook( wlPrevious );
    fdlPrevious = new FormData();
    fdlPrevious.left = new FormAttachment( 0, 0 );
    fdlPrevious.top = new FormAttachment( wRemoveSourceFiles, margin );
    fdlPrevious.right = new FormAttachment( middle, -margin );
    wlPrevious.setLayoutData( fdlPrevious );
    wPrevious = new Button( wSettingsComp, SWT.CHECK );
    props.setLook( wPrevious );
    wPrevious.setSelection( jobEntry.arg_from_previous );
    wPrevious.setToolTipText( BaseMessages.getString( PKG, "JobCopyFiles.Previous.Tooltip" ) );
    fdPrevious = new FormData();
    fdPrevious.left = new FormAttachment( middle, 0 );
    fdPrevious.top = new FormAttachment( wRemoveSourceFiles, margin );
    fdPrevious.right = new FormAttachment( 100, 0 );
    wPrevious.setLayoutData( fdPrevious );
    wPrevious.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {

        refreshArgFromPrevious();

      }
    } );
    
    // Add file to result
    wlAddFileToResult = new Label( wSettingsComp, SWT.RIGHT );
    wlAddFileToResult.setText( BaseMessages.getString( PKG, "JobCopyFiles.AddFileToResult.Label" ) );
    props.setLook( wlAddFileToResult );
    fdlAddFileToResult = new FormData();
    fdlAddFileToResult.left = new FormAttachment( 0, 0 );
    fdlAddFileToResult.top = new FormAttachment( wPrevious, margin );
    fdlAddFileToResult.right = new FormAttachment( middle, -margin );
    wlAddFileToResult.setLayoutData( fdlAddFileToResult );
    wAddFileToResult = new Button( wSettingsComp, SWT.CHECK );
    props.setLook( wAddFileToResult );
    wAddFileToResult.setToolTipText( BaseMessages.getString( PKG, "JobCopyFiles.AddFileToResult.Tooltip" ) );
    fdAddFileToResult = new FormData();
    fdAddFileToResult.left = new FormAttachment( middle, 0 );
    fdAddFileToResult.top = new FormAttachment( wPrevious, margin );
    fdAddFileToResult.right = new FormAttachment( 100, 0 );
    wAddFileToResult.setLayoutData( fdAddFileToResult );
    wAddFileToResult.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );
    
    fdSettingsComp = new FormData();
    fdSettingsComp.left = new FormAttachment( 0, 0 );
    fdSettingsComp.top = new FormAttachment( 0, 0 );
    fdSettingsComp.right = new FormAttachment( 100, 0 );
    fdSettingsComp.bottom = new FormAttachment( 100, 0 );
    wSettingsComp.setLayoutData( fdSettingsComp );

    wSettingsComp.layout();
    wSettingsTab.setControl( wSettingsComp );
    props.setLook( wSettingsComp );

    // ///////////////////////////////////////////////////////////
    // / END OF SETTINGS TAB
    // ///////////////////////////////////////////////////////////    
    
    ToolBar tb = new ToolBar( wFilesComp, SWT.HORIZONTAL | SWT.FLAT );
    props.setLook( tb );
    FormData fdTb = new FormData();
    fdTb.right = new FormAttachment( 100, 0 );
    fdTb.top = new FormAttachment( wFilesComp, margin );
    tb.setLayoutData( fdTb );

    deleteToolItem = new ToolItem( tb, SWT.PUSH );
    deleteToolItem.setImage( GUIResource.getInstance().getImageDelete() );
    deleteToolItem.setToolTipText( BaseMessages.getString( PKG, "JobCopyFiles.FilenameDelete.Tooltip" ) );
    deleteToolItem.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wFields.getSelectionIndices();
        wFields.remove( idx );
        wFields.removeEmptyRows();
        wFields.setRowNums();
      }
    } );

    wlFields = new Label( wFilesComp, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "JobCopyFiles.Fields.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, margin );
    fdlFields.right = new FormAttachment( middle, -margin );
    fdlFields.top = new FormAttachment( wFilesComp, 15 );
    wlFields.setLayoutData( fdlFields );

    int rows =
      jobEntry.source_filefolder == null ? 1 : ( jobEntry.source_filefolder.length == 0
        ? 0 : jobEntry.source_filefolder.length );
    final int FieldsRows = rows;

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobCopyFiles.Fields.SourceFileFolder.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT_BUTTON, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobCopyFiles.Fields.DestinationFileFolder.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT_BUTTON, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobCopyFiles.Fields.Wildcard.Label" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };

    colinf[0].setUsingVariables( true );
    colinf[0].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.SourceFileFolder.Tooltip" ) );
    colinf[1].setUsingVariables( true );
    colinf[1].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.DestinationFileFolder.Tooltip" ) );
    colinf[2].setUsingVariables( true );
    colinf[2].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.Wildcard.Tooltip" ) );

    colinf[0].setTextVarButtonSelectionListener( getFileSelectionAdapter() );
    colinf[1].setTextVarButtonSelectionListener( getFileSelectionAdapter() );
    
    wFields =
      new TableView(
        jobMeta, wFilesComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, margin );
    fdFields.top = new FormAttachment( tb, margin );
    fdFields.right = new FormAttachment( 100, -margin );
    fdFields.bottom = new FormAttachment( 100, -margin );
    wFields.setLayoutData( fdFields );

    refreshArgFromPrevious();

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    Label lBottomSeparator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdBottomSeparator = new FormData();
    fdBottomSeparator.top = new FormAttachment( wTabFolder, margin * 3 );
    fdBottomSeparator.left = new FormAttachment( 0, 0 );
    fdBottomSeparator.right = new FormAttachment( 100, 0 );
    lBottomSeparator.setLayoutData( fdBottomSeparator );

    BaseStepDialog.positionBottomRightButtons( shell, new Button[] { wOK, wCancel }, margin, lBottomSeparator );
    FormData fdOK = (FormData) wOK.getLayoutData();
    FormData fdHelpButton = new FormData();
    fdHelpButton.top = fdOK.top;
    fdHelpButton.left = new FormAttachment( 0, margin );
    helpButton.setLayoutData( fdHelpButton );

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

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    CheckIncludeSubFolders();
    wTabFolder.setSelection( 0 );
    
  }
  
  public JobEntryInterface open() {
    initUI();
    BaseStepDialog.setSize( shell );
    shell.open();
    Display display = getParent().getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  protected SelectionAdapter getFileSelectionAdapter() {
    return new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        
        FileObject selectedFile = null;

        try {
          // Get current file
          FileObject rootFile = null;
          FileObject initialFile = null;
          FileObject defaultInitialFile = null;

          String original = wFields.getActiveTableItem().getText( wFields.getActiveTableColumn() );
          
          if ( original != null ) {

            String fileName = jobMeta.environmentSubstitute( original );

            if ( fileName != null && !fileName.equals( "" ) ) {
              try {
                initialFile = KettleVFS.getFileObject( fileName );
              } catch ( KettleException ex ) {
                initialFile = KettleVFS.getFileObject( "" );
              }
              defaultInitialFile = KettleVFS.getFileObject( "file:///c:/" );
              rootFile = initialFile.getFileSystem().getRoot();
            } else {
              defaultInitialFile = KettleVFS.getFileObject( Spoon.getInstance().getLastFileOpened() );
            }
          }

          if ( rootFile == null ) {
            rootFile = defaultInitialFile.getFileSystem().getRoot();
            initialFile = defaultInitialFile;
          }
          VfsFileChooserDialog fileChooserDialog = Spoon.getInstance().getVfsFileChooserDialog( rootFile, initialFile );
          fileChooserDialog.defaultInitialFile = defaultInitialFile;
          
          selectedFile =
              fileChooserDialog.open( shell, null, null, true, null, new String[] { "*.*" },
                  FILETYPES, VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY );
          
          if ( selectedFile != null ) {
            String url = selectedFile.getURL().toString();
            wFields.getActiveTableItem().setText( wFields.getActiveTableColumn(), url );
          }
          
        } catch ( KettleFileException ex ) {
        } catch ( FileSystemException ex ) {
        }        
      }
    };
  }
  
  private void refreshArgFromPrevious() {
    wlFields.setEnabled( !wPrevious.getSelection() );
    wFields.setEnabled( !wPrevious.getSelection() );
    deleteToolItem.setEnabled( !wPrevious.getSelection() );
  }

  public void dispose() {
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  private void CheckIncludeSubFolders() {
    wlCopyEmptyFolders.setEnabled( wIncludeSubfolders.getSelection() );
    wCopyEmptyFolders.setEnabled( wIncludeSubfolders.getSelection() );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( jobEntry.getName() != null ) {
      wName.setText( jobEntry.getName() );
    }
    wCopyEmptyFolders.setSelection( jobEntry.copy_empty_folders );

    if ( jobEntry.source_filefolder != null ) {
      for ( int i = 0; i < jobEntry.source_filefolder.length; i++ ) {
        TableItem ti = wFields.table.getItem( i );
        if ( jobEntry.source_filefolder[i] != null ) {
          ti.setText( 1, jobEntry.source_filefolder[i] );
        }
        if ( jobEntry.destination_filefolder[i] != null ) {
          ti.setText( 2, jobEntry.destination_filefolder[i] );
        }
        if ( jobEntry.wildcard[i] != null ) {
          ti.setText( 3, jobEntry.wildcard[i] );
        }
      }
      wFields.setRowNums();
      wFields.optWidth( true );
    }
    wPrevious.setSelection( jobEntry.arg_from_previous );
    wOverwriteFiles.setSelection( jobEntry.overwrite_files );
    wIncludeSubfolders.setSelection( jobEntry.include_subfolders );
    wRemoveSourceFiles.setSelection( jobEntry.remove_source_files );
    wDestinationIsAFile.setSelection( jobEntry.destination_is_a_file );
    wCreateDestinationFolder.setSelection( jobEntry.create_destination_folder );

    wAddFileToResult.setSelection( jobEntry.add_result_filesname );

    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    jobEntry.setChanged( changed );
    jobEntry = null;
    dispose();
  }

  protected void ok() {
    if ( Const.isEmpty( wName.getText() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
      mb.open();
      return;
    }

    jobEntry.setName( wName.getText() );
    jobEntry.setCopyEmptyFolders( wCopyEmptyFolders.getSelection() );
    jobEntry.setoverwrite_files( wOverwriteFiles.getSelection() );
    jobEntry.setIncludeSubfolders( wIncludeSubfolders.getSelection() );
    jobEntry.setArgFromPrevious( wPrevious.getSelection() );
    jobEntry.setRemoveSourceFiles( wRemoveSourceFiles.getSelection() );
    jobEntry.setAddresultfilesname( wAddFileToResult.getSelection() );
    jobEntry.setDestinationIsAFile( wDestinationIsAFile.getSelection() );
    jobEntry.setCreateDestinationFolder( wCreateDestinationFolder.getSelection() );

    int nritems = wFields.nrNonEmpty();
    int nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String arg = wFields.getNonEmpty( i ).getText( 1 );
      if ( arg != null && arg.length() != 0 ) {
        nr++;
      }
    }
    jobEntry.source_filefolder = new String[nr];
    jobEntry.destination_filefolder = new String[nr];
    jobEntry.wildcard = new String[nr];
    nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String source = wFields.getNonEmpty( i ).getText( 1 );
      String dest = wFields.getNonEmpty( i ).getText( 2 );
      String wild = wFields.getNonEmpty( i ).getText( 3 );
      if ( source != null && source.length() != 0 ) {
        jobEntry.source_filefolder[nr] = source;
        jobEntry.destination_filefolder[nr] = dest;
        jobEntry.wildcard[nr] = wild;
        nr++;
      }
    }
    dispose();
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }
 
  protected Image getImage() {
    return GUIResource.getInstance().getImage( "ui/images/CPY.svg", ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );    
  }
  
  public boolean showFileButtons() {
    return true;
  }
  
}