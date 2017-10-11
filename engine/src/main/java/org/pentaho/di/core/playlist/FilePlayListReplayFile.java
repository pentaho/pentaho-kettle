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

package org.pentaho.di.core.playlist;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;

public class FilePlayListReplayFile implements FilePlayList {
  private FileObject processingFile;
  private String processingFilePart;

  public FilePlayListReplayFile( FileObject processingFile, String processingFilePart ) {
    this.processingFile = processingFile;
    this.processingFilePart = processingFilePart;
  }

  FileObject getProcessingFile() {
    return processingFile;
  }

  String getProcessingFilePart() {
    return processingFilePart;
  }

  public boolean isProcessingNeeded( FileObject file, long lineNr, String filePart ) throws KettleException {
    return false;
  }
}
