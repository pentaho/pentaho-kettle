/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.Optional;
import java.util.Properties;

/**
 * https://docs.aws.amazon.com/athena/latest/ug/jdbc-v3-driver.html
 */
public class AthenaDatabaseMeta extends BaseDatabaseMeta {
  private static final String DEFAULT_WORKGROUP  = "primary";
  private static final String DEFAULT_REGION  = "us-east-1";
  private static final String DEFAULT_CATALOG  = "AwsDataCatalog";

  /** Attributes to read from the UI */
  private static final class Attributes {
    static final String WORKGROUP = "WORKGROUP";
    static final String REGION = "REGION";
    static final String CATALOG = "CATALOG";
    static final String OUTPUT_LOCATION = "OUTPUT_LOCATION";
    static final String AUTH_TYPE = "AUTH_TYPE";
    static final String PROFILE_NAME = "PROFILE_NAME";
  }

  /** Parameters for the JDBC URL */
  private static final class Params {
    static final String WORKGROUP = "WorkGroup";
    static final String REGION = "Region";
    static final String CATALOG = "Catalog";
    static final String DATABASE = "Database";
    static final String OUTPUT_LOCATION = "OutputLocation";
    static final String CREDENTIALS_PROVIDER = "CredentialsProvider";
    static final String PROFILE_NAME = "ProfileName";
  }

  public enum AuthType {
    DefaultChain,
    ProfileCredentials;

    public static AuthType defaultValue() {
      return AuthType.DefaultChain;
    }

    public static AuthType parse( String option ) {
      try {
        return valueOf( option );
      } catch ( Exception e ) {
        return defaultValue();
      }
    }
  }

  /** To reuse generic SQL generation */
  private final GenericDatabaseMeta fallback = new GenericDatabaseMeta();

  @Override
  public String getDriverClass() {
    return "com.amazon.athena.jdbc.AthenaDriver";
  }

  @Override
  public void setAttributes( Properties attributes ) {
    super.setAttributes( attributes );
    fallback.setAttributes( attributes );
  }

  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE };
  }

  @Override
  public int getDefaultDatabasePort() {
    return 443;
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append( "jdbc:athena://" );
    appendProperty( urlBuilder, Params.WORKGROUP, getMandatoryAttribute( Attributes.WORKGROUP ) );
    appendProperty( urlBuilder, Params.REGION, getMandatoryAttribute( Attributes.REGION ) );
    appendProperty( urlBuilder, Params.CATALOG, getMandatoryAttribute( Attributes.CATALOG ) );
    appendProperty( urlBuilder, Params.DATABASE, databaseName );
    appendProperty( urlBuilder, Params.OUTPUT_LOCATION, getMandatoryAttribute( Attributes.OUTPUT_LOCATION ) );
    appendProperty( urlBuilder, Params.CREDENTIALS_PROVIDER, getMandatoryAttribute( Attributes.AUTH_TYPE ) );
    if ( getAuthType() == AuthType.ProfileCredentials ) {
      appendProperty( urlBuilder, Params.PROFILE_NAME, getMandatoryAttribute( Attributes.PROFILE_NAME ) );
    }
    return urlBuilder.toString();
  }

  private void appendProperty( StringBuilder sb, String propName, String propValue ) {
    sb.append( ";" );
    sb.append( propName );
    sb.append( "=" );
    sb.append( propValue );
  }

  public String getWorkGroup() {
    return getAttribute( Attributes.WORKGROUP ).orElse( DEFAULT_WORKGROUP );
  }

  public void setWorkGroup( String workgroup ) {
    addAttribute( Attributes.WORKGROUP, workgroup );
  }

  public String getRegion() {
    return getAttribute( Attributes.REGION ).orElse( DEFAULT_REGION );
  }

  public void setRegion( String region ) {
    addAttribute( Attributes.REGION, region );
  }

  public String getCatalog() {
    return getAttribute( Attributes.CATALOG ).orElse( DEFAULT_CATALOG );
  }

  public void setCatalog( String catalog ) {
    addAttribute( Attributes.CATALOG, catalog );
  }

  public String getOutputLocation() {
    return getAttribute( Attributes.OUTPUT_LOCATION ).orElse( "" );
  }

  public void setOutputLocation( String outputLocation ) {
    addAttribute( Attributes.OUTPUT_LOCATION, outputLocation );
  }

  public AuthType getAuthType() {
    return getAttribute( Attributes.AUTH_TYPE ).map( AuthType::parse ).orElseGet( AuthType::defaultValue );
  }

  public void setAuthType( AuthType AuthType ){
    addAttribute( Attributes.AUTH_TYPE, AuthType.toString() );
  }

  public Optional<String> getProfileName() {
    return getAttribute( Attributes.PROFILE_NAME );
  }

  public void setProfileName( String profileName ) {
    addAttribute( Attributes.PROFILE_NAME, profileName );
  }

  public Optional<String> getAttribute( String name ){
    return Optional.ofNullable( getAttributes().getProperty( name ) );
  }

  private String getMandatoryAttribute( String name ) throws KettleDatabaseException {
    return getAttribute( name )
           .orElseThrow( () -> new KettleDatabaseException( String.format( "Missing mandatory attribute `%s`", name ) ) );
  }

  @Override
  public String getLimitClause( int nrRows ){
    return " LIMIT " + nrRows;
  }

  @Override
  public String getSQLQueryFields( String tableName ){
    return "SELECT * FROM " + tableName + getLimitClause( 0 );
  }

  @Override
  public String getXulOverlayFile(){
    return "athena";
  }

  @Override
  public String[] getReservedWords(){
    // parsed from https://docs.aws.amazon.com/athena/latest/ug/reserved-words.html
    return new String[] {
      "ALL", "ALTER", "AND", "ARRAY", "AS", "AUTHORIZATION", "BETWEEN", "BIGINT", "BINARY", "BOOLEAN", "BOTH", "BY",
      "CASE", "CASHE", "CAST", "CHAR", "COLUMN", "COMMIT", "CONF", "CONSTRAINT", "CREATE", "CROSS", "CUBE", "CURRENT",
      "CURRENT_CATALOG", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP",
      "CURRENT_USER", "CURSOR", "DATABASE", "DATE", "DAYOFWEEK", "DEALLOCATE", "DECIMAL", "DELETE", "DESCRIBE",
      "DISTINCT", "DOUBLE", "DROP", "ELSE", "END", "ESCAPE", "EXCEPT", "EXCHANGE", "EXECUTE", "EXISTS", "EXTENDED",
      "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIRST", "FLOAT", "FLOOR", "FOLLOWING", "FOR", "FOREIGN", "FROM",
      "FULL", "FUNCTION", "GRANT", "GROUP", "GROUPING", "HAVING", "IF", "IMPORT", "IN", "INNER", "INSERT", "INT",
      "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "JOIN", "JSON_ARRAY", "JSON_EXISTS", "JSON_OBJECT",
      "JSON_QUERY", "JSON_TABLE", "JSON_VALUE", "LAST", "LATERAL", "LEFT", "LESS", "LIKE", "LISTAGG", "LOCAL",
      "LOCALTIME", "LOCALTIMESTAMP", "MACRO", "MAP", "MORE", "NATURAL", "NONE", "NORMALIZE", "NOT", "NULL", "NUMERIC",
      "OF", "ON", "ONLY", "OR", "ORDER", "OUT", "OUTER", "OVER", "PARTIALSCAN", "PARTITION", "PERCENT", "PRECEDING",
      "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PROCEDURE", "RANGE", "READS", "RECURSIVE", "REDUCE",
      "REFERENCES", "REGEXP", "REVOKE", "RIGHT", "RLIKE", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "SELECT", "SET",
      "SKIP", "SMALLINT", "START", "TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TO", "TRANSFORM", "TRIGGER",
      "TRIM", "TRUE", "TRUNCATE", "UESCAPE", "UNBOUNDED", "UNION", "UNIQUEJOIN", "UNNEST", "UPDATE", "USER", "USING",
      "UTC_TIMESTAMP", "VALUES", "VARCHAR", "VIEWS", "WHEN", "WHERE", "WINDOW", "WITH" };
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ){
    return fallback.getFieldDefinition( v, tk, pk, useAutoinc, addFieldName, addCr );
  }

  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc, String pk,
                                       boolean semicolon ){
    return fallback.getAddColumnStatement( tablename, v, tk, useAutoinc, pk, semicolon );
  }

  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                          String pk, boolean semicolon ){
    return fallback.getModifyColumnStatement( tablename, v, tk, useAutoinc, pk, semicolon );
  }

  @Override
  public String[] getUsedLibraries(){
    return new String[] { };
  }

  /**
   * @return The start quote sequence, mostly just double quote, but sometimes [, ...
   */
  @Override
  public String getStartQuote(){
    return "`";
  }

  /**
   * @return The end quote sequence, mostly just double quote, but sometimes ], ...
   */
  @Override
  public String getEndQuote(){
    return "`";
  }
}
