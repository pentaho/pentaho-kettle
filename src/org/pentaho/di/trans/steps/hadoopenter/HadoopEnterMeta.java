/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.hadoopenter;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

@Step(id = "HadoopEnterPlugin", image = "MRI.png", name = "Map/Reduce Input", description = "Enter a Hadoop Mapper or Reducer transformation", categoryDescription = "Hadoop")
public class HadoopEnterMeta extends InjectorMeta {
  private static Class<?> PKG = HadoopEnterMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public static final String KEY_FIELDNAME = "key";
  public static final String VALUE_FIELDNAME = "value";
  
  public HadoopEnterMeta() throws Throwable {
    super();

    allocate(2);

    getFieldname()[0] = HadoopEnterMeta.KEY_FIELDNAME;
    getFieldname()[1] = HadoopEnterMeta.VALUE_FIELDNAME;
  }

  public void setDefault() {
    allocate(2);

    getFieldname()[0] = HadoopEnterMeta.KEY_FIELDNAME;
    getFieldname()[1] = HadoopEnterMeta.VALUE_FIELDNAME;
  }
}