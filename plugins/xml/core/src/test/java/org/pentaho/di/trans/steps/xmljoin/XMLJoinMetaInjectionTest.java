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


package org.pentaho.di.trans.steps.xmljoin;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

/**
 * Created by ecuellar on 3/3/2016.
 */
public class XMLJoinMetaInjectionTest extends BaseMetadataInjectionTest<XMLJoinMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new XMLJoinMeta() );
  }

  @Test
  public void test() throws Exception {

    check( "COMPLEX_JOIN", new BooleanGetter() {
      public boolean get() {
        return meta.isComplexJoin();
      }
    } );

    check( "TARGET_XML_STEP", new StringGetter() {
      public String get() {
        return meta.getTargetXMLstep();
      }
    } );

    check( "TARGET_XML_FIELD", new StringGetter() {
      public String get() {
        return meta.getTargetXMLfield();
      }
    } );

    check( "SOURCE_XML_FIELD", new StringGetter() {
      public String get() {
        return meta.getSourceXMLfield();
      }
    } );

    check( "VALUE_XML_FIELD", new StringGetter() {
      public String get() {
        return meta.getValueXMLfield();
      }
    } );

    check( "TARGET_XPATH", new StringGetter() {
      public String get() {
        return meta.getTargetXPath();
      }
    } );

    check( "SOURCE_XML_STEP", new StringGetter() {
      public String get() {
        return meta.getSourceXMLstep();
      }
    } );

    check( "JOIN_COMPARE_FIELD", new StringGetter() {
      public String get() {
        return meta.getJoinCompareField();
      }
    } );

    check( "ENCODING", new StringGetter() {
      public String get() {
        return meta.getEncoding();
      }
    } );

    check( "OMIT_XML_HEADER", new BooleanGetter() {
      public boolean get() {
        return meta.isOmitXMLHeader();
      }
    } );

    check( "OMIT_NULL_VALUES", new BooleanGetter() {
      public boolean get() {
        return meta.isOmitNullValues();
      }
    } );
  }
}
