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

package org.pentaho.di.trans.steps.gettablenames;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaString;
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

/*
 * Created on 03-Juin-2008
 *
 */
@InjectionSupported( localizationPrefix = "GetTableNames.Injection.", groups = { "FIELDS", "SETTINGS", "OUTPUT" } )
public class GetTableNamesMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = GetTableNamesMeta.class; // for i18n purposes, needed by Translator2!!

  /** database connection */
  private DatabaseMeta database;

  @Injection( name = "SCHEMANAME", group = "FIELDS" )
  private String schemaname;

  @Injection( name = "TABLENAMEFIELDNAME", group = "OUTPUT" )
  /** function result: new value name */
  private String tablenamefieldname;

  @Injection( name = "SQLCREATIONFIELDNAME", group = "OUTPUT" )
  private String sqlcreationfieldname;

  @Injection( name = "OBJECTTYPEFIELDNAME", group = "OUTPUT" )
  private String objecttypefieldname;

  @Injection( name = "ISSYSTEMOBJECTFIELDNAME", group = "OUTPUT" )
  private String issystemobjectfieldname;

  @Injection( name = "INCLUDECATALOG", group = "SETTINGS" )
  private boolean includeCatalog;

  @Injection( name = "INCLUDESCHEMA", group = "SETTINGS" )
  private boolean includeSchema;

  @Injection( name = "INCLUDETABLE", group = "SETTINGS" )
  private boolean includeTable;

  @Injection( name = "INCLUDEVIEW", group = "SETTINGS" )
  private boolean includeView;

  @Injection( name = "INCLUDEPROCEDURE", group = "SETTINGS" )
  private boolean includeProcedure;

  @Injection( name = "INCLUDESYNONYM", group = "SETTINGS" )
  private boolean includeSynonym;

  @Injection( name = "ADDSCHEMAINOUTPUT", group = "SETTINGS" )
  private boolean addSchemaInOutput;

  @Injection( name = "DYNAMICSCHEMA", group = "FIELDS" )
  private boolean dynamicSchema;

  @Injection( name = "SCHEMANAMEFIELD", group = "FIELDS" )
  private String schemaNameField;

  private List<? extends SharedObjectInterface> databases;

  public GetTableNamesMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabase() {
    return database;
  }

  /**
   * @param database
   *          The database to set.
   */
  public void setDatabase( DatabaseMeta database ) {
    this.database = database;
  }

  /**
   * @return Returns the resultName.
   */
  public String getTablenameFieldName() {
    return tablenamefieldname;
  }

  /**
   * @param tablenamefieldname
   *          The tablenamefieldname to set.
   */
  public void setTablenameFieldName( String tablenamefieldname ) {
    this.tablenamefieldname = tablenamefieldname;
  }

  /**
   * @return Returns the resultName.
   */
  public String getSQLCreationFieldName() {
    return sqlcreationfieldname;
  }

  /**
   * @param sqlcreationfieldname
   *          The sqlcreationfieldname to set.
   */
  public void setSQLCreationFieldName( String sqlcreationfieldname ) {
    this.sqlcreationfieldname = sqlcreationfieldname;
  }

  /**
   * @return Returns the resultName.
   */
  public String getSchemaName() {
    return schemaname;
  }

  /**
   * @param schemaname
   *          The schemaname to set.
   */
  public void setSchemaName( String schemaname ) {
    this.schemaname = schemaname;
  }

  /**
   * @param objecttypefieldname
   *          The objecttypefieldname to set.
   */
  public void setObjectTypeFieldName( String objecttypefieldname ) {
    this.objecttypefieldname = objecttypefieldname;
  }

  /**
   * @param issystemobjectfieldname
   *          The issystemobjectfieldname to set.
   */
  // TODO deprecate one of these
  public void setIsSystemObjectFieldName( String issystemobjectfieldname ) {
    this.issystemobjectfieldname = issystemobjectfieldname;
  }

  public void setSystemObjectFieldName( String issystemobjectfieldname ) {
    this.issystemobjectfieldname = issystemobjectfieldname;
  }

  /**
   * @return Returns the objecttypefieldname.
   */
  public String getObjectTypeFieldName() {
    return objecttypefieldname;
  }

  /**
   * @return Returns the issystemobjectfieldname.
   */
  public String isSystemObjectFieldName() {
    return issystemobjectfieldname;
  }

  /**
   * @return Returns the schenameNameField.
   */
  public String getSchemaFieldName() {
    return schemaNameField;
  }

  /**
   * @param schenameNameField
   *          The schemaNameField to set.
   */
  public void setSchemaFieldName( String schemaNameField ) {
    this.schemaNameField = schemaNameField;
  }

  public void setIncludeTable( boolean includetable ) {
    this.includeTable = includetable;
  }

  public boolean isIncludeTable() {
    return this.includeTable;
  }

  public void setIncludeSchema( boolean includeSchema ) {
    this.includeSchema = includeSchema;
  }

  public boolean isIncludeSchema() {
    return this.includeSchema;
  }

  public void setIncludeCatalog( boolean includeCatalog ) {
    this.includeCatalog = includeCatalog;
  }

  public boolean isIncludeCatalog() {
    return this.includeCatalog;
  }

  public void setIncludeView( boolean includeView ) {
    this.includeView = includeView;
  }

  public boolean isIncludeView() {
    return this.includeView;
  }

  public void setIncludeProcedure( boolean includeProcedure ) {
    this.includeProcedure = includeProcedure;
  }

  public boolean isIncludeProcedure() {
    return this.includeProcedure;
  }

  public void setIncludeSynonym( boolean includeSynonym ) {
    this.includeSynonym = includeSynonym;
  }

  public boolean isIncludeSynonym() {
    return this.includeSynonym;
  }

  public void setDynamicSchema( boolean dynamicSchema ) {
    this.dynamicSchema = dynamicSchema;
  }

  public boolean isDynamicSchema() {
    return this.dynamicSchema;
  }

  public void setAddSchemaInOut( boolean addSchemaInOutput ) {
    this.addSchemaInOutput = addSchemaInOutput;
  }

  public boolean isAddSchemaInOut() {
    return this.addSchemaInOutput;
  }

  @Injection( name = "CONNECTIONNAME" )
  public void setConnection( String connectionName ) {
    database = DatabaseMeta.findDatabase( databases, connectionName );
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    GetTableNamesMeta retval = (GetTableNamesMeta) super.clone();

    return retval;
  }

  public void setDefault() {
    database = null;
    schemaname = null;
    includeCatalog = false;
    includeSchema = false;
    includeTable = true;
    includeProcedure = true;
    includeView = true;
    includeSynonym = true;
    addSchemaInOutput = false;
    tablenamefieldname = "tablename";
    sqlcreationfieldname = null;
    objecttypefieldname = "type";
    issystemobjectfieldname = "is system";
    dynamicSchema = false;
    schemaNameField = null;
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    String realtablename = space.environmentSubstitute( tablenamefieldname );
    if ( !Utils.isEmpty( realtablename ) ) {
      ValueMetaInterface v = new ValueMetaString( realtablename );
      v.setLength( 500 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    String realObjectType = space.environmentSubstitute( objecttypefieldname );
    if ( !Utils.isEmpty( realObjectType ) ) {
      ValueMetaInterface v = new ValueMetaString( realObjectType );
      v.setLength( 500 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    String sysobject = space.environmentSubstitute( issystemobjectfieldname );
    if ( !Utils.isEmpty( sysobject ) ) {
      ValueMetaInterface v = new ValueMetaBoolean( sysobject );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    String realSQLCreation = space.environmentSubstitute( sqlcreationfieldname );
    if ( !Utils.isEmpty( realSQLCreation ) ) {
      ValueMetaInterface v = new ValueMetaString( realSQLCreation );
      v.setLength( 500 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( "connection", database == null ? "" : database.getName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "schemaname", schemaname ) );
    retval.append( "    " + XMLHandler.addTagValue( "tablenamefieldname", tablenamefieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "objecttypefieldname", objecttypefieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "issystemobjectfieldname", issystemobjectfieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "sqlcreationfieldname", sqlcreationfieldname ) );

    retval.append( "    " + XMLHandler.addTagValue( "includeCatalog", includeCatalog ) );
    retval.append( "    " + XMLHandler.addTagValue( "includeSchema", includeSchema ) );
    retval.append( "    " + XMLHandler.addTagValue( "includeTable", includeTable ) );
    retval.append( "    " + XMLHandler.addTagValue( "includeView", includeView ) );
    retval.append( "    " + XMLHandler.addTagValue( "includeProcedure", includeProcedure ) );
    retval.append( "    " + XMLHandler.addTagValue( "includeSynonym", includeSynonym ) );
    retval.append( "    " + XMLHandler.addTagValue( "addSchemaInOutput", addSchemaInOutput ) );
    retval.append( "    " + XMLHandler.addTagValue( "dynamicSchema", dynamicSchema ) );
    retval.append( "    " + XMLHandler.addTagValue( "schemaNameField", schemaNameField ) );

    return retval.toString();
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      this.databases = databases;
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      database = DatabaseMeta.findDatabase( databases, con );
      schemaname = XMLHandler.getTagValue( stepnode, "schemaname" );
      tablenamefieldname = XMLHandler.getTagValue( stepnode, "tablenamefieldname" );
      objecttypefieldname = XMLHandler.getTagValue( stepnode, "objecttypefieldname" );
      sqlcreationfieldname = XMLHandler.getTagValue( stepnode, "sqlcreationfieldname" );

      issystemobjectfieldname = XMLHandler.getTagValue( stepnode, "issystemobjectfieldname" );
      includeCatalog = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeCatalog" ) );
      includeSchema = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeSchema" ) );
      includeTable = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeTable" ) );
      includeView = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeView" ) );
      includeProcedure = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeProcedure" ) );
      includeSynonym = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includeSynonym" ) );
      addSchemaInOutput = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addSchemaInOutput" ) );
      dynamicSchema = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "dynamicSchema" ) );
      schemaNameField = XMLHandler.getTagValue( stepnode, "schemaNameField" );

      if ( XMLHandler.getTagValue( stepnode, "schenameNameField" ) != null ) {
        /*
         * Fix for wrong field name in the 7.0. Can be removed if we don't want to keep backward compatibility with 7.0
         * tranformations.
         */
        schemaNameField = XMLHandler.getTagValue( stepnode, "schenameNameField" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "GetTableNamesMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      this.databases = databases;
      database = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      schemaname = rep.getStepAttributeString( id_step, "schemaname" );
      tablenamefieldname = rep.getStepAttributeString( id_step, "tablenamefieldname" );
      objecttypefieldname = rep.getStepAttributeString( id_step, "objecttypefieldname" );
      sqlcreationfieldname = rep.getStepAttributeString( id_step, "sqlcreationfieldname" );

      issystemobjectfieldname = rep.getStepAttributeString( id_step, "issystemobjectfieldname" );
      includeCatalog = rep.getStepAttributeBoolean( id_step, "includeCatalog" );
      includeSchema = rep.getStepAttributeBoolean( id_step, "includeSchema" );
      includeTable = rep.getStepAttributeBoolean( id_step, "includeTable" );
      includeView = rep.getStepAttributeBoolean( id_step, "includeView" );
      includeProcedure = rep.getStepAttributeBoolean( id_step, "includeProcedure" );
      includeSynonym = rep.getStepAttributeBoolean( id_step, "includeSynonym" );
      addSchemaInOutput = rep.getStepAttributeBoolean( id_step, "addSchemaInOutput" );
      dynamicSchema = rep.getStepAttributeBoolean( id_step, "dynamicSchema" );
      schemaNameField = rep.getStepAttributeString( id_step, "schemaNameField" );

      if ( rep.getStepAttributeString( id_step, "schenameNameField" ) != null ) {
        /*
         * Fix for wrong field name in the 7.0. Can be removed if we don't want to keep backward compatibility with 7.0
         * tranformations.
         */
        schemaNameField = rep.getStepAttributeString( id_step, "schenameNameField" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "GetTableNamesMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", database );
      rep.saveStepAttribute( id_transformation, id_step, "schemaname", schemaname );
      rep.saveStepAttribute( id_transformation, id_step, "tablenamefieldname", tablenamefieldname );
      rep.saveStepAttribute( id_transformation, id_step, "objecttypefieldname", objecttypefieldname );
      rep.saveStepAttribute( id_transformation, id_step, "sqlcreationfieldname", sqlcreationfieldname );

      rep.saveStepAttribute( id_transformation, id_step, "issystemobjectfieldname", issystemobjectfieldname );
      // Also, save the step-database relationship!
      if ( database != null ) {
        rep.insertStepDatabase( id_transformation, id_step, database.getObjectId() );
      }
      rep.saveStepAttribute( id_transformation, id_step, "includeCatalog", includeCatalog );
      rep.saveStepAttribute( id_transformation, id_step, "includeSchema", includeSchema );
      rep.saveStepAttribute( id_transformation, id_step, "includeTable", includeTable );
      rep.saveStepAttribute( id_transformation, id_step, "includeView", includeView );
      rep.saveStepAttribute( id_transformation, id_step, "includeProcedure", includeProcedure );
      rep.saveStepAttribute( id_transformation, id_step, "includeSynonym", includeSynonym );
      rep.saveStepAttribute( id_transformation, id_step, "addSchemaInOutput", addSchemaInOutput );
      rep.saveStepAttribute( id_transformation, id_step, "dynamicSchema", dynamicSchema );
      rep.saveStepAttribute( id_transformation, id_step, "schemaNameField", schemaNameField );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GetTableNamesMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    if ( database == null ) {
      error_message = BaseMessages.getString( PKG, "GetTableNamesMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }
    if ( Utils.isEmpty( tablenamefieldname ) ) {
      error_message = BaseMessages.getString( PKG, "GetTableNamesMeta.CheckResult.TablenameFieldNameMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "GetTableNamesMeta.CheckResult.TablenameFieldNameOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 && !isDynamicSchema() ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "GetTableNamesMeta.CheckResult.NoInpuReceived" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "GetTableNamesMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
    }
    remarks.add( cr );

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new GetTableNames( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new GetTableNamesData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( database != null ) {
      return new DatabaseMeta[] { database };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  public boolean supportsErrorHandling() {
    return true;
  }
}
