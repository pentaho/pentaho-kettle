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

package org.pentaho.di.trans.steps.pgpdecryptstream;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
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

public class PGPDecryptStreamMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = PGPDecryptStreamMeta.class; // for i18n purposes, needed by Translator2!!

  /** GPG location */
  private String gpglocation;

  /** passhrase **/
  private String passhrase;

  /** Flag : passphrase from field **/
  private boolean passphraseFromField;

  /** passphrase fieldname **/
  private String passphraseFieldName;

  /** dynamic stream filed */
  private String streamfield;

  /** function result: new value name */
  private String resultfieldname;

  public PGPDecryptStreamMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @deprecated typo
   * @param gpglocation
   */
  @Deprecated
  public void setGPGPLocation( String value ) {
    this.setGPGLocation( value );
  }

  public void setGPGLocation( String gpglocation ) {
    this.gpglocation = gpglocation;
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
   * @return Returns the passphraseFieldName.
   */
  public String getPassphraseFieldName() {
    return passphraseFieldName;
  }

  /**
   * @param passphraseFieldName
   *          The passphraseFieldName to set.
   */
  public void setPassphraseFieldName( String passphraseFieldName ) {
    this.passphraseFieldName = passphraseFieldName;
  }

  /**
   * @return Returns the passphraseFromField.
   */
  public boolean isPassphraseFromField() {
    return passphraseFromField;
  }

  /**
   * @param passphraseFromField
   *          The passphraseFromField to set.
   */
  public void setPassphraseFromField( boolean passphraseFromField ) {
    this.passphraseFromField = passphraseFromField;
  }

  /**
   * @return Returns the resultName.
   */
  public String getResultFieldName() {
    return resultfieldname;
  }

  /**
   * @param resultfieldname
   *          The resultfieldname to set.
   * @deprecated typo
   */
  @Deprecated
  public void setResultfieldname( String value ) {
    this.setResultFieldName( value );
  }

  /**
   *
   * @param resultfieldname
   *          The resultFieldName to set
   *
   */
  public void setResultFieldName( String resultfieldname ) {
    this.resultfieldname = resultfieldname;
  }
  /**
   * @return Returns the passhrase.
   */
  public String getPassphrase() {
    return passhrase;
  }

  /**
   * @param passhrase
   *          The passhrase to set.
   */
  public void setPassphrase( String passhrase ) {
    this.passhrase = passhrase;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  @Override
  public Object clone() {
    PGPDecryptStreamMeta retval = (PGPDecryptStreamMeta) super.clone();

    return retval;
  }

  @Override
  public void setDefault() {
    resultfieldname = "result";
    streamfield = null;
    passhrase = null;
    gpglocation = null;
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
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "passhrase", Encr.encryptPasswordIfNotUsingVariables( passhrase ) ) );
    retval.append( "    " + XMLHandler.addTagValue( "streamfield", streamfield ) );
    retval.append( "    " + XMLHandler.addTagValue( "resultfieldname", resultfieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "passphraseFromField", passphraseFromField ) );
    retval.append( "    " + XMLHandler.addTagValue( "passphraseFieldName", passphraseFieldName ) );
    return retval.toString();
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      gpglocation = XMLHandler.getTagValue( stepnode, "gpglocation" );
      passhrase = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "passhrase" ) );
      streamfield = XMLHandler.getTagValue( stepnode, "streamfield" );
      resultfieldname = XMLHandler.getTagValue( stepnode, "resultfieldname" );
      passphraseFromField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "passphraseFromField" ) );
      passphraseFieldName = XMLHandler.getTagValue( stepnode, "passphraseFieldName" );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "PGPDecryptStreamMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      gpglocation = rep.getStepAttributeString( id_step, "gpglocation" );
      passhrase = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "passhrase" ) );

      streamfield = rep.getStepAttributeString( id_step, "streamfield" );
      resultfieldname = rep.getStepAttributeString( id_step, "resultfieldname" );
      passphraseFromField = rep.getStepAttributeBoolean( id_step, "passphraseFromField" );
      passphraseFieldName = rep.getStepAttributeString( id_step, "passphraseFieldName" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "PGPDecryptStreamMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "gpglocation", gpglocation );
      rep.saveStepAttribute( id_transformation, id_step, "passhrase", Encr
        .encryptPasswordIfNotUsingVariables( passhrase ) );
      rep.saveStepAttribute( id_transformation, id_step, "streamfield", streamfield );
      rep.saveStepAttribute( id_transformation, id_step, "resultfieldname", resultfieldname );
      rep.saveStepAttribute( id_transformation, id_step, "passphraseFromField", passphraseFromField );
      rep.saveStepAttribute( id_transformation, id_step, "passphraseFieldName", passphraseFieldName );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "PGPDecryptStreamMeta.Exception.UnableToSaveStepInfo" )
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
      error_message = BaseMessages.getString( PKG, "PGPDecryptStreamMeta.CheckResult.GPGLocationMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "PGPDecryptStreamMeta.CheckResult.GPGLocationOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
    }
    if ( !isPassphraseFromField() ) {
      // Check static pass-phrase
      if ( Utils.isEmpty( passhrase ) ) {
        error_message = BaseMessages.getString( PKG, "PGPDecryptStreamMeta.CheckResult.PassphraseMissing" );
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        error_message = BaseMessages.getString( PKG, "PGPDecryptStreamMeta.CheckResult.PassphraseOK" );
        cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      }
    }
    if ( Utils.isEmpty( resultfieldname ) ) {
      error_message = BaseMessages.getString( PKG, "PGPDecryptStreamMeta.CheckResult.ResultFieldMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "PGPDecryptStreamMeta.CheckResult.ResultFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      remarks.add( cr );
    }
    if ( Utils.isEmpty( streamfield ) ) {
      error_message = BaseMessages.getString( PKG, "PGPDecryptStreamMeta.CheckResult.StreamFieldMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "PGPDecryptStreamMeta.CheckResult.StreamFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      remarks.add( cr );
    }
    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "PGPDecryptStreamMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "PGPDecryptStreamMeta.CheckResult.NoInpuReceived" ), stepMeta );
      remarks.add( cr );
    }

  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new PGPDecryptStream( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new PGPDecryptStreamData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

}
