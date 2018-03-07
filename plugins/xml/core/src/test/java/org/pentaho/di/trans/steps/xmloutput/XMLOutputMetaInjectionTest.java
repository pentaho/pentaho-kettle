/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.xmloutput;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class XMLOutputMetaInjectionTest extends BaseMetadataInjectionTest<XMLOutputMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new XMLOutputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FILENAME", new StringGetter() {
      public String get() {
        return meta.getFileName();
      }
    } );
    check( "EXTENSION", new StringGetter() {
      public String get() {
        return meta.getExtension();
      }
    } );
    check( "PASS_TO_SERVLET", new BooleanGetter() {
      public boolean get() {
        return meta.isServletOutput();
      }
    } );
    check( "SPLIT_EVERY", new IntGetter() {
      public int get() {
        return meta.getSplitEvery();
      }
    } );
    check( "INC_STEPNR_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isStepNrInFilename();
      }
    } );
    check( "INC_DATE_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isDateInFilename();
      }
    } );
    check( "INC_TIME_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isTimeInFilename();
      }
    } );
    check( "ZIPPED", new BooleanGetter() {
      public boolean get() {
        return meta.isZipped();
      }
    } );
    check( "ENCODING", new StringGetter() {
      public String get() {
        return meta.getEncoding();
      }
    } );
    check( "NAMESPACE", new StringGetter() {
      public String get() {
        return meta.getNameSpace();
      }
    } );
    check( "MAIN_ELEMENT", new StringGetter() {
      public String get() {
        return meta.getMainElement();
      }
    } );
    check( "REPEAT_ELEMENT", new StringGetter() {
      public String get() {
        return meta.getRepeatElement();
      }
    } );
    check( "ADD_TO_RESULT", new BooleanGetter() {
      public boolean get() {
        return meta.isAddToResultFiles();
      }
    } );
    check( "DO_NOT_CREATE_FILE_AT_STARTUP", new BooleanGetter() {
      public boolean get() {
        return meta.isDoNotOpenNewFileInit();
      }
    } );
    check( "OMIT_NULL_VALUES", new BooleanGetter() {
      public boolean get() {
        return meta.isOmitNullValues();
      }
    } );
    check( "SPEFICY_FORMAT", new BooleanGetter() {
      public boolean get() {
        return meta.isSpecifyFormat();
      }
    } );
    check( "DATE_FORMAT", new StringGetter() {
      public String get() {
        return meta.getDateTimeFormat();
      }
    } );
    check( "OUTPUT_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getFieldName();
      }
    } );
    check( "OUTPUT_ELEMENTNAME", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getElementName();
      }
    } );
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
    check( "OUTPUT_CURRENCY", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getCurrencySymbol();
      }
    } );
    check( "OUTPUT_DECIMAL", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getDecimalSymbol();
      }
    } );
    check( "OUTPUT_GROUP", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getGroupingSymbol();
      }
    } );
    check( "OUTPUT_NULL", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getNullString();
      }
    } );
    check( "OUTPUT_CONTENT_TYPE", new EnumGetter() {
      public Enum<?> get() {
        return meta.getOutputFields()[0].getContentType();
      }
    }, XMLField.ContentType.class );

    // TODO check field type plugins
    skipPropertyTest( "OUTPUT_TYPE" );
  }
}
