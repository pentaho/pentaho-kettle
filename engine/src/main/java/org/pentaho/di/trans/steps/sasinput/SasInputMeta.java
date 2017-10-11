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

package org.pentaho.di.trans.steps.sasinput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
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
 * @since 06-OCT-2011
 * @author matt
 */

public class SasInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SasInputMeta.class; // for i18n purposes,

  public static final String XML_TAG_FIELD = "field";

  /** The field in which the filename is placed */
  private String acceptingField;

  private List<SasInputField> outputFields;

  public SasInputMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void setDefault() {
    outputFields = new ArrayList<SasInputField>();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      acceptingField = XMLHandler.getTagValue( stepnode, "accept_field" );
      int nrFields = XMLHandler.countNodes( stepnode, XML_TAG_FIELD );
      outputFields = new ArrayList<SasInputField>();
      for ( int i = 0; i < nrFields; i++ ) {
        Node fieldNode = XMLHandler.getSubNodeByNr( stepnode, XML_TAG_FIELD, i );
        outputFields.add( new SasInputField( fieldNode ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "SASInputMeta.Exception.UnableToReadStepInformationFromXML" ), e );
    }
  }

  public Object clone() {
    SasInputMeta retval = (SasInputMeta) super.clone();
    retval.setOutputFields( new ArrayList<SasInputField>() );
    for ( SasInputField field : outputFields ) {
      retval.getOutputFields().add( field.clone() );
    }
    return retval;
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    for ( SasInputField field : outputFields ) {
      try {
        ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta( field.getRename(), field.getType() );
        valueMeta.setLength( field.getLength(), field.getPrecision() );
        valueMeta.setDecimalSymbol( field.getDecimalSymbol() );
        valueMeta.setGroupingSymbol( field.getGroupingSymbol() );
        valueMeta.setConversionMask( field.getConversionMask() );
        valueMeta.setTrimType( field.getTrimType() );
        valueMeta.setOrigin( name );

        row.addValueMeta( valueMeta );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( "accept_field", acceptingField ) );
    for ( SasInputField field : outputFields ) {
      retval.append( XMLHandler.openTag( XML_TAG_FIELD ) );
      retval.append( field.getXML() );
      retval.append( XMLHandler.closeTag( XML_TAG_FIELD ) );
    }

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId stepId, List<DatabaseMeta> databases ) throws KettleException {
    try {
      acceptingField = rep.getStepAttributeString( stepId, "accept_field" );
      outputFields = new ArrayList<SasInputField>();
      int nrFields = rep.countNrStepAttributes( stepId, "field_name" );
      for ( int i = 0; i < nrFields; i++ ) {
        outputFields.add( new SasInputField( rep, stepId, i ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SASInputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "accept_field", acceptingField );
      for ( int i = 0; i < outputFields.size(); i++ ) {
        outputFields.get( i ).saveRep( rep, metaStore, id_transformation, id_step, i );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SASInputMeta.Exception.UnableToSaveMetaDataToRepository" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;

    if ( Utils.isEmpty( getAcceptingField() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SASInput.Log.Error.InvalidAcceptingFieldName" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new SasInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new SasInputData();
  }

  /**
   * @return Returns the acceptingField.
   */
  public String getAcceptingField() {
    return acceptingField;
  }

  /**
   * @param acceptingField
   *          The acceptingField to set.
   */
  public void setAcceptingField( String acceptingField ) {
    this.acceptingField = acceptingField;
  }

  /**
   * @return the outputFields
   */
  public List<SasInputField> getOutputFields() {
    return outputFields;
  }

  /**
   * @param outputFields
   *          the outputFields to set
   */
  public void setOutputFields( List<SasInputField> outputFields ) {
    this.outputFields = outputFields;
  }

}
