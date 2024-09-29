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


package org.pentaho.di.trans.steps.dimensionlookup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class DimensionLookupMetaInjectionTest extends BaseMetadataInjectionTest<DimensionLookupMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    super.setup( new DimensionLookupMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "TARGET_SCHEMA", new StringGetter() {
      @Override
      public String get() {
        return meta.getSchemaName();
      }
    } );
    check( "TARGET_TABLE", new StringGetter() {
      @Override
      public String get() {
        return meta.getTableName();
      }
    } );
    check( "UPDATE_DIMENSION", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.isUpdate();
      }
    } );
    check( "KEY_STREAM_FIELDNAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyStream()[0];
      }
    } );
    check( "KEY_DATABASE_FIELDNAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyLookup()[0];
      }
    } );
    check( "STREAM_DATE_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getDateField();
      }
    } );
    check( "DATE_RANGE_START_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getDateFrom();
      }
    } );
    check( "DATE_RANGE_END_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getDateTo();
      }
    } );
    check( "DATABASE_STREAM_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getFieldStream()[0];
      }
    } );
    check( "DATABASE_FIELD_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getFieldLookup()[0];
      }
    } );
    check( "TECHNICAL_KEY_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyField();
      }
    } );
    check( "TECHNICAL_KEY_NEW_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getKeyRename();
      }
    } );
    check( "VERSION_FIELD", new StringGetter() {
      @Override
      public String get() {
        return meta.getVersionField();
      }
    } );
    check( "TECHNICAL_KEY_SEQUENCE", new StringGetter() {
      @Override
      public String get() {
        return meta.getSequenceName();
      }
    } );
    check( "COMMIT_SIZE", new IntGetter() {
      @Override
      public int get() {
        return meta.getCommitSize();
      }
    } );
    check( "MIN_YEAR", new IntGetter() {
      @Override
      public int get() {
        return meta.getMinYear();
      }
    } );
    check( "MAX_YEAR", new IntGetter() {
      @Override
      public int get() {
        return meta.getMaxYear();
      }
    } );
    check( "TECHNICAL_KEY_CREATION", new StringGetter() {
      @Override
      public String get() {
        return meta.getTechKeyCreation();
      }
    } );
    check( "CACHE_SIZE", new IntGetter() {
      @Override
      public int get() {
        return meta.getCacheSize();
      }
    } );
    check( "USE_ALTERNATIVE_START_DATE", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.isUsingStartDateAlternative();
      }
    } );
    check( "ALTERNATIVE_START_COLUMN", new StringGetter() {
      @Override
      public String get() {
        return meta.getStartDateFieldName();
      }
    } );
    check( "PRELOAD_CACHE", new BooleanGetter() {
      @Override
      public boolean get() {
        return meta.isPreloadingCache();
      }
    } );
    check( "CONNECTION_NAME", new StringGetter() {
      public String get() {
        return "My Connection";
      }
    }, "My Connection" );

    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "ALTERNATIVE_START_OPTION", setValue( mftt, DimensionLookupMeta
        .getStartDateAlternativeCode( 0 ) ), "f" );
    Assert.assertEquals( 0, meta.getStartDateAlternative() );

    String[] valueMetaNames = ValueMetaFactory.getValueMetaNames();
    checkStringToInt( "TYPE_OF_RETURN_FIELD", new IntGetter() {
      @Override
      public int get() {
        return meta.getReturnType()[0];
      }
    }, valueMetaNames, getTypeCodes( valueMetaNames ) );

    skipPropertyTest( "ALTERNATIVE_START_OPTION" );

    skipPropertyTest( "UPDATE_TYPE" );
  }

}
