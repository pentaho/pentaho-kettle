package org.pentaho.di.trans.steps.baseinput;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;

/**
 * Interface for some step operations required for parse input file.
 */
public interface IBaseInputStepControl {
    long incrementLinesInput();

  long getLinesWritten();

  void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException;

  long getLinesInput();

  boolean checkFeedback( long lines );

  long incrementLinesUpdated();

  boolean failAfterBadFile( String errorMsg );
  void stopAll() ;
  long getErrors();
  void setErrors( long e );
}
