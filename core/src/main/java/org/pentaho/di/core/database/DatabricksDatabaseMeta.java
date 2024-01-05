package org.pentaho.di.core.database;

import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * https://docs.databricks.com/en/integrations/jdbc-odbc-bi.html#jdbc-driver
 */
public class DatabricksDatabaseMeta extends BaseDatabaseMeta {

  /** Attributes to read from the UI */
  private static final class Attributes {
    public static final String TOKEN = "ACCESS_TOKEN";
    public static final String HTTP_PATH = "HTTP_PATH";
    public static final String AUTH_METHOD = "AUTH_METHOD";
  }

  /** Parameters for the JDBC URL */
  private static final class Params {
    static final String AUTH_MECH = "AuthMech";
    static final String USER = "UID";
    static final String PASS = "PWD";
    static final String HTTP_PATH = "httpPath";
    static final String CATALOG = "ConnCatalog";
  }

  public static enum AuthMethod {
    Credentials,
    Token;

    public static AuthMethod defaultValue() {
      return AuthMethod.Token;
    }

    public static AuthMethod parse( String option ) {
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
    return "com.databricks.client.jdbc.Driver";
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
    urlBuilder.append( "jdbc:databricks://" );
    urlBuilder.append( hostname );
    if ( StringUtils.isNotBlank( port ) ) {
      urlBuilder.append( ":" );
      urlBuilder.append( port );
    }
    appendProperty( urlBuilder, Params.HTTP_PATH, getMandatoryAttribute( Attributes.HTTP_PATH ) );
    if ( getAuthMethod() == AuthMethod.Token ) {
      // user/pass will be passed as properties if set
      appendProperty( urlBuilder, Params.AUTH_MECH, "3" );
      appendProperty( urlBuilder, Params.USER, "token" );
      appendProperty( urlBuilder, Params.PASS, Encr.decryptPassword( getMandatoryAttribute( Attributes.TOKEN ) ) );
    }
    if ( StringUtils.isNotBlank( databaseName ) ) {
      appendProperty( urlBuilder, Params.CATALOG, databaseName );
    }
    return urlBuilder.toString();
  }

  private void appendProperty( StringBuilder sb, String propName, String propValue ) {
    sb.append( ";" );
    sb.append( propName );
    sb.append( "=" );
    sb.append( propValue );
  }

  public AuthMethod getAuthMethod() {
    return getAttribute( Attributes.AUTH_METHOD ).map( AuthMethod::parse ).orElseGet( AuthMethod::defaultValue );
  }

  public void setAuthMethod( AuthMethod authMethod ) {
    addAttribute( Attributes.AUTH_METHOD, authMethod.toString() );
  }

  public Optional<String> getHttpPath() {
    return getAttribute( Attributes.HTTP_PATH );
  }

  public Optional<String> getToken() {
    return getAttribute( Attributes.TOKEN ).map( Encr::decryptPassword );
  }

  public void setToken( String token ) {
    addAttribute( Attributes.TOKEN, Encr.encryptPassword( token ) );
    // if there, user and pass will always be added to connection properties
    // and override token auth;
    setUsername( null );
    setPassword( null );
  }

  public void setHttpPath( String httpPath ) {
    addAttribute( Attributes.HTTP_PATH, httpPath );
  }

  public Optional<String> getAttribute( String name ) {
    return Optional.ofNullable( getAttributes().getProperty( name ) );
  }

  private String getMandatoryAttribute( String name ) throws KettleDatabaseException {
    return getAttribute( name )
        .orElseThrow( () -> new KettleDatabaseException( String.format( "Missing mandatory attribute `%s`", name ) ) );
  }

  @Override
  public String getLimitClause( int nrRows ) {
    return " LIMIT " + nrRows;
  }

  @Override
  public String getSQLQueryFields( String tableName ) {
    return "SELECT * FROM " + tableName + getLimitClause( 0 );
  }

  @Override
  public String getXulOverlayFile() {
    return "databricks";
  }

  @Override
  public String[] getReservedWords() {
    // superset of all reserved words in SQL and Hive modes
    // parsed from https://docs.databricks.com/en/sql/language-manual/sql-ref-ansi-compliance.html#sql-keywords
    return new String[] {
      "ADD", "AFTER", "ALL", "ALTER", "ALWAYS", "ANALYZE", "AND", "ANTI", "ANY", "ARCHIVE", "ARRAY", "AS", "ASC", "AT",
      "AUTHORIZATION", "BETWEEN", "BOTH", "BUCKET", "BUCKETS", "BY", "CACHE", "CASCADE", "CASE", "CAST", "CHANGE",
      "CHECK", "CLEAR", "CLUSTER", "CLUSTERED", "CODEGEN", "COLLATE", "COLLECTION", "COLUMN", "COLUMNS", "COMMENT",
      "COMMIT", "COMPACT", "COMPACTIONS", "COMPUTE", "CONCATENATE", "CONSTRAINT", "COST", "CREATE", "CROSS", "CUBE",
      "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DATA", "DATABASE", "DATABASES",
      "DAY", "DBPROPERTIES", "DEFINED", "DELETE", "DELIMITED", "DESC", "DESCRIBE", "DFS", "DIRECTORIES", "DIRECTORY",
      "DISTINCT", "DISTRIBUTE", "DIV", "DROP", "ELSE", "END", "ESCAPE", "ESCAPED", "EXCEPT", "EXCHANGE", "EXISTS",
      "EXPLAIN", "EXPORT", "EXTENDED", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIELDS", "FILTER", "FILEFORMAT",
      "FIRST", "FN", "FOLLOWING", "FOR", "FOREIGN", "FORMAT", "FORMATTED", "FROM", "FULL", "FUNCTION", "FUNCTIONS",
      "GENERATED", "GLOBAL", "GRANT", "GRANTS", "GROUP", "GROUPING", "HAVING", "HOUR", "IF", "IGNORE", "IMPORT", "IN",
      "INDEX", "INDEXES", "INNER", "INPATH", "INPUTFORMAT", "INSERT", "INTERSECT", "INTERVAL", "INTO", "IS", "ITEMS",
      "JOIN", "KEY", "KEYS", "LAST", "LATERAL", "LAZY", "LEADING", "LEFT", "LIKE", "ILIKE", "LIMIT", "LINES", "LIST",
      "LOAD", "LOCAL", "LOCATION", "LOCK", "LOCKS", "LOGICAL", "MACRO", "MAP", "MATCHED", "MERGE", "MINUTE", "MINUS",
      "MONTH", "MSCK", "NAMESPACE", "NAMESPACES", "NATURAL", "NO", "NOT", "NULL", "NULLS", "OF", "ON", "ONLY", "OPTION",
      "OPTIONS", "OR", "ORDER", "OUT", "OUTER", "OUTPUTFORMAT", "OVER", "OVERLAPS", "OVERLAY", "OVERWRITE", "PARTITION",
      "PARTITIONED", "PARTITIONS", "PERCENT", "PIVOT", "PLACING", "POSITION", "PRECEDING", "PRIMARY", "PRINCIPALS",
      "PROPERTIES", "PURGE", "QUALIFY", "QUERY", "RANGE", "RECIPIENT", "RECIPIENTS", "RECORDREADER", "RECORDWRITER",
      "RECOVER", "REDUCE", "REFERENCES", "REFRESH", "REGEXP", "REMOVE", "RENAME", "REPAIR", "REPLACE", "RESET",
      "RESPECT", "RESTRICT", "REVOKE", "RIGHT", "RLIKE", "ROLE", "ROLES", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "SCHEMA",
      "SCHEMAS", "SECOND", "SELECT", "SEMI", "SEPARATED", "SERDE", "SERDEPROPERTIES", "SESSION_USER", "SET", "SETS",
      "SHARE", "SHARES", "SHOW", "SKEWED", "SOME", "SORT", "SORTED", "START", "STATISTICS", "STORED", "STRATIFY",
      "STRUCT", "SUBSTR", "SUBSTRING", "SYNC", "TABLE", "TABLES", "TABLESAMPLE", "TBLPROPERTIES", "TEMP", "TEMPORARY",
      "TERMINATED", "THEN", "TIME", "TO", "TOUCH", "TRAILING", "TRANSACTION", "TRANSACTIONS", "TRANSFORM", "TRIM",
      "TRUE", "TRUNCATE", "TRY_CAST", "TYPE", "UNARCHIVE", "UNBOUNDED", "UNCACHE", "UNION", "UNIQUE", "UNKNOWN",
      "UNLOCK", "UNSET", "UPDATE", "USE", "USER", "USING", "VALUES", "VIEW", "VIEWS", "WHEN", "WHERE", "WINDOW", "WITH",
      "YEAR", "ZONE" };
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
      boolean addFieldName, boolean addCr ) {
    return fallback.getFieldDefinition( v, tk, pk, useAutoinc, addFieldName, addCr );
  }

  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc, String pk,
      boolean semicolon ) {
    return fallback.getAddColumnStatement( tablename, v, tk, useAutoinc, pk, semicolon );
  }

  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
      String pk, boolean semicolon ) {
    return fallback.getModifyColumnStatement( tablename, v, tk, useAutoinc, pk, semicolon );
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] {};
  }

  /**
   * @return The start quote sequence, mostly just double quote, but sometimes [, ...
   */
  @Override
  public String getStartQuote() {
    return "`";
  }

  /**
   * @return The end quote sequence, mostly just double quote, but sometimes ], ...
   */
  @Override
  public String getEndQuote() {
    return "`";
  }
}
