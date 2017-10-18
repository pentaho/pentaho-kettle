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

package org.pentaho.di.trans.steps.prioritizestreams;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Prioritize INPUT Streams.
 *
 * @author Samatar
 * @since 30-06-2008
 */

public class PrioritizeStreams extends BaseStep implements StepInterface {
  private static Class<?> PKG = PrioritizeStreamsMeta.class; // for i18n purposes, needed by Translator2!!

  private PrioritizeStreamsMeta meta;
  private PrioritizeStreamsData data;

  public PrioritizeStreams( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (PrioritizeStreamsMeta) smi;
    data = (PrioritizeStreamsData) sdi;

    if ( first ) {
      if ( meta.getStepName() != null || meta.getStepName().length > 0 ) {
        data.stepnrs = meta.getStepName().length;
        data.rowSets = new RowSet[data.stepnrs];

        for ( int i = 0; i < data.stepnrs; i++ ) {
          data.rowSets[i] = findInputRowSet( meta.getStepName()[i] );
          if ( i > 0 ) {
            // Compare layout of first stream with the current stream
            checkInputLayoutValid( data.rowSets[0].getRowMeta(), data.rowSets[i].getRowMeta() );
          }
        }
      } else {
        // error
        throw new KettleException( BaseMessages.getString( PKG, "PrioritizeStreams.Error.NotInputSteps" ) );
      }
      data.currentRowSet = data.rowSets[0];
    } // end if first, part 1

    Object[] input = getOneRow();

    while ( input == null && data.stepnr < data.stepnrs - 1 && !isStopped() ) {
      input = getOneRow();
    }

    if ( input == null ) {
      // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first ) {
      // Take the row Meta from the first rowset read
      data.outputRowMeta = data.currentRowSet.getRowMeta();
      first = false;
    }

    putRow( data.outputRowMeta, input );

    return true;
  }

  private Object[] getOneRow() throws KettleException {
    Object[] input = getRowFrom( data.currentRowSet );
    if ( input == null ) {
      if ( data.stepnr < data.stepnrs - 1 ) {
        // read rows from the next step
        data.stepnr++;
        data.currentRowSet = data.rowSets[data.stepnr];
        input = getRowFrom( data.currentRowSet );
      }
    }
    return input;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PrioritizeStreamsMeta) smi;
    data = (PrioritizeStreamsData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      data.stepnr = 0;
      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    data.currentRowSet = null;
    data.rowSets = null;
    super.dispose( smi, sdi );
  }

  /**
   * Checks whether 2 template rows are compatible for the mergestep.
   *
   * @param referenceRow
   *          Reference row
   * @param compareRow
   *          Row to compare to
   *
   * @return true when templates are compatible.
   * @throws KettleRowException
   *           in case there is a compatibility error.
   */
  protected void checkInputLayoutValid( RowMetaInterface referenceRowMeta, RowMetaInterface compareRowMeta ) throws KettleRowException {
    if ( referenceRowMeta != null && compareRowMeta != null ) {
      BaseStep.safeModeChecking( referenceRowMeta, compareRowMeta );
    }
  }
}
