/**
 * 
 */
package org.pentaho.di.core.database;

/**
 * @author mbatchelor
 *
 */
public class RedshiftDatabaseMeta extends PostgreSQLDatabaseMeta {

  public RedshiftDatabaseMeta() {
    addExtraOption( "REDSHIFT", "tcpKeepAlive", "true" );
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 5439;
    }
    return -1;
  }
  
  @Override
  public String getDriverClass() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "sun.jdbc.odbc.JdbcOdbcDriver";
    } else {
      return "com.amazon.redshift.jdbc4.Driver";
    }
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "jdbc:odbc:" + databaseName;
    } else {
      return "jdbc:redshift://" + hostname + ":" + port + "/" + databaseName;
    }
  }
  
  @Override
  public String getExtraOptionsHelpText() {
    return "http://docs.aws.amazon.com/redshift/latest/mgmt/configure-jdbc-connection.html";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "RedshiftJDBC4_1.0.10.1010.jar" };
  }
  
}
