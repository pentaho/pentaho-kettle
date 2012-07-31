package org.pentaho.di.core.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ThinDatabaseMetaData implements DatabaseMetaData {
  
  public static final String SCHEMA_NAME_KETTLE = "Kettle";

  private ThinConnection connection;
  private String serviceUrl;

  public ThinDatabaseMetaData(ThinConnection connection) {
    this.connection = connection;
    serviceUrl = connection.getService()+"/listServices/";
  }
  
  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException {
    throw new SQLException("Unwrap is not supported");
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
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("TYPE_CAT", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("TYPE_SCHEM", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("TYPE_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("ATTR_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("DATA_TYPE", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("ATTR_TYPE_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("ATTR_SIZE", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("DECIMAL_DIGITS", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("NUM_PREC_RADIX", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("NULLABLE", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("REMARKS", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("ATTR_DEF", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("SQL_DATA_TYPE", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("SQL_DATETIME_SUB", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("CHAR_OCTET_LENGTH", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("ORDINAL_POSITION", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("IS_NULLABLE", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("SCOPE_CATALOG", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("SCOPE_SCHEMA", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("SCOPE_TABLE", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("SOURCE_DATA_TYPE", ValueMetaInterface.TYPE_INTEGER));
    
    return new RowsResultSet(rowMeta, new ArrayList<Object[]>());
  }

  @Override
  public ResultSet getBestRowIdentifier(String arg0, String arg1, String arg2, int arg3, boolean arg4) throws SQLException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("SCOPE", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("COLUMN_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("DATA_TYPE", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("TYPE_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("COLUMN_SIZE", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("BUFFER_LENGTH", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("DECIMAL_DIGITS", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("PSEUDO_COLUMN", ValueMetaInterface.TYPE_INTEGER));
    
    return new RowsResultSet(rowMeta, new ArrayList<Object[]>());
  }

  @Override
  public String getCatalogSeparator() throws SQLException {
    return ".";
  }

  @Override
  public String getCatalogTerm() throws SQLException {
    return "Catalog";
  }

  @Override
  public ResultSet getCatalogs() throws SQLException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("TABLE_CAT", ValueMetaInterface.TYPE_STRING));
    
    return new RowsResultSet(rowMeta, new ArrayList<Object[]>());
  }

  @Override
  public ResultSet getClientInfoProperties() throws SQLException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("MAX_LEN", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("DEFAULT_VALUE", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING));

    return new RowsResultSet(rowMeta, new ArrayList<Object[]>());
  }

  @Override
  public ResultSet getColumnPrivileges(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("TABLE_CAT", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("TABLE_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("COLUMN_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("GRANTOR", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("GRANTEE", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("PRIVILEGE", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("IS_GRANTABLE", ValueMetaInterface.TYPE_STRING));

    return new RowsResultSet(rowMeta, new ArrayList<Object[]>());
  }

  @Override
  public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
    
    System.out.println("getColumns("+catalog+", "+schemaPattern+", "+tableNamePattern+", "+columnNamePattern+")");
    
    try {

      // Get the service information from the remote server...
      //
      List<ThinServiceInformation> services = getServiceInformation();
      
      RowMetaInterface rowMeta = new RowMeta();
      rowMeta.addValueMeta(new ValueMeta("TABLE_CAT", ValueMetaInterface.TYPE_STRING)); // null
      rowMeta.addValueMeta(new ValueMeta("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING)); // null
      rowMeta.addValueMeta(new ValueMeta("TABLE_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("COLUMN_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("DATA_TYPE", ValueMetaInterface.TYPE_INTEGER));
      rowMeta.addValueMeta(new ValueMeta("TYPE_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("COLUMN_SIZE", ValueMetaInterface.TYPE_INTEGER)); // length
      rowMeta.addValueMeta(new ValueMeta("BUFFER_LENGTH", ValueMetaInterface.TYPE_INTEGER)); // not used
      rowMeta.addValueMeta(new ValueMeta("DECIMAL_DIGITS", ValueMetaInterface.TYPE_INTEGER)); // precision
      rowMeta.addValueMeta(new ValueMeta("NUM_PREC_RADIX", ValueMetaInterface.TYPE_INTEGER)); // Radix, typically either 10 or 2
      rowMeta.addValueMeta(new ValueMeta("NULLABLE", ValueMetaInterface.TYPE_INTEGER)); // columnsNullableUnknown
      rowMeta.addValueMeta(new ValueMeta("REMARKS", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("COLUMN_DEF", ValueMetaInterface.TYPE_STRING)); // default value, null
      rowMeta.addValueMeta(new ValueMeta("SQL_DATA_TYPE", ValueMetaInterface.TYPE_INTEGER)); // unused
      rowMeta.addValueMeta(new ValueMeta("SQL_DATATIME_SUB", ValueMetaInterface.TYPE_INTEGER)); // unused
      rowMeta.addValueMeta(new ValueMeta("CHAR_OCTET_LENGTH", ValueMetaInterface.TYPE_INTEGER)); // max string length in bytes
      rowMeta.addValueMeta(new ValueMeta("ORDINAL_POSITION", ValueMetaInterface.TYPE_INTEGER)); // column position, 1 based 
      rowMeta.addValueMeta(new ValueMeta("IS_NULLABLE", ValueMetaInterface.TYPE_STRING)); // empty string: nobody knows
      rowMeta.addValueMeta(new ValueMeta("SCOPE_CATALOG", ValueMetaInterface.TYPE_STRING)); // null
      rowMeta.addValueMeta(new ValueMeta("SCOPE_SCHEMA", ValueMetaInterface.TYPE_STRING)); // null
      rowMeta.addValueMeta(new ValueMeta("SCOPE_TABLE", ValueMetaInterface.TYPE_STRING)); // null
      rowMeta.addValueMeta(new ValueMeta("SOURCE_DATA_TYPE", ValueMetaInterface.TYPE_STRING)); // Kettle source data type description

      List<Object[]> rows = new ArrayList<Object[]>();
      for (ThinServiceInformation service : services) {
        
        if (Const.isEmpty(tableNamePattern) || service.getName().equalsIgnoreCase(tableNamePattern)) { 
          int ordinal=1;
          for (ValueMetaInterface valueMeta : service.getServiceFields().getValueMetaList()) {
            if (Const.isEmpty(columnNamePattern) || valueMeta.getName().equalsIgnoreCase(columnNamePattern)) {
              Object[] row = RowDataUtil.allocateRowData(rowMeta.size());
              int index=0;
              row[index++] = null; // TABLE_CAT - TYPE_STRING
              row[index++] = SCHEMA_NAME_KETTLE; // TABLE_SCHEM - TYPE_STRING
              row[index++] = service.getName(); // TABLE_NAME - TYPE_STRING
              row[index++] = valueMeta.getName(); // COLUMN_NAME - TYPE_STRING
              row[index++] = Long.valueOf(ThinUtil.getSqlType(valueMeta)); // DATA_TYPE - TYPE_INTEGER
              row[index++] = ThinUtil.getSqlTypeDesc(valueMeta); // TYPE_NAME - TYPE_STRING
              row[index++] = Long.valueOf(valueMeta.getLength()); // COLUMN_SIZE - TYPE_INTEGER
              row[index++] = null; // BUFFER_LENGTH
              row[index++] = Long.valueOf(valueMeta.getPrecision()); // DECIMAL_DIGITS
              row[index++] = Long.valueOf(10); // NUM_PREC_RADIX
              row[index++] = DatabaseMetaData.columnNullableUnknown; // NULLABLE
              row[index++] = valueMeta.getComments(); // REMARKS
              row[index++] = null; // COLUMN_DEF
              row[index++] = null; // SQL_DATA_TYPE
              row[index++] = null; // SQL_DATATIME_SUB_
              row[index++] = Long.valueOf(valueMeta.getLength()); // CHAR_OCTET_LENGTH
              row[index++] = Long.valueOf(ordinal); // ORDINAL_POSITION
              row[index++] = ""; // IS_NULLABLE
              row[index++] = null; // SCOPE_CATALOG
              row[index++] = null; // SCOPE_SCHEMA
              row[index++] = null; // SCOPE_TABLE
              row[index++] = valueMeta.getTypeDesc(); // SOURCE_DATA_TYPE
              rows.add(row);
            }
            ordinal++;
          }
        }
      }

      return new RowsResultSet(rowMeta, rows);
    } catch(Exception e) {
      throw new SQLException(e);
    }   
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connection;
  }

  @Override
  public ResultSet getCrossReference(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) throws SQLException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("TABLE_CAT", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("TABLE_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("COLUMN_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("GRANTOR", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("GRANTEE", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("PRIVILEGE", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("IS_GRANTABLE", ValueMetaInterface.TYPE_STRING));

    return new RowsResultSet(rowMeta, new ArrayList<Object[]>());
  }

  @Override
  public int getDatabaseMajorVersion() throws SQLException {
    return 5;
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
    return 5;
  }

  @Override
  public int getDriverMinorVersion() {
    return 0;
  }

  @Override
  public String getDriverName() throws SQLException {
    return "Kettle thin JDBC driver";
  }

  @Override
  public String getDriverVersion() throws SQLException {
    return "5.0";
  }

  @Override
  public ResultSet getExportedKeys(String arg0, String arg1, String arg2) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public String getExtraNameCharacters() throws SQLException {
    return "";
  }

  @Override
  public ResultSet getFunctionColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public ResultSet getFunctions(String arg0, String arg1, String arg2) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public String getIdentifierQuoteString() throws SQLException {
    return "\"";
  }

  @Override
  public ResultSet getImportedKeys(String arg0, String arg1, String arg2) throws SQLException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("PKTABLE_CAT", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("PKTABLE_SCHEM", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("PKTABLE_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("PKCOLUMN_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("FKTABLE_CAT", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("FKTABLE_SCHEM", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("FKTABLE_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("FKCOLUMN_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("KEY_SEQ", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("UPDATE_RULE", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("DELETE_RULE", ValueMetaInterface.TYPE_INTEGER));
    rowMeta.addValueMeta(new ValueMeta("FK_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("PK_NAME", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("DEFERRABILITY", ValueMetaInterface.TYPE_INTEGER));
    
    return new RowsResultSet(rowMeta, new ArrayList<Object[]>());
  }

  @Override
  public ResultSet getIndexInfo(String arg0, String arg1, String arg2, boolean arg3, boolean arg4) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public int getJDBCMajorVersion() throws SQLException {
    return 1;
  }

  @Override
  public int getJDBCMinorVersion() throws SQLException {
    return 0;
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
    return "";
  }

  @Override
  public ResultSet getPrimaryKeys(String arg0, String arg1, String arg2) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public ResultSet getProcedureColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public String getProcedureTerm() throws SQLException {
    return "";
  }

  @Override
  public ResultSet getProcedures(String arg0, String arg1, String arg2) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    return 0;
  }

  @Override
  public RowIdLifetime getRowIdLifetime() throws SQLException {
    return RowIdLifetime.ROWID_VALID_OTHER;
  }

  @Override
  public String getSQLKeywords() throws SQLException {
    return "SELECT,FROM,WHERE,GROUP,BY,AS,HAVING,ORDER,AND,OR,IN,NOT,NULL,REGEX,IIF,PARAMETER";
  }

  @Override
  public int getSQLStateType() throws SQLException {
    return 0;
  }

  @Override
  public String getSchemaTerm() throws SQLException {
    return "Schema";
  }

  @Override
  public String getSearchStringEscape() throws SQLException {
    return "";
  }

  @Override
  public String getStringFunctions() throws SQLException {
    return "";
  }

  @Override
  public ResultSet getSuperTables(String arg0, String arg1, String arg2) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public ResultSet getSuperTypes(String arg0, String arg1, String arg2) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public String getSystemFunctions() throws SQLException {
    return "";
  }

  @Override
  public ResultSet getTablePrivileges(String arg0, String arg1, String arg2) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public ResultSet getTableTypes() throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }
  
  public List<ThinServiceInformation> getServiceInformation() throws SQLException {
    try {
      String xml = HttpUtil.execService(new Variables(),
          connection.getHostname(), connection.getPort(), connection.getWebAppName(), 
          serviceUrl, connection.getUsername(), connection.getPassword(), 
          connection.getProxyHostname(), connection.getProxyPort(), connection.getNonProxyHosts()
         );
  
      List<ThinServiceInformation> services = new ArrayList<ThinServiceInformation>();
      
      Document doc = XMLHandler.loadXMLString(xml);
      Node servicesNode = XMLHandler.getSubNode(doc, "services");
      List<Node> serviceNodes = XMLHandler.getNodes(servicesNode, "service");
      for (Node serviceNode : serviceNodes) {
        
        String name = XMLHandler.getTagValue(serviceNode, "name");
        Node rowMetaNode = XMLHandler.getSubNode(serviceNode, RowMeta.XML_META_TAG);
        RowMetaInterface serviceFields = new RowMeta(rowMetaNode);
        ThinServiceInformation service = new ThinServiceInformation(name, serviceFields);
        services.add(service);
      }
      
      return services;
    } catch(Exception e) {
      throw new SQLException("Unable to get service information from server", e);
    }

  }
  
  @Override
  public ResultSet getSchemas() throws SQLException {
    return getSchemas(null, null);
  }

  @Override
  public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING));
    rowMeta.addValueMeta(new ValueMeta("TABLE_CATALOG", ValueMetaInterface.TYPE_STRING));
    
    List<Object[]> rows = new ArrayList<Object[]>();

    if (Const.isEmpty(schemaPattern) || SCHEMA_NAME_KETTLE.equalsIgnoreCase(schemaPattern)) { 
      Object[] row = RowDataUtil.allocateRowData(rowMeta.size());
      int index=0;
      row[index++] = SCHEMA_NAME_KETTLE; // TABLE_SCHEM
      row[index++] = null; // TABLE_CATALOG
      
      System.out.println("!!!!!!!-----> reporting one schema: "+SCHEMA_NAME_KETTLE);
      
      rows.add(row);
    }
    
    return new RowsResultSet(rowMeta, rows);
  }
  

  @Override
  public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
    
    if (!Const.isEmpty(types) && Const.indexOfString("TABLE", types)<0) {
      System.out.println("-------------> Requesting table types: "+Arrays.toString(types));
      System.out.println("-------------> We only serve up table information, it's all we have!");
      return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>());
    }
    
    if (Const.isEmpty(tableNamePattern)) {
      System.out.println("-------------> Listing all tables!");
    } else {
      System.out.println("-------------> Looking for table "+tableNamePattern);
    }
    
    try {

      // Get the service information from the remote server...
      //
      List<ThinServiceInformation> services = getServiceInformation();

      RowMetaInterface rowMeta = new RowMeta();
      rowMeta.addValueMeta(new ValueMeta("TABLE_CAT", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TABLE_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TABLE_TYPE", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("REMARKS", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TYPE_CAT", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TYPE_SCHEM", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("TYPE_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("SELF_REFERENCING_COL_NAME", ValueMetaInterface.TYPE_STRING));
      rowMeta.addValueMeta(new ValueMeta("REF_GENERATION", ValueMetaInterface.TYPE_STRING));

      List<Object[]> rows = new ArrayList<Object[]>();
      for (ThinServiceInformation service : services) {
        if (Const.isEmpty(tableNamePattern) || service.getName().equalsIgnoreCase(tableNamePattern)) {
          Object[] row = RowDataUtil.allocateRowData(rowMeta.size());
          int index=0;
          row[index++] = null; // TABLE_CAT
          row[index++] = SCHEMA_NAME_KETTLE; // TABLE_SCHEM
          row[index++] = service.getName(); // TABLE_NAME
          row[index++] = "TABLE"; // TABLE_TYPE
          row[index++] = null; // REMARKS
          row[index++] = null; // TYPE_CAT
          row[index++] = null; // TYPE_SCHEM
          row[index++] = null; // TYPE_NAME
          row[index++] = null; // SELF_REFERENCING_COL_NAME
          row[index++] = null; // REF_GENERATION
          rows.add(row);
        }
      }

      System.out.println("-------------> Found "+rows.size()+" tables for the rows resultset.");

      return new RowsResultSet(rowMeta, rows);
    } catch(Exception e) {
      throw new SQLException(e);
    }
  }

  @Override
  public String getTimeDateFunctions() throws SQLException {
    return "";
  }

  @Override
  public ResultSet getTypeInfo() throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
  }

  @Override
  public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>());
  }

  @Override
  public String getURL() throws SQLException {
    return connection.getUrl();
  }

  @Override
  public String getUserName() throws SQLException {
    return connection.getUsername();
  }

  @Override
  public ResultSet getVersionColumns(String arg0, String arg1, String arg2) throws SQLException {
    return new RowsResultSet(new RowMeta(), new ArrayList<Object[]>()); // empty set
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
    return true;
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
