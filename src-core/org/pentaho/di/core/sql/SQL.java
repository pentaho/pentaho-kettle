package org.pentaho.di.core.sql;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;

public class SQL {
  private String sqlString;
  
  private RowMetaInterface rowMeta;

  private String serviceName;

  private String selectClause;
  private SQLFields selectFields;

  private String whereClause;
  private SQLCondition whereCondition;
  
  private String groupClause;
  private SQLFields groupFields;

  private String havingClause;
  private SQLCondition havingCondition;

  private String orderClause;
  private SQLFields orderFields;
  
  /**
   * Create a new SQL object by parsing the supplied SQL string.
   * This is a simple implementation with only one table allows
   * 
   * @param sqlString the SQL string to parse
   * @param serviceName the name of the service this SQL references
   * @param rowMeta the row layout of the service
   * @throws KettleSQLException in case there is a SQL parsing error
   */
  public SQL(String sqlString) throws KettleSQLException {
    this.sqlString = sqlString;
    
    splitSql(sqlString);
  }
  
  private void splitSql(String sql) throws KettleSQLException {
    // First get the major blocks...
    /*
     * SELECT A, B, C
     * FROM   Step
     * 
     * SELECT A, B, C
     * FROM   Step
     * WHERE  D > 6
     * AND    E = 'abcd'
     * 
     * SELECT A, B, C
     * FROM   Step
     * ORDER BY B, A, C
     * 
     * SELECT A, B, sum(C)
     * FROM   Step
     * WHERE  D > 6
     * AND    E = 'abcd'
     * GROUP BY A, B
     * HAVING sum(C) > 100
     * ORDER BY sum(C) DESC
     * 
     */
    //
    selectClause = findClause(sql, "SELECT", "FROM");
    serviceName = findClause(sql, "FROM", "WHERE", "GROUP BY", "ORDER BY");    
    whereClause = findClause(sql, "WHERE", "GROUP BY", "ORDER BY");
    groupClause = findClause(sql, "GROUP BY", "HAVING", "ORDER BY");
    havingClause = findClause(sql, "HAVING", "ORDER BY");
    orderClause = findClause(sql, "ORDER BY");
  }
  
  public void parse(RowMetaInterface rowMeta) throws KettleSQLException {
    
    // Now do the actual parsing and interpreting of the SQL, map it to the service row metadata
    
    this.rowMeta = rowMeta;

    selectFields = new SQLFields(rowMeta, selectClause);
    if (!Const.isEmpty(whereClause)) {
      whereCondition = new SQLCondition(whereClause, rowMeta);
    }
    if (!Const.isEmpty(groupClause)) {
      groupFields = new SQLFields(rowMeta, groupClause);
    } else {
      groupFields = new SQLFields(new RowMeta(), null);
    }
    if (!Const.isEmpty(havingClause)) {
      havingCondition = new SQLCondition(havingClause, rowMeta, selectFields);
    }
    if (!Const.isEmpty(orderClause)) {
      orderFields = new SQLFields(rowMeta, orderClause, true, selectFields);
    }
  }
  
  public static String findClause(String sqlString, String startClause, String...endClauses) throws KettleSQLException {
    if (Const.isEmpty(sqlString)) return null;
    
    String sql = sqlString.toUpperCase();
    
    int startIndex=0;
    while (startIndex<sql.length()) {
      startIndex = skipChars(sql, startIndex, '"', '\'');
      if (sql.substring(startIndex).startsWith(startClause.toUpperCase())) {
        break;
      }
      startIndex++;
    }
    
    if (startIndex<0 || startIndex>=sql.length()) return null;
    
    startIndex+=startClause.length()+1;
    if (endClauses.length==0) return sql.substring(startIndex);
    
    int endIndex=sql.length();
    for (String endClause : endClauses) {
      
      int index=startIndex;
      while (index<sql.length()) {
        index = skipChars(sql, index, '"', '\'');

        // See if the end-clause is present at this location.
        //
        if (sql.substring(index).startsWith(endClause.toUpperCase())) {
          if (index<endIndex) endIndex=index;
        }
        index++;
      }
    }
    return Const.trim( sqlString.substring(startIndex, endIndex) );
  }

  public static int skipChars(String sql, int index, char...skipChars) throws KettleSQLException {
    // Skip over double quotes and quotes
    char c = sql.charAt(index);
    boolean count=false;
    for (char skipChar : skipChars) {
      if (c==skipChar) {
        char nextChar = skipChar;
        if (skipChar=='(') { nextChar = ')'; count=true; }
        if (skipChar=='{') { nextChar = '}'; count=true; }
        if (skipChar=='[') { nextChar = ']'; count=true; }
        
        if (count) {
          index = findNextBracket(sql, skipChar, nextChar, index);
        } else {
          index = findNext(sql, nextChar, index);
        }
        if (index>=sql.length()) break;
        c = sql.charAt(index);
      }
    }

    return index;
  }

  public static int findNext(String sql, char nextChar, int index) throws KettleSQLException {
    int quoteIndex=index;
    index++;
    while (index<sql.length() && sql.charAt(index)!=nextChar) index++;
    if (index+1>sql.length()) {
      throw new KettleSQLException("No closing "+nextChar+" found, starting at location "+quoteIndex+" in : ["+sql+"]");
    }
    index++;
    return index;
  }
  
  public static int findNextBracket(String sql, char skipChar, char nextChar, int index) throws KettleSQLException {
    
    int counter=0;
    for (int i=index;i<sql.length();i++) {
      i=skipChars(sql, i, '\''); // skip quotes
      char c = sql.charAt(i);
      if (c==skipChar) counter++;
      if (c==nextChar) counter--;
      if (counter==0) {
        return i;
      }
    }
    
    throw new KettleSQLException("No closing "+nextChar+" bracket found for "+skipChar+" at location "+index+" in : ["+sql+"]");
  }
  
  
  public static String stripQuotes(String string, char...quoteChars) {
    StringBuilder builder = new StringBuilder(string);
    for (char quoteChar : quoteChars) {
      if (builder.length()>0 && builder.charAt(0)==quoteChar && builder.charAt(builder.length()-1)==quoteChar) {
        builder.deleteCharAt(builder.length()-1);
        builder.deleteCharAt(0);
      }
    }
    return builder.toString();
  }

  public String getSqlString() {
    return sqlString;
  }
  
  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }
  
  public String getServiceName() {
    return serviceName;
  }

  /**
   * @return the selectClause
   */
  public String getSelectClause() {
    return selectClause;
  }

  /**
   * @param selectClause the selectClause to set
   */
  public void setSelectClause(String selectClause) {
    this.selectClause = selectClause;
  }

  /**
   * @return the whereClause
   */
  public String getWhereClause() {
    return whereClause;
  }

  /**
   * @param whereClause the whereClause to set
   */
  public void setWhereClause(String whereClause) {
    this.whereClause = whereClause;
  }

  /**
   * @return the groupClause
   */
  public String getGroupClause() {
    return groupClause;
  }

  /**
   * @param groupClause the groupClause to set
   */
  public void setGroupClause(String groupClause) {
    this.groupClause = groupClause;
  }

  /**
   * @return the havingClause
   */
  public String getHavingClause() {
    return havingClause;
  }

  /**
   * @param havingClause the havingClause to set
   */
  public void setHavingClause(String havingClause) {
    this.havingClause = havingClause;
  }

  /**
   * @return the orderClause
   */
  public String getOrderClause() {
    return orderClause;
  }

  /**
   * @param orderClause the orderClause to set
   */
  public void setOrderClause(String orderClause) {
    this.orderClause = orderClause;
  }

  /**
   * @param sqlString the sql string to set
   */
  public void setSqlString(String sqlString) {
    this.sqlString = sqlString;
  }

  /**
   * @return the selectFields
   */
  public SQLFields getSelectFields() {
    return selectFields;
  }

  /**
   * @param selectFields the selectFields to set
   */
  public void setSelectFields(SQLFields selectFields) {
    this.selectFields = selectFields;
  }

  /**
   * @return the groupFields
   */
  public SQLFields getGroupFields() {
    return groupFields;
  }

  /**
   * @param groupFields the groupFields to set
   */
  public void setGroupFields(SQLFields groupFields) {
    this.groupFields = groupFields;
  }

  /**
   * @return the orderFields
   */
  public SQLFields getOrderFields() {
    return orderFields;
  }

  /**
   * @param orderFields the orderFields to set
   */
  public void setOrderFields(SQLFields orderFields) {
    this.orderFields = orderFields;
  }

  /**
   * @return the whereCondition
   */
  public SQLCondition getWhereCondition() {
    return whereCondition;
  }

  /**
   * @param whereCondition the whereCondition to set
   */
  public void setWhereCondition(SQLCondition whereCondition) {
    this.whereCondition = whereCondition;
  }

  /**
   * @param rowMeta the rowMeta to set
   */
  public void setRowMeta(RowMetaInterface rowMeta) {
    this.rowMeta = rowMeta;
  }

  /**
   * @param serviceName the serviceName to set
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * @return the havingCondition
   */
  public SQLCondition getHavingCondition() {
    return havingCondition;
  }

  /**
   * @param havingCondition the havingCondition to set
   */
  public void setHavingCondition(SQLCondition havingCondition) {
    this.havingCondition = havingCondition;
  }
  
  
}