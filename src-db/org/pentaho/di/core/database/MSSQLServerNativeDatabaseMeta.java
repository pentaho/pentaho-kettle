package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;


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
      String useIntegratedSecurity = null;
      Object value = getAttributes().get(ATTRIBUTE_USE_INTEGRATED_SECURITY);
      if(value != null && value instanceof String) {
        useIntegratedSecurity = (String) value;
        // Check if the String can be parsed into a boolean
        try {
            Boolean.parseBoolean(useIntegratedSecurity);
        } catch (IllegalArgumentException e) {
          useIntegratedSecurity = "false";//$NON-NLS-1$
        }
      }
      
      String url = "jdbc:sqlserver://"+hostname;
      
      if (!Const.isEmpty(port) && Const.toInt(port, -1)>0) {
        url += ":"+port;
      }
      url+=";databaseName="+databaseName+";integratedSecurity=" + useIntegratedSecurity;
      
      return url;
    }
  }

}
