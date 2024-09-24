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

package org.pentaho.di.trans.steps.filesfromresult;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads results from a previous transformation in a Job
 *
 * @author Matt
 * @since 2-jun-2003
 */
public class FilesFromResult extends BaseStep implements StepInterface {
  private static Class<?> PKG = FilesFromResult.class; // for i18n purposes, needed by Translator2!!

  private FilesFromResultData data;

  public FilesFromResult( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    data = (FilesFromResultData) stepDataInterface;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( data.resultFilesList == null || getLinesRead() >= data.resultFilesList.size() ) {
      setOutputDone();
      return false;
    }

    ResultFile resultFile = data.resultFilesList.get( (int) getLinesRead() );
    RowMetaAndData r = resultFile.getRow();

    if ( first ) {
      first = false;
      data.outputRowMeta = new RowMeta();
      smi.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
    }
    incrementLinesRead();

    putRow( data.outputRowMeta, r.getData() ); // copy row to possible alternate
    // rowset(s).

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "FilesFromResult.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    data = (FilesFromResultData) sdi;

    if ( super.init( smi, sdi ) ) {
      Result result = getTrans().getPreviousResult();

      if ( result != null ) {
        data.resultFilesList = result.getResultFilesList();
      } else {
        data.resultFilesList = null;
      }

      // Add init code here.
      return true;
    }
    return false;
  }
}
