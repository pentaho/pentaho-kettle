/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
