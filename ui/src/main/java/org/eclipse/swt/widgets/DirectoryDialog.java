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
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class DirectoryDialog extends FileDialog {

  public DirectoryDialog( Shell parent ) {
    super( parent );
    filterNames = new String[] { "All folders" };
    /**
     * To get only folders, we have to have a filter that filters out all files.
     * Special characters, e.g., "@", are not allowed for filename; hence, are good for such a filter.
     */
    filterExtensions = new String[] { "@" };
    fileDialogMode = VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY;
  }

  public DirectoryDialog( Shell parent, int style ) {
    this( parent );
    this.style = style;
  }

  @Override
  public String open() {
    String directoryPath = null;
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
      directoryPath = filterPath = file.getPath();
    }
    return directoryPath;
  }
}
