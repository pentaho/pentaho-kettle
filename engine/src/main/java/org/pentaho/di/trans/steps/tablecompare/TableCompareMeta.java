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

package org.pentaho.di.trans.steps.tablecompare;

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
import org.pentaho.di.core.row.value.ValueMetaInteger;
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

//@Step(
//    id = "TableCompare",
//    image = "be/kjube/plugins/images/table_compare.png",
//    description ="kJube.Plugins.TableCompare.Description",
//    name = "kJube.Plugins.TableCompare.Name",
//    categoryDescription="kJube.Category.Name",
//    i18nPackageName="be.kjube.plugins"
//  )
public class TableCompareMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = TableCompare.class; // for i18n purposes, needed by Translator2!!

  private DatabaseMeta referenceConnection;
  private String referenceSchemaField;
  private String referenceTableField;

  private DatabaseMeta compareConnection;
  private String compareSchemaField;
  private String compareTableField;

  private String keyFieldsField;
  private String excludeFieldsField;

  private String nrErrorsField;
  private String nrRecordsReferenceField;
  private String nrRecordsCompareField;
  private String nrErrorsLeftJoinField;
  private String nrErrorsInnerJoinField;
  private String nrErrorsRightJoinField;

  private String keyDescriptionField;
  private String valueReferenceField;
  private String valueCompareField;

  public TableCompareMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return the referenceSchemaField
   */
  public String getReferenceSchemaField() {
    return referenceSchemaField;
  }

  /**
   * @param referenceSchemaField
   *          the referenceSchemaField to set
   */
  public void setReferenceSchemaField( String referenceSchemaField ) {
    this.referenceSchemaField = referenceSchemaField;
  }

  /**
   * @return the referenceTableField
   */
  public String getReferenceTableField() {
    return referenceTableField;
  }

  /**
   * @param referenceTableField
   *          the referenceTableField to set
   */
  public void setReferenceTableField( String referenceTableField ) {
    this.referenceTableField = referenceTableField;
  }

  /**
   * @return the compareSchemaField
   */
  public String getCompareSchemaField() {
    return compareSchemaField;
  }

  /**
   * @param compareSchemaField
   *          the compareSchemaField to set
   */
  public void setCompareSchemaField( String compareSchemaField ) {
    this.compareSchemaField = compareSchemaField;
  }

  /**
   * @return the compareTableField
   */
  public String getCompareTableField() {
    return compareTableField;
  }

  /**
   * @param compareTableField
   *          the compareTableField to set
   */
  public void setCompareTableField( String compareTableField ) {
    this.compareTableField = compareTableField;
  }

  /**
   * @return the nrErrorsField
   */
  public String getNrErrorsField() {
    return nrErrorsField;
  }

  /**
   * @param nrErrorsField
   *          the nrErrorsField to set
   */
  public void setNrErrorsField( String nrErrorsField ) {
    this.nrErrorsField = nrErrorsField;
  }

  /**
   * @return the referenceConnection
   */
  public DatabaseMeta getReferenceConnection() {
    return referenceConnection;
  }

  /**
   * @param referenceConnection
   *          the referenceConnection to set
   */
  public void setReferenceConnection( DatabaseMeta referenceConnection ) {
    this.referenceConnection = referenceConnection;
  }

  /**
   * @return the compareConnection
   */
  public DatabaseMeta getCompareConnection() {
    return compareConnection;
  }

  /**
   * @param compareConnection
   *          the compareConnection to set
   */
  public void setCompareConnection( DatabaseMeta compareConnection ) {
    this.compareConnection = compareConnection;
  }

  @Override
  public DatabaseMeta[] getUsedDatabaseConnections() {
    List<DatabaseMeta> connList = new ArrayList<DatabaseMeta>( 2 );
    if ( compareConnection != null ) {
      connList.add( compareConnection );
    }
    if ( referenceConnection != null ) {
      connList.add( referenceConnection );
    }
    if ( connList.size() > 0 ) {
      DatabaseMeta[] rtn = new DatabaseMeta[ connList.size() ];
      connList.toArray( rtn );
      return rtn;
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return the keyFieldsField
   */
  public String getKeyFieldsField() {
    return keyFieldsField;
  }

  /**
   * @param keyFieldsField
   *          the keyFieldsField to set
   */
  public void setKeyFieldsField( String keyFieldsField ) {
    this.keyFieldsField = keyFieldsField;
  }

  /**
   * @return the excludeFieldsField
   */
  public String getExcludeFieldsField() {
    return excludeFieldsField;
  }

  /**
   * @param excludeFieldsField
   *          the excludeFieldsField to set
   */
  public void setExcludeFieldsField( String excludeFieldsField ) {
    this.excludeFieldsField = excludeFieldsField;
  }

  /**
   * @return the nrRecordsReferenceField
   */
  public String getNrRecordsReferenceField() {
    return nrRecordsReferenceField;
  }

  /**
   * @param nrRecordsReferenceField
   *          the nrRecordsReferenceField to set
   */
  public void setNrRecordsReferenceField( String nrRecordsReferenceField ) {
    this.nrRecordsReferenceField = nrRecordsReferenceField;
  }

  /**
   * @return the nrRecordsCompareField
   */
  public String getNrRecordsCompareField() {
    return nrRecordsCompareField;
  }

  /**
   * @param nrRecordsCompareField
   *          the nrRecordsCompareField to set
   */
  public void setNrRecordsCompareField( String nrRecordsCompareField ) {
    this.nrRecordsCompareField = nrRecordsCompareField;
  }

  /**
   * @return the nrErrorsLeftJoinField
   */
  public String getNrErrorsLeftJoinField() {
    return nrErrorsLeftJoinField;
  }

  /**
   * @param nrErrorsLeftJoinField
   *          the nrErrorsLeftJoinField to set
   */
  public void setNrErrorsLeftJoinField( String nrErrorsLeftJoinField ) {
    this.nrErrorsLeftJoinField = nrErrorsLeftJoinField;
  }

  /**
   * @return the nrErrorsInnerJoinField
   */
  public String getNrErrorsInnerJoinField() {
    return nrErrorsInnerJoinField;
  }

  /**
   * @param nrErrorsInnerJoinField
   *          the nrErrorsInnerJoinField to set
   */
  public void setNrErrorsInnerJoinField( String nrErrorsInnerJoinField ) {
    this.nrErrorsInnerJoinField = nrErrorsInnerJoinField;
  }

  /**
   * @return the nrErrorsRightJoinField
   */
  public String getNrErrorsRightJoinField() {
    return nrErrorsRightJoinField;
  }

  /**
   * @param nrErrorsRightJoinField
   *          the nrErrorsRightJoinField to set
   */
  public void setNrErrorsRightJoinField( String nrErrorsRightJoinField ) {
    this.nrErrorsRightJoinField = nrErrorsRightJoinField;
  }

  /**
   * @return the keyDescriptionField
   */
  public String getKeyDescriptionField() {
    return keyDescriptionField;
  }

  /**
   * @param keyDescriptionField
   *          the keyDescriptionField to set
   */
  public void setKeyDescriptionField( String keyDescriptionField ) {
    this.keyDescriptionField = keyDescriptionField;
  }

  /**
   * @return the valueReferenceField
   */
  public String getValueReferenceField() {
    return valueReferenceField;
  }

  /**
   * @param valueReferenceField
   *          the valueReferenceField to set
   */
  public void setValueReferenceField( String valueReferenceField ) {
    this.valueReferenceField = valueReferenceField;
  }

  /**
   * @return the valueCompareField
   */
  public String getValueCompareField() {
    return valueCompareField;
  }

  /**
   * @param valueCompareField
   *          the valueCompareField to set
   */
  public void setValueCompareField( String valueCompareField ) {
    this.valueCompareField = valueCompareField;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  @Override
  public Object clone() {
    TableCompareMeta retval = (TableCompareMeta) super.clone();

    return retval;
  }

  @Override
  public void getFields( RowMetaInterface inputRowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    if ( Utils.isEmpty( nrErrorsField ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "TableCompareMeta.Exception.NrErrorsFieldIsNotSpecified" ) );
    }
    if ( Utils.isEmpty( nrRecordsReferenceField ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "TableCompareMeta.Exception.NrRecordsReferenceFieldNotSpecified" ) );
    }
    if ( Utils.isEmpty( nrRecordsCompareField ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "TableCompareMeta.Exception.NrRecordsCompareFieldNotSpecified" ) );
    }
    if ( Utils.isEmpty( nrErrorsLeftJoinField ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "TableCompareMeta.Exception.NrErrorsLeftJoinFieldNotSpecified" ) );
    }
    if ( Utils.isEmpty( nrErrorsInnerJoinField ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "TableCompareMeta.Exception.NrErrorsInnerJoinFieldNotSpecified" ) );
    }
    if ( Utils.isEmpty( nrErrorsRightJoinField ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "TableCompareMeta.Exception.NrErrorsRightJoinFieldNotSpecified" ) );
    }

    ValueMetaInterface nrErrorsValueMeta = new ValueMetaInteger( nrErrorsField );
    nrErrorsValueMeta.setLength( 9 );
    nrErrorsValueMeta.setOrigin( origin );
    inputRowMeta.addValueMeta( nrErrorsValueMeta );

    ValueMetaInterface nrRecordsReference =
      new ValueMetaInteger( nrRecordsReferenceField );
    nrRecordsReference.setLength( 9 );
    nrRecordsReference.setOrigin( origin );
    inputRowMeta.addValueMeta( nrRecordsReference );

    ValueMetaInterface nrRecordsCompare = new ValueMetaInteger( nrRecordsCompareField );
    nrRecordsCompare.setLength( 9 );
    nrRecordsCompare.setOrigin( origin );
    inputRowMeta.addValueMeta( nrRecordsCompare );

    ValueMetaInterface nrErrorsLeft = new ValueMetaInteger( nrErrorsLeftJoinField );
    nrErrorsLeft.setLength( 9 );
    nrErrorsLeft.setOrigin( origin );
    inputRowMeta.addValueMeta( nrErrorsLeft );

    ValueMetaInterface nrErrorsInner = new ValueMetaInteger( nrErrorsInnerJoinField );
    nrErrorsInner.setLength( 9 );
    nrErrorsInner.setOrigin( origin );
    inputRowMeta.addValueMeta( nrErrorsInner );

    ValueMetaInterface nrErrorsRight = new ValueMetaInteger( nrErrorsRightJoinField );
    nrErrorsRight.setLength( 9 );
    nrErrorsRight.setOrigin( origin );
    inputRowMeta.addValueMeta( nrErrorsRight );
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      referenceConnection =
        DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( stepnode, "reference_connection" ) );
      referenceSchemaField = XMLHandler.getTagValue( stepnode, "reference_schema_field" );
      referenceTableField = XMLHandler.getTagValue( stepnode, "reference_table_field" );

      compareConnection =
        DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( stepnode, "compare_connection" ) );
      compareSchemaField = XMLHandler.getTagValue( stepnode, "compare_schema_field" );
      compareTableField = XMLHandler.getTagValue( stepnode, "compare_table_field" );

      keyFieldsField = XMLHandler.getTagValue( stepnode, "key_fields_field" );
      excludeFieldsField = XMLHandler.getTagValue( stepnode, "exclude_fields_field" );
      nrErrorsField = XMLHandler.getTagValue( stepnode, "nr_errors_field" );

      nrRecordsReferenceField = XMLHandler.getTagValue( stepnode, "nr_records_reference_field" );
      nrRecordsCompareField = XMLHandler.getTagValue( stepnode, "nr_records_compare_field" );
      nrErrorsLeftJoinField = XMLHandler.getTagValue( stepnode, "nr_errors_left_join_field" );
      nrErrorsInnerJoinField = XMLHandler.getTagValue( stepnode, "nr_errors_inner_join_field" );
      nrErrorsRightJoinField = XMLHandler.getTagValue( stepnode, "nr_errors_right_join_field" );

      keyDescriptionField = XMLHandler.getTagValue( stepnode, "key_description_field" );
      valueReferenceField = XMLHandler.getTagValue( stepnode, "value_reference_field" );
      valueCompareField = XMLHandler.getTagValue( stepnode, "value_compare_field" );

    } catch ( Exception e ) {
      throw new KettleXMLException( "It was not possibke to load the Trim metadata from XML", e );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "      " ).append(
      XMLHandler.addTagValue( "reference_connection", referenceConnection == null ? null : referenceConnection
        .getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "reference_schema_field", referenceSchemaField ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "reference_table_field", referenceTableField ) );

    retval.append( "      " ).append(
      XMLHandler.addTagValue( "compare_connection", compareConnection == null ? null : compareConnection
        .getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compare_schema_field", compareSchemaField ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compare_table_field", compareTableField ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "key_fields_field", keyFieldsField ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "exclude_fields_field", excludeFieldsField ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nr_errors_field", nrErrorsField ) );

    retval.append( "      " ).append(
      XMLHandler.addTagValue( "nr_records_reference_field", nrRecordsReferenceField ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nr_records_compare_field", nrRecordsCompareField ) );
    retval
      .append( "      " ).append( XMLHandler.addTagValue( "nr_errors_left_join_field", nrErrorsLeftJoinField ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "nr_errors_inner_join_field", nrErrorsInnerJoinField ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "nr_errors_right_join_field", nrErrorsRightJoinField ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "key_description_field", keyDescriptionField ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "value_reference_field", valueReferenceField ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "value_compare_field", valueCompareField ) );

    return retval.toString();
  }

  @Override
  public void setDefault() {
    nrErrorsField = "nrErrors";
    nrRecordsReferenceField = "nrRecordsReferenceTable";
    nrRecordsCompareField = "nrRecordsCompareTable";
    nrErrorsLeftJoinField = "nrErrorsLeftJoin";
    nrErrorsInnerJoinField = "nrErrorsInnerJoin";
    nrErrorsRightJoinField = "nrErrorsRightJoin";
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {

      referenceConnection = rep.loadDatabaseMetaFromStepAttribute( id_step, "reference_connection_id", databases );
      referenceSchemaField = rep.getStepAttributeString( id_step, "reference_schema_field" );
      referenceTableField = rep.getStepAttributeString( id_step, "reference_table_field" );

      compareConnection = rep.loadDatabaseMetaFromStepAttribute( id_step, "compare_connection_id", databases );
      compareSchemaField = rep.getStepAttributeString( id_step, "compare_schema_field" );
      compareTableField = rep.getStepAttributeString( id_step, "compare_table_field" );

      keyFieldsField = rep.getStepAttributeString( id_step, "key_fields_field" );
      excludeFieldsField = rep.getStepAttributeString( id_step, "exclude_fields_field" );
      nrErrorsField = rep.getStepAttributeString( id_step, "nr_errors_field" );

      nrRecordsReferenceField = rep.getStepAttributeString( id_step, "nr_records_reference_field" );
      nrRecordsCompareField = rep.getStepAttributeString( id_step, "nr_records_compare_field" );
      nrErrorsLeftJoinField = rep.getStepAttributeString( id_step, "nr_errors_left_join_field" );
      nrErrorsInnerJoinField = rep.getStepAttributeString( id_step, "nr_errors_inner_join_field" );
      nrErrorsRightJoinField = rep.getStepAttributeString( id_step, "nr_errors_right_join_field" );

      keyDescriptionField = rep.getStepAttributeString( id_step, "key_description_field" );
      valueReferenceField = rep.getStepAttributeString( id_step, "value_reference_field" );
      valueCompareField = rep.getStepAttributeString( id_step, "value_compare_field" );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute(
        id_transformation, id_step, "reference_connection_id", referenceConnection );
      rep.saveStepAttribute( id_transformation, id_step, "reference_schema_field", referenceSchemaField );
      rep.saveStepAttribute( id_transformation, id_step, "reference_table_field", referenceTableField );

      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "compare_connection_id", compareConnection );
      rep.saveStepAttribute( id_transformation, id_step, "compare_schema_field", compareSchemaField );
      rep.saveStepAttribute( id_transformation, id_step, "compare_table_field", compareTableField );

      rep.saveStepAttribute( id_transformation, id_step, "key_fields_field", keyFieldsField );
      rep.saveStepAttribute( id_transformation, id_step, "exclude_fields_field", excludeFieldsField );
      rep.saveStepAttribute( id_transformation, id_step, "nr_errors_field", nrErrorsField );

      rep.saveStepAttribute( id_transformation, id_step, "nr_records_reference_field", nrRecordsReferenceField );
      rep.saveStepAttribute( id_transformation, id_step, "nr_records_compare_field", nrRecordsCompareField );
      rep.saveStepAttribute( id_transformation, id_step, "nr_errors_left_join_field", nrErrorsLeftJoinField );
      rep.saveStepAttribute( id_transformation, id_step, "nr_errors_inner_join_field", nrErrorsInnerJoinField );
      rep.saveStepAttribute( id_transformation, id_step, "nr_errors_right_join_field", nrErrorsRightJoinField );

      rep.saveStepAttribute( id_transformation, id_step, "key_description_field", keyDescriptionField );
      rep.saveStepAttribute( id_transformation, id_step, "value_reference_field", valueReferenceField );
      rep.saveStepAttribute( id_transformation, id_step, "value_compare_field", valueCompareField );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "IfNullMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "IfNullMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "IfNullMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "IfNullMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new TableCompare( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new TableCompareData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

}
