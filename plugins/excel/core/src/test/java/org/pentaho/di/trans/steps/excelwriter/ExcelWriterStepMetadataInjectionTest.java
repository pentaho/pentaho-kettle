/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelwriter;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class ExcelWriterStepMetadataInjectionTest extends BaseMetadataInjectionTest<ExcelWriterStepMeta> {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new ExcelWriterStepMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FILENAME", () -> meta.getFileName() );
    check( "EXTENSION", () -> meta.getExtension() );
    check( "STREAM_XSLX_DATA", () -> meta.isStreamingData() );
    check( "SPLIT_EVERY_DATA_ROWS", () -> meta.getSplitEvery() );
    check( "INCLUDE_STEPNR_IN_FILENAME", () -> meta.isStepNrInFilename() );
    check( "INCLUDE_DATE_IN_FILENAME", () -> meta.isDateInFilename() );
    check( "INCLUDE_TIME_IN_FILENAME", () -> meta.isTimeInFilename() );
    check( "SPECIFY_DATE_TIME_FORMAT", () -> meta.isSpecifyFormat() );
    check( "DATE_TIME_FORMAT", () -> meta.getDateTimeFormat() );
    check( "IF_OUTPUT_FILE_EXISTS", () -> meta.getIfFileExists() );
    check( "WAIT_FOR_FIRST_ROW", () -> meta.isDoNotOpenNewFileInit() );
    check( "ADD_FILENAMES_TO_RESULT", () -> meta.isAddToResultFiles() );
    check( "SHEET_NAME", () -> meta.getSheetname() );
    check( "MAKE_SHEET_ACTIVE", () -> meta.isMakeSheetActive() );
    check( "IF_SHEET_EXISTS_IN_OUTPUT", () -> meta.getIfSheetExists() );
    check( "PROTECT_SHEET", () -> meta.isSheetProtected() );
    check( "PROTECTED_BY_USER", () -> meta.getProtectedBy() );
    check( "PASSWORD", () -> meta.getPassword() );
    check( "USE_TEMPLATE_FOR_NEW_FILES", () -> meta.isTemplateEnabled() );
    check( "TEMPLATE_FILE", () -> meta.getTemplateFileName() );
    check( "USE_TEMPLATE_FOR_NEW_SHEETS", () -> meta.isTemplateSheetEnabled() );
    check( "TEMPLATE_SHEET", () -> meta.getTemplateSheetName() );
    check( "HIDE_TEMPLATE_SHEET", () -> meta.isTemplateSheetHidden() );
    check( "START_WRITING_AT_CELL", () -> meta.getStartingCell() );
    check( "WHEN_WRITING_ROWS", () -> meta.getRowWritingMethod() );
    check( "WRITE_HEADER", () -> meta.isHeaderEnabled() );
    check( "WRITE_FOOTER", () -> meta.isFooterEnabled() );
    check( "AUTO_SIZE_COLUMNS", () -> meta.isAutoSizeColums() );
    check( "FORCE_FORMULA_RECALC", () -> meta.isForceFormulaRecalculation() );
    check( "LEAVE_STYLES_UNCHANGED", () -> meta.isLeaveExistingStylesUnchanged() );
    check( "START_WRITING_AT_SHEET_END", () -> meta.isAppendLines() );
    check( "OFFSET_BY_ROWS", () -> meta.getAppendOffset() );
    check( "BEGIN_BY_WRITING_EMPTY_LINES", () -> meta.getAppendEmpty() );
    check( "OMIT_HEADER", () -> meta.isAppendOmitHeader() );
    check( "EXTEND_DATA_VALIDATION" , () -> meta.isExtendDataValidationRanges() );
    check( "CREATE_PARENT_FOLDERS" , () -> meta.isCreateParentFolders() );
    check( "RETAIN_NULL_VALUES" , () -> meta.isRetainNullValues() );

    // Fields
    check( "NAME", () -> meta.getOutputFields()[0].getName() );
    check( "TYPE", () -> meta.getOutputFields()[0].getType() );
    check( "FORMAT", () -> meta.getOutputFields()[0].getFormat() );
    check( "STYLE_FROM_CELL", () -> meta.getOutputFields()[0].getStyleCell() );
    check( "TITLE", () -> meta.getOutputFields()[0].getTitle() );
    check( "HEADERFOOTER_STYLE_FROM_CELL", () -> meta.getOutputFields()[0].getTitleStyleCell() );
    check( "CONTAINS_FORMULA", () -> meta.getOutputFields()[0].isFormula() );
    check( "HYPERLINK", () -> meta.getOutputFields()[0].getHyperlinkField() );
    check( "CELL_COMMENT", () -> meta.getOutputFields()[0].getCommentField() );
    check( "CELL_COMMENT_AUTHOR", () -> meta.getOutputFields()[0].getCommentAuthorField() );
  }
}
