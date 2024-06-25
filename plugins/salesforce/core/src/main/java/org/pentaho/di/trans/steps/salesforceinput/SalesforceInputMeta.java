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
package org.pentaho.di.trans.steps.salesforceinput;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
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
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/salesforce-input" )
@InjectionSupported( localizationPrefix = "SalesforceInputMeta.Injection.", groups = { "FIELDS" } )
public class SalesforceInputMeta extends SalesforceStepMeta {
  public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!

  /** Flag indicating that we should include the generated SQL in the output */
  @Injection( name = "INCLUDE_SQL_IN_OUTPUT" )
  private boolean includeSQL;

  /** The name of the field in the output containing the generated SQL */
  @Injection( name = "SQL_FIELDNAME" )
  private String sqlField;

  /** Flag indicating that we should include the server Timestamp in the output */
  @Injection( name = "INCLUDE_TIMESTAMP_IN_OUTPUT" )
  private boolean includeTimestamp;

  /** The name of the field in the output containing the server Timestamp */
  @Injection( name = "TIMESTAMP_FIELDNAME" )
  private String timestampField;

  /** Flag indicating that we should include the filename in the output */
  @Injection( name = "INCLUDE_URL_IN_OUTPUT" )
  private boolean includeTargetURL;

  /** The name of the field in the output containing the filename */
  @Injection( name = "URL_FIELDNAME" )
  private String targetURLField;

  /** Flag indicating that we should include the module in the output */
  @Injection( name = "INCLUDE_MODULE_IN_OUTPUT" )
  private boolean includeModule;

  /** The name of the field in the output containing the module */
  @Injection( name = "MODULE_FIELDNAME" )
  private String moduleField;

  /** Flag indicating that a deletion date field should be included in the output */
  @Injection( name = "INCLUDE_DELETION_DATE_IN_OUTPUT" )
  private boolean includeDeletionDate;

  /** The name of the field in the output containing the deletion Date */
  @Injection( name = "DELETION_DATE_FIELDNAME" )
  private String deletionDateField;

  /** Flag indicating that a row number field should be included in the output */
  @Injection( name = "INCLUDE_ROWNUM_IN_OUTPUT" )
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  @Injection( name = "ROWNUM_FIELDNAME" )
  private String rowNumberField;

  /** The condition */
  @Injection( name = "QUERY_CONDITION" )
  private String condition;

  /** The maximum number or lines to read */
  @Injection( name = "LIMIT" )
  private String rowLimit;

  /** The fields to return... */
  @InjectionDeep
  private SalesforceInputField[] inputFields;

  /** option: specify query **/
  @Injection( name = "USE_SPECIFIED_QUERY" )
  private boolean specifyQuery;

  // ** query entered by user **/
  @Injection( name = "SPECIFY_QUERY" )
  private String query;

  private int nrFields;

  @Injection( name = "END_DATE" )
  private String readTo;
  @Injection( name = "START_DATE" )
  private String readFrom;

  /** records filter */
  private int recordsFilter;

  /** Query all records including deleted ones **/
  @Injection( name = "QUERY_ALL" )
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
  public void setQueryAll( boolean queryAll ) {
    this.queryAll = queryAll;
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
   * @param targetURLField
   *          The targetURLField to set.
   */
  public void setTargetURLField( String targetURLField ) {
    this.targetURLField = targetURLField;
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
   * @param moduleField
   *          The moduleField to set.
   */
  public void setModuleField( String moduleField ) {
    this.moduleField = moduleField;
  }

  public int getRecordsFilter() {
    return recordsFilter;
  }

  public void setRecordsFilter( int recordsFilter ) {
    this.recordsFilter = recordsFilter;
  }

  @Injection( name = "RETRIEVE" )
  public void setRecordsFilterDesc( String recordsFilterDesc ) {
    this.recordsFilter = SalesforceConnectionUtils.getRecordsFilterByDesc( recordsFilterDesc );
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
    return includeModule;
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
  public void setIncludeModule( boolean includeModule ) {
    this.includeModule = includeModule;
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

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    super.loadXML( stepnode, databases, metaStore );
    readData( stepnode );
  }

  @Override
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

  @Override
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

  @Override
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

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases ) throws KettleException {
    super.readRep( rep, metaStore, idStep, databases );
    try {
      // H.kawaguchi Add 19-01-2009
      setCondition( rep.getStepAttributeString( idStep, "condition" ) );
      // H.kawaguchi Add 19-01-2009
      setQuery( rep.getStepAttributeString( idStep, "query" ) );
      setSpecifyQuery( rep.getStepAttributeBoolean( idStep, "specifyQuery" ) );
      setIncludeTargetURL( rep.getStepAttributeBoolean( idStep, "include_targeturl" ) );
      setTargetURLField( rep.getStepAttributeString( idStep, "targeturl_field" ) );
      setIncludeModule( rep.getStepAttributeBoolean( idStep, "include_module" ) );
      setModuleField( rep.getStepAttributeString( idStep, "module_field" ) );
      setIncludeRowNumber( rep.getStepAttributeBoolean( idStep, "include_rownum" ) );
      setIncludeDeletionDate( rep.getStepAttributeBoolean( idStep, "include_deletion_date" ) );
      setRowNumberField( rep.getStepAttributeString( idStep, "rownum_field" ) );
      setDeletionDateField( rep.getStepAttributeString( idStep, "deletion_date_field" ) );
      setIncludeSQL( rep.getStepAttributeBoolean( idStep, "include_sql" ) );
      setSQLField( rep.getStepAttributeString( idStep, "sql_field" ) );
      setIncludeTimestamp( rep.getStepAttributeBoolean( idStep, "include_Timestamp" ) );
      setTimestampField( rep.getStepAttributeString( idStep, "timestamp_field" ) );
      setRowLimit( rep.getStepAttributeString( idStep, "limit" ) );
      setReadFrom( rep.getStepAttributeString( idStep, "read_from" ) );
      setReadTo( rep.getStepAttributeString( idStep, "read_to" ) );
      setRecordsFilter(
        SalesforceConnectionUtils.getRecordsFilterByCode( Const.NVL( rep.getStepAttributeString(
          idStep, "records_filter" ), "" ) ) );
      setQueryAll( rep.getStepAttributeBoolean( idStep, "queryAll" ) );

      int nrFields = rep.countNrStepAttributes( idStep, "field_name" );

      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        SalesforceInputField field = new SalesforceInputField();
        field.readRep( rep, metaStore, idStep, i );
        inputFields[i] = field;
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep ) throws KettleException {
    super.saveRep( rep, metaStore, idTransformation, idStep );
    try {
      // H.kawaguchi Add 19-01-2009
      rep.saveStepAttribute( idTransformation, idStep, "condition", getCondition() );
      // H.kawaguchi Add 19-01-2009
      rep.saveStepAttribute( idTransformation, idStep, "query", getQuery() );
      rep.saveStepAttribute( idTransformation, idStep, "specifyQuery", isSpecifyQuery() );

      rep.saveStepAttribute( idTransformation, idStep, "include_targeturl", includeTargetURL() );
      rep.saveStepAttribute( idTransformation, idStep, "targeturl_field", getTargetURLField() );
      rep.saveStepAttribute( idTransformation, idStep, "include_module", includeModule() );
      rep.saveStepAttribute( idTransformation, idStep, "module_field", getModuleField() );
      rep.saveStepAttribute( idTransformation, idStep, "include_rownum", includeRowNumber() );
      rep.saveStepAttribute( idTransformation, idStep, "include_deletion_date", includeDeletionDate() );

      rep.saveStepAttribute( idTransformation, idStep, "include_sql", includeSQL() );
      rep.saveStepAttribute( idTransformation, idStep, "sql_field", getSQLField() );
      rep.saveStepAttribute( idTransformation, idStep, "include_Timestamp", includeTimestamp() );
      rep.saveStepAttribute( idTransformation, idStep, "timestamp_field", getTimestampField() );
      rep.saveStepAttribute( idTransformation, idStep, "rownum_field", getRowNumberField() );
      rep.saveStepAttribute( idTransformation, idStep, "deletion_date_field", getDeletionDateField() );

      rep.saveStepAttribute( idTransformation, idStep, "limit", getRowLimit() );
      rep.saveStepAttribute( idTransformation, idStep, "read_from", getReadFrom() );
      rep.saveStepAttribute( idTransformation, idStep, "read_to", getReadTo() );
      rep.saveStepAttribute( idTransformation, idStep, "records_filter", SalesforceConnectionUtils
        .getRecordsFilterCode( getRecordsFilter() ) );
      rep.saveStepAttribute( idTransformation, idStep, "queryAll", isQueryAll() );

      for ( int i = 0; i < inputFields.length; i++ ) {
        SalesforceInputField field = inputFields[i];
        field.saveRep( rep, metaStore, idTransformation, idStep, i );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceInputMeta.Exception.ErrorSavingToRepository", "" + idStep ), e );
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
