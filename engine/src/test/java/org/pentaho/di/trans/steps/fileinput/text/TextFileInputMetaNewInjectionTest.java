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

package org.pentaho.di.trans.steps.fileinput.text;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

public class TextFileInputMetaNewInjectionTest extends BaseMetadataInjectionTest<TextFileInputMeta> {
  @Before
  public void setup() {
    setup( new TextFileInputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FILE_TYPE", new StringGetter() {
      public String get() {
        return meta.content.fileType;
      }
    } );

    check( "SEPARATOR", new StringGetter() {
      public String get() {
        return meta.content.separator;
      }
    } );

    check( "ENCLOSURE", new StringGetter() {
      public String get() {
        return meta.content.enclosure;
      }
    } );

    check( "BREAK_IN_ENCLOSURE", new BooleanGetter() {
      public boolean get() {
        return meta.content.breakInEnclosureAllowed;
      }
    } );

    check( "ESCAPE_CHAR", new StringGetter() {
      public String get() {
        return meta.content.escapeCharacter;
      }
    } );

    check( "HEADER_PRESENT", new BooleanGetter() {
      public boolean get() {
        return meta.content.header;
      }
    } );

    check( "NR_HEADER_LINES", new IntGetter() {
      public int get() {
        return meta.content.nrHeaderLines;
      }
    } );

    check( "HAS_FOOTER", new BooleanGetter() {
      public boolean get() {
        return meta.content.footer;
      }
    } );

    check( "NR_FOOTER_LINES", new IntGetter() {
      public int get() {
        return meta.content.nrFooterLines;
      }
    } );

    check( "HAS_WRAPPED_LINES", new BooleanGetter() {
      public boolean get() {
        return meta.content.lineWrapped;
      }
    } );

    check( "NR_WRAPS", new IntGetter() {
      public int get() {
        return meta.content.nrWraps;
      }
    } );

    check( "HAS_PAGED_LAYOUT", new BooleanGetter() {
      public boolean get() {
        return meta.content.layoutPaged;
      }
    } );

    check( "NR_LINES_PER_PAGE", new IntGetter() {
      public int get() {
        return meta.content.nrLinesPerPage;
      }
    } );

    check( "NR_DOC_HEADER_LINES", new IntGetter() {
      public int get() {
        return meta.content.nrLinesDocHeader;
      }
    } );

    check( "COMPRESSION_TYPE", new StringGetter() {
      public String get() {
        return meta.content.fileCompression;
      }
    } );

    check( "NO_EMPTY_LINES", new BooleanGetter() {
      public boolean get() {
        return meta.content.noEmptyLines;
      }
    } );

    check( "INCLUDE_FILENAME", new BooleanGetter() {
      public boolean get() {
        return meta.content.includeFilename;
      }
    } );

    check( "FILENAME_FIELD", new StringGetter() {
      public String get() {
        return meta.content.filenameField;
      }
    } );

    check( "INCLUDE_ROW_NUMBER", new BooleanGetter() {
      public boolean get() {
        return meta.content.includeRowNumber;
      }
    } );

    check( "ROW_NUMBER_FIELD", new StringGetter() {
      public String get() {
        return meta.content.rowNumberField;
      }
    } );

    check( "ROW_NUMBER_BY_FILE", new BooleanGetter() {
      public boolean get() {
        return meta.content.rowNumberByFile;
      }
    } );

    check( "FILE_FORMAT", new StringGetter() {
      public String get() {
        return meta.content.fileFormat;
      }
    } );

    check( "ENCODING", new StringGetter() {
      public String get() {
        return meta.content.encoding;
      }
    } );

    check( "LENGTH", new StringGetter() {
      public String get() {
        return meta.content.length;
      }
    } );

    check( "ROW_LIMIT", new LongGetter() {
      public long get() {
        return meta.content.rowLimit;
      }
    } );

    check( "DATE_FORMAT_LENIENT", new BooleanGetter() {
      public boolean get() {
        return meta.content.dateFormatLenient;
      }
    } );

    check( "DATE_FORMAT_LOCALE", new StringGetter() {
      public String get() {
        return meta.content.dateFormatLocale.toString();
      }
    }, "en", "en_us" );

    ///////////////////////////////
    check( "FILTER_POSITION", new IntGetter() {
      public int get() {
        return meta.getFilter()[0].getFilterPosition();
      }
    } );

    check( "FILTER_STRING", new StringGetter() {
      public String get() {
        return meta.getFilter()[0].getFilterString();
      }
    } );

    check( "FILTER_LAST_LINE", new BooleanGetter() {
      public boolean get() {
        return meta.getFilter()[0].isFilterLastLine();
      }
    } );

    check( "FILTER_POSITIVE", new BooleanGetter() {
      public boolean get() {
        return meta.getFilter()[0].isFilterPositive();
      }
    } );

    ///////////////////////////////
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

    check( "ACCEPT_FILE_NAMES", new BooleanGetter() {
      public boolean get() {
        return meta.inputFiles.acceptingFilenames;
      }
    } );

    check( "ACCEPT_FILE_STEP", new StringGetter() {
      public String get() {
        return meta.inputFiles.acceptingStepName;
      }
    } );

    check( "PASS_THROUGH_FIELDS", new BooleanGetter() {
      public boolean get() {
        return meta.inputFiles.passingThruFields;
      }
    } );

    check( "ACCEPT_FILE_FIELD", new StringGetter() {
      public String get() {
        return meta.inputFiles.acceptingField;
      }
    } );

    check( "ADD_FILES_TO_RESULT", new BooleanGetter() {
      public boolean get() {
        return meta.inputFiles.isaddresult;
      }
    } );

    /////////////////////////////
    check( "FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getName();
      }
    } );

    check( "FIELD_POSITION", new IntGetter() {
      public int get() {
        return meta.inputFields[0].getPosition();
      }
    } );

    check( "FIELD_LENGTH", new IntGetter() {
      public int get() {
        return meta.inputFields[0].getLength();
      }
    } );

    // TODO check field type plugins
    // ValueMetaInterface mft = new ValueMetaString( "f" );
    // ValueMetaFactory.createValueMeta( "INTEGER", 5 );
    // injector.setProperty( meta, "FIELD_TYPE", setValue( mft, "INTEGER" ), "f" );
    // assertEquals( 5, meta.inputFiles.inputFields[0].getType() );
    skipPropertyTest( "FIELD_TYPE" );

    check( "FIELD_IGNORE", new BooleanGetter() {
      public boolean get() {
        return meta.inputFields[0].isIgnored();
      }
    } );

    check( "FIELD_FORMAT", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getFormat();
      }
    } );

    ValueMetaInterface mftt = new ValueMetaString( "f" );
    injector.setProperty( meta, "FIELD_TRIM_TYPE", setValue( mftt, "left" ), "f" );
    assertEquals( 1, meta.inputFields[0].getTrimType() );
    injector.setProperty( meta, "FIELD_TRIM_TYPE", setValue( mftt, "right" ), "f" );
    assertEquals( 2, meta.inputFields[0].getTrimType() );
    skipPropertyTest( "FIELD_TRIM_TYPE" );

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

    check( "FIELD_NULL_STRING", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getNullString();
      }
    } );

    check( "FIELD_IF_NULL", new StringGetter() {
      public String get() {
        return meta.inputFields[0].getIfNullValue();
      }
    } );

    ///////////////////////////////
    check( "IGNORE_ERRORS", new BooleanGetter() {
      public boolean get() {
        return meta.errorHandling.errorIgnored;
      }
    } );

    check( "FILE_ERROR_FIELD", new StringGetter() {
      public String get() {
        return meta.errorHandling.fileErrorField;
      }
    } );

    check( "FILE_ERROR_MESSAGE_FIELD", new StringGetter() {
      public String get() {
        return meta.errorHandling.fileErrorMessageField;
      }
    } );

    check( "SKIP_BAD_FILES", new BooleanGetter() {
      public boolean get() {
        return meta.errorHandling.skipBadFiles;
      }
    } );

    check( "WARNING_FILES_TARGET_DIR", new StringGetter() {
      public String get() {
        return meta.errorHandling.warningFilesDestinationDirectory;
      }
    } );

    check( "WARNING_FILES_EXTENTION", new StringGetter() {
      public String get() {
        return meta.errorHandling.warningFilesExtension;
      }
    } );

    check( "ERROR_FILES_TARGET_DIR", new StringGetter() {
      public String get() {
        return meta.errorHandling.errorFilesDestinationDirectory;
      }
    } );

    check( "ERROR_FILES_EXTENTION", new StringGetter() {
      public String get() {
        return meta.errorHandling.errorFilesExtension;
      }
    } );

    check( "LINE_NR_FILES_TARGET_DIR", new StringGetter() {
      public String get() {
        return meta.errorHandling.lineNumberFilesDestinationDirectory;
      }
    } );

    check( "LINE_NR_FILES_EXTENTION", new StringGetter() {
      public String get() {
        return meta.errorHandling.lineNumberFilesExtension;
      }
    } );

    //////////////////////
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

    /////////////////
    check( "ERROR_COUNT_FIELD", new StringGetter() {
      public String get() {
        return meta.errorCountField;
      }
    } );
    check( "ERROR_FIELDS_FIELD", new StringGetter() {
      public String get() {
        return meta.errorFieldsField;
      }
    } );
    check( "ERROR_TEXT_FIELD", new StringGetter() {
      public String get() {
        return meta.errorTextField;
      }
    } );
    check( "ERROR_LINES_SKIPPED", new BooleanGetter() {
      public boolean get() {
        return meta.errorLineSkipped;
      }
    } );
  }
}
