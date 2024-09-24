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
