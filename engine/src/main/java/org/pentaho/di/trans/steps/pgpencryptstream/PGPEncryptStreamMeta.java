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

package org.pentaho.di.trans.steps.pgpencryptstream;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
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

/*
 * Created on 03-Juin-2008
 *
 */

public class PGPEncryptStreamMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = PGPEncryptStreamMeta.class; // for i18n purposes, needed by Translator2!!

  /** GPG location */
  private String gpglocation;

  /** Key name **/
  private String keyname;

  /** dynamic stream filed */
  private String streamfield;

  /** function result: new value name */
  private String resultfieldname;

  /** Flag: keyname is dynamic **/
  private boolean keynameInField;

  /** keyname fieldname **/
  private String keynameFieldName;

  public PGPEncryptStreamMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @deprecated - typo
   */
  @Deprecated
  public void setGPGPLocation( String value ) {
    this.setGPGLocation( value );
  }

  public void setGPGLocation( String value ) {
    this.gpglocation = value;
  }

  public String getGPGLocation() {
    return gpglocation;
  }

  /**
   * @return Returns the streamfield.
   */
  public String getStreamField() {
    return streamfield;
  }

  /**
   * @param streamfield
   *          The streamfield to set.
   */
  public void setStreamField( String streamfield ) {
    this.streamfield = streamfield;
  }

  /**
   * @return Returns the keynameFieldName.
   */
  public String getKeynameFieldName() {
    return keynameFieldName;
  }

  /**
   * @param keynameFieldName
   *          The keynameFieldName to set.
   */
  public void setKeynameFieldName( String keynameFieldName ) {
    this.keynameFieldName = keynameFieldName;
  }

  /**
   * @return Returns the keynameInField.
   */
  public boolean isKeynameInField() {
    return keynameInField;
  }

  /**
   * @param keynameInField
   *          The keynameInField to set.
   */
  public void setKeynameInField( boolean keynameInField ) {
    this.keynameInField = keynameInField;
  }

  /**
   * @return Returns the resultName.
   */
  public String getResultFieldName() {
    return resultfieldname;
  }

  /**
   * @deprecated - typo
   */
  @Deprecated
  public void setResultfieldname( String value ) {
    this.setResultFieldName( value );
  }

  /**
   * @param resultfieldname
   *          The resultfieldname to set.
   */
  public void setResultFieldName( String value ) {
    this.resultfieldname = value;
  }

  /**
   * @return Returns the keyname.
   */
  public String getKeyName() {
    return keyname;
  }

  /**
   * @param keyname
   *          The keyname to set.
   */
  public void setKeyName( String keyname ) {
    this.keyname = keyname;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  @Override
  public Object clone() {
    PGPEncryptStreamMeta retval = (PGPEncryptStreamMeta) super.clone();

    return retval;
  }

  @Override
  public void setDefault() {
    resultfieldname = "result";
    streamfield = null;
    keyname = null;
    gpglocation = null;
    keynameInField = false;
    keynameFieldName = null;
  }

  @Override
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Output fields (String)
    if ( !Utils.isEmpty( resultfieldname ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( resultfieldname ) );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }

  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "    " + XMLHandler.addTagValue( "gpglocation", gpglocation ) );
    retval.append( "    " + XMLHandler.addTagValue( "keyname", keyname ) );
    retval.append( "    " + XMLHandler.addTagValue( "keynameInField", keynameInField ) );
    retval.append( "    " + XMLHandler.addTagValue( "keynameFieldName", keynameFieldName ) );
    retval.append( "    " + XMLHandler.addTagValue( "streamfield", streamfield ) );
    retval.append( "    " + XMLHandler.addTagValue( "resultfieldname", resultfieldname ) );
    return retval.toString();
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      gpglocation = XMLHandler.getTagValue( stepnode, "gpglocation" );
      keyname = XMLHandler.getTagValue( stepnode, "keyname" );

      keynameInField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "keynameInField" ) );
      keynameFieldName = XMLHandler.getTagValue( stepnode, "keynameFieldName" );
      streamfield = XMLHandler.getTagValue( stepnode, "streamfield" );
      resultfieldname = XMLHandler.getTagValue( stepnode, "resultfieldname" );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "PGPEncryptStreamMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      gpglocation = rep.getStepAttributeString( id_step, "gpglocation" );
      keyname = rep.getStepAttributeString( id_step, "keyname" );
      keynameInField = rep.getStepAttributeBoolean( id_step, "keynameInField" );
      keynameFieldName = rep.getStepAttributeString( id_step, "keynameFieldName" );
      streamfield = rep.getStepAttributeString( id_step, "streamfield" );
      resultfieldname = rep.getStepAttributeString( id_step, "resultfieldname" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "PGPEncryptStreamMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "gpglocation", gpglocation );
      rep.saveStepAttribute( id_transformation, id_step, "keyname", keyname );
      rep.saveStepAttribute( id_transformation, id_step, "keynameInField", keynameInField );
      rep.saveStepAttribute( id_transformation, id_step, "keynameFieldName", keynameFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "streamfield", streamfield );
      rep.saveStepAttribute( id_transformation, id_step, "resultfieldname", resultfieldname );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "PGPEncryptStreamMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    if ( Utils.isEmpty( gpglocation ) ) {
      error_message = BaseMessages.getString( PKG, "PGPEncryptStreamMeta.CheckResult.GPGLocationMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "PGPEncryptStreamMeta.CheckResult.GPGLocationOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
    }
    if ( !isKeynameInField() ) {
      if ( Utils.isEmpty( keyname ) ) {
        error_message = BaseMessages.getString( PKG, "PGPEncryptStreamMeta.CheckResult.KeyNameMissing" );
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        error_message = BaseMessages.getString( PKG, "PGPEncryptStreamMeta.CheckResult.KeyNameOK" );
        cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      }
    }
    if ( Utils.isEmpty( resultfieldname ) ) {
      error_message = BaseMessages.getString( PKG, "PGPEncryptStreamMeta.CheckResult.ResultFieldMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "PGPEncryptStreamMeta.CheckResult.ResultFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      remarks.add( cr );
    }
    if ( Utils.isEmpty( streamfield ) ) {
      error_message = BaseMessages.getString( PKG, "PGPEncryptStreamMeta.CheckResult.StreamFieldMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "PGPEncryptStreamMeta.CheckResult.StreamFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      remarks.add( cr );
    }
    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "PGPEncryptStreamMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "PGPEncryptStreamMeta.CheckResult.NoInpuReceived" ), stepMeta );
      remarks.add( cr );
    }

  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new PGPEncryptStream( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new PGPEncryptStreamData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

}
