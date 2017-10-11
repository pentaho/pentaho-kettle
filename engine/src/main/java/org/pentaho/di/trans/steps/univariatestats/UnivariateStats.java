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

package org.pentaho.di.trans.steps.univariatestats;

import java.util.ArrayList;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Calculate univariate statistics based on one column of the input data.
 * <p>
 * Calculates N, mean, standard deviation, minimum, maximum, median and arbitrary percentiles. Percentiles can be
 * calculated using interpolation or a simple method. See <a
 * href="http://www.itl.nist.gov/div898/handbook/prc/section2/prc252.htm"> The Engineering Statistics Handbook</a> for
 * details.
 *
 * @author Mark Hall (mhall{[at]}pentaho.org)
 * @version 1.0
 */
public class UnivariateStats extends BaseStep implements StepInterface {

  private UnivariateStatsMeta m_meta;
  private UnivariateStatsData m_data;

  /**
   * holds cached input values if median/percentiles are to be calculated
   */
  private ArrayList<Number>[] m_dataCache;

  /**
   * Creates a new <code>UnivariateStats</code> instance.
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
  public UnivariateStats( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
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
  @SuppressWarnings( { "unchecked" } )
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    m_meta = (UnivariateStatsMeta) smi;
    m_data = (UnivariateStatsData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      // compute the derived stats and generate an output row
      Object[] outputRow = generateOutputRow();
      // emit the single output row
      putRow( m_data.getOutputRowMeta(), outputRow );
      setOutputDone();

      // save memory
      m_dataCache = null;

      return false;
    }

    // Handle the first row
    if ( first ) {
      first = false;
      // Don't want to clone and add to the input meta data - want
      // to create a new row meta data for derived calculations
      RowMetaInterface outputMeta = new RowMeta();

      m_data.setInputRowMeta( getInputRowMeta() );
      m_data.setOutputRowMeta( outputMeta );

      // Determine the output format
      m_meta.getFields( m_data.getOutputRowMeta(), getStepname(), null, null, this, repository, metaStore );

      // Set up data cache for calculating median/percentiles
      m_dataCache = new ArrayList[m_meta.getNumFieldsToProcess()];

      // Initialize the step meta data
      FieldIndex[] fi = new FieldIndex[m_meta.getNumFieldsToProcess()];

      m_data.setFieldIndexes( fi );

      // allocate the field indexes in the data class and meta stats functions
      // in the step meta
      for ( int i = 0; i < m_meta.getNumFieldsToProcess(); i++ ) {
        UnivariateStatsMetaFunction usmf = m_meta.getInputFieldMetaFunctions()[i];
        //CHECKSTYLE:Indentation:OFF
        m_data.getFieldIndexes()[i] = new FieldIndex();

        // check that this univariate stats computation has been
        // defined on an input field
        if ( !Utils.isEmpty( usmf.getSourceFieldName() ) ) {
          int fieldIndex = m_data.getInputRowMeta().indexOfValue( usmf.getSourceFieldName() );

          if ( fieldIndex < 0 ) {
            throw new KettleStepException( "Unable to find the specified fieldname '"
              + usmf.getSourceFieldName() + "' for stats calc #" + ( i + 1 ) );
          }

          FieldIndex tempData = m_data.getFieldIndexes()[i];

          tempData.m_columnIndex = fieldIndex;

          ValueMetaInterface inputFieldMeta = m_data.getInputRowMeta().getValueMeta( fieldIndex );

          // check the type of the input field
          if ( !inputFieldMeta.isNumeric() ) {
            throw new KettleException( "The input field for stats calc #" + ( i + 1 ) + "is not numeric." );
          }

          // finish initializing
          tempData.m_min = Double.MAX_VALUE;
          tempData.m_max = Double.MIN_VALUE;

          // set up caches if median/percentiles have been
          // requested

          if ( usmf.getCalcMedian() || usmf.getCalcPercentile() >= 0 ) {
            m_dataCache[i] = new ArrayList<Number>();
          }
        } else {
          throw new KettleException( "There is no input field specified for stats calc #" + ( i + 1 ) );
        }
      }
    } // end (if first)

    for ( int i = 0; i < m_meta.getNumFieldsToProcess(); i++ ) {

      UnivariateStatsMetaFunction usmf = m_meta.getInputFieldMetaFunctions()[i];
      if ( !Utils.isEmpty( usmf.getSourceFieldName() ) ) {
        FieldIndex tempData = m_data.getFieldIndexes()[i];

        ValueMetaInterface metaI = getInputRowMeta().getValueMeta( tempData.m_columnIndex );

        Number input = null;
        try {
          input = metaI.getNumber( r[tempData.m_columnIndex] );
        } catch ( Exception ex ) {
          // quietly ignore -- assume missing for anything not
          // parsable as a number
        }
        if ( input != null ) {

          // add to the cache?
          if ( usmf.getCalcMedian() || usmf.getCalcPercentile() >= 0 ) {
            m_dataCache[i].add( input );
          }

          // update stats
          double val = input.doubleValue();
          tempData.m_count++;
          tempData.m_sum += val;
          tempData.m_sumSq += ( val * val );
          if ( val < tempData.m_min ) {
            tempData.m_min = val;
          }
          if ( val > tempData.m_max ) {
            tempData.m_max = val;
          }

        } // otherwise, treat non-numeric values as missing
      }
    }

    if ( log.isRowLevel() ) {
      logRowlevel( "Read row #" + getLinesRead() + " : " + r );
    }

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( "Linenr " + getLinesRead() );
    }
    return true;
  }

  /**
   * Generates an output row
   *
   * @return an <code>Object[]</code> value
   */
  private Object[] generateOutputRow() {

    int totalNumOutputFields = 0;

    for ( int i = 0; i < m_meta.getNumFieldsToProcess(); i++ ) {
      UnivariateStatsMetaFunction usmf = m_meta.getInputFieldMetaFunctions()[i];

      if ( !Utils.isEmpty( usmf.getSourceFieldName() ) ) {
        totalNumOutputFields += usmf.numberOfMetricsRequested();
      }
    }

    Object[] result = new Object[totalNumOutputFields];
    int index = 0;
    for ( int i = 0; i < m_meta.getNumFieldsToProcess(); i++ ) {
      UnivariateStatsMetaFunction usmf = m_meta.getInputFieldMetaFunctions()[i];

      if ( !Utils.isEmpty( usmf.getSourceFieldName() ) ) {
        Object[] tempOut = m_data.getFieldIndexes()[i].generateOutputValues( usmf, m_dataCache[i] );

        for ( int j = 0; j < tempOut.length; j++ ) {
          result[index++] = tempOut[j];
        }
      }
    }

    return result;
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
    m_meta = (UnivariateStatsMeta) smi;
    m_data = (UnivariateStatsData) sdi;

    if ( super.init( smi, sdi ) ) {
      return true;
    }
    return false;
  }
}
