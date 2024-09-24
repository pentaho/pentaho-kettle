/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.salesforceupsert;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
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
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step(
    id = "SalesforceUpsert",
    i18nPackageName = "org.pentaho.di.trans.steps.salesforceupsert",
    name = "SalesforceUpsert.TypeLongDesc.SalesforceUpsert",
    description = "SalesforceUpsert.TypeTooltipDesc.SalesforceUpsert",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Output",
    image = "SFU.svg",
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/salesforce-upsert" )
public class SalesforceUpsertMeta extends SalesforceStepMeta {
  private static Class<?> PKG = SalesforceUpsertMeta.class; // for i18n purposes, needed by Translator2!!

  /** UpsertField */
  private String UpsertField;

  /** Field value to update */
  private String[] updateLookup;

  /** Stream name to update value with */
  private String[] updateStream;

  /** boolean indicating if field uses External id */
  private Boolean[] useExternalId;

  /** Batch size */
  private String batchSize;

  private String salesforceIDFieldName;

  private boolean rollbackAllChangesOnError;

  public SalesforceUpsertMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the rollbackAllChangesOnError.
   */
  public boolean isRollbackAllChangesOnError() {
    return rollbackAllChangesOnError;
  }

  /**
   * @param rollbackAllChangesOnError
   *          The rollbackAllChangesOnError to set.
   */
  public void setRollbackAllChangesOnError( boolean rollbackAllChangesOnError ) {
    this.rollbackAllChangesOnError = rollbackAllChangesOnError;
  }

  /**
   * @return Returns the updateLookup.
   */
  public String[] getUpdateLookup() {
    return updateLookup;
  }

  /**
   * @param updateLookup
   *          The updateLookup to set.
   */
  public void setUpdateLookup( String[] updateLookup ) {
    this.updateLookup = updateLookup;
  }

  /**
   * @return Returns the updateStream.
   */
  public String[] getUpdateStream() {
    return updateStream;
  }

  /**
   * @param updateStream
   *          The updateStream to set.
   */
  public void setUpdateStream( String[] updateStream ) {
    this.updateStream = updateStream;
  }

  /**
   * @return Returns the useExternalId.
   */
  public Boolean[] getUseExternalId() {
    return useExternalId;
  }

  /**
   * @param useExternalId
   *          The useExternalId to set.
   */
  public void setUseExternalId( Boolean[] useExternalId ) {
    this.useExternalId = useExternalId;
  }

  /**
   * @param upsertField
   *          The upsert field to set.
   */
  public void setUpsertField( String upsertField ) {
    this.UpsertField = upsertField;
  }

  /**
   * @return Returns the upsertfield.
   */
  public String getUpsertField() {
    return this.UpsertField;
  }

  /**
   * @param batch
   *          size.
   */
  public void setBatchSize( String value ) {
    this.batchSize = value;
  }

  /**
   * @return Returns the batchSize.
   */
  public String getBatchSize() {
    return this.batchSize;
  }

  public int getBatchSizeInt() {
    return Const.toInt( this.batchSize, 10 );
  }

  public String getSalesforceIDFieldName() {
    return this.salesforceIDFieldName;
  }

  public void setSalesforceIDFieldName( String salesforceIDFieldName ) {
    this.salesforceIDFieldName = salesforceIDFieldName;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    super.loadXML( stepnode, databases, metaStore );
    readData( stepnode );
  }

  public Object clone() {
    SalesforceUpsertMeta retval = (SalesforceUpsertMeta) super.clone();

    int nrvalues = updateLookup.length;

    retval.allocate( nrvalues );

    for ( int i = 0; i < nrvalues; i++ ) {
      retval.updateLookup[i] = updateLookup[i];
      retval.updateStream[i] = updateStream[i];
      retval.useExternalId[i] = useExternalId[i];
    }

    return retval;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( super.getXML() );
    retval.append( "    " + XMLHandler.addTagValue( "upsertfield", getUpsertField() ) );
    retval.append( "    " + XMLHandler.addTagValue( "batchSize", getBatchSize() ) );
    retval.append( "    " + XMLHandler.addTagValue( "salesforceIDFieldName", getSalesforceIDFieldName() ) );

    retval.append( "    <fields>" + Const.CR );

    for ( int i = 0; i < updateLookup.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", getUpdateLookup()[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", getUpdateStream()[i] ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "useExternalId", getUseExternalId()[i].booleanValue() ) );
      retval.append( "      </field>" ).append( Const.CR );
    }

    retval.append( "      </fields>" + Const.CR );
    retval.append( "    " + XMLHandler.addTagValue( "rollbackAllChangesOnError", isRollbackAllChangesOnError() ) );
    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      setUpsertField( XMLHandler.getTagValue( stepnode, "upsertfield" ) );

      setBatchSize( XMLHandler.getTagValue( stepnode, "batchSize" ) );
      setSalesforceIDFieldName( XMLHandler.getTagValue( stepnode, "salesforceIDFieldName" ) );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        updateLookup[i] = XMLHandler.getTagValue( fnode, "name" );
        updateStream[i] = XMLHandler.getTagValue( fnode, "field" );
        if ( updateStream[i] == null ) {
          updateStream[i] = updateLookup[i]; // default: the same name!
        }
        String updateValue = XMLHandler.getTagValue( fnode, "useExternalId" );
        if ( updateValue == null ) {
          // default FALSE
          useExternalId[i] = Boolean.FALSE;
        } else {
          if ( updateValue.equalsIgnoreCase( "Y" ) ) {
            useExternalId[i] = Boolean.TRUE;
          } else {
            useExternalId[i] = Boolean.FALSE;
          }
        }
      }
      setRollbackAllChangesOnError(
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rollbackAllChangesOnError" ) ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void allocate( int nrvalues ) {
    setUpdateLookup( new String[nrvalues] );
    setUpdateStream( new String[nrvalues] );
    setUseExternalId( new Boolean[nrvalues] );
  }

  public void setDefault() {
    super.setDefault();
    setUpsertField( "Id" );
    setBatchSize( "10" );
    setSalesforceIDFieldName( "Id" );

    allocate( 0 );

    setRollbackAllChangesOnError( false );
  }

  /* This function adds meta data to the rows being pushed out */
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    String realfieldname = space.environmentSubstitute( getSalesforceIDFieldName() );
    if ( !Utils.isEmpty( realfieldname ) ) {
      ValueMetaInterface v = new ValueMetaString( realfieldname );
      v.setLength( 18 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    super.readRep( rep, metaStore, id_step, databases );
    try {
      setUpsertField( rep.getStepAttributeString( id_step, "upsertfield" ) );
      setBatchSize( rep.getStepAttributeString( id_step, "batchSize" ) );
      setSalesforceIDFieldName( rep.getStepAttributeString( id_step, "salesforceIDFieldName" ) );
      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );
      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        updateLookup[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        updateStream[i] = rep.getStepAttributeString( id_step, i, "field_attribut" );
        useExternalId[i] =
          Boolean.valueOf( rep.getStepAttributeBoolean( id_step, i, "field_useExternalId", false ) );
      }
      setRollbackAllChangesOnError( rep.getStepAttributeBoolean( id_step, "rollbackAllChangesOnError" ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceUpsertMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    super.saveRep( rep, metaStore, id_transformation, id_step );
    try {
      rep.saveStepAttribute( id_transformation, id_step, "batchSize", getBatchSize() );
      rep.saveStepAttribute( id_transformation, id_step, "upsertfield", getUpsertField() );
      rep.saveStepAttribute( id_transformation, id_step, "salesforceIDFieldName", getSalesforceIDFieldName() );

      for ( int i = 0; i < updateLookup.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", getUpdateLookup()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_attribut", getUpdateStream()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_useExternalId", getUseExternalId()[i]
          .booleanValue() );

      }
      rep.saveStepAttribute( id_transformation, id_step, "rollbackAllChangesOnError", isRollbackAllChangesOnError() );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceUpsertMeta.Exception.ErrorSavingToRepository", "" + id_step ), e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    super.check( remarks, transMeta, stepMeta, prev, input, output, info, space, repository, metaStore );
    CheckResult cr;

    // See if we get input...
    if ( input != null && input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceUpsertMeta.CheckResult.NoInputExpected" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SalesforceUpsertMeta.CheckResult.NoInput" ), stepMeta );
    }
    remarks.add( cr );

    // check return fields
    if ( getUpdateLookup().length == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceUpsertMeta.CheckResult.NoFields" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SalesforceUpsertMeta.CheckResult.FieldsOk" ), stepMeta );
    }
    remarks.add( cr );

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new SalesforceUpsert( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new SalesforceUpsertData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

}
