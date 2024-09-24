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

package org.pentaho.di.trans.steps.replacestring;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

/**
 * Created by bmorrise on 3/21/16.
 */
public class ReplaceStringMetaInjectionTest extends BaseMetadataInjectionTest<ReplaceStringMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
    check( "USE_REGEX", new BooleanGetter() {
      @Override public boolean get() {
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
    check( "REPLACE_WHOLE_WORD", new BooleanGetter() {
      @Override public boolean get() {
        return meta.getWholeWord()[ 0 ];
      }
    } );
    check( "CASE_SENSITIVE", new BooleanGetter() {
      @Override public boolean get() {
        return meta.getCaseSensitive()[ 0 ];
      }
    } );
    check( "IS_UNICODE", new BooleanGetter() {
      @Override public boolean get() {
        return meta.isUnicode()[ 0 ];
      }
    } );
  }
}
