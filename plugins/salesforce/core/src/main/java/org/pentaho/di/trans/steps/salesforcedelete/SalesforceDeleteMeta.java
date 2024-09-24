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

package org.pentaho.di.trans.steps.salesforcedelete;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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
    id = "SalesforceDelete",
    i18nPackageName = "org.pentaho.di.trans.steps.salesforcedelete",
    name = "SalesforceDelete.TypeLongDesc.SalesforceDelete",
    description = "SalesforceDelete.TypeTooltipDesc.SalesforceDelete",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Output",
    image = "SFD.svg",
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/salesforce-delete" )
public class SalesforceDeleteMeta extends SalesforceStepMeta {
  private static Class<?> PKG = SalesforceDeleteMeta.class; // for i18n purposes, needed by Translator2!!

  /** Deletefield */
  private String DeleteField;

  /** Batch size */
  private String batchSize;

  private boolean rollbackAllChangesOnError;

  public SalesforceDeleteMeta() {
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
   * @param DeleteField
   *          The DeleteField to set.
   */
  public void setDeleteField( String DeleteField ) {
    this.DeleteField = DeleteField;
  }

  /**
   * @return Returns the DeleteField.
   */
  public String getDeleteField() {
    return this.DeleteField;
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

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    super.loadXML( stepnode, databases, metaStore );
    readData( stepnode );
  }

  public Object clone() {
    SalesforceDeleteMeta retval = (SalesforceDeleteMeta) super.clone();

    return retval;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( super.getXML() );
    retval.append( "    " + XMLHandler.addTagValue( "DeleteField", getDeleteField() ) );
    retval.append( "    " + XMLHandler.addTagValue( "batchSize", getBatchSize() ) );
    retval.append( "    " + XMLHandler.addTagValue( "rollbackAllChangesOnError", isRollbackAllChangesOnError() ) );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      setDeleteField( XMLHandler.getTagValue( stepnode, "DeleteField" ) );

      setBatchSize( XMLHandler.getTagValue( stepnode, "batchSize" ) );
      setRollbackAllChangesOnError(
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rollbackAllChangesOnError" ) ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    super.setDefault();
    setModule( "Account" );
    setDeleteField( null );
    setBatchSize( "10" );
    setRollbackAllChangesOnError( false );
  }

  /* This function adds meta data to the rows being pushed out */
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    super.readRep( rep, metaStore, id_step, databases );
    try {
      setDeleteField( rep.getStepAttributeString( id_step, "DeleteField" ) );
      setBatchSize( rep.getStepAttributeString( id_step, "batchSize" ) );
      setRollbackAllChangesOnError( rep.getStepAttributeBoolean( id_step, "rollbackAllChangesOnError" ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceDeleteMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    super.saveRep( rep, metaStore, id_transformation, id_step );
    try {
      rep.saveStepAttribute( id_transformation, id_step, "batchSize", getBatchSize() );
      rep.saveStepAttribute( id_transformation, id_step, "DeleteField", getDeleteField() );
      rep.saveStepAttribute( id_transformation, id_step, "rollbackAllChangesOnError", isRollbackAllChangesOnError() );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceDeleteMeta.Exception.ErrorSavingToRepository", "" + id_step ), e );
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
          PKG, "SalesforceDeleteMeta.CheckResult.NoInputExpected" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SalesforceDeleteMeta.CheckResult.NoInput" ), stepMeta );
    }
    remarks.add( cr );
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new SalesforceDelete( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new SalesforceDeleteData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

}
