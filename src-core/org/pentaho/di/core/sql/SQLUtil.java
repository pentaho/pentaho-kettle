package org.pentaho.di.core.sql;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleSQLException;

public class SQLUtil {

  public static List<String> splitClause(String fieldClause, char splitChar, char...skipChars) throws KettleSQLException {
    List<String> strings = new ArrayList<String>();
    int startIndex = 0;
    for (int index=0 ; index < fieldClause.length();index++) {
      index = SQL.skipChars(fieldClause, index, skipChars);
      if (index>=fieldClause.length()) {
        strings.add( fieldClause.substring(startIndex) );
        startIndex=-1;
        break;
      }
      if (fieldClause.charAt(index)==splitChar) {
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
}
