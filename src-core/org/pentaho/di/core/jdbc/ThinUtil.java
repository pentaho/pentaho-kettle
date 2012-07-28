package org.pentaho.di.core.jdbc;

public class ThinUtil {
  
  public static String stripNewlines(String sql) {
    if (sql==null) return null;
    
    StringBuffer sbsql = new StringBuffer(sql);
    
    for (int i=sbsql.length()-1;i>=0;i--)
    {
      if (sbsql.charAt(i)=='\n' || sbsql.charAt(i)=='\r') sbsql.setCharAt(i, ' ');
    }
    return sbsql.toString();
  }
  
}
