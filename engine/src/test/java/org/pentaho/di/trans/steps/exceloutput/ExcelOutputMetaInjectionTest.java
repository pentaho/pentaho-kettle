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

package org.pentaho.di.trans.steps.exceloutput;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

public class ExcelOutputMetaInjectionTest extends BaseMetadataInjectionTest<ExcelOutputMeta> {
  @Before
  public void setup() {
    setup( new ExcelOutputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "HEADER_FONT_SIZE", new StringGetter() {
      public String get() {
        return meta.getHeaderFontSize();
      }
    } );
    check( "HEADER_FONT_BOLD", new BooleanGetter() {
      public boolean get() {
        return meta.isHeaderFontBold();
      }
    } );
    check( "HEADER_FONT_ITALIC", new BooleanGetter() {
      public boolean get() {
        return meta.isHeaderFontItalic();
      }
    } );
    check( "HEADER_FONT_COLOR", new IntGetter() {
      public int get() {
        return meta.getHeaderFontColor();
      }
    } );
    check( "HEADER_BACKGROUND_COLOR", new IntGetter() {
      public int get() {
        return meta.getHeaderBackGroundColor();
      }
    } );
    check( "HEADER_ROW_HEIGHT", new IntGetter() {
      public int get() {
        return Integer.parseInt( meta.getHeaderRowHeight() );
      }
    } );
    check( "HEADER_IMAGE", new StringGetter() {
      public String get() {
        return meta.getHeaderImage();
      }
    } );
    check( "ROW_FONT_SIZE", new StringGetter() {
      public String get() {
        return meta.getRowFontSize();
      }
    } );
    check( "ROW_FONT_COLOR", new IntGetter() {
      public int get() {
        return meta.getRowFontColor();
      }
    } );
    check( "ROW_BACKGROUND_COLOR", new IntGetter() {
      public int get() {
        return meta.getRowBackGroundColor();
      }
    } );

    check( "FILENAME", new StringGetter() {
      public String get() {
        return meta.getFileName();
      }
    } );
    check( "EXTENSION", new StringGetter() {
      public String get() {
        return meta.getExtension();
      }
    } );
    check( "PASSWORD", new StringGetter() {
      public String get() {
        return meta.getPassword();
      }
    } );

    check( "HEADER_ENABLED", new BooleanGetter() {
      public boolean get() {
        return meta.isHeaderEnabled();
      }
    } );
    check( "FOOTER_ENABLED", new BooleanGetter() {
      public boolean get() {
        return meta.isFooterEnabled();
      }
    } );
    check( "SPLIT_EVERY", new IntGetter() {
      public int get() {
        return meta.getSplitEvery();
      }
    } );
    check( "STEP_NR_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isStepNrInFilename();
      }
    } );
    check( "DATE_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isDateInFilename();
      }
    } );
    check( "FILENAME_TO_RESULT", new BooleanGetter() {
      public boolean get() {
        return meta.isAddToResultFiles();
      }
    } );
    check( "PROTECT", new BooleanGetter() {
      public boolean get() {
        return meta.isSheetProtected();
      }
    } );
    check( "TIME_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isTimeInFilename();
      }
    } );
    check( "TEMPLATE", new BooleanGetter() {
      public boolean get() {
        return meta.isTemplateEnabled();
      }
    } );
    check( "TEMPLATE_FILENAME", new StringGetter() {
      public String get() {
        return meta.getTemplateFileName();
      }
    } );
    check( "TEMPLATE_APPEND", new BooleanGetter() {
      public boolean get() {
        return meta.isTemplateAppend();
      }
    } );
    check( "SHEET_NAME", new StringGetter() {
      public String get() {
        return meta.getSheetname();
      }
    } );
    check( "USE_TEMPFILES", new BooleanGetter() {
      public boolean get() {
        return meta.isUseTempFiles();
      }
    } );
    check( "TEMPDIR", new StringGetter() {
      public String get() {
        return meta.getTempDirectory();
      }
    } );

    check( "NAME", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getName();
      }
    } );
    // TODO check field type plugins
    skipPropertyTest( "TYPE" );

    check( "FORMAT", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getFormat();
      }
    } );

    check( "ENCODING", new StringGetter() {
      public String get() {
        return meta.getEncoding();
      }
    } );
    check( "NEWLINE", new StringGetter() {
      public String get() {
        return meta.getNewline();
      }
    } );
    check( "APPEND", new BooleanGetter() {
      public boolean get() {
        return meta.isAppend();
      }
    } );
    check( "DONT_OPEN_NEW_FILE", new BooleanGetter() {
      public boolean get() {
        return meta.isDoNotOpenNewFileInit();
      }
    } );
    check( "CREATE_PARENT_FOLDER", new BooleanGetter() {
      public boolean get() {
        return meta.isCreateParentFolder();
      }
    } );
    check( "DATE_FORMAT_SPECIFIED", new BooleanGetter() {
      public boolean get() {
        return meta.isSpecifyFormat();
      }
    } );
    check( "DATE_FORMAT", new StringGetter() {
      public String get() {
        return meta.getDateTimeFormat();
      }
    } );
    check( "AUTOSIZE_COLUMNS", new BooleanGetter() {
      public boolean get() {
        return meta.isAutoSizeColums();
      }
    } );
    check( "NULL_AS_BLANK", new BooleanGetter() {
      public boolean get() {
        return meta.isNullBlank();
      }
    } );

    checkStringToInt( "HEADER_FONT_NAME", new IntGetter() {
      public int get() {
        return meta.getHeaderFontName();
      }
    }, new String[] { "arial", "courier", "tahoma", "times" }, new int[] { 0, 1, 2, 3 } );

    checkStringToInt( "ROW_FONT_NAME", new IntGetter() {
      public int get() {
        return meta.getRowFontName();
      }
    }, new String[] { "arial", "courier", "tahoma", "times" }, new int[] { 0, 1, 2, 3 } );

    checkStringToInt( "HEADER_FONT_UNDERLINE", new IntGetter() {
      public int get() {
        return meta.getHeaderFontUnderline();
      }
    }, new String[] { "no", "single", "single_accounting", "double", "double_accounting" }, new int[] { 0, 1, 2, 3,
      4 } );
    checkStringToInt( "HEADER_FONT_ORIENTATION", new IntGetter() {
      public int get() {
        return meta.getHeaderFontOrientation();
      }
    }, new String[] { "horizontal", "minus_45", "minus_90", "plus_45", "plus_90", "stacked", "vertical" }, new int[] {
      0, 1, 2, 3, 4, 5, 6 } );
    checkStringToInt( "HEADER_ALIGNMENT", new IntGetter() {
      public int get() {
        return meta.getHeaderAlignment();
      }
    }, new String[] { "left", "right", "center", "fill", "general", "justify" }, new int[] { 0, 1, 2, 3, 4, 5 } );
  }
}
