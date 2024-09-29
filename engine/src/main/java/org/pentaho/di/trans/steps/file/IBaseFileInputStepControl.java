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


package org.pentaho.di.trans.steps.file;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;

/**
 * Interface for some step operations required for parse input file.
 */
public interface IBaseFileInputStepControl {
  long incrementLinesInput();

  long getLinesWritten();

  void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException;

  long getLinesInput();

  boolean checkFeedback( long lines );

  long incrementLinesUpdated();

  boolean failAfterBadFile( String errorMsg );

  void stopAll();

  long getErrors();

  void setErrors( long e );
}
