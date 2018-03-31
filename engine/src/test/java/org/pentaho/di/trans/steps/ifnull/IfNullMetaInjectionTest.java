/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.ifnull;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class IfNullMetaInjectionTest extends BaseMetadataInjectionTest<IfNullMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new IfNullMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.getFields()[0].getFieldName();
      }
    } );
    check( "REPLACE_VALUE", new StringGetter() {
      public String get() {
        return meta.getFields()[0].getReplaceValue();
      }
    } );
    check( "TYPE_NAME", new StringGetter() {
      public String get() {
        return meta.getValueTypes()[0].getTypeName();
      }
    } );
    check( "TYPE_REPLACE_VALUE", new StringGetter() {
      public String get() {
        return meta.getValueTypes()[0].getTypereplaceValue();
      }
    } );
    check( "TYPE_REPLACE_MASK", new StringGetter() {
      public String get() {
        return meta.getValueTypes()[0].getTypereplaceMask();
      }
    } );
    check( "REPLACE_MASK", new StringGetter() {
      public String get() {
        return meta.getFields()[0].getReplaceMask();
      }
    } );
    check( "SET_TYPE_EMPTY_STRING", new BooleanGetter() {
      public boolean get() {
        return meta.getValueTypes()[0].isSetTypeEmptyString();
      }
    } );
    check( "SET_EMPTY_STRING", new BooleanGetter() {
      public boolean get() {
        return meta.getFields()[0].isSetEmptyString();
      }
    } );
    check( "SELECT_FIELDS", new BooleanGetter() {
      public boolean get() {
        return meta.isSelectFields();
      }
    } );
    check( "SELECT_VALUES_TYPE", new BooleanGetter() {
      public boolean get() {
        return meta.isSelectValuesType();
      }
    } );
    check( "REPLACE_ALL_BY_VALUE", new StringGetter() {
      public String get() {
        return meta.getReplaceAllByValue();
      }
    } );
    check( "REPLACE_ALL_MASK", new StringGetter() {
      public String get() {
        return meta.getReplaceAllMask();
      }
    } );
    check( "SET_EMPTY_STRING_ALL", new BooleanGetter() {
      public boolean get() {
        return meta.isSetEmptyStringAll();
      }
    } );
  }

}
