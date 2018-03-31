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

package org.pentaho.di.trans.steps.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
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

/**
 * Contains the meta-data for the Validator step: calculates predefined formula's
 *
 * Created on 08-sep-2005
 */
@InjectionSupported( localizationPrefix = "Validator.Injection.", groups = { "VALIDATIONS" } )
public class ValidatorMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ValidatorMeta.class; // for i18n purposes, needed by Translator2!!

  /** The calculations to be performed */
  @InjectionDeep
  private List<Validation> validations;

  /** Checkbox to have all rules validated, with all the errors in the output */
  @Injection( name = "VALIDATE_ALL" )
  private boolean validatingAll;

  /**
   * If enabled, it concatenates all encountered errors with the selected separator
   */
  @Injection( name = "CONCATENATE_ERRORS" )
  private boolean concatenatingErrors;

  /**
   * The concatenation separator
   */
  @Injection( name = "CONCATENATION_SEPARATOR" )
  private String concatenationSeparator;

  public void allocate( int nrValidations ) {
    validations = new ArrayList<Validation>( nrValidations );
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    int nrCalcs = XMLHandler.countNodes( stepnode, Validation.XML_TAG );
    allocate( nrCalcs );
    validatingAll = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "validate_all" ) );
    concatenatingErrors = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "concat_errors" ) );
    concatenationSeparator = XMLHandler.getTagValue( stepnode, "concat_separator" );

    for ( int i = 0; i < nrCalcs; i++ ) {
      Node calcnode = XMLHandler.getSubNodeByNr( stepnode, Validation.XML_TAG, i );
      validations.add( new Validation( calcnode ) );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( XMLHandler.addTagValue( "validate_all", validatingAll ) );
    retval.append( XMLHandler.addTagValue( "concat_errors", concatenatingErrors ) );
    retval.append( XMLHandler.addTagValue( "concat_separator", concatenationSeparator ) );

    for ( int i = 0; i < validations.size(); i++ ) {
      retval.append( "       " ).append( validations.get( i ).getXML() ).append( Const.CR );
    }

    return retval.toString();
  }

  public boolean equals( Object obj ) {
    if ( obj != null && ( obj.getClass().equals( this.getClass() ) ) ) {
      ValidatorMeta m = (ValidatorMeta) obj;
      return ( getXML() == m.getXML() );
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash( validatingAll, concatenatingErrors, concatenationSeparator, validations );
  }

  public Object clone() {
    ValidatorMeta retval = (ValidatorMeta) super.clone();
    if ( validations != null ) {
      int valSize = validations.size();
      retval.allocate( valSize );
      for ( int i = 0; i < valSize; i++ ) {
        retval.validations.add( validations.get( i ).clone() );
      }
    } else {
      retval.allocate( 0 );
    }
    return retval;
  }

  public void setDefault() {
    validations = new ArrayList<Validation>();
    concatenationSeparator = "|";
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    int nrValidationFields = rep.countNrStepAttributes( id_step, "validator_field_name" );
    allocate( nrValidationFields );
    validatingAll = rep.getStepAttributeBoolean( id_step, "validate_all" );
    concatenatingErrors = rep.getStepAttributeBoolean( id_step, "concat_errors" );
    concatenationSeparator = rep.getStepAttributeString( id_step, "concat_separator" );

    for ( int i = 0; i < nrValidationFields; i++ ) {
      validations.add( new Validation( rep, id_step, i ) );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, "validate_all", validatingAll );
    rep.saveStepAttribute( id_transformation, id_step, "concat_errors", concatenatingErrors );
    rep.saveStepAttribute( id_transformation, id_step, "concat_separator", concatenationSeparator );

    for ( int i = 0; i < validations.size(); i++ ) {
      validations.get( i ).saveRep( rep, metaStore, id_transformation, id_step, i );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "ValidatorMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ValidatorMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ValidatorMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ValidatorMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new Validator( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new ValidatorData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * @return the validations
   */
  public List<Validation> getValidations() {
    return validations;
  }

  /**
   * @param validations
   *          the validations to set
   */
  public void setValidations( List<Validation> validations ) {
    this.validations = validations;
  }

  public boolean excludeFromRowLayoutVerification() {
    return true;
  }

  /**
   * @return the validatingAll
   */
  public boolean isValidatingAll() {
    return validatingAll;
  }

  /**
   * @param validatingAll
   *          the validatingAll to set
   */
  public void setValidatingAll( boolean validatingAll ) {
    this.validatingAll = validatingAll;
  }

  /**
   * @return the concatenatingErrors
   */
  public boolean isConcatenatingErrors() {
    return concatenatingErrors;
  }

  /**
   * @param concatenatingErrors
   *          the concatenatingErrors to set
   */
  public void setConcatenatingErrors( boolean concatenatingErrors ) {
    this.concatenatingErrors = concatenatingErrors;
  }

  /**
   * @return the concatenationSeparator
   */
  public String getConcatenationSeparator() {
    return concatenationSeparator;
  }

  /**
   * @param concatenationSeparator
   *          the concatenationSeparator to set
   */
  public void setConcatenationSeparator( String concatenationSeparator ) {
    this.concatenationSeparator = concatenationSeparator;
  }

  /**
   * Returns the Input/Output metadata for this step.
   */
  public StepIOMetaInterface getStepIOMeta() {
    if ( ioMeta == null ) {

      ioMeta = new StepIOMeta( true, true, false, false, true, false );

      // Add the info sources...
      //
      for ( Validation validation : validations ) {
        StreamInterface stream =
          new Stream( StreamType.INFO, validation.getSourcingStep(), BaseMessages
            .getString( PKG, "ValidatorMeta.InfoStream.ValidationInput.Description", Const.NVL( validation
              .getName(), "" ) ), StreamIcon.INFO, validation );
        ioMeta.addStream( stream );
      }
    }

    return ioMeta;
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    for ( StreamInterface stream : getStepIOMeta().getInfoStreams() ) {
      Validation validation = (Validation) stream.getSubject();
      StepMeta stepMeta = StepMeta.findStep( steps, validation.getSourcingStepName() );
      validation.setSourcingStep( stepMeta );
    }
    resetStepIoMeta();
  }

  private static StreamInterface newValidation = new Stream( StreamType.INFO, null, BaseMessages.getString(
    PKG, "ValidatorMeta.NewValidation.Description" ), StreamIcon.INFO, null );

  public List<StreamInterface> getOptionalStreams() {
    List<StreamInterface> list = new ArrayList<StreamInterface>();

    list.add( newValidation );

    return list;
  }

  public void handleStreamSelection( StreamInterface stream ) {
    // A hack to prevent us from losing information in the Trans UI because
    // of the resetStepIoMeta() call at the end of this method.
    //
    List<StreamInterface> streams = getStepIOMeta().getInfoStreams();
    for ( int i = 0; i < validations.size(); i++ ) {
      validations.get( i ).setSourcingStep( streams.get( i ).getStepMeta() );
    }

    if ( stream == newValidation ) {

      // Add the info..
      //
      Validation validation = new Validation();
      validation.setName( stream.getStepname() );
      validation.setSourcingStep( stream.getStepMeta() );
      validation.setSourcingValues( true );
      validations.add( validation );
    }

    resetStepIoMeta(); // force stepIo to be recreated when it is next needed.
  }
}
