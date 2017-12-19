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

package org.pentaho.di.trans.steps.valuemapper;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
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
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Maps String values of a certain field to new values
 *
 * Created on 03-apr-2006
 */
@InjectionSupported( localizationPrefix = "ValueMapper.Injection.", groups = { "VALUES" } )
public class ValueMapperMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ValueMapperMeta.class; // for i18n purposes, needed by Translator2!!

  @Injection( name = "FIELDNAME" )
  private String fieldToUse;
  @Injection( name = "TARGET_FIELDNAME" )
  private String targetField;
  @Injection( name = "NON_MATCH_DEFAULT" )
  private String nonMatchDefault;

  @Injection( name = "SOURCE", group = "VALUES" )
  private String[] sourceValue;
  @Injection( name = "TARGET", group = "VALUES" )
  private String[] targetValue;

  public ValueMapperMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldName.
   */
  public String[] getSourceValue() {
    return sourceValue;
  }

  /**
   * @param fieldName
   *          The fieldName to set.
   */
  public void setSourceValue( String[] fieldName ) {
    this.sourceValue = fieldName;
  }

  /**
   * @return Returns the fieldValue.
   */
  public String[] getTargetValue() {
    return targetValue;
  }

  /**
   * @param fieldValue
   *          The fieldValue to set.
   */
  public void setTargetValue( String[] fieldValue ) {
    this.targetValue = fieldValue;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int count ) {
    sourceValue = new String[count];
    targetValue = new String[count];
  }

  @Override
  public Object clone() {
    ValueMapperMeta retval = (ValueMapperMeta) super.clone();

    int count = sourceValue.length;

    retval.allocate( count );

    System.arraycopy( sourceValue, 0, retval.sourceValue, 0, count );
    System.arraycopy( targetValue, 0, retval.targetValue, 0, count );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      fieldToUse = XMLHandler.getTagValue( stepnode, "field_to_use" );
      targetField = XMLHandler.getTagValue( stepnode, "target_field" );
      nonMatchDefault = XMLHandler.getTagValue( stepnode, "non_match_default" );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int count = XMLHandler.countNodes( fields, "field" );

      allocate( count );

      for ( int i = 0; i < count; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        sourceValue[i] = XMLHandler.getTagValue( fnode, "source_value" );
        targetValue[i] = XMLHandler.getTagValue( fnode, "target_value" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "ValueMapperMeta.RuntimeError.UnableToReadXML.VALUEMAPPER0004" ), e );
    }
  }

  @Override
  public void setDefault() {
    int count = 0;

    allocate( count );

    for ( int i = 0; i < count; i++ ) {
      sourceValue[i] = "field" + i;
      targetValue[i] = "";
    }
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) {
    ValueMetaInterface extra = null;
    if ( !Utils.isEmpty( getTargetField() ) ) {
      extra = new ValueMetaString( getTargetField() );

      // Lengths etc?
      // Take the max length of all the strings...
      //
      int maxlen = -1;
      for ( int i = 0; i < targetValue.length; i++ ) {
        if ( targetValue[i] != null && targetValue[i].length() > maxlen ) {
          maxlen = targetValue[i].length();
        }
      }

      // include default value in max length calculation
      //
      if ( nonMatchDefault != null && nonMatchDefault.length() > maxlen ) {
        maxlen = nonMatchDefault.length();
      }
      extra.setLength( maxlen );
      extra.setOrigin( name );
      r.addValueMeta( extra );
    } else {
      if ( !Utils.isEmpty( getFieldToUse() ) ) {
        extra = r.searchValueMeta( getFieldToUse() );
      }
    }

    if ( extra != null ) {
      // The output of a changed field or new field is always a normal storage type...
      //
      extra.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " ).append( XMLHandler.addTagValue( "field_to_use", fieldToUse ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "target_field", targetField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "non_match_default", nonMatchDefault ) );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < sourceValue.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "source_value", sourceValue[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "target_value", targetValue[i] ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      fieldToUse = rep.getStepAttributeString( id_step, "field_to_use" );
      targetField = rep.getStepAttributeString( id_step, "target_field" );
      nonMatchDefault = rep.getStepAttributeString( id_step, "non_match_default" );

      int nrfields = rep.countNrStepAttributes( id_step, "source_value" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        sourceValue[i] = rep.getStepAttributeString( id_step, i, "source_value" );
        targetValue[i] = rep.getStepAttributeString( id_step, i, "target_value" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ValueMapperMeta.RuntimeError.UnableToReadRepository.VALUEMAPPER0005" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "field_to_use", fieldToUse );
      rep.saveStepAttribute( id_transformation, id_step, "target_field", targetField );
      rep.saveStepAttribute( id_transformation, id_step, "non_match_default", nonMatchDefault );

      for ( int i = 0; i < sourceValue.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "source_value", getNullOrEmpty( sourceValue[i] ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_value", getNullOrEmpty( targetValue[i] ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ValueMapperMeta.RuntimeError.UnableToSaveRepository.VALUEMAPPER0006", "" + id_step ), e );
    }

  }

  private String getNullOrEmpty( String str ) {
    return str == null ? StringUtils.EMPTY : str;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "ValueMapperMeta.CheckResult.NotReceivingFieldsFromPreviousSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ValueMapperMeta.CheckResult.ReceivingFieldsFromPreviousSteps", "" + prev.size() ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ValueMapperMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ValueMapperMeta.CheckResult.NotReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ValueMapper( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new ValueMapperData();
  }

  /**
   * @return Returns the fieldToUse.
   */
  public String getFieldToUse() {
    return fieldToUse;
  }

  /**
   * @param fieldToUse
   *          The fieldToUse to set.
   */
  public void setFieldToUse( String fieldToUse ) {
    this.fieldToUse = fieldToUse;
  }

  /**
   * @return Returns the targetField.
   */
  public String getTargetField() {
    return targetField;
  }

  /**
   * @param targetField
   *          The targetField to set.
   */
  public void setTargetField( String targetField ) {
    this.targetField = targetField;
  }

  /**
   * @return the non match default. This is the string that will be used to fill in the data when no match is found.
   */
  public String getNonMatchDefault() {
    return nonMatchDefault;
  }

  /**
   * @param nonMatchDefault
   *          the non match default. This is the string that will be used to fill in the data when no match is found.
   */
  public void setNonMatchDefault( String nonMatchDefault ) {
    this.nonMatchDefault = nonMatchDefault;
  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    int nrFields = ( sourceValue == null ) ? -1 : sourceValue.length;
    if ( nrFields <= 0 ) {
      return;
    }
    String[][] rtn = Utils.normalizeArrays( nrFields, targetValue );
    targetValue = rtn[ 0 ];
  }

}
