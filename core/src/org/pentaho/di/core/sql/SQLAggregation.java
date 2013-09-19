package org.pentaho.di.core.sql;

public enum SQLAggregation {
  SUM("SUM"), AVG("AVG"), MIN("MIN"), MAX("MAX"), COUNT("COUNT"),
  ;
  
  private String keyWord;

  private SQLAggregation(String keyWord) {
    this.keyWord = keyWord;
  }
  
  public String getKeyWord() {
    return keyWord;
  }
}
