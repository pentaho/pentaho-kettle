package org.pentaho.di.core.database;

public class MSSQLServerNativeDatabaseMeta extends MSSQLServerDatabaseMeta{
  public static final String ATTRIBUTE_USE_INTEGRATED_SECURITY = "MSSQLUseIntegratedSecurity"; //$NON-NLS-1$
  @Override
  public String getDriverClass() {
    if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
    {
      return "sun.jdbc.odbc.JdbcOdbcDriver"; //$NON-NLS-1$
    }
    else
    {
      return "com.microsoft.sqlserver.jdbc.SQLServerDriver";//$NON-NLS-1$
    }
  }

  @Override
  public String getURL(String hostname, String port, String databaseName) {
    if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
    {
      return "jdbc:odbc:"+databaseName;//$NON-NLS-1$
    }
    else
    {
      Boolean useIntegratedSecurity = false;
      Object value = getAttributes().get(ATTRIBUTE_USE_INTEGRATED_SECURITY);
      if(value != null && value instanceof Boolean) {
        useIntegratedSecurity = (Boolean) value;
      }
      return "jdbc:sqlserver://"+hostname+":"+port+";databaseName="+databaseName+";integratedSecurity=" + useIntegratedSecurity.toString();//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
  }

}
