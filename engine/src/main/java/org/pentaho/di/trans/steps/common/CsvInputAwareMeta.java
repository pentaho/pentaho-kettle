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
