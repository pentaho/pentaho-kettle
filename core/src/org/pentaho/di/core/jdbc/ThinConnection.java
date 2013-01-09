package org.pentaho.di.core.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.variables.Variables;

public class ThinConnection implements Connection {
  
  public static final String ARG_WEBAPPNAME="webappname";
  
  public static final String ARG_PROXYHOSTNAME = "proxyhostname";
  public static final String ARG_PROXYPORT =  "proxyport"; 
  public static final String ARG_NONPROXYHOSTS = "nonproxyhosts";
  public static final String ARG_DEBUGTRANS = "debugtrans";
  public static final String ARG_DEBUGLOG = "debuglog";
  
  private String url;
  private String slaveBaseAddress;
  private String username;
  private String password;
  private String hostname;
  private String port;
  private Map<String, String> arguments;

  private String webAppName;
  private String proxyHostname;
  private String proxyPort;
  private String nonProxyHosts;
  
  private String debugTransFilename;
  private boolean debuggingRemoteLog;

  private String service;
  
  public ThinConnection(String url, String username, String password) throws SQLException {
    this.url = url;
    this.username = username;
    this.password = password;

    parseUrl();
  }
  
  private void parseUrl() throws SQLException {
    
    if (!url.startsWith(ThinDriver.BASE_URL)) {
      throw new SQLException("Invalid url for this driver: "+url+", not starting with "+ThinDriver.BASE_URL);
    }
    
    // Example URLs : 
    //
    //   jdbc:pdi://slaveserver:8181/kettle/?webappname=pdi&proxyserver=proxy1&parameter_area=EAST
    //
    // converts to:
    //
    //   http://cluster:cluster@slaveserver:8181/kettle/?webappname=pdi&proxyserver=proxy1&parameter_area=EAST
    //
    
    try {
      int portColonIndex = url.indexOf(':', ThinDriver.BASE_URL.length());
      
      hostname = url.substring(ThinDriver.BASE_URL.length(), portColonIndex);
      int kettleIndex = url.indexOf(ThinDriver.SERVICE_NAME, portColonIndex);
      port = url.substring(portColonIndex+1, kettleIndex);
      service = ThinDriver.SERVICE_NAME;
      int startIndex = url.indexOf('?', kettleIndex)+1;
      arguments = new HashMap<String, String>();
      if (startIndex>0) {
        // Correct the path, exclude the arguments
        //
        String path=url.substring(startIndex);
        String[] args = path.split("\\&");
        for (String arg : args) {
          String[] parts = arg.split("=");
          if (parts.length==2) {
            arguments.put(parts[0], parts[1]);
          }
        }
      }
      
      slaveBaseAddress = "http://"+hostname+":"+port+service;
      
      // Determine the web app name
      //
      webAppName = arguments.get(ARG_WEBAPPNAME);
      proxyHostname = arguments.get(ARG_PROXYHOSTNAME);
      proxyPort= arguments.get(ARG_PROXYPORT);
      nonProxyHosts= arguments.get(ARG_NONPROXYHOSTS);
      debugTransFilename = arguments.get(ARG_DEBUGTRANS);
      debuggingRemoteLog = "true".equalsIgnoreCase(arguments.get(ARG_DEBUGLOG));
      
      // Try to get a status from the carte server to see if the connection works...
      //
      HttpUtil.execService(new Variables(), 
          hostname, port, webAppName, service+"/status/", username, password, 
          proxyHostname, proxyPort, nonProxyHosts);
      
    } catch (Exception e) {
      throw new SQLException("Unable to de-compose slave server address for URL: "+slaveBaseAddress, e);
    }
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
  public void clearWarnings() throws SQLException {
  }

  @Override
  public void close() throws SQLException {
    // TODO 

  }

  @Override
  public void commit() throws SQLException {
    throw new SQLException("Transactions are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
    throw new SQLException("Arrays are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public Blob createBlob() throws SQLException {
    throw new SQLException("Creating BLOBs is not supported by the thin Kettle JDBC driver");
  }

  @Override
  public Clob createClob() throws SQLException {
    throw new SQLException("Creating CLOBs is not supported by the thin Kettle JDBC driver");
  }

  @Override
  public NClob createNClob() throws SQLException {
    throw new SQLException("Creating NCLOBs is not supported by the thin Kettle JDBC driver");
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    throw new SQLException("Creating SQL XML is not supported by the thin Kettle JDBC driver");
  }

  @Override
  public Statement createStatement() throws SQLException {
    return new ThinStatement(this);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
   return new ThinStatement(this, resultSetType, resultSetConcurrency);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return new ThinStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
  }


  @Override
  public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
    throw new SQLException("Creating structs is not supported by the thin Kettle JDBC driver");
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return true;
  }

  @Override
  public String getCatalog() throws SQLException {
    return null;
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    return null;
  }

  @Override
  public String getClientInfo(String arg0) throws SQLException {
    return null;
  }

  @Override
  public int getHoldability() throws SQLException {
    return 0;
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return new ThinDatabaseMetaData(this);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return 0;
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    // TODO
    return null;
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    // TODO
    return null;
  }

  @Override
  public boolean isClosed() throws SQLException {
    // TODO
    return false;
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return true; // always read-only
  }

  @Override
  public boolean isValid(int arg0) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String nativeSQL(String arg0) throws SQLException {
    throw new SQLException("Native SQL statements are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public CallableStatement prepareCall(String arg0) throws SQLException {
    throw new SQLException("Perpared calls are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public CallableStatement prepareCall(String arg0, int arg1, int arg2) throws SQLException {
    throw new SQLException("Perpared calls are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
    throw new SQLException("Perpared calls are not supported by the thin Kettle JDBC driver");
  }

  
  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return new ThinPreparedStatement(this, sql);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return new ThinPreparedStatement(this, sql);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndex) throws SQLException {
    return new ThinPreparedStatement(this, sql);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return new ThinPreparedStatement(this, sql);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    return new ThinPreparedStatement(this, sql);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return new ThinPreparedStatement(this, sql);
  }
  

  @Override
  public void releaseSavepoint(Savepoint arg0) throws SQLException {
    throw new SQLException("Transactions are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public void rollback() throws SQLException {
    throw new SQLException("Transactions are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public void rollback(Savepoint arg0) throws SQLException {
    throw new SQLException("Transactions are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public void setAutoCommit(boolean auto) throws SQLException {
    // Ignore this one.
  }

  @Override
  public void setCatalog(String arg0) throws SQLException {
    // Ignore: we don't use catalogs
  }

  @Override
  public void setClientInfo(Properties arg0) throws SQLClientInfoException {
  }

  @Override
  public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
  }

  @Override
  public void setHoldability(int arg0) throws SQLException {
  }

  @Override
  public void setReadOnly(boolean arg0) throws SQLException {
    // Ignore, always read-only  
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    throw new SQLException("Safepoints calls are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public Savepoint setSavepoint(String arg0) throws SQLException {
    throw new SQLException("Safepoints calls are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public void setTransactionIsolation(int arg0) throws SQLException {
    throw new SQLException("Transactions are not supported by the thin Kettle JDBC driver");
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
    // TODO 

  }



  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @return the slaveBaseAddress
   */
  public String getSlaveBaseAddress() {
    return slaveBaseAddress;
  }

  /**
   * @param slaveBaseAddress the slaveBaseAddress to set
   */
  public void setSlaveBaseAddress(String slaveBaseAddress) {
    this.slaveBaseAddress = slaveBaseAddress;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String passsword) {
    this.password = passsword;
  }

  /**
   * @return the hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @return the port
   */
  public String getPort() {
    return port;
  }

  /**
   * @return the arguments
   */
  public Map<String, String> getArguments() {
    return arguments;
  }

  /**
   * @return the webAppName
   */
  public String getWebAppName() {
    return webAppName;
  }

  /**
   * @return the proxyHostname
   */
  public String getProxyHostname() {
    return proxyHostname;
  }

  /**
   * @return the proxyPort
   */
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * @return the nonProxyHosts
   */
  public String getNonProxyHosts() {
    return nonProxyHosts;
  }

  /**
   * @return the service
   */
  public String getService() {
    return service;
  }

  /**
   * @return the debugTransFilename
   */
  public String getDebugTransFilename() {
    return debugTransFilename;
  }

  /**
   * @return the debuggingRemoteLog
   */
  public boolean isDebuggingRemoteLog() {
    return debuggingRemoteLog;
  }

}
