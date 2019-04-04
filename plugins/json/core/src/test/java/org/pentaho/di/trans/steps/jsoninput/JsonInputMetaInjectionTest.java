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

package org.pentaho.di.trans.steps.jsoninput;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JsonInputMetaInjectionTest extends BaseMetadataInjectionTest<JsonInputMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new JsonInputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FILE_NAME_OUTPUT", new BooleanGetter() {
      public boolean get() {
        return meta.includeFilename();
      }
    } );
    check( "FILE_NAME_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getFilenameField();
      }
    } );
    check( "ROW_NUMBER_OUTPUT", new BooleanGetter() {
      public boolean get() {
        return meta.includeRowNumber();
      }
    } );
    check( "ROW_NUMBER_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getRowNumberField();
      }
    } );
    check( "ROW_LIMIT", new LongGetter() {
      public long get() {
        return meta.getRowLimit();
      }
    } );
    check( "ADD_RESULT_FILE", new BooleanGetter() {
      public boolean get() {
        return meta.addResultFile();
      }
    } );
    check( "IGNORE_EMPTY_FILE", new BooleanGetter() {
      public boolean get() {
        return meta.isIgnoreEmptyFile();
      }
    } );
    check( "DO_NOT_FAIL_IF_NO_FILE", new BooleanGetter() {
      public boolean get() {
        return meta.isDoNotFailIfNoFile();
      }
    } );
    check( "IGNORE_MISSING_PATH", new BooleanGetter() {
      public boolean get() {
        return meta.isIgnoreMissingPath();
      }
    } );
    check( "READ_SOURCE_AS_URL", new BooleanGetter() {
      public boolean get() {
        return meta.isReadUrl();
      }
    } );
    check( "REMOVE_SOURCE_FIELDS", new BooleanGetter() {
      public boolean get() {
        return meta.isRemoveSourceField();
      }
    } );
    check( "FILE_SHORT_FILE_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.additionalOutputFields.shortFilenameField;
      }
    } );
    check( "FILE_EXTENSION_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.additionalOutputFields.extensionField;
      }
    } );
    check( "FILE_PATH_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.additionalOutputFields.pathField;
      }
    } );
    check( "FILE_SIZE_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.additionalOutputFields.sizeField;
      }
    } );
    check( "FILE_HIDDEN_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.additionalOutputFields.hiddenField;
      }
    } );
    check( "FILE_LAST_MODIFICATION_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.additionalOutputFields.lastModificationField;
      }
    } );
    check( "FILE_URI_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.additionalOutputFields.uriField;
      }
    } );
    check( "FILE_ROOT_URI_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.additionalOutputFields.rootUriField;
      }
    } );
    check( "FILENAME", new StringGetter() {
      public String get() {
        return meta.inputFiles.fileName[0];
      }
    } );
    check( "FILEMASK", new StringGetter() {
      public String get() {
        return meta.inputFiles.fileMask[0];
      }
    } );
    check( "EXCLUDE_FILEMASK", new StringGetter() {
      public String get() {
        return meta.inputFiles.excludeFileMask[0];
      }
    } );
    check( "FILE_REQUIRED", new StringGetter() {
      public String get() {
        return meta.inputFiles.fileRequired[0];
      }
    } );
    check( "INCLUDE_SUBFOLDERS", new StringGetter() {
      public String get() {
        return meta.inputFiles.includeSubFolders[0];
      }
    } );
    check( "FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getName();
      }
    } );
    check( "FIELD_PATH", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getPath();
      }
    } );
    check( "FIELD_LENGTH", new IntGetter() {
      public int get() {
        return meta.inputFields[0].getLength();
      }
    } );
    check( "FIELD_FORMAT", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getFormat();
      }
    } );
    check( "FIELD_PRECISION", new IntGetter() {
      public int get() {
        return meta.inputFields[0].getPrecision();
      }
    } );
    check( "FIELD_CURRENCY", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getCurrencySymbol();
      }
    } );
    check( "FIELD_DECIMAL", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getDecimalSymbol();
      }
    } );
    check( "FIELD_GROUP", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getGroupSymbol();
      }
    } );
    check( "FIELD_REPEAT", new BooleanGetter() {
      public boolean get() {
        return meta.inputFields[0].isRepeated();
      }
    } );
    check( "SOURCE_FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.getFieldValue();
      }
    } );
    check( "SOURCE_IN_FIELD", new BooleanGetter() {
      public boolean get() {
        return meta.isInFields();
      }
    } );
    check( "SOURCE_FIELD_IS_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.getIsAFile();
      }
    } );

    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "FIELD_TRIM_TYPE", setValue( mftt, "left" ), "f" );
    assertEquals( 1, meta.inputFields[0].getTrimType() );
    injector.setProperty( meta, "FIELD_TRIM_TYPE", setValue( mftt, "right" ), "f" );
    assertEquals( 2, meta.inputFields[0].getTrimType() );
    skipPropertyTest( "FIELD_TRIM_TYPE" );

    skipPropertyTest( "FIELD_TYPE" );
  }
}
