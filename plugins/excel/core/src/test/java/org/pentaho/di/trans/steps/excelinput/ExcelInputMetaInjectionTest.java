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


package org.pentaho.di.trans.steps.excelinput;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class ExcelInputMetaInjectionTest extends BaseMetadataInjectionTest<ExcelInputMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new ExcelInputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "NAME", new StringGetter() {
      public String get() {
        return meta.getField()[0].getName();
      }
    } );
    check( "LENGTH", new IntGetter() {
      public int get() {
        return meta.getField()[0].getLength();
      }
    } );
    check( "PRECISION", new IntGetter() {
      public int get() {
        return meta.getField()[0].getPrecision();
      }
    } );
    int[] trimInts = new int[ ValueMetaBase.trimTypeCode.length ];
    for ( int i = 0; i < trimInts.length; i++ ) {
      trimInts[i] = i;
    }
    checkStringToInt( "TRIM_TYPE", new IntGetter() {
      public int get() {
        return meta.getField()[0].getTrimType();
      }
    }, ValueMetaBase.trimTypeCode, trimInts );
    check( "FORMAT", new StringGetter() {
      public String get() {
        return meta.getField()[0].getFormat();
      }
    } );
    check( "CURRENCY", new StringGetter() {
      public String get() {
        return meta.getField()[0].getCurrencySymbol();
      }
    } );
    check( "DECIMAL", new StringGetter() {
      public String get() {
        return meta.getField()[0].getDecimalSymbol();
      }
    } );
    check( "GROUP", new StringGetter() {
      public String get() {
        return meta.getField()[0].getGroupSymbol();
      }
    } );
    check( "REPEAT", new BooleanGetter() {
      public boolean get() {
        return meta.getField()[0].isRepeated();
      }
    } );

    // TODO check field type plugins
    skipPropertyTest( "TYPE" );

    check( "SHEET_NAME", new StringGetter() {
      public String get() {
        return meta.getSheetName()[0];
      }
    } );
    check( "SHEET_START_ROW", new IntGetter() {
      public int get() {
        return meta.getStartRow()[0];
      }
    } );
    check( "SHEET_START_COL", new IntGetter() {
      public int get() {
        return meta.getStartColumn()[0];
      }
    } );
    check( "FILENAME", new StringGetter() {
      public String get() {
        return meta.getFileName()[0];
      }
    } );
    check( "FILEMASK", new StringGetter() {
      public String get() {
        return meta.getFileMask()[0];
      }
    } );
    check( "EXCLUDE_FILEMASK", new StringGetter() {
      public String get() {
        return meta.getExludeFileMask()[0];
      }
    } );
    check( "FILE_REQUIRED", new StringGetter() {
      public String get() {
        return meta.getFileRequired()[0];
      }
    } );
    check( "INCLUDE_SUBFOLDERS", new StringGetter() {
      public String get() {
        return meta.getIncludeSubFolders()[0];
      }
    } );
    check( "SPREADSHEET_TYPE", new EnumGetter() {
      public SpreadSheetType get() {
        return meta.getSpreadSheetType();
      }
    }, SpreadSheetType.class );
  }
}
