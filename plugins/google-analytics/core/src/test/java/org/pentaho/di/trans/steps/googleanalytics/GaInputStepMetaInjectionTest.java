/*! ******************************************************************************
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
