/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.salesforceinput;

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
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
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
import org.pentaho.di.trans.steps.salesforce.SalesforceConnectionUtils;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step(
    id = "SalesforceInput",
    i18nPackageName = "org.pentaho.di.trans.steps.salesforceinput",
    name = "SalesforceInput.TypeLongDesc.SalesforceInput",
    description = "SalesforceInput.TypeTooltipDesc.SalesforceInput",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Input",
    image = "SFI.svg",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/SalesForce+Input" )
public class SalesforceInputMeta extends SalesforceStepMeta {
  public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!

  /** Flag indicating that we should include the generated SQL in the output */
  private boolean includeSQL;

  /** The name of the field in the output containing the generated SQL */
  private String sqlField;

  /** Flag indicating that we should include the server Timestamp in the output */
  private boolean includeTimestamp;

  /** The name of the field in the output containing the server Timestamp */
  private String timestampField;

  /** Flag indicating that we should include the filename in the output */
  private boolean includeTargetURL;

  /** The name of the field in the output containing the filename */
  private String targetURLField;

  /** Flag indicating that we should include the module in the output */
  private boolean includeModule;

  /** The name of the field in the output containing the module */
  private String moduleField;

  /** Flag indicating that a deletion date field should be included in the output */
  private boolean includeDeletionDate;

  /** The name of the field in the output containing the deletion Date */
  private String deletionDateField;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** The condition */
  private String condition;

  /** The maximum number or lines to read */
  private String rowLimit;

  /** The fields to return... */
  private SalesforceInputField[] inputFields;

  /** option: specify query **/
  private boolean specifyQuery;

  // ** query entered by user **/
  private String query;

  private int nrFields;

  private String readTo;
  private String readFrom;

  /** records filter */
  private int recordsFilter;

  /** Query all records including deleted ones **/
  private boolean queryAll;

  public SalesforceInputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the input fields.
   */
  public SalesforceInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( SalesforceInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the query.
   */
  public String getQuery() {
    return query;
  }

  /**
   * @param query
   *          The query to set.
   */
  public void setQuery( String query ) {
    this.query = query;
  }

  /**
   * @return Returns the specifyQuery.
   */
  public boolean isSpecifyQuery() {
    return specifyQuery;
  }

  /**
   * @param specifyQuery
   *          The specifyQuery to set.
   */
  public void setSpecifyQuery( boolean specifyQuery ) {
    this.specifyQuery = specifyQuery;
  }

  /**
   * @return Returns the queryAll.
   */
  public boolean isQueryAll() {
    return queryAll;
  }

  /**
   * @param queryAll
   *          The queryAll to set.
   */
  public void setQueryAll( boolean value ) {
    this.queryAll = value;
  }

  /**
   * @return Returns the condition.
   */
  public String getCondition() {
    return condition;
  }

  /**
   * @param condition
   *          The condition to set.
   */
  public void setCondition( String condition ) {
    this.condition = condition;
  }

  /**
   * @param TargetURLField
   *          The TargetURLField to set.
   */
  public void setTargetURLField( String TargetURLField ) {
    this.targetURLField = TargetURLField;
  }

  /**
   * @param sqlField
   *          The sqlField to set.
   */
  public void setSQLField( String sqlField ) {
    this.sqlField = sqlField;
  }

  /**
   * @param timestampField
   *          The timestampField to set.
   */
  public void setTimestampField( String timestampField ) {
    this.timestampField = timestampField;
  }

  /**
   * @param ModuleField
   *          The ModuleField to set.
   */
  public void setModuleField( String module_field ) {
    this.moduleField = module_field;
  }

  public int getRecordsFilter() {
    return recordsFilter;
  }

  public void setRecordsFilter( int recordsFilter ) {
    this.recordsFilter = recordsFilter;
  }

  /**
   * @return Returns the includeTargetURL.
   */
  public boolean includeTargetURL() {
    return includeTargetURL;
  }

  /**
   * @return Returns the includeSQL.
   */
  public boolean includeSQL() {
    return includeSQL;
  }

  /**
   * @param includeSQL
   *          to set.
   */
  public void setIncludeSQL( boolean includeSQL ) {
    this.includeSQL = includeSQL;
  }

  /**
   * @return Returns the includeTimestamp.
   */
  public boolean includeTimestamp() {
    return includeTimestamp;
  }

  /**
   * @param includeTimestamp
   *          to set.
   */
  public void setIncludeTimestamp( boolean includeTimestamp ) {
    this.includeTimestamp = includeTimestamp;
  }

  /**
   * @return Returns the includeModule.
   */
  public boolean includeModule() {
    return includeTargetURL;
  }

  /**
   * @param includeTargetURL
   *          The includeTargetURL to set.
   */
  public void setIncludeTargetURL( boolean includeTargetURL ) {
    this.includeTargetURL = includeTargetURL;
  }

  /**
   * @param includeModule
   *          The includeModule to set.
   */
  public void setIncludeModule( boolean includemodule ) {
    this.includeModule = includemodule;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean includeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @param includeRowNumber
   *          The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @return Returns the includeDeletionDate.
   */
  public boolean includeDeletionDate() {
    return includeDeletionDate;
  }

  /**
   * @param includeDeletionDate
   *          The includeDeletionDate to set.
   */
  public void setIncludeDeletionDate( boolean includeDeletionDate ) {
    this.includeDeletionDate = includeDeletionDate;
  }

  /**
   * @return Returns the rowLimit.
   */
  public String getRowLimit() {
    return rowLimit;
  }

  /**
   * @param rowLimit
   *          The rowLimit to set.
   */
  public void setRowLimit( String rowLimit ) {
    this.rowLimit = rowLimit;
  }

  /**
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }

  /**
   * @return Returns the deletionDateField.
   */
  public String getDeletionDateField() {
    return deletionDateField;
  }

  /**
   * @param value
   *          the deletionDateField to set.
   */
  public void setDeletionDateField( String value ) {
    this.deletionDateField = value;
  }

  /**
   * @return Returns the targetURLField.
   */
  public String getTargetURLField() {
    return targetURLField;
  }

  /**
   * @return Returns the readFrom.
   */
  public String getReadFrom() {
    return readFrom;
  }

  /**
   * @param readFrom
   *          the readFrom to set.
   */
  public void setReadFrom( String readFrom ) {
    this.readFrom = readFrom;
  }

  /**
   * @return Returns the readTo.
   */
  public String getReadTo() {
    return readTo;
  }

  /**
   * @param readTo
   *          the readTo to set.
   */
  public void setReadTo( String readTo ) {
    this.readTo = readTo;
  }

  /**
   * @return Returns the sqlField.
   */
  public String getSQLField() {
    return sqlField;
  }

  /**
   * @return Returns the timestampField.
   */
  public String getTimestampField() {
    return timestampField;
  }

  /**
   * @return Returns the moduleField.
   */
  public String getModuleField() {
    return moduleField;
  }

  /**
   * @param rowNumberField
   *          The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    super.loadXML( stepnode, databases, metaStore );
    readData( stepnode );
  }

  public Object clone() {
    SalesforceInputMeta retval = (SalesforceInputMeta) super.clone();

    int nrFields = inputFields.length;

    retval.allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      if ( inputFields[i] != null ) {
        retval.inputFields[i] = (SalesforceInputField) inputFields[i].clone();
      }
    }

    return retval;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( super.getXML() );
    retval.append( "    " ).append( XMLHandler.addTagValue( "condition", getCondition() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "specifyQuery", isSpecifyQuery() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "query", getQuery() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_targeturl", includeTargetURL() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "targeturl_field", getTargetURLField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_module", includeModule() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "module_field", getModuleField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_rownum", includeRowNumber() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_deletion_date", includeDeletionDate() ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "deletion_date_field", getDeletionDateField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum_field", getRowNumberField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_sql", includeSQL() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sql_field", getSQLField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_Timestamp", includeTimestamp() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "timestamp_field", getTimestampField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "read_from", getReadFrom() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "read_to", getReadTo() ) );
    retval.append( "    " ).append(
      XMLHandler
        .addTagValue( "records_filter", SalesforceConnectionUtils.getRecordsFilterCode( getRecordsFilter() ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "queryAll", isQueryAll() ) );

    retval.append( "    " ).append( XMLHandler.openTag( "fields" ) ).append( Const.CR );
    for ( SalesforceInputField field : inputFields ) {
      retval.append( field.getXML() );
    }
    retval.append( "    " ).append( XMLHandler.closeTag( "fields" ) ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( "limit", getRowLimit() ) );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      setCondition( XMLHandler.getTagValue( stepnode, "condition" ) );
      setQuery( XMLHandler.getTagValue( stepnode, "query" ) );
      setSpecifyQuery( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "specifyQuery" ) ) );
      setIncludeTargetURL( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_targeturl" ) ) );
      setTargetURLField( XMLHandler.getTagValue( stepnode, "targeturl_field" ) );
      setIncludeModule( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_module" ) ) );
      setModuleField( XMLHandler.getTagValue( stepnode, "module_field" ) );
      setIncludeRowNumber( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_rownum" ) ) );
      setIncludeDeletionDate( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_deletion_date" ) ) );
      setRowNumberField( XMLHandler.getTagValue( stepnode, "rownum_field" ) );
      setDeletionDateField( XMLHandler.getTagValue( stepnode, "deletion_date_field" ) );

      setIncludeSQL( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_sql" ) ) );
      setSQLField( XMLHandler.getTagValue( stepnode, "sql_field" ) );
      setIncludeTimestamp( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_Timestamp" ) ) );
      setTimestampField( XMLHandler.getTagValue( stepnode, "timestamp_field" ) );
      setReadFrom( XMLHandler.getTagValue( stepnode, "read_from" ) );
      setReadTo( XMLHandler.getTagValue( stepnode, "read_to" ) );
      setRecordsFilter(
        SalesforceConnectionUtils.getRecordsFilterByCode( Const.NVL( XMLHandler.getTagValue(
          stepnode, "records_filter" ), "" ) ) );
      setQueryAll( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "queryAll" ) ) );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        SalesforceInputField field = new SalesforceInputField( fnode );
        inputFields[i] = field;
      }
      // Is there a limit on the number of rows we process?
      setRowLimit( XMLHandler.getTagValue( stepnode, "limit" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void allocate( int nrfields ) {
    setInputFields( new SalesforceInputField[nrfields] );
  }

  public int getNrFields() {
    return nrFields;
  }

  public void setDefault() {
    super.setDefault();
    setIncludeDeletionDate( false );
    setQueryAll( false );
    setReadFrom( "" );
    setReadTo( "" );
    nrFields = 0;
    setSpecifyQuery( false );
    setQuery( "" );
    setCondition( "" );
    setIncludeTargetURL( false );
    setTargetURLField( "" );
    setIncludeModule( false );
    setModuleField( "" );
    setIncludeRowNumber( false );
    setRowNumberField( "" );
    setDeletionDateField( "" );
    setIncludeSQL( false );
    setSQLField( "" );
    setIncludeTimestamp( false );
    setTimestampField( "" );
    allocate( 0 );

    setRowLimit( "0" );
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    int i;
    for ( i = 0; i < inputFields.length; i++ ) {
      SalesforceInputField field = inputFields[i];

      int type = field.getType();
      if ( type == ValueMetaInterface.TYPE_NONE ) {
        type = ValueMetaInterface.TYPE_STRING;
      }
      try {
        ValueMetaInterface v =
          ValueMetaFactory.createValueMeta( space.environmentSubstitute( field.getName() ), type );
        v.setLength( field.getLength() );
        v.setPrecision( field.getPrecision() );
        v.setOrigin( name );
        v.setConversionMask( field.getFormat() );
        v.setDecimalSymbol( field.getDecimalSymbol() );
        v.setGroupingSymbol( field.getGroupSymbol() );
        v.setCurrencySymbol( field.getCurrencySymbol() );
        r.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }

    if ( includeTargetURL && !Utils.isEmpty( targetURLField ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( targetURLField ) );
      v.setLength( 250 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( includeModule && !Utils.isEmpty( moduleField ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( moduleField ) );
      v.setLength( 250 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( includeSQL && !Utils.isEmpty( sqlField ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( sqlField ) );
      v.setLength( 250 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( includeTimestamp && !Utils.isEmpty( timestampField ) ) {
      ValueMetaInterface v = new ValueMetaDate( space.environmentSubstitute( timestampField ) );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( includeRowNumber && !Utils.isEmpty( rowNumberField ) ) {
      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( includeDeletionDate && !Utils.isEmpty( deletionDateField ) ) {
      ValueMetaInterface v = new ValueMetaDate( space.environmentSubstitute( deletionDateField ) );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    super.readRep( rep, metaStore, id_step, databases );
    try {
      // H.kawaguchi Add 19-01-2009
      setCondition( rep.getStepAttributeString( id_step, "condition" ) );
      // H.kawaguchi Add 19-01-2009
      setQuery( rep.getStepAttributeString( id_step, "query" ) );
      setSpecifyQuery( rep.getStepAttributeBoolean( id_step, "specifyQuery" ) );
      setIncludeTargetURL( rep.getStepAttributeBoolean( id_step, "include_targeturl" ) );
      setTargetURLField( rep.getStepAttributeString( id_step, "targeturl_field" ) );
      setIncludeModule( rep.getStepAttributeBoolean( id_step, "include_module" ) );
      setModuleField( rep.getStepAttributeString( id_step, "module_field" ) );
      setIncludeRowNumber( rep.getStepAttributeBoolean( id_step, "include_rownum" ) );
      setIncludeDeletionDate( rep.getStepAttributeBoolean( id_step, "include_deletion_date" ) );
      setRowNumberField( rep.getStepAttributeString( id_step, "rownum_field" ) );
      setDeletionDateField( rep.getStepAttributeString( id_step, "deletion_date_field" ) );
      setIncludeSQL( rep.getStepAttributeBoolean( id_step, "include_sql" ) );
      setSQLField( rep.getStepAttributeString( id_step, "sql_field" ) );
      setIncludeTimestamp( rep.getStepAttributeBoolean( id_step, "include_Timestamp" ) );
      setTimestampField( rep.getStepAttributeString( id_step, "timestamp_field" ) );
      setRowLimit( rep.getStepAttributeString( id_step, "limit" ) );
      setReadFrom( rep.getStepAttributeString( id_step, "read_from" ) );
      setReadTo( rep.getStepAttributeString( id_step, "read_to" ) );
      setRecordsFilter(
        SalesforceConnectionUtils.getRecordsFilterByCode( Const.NVL( rep.getStepAttributeString(
          id_step, "records_filter" ), "" ) ) );
      setQueryAll( rep.getStepAttributeBoolean( id_step, "queryAll" ) );

      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        SalesforceInputField field = new SalesforceInputField();
        field.readRep( rep, metaStore, id_step, i );
        inputFields[i] = field;
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    super.saveRep( rep, metaStore, id_transformation, id_step );
    try {
      // H.kawaguchi Add 19-01-2009
      rep.saveStepAttribute( id_transformation, id_step, "condition", getCondition() );
      // H.kawaguchi Add 19-01-2009
      rep.saveStepAttribute( id_transformation, id_step, "query", getQuery() );
      rep.saveStepAttribute( id_transformation, id_step, "specifyQuery", isSpecifyQuery() );

      rep.saveStepAttribute( id_transformation, id_step, "include_targeturl", includeTargetURL() );
      rep.saveStepAttribute( id_transformation, id_step, "targeturl_field", getTargetURLField() );
      rep.saveStepAttribute( id_transformation, id_step, "include_module", includeModule() );
      rep.saveStepAttribute( id_transformation, id_step, "module_field", getModuleField() );
      rep.saveStepAttribute( id_transformation, id_step, "include_rownum", includeRowNumber() );
      rep.saveStepAttribute( id_transformation, id_step, "include_deletion_date", includeDeletionDate() );

      rep.saveStepAttribute( id_transformation, id_step, "include_sql", includeSQL() );
      rep.saveStepAttribute( id_transformation, id_step, "sql_field", getSQLField() );
      rep.saveStepAttribute( id_transformation, id_step, "include_Timestamp", includeTimestamp() );
      rep.saveStepAttribute( id_transformation, id_step, "timestamp_field", getTimestampField() );
      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", getRowNumberField() );
      rep.saveStepAttribute( id_transformation, id_step, "deletion_date_field", getDeletionDateField() );

      rep.saveStepAttribute( id_transformation, id_step, "limit", getRowLimit() );
      rep.saveStepAttribute( id_transformation, id_step, "read_from", getReadFrom() );
      rep.saveStepAttribute( id_transformation, id_step, "read_to", getReadTo() );
      rep.saveStepAttribute( id_transformation, id_step, "records_filter", SalesforceConnectionUtils
        .getRecordsFilterCode( getRecordsFilter() ) );
      rep.saveStepAttribute( id_transformation, id_step, "queryAll", isQueryAll() );

      for ( int i = 0; i < inputFields.length; i++ ) {
        SalesforceInputField field = inputFields[i];
        field.saveRep( rep, metaStore, id_transformation, id_step, i );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceInputMeta.Exception.ErrorSavingToRepository", "" + id_step ), e );
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
          PKG, "SalesforceInputMeta.CheckResult.NoInputExpected" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.NoInput" ), stepMeta );
    }
    remarks.add( cr );

    // check return fields
    if ( getInputFields().length == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.NoFields" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.FieldsOk" ), stepMeta );
    }
    remarks.add( cr );

    // check additional fields
    if ( includeTargetURL() && Utils.isEmpty( getTargetURLField() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.NoTargetURLField" ), stepMeta );
      remarks.add( cr );
    }
    if ( includeSQL() && Utils.isEmpty( getSQLField() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.NoSQLField" ), stepMeta );
      remarks.add( cr );
    }
    if ( includeModule() && Utils.isEmpty( moduleField ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.NoModuleField" ), stepMeta );
      remarks.add( cr );
    }
    if ( includeTimestamp() && Utils.isEmpty( getTimestampField() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.NoTimestampField" ), stepMeta );
      remarks.add( cr );
    }
    if ( includeRowNumber() && Utils.isEmpty( getRowNumberField() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.NoRowNumberField" ), stepMeta );
      remarks.add( cr );
    }
    if ( includeDeletionDate() && Utils.isEmpty( getDeletionDateField() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceInputMeta.CheckResult.NoDeletionDateField" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new SalesforceInput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new SalesforceInputData();
  }
}
