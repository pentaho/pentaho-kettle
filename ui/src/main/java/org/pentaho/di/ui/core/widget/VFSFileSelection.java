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
package org.pentaho.di.ui.core.widget;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.dialog.SelectObjectDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class VFSFileSelection extends Composite {
  private static final Class<?> PKG = VFSFileSelection.class;
  public final TextVar wFileName;
  public final Button wBrowse;
  private final String[] fileFilters;
  private final String[] fileFilterNames;
  private final TransMeta transMeta;
  private final Repository repository;
  private final Supplier<Optional<String>> fileNameSupplier;

  public VFSFileSelection( Composite composite, int i, String[] fileFilters, String[] fileFilterNames, TransMeta transMeta ) {
    this( composite, i, fileFilters, fileFilterNames, transMeta, null );
  }

  public VFSFileSelection( Composite composite, int i, String[] fileFilters, String[] fileFilterNames, TransMeta transMeta, Repository repository ) {
    super( composite, i );
    this.fileFilters = fileFilters;
    this.fileFilterNames = fileFilterNames;
    this.transMeta = transMeta;
    this.repository = repository;
    fileNameSupplier = repository == null ? this::promptForLocalFile : this::promptForRepositoryFile;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;
    this.setLayout( formLayout );

    wFileName = new TextVar( transMeta, this, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    FormData fdFileName = new FormData();
    fdFileName.left = new FormAttachment( 0, 0 );
    fdFileName.top = new FormAttachment( 0, 0 );
    fdFileName.width = 275;
    wFileName.setLayoutData( fdFileName );

    wBrowse = new Button( this, SWT.PUSH );
    wBrowse.setText( BaseMessages.getString( PKG, "VFSFileSelection.Dialog.Browse" ) );
    FormData fdBrowse = new FormData();
    fdBrowse.left = new FormAttachment( wFileName, 5 );
    fdBrowse.top = new FormAttachment( wFileName, 0, SWT.TOP );
    wBrowse.setLayoutData( fdBrowse );

    wBrowse.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        fileNameSupplier.get().ifPresent( wFileName::setText );
      }
    } );
  }

  private Optional<String> promptForLocalFile() {
    String curFile = transMeta.environmentSubstitute( wFileName.getText() );

    FileObject root;

    try {
      root = KettleVFS.getFileObject( curFile != null ? curFile : Const.getUserHomeDirectory() );

      VfsFileChooserDialog vfsFileChooser = Spoon.getInstance().getVfsFileChooserDialog( root.getParent(), root );
      FileObject file =
        vfsFileChooser.open( getShell(), null, fileFilters, fileFilterNames, VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE );
      if ( file == null ) {
        return Optional.empty();
      }

      String filePath = getRelativePath( file.getName().toString() );
      return Optional.ofNullable( filePath );
    } catch ( IOException | KettleException e ) {
      new ErrorDialog( getShell(),
        BaseMessages.getString( PKG, "VFSFileSelection.ErrorLoadingFile.DialogTitle" ),
        BaseMessages.getString( PKG, "VFSFileSelection.ErrorLoadingFile.DialogMessage" ), e );
    }
    return Optional.empty();
  }

  private String getRelativePath( String filePath ) {
    String parentFolder = null;
    try {
      parentFolder =
        KettleVFS.getFileObject( transMeta.environmentSubstitute( transMeta.getFilename() ) ).getParent().toString();
    } catch ( Exception e ) {
      // Take no action
    }

    if ( filePath != null ) {
      if ( parentFolder != null && filePath.startsWith( parentFolder ) ) {
        filePath = filePath.replace( parentFolder, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
      }
    }
    return filePath;
  }

  private Optional<String> promptForRepositoryFile() {
    SelectObjectDialog sod = new SelectObjectDialog( getShell(), repository );
    String fileName = sod.open();
    RepositoryDirectoryInterface repdir = sod.getDirectory();
    if ( fileName != null && repdir != null ) {
      String path = getRepositoryRelativePath( repdir + RepositoryDirectory.DIRECTORY_SEPARATOR + fileName );
      return Optional.ofNullable( path );
    }
    return Optional.empty();
  }

  private String getRepositoryRelativePath( String path ) {
    String parentPath = this.transMeta.getRepositoryDirectory().getPath();
    if ( path.startsWith( parentPath ) ) {
      path = path.replace( parentPath, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
    }
    return path;
  }
}
