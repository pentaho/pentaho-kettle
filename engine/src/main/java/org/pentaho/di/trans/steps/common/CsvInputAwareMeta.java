/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.common;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.trans.TransMeta;

/**
 * A common interface for all metas aware of the csv input format, such as
 * {@link org.pentaho.di.trans.steps.csvinput.CsvInputMeta}
 * and {@link org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta}
 */
public interface CsvInputAwareMeta {

  String getDelimiter();

  String getEncoding();

  String getEnclosure();

  String getEscapeCharacter();

  int getFileFormatTypeNr();

  boolean hasHeader();

  /**
   * Returns a {@link FileObject} that corresponds to the first encountered input file. This object is used to read the
   * file headers for the purpose of field parsing.
   *
   * @param transMeta the {@link TransMeta}
   * @return null if the {@link FileObject} cannot be created.
   */
  FileObject getHeaderFileObject( final TransMeta transMeta );

}
