/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.playlist;

import org.apache.commons.vfs2.FileObject;

public class FilePlayListAll implements FilePlayList {

  public static final FilePlayList INSTANCE = new FilePlayListAll();

  public boolean isProcessingNeeded( FileObject file, long lineNr, String filePart ) {
    return true;
  }

}
