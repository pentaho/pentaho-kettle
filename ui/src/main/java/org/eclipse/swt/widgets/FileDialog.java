/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.eclipse.swt.widgets;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.swt.SWT;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class FileDialog implements IFileDialog {
  private static Class<?> PKG = Spoon.class;

  int style;
  Shell parent;
  String title;
  String message = "";
  String filterPath = "";
  String [] filterNames = new String[] { "All files" };
  String [] filterExtensions = new String[] { "*" };
  String [] fileNames = new String[1];
  String fileName = null;
  int fileDialogMode = VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE;

  protected VfsFileChooserDialog vfsFileChooserDialog;

  public FileDialog( Shell parent ) {
    this.parent = parent;
    FileObject initialFile;
    FileObject rootFile;
    try {
      initialFile = KettleVFS.getFileObject( Spoon.getInstance().getLastFileOpened() );
      rootFile = initialFile.getFileSystem().getRoot();
    } catch ( Exception e ) {
      String message = Const.getStackTracker( e );
      new ErrorDialog( parent, BaseMessages.getString( PKG, "Spoon.Error" ), message, e );

      return;
    }
    vfsFileChooserDialog = Spoon.getInstance().getVfsFileChooserDialog( rootFile, initialFile );
  }

  public FileDialog( Shell parent, int style ) {
    this( parent );
    this.style = style;
  }

  public String open() {
    String filePath = null;
    
    if ( ( style & SWT.SAVE ) > 0 ) {
      fileDialogMode = VfsFileChooserDialog.VFS_DIALOG_SAVEAS;
    }
    
    FileObject returnFile =
        vfsFileChooserDialog.open( parent, fileName, filterExtensions, filterNames, fileDialogMode );
    File file = null;
    if ( returnFile != null ) {
      try {
        file = new File( returnFile.getURL().getPath() );
        Spoon.getInstance().setLastFileOpened( file.getPath() );
      } catch ( FileSystemException e ) {
        e.printStackTrace();
      }
      filePath = file.getAbsolutePath();
      fileName = fileNames[0] = file.getName();
      filterPath = file.getParent();
    }
    return filePath;
  }

  public String getFilterPath() {
    return filterPath;
  }

  public void setFilterPath( String string ) {
    filterPath = string;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String string ) {
    if ( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    message = string;
  }

  public String getText() {
    return title;
  }

  public void setText( String string ) {
    if ( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    title = string;
  }

  public Shell getParent() {
    return parent;
  }

  public int getStyle() {
    return style;
  }

  public String[] getFilterExtensions() {
    return filterExtensions;
  }

  public void setFilterExtensions( String[] exts ) {
    filterExtensions = exts;
  }

  public String[] getFilterNames() {
    return filterNames;
  }

  public void setFilterNames( String[] strings ) {
    filterNames = strings;
  }

  public String getFileName() {
    return fileName;
  }

  public String[] getFileNames() {
    return fileNames;
  }

  /**
   * Set the initial filename
   */
  public void setFileName( String fname ) {
    fileName = fname;
  }
}
