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

package org.pentaho.di.trans.steps.textfileoutput;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

public class TextFileOutputMetaInjectionTest extends BaseMetadataInjectionTest<TextFileOutputMeta> {
  @Before
  public void setup() {
    setup( new TextFileOutputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FILENAME", new StringGetter() {
      public String get() {
        return meta.getFileName();
      }
    } );
    check( "RUN_AS_COMMAND", new BooleanGetter() {
      public boolean get() {
        return meta.isFileAsCommand();
      }
    } );
    check( "PASS_TO_SERVLET", new BooleanGetter() {
      public boolean get() {
        return meta.isServletOutput();
      }
    } );
    check( "CREATE_PARENT_FOLDER", new BooleanGetter() {
      public boolean get() {
        return meta.isCreateParentFolder();
      }
    } );
    check( "EXTENSION", new StringGetter() {
      public String get() {
        return meta.getExtension();
      }
    } );
    check( "SEPARATOR", new StringGetter() {
      public String get() {
        return meta.getSeparator();
      }
    } );
    check( "ENCLOSURE", new StringGetter() {
      public String get() {
        return meta.getEnclosure();
      }
    } );
    check( "FORCE_ENCLOSURE", new BooleanGetter() {
      public boolean get() {
        return meta.isEnclosureForced();
      }
    } );
    check( "DISABLE_ENCLOSURE_FIX", new BooleanGetter() {
      public boolean get() {
        return meta.isEnclosureFixDisabled();
      }
    } );
    check( "HEADER", new BooleanGetter() {
      public boolean get() {
        return meta.isHeaderEnabled();
      }
    } );
    check( "FOOTER", new BooleanGetter() {
      public boolean get() {
        return meta.isFooterEnabled();
      }
    } );
    check( "FORMAT", new StringGetter() {
      public String get() {
        return meta.getFileFormat();
      }
    } );
    check( "COMPRESSION", new StringGetter() {
      public String get() {
        return meta.getFileCompression();
      }
    } );
    check( "SPLIT_EVERY", new IntGetter() {
      public int get() {
        return meta.getSplitEvery();
      }
    } );
    check( "APPEND", new BooleanGetter() {
      public boolean get() {
        return meta.isFileAppended();
      }
    } );
    check( "INC_STEPNR_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isStepNrInFilename();
      }
    } );
    check( "INC_PARTNR_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isPartNrInFilename();
      }
    } );
    check( "INC_DATE_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isDateInFilename();
      }
    } );
    check( "INC_TIME_IN_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.isTimeInFilename();
      }
    } );
    check( "RIGHT_PAD_FIELDS", new BooleanGetter() {
      public boolean get() {
        return meta.isPadded();
      }
    } );
    check( "FAST_DATA_DUMP", new BooleanGetter() {
      public boolean get() {
        return meta.isFastDump();
      }
    } );
    check( "ENCODING", new StringGetter() {
      public String get() {
        return meta.getEncoding();
      }
    } );
    check( "ADD_ENDING_LINE", new StringGetter() {
      public String get() {
        return meta.getEndedLine();
      }
    } );
    check( "FILENAME_IN_FIELD", new BooleanGetter() {
      public boolean get() {
        return meta.isFileNameInField();
      }
    } );
    check( "FILENAME_FIELD", new StringGetter() {
      public String get() {
        return meta.getFileNameField();
      }
    } );
    check( "NEW_LINE", new StringGetter() {
      public String get() {
        return meta.getNewline();
      }
    } );
    check( "ADD_TO_RESULT", new BooleanGetter() {
      public boolean get() {
        return meta.isAddToResultFiles();
      }
    } );
    check( "DO_NOT_CREATE_FILE_AT_STARTUP", new BooleanGetter() {
      public boolean get() {
        return meta.isDoNotOpenNewFileInit();
      }
    } );
    check( "SPECIFY_DATE_FORMAT", new BooleanGetter() {
      public boolean get() {
        return meta.isSpecifyingFormat();
      }
    } );
    check( "DATE_FORMAT", new StringGetter() {
      public String get() {
        return meta.getDateTimeFormat();
      }
    } );

    /////////////////////////////
    check( "OUTPUT_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getName();
      }
    } );

    // TODO check field type plugins
    skipPropertyTest( "OUTPUT_TYPE" );

    check( "OUTPUT_FORMAT", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getFormat();
      }
    } );
    check( "OUTPUT_LENGTH", new IntGetter() {
      public int get() {
        return meta.getOutputFields()[0].getLength();
      }
    } );
    check( "OUTPUT_PRECISION", new IntGetter() {
      public int get() {
        return meta.getOutputFields()[0].getPrecision();
      }
    } );
    check( "OUTPUT_CURRENCY", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getCurrencySymbol();
      }
    } );
    check( "OUTPUT_DECIMAL", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getDecimalSymbol();
      }
    } );
    check( "OUTPUT_GROUP", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getGroupingSymbol();
      }
    } );
    check( "OUTPUT_NULL", new StringGetter() {
      public String get() {
        return meta.getOutputFields()[0].getNullString();
      }
    } );

    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "OUTPUT_TRIM", setValue( mftt, "left" ), "f" );
    assertEquals( 1, meta.getOutputFields()[0].getTrimType() );
    injector.setProperty( meta, "OUTPUT_TRIM", setValue( mftt, "right" ), "f" );
    assertEquals( 2, meta.getOutputFields()[0].getTrimType() );
    skipPropertyTest( "OUTPUT_TRIM" );
  }
}
