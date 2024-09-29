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
import java.util.Objects;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * Contains the meta data for the ReservoirSampling step.
 *
 * @author Mark Hall (mhall{[at]}pentaho.org)
 * @version 1.0
 */
public class ReservoirSamplingMeta extends BaseStepMeta implements StepMetaInterface {

  public static final String XML_TAG = "reservoir_sampling";

  // Size of the sample to output
  protected String m_sampleSize = "100";

  // Seed for the random number generator
  protected String m_randomSeed = "1";

  /**
   * Creates a new <code>ReservoirMeta</code> instance.
   */
  public ReservoirSamplingMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * Get the sample size to generate.
   *
   * @return the sample size
   */
  public String getSampleSize() {
    return m_sampleSize;
  }

  /**
   * Set the size of the sample to generate
   *
   * @param sampleS
   *          the size of the sample
   */
  public void setSampleSize( String sampleS ) {
    m_sampleSize = sampleS;
  }

  /**
   * Get the random seed
   *
   * @return the random seed
   */
  public String getSeed() {
    return m_randomSeed;
  }

  /**
   * Set the seed value for the random number generator
   *
   * @param seed
   *          the seed value
   */
  public void setSeed( String seed ) {
    m_randomSeed = seed;
  }

  /**
   * Return the XML describing this (configured) step
   *
   * @return a <code>String</code> containing the XML
   */
  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    retval.append( XMLHandler.addTagValue( "sample_size", m_sampleSize ) );
    retval.append( XMLHandler.addTagValue( "seed", m_randomSeed ) );
    retval.append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return retval.toString();
  }

  /**
   * Check for equality
   *
   * @param obj
   *          an <code>Object</code> to compare with
   * @return true if equal to the supplied object
   */
  public boolean equals( Object obj ) {
    if ( obj != null && ( obj.getClass().equals( this.getClass() ) ) ) {
      ReservoirSamplingMeta m = (ReservoirSamplingMeta) obj;
      return ( getXML() == m.getXML() );
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash( m_sampleSize, m_randomSeed );
  }

  /**
   * Set the defaults for this step.
   */
  public void setDefault() {
    m_sampleSize = "100";
    m_randomSeed = "1";
  }

  /**
   * Clone this step's meta data
   *
   * @return the cloned meta data
   */
  public Object clone() {
    ReservoirSamplingMeta retval = (ReservoirSamplingMeta) super.clone();
    return retval;
  }

  /**
   * Loads the meta data for this (configured) step from XML.
   *
   * @param stepnode
   *          the step to load
   * @exception KettleXMLException
   *              if an error occurs
   */
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {

    int nrSteps = XMLHandler.countNodes( stepnode, XML_TAG );

    if ( nrSteps > 0 ) {
      Node reservoirnode = XMLHandler.getSubNodeByNr( stepnode, XML_TAG, 0 );

      m_sampleSize = XMLHandler.getTagValue( reservoirnode, "sample_size" );
      m_randomSeed = XMLHandler.getTagValue( reservoirnode, "seed" );

    }
  }

  /**
   * Read this step's configuration from a repository
   *
   * @param rep
   *          the repository to access
   * @param metaStore
   *          the MetaStore to read from
   * @param id_step
   *          the id for this step
   * @exception KettleException
   *              if an error occurs
   */
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {

    m_sampleSize = rep.getStepAttributeString( id_step, "sample_size" );
    m_randomSeed = rep.getStepAttributeString( id_step, "seed" );
  }

  /**
   * Save this step's meta data to a repository
   *
   * @param rep
   *          the repository to save to
   * @param id_transformation
   *          transformation id
   * @param id_step
   *          step id
   * @exception KettleException
   *              if an error occurs
   */
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {

    rep.saveStepAttribute( id_transformation, id_step, "sample_size", m_sampleSize );
    rep.saveStepAttribute( id_transformation, id_step, "seed", m_randomSeed );
  }

  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    // nothing to do, as no fields are added/deleted
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;

    if ( ( prev == null ) || ( prev.size() == 0 ) ) {
      cr =
        new CheckResult(
          CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "
          + prev.size() + " fields", stepMeta );
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
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new ReservoirSampling( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  /**
   * Get a new instance of the appropriate data class. This data class implements the StepDataInterface. It basically
   * contains the persisting data that needs to live on, even if a worker thread is terminated.
   *
   * @return a <code>StepDataInterface</code> value
   */
  public StepDataInterface getStepData() {
    return new ReservoirSamplingData();
  }
}
