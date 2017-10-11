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

package org.pentaho.di.trans.steps.denormaliser;

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
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * The Denormaliser transformation step meta-data
 *
 * @since 17-jan-2006
 * @author Matt
 */

public class DenormaliserMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = DenormaliserMeta.class; // for i18n purposes, needed by Translator2!!

  /** Fields to group over */
  private String[] groupField;

  /** The key field */
  private String keyField;

  /** The fields to unpivot */
  private DenormaliserTargetField[] denormaliserTargetField;

  public DenormaliserMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the keyField.
   */
  public String getKeyField() {
    return keyField;
  }

  /**
   * @param keyField
   *          The keyField to set.
   */
  public void setKeyField( String keyField ) {
    this.keyField = keyField;
  }

  /**
   * @return Returns the groupField.
   */
  public String[] getGroupField() {
    return groupField;
  }

  /**
   * @param groupField
   *          The groupField to set.
   */
  public void setGroupField( String[] groupField ) {
    this.groupField = groupField;
  }

  public String[] getDenormaliserTargetFields() {
    String[] fields = new String[denormaliserTargetField.length];
    for ( int i = 0; i < fields.length; i++ ) {
      fields[i] = denormaliserTargetField[i].getTargetName();
    }

    return fields;
  }

  public DenormaliserTargetField searchTargetField( String targetName ) {
    for ( int i = 0; i < denormaliserTargetField.length; i++ ) {
      DenormaliserTargetField field = denormaliserTargetField[i];
      if ( field.getTargetName().equalsIgnoreCase( targetName ) ) {
        return field;
      }
    }
    return null;
  }

  /**
   * @return Returns the pivotField.
   */
  public DenormaliserTargetField[] getDenormaliserTargetField() {
    return denormaliserTargetField;
  }

  /**
   * @param pivotField
   *          The pivotField to set.
   */
  public void setDenormaliserTargetField( DenormaliserTargetField[] pivotField ) {
    this.denormaliserTargetField = pivotField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int sizegroup, int nrfields ) {
    groupField = new String[sizegroup];
    denormaliserTargetField = new DenormaliserTargetField[nrfields];
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public void setDefault() {
    int sizegroup = 0;
    int nrfields = 0;

    allocate( sizegroup, nrfields );
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    // Remove the key value (there will be different entries for each output row)
    //
    if ( keyField != null && keyField.length() > 0 ) {
      int idx = row.indexOfValue( keyField );
      if ( idx < 0 ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "DenormaliserMeta.Exception.UnableToLocateKeyField", keyField ) );
      }
      row.removeValueMeta( idx );
    } else {
      throw new KettleStepException( BaseMessages.getString( PKG, "DenormaliserMeta.Exception.RequiredKeyField" ) );
    }

    // Remove all field value(s) (there will be different entries for each output row)
    //
    for ( int i = 0; i < denormaliserTargetField.length; i++ ) {
      String fieldname = denormaliserTargetField[i].getFieldName();
      if ( fieldname != null && fieldname.length() > 0 ) {
        int idx = row.indexOfValue( fieldname );
        if ( idx >= 0 ) {
          row.removeValueMeta( idx );
        }
      } else {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "DenormaliserMeta.Exception.RequiredTargetFieldName", ( i + 1 ) + "" ) );
      }
    }

    // Re-add the target fields
    for ( int i = 0; i < denormaliserTargetField.length; i++ ) {
      DenormaliserTargetField field = denormaliserTargetField[i];
      try {
        ValueMetaInterface target =
          ValueMetaFactory.createValueMeta( field.getTargetName(), field.getTargetType() );
        target.setLength( field.getTargetLength(), field.getTargetPrecision() );
        target.setOrigin( name );
        row.addValueMeta( target );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      keyField = XMLHandler.getTagValue( stepnode, "key_field" );

      Node groupn = XMLHandler.getSubNode( stepnode, "group" );
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );

      int sizegroup = XMLHandler.countNodes( groupn, "field" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( sizegroup, nrfields );

      for ( int i = 0; i < sizegroup; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( groupn, "field", i );
        groupField[i] = XMLHandler.getTagValue( fnode, "name" );
      }

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        denormaliserTargetField[i] = new DenormaliserTargetField();
        denormaliserTargetField[i].setFieldName( XMLHandler.getTagValue( fnode, "field_name" ) );
        denormaliserTargetField[i].setKeyValue( XMLHandler.getTagValue( fnode, "key_value" ) );
        denormaliserTargetField[i].setTargetName( XMLHandler.getTagValue( fnode, "target_name" ) );
        denormaliserTargetField[i].setTargetType( XMLHandler.getTagValue( fnode, "target_type" ) );
        denormaliserTargetField[i].setTargetFormat( XMLHandler.getTagValue( fnode, "target_format" ) );
        denormaliserTargetField[i].setTargetLength( Const.toInt(
          XMLHandler.getTagValue( fnode, "target_length" ), -1 ) );
        denormaliserTargetField[i].setTargetPrecision( Const.toInt( XMLHandler.getTagValue(
          fnode, "target_precision" ), -1 ) );
        denormaliserTargetField[i]
          .setTargetDecimalSymbol( XMLHandler.getTagValue( fnode, "target_decimal_symbol" ) );
        denormaliserTargetField[i].setTargetGroupingSymbol( XMLHandler.getTagValue(
          fnode, "target_grouping_symbol" ) );
        denormaliserTargetField[i].setTargetCurrencySymbol( XMLHandler.getTagValue(
          fnode, "target_currency_symbol" ) );
        denormaliserTargetField[i].setTargetNullString( XMLHandler.getTagValue( fnode, "target_null_string" ) );
        denormaliserTargetField[i].setTargetAggregationType( XMLHandler.getTagValue(
          fnode, "target_aggregation_type" ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "DenormaliserMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "      " + XMLHandler.addTagValue( "key_field", keyField ) );

    retval.append( "      <group>" + Const.CR );
    for ( int i = 0; i < groupField.length; i++ ) {
      retval.append( "        <field>" + Const.CR );
      retval.append( "          " + XMLHandler.addTagValue( "name", groupField[i] ) );
      retval.append( "          </field>" + Const.CR );
    }
    retval.append( "        </group>" + Const.CR );

    retval.append( "      <fields>" + Const.CR );
    for ( int i = 0; i < denormaliserTargetField.length; i++ ) {
      DenormaliserTargetField field = denormaliserTargetField[i];

      retval.append( "        <field>" + Const.CR );
      retval.append( "          " + XMLHandler.addTagValue( "field_name", field.getFieldName() ) );
      retval.append( "          " + XMLHandler.addTagValue( "key_value", field.getKeyValue() ) );
      retval.append( "          " + XMLHandler.addTagValue( "target_name", field.getTargetName() ) );
      retval.append( "          " + XMLHandler.addTagValue( "target_type", field.getTargetTypeDesc() ) );
      retval.append( "          " + XMLHandler.addTagValue( "target_format", field.getTargetFormat() ) );
      retval.append( "          " + XMLHandler.addTagValue( "target_length", field.getTargetLength() ) );
      retval.append( "          " + XMLHandler.addTagValue( "target_precision", field.getTargetPrecision() ) );
      retval.append( "          "
        + XMLHandler.addTagValue( "target_decimal_symbol", field.getTargetDecimalSymbol() ) );
      retval.append( "          "
        + XMLHandler.addTagValue( "target_grouping_symbol", field.getTargetGroupingSymbol() ) );
      retval.append( "          "
        + XMLHandler.addTagValue( "target_currency_symbol", field.getTargetCurrencySymbol() ) );
      retval.append( "          " + XMLHandler.addTagValue( "target_null_string", field.getTargetNullString() ) );
      retval.append( "          "
        + XMLHandler.addTagValue( "target_aggregation_type", field.getTargetAggregationTypeDesc() ) );
      retval.append( "          </field>" + Const.CR );
    }
    retval.append( "        </fields>" + Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      keyField = rep.getStepAttributeString( id_step, "key_field" );

      int groupsize = rep.countNrStepAttributes( id_step, "group_name" );
      int nrvalues = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( groupsize, nrvalues );

      for ( int i = 0; i < groupsize; i++ ) {
        groupField[i] = rep.getStepAttributeString( id_step, i, "group_name" );
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        denormaliserTargetField[i] = new DenormaliserTargetField();
        denormaliserTargetField[i].setFieldName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        denormaliserTargetField[i].setKeyValue( rep.getStepAttributeString( id_step, i, "key_value" ) );
        denormaliserTargetField[i].setTargetName( rep.getStepAttributeString( id_step, i, "target_name" ) );
        denormaliserTargetField[i].setTargetType( rep.getStepAttributeString( id_step, i, "target_type" ) );
        denormaliserTargetField[i].setTargetFormat( rep.getStepAttributeString( id_step, i, "target_format" ) );
        denormaliserTargetField[i].setTargetLength( (int) rep
          .getStepAttributeInteger( id_step, i, "target_length" ) );
        denormaliserTargetField[i].setTargetPrecision( (int) rep.getStepAttributeInteger(
          id_step, i, "target_precision" ) );
        denormaliserTargetField[i].setTargetDecimalSymbol( rep.getStepAttributeString(
          id_step, i, "target_decimal_symbol" ) );
        denormaliserTargetField[i].setTargetGroupingSymbol( rep.getStepAttributeString(
          id_step, i, "target_grouping_symbol" ) );
        denormaliserTargetField[i].setTargetCurrencySymbol( rep.getStepAttributeString(
          id_step, i, "target_currency_symbol" ) );
        denormaliserTargetField[i].setTargetNullString( rep.getStepAttributeString(
          id_step, i, "target_null_string" ) );
        denormaliserTargetField[i].setTargetAggregationType( rep.getStepAttributeString(
          id_step, i, "target_aggregation_type" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "key_field", keyField );

      for ( int i = 0; i < groupField.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "group_name", groupField[i] );
      }

      for ( int i = 0; i < denormaliserTargetField.length; i++ ) {
        DenormaliserTargetField field = denormaliserTargetField[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getFieldName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "key_value", field.getKeyValue() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_name", field.getTargetName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_type", field.getTargetTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_format", field.getTargetFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_length", field.getTargetLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_precision", field.getTargetPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_decimal_symbol", field
          .getTargetDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_grouping_symbol", field
          .getTargetGroupingSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_currency_symbol", field
          .getTargetCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_null_string", field.getTargetNullString() );
        rep.saveStepAttribute( id_transformation, id_step, i, "target_aggregation_type", field
          .getTargetAggregationTypeDesc() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "DenormaliserMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "DenormaliserMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "DenormaliserMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new Denormaliser( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new DenormaliserData();
  }

  @Override
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return new DenormaliserMetaInjection( this );
  }
}
