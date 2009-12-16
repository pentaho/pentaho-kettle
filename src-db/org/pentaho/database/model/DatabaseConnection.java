package org.pentaho.database.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConnection implements Serializable, IDatabaseConnection {

  private static final long serialVersionUID = -3816140282186728714L;

  public static final String EMPTY_OPTIONS_STRING = "><EMPTY><";
  
  // part of the generic database connection, move somewhere else 
  public static final String ATTRIBUTE_CUSTOM_URL          = "CUSTOM_URL"; 
  public static final String ATTRIBUTE_CUSTOM_DRIVER_CLASS = "CUSTOM_DRIVER_CLASS";
  
  public static final String ATTRIBUTE_PREFIX_EXTRA_OPTION    = "EXTRA_OPTION_";
  
  protected String name;
  protected String databaseName;
  protected String databasePort;
  protected String hostname;
  protected String username;
  protected String password;
  protected String dataTablespace;
  protected String indexTablespace;
  protected boolean streamingResults;
  protected boolean quoteAllFields;

  // should this be here?
  protected boolean changed;
  
  // dialect specific fields?
  protected boolean usingDoubleDecimalAsSchemaTableSeparator;
  
  // Informix server name
  protected String informixServername;
  
  protected boolean forcingIdentifiersToLowerCase;
  protected boolean forcingIdentifiersToUpperCase;
  protected String connectSql;
  protected boolean usingConnectionPool;
  
  protected DatabaseAccessType accessType = null;
  protected IDatabaseType driver = null;
  protected Map<String, String> extraOptions = new HashMap<String, String>();
  protected Map<String, String> attributes = new HashMap<String, String>();
  protected Map<String, String> connectionPoolingProperties = new HashMap<String, String>();
  protected List<PartitionDatabaseMeta> partitioningInformation;
  protected int initialPoolSize;
  protected int maxPoolSize;
  protected boolean partitioned;
  
  
  
  public DatabaseConnection() {
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setAccessType(org.pentaho.database.model.DatabaseAccessType)
   */
  public void setAccessType(DatabaseAccessType accessType) {
    this.accessType = accessType;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getAccessType()
   */
  public DatabaseAccessType getAccessType() {
    return accessType;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setDatabaseDriver(org.pentaho.database.model.DatabaseType)
   */
  public void setDatabaseType(IDatabaseType driver) {
    this.driver = driver;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getDatabaseType()
   */
  public IDatabaseType getDatabaseType() {
    return driver;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getExtraOptions()
   */
  public Map<String, String> getExtraOptions() {
    return extraOptions;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getName()
   */
  public String getName() {
    return name;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setHostname(java.lang.String)
   */
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getHostname()
   */
  public String getHostname() {
    return hostname;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setDatabaseName(java.lang.String)
   */
  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getDatabaseName()
   */
  public String getDatabaseName() {
    return databaseName;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setDatabasePort(java.lang.String)
   */
  public void setDatabasePort(String databasePort) {
    this.databasePort = databasePort;
  }
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getDatabasePort()
   */
  public String getDatabasePort() {
    return databasePort;
  }

  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setUsername(java.lang.String)
   */
  public void setUsername(String username) {
    this.username = username;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getUsername()
   */
  public String getUsername() {
    return username;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setPassword(java.lang.String)
   */
  public void setPassword(String password) {
    this.password = password;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getPassword()
   */
  public String getPassword() {
    return password;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setStreamingResults(boolean)
   */
  public void setStreamingResults(boolean streamingResults) {
    this.streamingResults = streamingResults;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#isStreamingResults()
   */
  public boolean isStreamingResults() {
    return streamingResults;
  }
 
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setDataTablespace(java.lang.String)
   */
  public void setDataTablespace(String dataTablespace) {
    this.dataTablespace = dataTablespace;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getDataTablespace()
   */
  public String getDataTablespace() {
    return dataTablespace;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setIndexTablespace(java.lang.String)
   */
  public void setIndexTablespace(String indexTablespace) {
    this.indexTablespace = indexTablespace;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getIndexTablespace()
   */
  public String getIndexTablespace() {
    return indexTablespace;
  }
  
  // can we move these out into some other list like advanced features?

  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setSQLServerInstance(java.lang.String)
   */
  public void setSQLServerInstance(String sqlServerInstance) {
    addExtraOption("MSSQL", "instance", sqlServerInstance);
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getSQLServerInstance()
   */
  public String getSqlServerInstance() {
    return getExtraOptions().get("MSSQL.instance");
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setUsingDoubleDecimalAsSchemaTableSeparator(boolean)
   */
  public void setUsingDoubleDecimalAsSchemaTableSeparator(boolean usingDoubleDecimalAsSchemaTableSeparator) {
    this.usingDoubleDecimalAsSchemaTableSeparator = usingDoubleDecimalAsSchemaTableSeparator; 
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#isUsingDoubleDecimalAsSchemaTableSeparator()
   */
  public boolean isUsingDoubleDecimalAsSchemaTableSeparator() {
    return usingDoubleDecimalAsSchemaTableSeparator;
  }
  
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setInformixServername(java.lang.String)
   */
  public void setInformixServername(String informixServername) {
    this.informixServername = informixServername;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getInformixServername()
   */
  public String getInformixServername() {
    return informixServername;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#addExtraOption(java.lang.String, java.lang.String, java.lang.String)
   */
  public void addExtraOption(String databaseTypeCode, String option, String value) {
    extraOptions.put(databaseTypeCode + "." + option, value);
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getAttributes()
   */
  public Map<String, String> getAttributes() {
    return attributes;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setChanged(boolean)
   */
  public void setChanged(boolean changed) {
    this.changed = changed;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getChanged()
   */
  public boolean getChanged() {
    return changed;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setQuoteAllFields(boolean)
   */
  public void setQuoteAllFields(boolean quoteAllFields) {
    this.quoteAllFields = quoteAllFields;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#isQuoteAllFields()
   */
  public boolean isQuoteAllFields() {
    return quoteAllFields;
  }

  // advanced option (convert to enum with upper, lower, none?)
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setForcingIdentifiersToLowerCase(boolean)
   */
  public void setForcingIdentifiersToLowerCase(boolean forcingIdentifiersToLowerCase) {
    this.forcingIdentifiersToLowerCase = forcingIdentifiersToLowerCase;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#isForcingIdentifiersToLowerCase()
   */
  public boolean isForcingIdentifiersToLowerCase() {
    return forcingIdentifiersToLowerCase;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setForcingIdentifiersToUpperCase(boolean)
   */
  public void setForcingIdentifiersToUpperCase(boolean forcingIdentifiersToUpperCase) {
    this.forcingIdentifiersToUpperCase = forcingIdentifiersToUpperCase;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#isForcingIdentifiersToUpperCase()
   */
  public boolean isForcingIdentifiersToUpperCase() {
    return forcingIdentifiersToUpperCase;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setConnectSql(java.lang.String)
   */
  public void setConnectSql(String sql) {
    this.connectSql = sql;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getConnectSql()
   */
  public String getConnectSql() {
    return connectSql;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setUsingConnectionPool(boolean)
   */
  public void setUsingConnectionPool(boolean usingConnectionPool) {
    this.usingConnectionPool = usingConnectionPool;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#isUsingConnectionPool()
   */
  public boolean isUsingConnectionPool() {
    return usingConnectionPool;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setInitialPoolSize(int)
   */
  public void setInitialPoolSize(int initialPoolSize) {
    this.initialPoolSize = initialPoolSize;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getInitialPoolSize()
   */
  public int getInitialPoolSize() {
    return initialPoolSize;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setMaximumPoolSize(int)
   */
  public void setMaximumPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getMaximumPoolSize()
   */
  public int getMaximumPoolSize() {
    return maxPoolSize;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setPartitioned(boolean)
   */
  public void setPartitioned(boolean partitioned) {
    this.partitioned = partitioned;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#isPartitioned()
   */
  public boolean isPartitioned() {
    return partitioned;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getConnectionPoolingProperties()
   */
  public Map<String, String> getConnectionPoolingProperties() {
    return connectionPoolingProperties;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setConnectionPoolingProperties(java.util.Map)
   */
  public void setConnectionPoolingProperties(Map<String, String> connectionPoolingProperties) {
    this.connectionPoolingProperties = connectionPoolingProperties;
  }  

  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#setPartitioningInformation(java.util.List)
   */
  public void setPartitioningInformation(List<PartitionDatabaseMeta> partitioningInformation) {
    this.partitioningInformation = partitioningInformation;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseConnection#getPartitioningInformation()
   */
  public List<PartitionDatabaseMeta> getPartitioningInformation() {
    return this.partitioningInformation;
  }
}
