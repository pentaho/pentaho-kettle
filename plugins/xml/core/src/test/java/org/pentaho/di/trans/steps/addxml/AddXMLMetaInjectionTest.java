/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.addxml;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.value.ValueMetaBase;

public class AddXMLMetaInjectionTest extends BaseMetadataInjectionTest<AddXMLMeta> {

  @Before
  public void setup() {
    setup( new AddXMLMeta() );
  }

  @Test
  public void test() throws Exception {

    check( "OMIT_XML_HEADER", new BooleanGetter() {
      public boolean get() {
        return meta.isOmitXMLheader();
      }
    } );

    check( "OMIT_NULL_VALUES", new BooleanGetter() {
      public boolean get() {
        return meta.isOmitNullValues();
      }
    } );

    check( "ENCODING", new StringGetter() {
      public String get() {
        return meta.getEncoding();
      }
    } );

    check( "VALUE_NAME", new StringGetter() {
      public String get() {
        return meta.getValueName();
      }
    } );

    check( "ROOT_NODE", new StringGetter() {
      public String get() {
        return meta.getRootNode();
      }
    } );

    check( "OUTPUT_FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getFieldName();
      }
    } );

    check( "OUTPUT_ELEMENT_NAME", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getElementName();
      }
    } );

    String[] typeNames = ValueMetaBase.getAllTypes();
    checkStringToInt( "OUTPUT_TYPE", new IntGetter() {
      public int get() {
        return meta.getOutputFields()[0].getType();
      }
    }, typeNames, getTypeCodes( typeNames ) );

    check( "OUTPUT_FORMAT", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getFormat();
      }
    } );

    check( "OUTPUT_LENGTH", new IntGetter() {
      public int get() {
        return meta.getOutputFields()[0].getLength();
      }
    } );

    check( "OUTPUT_PRECISION", new IntGetter() {
      public int get() {
        return meta.getOutputFields()[0].getPrecision();
      }
    } );

    check( "OUTPUT_CURRENCY_SYMBOL", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getCurrencySymbol();
      }
    } );

    check( "OUTPUT_DECIMAL_SYMBOL", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getDecimalSymbol();
      }
    } );

    check( "OUTPUT_GROUPING_SYMBOL", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getGroupingSymbol();
      }
    } );

    check( "OUTPUT_ATTRIBUTE", new BooleanGetter() {
      public boolean get() {
        return meta.getOutputFields()[0].isAttribute();
      }
    } );

    check( "OUTPUT_ATTRIBUTE_PARENT_NAME", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getAttributeParentName();
      }
    } );

    check( "OUTPUT_NULL_STRING", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getNullString();
      }
    } );
  }

}
