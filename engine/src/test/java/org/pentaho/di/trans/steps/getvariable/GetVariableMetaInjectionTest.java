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


package org.pentaho.di.trans.steps.getvariable;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class GetVariableMetaInjectionTest extends BaseMetadataInjectionTest<GetVariableMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new GetVariableMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getFieldDefinitions()[0].getFieldName();
      }
    } );
    check( "VARIABLE", new StringGetter() {
      public String get() {
        return meta.getFieldDefinitions()[0].getVariableString();
      }
    } );
    check( "FIELDTYPE", new IntGetter() {
      public int get() {
        return meta.getFieldDefinitions()[0].getFieldType();
      }
    } );
    check( "FIELDFORMAT", new StringGetter() {
      public String get() {
        return meta.getFieldDefinitions()[0].getFieldFormat();
      }
    } );
    check( "FIELDLENGTH", new IntGetter() {
      public int get() {
        return meta.getFieldDefinitions()[0].getFieldLength();
      }
    } );
    check( "FIELDPRECISION", new IntGetter() {
      public int get() {
        return meta.getFieldDefinitions()[0].getFieldPrecision();
      }
    } );
    check( "CURRENCY", new StringGetter() {
      public String get() {
        return meta.getFieldDefinitions()[0].getCurrency();
      }
    } );
    check( "DECIMAL", new StringGetter() {
      public String get() {
        return meta.getFieldDefinitions()[0].getDecimal();
      }
    } );
    check( "GROUP", new StringGetter() {
      public String get() {
        return meta.getFieldDefinitions()[0].getGroup();
      }
    } );
    check( "TRIMTYPE", new IntGetter() {
      public int get() {
        return meta.getFieldDefinitions()[0].getTrimType();
      }
    } );
  }
}
