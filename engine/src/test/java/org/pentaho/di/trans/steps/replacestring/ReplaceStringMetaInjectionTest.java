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

package org.pentaho.di.trans.steps.replacestring;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

/**
 * Created by bmorrise on 3/21/16.
 */
public class ReplaceStringMetaInjectionTest extends BaseMetadataInjectionTest<ReplaceStringMeta> {

  @Before
  public void setup() {
    setup( new ReplaceStringMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_IN_STREAM", new StringGetter() {
      @Override public String get() {
        return meta.getFieldInStream()[ 0 ];
      }
    } );
    check( "FIELD_OUT_STREAM", new StringGetter() {
      @Override public String get() {
        return meta.getFieldOutStream()[ 0 ];
      }
    } );
    check( "USE_REGEX", new IntGetter() {
      @Override public int get() {
        return meta.getUseRegEx()[ 0 ];
      }
    } );
    check( "REPLACE_STRING", new StringGetter() {
      @Override public String get() {
        return meta.getReplaceString()[ 0 ];
      }
    } );
    check( "REPLACE_BY", new StringGetter() {
      @Override public String get() {
        return meta.getReplaceByString()[ 0 ];
      }
    } );
    check( "EMPTY_STRING", new BooleanGetter() {
      @Override public boolean get() {
        return meta.isSetEmptyString()[ 0 ];
      }
    } );
    check( "REPLACE_WITH_FIELD", new StringGetter() {
      @Override public String get() {
        return meta.getFieldReplaceByString()[ 0 ];
      }
    } );
    check( "REPLACE_WHOLE_WORD", new IntGetter() {
      @Override public int get() {
        return meta.getWholeWord()[ 0 ];
      }
    } );
    check( "CASE_SENSITIVE", new IntGetter() {
      @Override public int get() {
        return meta.getCaseSensitive()[ 0 ];
      }
    } );
    check( "IS_UNICODE", new IntGetter() {
      @Override public int get() {
        return meta.isUnicode()[ 0 ];
      }
    } );
  }
}
