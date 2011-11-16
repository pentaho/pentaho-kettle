package org.pentaho.di.starmodeler;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.pentaho.di.i18n.BaseMessages;

public class PDIMessages extends ResourceBundle{

  // private static ResourceBundle lafBundle;
  
  private Class<?> clz = this.getClass();
  
  public PDIMessages(){
  }
  
  public PDIMessages(Class<?> pkg){
    this.clz = pkg;
  }
  
  @Override
  public Enumeration<String> getKeys() {
    return null;
  }

  @Override
  protected Object handleGetObject(String key) {
    String result = BaseMessages.getString(clz, key);
    return result;
  }
  
}