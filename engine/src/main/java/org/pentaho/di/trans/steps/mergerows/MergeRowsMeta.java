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

package org.pentaho.di.trans.steps.mergerows;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 02-jun-2003
 *
 */
@InjectionSupported( localizationPrefix = "MergeRows.Injection." )
public class MergeRowsMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = MergeRowsMeta.class; // for i18n purposes, needed by Translator2!!

  @Injection( name = "FLAG_FIELD" )
  private String flagField;

  @Injection( name = "KEY_FIELDS" )
  private String[] keyFields;
  @Injection( name = "VALUE_FIELDS" )
  private String[] valueFields;

  /**
   * @return Returns the keyFields.
   */
  public String[] getKeyFields() {
    return keyFields;
  }

  /**
   * @param keyFields
   *          The keyFields to set.
   */
  public void setKeyFields( String[] keyFields ) {
    this.keyFields = keyFields;
  }

  /**
   * @return Returns the valueFields.
   */
  public String[] getValueFields() {
    return valueFields;
  }

  /**
   * @param valueFields
   *          The valueFields to set.
   */
  public void setValueFields( String[] valueFields ) {
    this.valueFields = valueFields;
  }

  public MergeRowsMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  /**
   * @return Returns the flagField.
   */
  public String getFlagField() {
    return flagField;
  }

  /**
   * @param flagField
   *          The flagField to set.
   */
  public void setFlagField( String flagField ) {
    this.flagField = flagField;
  }

  public void allocate( int nrKeys, int nrValues ) {
    keyFields = new String[nrKeys];
    valueFields = new String[nrValues];
  }

  @Override
  public Object clone() {
    MergeRowsMeta retval = (MergeRowsMeta) super.clone();
    int nrKeys = keyFields.length;
    int nrValues = valueFields.length;
    retval.allocate( nrKeys, nrValues );
    System.arraycopy( keyFields, 0, retval.keyFields, 0, nrKeys );
    System.arraycopy( valueFields, 0, retval.valueFields, 0, nrValues );
    return retval;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    <keys>" + Const.CR );
    for ( int i = 0; i < keyFields.length; i++ ) {
      retval.append( "      " + XMLHandler.addTagValue( "key", keyFields[i] ) );
    }
    retval.append( "    </keys>" + Const.CR );

    retval.append( "    <values>" + Const.CR );
    for ( int i = 0; i < valueFields.length; i++ ) {
      retval.append( "      " + XMLHandler.addTagValue( "value", valueFields[i] ) );
    }
    retval.append( "    </values>" + Const.CR );

    retval.append( XMLHandler.addTagValue( "flag_field", flagField ) );

    List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
    retval.append( XMLHandler.addTagValue( "reference", infoStreams.get( 0 ).getStepname() ) );
    retval.append( XMLHandler.addTagValue( "compare", infoStreams.get( 1 ).getStepname() ) );
    retval.append( "    <compare>" + Const.CR );

    retval.append( "    </compare>" + Const.CR );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {

      Node keysnode = XMLHandler.getSubNode( stepnode, "keys" );
      Node valuesnode = XMLHandler.getSubNode( stepnode, "values" );

      int nrKeys = XMLHandler.countNodes( keysnode, "key" );
      int nrValues = XMLHandler.countNodes( valuesnode, "value" );

      allocate( nrKeys, nrValues );

      for ( int i = 0; i < nrKeys; i++ ) {
        Node keynode = XMLHandler.getSubNodeByNr( keysnode, "key", i );
        keyFields[i] = XMLHandler.getNodeValue( keynode );
      }

      for ( int i = 0; i < nrValues; i++ ) {
        Node valuenode = XMLHandler.getSubNodeByNr( valuesnode, "value", i );
        valueFields[i] = XMLHandler.getNodeValue( valuenode );
      }

      flagField = XMLHandler.getTagValue( stepnode, "flag_field" );

      List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
      StreamInterface referenceStream = infoStreams.get( 0 );
      StreamInterface compareStream = infoStreams.get( 1 );

      compareStream.setSubject( XMLHandler.getTagValue( stepnode, "compare" ) );
      referenceStream.setSubject( XMLHandler.getTagValue( stepnode, "reference" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "MergeRowsMeta.Exception.UnableToLoadStepInfo" ), e );
    }
  }

  @Override
  public void setDefault() {
    flagField = "flagfield";
    allocate( 0, 0 );
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      int nrKeys = rep.countNrStepAttributes( id_step, "key_field" );
      int nrValues = rep.countNrStepAttributes( id_step, "value_field" );

      allocate( nrKeys, nrValues );

      for ( int i = 0; i < nrKeys; i++ ) {
        keyFields[i] = rep.getStepAttributeString( id_step, i, "key_field" );
      }
      for ( int i = 0; i < nrValues; i++ ) {
        valueFields[i] = rep.getStepAttributeString( id_step, i, "value_field" );
      }

      flagField = rep.getStepAttributeString( id_step, "flag_field" );

      List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
      StreamInterface referenceStream = infoStreams.get( 0 );
      StreamInterface compareStream = infoStreams.get( 1 );

      referenceStream.setSubject( rep.getStepAttributeString( id_step, "reference" ) );
      compareStream.setSubject( rep.getStepAttributeString( id_step, "compare" ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "MergeRowsMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    for ( StreamInterface stream : getStepIOMeta().getInfoStreams() ) {
      stream.setStepMeta( StepMeta.findStep( steps, (String) stream.getSubject() ) );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      for ( int i = 0; i < keyFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "key_field", keyFields[i] );
      }

      for ( int i = 0; i < valueFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "value_field", valueFields[i] );
      }

      rep.saveStepAttribute( id_transformation, id_step, "flag_field", flagField );

      List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
      StreamInterface referenceStream = infoStreams.get( 0 );
      StreamInterface compareStream = infoStreams.get( 1 );

      rep.saveStepAttribute( id_transformation, id_step, "reference", referenceStream.getStepname() );
      rep.saveStepAttribute( id_transformation, id_step, "compare", compareStream.getStepname() );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MergeRowsMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public boolean chosesTargetSteps() {
    return false;
  }

  public String[] getTargetSteps() {
    return null;
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // We don't have any input fields here in "r" as they are all info fields.
    // So we just merge in the info fields.
    //
    if ( info != null ) {
      boolean found = false;
      for ( int i = 0; i < info.length && !found; i++ ) {
        if ( info[i] != null ) {
          r.mergeRowMeta( info[i], name );
          found = true;
        }
      }
    }

    if ( Utils.isEmpty( flagField ) ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "MergeRowsMeta.Exception.FlagFieldNotSpecified" ) );
    }
    ValueMetaInterface flagFieldValue = new ValueMetaString( flagField );
    flagFieldValue.setOrigin( name );
    r.addValueMeta( flagFieldValue );

  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
    StreamInterface referenceStream = infoStreams.get( 0 );
    StreamInterface compareStream = infoStreams.get( 1 );

    if ( referenceStream.getStepname() != null && compareStream.getStepname() != null ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MergeRowsMeta.CheckResult.SourceStepsOK" ), stepMeta );
      remarks.add( cr );
    } else if ( referenceStream.getStepname() == null && compareStream.getStepname() == null ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MergeRowsMeta.CheckResult.SourceStepsMissing" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MergeRowsMeta.CheckResult.OneSourceStepMissing" ), stepMeta );
      remarks.add( cr );
    }

    RowMetaInterface referenceRowMeta = null;
    RowMetaInterface compareRowMeta = null;
    try {
      referenceRowMeta = transMeta.getPrevStepFields( referenceStream.getStepname() );
      compareRowMeta = transMeta.getPrevStepFields( compareStream.getStepname() );
    } catch ( KettleStepException kse ) {
      new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
        PKG, "MergeRowsMeta.CheckResult.ErrorGettingPrevStepFields" ), stepMeta );
    }
    if ( referenceRowMeta != null && compareRowMeta != null ) {
      boolean rowsMatch = false;
      try {
        MergeRows.checkInputLayoutValid( referenceRowMeta, compareRowMeta );
        rowsMatch = true;
      } catch ( KettleRowException kre ) {
        rowsMatch = false;
      }
      if ( rowsMatch ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MergeRowsMeta.CheckResult.RowDefinitionMatch" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "MergeRowsMeta.CheckResult.RowDefinitionNotMatch" ), stepMeta );
        remarks.add( cr );
      }
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new MergeRows( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new MergeRowsData();
  }

  /**
   * Returns the Input/Output metadata for this step.
   */
  @Override
  public StepIOMetaInterface getStepIOMeta() {
    if ( ioMeta == null ) {

      ioMeta = new StepIOMeta( true, true, false, false, false, false );

      ioMeta.addStream( new Stream( StreamType.INFO, null, BaseMessages.getString(
        PKG, "MergeRowsMeta.InfoStream.FirstStream.Description" ), StreamIcon.INFO, null ) );
      ioMeta.addStream( new Stream( StreamType.INFO, null, BaseMessages.getString(
        PKG, "MergeRowsMeta.InfoStream.SecondStream.Description" ), StreamIcon.INFO, null ) );
    }

    return ioMeta;
  }

  @Override
  public void resetStepIoMeta() {
  }

  @Override
  public TransformationType[] getSupportedTransformationTypes() {
    return new TransformationType[] { TransformationType.Normal, };
  }

}
