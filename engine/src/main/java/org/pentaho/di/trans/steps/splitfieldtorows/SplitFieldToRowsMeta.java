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

package org.pentaho.di.trans.steps.splitfieldtorows;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
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

public class SplitFieldToRowsMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SplitFieldToRowsMeta.class; // for i18n purposes, needed by Translator2!!

  /** Field to split */
  private String splitField;

  /** Split field based upon this delimiter. */
  private String delimiter;

  /** New name of the split field */
  private String newFieldname;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** Flag indicating that we should reset RowNum for each file */
  private boolean resetRowNumber;

  /** Flag indicating that the delimiter is a regular expression */
  private boolean isDelimiterRegex;

  public boolean isDelimiterRegex() {
    return isDelimiterRegex;
  }

  public void setDelimiterRegex( boolean isDelimiterRegex ) {
    this.isDelimiterRegex = isDelimiterRegex;
  }

  public SplitFieldToRowsMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the delimiter.
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * @param delimiter
   *          The delimiter to set.
   */
  public void setDelimiter( String delimiter ) {
    this.delimiter = delimiter;
  }

  /**
   * @return Returns the splitField.
   */
  public String getSplitField() {
    return splitField;
  }

  /**
   * @param splitField
   *          The splitField to set.
   */
  public void setSplitField( String splitField ) {
    this.splitField = splitField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      splitField = XMLHandler.getTagValue( stepnode, "splitfield" );
      delimiter = XMLHandler.getTagValue( stepnode, "delimiter" );
      newFieldname = XMLHandler.getTagValue( stepnode, "newfield" );
      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );
      resetRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "resetrownumber" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );
      isDelimiterRegex = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "delimiter_is_regex" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "SplitFieldToRowsMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    splitField = "";
    delimiter = ";";
    newFieldname = "";
    includeRowNumber = false;
    isDelimiterRegex = false;
    rowNumberField = "";
    resetRowNumber = true;
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    ValueMetaInterface v = new ValueMetaString( newFieldname );
    v.setOrigin( name );
    row.addValueMeta( v );

    // include row number
    if ( includeRowNumber ) {
      v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "   " + XMLHandler.addTagValue( "splitfield", splitField ) );
    retval.append( "   " + XMLHandler.addTagValue( "delimiter", delimiter ) );
    retval.append( "   " + XMLHandler.addTagValue( "newfield", newFieldname ) );
    retval.append( "   " + XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "   " + XMLHandler.addTagValue( "rownum_field", rowNumberField ) );
    retval.append( "   " + XMLHandler.addTagValue( "resetrownumber", resetRowNumber ) );
    retval.append( "   " + XMLHandler.addTagValue( "delimiter_is_regex", isDelimiterRegex ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      splitField = rep.getStepAttributeString( id_step, "splitfield" );
      delimiter = rep.getStepAttributeString( id_step, "delimiter" );
      newFieldname = rep.getStepAttributeString( id_step, "newfield" );
      includeRowNumber = rep.getStepAttributeBoolean( id_step, "rownum" );
      rowNumberField = rep.getStepAttributeString( id_step, "rownum_field" );
      resetRowNumber = rep.getStepAttributeBoolean( id_step, "reset_rownumber" );
      isDelimiterRegex = rep.getStepAttributeBoolean( id_step, 0, "delimiter_is_regex", false );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SplitFieldToRowsMeta.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "splitfield", splitField );
      rep.saveStepAttribute( id_transformation, id_step, "delimiter", delimiter );
      rep.saveStepAttribute( id_transformation, id_step, "newfield", newFieldname );
      rep.saveStepAttribute( id_transformation, id_step, "rownum", includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "reset_rownumber", resetRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", rowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, "delimiter_is_regex", isDelimiterRegex );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SplitFieldToRowsMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    String error_message = "";
    CheckResult cr;

    // Look up fields in the input stream <prev>
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SplitFieldToRowsMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      error_message = "";

      ValueMetaInterface v = prev.searchValueMeta( splitField );
      if ( v == null ) {
        error_message =
          BaseMessages.getString(
            PKG, "SplitFieldToRowsMeta.CheckResult.FieldToSplitNotPresentInInputStream", splitField );
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "SplitFieldToRowsMeta.CheckResult.FieldToSplitFoundInInputStream", splitField ), stepMeta );
        remarks.add( cr );
      }
    } else {
      error_message =
        BaseMessages.getString( PKG, "SplitFieldToRowsMeta.CheckResult.CouldNotReadFieldsFromPreviousStep" )
          + Const.CR;
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SplitFieldToRowsMeta.CheckResult.StepReceivingInfoFromOtherStep" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SplitFieldToRowsMeta.CheckResult.NoInputReceivedFromOtherStep" ), stepMeta );
      remarks.add( cr );
    }

    if ( Utils.isEmpty( newFieldname ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SplitFieldToRowsMeta.CheckResult.NewFieldNameIsNull" ), stepMeta );
      remarks.add( cr );
    }
    if ( includeRowNumber ) {
      if ( Utils.isEmpty( transMeta.environmentSubstitute( rowNumberField ) ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "SplitFieldToRowsMeta.CheckResult.RowNumberFieldMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "SplitFieldToRowsMeta.CheckResult.RowNumberFieldOk" ), stepMeta );
      }
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new SplitFieldToRows( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new SplitFieldToRowsData();
  }

  /**
   * @return the newFieldname
   */
  public String getNewFieldname() {
    return newFieldname;
  }

  /**
   * @param newFieldname
   *          the newFieldname to set
   */
  public void setNewFieldname( String newFieldname ) {
    this.newFieldname = newFieldname;
  }

  /**
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }

  /**
   * @param rowNumberField
   *          The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  /**
   * @return Returns the resetRowNumber.
   */
  public boolean resetRowNumber() {
    return resetRowNumber;
  }

  /**
   * @param resetRowNumber
   *          The resetRowNumber to set.
   */
  public void setResetRowNumber( boolean resetRowNumber ) {
    this.resetRowNumber = resetRowNumber;
  }

  /**
   * @param includeRowNumber
   *          The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean includeRowNumber() {
    return includeRowNumber;
  }
}
