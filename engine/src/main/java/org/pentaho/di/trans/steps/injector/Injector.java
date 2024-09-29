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


package org.pentaho.di.trans.steps.injector;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Executor class to allow a java program to inject rows of data into a transformation. This step can be used as a
 * starting point in such a "headless" transformation.
 *
 * @since 22-jun-2006
 */
public class Injector extends BaseStep implements StepInterface {
  private static Class<?> PKG = InjectorMeta.class; // for i18n purposes, needed by Translator2!!

  public Injector( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    // Get a row from the previous step OR from an extra RowSet
    //
    Object[] row = getRow();

    // Nothing more to be had from any input rowset
    //
    if ( row == null ) {
      setOutputDone();
      return false;
    }

    putRow( getInputRowMeta(), row ); // copy row to possible alternate rowset(s).

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "Injector.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }
}
