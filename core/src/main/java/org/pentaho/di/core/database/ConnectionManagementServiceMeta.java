package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Acts as a placeholder for the Connection Management Service connection, which is not a real database, but needs to be
 * acting as one in the internal flow.
 * <p>
 * Internally contains an instance of a real database and proxies all calls to it after it's data is fetched from the
 * connection management service.
 */
public class ConnectionManagementServiceMeta extends BaseDatabaseMeta implements DatabaseInterface {

  private DatabaseInterface delegate;

  public boolean isDataLoaded() {
    return delegate != null;
  }

  public DatabaseInterface getDelegateDatabaseInterface() {
    return delegate;
  }

  public void setDelegateDatabaseInterface( DatabaseInterface delegate ) {
    this.delegate = delegate;
  }

  @Override public String getPluginId() {
    return delegate == null ? null : delegate.getPluginId();
  }

  @Override public void setPluginId( String pluginId ) {
    if ( delegate != null ) {
      delegate.setPluginId( pluginId );
    }
  }

  @Override public String getPluginName() {
    return delegate == null ? null : delegate.getPluginName();
  }

  @Override public void setPluginName( String pluginName ) {
    if ( delegate != null ) {
      delegate.setPluginName( pluginName );
    }
  }

  @Override public int getAccessType() {
    return delegate == null ? 0 : delegate.getAccessType();
  }

  @Override public void setAccessType( int accessType ) {
    if ( delegate != null ) {
      delegate.setAccessType( accessType );
    }
  }

  @Override public boolean isChanged() {
    return delegate != null && delegate.isChanged();
  }

  @Override public void setChanged( boolean changed ) {
    if ( delegate != null ) {
      delegate.setChanged( changed );
    }
  }

  @Override public String getDatabaseName() {
    return delegate == null ? null : delegate.getDatabaseName();
  }

  @Override public void setDatabaseName( String databaseName ) {
    if ( delegate != null ) {
      delegate.setDatabaseName( databaseName );
    }
  }

  @Override public String getDatabasePortNumberString() {
    return delegate == null ? null : delegate.getDatabasePortNumberString();
  }

  @Override public void setDatabasePortNumberString( String databasePortNumberString ) {
    if ( delegate != null ) {
      delegate.setDatabasePortNumberString( databasePortNumberString );
    }
  }

  @Override public String getHostname() {
    return delegate == null ? null : delegate.getHostname();
  }

  @Override public void setHostname( String hostname ) {
    if ( delegate != null ) {
      delegate.setHostname( hostname );
    }
  }

  @Override public String getUsername() {
    return delegate == null ? null : delegate.getUsername();
  }

  @Override public void setUsername( String username ) {
    if ( delegate != null ) {
      delegate.setUsername( username );
    }
  }

  @Override public String getPassword() {
    return delegate == null ? null : delegate.getPassword();
  }

  @Override public void setPassword( String password ) {
    if ( delegate != null ) {
      delegate.setPassword( password );
    }
  }

  @Override public String getServername() {
    return delegate == null ? null : delegate.getServername();
  }

  @Override public void setServername( String servername ) {
    if ( delegate != null ) {
      delegate.setServername( servername );
    }
  }

  @Override public String getDataTablespace() {
    return delegate == null ? null : delegate.getDataTablespace();
  }

  @Override public void setDataTablespace( String dataTablespace ) {
    if ( delegate != null ) {
      delegate.setDataTablespace( dataTablespace );
    }
  }

  @Override public String getIndexTablespace() {
    return delegate == null ? null : delegate.getIndexTablespace();
  }

  @Override public void setIndexTablespace( String indexTablespace ) {
    if ( delegate != null ) {
      delegate.setIndexTablespace( indexTablespace );
    }
  }

  @Override public Properties getAttributes() {
    return delegate == null ? super.getAttributes() : delegate.getAttributes();
  }

  @Override public void setAttributes( Properties attributes ) {
    if ( delegate != null ) {
      delegate.setAttributes( attributes );
    } else {
      super.setAttributes( attributes );
    }
  }

  @Override public void addAttribute( String attributeId, String value ) {
    if ( delegate != null ) {
      delegate.addAttribute( attributeId, value );
    }
  }

  @Override public String getAttribute( String attributeId, String defaultValue ) {
    return delegate == null ? null : delegate.getAttribute( attributeId, defaultValue );
  }

  @Override public boolean supportsSetCharacterStream() {
    return delegate != null && delegate.supportsSetCharacterStream();
  }

  @Override public boolean supportsAutoInc() {
    return delegate != null && delegate.supportsAutoInc();
  }

  @Override public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                              boolean addFieldName, boolean addCr ) {
    return delegate == null ? null : delegate.getFieldDefinition( v, tk, pk, useAutoinc, addFieldName, addCr );
  }

  @Override public int[] getAccessTypeList() {
    return delegate == null ? new int[ 0 ] : delegate.getAccessTypeList();
  }

  @Override public int getDefaultDatabasePort() {
    return delegate == null ? 0 : delegate.getDefaultDatabasePort();
  }

  @Override public Map<String, String> getDefaultOptions() {
    return delegate == null ? null : delegate.getDefaultOptions();
  }

  @Override public String getLimitClause( int nrRows ) {
    return delegate == null ? null : delegate.getLimitClause( nrRows );
  }

  @Override public String getSQLQueryFields( String tableName ) {
    return delegate == null ? null : delegate.getSQLQueryFields( tableName );
  }

  @Override public int getNotFoundTK( boolean useAutoinc ) {
    return delegate == null ? 0 : delegate.getNotFoundTK( useAutoinc );
  }

  @Override public String getDriverClass() {
    return delegate == null ? null : delegate.getDriverClass();
  }

  @Override public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
    return delegate == null ? null : delegate.getURL( hostname, port, databaseName );
  }

  @Override public boolean supportsSequences() {
    return delegate != null && delegate.supportsSequences();
  }

  @Override public String getSQLNextSequenceValue( String sequenceName ) {
    return delegate == null ? null : delegate.getSQLNextSequenceValue( sequenceName );
  }

  @Override public String getSQLCurrentSequenceValue( String sequenceName ) {
    return delegate == null ? null : delegate.getSQLCurrentSequenceValue( sequenceName );
  }

  @Override public String getSQLSequenceExists( String sequenceName ) {
    return delegate == null ? null : delegate.getSQLSequenceExists( sequenceName );
  }

  @Override public boolean isFetchSizeSupported() {
    return delegate != null && delegate.isFetchSizeSupported();
  }

  @Override public boolean supportsTransactions() {
    return delegate != null && delegate.supportsTransactions();
  }

  @Override public boolean supportsBitmapIndex() {
    return delegate != null && delegate.supportsBitmapIndex();
  }

  @Override public boolean supportsIndexes() {
    return delegate != null && delegate.supportsIndexes();
  }

  @Override public boolean supportsSetLong() {
    return delegate != null && delegate.supportsSetLong();
  }

  @Override public boolean supportsSchemas() {
    return delegate != null && delegate.supportsSchemas();
  }

  @Override public boolean supportsCatalogs() {
    return delegate != null && delegate.supportsCatalogs();
  }

  @Override public boolean supportsEmptyTransactions() {
    return delegate != null && delegate.supportsEmptyTransactions();
  }

  @Override public boolean needsPlaceHolder() {
    return delegate != null && delegate.needsPlaceHolder();
  }

  @Override public String getFunctionSum() {
    return delegate == null ? null : delegate.getFunctionSum();
  }

  @Override public String getFunctionAverage() {
    return delegate == null ? null : delegate.getFunctionAverage();
  }

  @Override public String getFunctionMinimum() {
    return delegate == null ? null : delegate.getFunctionMinimum();
  }

  @Override public String getFunctionMaximum() {
    return delegate == null ? null : delegate.getFunctionMaximum();
  }

  @Override public String getFunctionCount() {
    return delegate == null ? null : delegate.getFunctionCount();
  }

  @Override public String getSchemaTableCombination( String schemaName, String tablePart ) {
    return delegate == null ? null : delegate.getSchemaTableCombination( schemaName, tablePart );
  }

  @Override public int getMaxTextFieldLength() {
    return delegate == null ? 0 : delegate.getMaxTextFieldLength();
  }

  @Override public int getMaxVARCHARLength() {
    return delegate == null ? 0 : delegate.getMaxVARCHARLength();
  }

  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc, String pk,
                                       boolean semicolon ) {
    return delegate == null ? null : delegate.getAddColumnStatement( tablename, v, tk, useAutoinc, pk, semicolon );
  }

  @Override public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                                  String pk, boolean semicolon ) {
    return delegate == null ? null : delegate.getDropColumnStatement( tablename, v, tk, useAutoinc, pk, semicolon );
  }

  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                          String pk, boolean semicolon ) {
    return delegate == null ? null : delegate.getModifyColumnStatement( tablename, v, tk, useAutoinc, pk, semicolon );
  }

  @Override public String[] getReservedWords() {
    return delegate == null ? new String[ 0 ] : delegate.getReservedWords();
  }

  @Override public boolean quoteReservedWords() {
    return delegate != null && delegate.quoteReservedWords();
  }

  @Override public String getStartQuote() {
    return delegate == null ? null : delegate.getStartQuote();
  }

  @Override public String getEndQuote() {
    return delegate == null ? null : delegate.getEndQuote();
  }

  @Override public boolean supportsRepository() {
    return delegate != null && delegate.supportsRepository();
  }

  @Override public String[] getTableTypes() {
    return delegate == null ? null : delegate.getTableTypes();
  }

  @Override public String[] getViewTypes() {
    return delegate == null ? null : delegate.getViewTypes();
  }

  @Override public String[] getSynonymTypes() {
    return delegate == null ? null : delegate.getSynonymTypes();
  }

  @Override public boolean useSchemaNameForTableList() {
    return delegate != null && delegate.useSchemaNameForTableList();
  }

  @Override public boolean supportsViews() {
    return delegate != null && delegate.supportsViews();
  }

  @Override public boolean supportsSynonyms() {
    return delegate != null && delegate.supportsSynonyms();
  }

  @Override public String getSQLListOfProcedures() {
    return delegate == null ? null : delegate.getSQLListOfProcedures();
  }

  @Override public String getTruncateTableStatement( String tableName ) {
    return delegate == null ? null : delegate.getTruncateTableStatement( tableName );
  }

  @Override public boolean supportsFloatRoundingOnUpdate() {
    return delegate != null && delegate.supportsFloatRoundingOnUpdate();
  }

  @Override public String getSQLLockTables( String[] tableNames ) {
    return delegate == null ? null : delegate.getSQLLockTables( tableNames );
  }

  @Override public String getSQLUnlockTables( String[] tableNames ) {
    return delegate == null ? null : delegate.getSQLUnlockTables( tableNames );
  }

  @Override public boolean supportsTimeStampToDateConversion() {
    return delegate != null && delegate.supportsTimeStampToDateConversion();
  }

  @Override public boolean supportsBatchUpdates() {
    return delegate != null && delegate.supportsBatchUpdates();
  }

  @Override public boolean supportsBooleanDataType() {
    return delegate != null && delegate.supportsBooleanDataType();
  }

  @Override public void setSupportsBooleanDataType( boolean b ) {
    if ( delegate != null ) {
      delegate.setSupportsBooleanDataType( b );
    }
  }

  @Override public boolean preserveReservedCase() {
    return delegate != null && delegate.preserveReservedCase();
  }

  @Override public void setPreserveReservedCase( boolean b ) {
    if ( delegate != null ) {
      delegate.setPreserveReservedCase( b );
    }
  }

  @Override public boolean isDefaultingToUppercase() {
    return delegate != null && delegate.isDefaultingToUppercase();
  }

  @Override public Map<String, String> getExtraOptions() {
    return delegate == null ? super.getExtraOptions() : delegate.getExtraOptions();
  }

  @Override public void addExtraOption( String databaseTypeCode, String option, String value ) {
    if ( delegate != null ) {
      delegate.addExtraOption( databaseTypeCode, option, value );
    } else {
      super.addExtraOption( databaseTypeCode, option, value );
    }
  }

  @Override public String getExtraOptionSeparator() {
    return delegate == null ? null : delegate.getExtraOptionSeparator();
  }

  @Override public String getExtraOptionValueSeparator() {
    return delegate == null ? null : delegate.getExtraOptionValueSeparator();
  }

  @Override public String getExtraOptionIndicator() {
    return delegate == null ? null : delegate.getExtraOptionIndicator();
  }

  @Override public boolean supportsOptionsInURL() {
    return delegate != null && delegate.supportsOptionsInURL();
  }

  @Override public String getExtraOptionsHelpText() {
    return delegate == null ? null : delegate.getExtraOptionsHelpText();
  }

  @Override public boolean supportsGetBlob() {
    return delegate != null && delegate.supportsGetBlob();
  }

  @Override public String getConnectSQL() {
    return delegate == null ? null : delegate.getConnectSQL();
  }

  @Override public void setConnectSQL( String sql ) {
    if ( delegate != null ) {
      delegate.setConnectSQL( sql );
    }
  }

  @Override public boolean supportsSetMaxRows() {
    return delegate != null && delegate.supportsSetMaxRows();
  }

  @Override public boolean isUsingConnectionPool() {
    return delegate != null && delegate.isUsingConnectionPool();
  }

  @Override public void setUsingConnectionPool( boolean usePool ) {
    if ( delegate != null ) {
      delegate.setUsingConnectionPool( usePool );
    }
  }

  @Override public int getMaximumPoolSize() {
    return delegate == null ? 0 : delegate.getMaximumPoolSize();
  }

  @Override public void setMaximumPoolSize( int maximumPoolSize ) {
    if ( delegate != null ) {
      delegate.setMaximumPoolSize( maximumPoolSize );
    }
  }

  @Override public String getMaximumPoolSizeString() {
    return delegate == null ? "0" : delegate.getMaximumPoolSizeString();
  }

  @Override public void setMaximumPoolSizeString( String maximumPoolSize ) {
    if ( delegate != null ) {
      delegate.setMaximumPoolSizeString( maximumPoolSize );
    }
  }

  @Override public int getInitialPoolSize() {
    return delegate == null ? 0 : delegate.getInitialPoolSize();
  }

  @Override public void setInitialPoolSize( int initalPoolSize ) {
    if ( delegate != null ) {
      delegate.setInitialPoolSize( initalPoolSize );
    }
  }

  @Override public String getInitialPoolSizeString() {
    return delegate == null ? "0" : delegate.getInitialPoolSizeString();
  }

  @Override public void setInitialPoolSizeString( String initialPoolSize ) {
    if ( delegate != null ) {
      delegate.setInitialPoolSizeString( initialPoolSize );
    }
  }

  @Override public boolean isPartitioned() {
    return delegate != null && delegate.isPartitioned();
  }

  @Override public void setPartitioned( boolean partitioned ) {
    if ( delegate != null ) {
      delegate.setPartitioned( partitioned );
    }
  }

  @Override public PartitionDatabaseMeta[] getPartitioningInformation() {
    return delegate == null ? null : delegate.getPartitioningInformation();
  }

  @Override public void setPartitioningInformation( PartitionDatabaseMeta[] partitionInfo ) {
    if ( delegate != null ) {
      delegate.setPartitioningInformation( partitionInfo );
    }
  }

  @Override public String[] getUsedLibraries() {
    return delegate == null ? new String[ 0 ] : delegate.getUsedLibraries();
  }

  @Override public Properties getConnectionPoolingProperties() {
    return delegate == null ? null : delegate.getConnectionPoolingProperties();
  }

  @Override public void setConnectionPoolingProperties( Properties properties ) {
    if ( delegate != null ) {
      delegate.setConnectionPoolingProperties( properties );
    }
  }

  @Override public String getSQLTableExists( String tablename ) {
    return delegate == null ? null : delegate.getSQLTableExists( tablename );
  }

  @Override public String getSQLColumnExists( String column, String tablename ) {
    return delegate == null ? null : delegate.getSQLColumnExists( column, tablename );
  }

  @Override public boolean needsToLockAllTables() {
    return delegate != null && delegate.needsToLockAllTables();
  }

  @Override public boolean isStreamingResults() {
    return delegate != null && delegate.isStreamingResults();
  }

  @Override public void setStreamingResults( boolean useStreaming ) {
    if ( delegate != null ) {
      delegate.setStreamingResults( useStreaming );
    }
  }

  @Override public boolean isQuoteAllFields() {
    return delegate != null && delegate.isQuoteAllFields();
  }

  @Override public void setQuoteAllFields( boolean quoteAllFields ) {
    if ( delegate != null ) {
      delegate.setQuoteAllFields( quoteAllFields );
    }
  }

  @Override public boolean isForcingIdentifiersToLowerCase() {
    return delegate != null && delegate.isForcingIdentifiersToLowerCase();
  }

  @Override public void setForcingIdentifiersToLowerCase( boolean forceLowerCase ) {
    if ( delegate != null ) {
      delegate.setForcingIdentifiersToLowerCase( forceLowerCase );
    }
  }

  @Override public boolean isForcingIdentifiersToUpperCase() {
    return delegate != null && delegate.isForcingIdentifiersToUpperCase();
  }

  @Override public void setForcingIdentifiersToUpperCase( boolean forceUpperCase ) {
    if ( delegate != null ) {
      delegate.setForcingIdentifiersToUpperCase( forceUpperCase );
    }
  }

  @Override public boolean isUsingDoubleDecimalAsSchemaTableSeparator() {
    return delegate != null && delegate.isUsingDoubleDecimalAsSchemaTableSeparator();
  }

  @Override public void setUsingDoubleDecimalAsSchemaTableSeparator( boolean useDoubleDecimalSeparator ) {
    if ( delegate != null ) {
      delegate.setUsingDoubleDecimalAsSchemaTableSeparator( useDoubleDecimalSeparator );
    }
  }

  @Override public boolean isRequiringTransactionsOnQueries() {
    return delegate != null && delegate.isRequiringTransactionsOnQueries();
  }

  @Override public String getDatabaseFactoryName() {
    return delegate == null ? null : delegate.getDatabaseFactoryName();
  }

  @Override public String getPreferredSchemaName() {
    return delegate == null ? null : delegate.getPreferredSchemaName();
  }

  @Override public void setPreferredSchemaName( String preferredSchemaName ) {
    if ( delegate != null ) {
      delegate.setPreferredSchemaName( preferredSchemaName );
    }
  }

  @Override
  public boolean checkIndexExists( Database database, String schemaName, String tableName, String[] idxFields )
    throws KettleDatabaseException {
    return delegate != null && delegate.checkIndexExists( database, schemaName, tableName, idxFields );
  }

  @Override public boolean supportsSequenceNoMaxValueOption() {
    return delegate != null && delegate.supportsSequenceNoMaxValueOption();
  }

  @Override public boolean requiresCreateTablePrimaryKeyAppend() {
    return delegate != null && delegate.requiresCreateTablePrimaryKeyAppend();
  }

  @Override public boolean requiresCastToVariousForIsNull() {
    return delegate != null && delegate.requiresCastToVariousForIsNull();
  }

  @Override public boolean isDisplaySizeTwiceThePrecision() {
    return delegate != null && delegate.isDisplaySizeTwiceThePrecision();
  }

  @Override public boolean supportsPreparedStatementMetadataRetrieval() {
    return delegate != null && delegate.supportsPreparedStatementMetadataRetrieval();
  }

  @Override public boolean isSystemTable( String tableName ) {
    return delegate != null && delegate.isSystemTable( tableName );
  }

  @Override public boolean supportsNewLinesInSQL() {
    return delegate != null && delegate.supportsNewLinesInSQL();
  }

  @Override public String getSQLListOfSchemas() {
    return delegate == null ? null : delegate.getSQLListOfSchemas();
  }

  @Override public String getSQLListOfSchemas( DatabaseMeta dbMeta ) {
    return delegate == null ? null : delegate.getSQLListOfSchemas( dbMeta );
  }

  @Override public int getMaxColumnsInIndex() {
    return delegate == null ? 0 : delegate.getMaxColumnsInIndex();
  }

  @Override public boolean supportsErrorHandlingOnBatchUpdates() {
    return delegate != null && delegate.supportsErrorHandlingOnBatchUpdates();
  }

  @Override
  public String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField, String versionField ) {
    return delegate == null ? null :
      delegate.getSQLInsertAutoIncUnknownDimensionRow( schemaTable, keyField, versionField );
  }

  @Override public boolean isExplorable() {
    return delegate != null && delegate.isExplorable();
  }

  @Override public String getXulOverlayFile() {
    return delegate == null ? null : delegate.getXulOverlayFile();
  }

  @Override public String getSQLListOfSequences() {
    return delegate == null ? null : delegate.getSQLListOfSequences();
  }

  @Override public String quoteSQLString( String string ) {
    if ( delegate != null ) {
      return delegate.quoteSQLString( string );
    } else {
      throw new IllegalStateException( "Delegate is null" );
    }
  }

  @Override public String getSelectCountStatement( String tableName ) {
    return delegate == null ? null : delegate.getSelectCountStatement( tableName );
  }

  @Override public String generateColumnAlias( int columnIndex, String suggestedName ) {
    if ( delegate != null ) {
      return delegate.generateColumnAlias( columnIndex, suggestedName );
    } else {
      throw new IllegalStateException( "Delegate is null" );
    }
  }

  @Override public List<String> parseStatements( String sqlScript ) {
    if ( delegate != null ) {
      return delegate.parseStatements( sqlScript );
    } else {
      throw new IllegalStateException( "Delegate is null" );
    }
  }

  @Override public List<SqlScriptStatement> getSqlScriptStatements( String sqlScript ) {
    return delegate == null ? null : delegate.getSqlScriptStatements( sqlScript );
  }

  @Override public boolean isMySQLVariant() {
    return delegate != null && delegate.isMySQLVariant();
  }

  @Override public boolean releaseSavepoint() {
    return delegate != null && delegate.releaseSavepoint();
  }

  @Override
  public Long getNextBatchId( DatabaseMeta dbm, Database ldb, String schemaName, String tableName, String fieldName )
    throws KettleDatabaseException {
    return delegate == null ? null : delegate.getNextBatchId( dbm, ldb, schemaName, tableName, fieldName );
  }

  @Override public String getDataTablespaceDDL( VariableSpace variables, DatabaseMeta databaseMeta ) {
    return delegate == null ? null : delegate.getDataTablespaceDDL( variables, databaseMeta );
  }

  @Override public String getIndexTablespaceDDL( VariableSpace variables, DatabaseMeta databaseMeta ) {
    return delegate == null ? null : delegate.getIndexTablespaceDDL( variables, databaseMeta );
  }

  @Override public Object getValueFromResultSet( ResultSet resultSet, ValueMetaInterface valueMeta, int index )
    throws KettleDatabaseException {
    return delegate == null ? null : delegate.getValueFromResultSet( resultSet, valueMeta, index );
  }

  @Override public boolean useSafePoints() {
    return delegate != null && delegate.useSafePoints();
  }

  @Override public boolean supportsErrorHandling() {
    return delegate != null && delegate.supportsErrorHandling();
  }

  @Override public String getSQLValue( ValueMetaInterface valueMeta, Object valueData, String dateFormat )
    throws KettleValueException {
    return delegate == null ? null : delegate.getSQLValue( valueMeta, valueData, dateFormat );
  }

  @Override public boolean supportsResultSetMetadataRetrievalOnly() {
    return delegate != null && delegate.supportsResultSetMetadataRetrievalOnly();
  }

  @Override public boolean supportsTimestampDataType() {
    return delegate != null && delegate.supportsTimestampDataType();
  }

  @Override public void setSupportsTimestampDataType( boolean b ) {
    if ( delegate != null ) {
      delegate.setSupportsTimestampDataType( b );
    }
  }

  @Override public String getSafeFieldname( String fieldName ) {
    return delegate == null ? null : delegate.getSafeFieldname( fieldName );
  }

  @Override public String getSequenceNoMaxValueOption() {
    return delegate == null ? null : delegate.getSequenceNoMaxValueOption();
  }

  @Override public boolean supportsAutoGeneratedKeys() {
    return delegate != null && delegate.supportsAutoGeneratedKeys();
  }

  @Override public ValueMetaInterface customizeValueFromSQLType( ValueMetaInterface v, ResultSetMetaData rm, int index )
    throws SQLException {
    if ( delegate != null ) {
      return delegate.customizeValueFromSQLType( v, rm, index );
    } else {
      throw new IllegalStateException( "Delegate is null" );
    }
  }

  @Override public String getCreateTableStatement() {
    return delegate == null ? null : delegate.getCreateTableStatement();
  }

  @Override @Deprecated public void addDefaultOptions() {
    if ( delegate != null ) {
      delegate.addDefaultOptions();
    }
  }

  @Override public SqlScriptParser createSqlScriptParser() {
    if ( delegate != null ) {
      return delegate.createSqlScriptParser();
    } else {
      throw new IllegalStateException( "Delegate is null" );
    }
  }

  @Override public boolean supportsStandardTableOutput() {
    return delegate != null && delegate.supportsStandardTableOutput();
  }

  @Override public String getUnsupportedTableOutputMessage() {
    return delegate == null ? null : delegate.getUnsupportedTableOutputMessage();
  }

  @Override public String getLegacyColumnName( DatabaseMetaData dbMetaData, ResultSetMetaData rsMetaData, int index )
    throws KettleDatabaseException {
    return delegate == null ? null : delegate.getLegacyColumnName( dbMetaData, rsMetaData, index );
  }

  @Override public void putOptionalOptions( Map<String, String> extraOptions ) {
    if ( delegate != null ) {
      delegate.putOptionalOptions( extraOptions );
    }
  }

  @Override public ResultSet getSchemas( DatabaseMetaData databaseMetaData, DatabaseMeta dbMeta ) throws SQLException {
    return delegate == null ? null : delegate.getSchemas( databaseMetaData, dbMeta );
  }

  @Override public ResultSet getTables( DatabaseMetaData databaseMetaData, DatabaseMeta dbMeta, String schemaPattern,
                                        String tableNamePattern, String[] tableTypes ) throws SQLException {
    return delegate == null ? null :
      delegate.getTables( databaseMetaData, dbMeta, schemaPattern, tableNamePattern, tableTypes );
  }

  @Override public String getNamedCluster() {
    return delegate == null ? null : delegate.getNamedCluster();
  }

  @Override public void setNamedCluster( String namedCluster ) {
    if ( delegate != null ) {
      delegate.setNamedCluster( namedCluster );
    }
  }

  @Override public List<String> getNamedClusterList() {
    return delegate == null ? null : delegate.getNamedClusterList();
  }

  @Override public void setConnectionSpecificInfoFromAttributes( Map<String, String> attributes ) {
    if ( delegate != null ) {
      delegate.setConnectionSpecificInfoFromAttributes( attributes );
    }
  }
}
