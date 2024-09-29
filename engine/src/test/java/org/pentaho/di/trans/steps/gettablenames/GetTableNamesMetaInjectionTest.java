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

package org.pentaho.di.trans.steps.gettablenames;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class GetTableNamesMetaInjectionTest extends BaseMetadataInjectionTest<GetTableNamesMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new GetTableNamesMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SCHEMANAME", new StringGetter() {
      public String get() {
        return meta.getSchemaName();
      }
    } );
    check( "TABLENAMEFIELDNAME", new StringGetter() {
      public String get() {
        return meta.getTablenameFieldName();
      }
    } );
    check( "SQLCREATIONFIELDNAME", new StringGetter() {
      public String get() {
        return meta.getSQLCreationFieldName();
      }
    } );
    check( "OBJECTTYPEFIELDNAME", new StringGetter() {
      public String get() {
        return meta.getObjectTypeFieldName();
      }
    } );
    check( "ISSYSTEMOBJECTFIELDNAME", new StringGetter() {
      public String get() {
        return meta.isSystemObjectFieldName();
      }
    } );
    check( "INCLUDECATALOG", new BooleanGetter() {
        public boolean get() {
          return meta.isIncludeCatalog();
        }
      } );
    check( "INCLUDESCHEMA", new BooleanGetter() {
        public boolean get() {
          return meta.isIncludeSchema();
        }
      } );
    check( "INCLUDETABLE", new BooleanGetter() {
        public boolean get() {
          return meta.isIncludeTable();
        }
      } );
    check( "INCLUDEVIEW", new BooleanGetter() {
        public boolean get() {
          return meta.isIncludeView();
        }
      } );
    check( "INCLUDEPROCEDURE", new BooleanGetter() {
        public boolean get() {
          return meta.isIncludeProcedure();
        }
      } );
    check( "INCLUDESYNONYM", new BooleanGetter() {
        public boolean get() {
          return meta.isIncludeSynonym();
        }
      } );
    check( "ADDSCHEMAINOUTPUT", new BooleanGetter() {
        public boolean get() {
          return meta.isAddSchemaInOut();
        }
      } );
    check( "DYNAMICSCHEMA", new BooleanGetter() {
        public boolean get() {
          return meta.isDynamicSchema();
        }
      } );
    check( "SCHEMANAMEFIELD", new StringGetter() {
        public String get() {
          return meta.getSchemaFieldName();
        }
      } );
    check( "CONNECTIONNAME", new StringGetter() {
        public String get() {
          return "My Connection";
        }
      }, "My Connection" );
  }
}
