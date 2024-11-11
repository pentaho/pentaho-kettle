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


package org.pentaho.di.trans.steps.script;

public class ScriptAddClasses {

  private Class<?> addClass;
  private Object addObject;
  private String strJSName;

  // private String strJarFile;

  public ScriptAddClasses( Class<?> addClass, Object addObject, String strJSName ) {
    super();
    this.addClass = addClass;
    this.addObject = addObject;
    this.strJSName = strJSName;
  }

  public Class<?> getAddClass() {
    return this.addClass;
  }

  public Object getAddObject() {
    return this.addObject;
  }

  public String getJSName() {
    return this.strJSName;
  }

}
