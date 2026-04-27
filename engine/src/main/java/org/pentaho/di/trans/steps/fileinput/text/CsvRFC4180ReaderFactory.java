/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.fileinput.text;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.file.IBaseFileInputReader;
import org.pentaho.di.trans.steps.file.IBaseFileInputStepControl;

import java.util.List;

/**
 * Service interface for the RFC 4180 compliant CSV reader.
 * Implementations are discovered via {@link org.pentaho.di.core.service.PluginServiceLoader}.
 */
public interface CsvRFC4180ReaderFactory {

  /**
   * Creates a new reader instance for the given file.
   *
   * @param step the step control that owns the reader
   * @param meta the text file input metadata
   * @param data the text file input runtime data
   * @param file the file to read
   * @param log  the log channel
   * @return a reader that produces rows from the file
   * @throws Exception if the reader cannot be created
   */
  IBaseFileInputReader createReader( IBaseFileInputStepControl step, TextFileInputMeta meta,
                                     TextFileInputData data, FileObject file,
                                     LogChannelInterface log ) throws Exception;

  /**
   * Reads the first lines from a file, used by the dialog for preview / get-fields.
   *
   * @param meta        the text file input metadata
   * @param transMeta   the transformation metadata (for variable substitution and file resolution)
   * @param nrlines     the maximum number of data lines to return (0 = all)
   * @param skipHeaders whether to skip header lines
   * @return a list of raw line strings
   * @throws KettleException if the file cannot be read
   */
  List<String> getFirst( TextFileInputMeta meta, TransMeta transMeta, int nrlines,
                         boolean skipHeaders ) throws KettleException;

  /**
   * Discovers the field names from the first header line of the file using the FSM parser.
   *
   * @param transMeta the transformation metadata
   * @param meta      the text file input metadata
   * @return an array of field names
   * @throws KettleException if the file cannot be read
   */
  String[] getFieldNames( TransMeta transMeta, TextFileInputMeta meta ) throws KettleException;
}

