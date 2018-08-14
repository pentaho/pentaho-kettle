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

package org.pentaho.di.trans.steps.metainject;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class MetaInjectMetaInjectionTest extends BaseMetadataInjectionTest<MetaInjectMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String TEST_ID = "TEST_ID";

  @Before
  public void setup() {
    setup( new MetaInjectMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "TRANS_NAME", new StringGetter() {
      public String get() {
        return meta.getTransName();
      }
    } );
    check( "FILE_NAME", new StringGetter() {
      public String get() {
        return meta.getFileName();
      }
    } );
    check( "DIRECTORY_PATH", new StringGetter() {
      public String get() {
        return meta.getDirectoryPath();
      }
    } );
    check( "SOURCE_STEP_NAME", new StringGetter() {
      public String get() {
        return meta.getSourceStepName();
      }
    } );
    check( "TARGET_FILE", new StringGetter() {
      public String get() {
        return meta.getTargetFile();
      }
    } );
    check( "NO_EXECUTION", new BooleanGetter() {
      public boolean get() {
        return meta.isNoExecution();
      }
    } );
    check( "STREAMING_SOURCE_STEP", new StringGetter() {
      public String get() {
        return meta.getStreamSourceStepname();
      }
    } );
    check( "STREAMING_TARGET_STEP", new StringGetter() {
      public String get() {
        return meta.getStreamTargetStepname();
      }
    } );
    check( "SOURCE_OUTPUT_NAME", new StringGetter() {
      public String get() {
        return meta.getSourceOutputFields().get( 0 ).getName();
      }
    } );
    String[] typeNames = ValueMetaBase.getAllTypes();

    checkStringToInt( "SOURCE_OUTPUT_TYPE", new IntGetter() {
      public int get() {
        return meta.getSourceOutputFields().get( 0 ).getType();
      }
    }, typeNames, getTypeCodes( typeNames ) );
    check( "SOURCE_OUTPUT_LENGTH", new IntGetter() {
      public int get() {
        return meta.getSourceOutputFields().get( 0 ).getLength();
      }
    } );
    check( "SOURCE_OUTPUT_PRECISION", new IntGetter() {
      public int get() {
        return meta.getSourceOutputFields().get( 0 ).getPrecision();
      }
    } );
    check( "MAPPING_SOURCE_STEP", new StringGetter() {
      public String get() {
        return meta.getMetaInjectMapping().get( 0 ).getSourceStep();
      }
    } );
    check( "MAPPING_SOURCE_FIELD", new StringGetter() {
      public String get() {
        return meta.getMetaInjectMapping().get( 0 ).getSourceField();
      }
    } );
    check( "MAPPING_TARGET_STEP", new StringGetter() {
      public String get() {
        return meta.getMetaInjectMapping().get( 0 ).getTargetStep();
      }
    } );
    check( "MAPPING_TARGET_FIELD", new StringGetter() {
      public String get() {
        return meta.getMetaInjectMapping().get( 0 ).getTargetField();
      }
    } );
    check( "TRANS_SEPECIFICATION_METHOD", new EnumGetter() {
      @Override
      public Enum<?> get() {
        return meta.getSpecificationMethod();
      }

    }, ObjectLocationSpecificationMethod.class );

    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "TRANS_OBJECT_ID", setValue( mftt, TEST_ID ), "f" );
    assertEquals( TEST_ID, meta.getTransObjectId().getId() );
    skipPropertyTest( "TRANS_OBJECT_ID" );
  }

}
