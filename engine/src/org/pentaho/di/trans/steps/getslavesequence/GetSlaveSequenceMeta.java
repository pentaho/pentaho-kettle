/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getslavesequence;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
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
 * Meta data for the Add Sequence step.
 *
 * Created on 13-may-2003
 */
public class GetSlaveSequenceMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = GetSlaveSequenceMeta.class; // for i18n purposes, needed by Translator2!!

  private String valuename;
  private String slaveServerName;
  private String sequenceName;
  private String increment;

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      valuename = XMLHandler.getTagValue( stepnode, "valuename" );
      slaveServerName = XMLHandler.getTagValue( stepnode, "slave" );
      sequenceName = XMLHandler.getTagValue( stepnode, "seqname" );
      increment = XMLHandler.getTagValue( stepnode, "increment" );
    } catch ( Exception e ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "GetSequenceMeta.Exception.ErrorLoadingStepInfo" ), e );
    }
  }

  public void setDefault() {
    valuename = "id";
    slaveServerName = "slave server name";
    sequenceName = "Slave Sequence Name -- To be configured";
    increment = "10000";
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    ValueMetaInterface v = new ValueMeta( valuename, ValueMetaInterface.TYPE_INTEGER );
    v.setOrigin( name );
    row.addValueMeta( v );
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 300 );

    retval.append( "      " ).append( XMLHandler.addTagValue( "valuename", valuename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "slave", slaveServerName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "seqname", sequenceName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "increment", increment ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      valuename = rep.getStepAttributeString( id_step, "valuename" );
      slaveServerName = rep.getStepAttributeString( id_step, "slave" );
      sequenceName = rep.getStepAttributeString( id_step, "seqname" );
      increment = rep.getStepAttributeString( id_step, "increment" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GetSequenceMeta.Exception.UnableToReadStepInfo" )
        + id_step, e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "valuename", valuename );
      rep.saveStepAttribute( id_transformation, id_step, "slave", slaveServerName );
      rep.saveStepAttribute( id_transformation, id_step, "seqname", sequenceName );
      rep.saveStepAttribute( id_transformation, id_step, "increment", increment );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GetSequenceMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "GetSequenceMeta.CheckResult.StepIsReceving.Title" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "GetSequenceMeta.CheckResult.NoInputReceived.Title" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new GetSlaveSequence( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new GetSlaveSequenceData();
  }

  /**
   * @return the valuename
   */
  public String getValuename() {
    return valuename;
  }

  /**
   * @param valuename
   *          the valuename to set
   */
  public void setValuename( String valuename ) {
    this.valuename = valuename;
  }

  /**
   * @return the slaveServerName
   */
  public String getSlaveServerName() {
    return slaveServerName;
  }

  /**
   * @param slaveServerName
   *          the slaveServerName to set
   */
  public void setSlaveServerName( String slaveServerName ) {
    this.slaveServerName = slaveServerName;
  }

  /**
   * @return the sequenceName
   */
  public String getSequenceName() {
    return sequenceName;
  }

  /**
   * @param sequenceName
   *          the sequenceName to set
   */
  public void setSequenceName( String sequenceName ) {
    this.sequenceName = sequenceName;
  }

  /**
   * @return the increment
   */
  public String getIncrement() {
    return increment;
  }

  /**
   * @param increment
   *          the increment to set
   */
  public void setIncrement( String increment ) {
    this.increment = increment;
  }

}
