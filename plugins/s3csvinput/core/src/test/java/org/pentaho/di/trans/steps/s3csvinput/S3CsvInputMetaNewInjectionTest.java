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

package org.pentaho.di.trans.steps.s3csvinput;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class S3CsvInputMetaNewInjectionTest extends BaseMetadataInjectionTest<S3CsvInputMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new S3CsvInputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "AWS_ACCESS_KEY", new StringGetter() {
      public String get() {
        return meta.getAwsAccessKey();
      }
    } );
    check( "AWS_SECRET_KEY", new StringGetter() {
      public String get() {
        return meta.getAwsSecretKey();
      }
    } );
    check( "BUCKET", new StringGetter() {
      public String get() {
        return meta.getBucket();
      }
    } );
    check( "FILENAME", new StringGetter() {
      public String get() {
        return meta.getFilename();
      }
    } );
    check( "FILENAME_FIELD", new StringGetter() {
      public String get() {
        return meta.getFilenameField();
      }
    } );
    check( "SEPARATOR", new StringGetter() {
      public String get() {
        return meta.getDelimiter();
      }
    } );
    check( "ENCLOSURE", new StringGetter() {
      public String get() {
        return meta.getEnclosure();
      }
    } );
    check( "HEADER_PRESENT", new BooleanGetter() {
      public boolean get() {
        return meta.isHeaderPresent();
      }
    } );
    check( "MAX_LINE_SIZE", new StringGetter() {
      public String get() {
        return meta.getMaxLineSize();
      }
    } );
    check( "LAZY_CONVERSION_ACTIVE", new BooleanGetter() {
      public boolean get() {
        return meta.isLazyConversionActive();
      }
    } );
    check( "RUNNING_IN_PARALLEL", new BooleanGetter() {
      public boolean get() {
        return meta.isRunningInParallel();
      }
    } );
    check( "ROW_NUMBER_FIELD", new StringGetter() {
      public String get() {
        return meta.getRowNumField();
      }
    } );
    check( "INCLUDE_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isIncludingFilename();
      }
    } );

    check( "INPUT_NAME", new StringGetter() {
      public String get() {
        return meta.getInputFields()[0].getName();
      }
    } );
    check( "INPUT_POSITION", new IntGetter() {
      public int get() {
        return meta.getInputFields()[0].getPosition();
      }
    } );
    check( "INPUT_PRECISION", new IntGetter() {
      public int get() {
        return meta.getInputFields()[0].getPrecision();
      }
    } );
    check( "INPUT_CURRENCY", new StringGetter() {
      public String get() {
        return meta.getInputFields()[0].getCurrencySymbol();
      }
    } );
    check( "INPUT_DECIMAL", new StringGetter() {
      public String get() {
        return meta.getInputFields()[0].getDecimalSymbol();
      }
    } );
    check( "INPUT_GROUP", new StringGetter() {
      public String get() {
        return meta.getInputFields()[0].getGroupSymbol();
      }
    } );
    check( "INPUT_NULL_STRING", new StringGetter() {
      public String get() {
        return meta.getInputFields()[0].getNullString();
      }
    } );
    check( "INPUT_IF_NULL", new StringGetter() {
      public String get() {
        return meta.getInputFields()[0].getIfNullValue();
      }
    } );
    check( "INPUT_LENGTH", new IntGetter() {
      public int get() {
        return meta.getInputFields()[0].getLength();
      }
    } );
    check( "INPUT_FORMAT", new StringGetter() {
      public String get() {
        return meta.getInputFields()[0].getFormat();
      }
    } );
    skipPropertyTest( "INPUT_REPEAT" );
    skipPropertyTest( "INPUT_IGNORE" );
  }

}
