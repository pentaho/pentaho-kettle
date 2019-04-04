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

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Contains the meta-data for the UnivariateStats step: calculates predefined univariate statistics
 *
 * @author Mark Hall (mhall{[at]}pentaho.org)
 * @version 1.0
 */
public class UnivariateStatsMeta extends BaseStepMeta implements StepMetaInterface {

  // The stats to be computed for various input fields.
  // User may elect to omit some stats for particular fields.
  private UnivariateStatsMetaFunction[] m_stats;

  /**
   * Creates a new <code>UnivariateStatsMeta</code> instance.
   */
  public UnivariateStatsMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * Get the stats to be computed for the input fields
   *
   * @return an <code>UnivariateStatsMetaFunction[]</code> value
   */
  public UnivariateStatsMetaFunction[] getInputFieldMetaFunctions() {
    return m_stats;
  }

  /**
   * Returns how many UnivariateStatsMetaFunctions are currently being used. Each UnivariateStatsMetaFunction represents
   * an input field to be processed along with the user-requested stats to compute for it. The same input field may
   * occur in more than one UnivariateStatsMetaFunction as more than one percentile may be required.
   *
   * @return the number of non-unique input fields
   */
  public int getNumFieldsToProcess() {
    return m_stats.length;
  }

  /**
   * Set the stats to be computed for the input fields
   *
   * @param mf
   *          an array of <code>UnivariateStatsMetaFunction</code>s
   */
  public void setInputFieldMetaFunctions( UnivariateStatsMetaFunction[] mf ) {
    m_stats = mf;
  }

  /**
   * Allocate space for stats to compute
   *
   * @param nrStats
   *          the number of UnivariateStatsMetaFunctions to allocate
   */
  public void allocate( int nrStats ) {
    m_stats = new UnivariateStatsMetaFunction[nrStats];
  }

  /**
   * Loads the meta data for this (configured) step from XML.
   *
   * @param stepnode
   *          the step to load
   * @exception KettleXMLException
   *              if an error occurs
   */
  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {

    int nrStats = XMLHandler.countNodes( stepnode, UnivariateStatsMetaFunction.XML_TAG );

    allocate( nrStats );
    for ( int i = 0; i < nrStats; i++ ) {
      Node statnode = XMLHandler.getSubNodeByNr( stepnode, UnivariateStatsMetaFunction.XML_TAG, i );
      m_stats[i] = new UnivariateStatsMetaFunction( statnode );
    }
  }

  /**
   * Return the XML describing this (configured) step
   *
   * @return a <code>String</code> containing the XML
   */
  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    if ( m_stats != null ) {
      for ( int i = 0; i < m_stats.length; i++ ) {
        retval.append( "       " ).append( m_stats[i].getXML() ).append( Const.CR );
      }
    }
    return retval.toString();
  }

  /**
   * Check for equality
   *
   * @param obj
   *          an <code>Object</code> to compare with
   * @return true if equal to the supplied object
   */
  @Override
  public boolean equals( Object obj ) {
    if ( obj != null && ( obj.getClass().equals( this.getClass() ) ) ) {
      UnivariateStatsMeta m = (UnivariateStatsMeta) obj;
      return ( getXML().equals( m.getXML() ) );
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode( m_stats );
  }

  /**
   * Clone this step's meta data
   *
   * @return the cloned meta data
   */
  @Override
  public Object clone() {
    UnivariateStatsMeta retval = (UnivariateStatsMeta) super.clone();
    if ( m_stats != null ) {
      retval.allocate( m_stats.length );
      for ( int i = 0; i < m_stats.length; i++ ) {
        // CHECKSTYLE:Indentation:OFF
        retval.getInputFieldMetaFunctions()[i] = (UnivariateStatsMetaFunction) m_stats[i].clone();
      }
    } else {
      retval.allocate( 0 );
    }
    return retval;
  }

  /**
   * Set the default state of the meta data?
   */
  @Override
  public void setDefault() {
    m_stats = new UnivariateStatsMetaFunction[0];
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {

    int nrStats = rep.countNrStepAttributes( id_step, "source_field_name" );
    allocate( nrStats );

    for ( int i = 0; i < nrStats; i++ ) {
      m_stats[i] = new UnivariateStatsMetaFunction( rep, id_step, i );
    }
  }

  /**
   * Save this step's meta data to a repository
   *
   * @param rep
   *          the repository to save to
   * @param metaStore
   *          the MetaStore to save to
   * @param id_transformation
   *          transformation id
   * @param id_step
   *          step id
   * @exception KettleException
   *              if an error occurs
   */
  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {

    for ( int i = 0; i < m_stats.length; i++ ) {
      m_stats[i].saveRep( rep, metaStore, id_transformation, id_step, i );
    }
  }

  @Override
  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    row.clear();
    for ( int i = 0; i < m_stats.length; i++ ) {
      UnivariateStatsMetaFunction fn = m_stats[i];

      ValueMetaInterface[] vmis = getValueMetas( fn, origin );

      for ( int j = 0; j < vmis.length; j++ ) {
        row.addValueMeta( vmis[j] );
      }
    }
  }

  /**
   * Returns an array of ValueMetaInterface that contains the meta data for each value computed by the supplied
   * UnivariateStatsMetaFunction
   *
   * @param fn
   *          the <code>UnivariateStatsMetaFunction</code> to construct meta data for
   * @param origin
   *          the origin
   * @return an array of meta data
   */
  private ValueMetaInterface[] getValueMetas( UnivariateStatsMetaFunction fn, String origin ) {

    ValueMetaInterface[] v = new ValueMetaInterface[fn.numberOfMetricsRequested()];

    int index = 0;
    if ( fn.getCalcN() ) {
      v[index] = new ValueMetaNumber( fn.getSourceFieldName() + "(N)" );
      v[index].setOrigin( origin );
      index++;
    }

    if ( fn.getCalcMean() ) {
      v[index] = new ValueMetaNumber( fn.getSourceFieldName() + "(mean)" );
      v[index].setOrigin( origin );
      index++;
    }

    if ( fn.getCalcStdDev() ) {
      v[index] = new ValueMetaNumber( fn.getSourceFieldName() + "(stdDev)" );
      v[index].setOrigin( origin );
      index++;
    }

    if ( fn.getCalcMin() ) {
      v[index] = new ValueMetaNumber( fn.getSourceFieldName() + "(min)" );
      v[index].setOrigin( origin );
      index++;
    }

    if ( fn.getCalcMax() ) {
      v[index] = new ValueMetaNumber( fn.getSourceFieldName() + "(max)" );
      v[index].setOrigin( origin );
      index++;
    }

    if ( fn.getCalcMedian() ) {
      v[index] = new ValueMetaNumber( fn.getSourceFieldName() + "(median)" );
      v[index].setOrigin( origin );
      index++;
    }

    if ( fn.getCalcPercentile() >= 0 ) {
      double percent = fn.getCalcPercentile();
      // NumberFormat pF = NumberFormat.getPercentInstance();
      NumberFormat pF = NumberFormat.getInstance();
      pF.setMaximumFractionDigits( 2 );
      String res = pF.format( percent * 100 );
      v[index] = new ValueMetaNumber( fn.getSourceFieldName() + "(" + res + "th percentile)" );
      v[index].setOrigin( origin );
      index++;
    }
    return v;
  }

  /**
   * Check the settings of this step and put findings in a remarks list.
   *
   * @param remarks
   *          the list to put the remarks in. see <code>org.pentaho.di.core.CheckResult</code>
   * @param transmeta
   *          the transform meta data
   * @param stepMeta
   *          the step meta data
   * @param prev
   *          the fields coming from a previous step
   * @param input
   *          the input step names
   * @param output
   *          the output step names
   * @param info
   *          the fields that are used as information by the step
   */
  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {

    CheckResult cr;

    if ( ( prev == null ) || ( prev.size() == 0 ) ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving " + prev.size()
              + " fields", stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta );
      remarks.add( cr );
    }
  }

  /**
   * Get the executing step, needed by Trans to launch a step.
   *
   * @param stepMeta
   *          the step info
   * @param stepDataInterface
   *          the step data interface linked to this step. Here the step can store temporary data, database connections,
   *          etc.
   * @param cnr
   *          the copy number to get.
   * @param tr
   *          the transformation info.
   * @param trans
   *          the launching transformation
   * @return a <code>StepInterface</code> value
   */
  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
      Trans trans ) {
    return new UnivariateStats( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  /**
   * Get a new instance of the appropriate data class. This data class implements the StepDataInterface. It basically
   * contains the persisting data that needs to live on, even if a worker thread is terminated.
   *
   * @return a <code>StepDataInterface</code> value
   */
  @Override
  public StepDataInterface getStepData() {
    return new UnivariateStatsData();
  }
}
