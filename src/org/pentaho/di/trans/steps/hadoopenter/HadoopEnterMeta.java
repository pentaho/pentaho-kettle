/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.hadoopenter;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

@Step(id = "HadoopEnterPlugin", image = "MRI.png", name = "MapReduce Input", description = "Enter a Hadoop Mapper or Reducer transformation", categoryDescription = "Big Data")
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