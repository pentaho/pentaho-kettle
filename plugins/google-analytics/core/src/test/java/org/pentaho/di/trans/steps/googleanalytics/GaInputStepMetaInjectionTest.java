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

package org.pentaho.di.trans.steps.googleanalytics;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class GaInputStepMetaInjectionTest extends BaseMetadataInjectionTest<GaInputStepMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void init() throws KettleException {
    // added for ValueMetaFactory.getIdForValueMeta to work for OUTPUT_TYPE
    KettleClientEnvironment.init();
  }

  @Before
  public void setup() {
    setup( new GaInputStepMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "OAUTH_SERVICE_EMAIL", new StringGetter() {
      public String get() {
        return meta.getOAuthServiceAccount();
      }
    } );
    check( "OAUTH_KEYFILE", new StringGetter() {
      public String get() {
        return meta.getOAuthKeyFile();
      }
    } );
    check( "APPLICATION_NAME", new StringGetter() {
      public String get() {
        return meta.getGaAppName();
      }
    } );
    check( "PROFILE_TABLE", new StringGetter() {
      public String get() {
        return meta.getGaProfileTableId();
      }
    } );
    check( "PROFILE_NAME", new StringGetter() {
      public String get() {
        return meta.getGaProfileName();
      }
    } );
    check( "USE_CUSTOM_TABLE_ID", new BooleanGetter() {
      public boolean get() {
        return meta.isUseCustomTableId();
      }
    } );
    check( "CUSTOM_TABLE_ID", new StringGetter() {
      public String get() {
        return meta.getGaCustomTableId();
      }
    } );
    check( "START_DATE", new StringGetter() {
      public String get() {
        return meta.getStartDate();
      }
    } );
    check( "END_DATE", new StringGetter() {
      public String get() {
        return meta.getEndDate();
      }
    } );
    check( "DIMENSIONS", new StringGetter() {
      public String get() {
        return meta.getDimensions();
      }
    } );
    check( "METRICS", new StringGetter() {
      public String get() {
        return meta.getMetrics();
      }
    } );
    check( "FILTERS", new StringGetter() {
      public String get() {
        return meta.getFilters();
      }
    } );
    check( "SORT", new StringGetter() {
      public String get() {
        return meta.getSort();
      }
    } );
    check( "USE_SEGMENT", new BooleanGetter() {
      public boolean get() {
        return meta.isUseSegment();
      }
    } );
    check( "USE_CUSTOM_SEGMENT", new BooleanGetter() {
      public boolean get() {
        return meta.isUseCustomSegment();
      }
    } );
    check( "ROW_LIMIT", new IntGetter() {
      public int get() {
        return meta.getRowLimit();
      }
    } );
    check( "CUSTOM_SEGMENT", new StringGetter() {
      public String get() {
        return meta.getCustomSegment();
      }
    } );
    check( "SEGMENT_NAME", new StringGetter() {
      public String get() {
        return meta.getSegmentName();
      }
    } );
    check( "SEGMENT_ID", new StringGetter() {
      public String get() {
        return meta.getSegmentId();
      }
    } );
    check( "FEED_FIELD", new StringGetter() {
      public String get() {
        return meta.getFeedField()[0];
      }
    } );
    check( "FEED_FIELD_TYPE", new StringGetter() {
      public String get() {
        return meta.getFeedFieldType()[0];
      }
    } );
    check( "OUTPUT_FIELD", new StringGetter() {
      public String get() {
        return meta.getOutputField()[0];
      }
    } );
    check( "CONVERSION_MASK", new StringGetter() {
      public String get() {
        return meta.getConversionMask()[0];
      }
    } );

    int[] typeInts = new int[ ValueMetaInterface.typeCodes.length ];
    for ( int i = 0; i < typeInts.length; i++ ) {
      typeInts[i] = i;
    }
    checkStringToInt( "OUTPUT_TYPE", new IntGetter() {
      public int get() {
        return meta.getOutputType()[0];
      }
    }, ValueMetaInterface.typeCodes, typeInts );
  }
}
