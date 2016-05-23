/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getvariable;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

public class GetVariableMetaInjectionTest extends BaseMetadataInjectionTest<GetVariableMeta> {
  @Before
  public void setup() {
    setup( new GetVariableMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getFieldName()[0];
      }
    } );
    check( "VARIABLE", new StringGetter() {
      public String get() {
        return meta.getVariableString()[0];
      }
    } );
    check( "FIELDTYPE", new IntGetter() {
      public int get() {
        return meta.getFieldType()[0];
      }
    } );
    check( "FIELDFORMAT", new StringGetter() {
      public String get() {
        return meta.getFieldFormat()[0];
      }
    } );
    check( "FIELDLENGTH", new IntGetter() {
      public int get() {
        return meta.getFieldLength()[0];
      }
    } );
    check( "FIELDPRECISION", new IntGetter() {
        public int get() {
          return meta.getFieldPrecision()[0];
        }
      } );
    check( "CURRENCY", new StringGetter() {
        public String get() {
          return meta.getCurrency()[0];
        }
      } );
    check( "DECIMAL", new StringGetter() {
        public String get() {
          return meta.getDecimal()[0];
        }
      } );
    check( "GROUP", new StringGetter() {
        public String get() {
          return meta.getGroup()[0];
        }
      } );
    check( "TRIMTYPE", new IntGetter() {
        public int get() {
          return meta.getTrimType()[0];
        }
      } );
  }
}
