package org.pentaho.di.core.jdbc;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.pms.util.Const;

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
  
  
  public static int getSqlType(ValueMetaInterface valueMeta) {
    switch(valueMeta.getType()) {
    case ValueMetaInterface.TYPE_STRING: return java.sql.Types.VARCHAR;
    case ValueMetaInterface.TYPE_DATE: return java.sql.Types.TIMESTAMP;
    case ValueMetaInterface.TYPE_INTEGER: return java.sql.Types.BIGINT; // TODO: for metadata we don't want a long?
    case ValueMetaInterface.TYPE_BIGNUMBER: return java.sql.Types.DECIMAL;
    case ValueMetaInterface.TYPE_NUMBER: return java.sql.Types.DOUBLE;
    case ValueMetaInterface.TYPE_BOOLEAN: return java.sql.Types.BOOLEAN;
    case ValueMetaInterface.TYPE_TIMESTAMP: return java.sql.Types.TIMESTAMP;
    case ValueMetaInterface.TYPE_BINARY: return java.sql.Types.BLOB;
    }
    return java.sql.Types.VARCHAR;
  }

  public static String getSqlTypeDesc(ValueMetaInterface valueMeta) {
    switch(valueMeta.getType()) {
    case ValueMetaInterface.TYPE_STRING: return "VARCHAR";
    case ValueMetaInterface.TYPE_DATE: return "TIMESTAMP";
    case ValueMetaInterface.TYPE_INTEGER: return "BIGINT"; // TODO: for metadata we don't want a long?
    case ValueMetaInterface.TYPE_NUMBER: return "DOUBLE";
    case ValueMetaInterface.TYPE_BIGNUMBER: return "DECIMAL";
    case ValueMetaInterface.TYPE_BOOLEAN: return "BOOLEAN";
    case ValueMetaInterface.TYPE_TIMESTAMP: return "TIMESTAMP";
    case ValueMetaInterface.TYPE_BINARY: return "BLOB";
    }
    return null;
  }
  
  public static ValueMetaInterface getValueMeta(String valueName, int sqlType) throws SQLException {
    switch(sqlType) {
    case java.sql.Types.BIGINT: return new ValueMeta(valueName, ValueMetaInterface.TYPE_INTEGER);
    case java.sql.Types.INTEGER: return new ValueMeta(valueName, ValueMetaInterface.TYPE_INTEGER);
    case java.sql.Types.SMALLINT: return new ValueMeta(valueName, ValueMetaInterface.TYPE_INTEGER);
    
    case java.sql.Types.CHAR: return new ValueMeta(valueName, ValueMetaInterface.TYPE_STRING);
    case java.sql.Types.VARCHAR: return new ValueMeta(valueName, ValueMetaInterface.TYPE_STRING);
    case java.sql.Types.CLOB: return new ValueMeta(valueName, ValueMetaInterface.TYPE_STRING);
    
    case java.sql.Types.DATE: return new ValueMeta(valueName, ValueMetaInterface.TYPE_DATE);
    case java.sql.Types.TIMESTAMP: return new ValueMeta(valueName, ValueMetaInterface.TYPE_DATE);
    case java.sql.Types.TIME: return new ValueMeta(valueName, ValueMetaInterface.TYPE_DATE);

    case java.sql.Types.DECIMAL: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BIGNUMBER);
    
    case java.sql.Types.DOUBLE: return new ValueMeta(valueName, ValueMetaInterface.TYPE_NUMBER);
    case java.sql.Types.FLOAT: return new ValueMeta(valueName, ValueMetaInterface.TYPE_NUMBER);
    
    case java.sql.Types.BOOLEAN: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BOOLEAN);
    case java.sql.Types.BIT: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BOOLEAN);
    
    case java.sql.Types.BINARY: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BINARY);
    case java.sql.Types.BLOB: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BINARY);
    
    default:
      throw new SQLException("Don't know how to handle SQL Type: "+sqlType+", with name: "+valueName);
    }
  }
  
  public static ValueMetaAndData attemptDateValueExtraction(String string) {
    if (string.length()>2 && string.startsWith("[") && string.endsWith("]")) {
      String unquoted=string.substring(1, string.length()-1);
      if (unquoted.length()>=9 && unquoted.charAt(4)=='/' && unquoted.charAt(7)=='/') {
        Date date = XMLHandler.stringToDate(unquoted);
        String format = "yyyy/MM/dd HH:mm:ss.SSS";
        if (date==null) {
          try {
            date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(unquoted);
            format = "yyyy/MM/dd HH:mm:ss";
          } catch(ParseException e1) {
            try {
              date = new SimpleDateFormat("yyyy/MM/dd").parse(unquoted);
              format = "yyyy/MM/dd";
            } catch (ParseException e2) {
              date=null;
            }
          }
        }
        if (date!=null) {
          ValueMetaInterface valueMeta = new ValueMeta("iif-date", ValueMetaInterface.TYPE_DATE);
          valueMeta.setConversionMask(format);
          return new ValueMetaAndData(valueMeta, date);
       
        } 
      }
    }
    return null;
  }
  
  public static ValueMetaAndData attemptIntegerValueExtraction(String string) {
    // Try an Integer
    if (!string.contains(".")) {
      try {
        long l = Long.parseLong(string);
        if (Long.toString(l).equals(string)) {
          ValueMetaAndData value = new ValueMetaAndData();
          ValueMetaInterface valueMeta = new ValueMeta("Constant", ValueMetaInterface.TYPE_INTEGER);
          valueMeta.setConversionMask("0");
          valueMeta.setGroupingSymbol(null);
          value.setValueMeta(valueMeta);
          value.setValueData(Long.valueOf(l));
          return value;
        }
      } catch(NumberFormatException e) {
      }
    }
    return null;
  }

  public static ValueMetaAndData attemptNumberValueExtraction(String string) {
    // Try a Number
    try {
      double d = Double.parseDouble(string);
      if (Double.toString(d).equals(string)) {
        ValueMetaAndData value = new ValueMetaAndData();
        ValueMetaInterface valueMeta = new ValueMeta("Constant", ValueMetaInterface.TYPE_NUMBER);
        valueMeta.setConversionMask("0.#");
        valueMeta.setGroupingSymbol(null);
        valueMeta.setDecimalSymbol(".");
        value.setValueMeta(valueMeta);
        value.setValueData(Double.valueOf(d));
        return value;
      }
    } catch(NumberFormatException e) {
    }
    return null;
  }

  public static ValueMetaAndData attemptBigNumberValueExtraction(String string) {
    // Try a BigNumber
    try {
      BigDecimal d = new BigDecimal(string);
      if (d.toString().equals(string)) {
        ValueMetaAndData value = new ValueMetaAndData();
        value.setValueMeta(new ValueMeta("Constant", ValueMetaInterface.TYPE_BIGNUMBER));
        value.setValueData(d);
        return value;
      }
    } catch(NumberFormatException e) {
    }
    return null;
  }

  public static ValueMetaAndData attemptStringValueExtraction(String string) {
    if (string.startsWith("'") && string.endsWith("'")) {
      String s = string.substring(1, string.length()-1);
      ValueMetaAndData value = new ValueMetaAndData();
      value.setValueMeta(new ValueMeta("Constant", ValueMetaInterface.TYPE_STRING));
      value.setValueData(s);
      return value;
    }
    return null;
  }

  public static ValueMetaAndData attemptBooleanValueExtraction(String string) {
    // Try an Integer
    if ("TRUE".equalsIgnoreCase(string) || "FALSE".equalsIgnoreCase(string)) {
      ValueMetaAndData value = new ValueMetaAndData();
      value.setValueMeta(new ValueMeta("Constant", ValueMetaInterface.TYPE_BOOLEAN));
      value.setValueData(Boolean.valueOf( "TRUE".equalsIgnoreCase(string) ));
      return value;
    }
    return null;
  }

  public static ValueMetaAndData extractConstant(String string) {
    // Try a date
    //
    ValueMetaAndData value = attemptDateValueExtraction(string);
    if (value!=null) return value;
    
    // String
    value = attemptStringValueExtraction(string);
    if (value!=null) return value;

    // Boolean
    value = attemptBooleanValueExtraction(string);
    if (value!=null) return value;

    // Integer
    value = attemptIntegerValueExtraction(string);
    if (value!=null) return value;

    // Number
    value = attemptNumberValueExtraction(string);
    if (value!=null) return value;

    // Number
    value = attemptBigNumberValueExtraction(string);
    if (value!=null) return value;
    
    return null;
  }
  
  public static String stripQuoteTableAlias(String field, String tableAliasPrefix) {
    if (field.toUpperCase().startsWith((tableAliasPrefix+".").toUpperCase())) {
      return ThinUtil.stripQuotes(field.substring(tableAliasPrefix.length()+1), '"');
    } else if (field.toUpperCase().startsWith(("\""+tableAliasPrefix+"\".").toUpperCase())) {
      return ThinUtil.stripQuotes(field.substring(tableAliasPrefix.length()+3), '"');
    } else {
      return ThinUtil.stripQuotes(Const.trim(field), '"');
    }
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
      if (countQuotes(builder.toString(), quoteChar)==2) {
        if (builder.length()>0 && builder.charAt(0)==quoteChar && builder.charAt(builder.length()-1)==quoteChar) {
          // If there are quotes in between, don't do it...
          //
          builder.deleteCharAt(builder.length()-1);
          builder.deleteCharAt(0);
        }
      }
    }
    return builder.toString();
  }
  
  private static int countQuotes(String string, char quoteChar) {
    int count=0;
    for (int i=0;i<string.length();i++) {
      if (string.charAt(i)==quoteChar) count++;
    }
    return count;
  }

  public static List<String> splitClause(String fieldClause, char splitChar, char...skipChars) throws KettleSQLException {
    List<String> strings = new ArrayList<String>();
    int startIndex = 0;
    for (int index=0 ; index < fieldClause.length();index++) {
      index = ThinUtil.skipChars(fieldClause, index, skipChars);
      if (index>=fieldClause.length()) {
        strings.add( fieldClause.substring(startIndex) );
        startIndex=-1;
        break;
      }
      // The CASE-WHEN-THEN-ELSE-END Hack // TODO: factor out
      // 
      if (fieldClause.substring(index).toUpperCase().startsWith("CASE WHEN ")) {
        // If we see CASE-WHEN then we skip to END
        //
        index = skipOverClause(fieldClause, index, " END");
      }
      
      if (index<fieldClause.length() && fieldClause.charAt(index)==splitChar) {
        strings.add( fieldClause.substring(startIndex, index) );
        while (index<fieldClause.length() && fieldClause.charAt(index)==splitChar) index++;
        startIndex=index;
        index--;
      }
    }
    if (startIndex>=0) {
      strings.add( fieldClause.substring(startIndex) );
    }
    
    return strings;
  }
  
  private static int skipOverClause(String fieldClause, int index, String clause) throws KettleSQLException {
    while (index<fieldClause.length()) {
      index=skipChars(fieldClause, index, '\'', '"');
      if (fieldClause.substring(index).toUpperCase().startsWith(clause.toUpperCase())) {
        return index+clause.length();
      }
      index++;
    }
    return fieldClause.length();
  }


  public static String findClause(String sqlString, String startClause, String...endClauses) throws KettleSQLException {
    if (Const.isEmpty(sqlString)) return null;
    
    String sql = sqlString.toUpperCase();
    
    int startIndex=0;
    while (startIndex<sql.length()) {
      startIndex = ThinUtil.skipChars(sql, startIndex, '"', '\'');
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
        index = ThinUtil.skipChars(sql, index, '"', '\'');

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
}
