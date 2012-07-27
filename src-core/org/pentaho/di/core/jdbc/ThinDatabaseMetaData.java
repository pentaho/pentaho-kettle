package org.pentaho.di.core.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ThinDatabaseMetaData implements DatabaseMetaData {

  private ThinConnection connection;
  private String serviceUrl;

  public ThinDatabaseMetaData(ThinConnection connection) {
    this.connection = connection;
    serviceUrl = connection.getSlaveBaseAddress()+"/listServices";
  }
  
  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException {
    return null;
  }

  @Override
  public boolean allProceduresAreCallable() throws SQLException {
    return false;
  }

  @Override
  public boolean allTablesAreSelectable() throws SQLException {
    return true;
  }

  @Override
  public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
    return false;
  }

  @Override
  public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
    return false;
  }

  @Override
  public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
    return false;
  }

  @Override
  public boolean deletesAreDetected(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
    return false;
  }

  @Override
  public ResultSet getAttributes(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    return null;
  }

  @Override
  public ResultSet getBestRowIdentifier(String arg0, String arg1, String arg2, int arg3, boolean arg4) throws SQLException {
    return null;
  }

  @Override
  public String getCatalogSeparator() throws SQLException {
    return null;
  }

  @Override
  public String getCatalogTerm() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getCatalogs() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getClientInfoProperties() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getColumnPrivileges(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    return null;
  }

  @Override
  public ResultSet getColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    // TODO: get column lists for all services
    return null;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connection;
  }

  @Override
  public ResultSet getCrossReference(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) throws SQLException {
    return null;
  }

  @Override
  public int getDatabaseMajorVersion() throws SQLException {
    return 0;
  }

  @Override
  public int getDatabaseMinorVersion() throws SQLException {
    return 0;
  }

  @Override
  public String getDatabaseProductName() throws SQLException {
    return "PDI";
  }

  @Override
  public String getDatabaseProductVersion() throws SQLException {
    return Const.VERSION;
  }

  @Override
  public int getDefaultTransactionIsolation() throws SQLException {
    return 0;
  }

  @Override
  public int getDriverMajorVersion() {
    return 0;
  }

  @Override
  public int getDriverMinorVersion() {
    return 1;
  }

  @Override
  public String getDriverName() throws SQLException {
    return "Kettle thin JDBC";
  }

  @Override
  public String getDriverVersion() throws SQLException {
    return "0.1";
  }

  @Override
  public ResultSet getExportedKeys(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public String getExtraNameCharacters() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getFunctionColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    return null;
  }

  @Override
  public ResultSet getFunctions(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public String getIdentifierQuoteString() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getImportedKeys(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public ResultSet getIndexInfo(String arg0, String arg1, String arg2, boolean arg3, boolean arg4) throws SQLException {
    return null;
  }

  @Override
  public int getJDBCMajorVersion() throws SQLException {
    return 0;
  }

  @Override
  public int getJDBCMinorVersion() throws SQLException {
    return 1;
  }

  @Override
  public int getMaxBinaryLiteralLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxCatalogNameLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxCharLiteralLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxColumnNameLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxColumnsInGroupBy() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxColumnsInIndex() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxColumnsInOrderBy() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxColumnsInSelect() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxColumnsInTable() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxConnections() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxCursorNameLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxIndexLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxProcedureNameLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxRowSize() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxSchemaNameLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxStatementLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxStatements() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxTableNameLength() throws SQLException {
    return 0;
  }

  @Override
  public int getMaxTablesInSelect() throws SQLException {
    return 1;
  }

  @Override
  public int getMaxUserNameLength() throws SQLException {
    return 0;
  }

  @Override
  public String getNumericFunctions() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getPrimaryKeys(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public ResultSet getProcedureColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    return null;
  }

  @Override
  public String getProcedureTerm() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getProcedures(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    return 0;
  }

  @Override
  public RowIdLifetime getRowIdLifetime() throws SQLException {
    return null;
  }

  @Override
  public String getSQLKeywords() throws SQLException {
    return null;
  }

  @Override
  public int getSQLStateType() throws SQLException {
    return 0;
  }

  @Override
  public String getSchemaTerm() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getSchemas() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getSchemas(String arg0, String arg1) throws SQLException {
    return null;
  }

  @Override
  public String getSearchStringEscape() throws SQLException {
    return null;
  }

  @Override
  public String getStringFunctions() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getSuperTables(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public ResultSet getSuperTypes(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public String getSystemFunctions() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getTablePrivileges(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public ResultSet getTableTypes() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getTables(String arg0, String arg1, String arg2, String[] arg3) throws SQLException {
    
    try {
      String xml = ThinUtil.getStringFromService(connection.getSlaveBaseAddress()+"/listServices", connection);
      RowMetaInterface rowMeta = new RowMeta();
      rowMeta.addValueMeta(new ValueMeta("TABLE_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TABLE_CAT", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TABLE_TYPE", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("REMARKS", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TYPE_CAT", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TYPE_SCHEM", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TYPE_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("SELF_REFERENCING_COL_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("REF_GENERATION", ValueMetaInterface.TYPE_STRING));
      
      Document doc = XMLHandler.loadXMLString(xml);
      Node servicesNode = XMLHandler.getSubNode(doc, "services");
      List<Node> serviceNodes = XMLHandler.getNodes(servicesNode, "service");
      List<Object[]> rows = new ArrayList<Object[]>();
      for (Node serviceNode : serviceNodes) {
        Object[] row = RowDataUtil.allocateRowData(rowMeta.size());
        row[0] = XMLHandler.getTagValue(serviceNode, "name");
        rows.add(row);
      }
      
      return new RowsResultSet(rowMeta, rows);
    } catch(Exception e) {
      LogChannel.GENERAL.logError("Error getting services list from server", e);
      throw new SQLException(e);
    }
    
  }

  @Override
  public String getTimeDateFunctions() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getTypeInfo() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getUDTs(String arg0, String arg1, String arg2, int[] arg3) throws SQLException {
    return null;
  }

  @Override
  public String getURL() throws SQLException {
    return null;
  }

  @Override
  public String getUserName() throws SQLException {
    return null;
  }

  @Override
  public ResultSet getVersionColumns(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  @Override
  public boolean insertsAreDetected(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean isCatalogAtStart() throws SQLException {
    return false;
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return true;
  }

  @Override
  public boolean locatorsUpdateCopy() throws SQLException {
    return false;
  }

  @Override
  public boolean nullPlusNonNullIsNull() throws SQLException {
    return true;
  }

  @Override
  public boolean nullsAreSortedAtEnd() throws SQLException {
    return false;
  }

  @Override
  public boolean nullsAreSortedAtStart() throws SQLException {
    return true;
  }

  @Override
  public boolean nullsAreSortedHigh() throws SQLException {
    return false;
  }

  @Override
  public boolean nullsAreSortedLow() throws SQLException {
    return true;
  }

  @Override
  public boolean othersDeletesAreVisible(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean othersInsertsAreVisible(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean othersUpdatesAreVisible(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean ownDeletesAreVisible(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean ownInsertsAreVisible(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean ownUpdatesAreVisible(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean storesLowerCaseIdentifiers() throws SQLException {
    return false;
  }

  @Override
  public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
    return false;
  }

  @Override
  public boolean storesMixedCaseIdentifiers() throws SQLException {
    return false;
  }

  @Override
  public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
    return false;
  }

  @Override
  public boolean storesUpperCaseIdentifiers() throws SQLException {
    return false;
  }

  @Override
  public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsANSI92EntryLevelSQL() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsANSI92FullSQL() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsANSI92IntermediateSQL() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsAlterTableWithAddColumn() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsAlterTableWithDropColumn() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsBatchUpdates() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsCatalogsInDataManipulation() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsCatalogsInProcedureCalls() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsCatalogsInTableDefinitions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsColumnAliasing() throws SQLException {
    return true;
  }

  @Override
  public boolean supportsConvert() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsConvert(int arg0, int arg1) throws SQLException {
    return false;
  }

  @Override
  public boolean supportsCoreSQLGrammar() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsCorrelatedSubqueries() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsDifferentTableCorrelationNames() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsExpressionsInOrderBy() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsExtendedSQLGrammar() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsFullOuterJoins() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsGetGeneratedKeys() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsGroupBy() throws SQLException {
    return true;
  }

  @Override
  public boolean supportsGroupByBeyondSelect() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsGroupByUnrelated() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsIntegrityEnhancementFacility() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsLikeEscapeClause() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsLimitedOuterJoins() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsMinimumSQLGrammar() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsMixedCaseIdentifiers() throws SQLException {
    return true;
  }

  @Override
  public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
    return true;
  }

  @Override
  public boolean supportsMultipleOpenResults() throws SQLException {
    return true;
  }

  @Override
  public boolean supportsMultipleResultSets() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsMultipleTransactions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsNamedParameters() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsNonNullableColumns() throws SQLException {
    return true;
  }

  @Override
  public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsOrderByUnrelated() throws SQLException {
    return true;
  }

  @Override
  public boolean supportsOuterJoins() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsPositionedDelete() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsPositionedUpdate() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsResultSetConcurrency(int arg0, int arg1) throws SQLException {
    return false;
  }

  @Override
  public boolean supportsResultSetHoldability(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean supportsResultSetType(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSavepoints() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSchemasInDataManipulation() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSchemasInIndexDefinitions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSchemasInProcedureCalls() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSchemasInTableDefinitions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSelectForUpdate() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsStatementPooling() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsStoredProcedures() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSubqueriesInComparisons() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSubqueriesInExists() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSubqueriesInIns() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsSubqueriesInQuantifieds() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsTableCorrelationNames() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsTransactionIsolationLevel(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean supportsTransactions() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsUnion() throws SQLException {
    return false;
  }

  @Override
  public boolean supportsUnionAll() throws SQLException {
    return false;
  }

  @Override
  public boolean updatesAreDetected(int arg0) throws SQLException {
    return false;
  }

  @Override
  public boolean usesLocalFilePerTable() throws SQLException {
    return false;
  }

  @Override
  public boolean usesLocalFiles() throws SQLException {
    return false;
  }

  /**
   * @return the serviceUrl
   */
  public String getServiceUrl() {
    return serviceUrl;
  }

  /**
   * @param serviceUrl the serviceUrl to set
   */
  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  /**
   * @param connection the connection to set
   */
  public void setConnection(ThinConnection connection) {
    this.connection = connection;
  }

}
