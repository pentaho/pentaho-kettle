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

package org.pentaho.di.trans.steps.reservoirsampling;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.reservoirsampling.ReservoirSamplingData.PROC_MODE;

public class ReservoirSampling extends BaseStep implements StepInterface {

  private ReservoirSamplingMeta m_meta;
  private ReservoirSamplingData m_data;

  /**
   * Creates a new <code>ReservoirSampling</code> instance.
   * <p>
   *
   * Implements the reservoir sampling algorithm "R" by Jeffrey Scott Vitter. (algorithm is implemented in
   * ReservoirSamplingData.java
   * <p>
   * For more information see:<br>
   * <br>
   *
   * Vitter, J. S. Random Sampling with a Reservoir. ACM Transactions on Mathematical Software, Vol. 11, No. 1, March
   * 1985. Pages 37-57.
   *
   * @param stepMeta
   *          holds the step's meta data
   * @param stepDataInterface
   *          holds the step's temporary data
   * @param copyNr
   *          the number assigned to the step
   * @param transMeta
   *          meta data for the transformation
   * @param trans
   *          a <code>Trans</code> value
   */
  public ReservoirSampling( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Process an incoming row of data.
   *
   * @param smi
   *          a <code>StepMetaInterface</code> value
   * @param sdi
   *          a <code>StepDataInterface</code> value
   * @return a <code>boolean</code> value
   * @exception KettleException
   *              if an error occurs
   */
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    if ( m_data.getProcessingMode() == PROC_MODE.DISABLED ) {
      setOutputDone();
      m_data.cleanUp();
      return ( false );
    }

    m_meta = (ReservoirSamplingMeta) smi;
    m_data = (ReservoirSamplingData) sdi;

    Object[] r = getRow();

    // Handle the first row
    if ( first ) {
      first = false;
      if ( r == null ) { // no input to be expected...

        setOutputDone();
        return false;
      }

      // Initialize the data object
      m_data.setOutputRowMeta( getInputRowMeta().clone() );
      String sampleSize = getTransMeta().environmentSubstitute( m_meta.getSampleSize() );
      String seed = getTransMeta().environmentSubstitute( m_meta.getSeed() );
      m_data.initialize( Integer.valueOf( sampleSize ), Integer.valueOf( seed ) );

      // no real reason to determine the output fields here
      // as we don't add/delete any fields
    } // end (if first)

    if ( m_data.getProcessingMode() == PROC_MODE.PASSTHROUGH ) {
      if ( r == null ) {
        setOutputDone();
        m_data.cleanUp();
        return ( false );
      }
      putRow( m_data.getOutputRowMeta(), r );
    } else if ( m_data.getProcessingMode() == PROC_MODE.SAMPLING ) {
      if ( r == null ) {
        // Output the rows in the sample
        List<Object[]> samples = m_data.getSample();

        int numRows = ( samples != null ) ? samples.size() : 0;
        logBasic( this.getStepname()
          + " Actual/Sample: " + numRows + "/" + m_data.m_k + " Seed:"
          + getTransMeta().environmentSubstitute( m_meta.m_randomSeed ) );
        if ( samples != null ) {
          for ( int i = 0; i < samples.size(); i++ ) {
            Object[] sample = samples.get( i );
            if ( sample != null ) {
              putRow( m_data.getOutputRowMeta(), sample );
            } else {
              // user probably requested more rows in
              // the sample than there were in total
              // in the end. Just break in this case
              break;
            }
          }
        }
        setOutputDone();
        m_data.cleanUp();
        return false;
      }

      // just pass the row to the data class for possible caching
      // in the sample
      m_data.processRow( r );
    }

    if ( log.isRowLevel() ) {
      logRowlevel( "Read row #" + getLinesRead() + " : " + r );
    }

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( "Line number " + getLinesRead() );
    }
    return true;
  }

  /**
   * Initialize the step.
   *
   * @param smi
   *          a <code>StepMetaInterface</code> value
   * @param sdi
   *          a <code>StepDataInterface</code> value
   * @return a <code>boolean</code> value
   */
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    m_meta = (ReservoirSamplingMeta) smi;
    m_data = (ReservoirSamplingData) sdi;

    if ( super.init( smi, sdi ) ) {

      boolean remoteInput = getStepMeta().getRemoteInputSteps().size() > 0;
      List<StepMeta> previous = getTransMeta().findPreviousSteps( getStepMeta() );
      if ( !remoteInput && ( previous == null || previous.size() <= 0 ) ) {
        m_data.setProcessingMode( PROC_MODE.DISABLED );
      }
      return true;
    }
    return false;
  }

  /**
   * Run is where the action happens!
   */
  public void run() {
    logBasic( "Starting to run..." );
    try {
      // Wait
      while ( processRow( m_meta, m_data ) ) {
        if ( isStopped() ) {
          break;
        }
      }
    } catch ( Exception e ) {
      logError( "Unexpected error : " + e.toString() );
      logError( Const.getStackTracker( e ) );
      setErrors( 1 );
      stopAll();
    } finally {
      dispose( m_meta, m_data );
      logBasic( "Finished, processing " + getLinesRead() + " rows" );
      markStop();
    }
  }
}
