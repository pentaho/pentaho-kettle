/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.stringcut;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

/**
 * Created by moliveira on 08/04/18.
 */
public class StringCutMetaInjectionTest extends BaseMetadataInjectionTest<StringCutMeta> {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new StringCutMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_IN_STREAM", () ->  meta.getFieldInStream()[0] );
    check( "FIELD_OUT_STREAM", () ->  meta.getFieldOutStream()[0] );
    check( "CUT_FROM", () ->  meta.getCutFrom()[0] );
    check( "CUT_TO", () ->  meta.getCutTo()[0] );
  }
}
