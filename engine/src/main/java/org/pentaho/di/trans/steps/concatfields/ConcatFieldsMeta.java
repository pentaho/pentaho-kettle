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

package org.pentaho.di.trans.steps.concatfields;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
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
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * ConcatFieldsMeta
 * @author jb
 * @since 2012-08-31
 *
 */
public class ConcatFieldsMeta extends TextFileOutputMeta implements StepMetaInterface {

  private static Class<?> PKG = ConcatFieldsMeta.class; // for i18n purposes, needed by Translator2!!

  // have a different namespace in repository in contrast to the TextFileOutput
  private static final String ConcatFieldsNodeNameSpace = "ConcatFields";

  private String targetFieldName; // the target field name
  private int targetFieldLength; // the length of the string field
  private boolean removeSelectedFields; // remove the selected fields in the output stream

  public String getTargetFieldName() {
    return targetFieldName;
  }

  public void setTargetFieldName( String targetField ) {
    this.targetFieldName = targetField;
  }

  public int getTargetFieldLength() {
    return targetFieldLength;
  }

  public void setTargetFieldLength( int targetFieldLength ) {
    this.targetFieldLength = targetFieldLength;
  }

  public boolean isRemoveSelectedFields() {
    return removeSelectedFields;
  }

  public void setRemoveSelectedFields( boolean removeSelectedFields ) {
    this.removeSelectedFields = removeSelectedFields;
  }

  public ConcatFieldsMeta() {
    super(); // allocate TextFileOutputMeta
  }

  @Override
  public void setDefault() {
    super.setDefault();
    // overwrite header
    super.setHeaderEnabled( false );
    // set default for new properties specific to the concat fields
    targetFieldName = "";
    targetFieldLength = 0;
    removeSelectedFields = false;
  }

  @Deprecated
  public void getFieldsModifyInput( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space ) throws KettleStepException {
    getFieldsModifyInput( row, name, info, nextStep, space, null, null );
  }

  public void getFieldsModifyInput( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // the field precisions and lengths are altered! see TextFileOutputMeta.getFields().
    super.getFields( row, name, info, nextStep, space, repository, metaStore );
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // do not call the super class from TextFileOutputMeta since it modifies the source meta data
    // see getFieldsModifyInput() instead

    // remove selected fields from the stream when true
    if ( removeSelectedFields ) {
      if ( getOutputFields().length > 0 ) {
        for ( int i = 0; i < getOutputFields().length; i++ ) {
          TextFileField field = getOutputFields()[i];
          try {
            row.removeValueMeta( field.getName() );
          } catch ( KettleValueException e ) {
            // just ignore exceptions since missing fields are handled in the ConcatFields class
          }
        }
      } else { // no output fields selected, take them all, remove them all
        row.clear();
      }
    }

    // Check Target Field Name
    if ( Utils.isEmpty( targetFieldName ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "ConcatFieldsMeta.CheckResult.TargetFieldNameMissing" ) );
    }
    // add targetFieldName
    ValueMetaInterface vValue = new ValueMetaString( targetFieldName );
    vValue.setLength( targetFieldLength, 0 );
    vValue.setOrigin( name );
    if ( !Utils.isEmpty( getEncoding() ) ) {
      vValue.setStringEncoding( getEncoding() );
    }
    row.addValueMeta( vValue );
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    super.loadXML( stepnode, databases, metaStore );
    targetFieldName = XMLHandler.getTagValue( stepnode, ConcatFieldsNodeNameSpace, "targetFieldName" );
    targetFieldLength =
      Const.toInt( XMLHandler.getTagValue( stepnode, ConcatFieldsNodeNameSpace, "targetFieldLength" ), 0 );
    removeSelectedFields =
      "Y"
        .equalsIgnoreCase( XMLHandler
          .getTagValue( stepnode, ConcatFieldsNodeNameSpace, "removeSelectedFields" ) );
  }

  @Override
  public String getXML() {
    String retval = super.getXML();
    retval = retval + "    <" + ConcatFieldsNodeNameSpace + ">" + Const.CR;
    retval = retval + XMLHandler.addTagValue( "targetFieldName", targetFieldName );
    retval = retval + XMLHandler.addTagValue( "targetFieldLength", targetFieldLength );
    retval = retval + XMLHandler.addTagValue( "removeSelectedFields", removeSelectedFields );
    retval = retval + "    </" + ConcatFieldsNodeNameSpace + ">" + Const.CR;
    return retval;
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    super.readRep( rep, metaStore, id_step, databases );
    targetFieldName = rep.getStepAttributeString( id_step, ConcatFieldsNodeNameSpace + "targetFieldName" );
    targetFieldLength =
      (int) rep.getStepAttributeInteger( id_step, ConcatFieldsNodeNameSpace + "targetFieldLength" );
    removeSelectedFields =
      rep.getStepAttributeBoolean( id_step, ConcatFieldsNodeNameSpace + "removeSelectedFields" );
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    super.saveRep( rep, metaStore, id_transformation, id_step );
    rep.saveStepAttribute(
      id_transformation, id_step, ConcatFieldsNodeNameSpace + "targetFieldName", targetFieldName );
    rep.saveStepAttribute(
      id_transformation, id_step, ConcatFieldsNodeNameSpace + "targetFieldLength", targetFieldLength );
    rep.saveStepAttribute(
      id_transformation, id_step, ConcatFieldsNodeNameSpace + "removeSelectedFields", removeSelectedFields );
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // Check Target Field Name
    if ( Utils.isEmpty( targetFieldName ) ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ConcatFieldsMeta.CheckResult.TargetFieldNameMissing" ), stepMeta );
      remarks.add( cr );
    }

    // Check Target Field Length when Fast Data Dump
    if ( targetFieldLength <= 0 && isFastDump() ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "ConcatFieldsMeta.CheckResult.TargetFieldLengthMissingFastDataDump" ), stepMeta );
      remarks.add( cr );
    }

    // Check output fields
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ConcatFieldsMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
      remarks.add( cr );

      String error_message = "";
      boolean error_found = false;

      // Starting from selected fields in ...
      for ( int i = 0; i < getOutputFields().length; i++ ) {
        int idx = prev.indexOfValue( getOutputFields()[i].getName() );
        if ( idx < 0 ) {
          error_message += "\t\t" + getOutputFields()[i].getName() + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message = BaseMessages.getString( PKG, "ConcatFieldsMeta.CheckResult.FieldsNotFound", error_message );
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ConcatFieldsMeta.CheckResult.AllFieldsFound" ), stepMeta );
        remarks.add( cr );
      }
    }

  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ConcatFields( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new ConcatFieldsData();
  }

  @Override
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return new ConcatFieldsMetaInjection( this );
  }

}
