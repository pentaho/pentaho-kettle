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

package org.pentaho.di.ui.job.entries.copyfiles;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
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
import org.eclipse.swt.widgets.Control;
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
import org.pentaho.di.core.util.Utils;
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
import org.pentaho.di.ui.core.widget.TextVarButtonRenderCallback;
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

  public static final String LOCAL_ENVIRONMENT = "Local";
  public static final String STATIC_ENVIRONMENT = "<Static>";

  protected Text wName;

  protected Button wPrevious;
  protected Button wCopyEmptyFolders;
  protected Button wOverwriteFiles;
  protected Button wIncludeSubfolders;
  protected Button wRemoveSourceFiles;
  protected Button wAddFileToResult;
  protected Button wDestinationIsAFile;
  protected Button wCreateDestinationFolder;

  protected JobEntryCopyFiles jobEntry;
  protected Shell shell;

  protected boolean changed;

  private Label wlFields;

  protected TableView wFields;

  private ToolItem deleteToolItem; // Delete

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
    Label wlName = new Label( shell, SWT.LEFT );
    wlName.setText( BaseMessages.getString( PKG, "JobCopyFiles.Name.Label" ) );
    props.setLook( wlName );
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.right = new FormAttachment( middle, -margin );
    fdlName.top = new FormAttachment( 0, margin );
    wlName.setLayoutData( fdlName );
    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    FormData fdName = new FormData();
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


    CTabFolder wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( lTopSeparator, margin * 3 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -60 );
    wTabFolder.setLayoutData( fdTabFolder );

    // ///////////////////////////////////////////////////////////
    // / START OF FILES TAB
    // ///////////////////////////////////////////////////////////

    CTabItem wFilesTab = new CTabItem( wTabFolder, SWT.NONE );
    wFilesTab.setText( BaseMessages.getString( PKG, "JobCopyFiles.Tab.Files.Label" ) );

    Composite wFilesComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFilesComp );

    FormLayout filesLayout = new FormLayout();
    filesLayout.marginWidth = 3;
    filesLayout.marginHeight = 3;
    wFilesComp.setLayout( filesLayout );

    FormData fdFilesComp = new FormData();
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

    CTabItem wSettingsTab = new CTabItem( wTabFolder, SWT.NONE );
    wSettingsTab.setText( BaseMessages.getString( PKG, "JobCopyFiles.Settings.Label" ) );

    Composite wSettingsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSettingsComp );

    FormLayout settingsLayout = new FormLayout();
    settingsLayout.marginWidth = 3;
    settingsLayout.marginHeight = 3;
    wSettingsComp.setLayout( settingsLayout );

    SelectionAdapter listener = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    };

    wIncludeSubfolders = createSettingsButton(
        wSettingsComp, "JobCopyFiles.IncludeSubfolders.Label", "JobCopyFiles.IncludeSubfolders.Tooltip", null, listener );

    wDestinationIsAFile = createSettingsButton(
        wSettingsComp, "JobCopyFiles.DestinationIsAFile.Label", "JobCopyFiles.DestinationIsAFile.Tooltip", wIncludeSubfolders, listener );

    wCopyEmptyFolders = createSettingsButton(
        wSettingsComp, "JobCopyFiles.CopyEmptyFolders.Label", "JobCopyFiles.CopyEmptyFolders.Tooltip", wDestinationIsAFile, listener );

    wCreateDestinationFolder = createSettingsButton(
        wSettingsComp, "JobCopyFiles.CreateDestinationFolder.Label", "JobCopyFiles.CreateDestinationFolder.Tooltip", wCopyEmptyFolders, listener );

    wOverwriteFiles = createSettingsButton(
        wSettingsComp, "JobCopyFiles.OverwriteFiles.Label", "JobCopyFiles.OverwriteFiles.Tooltip", wCreateDestinationFolder, listener );

    wRemoveSourceFiles = createSettingsButton(
        wSettingsComp, "JobCopyFiles.RemoveSourceFiles.Label", "JobCopyFiles.RemoveSourceFiles.Tooltip", wOverwriteFiles, listener );

    wPrevious = createSettingsButton(
        wSettingsComp, "JobCopyFiles.Previous.Label", "JobCopyFiles.Previous.Tooltip", wRemoveSourceFiles, listener );

    wAddFileToResult = createSettingsButton(
        wSettingsComp, "JobCopyFiles.AddFileToResult.Label", "JobCopyFiles.AddFileToResult.Tooltip", wPrevious, listener );

    FormData fdSettingsComp = new FormData();
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
    FormData fdlFields = new FormData();
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
          new ColumnInfo( BaseMessages.getString( PKG, "JobCopyFiles.Fields.SourceEnvironment.Label" ),
              ColumnInfo.COLUMN_TYPE_CCOMBO, false, true ),
          new ColumnInfo( BaseMessages.getString( PKG, "JobCopyFiles.Fields.SourceFileFolder.Label" ),
              ColumnInfo.COLUMN_TYPE_TEXT_BUTTON, false ),
          new ColumnInfo( BaseMessages.getString( PKG, "JobCopyFiles.Fields.Wildcard.Label" ),
              ColumnInfo.COLUMN_TYPE_TEXT, false ),
          new ColumnInfo( BaseMessages.getString( PKG, "JobCopyFiles.Fields.DestinationEnvironment.Label" ),
              ColumnInfo.COLUMN_TYPE_CCOMBO, false, true ),
          new ColumnInfo( BaseMessages.getString( PKG, "JobCopyFiles.Fields.DestinationFileFolder.Label" ),
              ColumnInfo.COLUMN_TYPE_TEXT_BUTTON, false ) };

    setComboValues( colinf[0] );

    TextVarButtonRenderCallback callback = new TextVarButtonRenderCallback() {
      public boolean shouldRenderButton() {
        String envType = wFields.getActiveTableItem().getText( wFields.getActiveTableColumn() - 1 );
        return !STATIC_ENVIRONMENT.equalsIgnoreCase( envType );
      }
    };

    colinf[1].setUsingVariables( true );
    colinf[1].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.SourceFileFolder.Tooltip" ) );
    colinf[1].setTextVarButtonSelectionListener( getFileSelectionAdapter() );
    colinf[1].setRenderTextVarButtonCallback( callback );

    colinf[2].setUsingVariables( true );
    colinf[2].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.Wildcard.Tooltip" ) );

    setComboValues( colinf[3] );

    colinf[4].setUsingVariables( true );
    colinf[4].setToolTip( BaseMessages.getString( PKG, "JobCopyFiles.Fields.DestinationFileFolder.Tooltip" ) );
    colinf[4].setTextVarButtonSelectionListener( getFileSelectionAdapter() );

    wFields =
      new TableView(
        jobMeta, wFilesComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, margin );
    fdFields.top = new FormAttachment( tb, margin );
    fdFields.right = new FormAttachment( 100, -margin );
    fdFields.bottom = new FormAttachment( 100, -margin );
    wFields.setLayoutData( fdFields );

    refreshArgFromPrevious();

    Button wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    Button wCancel = new Button( shell, SWT.PUSH );
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
    Listener lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    Listener lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    SelectionAdapter lsDef = new SelectionAdapter() {
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

  protected Button createSettingsButton( Composite p, String text, String title, Control top, SelectionAdapter sa ) {
    Button button = new Button( p, SWT.CHECK );
    button.setText( BaseMessages.getString( PKG, text ) );
    button.setToolTipText( BaseMessages.getString( PKG, title ) );
    props.setLook( button );
    FormData fd = new FormData();
    fd.left = new FormAttachment( 0, Const.MARGIN * 2 );
    if ( top == null ) {
      fd.top = new FormAttachment( 0, 10 );
    } else {
      fd.top = new FormAttachment( top, 5 );
    }
    fd.right = new FormAttachment( 100, 0 );
    button.setLayoutData( fd );
    button.addSelectionListener( sa );
    return button;
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
              fileChooserDialog.open( shell, new String[] { "file" }, "file", true, null, new String[] { "*.*" },
                  FILETYPES, true, VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE_OR_DIRECTORY, false, false );

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
          String sourceUrl = jobEntry.source_filefolder[i];
          String clusterName = jobEntry.getConfigurationBy( sourceUrl );
          if ( clusterName != null ) {
            clusterName =
                clusterName.startsWith( JobEntryCopyFiles.LOCAL_SOURCE_FILE ) ? LOCAL_ENVIRONMENT : clusterName;
            clusterName =
                clusterName.startsWith( JobEntryCopyFiles.STATIC_SOURCE_FILE ) ? STATIC_ENVIRONMENT : clusterName;

            ti.setText( 1, clusterName );
            sourceUrl =
                clusterName.equals( LOCAL_ENVIRONMENT ) || clusterName.equals( STATIC_ENVIRONMENT ) ? sourceUrl
                    : jobEntry.getUrlPath( sourceUrl );
          }
          if ( sourceUrl != null ) {
            sourceUrl = sourceUrl.replace( JobEntryCopyFiles.SOURCE_URL + i + "-", "" );
          } else {
            sourceUrl = "";
          }
          ti.setText( 2, sourceUrl );
        }
        if ( jobEntry.wildcard[i] != null ) {
          ti.setText( 3, jobEntry.wildcard[i] );
        }
        if ( jobEntry.destination_filefolder[i] != null ) {
          String destinationURL = jobEntry.destination_filefolder[i];
          String clusterName = jobEntry.getConfigurationBy( destinationURL );
          if ( clusterName != null ) {
            clusterName = clusterName.startsWith( JobEntryCopyFiles.LOCAL_DEST_FILE ) ? LOCAL_ENVIRONMENT : clusterName;
            clusterName =
                clusterName.startsWith( JobEntryCopyFiles.STATIC_DEST_FILE ) ? STATIC_ENVIRONMENT : clusterName;
            ti.setText( 4, clusterName );
            destinationURL =
                clusterName.equals( LOCAL_ENVIRONMENT ) || clusterName.equals( STATIC_ENVIRONMENT ) ? destinationURL
                    : jobEntry.getUrlPath( destinationURL );
          }
          if ( destinationURL != null ) {
            destinationURL = destinationURL.replace( JobEntryCopyFiles.DEST_URL + i + "-", "" );
          } else {
            destinationURL = "";
          }
          ti.setText( 5, destinationURL );
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
    if ( Utils.isEmpty( wName.getText() ) ) {
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


    Map<String, String> sourceDestinationMappings = new HashMap<String, String>();
    jobEntry.source_filefolder = new String[nritems];
    jobEntry.destination_filefolder = new String[nritems];
    jobEntry.wildcard = new String[nritems];

    for ( int i = 0; i < nritems; i++ ) {
      String sourceNc = wFields.getNonEmpty( i ).getText( 1 );
      sourceNc = sourceNc.equals( LOCAL_ENVIRONMENT ) ? JobEntryCopyFiles.LOCAL_SOURCE_FILE + i : sourceNc;
      sourceNc = sourceNc.equals( STATIC_ENVIRONMENT ) ? JobEntryCopyFiles.STATIC_SOURCE_FILE + i : sourceNc;
      String source = wFields.getNonEmpty( i ).getText( 2 );
      String wild = wFields.getNonEmpty( i ).getText( 3 );
      String destNc = wFields.getNonEmpty( i ).getText( 4 );
      destNc = destNc.equals( LOCAL_ENVIRONMENT ) ? JobEntryCopyFiles.LOCAL_DEST_FILE + i : destNc;
      destNc = destNc.equals( STATIC_ENVIRONMENT ) ? JobEntryCopyFiles.STATIC_DEST_FILE + i : destNc;
      String dest = wFields.getNonEmpty( i ).getText( 5 );
      source = JobEntryCopyFiles.SOURCE_URL + i + "-" + source;
      dest = JobEntryCopyFiles.DEST_URL + i + "-" + dest;
      jobEntry.source_filefolder[i] = jobEntry.loadURL( source, sourceNc, getMetaStore(), sourceDestinationMappings );
      jobEntry.destination_filefolder[i] = jobEntry.loadURL( dest, destNc, getMetaStore(), sourceDestinationMappings );
      jobEntry.wildcard[i] = wild;
    }
    jobEntry.setConfigurationMappings( sourceDestinationMappings );

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

  protected void setComboValues( ColumnInfo colInfo ) {
    String[] values = { LOCAL_ENVIRONMENT, STATIC_ENVIRONMENT };
    colInfo.setComboValues( values );
  }
}
