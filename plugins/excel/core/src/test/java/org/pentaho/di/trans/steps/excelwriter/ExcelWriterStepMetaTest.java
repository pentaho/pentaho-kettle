/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.Assert.assertEquals;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;

public class ExcelWriterStepMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList(
      "header", "footer", "makeSheetActive", "rowWritingMethod", "startingCell", "appendOmitHeader", "appendOffset",
      "appendEmpty", "rowWritingMethod", "forceFormulaRecalculation", "leaveExistingStylesUnchanged",
      "appendLines", "add_to_result_filenames", "name", "extention", "do_not_open_newfile_init", "split", "add_date",
      "add_time", "SpecifyFormat", "date_time_format", "sheetname", "autosizecolums", "stream_data", "protect_sheet",
      "password", "protected_by", "splitevery", "if_file_exists", "if_sheet_exists", "enabled", "sheet_enabled",
      "filename", "sheetname", "outputfields", "TemplateSheetHidden", "extend_data_validation", "retain_null_values",
      "create_parent" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "header", "isHeaderEnabled" );
    getterMap.put( "footer", "isFooterEnabled" );
    getterMap.put( "makeSheetActive", "isMakeSheetActive" );
    getterMap.put( "rowWritingMethod", "getRowWritingMethod" );
    getterMap.put( "startingCell", "getStartingCell" );
    getterMap.put( "appendOmitHeader", "isAppendOmitHeader" );
    getterMap.put( "appendOffset", "getAppendOffset" );
    getterMap.put( "appendEmpty", "getAppendEmpty" );
    getterMap.put( "rowWritingMethod", "getRowWritingMethod" );
    getterMap.put( "forceFormulaRecalculation", "isForceFormulaRecalculation" );
    getterMap.put( "leaveExistingStylesUnchanged", "isLeaveExistingStylesUnchanged" );
    getterMap.put( "appendLines", "isAppendLines" );
    getterMap.put( "add_to_result_filenames", "isAddToResultFiles" );
    getterMap.put( "name", "getFileName" );
    getterMap.put( "extention", "getExtension" );
    getterMap.put( "do_not_open_newfile_init", "isDoNotOpenNewFileInit" );
    getterMap.put( "split", "getSplitEvery" );
    getterMap.put( "add_date", "isDateInFilename" );
    getterMap.put( "add_time", "isTimeInFilename" );
    getterMap.put( "SpecifyFormat", "isSpecifyFormat" );
    getterMap.put( "date_time_format", "getDateTimeFormat" );
    getterMap.put( "sheetname", "getSheetname" );
    getterMap.put( "autosizecolums", "isAutoSizeColums" );
    getterMap.put( "stream_data", "isStreamingData" );
    getterMap.put( "protect_sheet", "isSheetProtected" );
    getterMap.put( "password", "getPassword" );
    getterMap.put( "protected_by", "getProtectedBy" );
    getterMap.put( "splitevery", "getSplitEvery" );
    getterMap.put( "if_file_exists", "getIfFileExists" );
    getterMap.put( "if_sheet_exists", "getIfSheetExists" );
    getterMap.put( "enabled", "isTemplateEnabled" );
    getterMap.put( "sheet_enabled", "isTemplateSheetEnabled" );
    getterMap.put( "filename", "getTemplateFileName" );
    getterMap.put( "sheetname", "getTemplateSheetName" );
    getterMap.put( "outputfields", "getOutputFields" );
    getterMap.put( "extend_data_validation", "isExtendDataValidationRanges" );
    getterMap.put( "retain_null_values", "isRetainNullValues" );
    getterMap.put( "create_parent", "isCreateParentFolders" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "header", "setHeaderEnabled" );
    setterMap.put( "footer", "setFooterEnabled" );
    setterMap.put( "makeSheetActive", "setMakeSheetActive" );
    setterMap.put( "rowWritingMethod", "setRowWritingMethod" );
    setterMap.put( "startingCell", "setStartingCell" );
    setterMap.put( "appendOmitHeader", "setAppendOmitHeader" );
    setterMap.put( "appendOffset", "setAppendOffset" );
    setterMap.put( "appendEmpty", "setAppendEmpty" );
    setterMap.put( "rowWritingMethod", "setRowWritingMethod" );
    setterMap.put( "forceFormulaRecalculation", "setForceFormulaRecalculation" );
    setterMap.put( "leaveExistingStylesUnchanged", "setLeaveExistingStylesUnchanged" );
    setterMap.put( "appendLines", "setAppendLines" );
    setterMap.put( "add_to_result_filenames", "setAddToResultFiles" );
    setterMap.put( "name", "setFileName" );
    setterMap.put( "extention", "setExtension" );
    setterMap.put( "do_not_open_newfile_init", "setDoNotOpenNewFileInit" );
    setterMap.put( "split", "setSplitEvery" );
    setterMap.put( "add_date", "setDateInFilename" );
    setterMap.put( "add_time", "setTimeInFilename" );
    setterMap.put( "SpecifyFormat", "setSpecifyFormat" );
    setterMap.put( "date_time_format", "setDateTimeFormat" );
    setterMap.put( "sheetname", "setSheetname" );
    setterMap.put( "autosizecolums", "setAutoSizeColums" );
    setterMap.put( "stream_data", "setStreamingData" );
    setterMap.put( "protect_sheet", "setProtectSheet" );
    setterMap.put( "password", "setPassword" );
    setterMap.put( "protected_by", "setProtectedBy" );
    setterMap.put( "splitevery", "setSplitEvery" );
    setterMap.put( "if_file_exists", "setIfFileExists" );
    setterMap.put( "if_sheet_exists", "setIfSheetExists" );
    setterMap.put( "enabled", "setTemplateEnabled" );
    setterMap.put( "sheet_enabled", "setTemplateSheetEnabled" );
    setterMap.put( "filename", "setTemplateFileName" );
    setterMap.put( "sheetname", "setTemplateSheetName" );
    setterMap.put( "outputfields", "setOutputFields" );
    setterMap.put( "extend_data_validation", "setExtendDataValidationRanges" );
    setterMap.put( "retain_null_values", "setRetainNullValues" );
    setterMap.put( "create_parent", "setCreateParentFolders" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    fieldLoadSaveValidatorTypeMap.put( ExcelWriterStepField[].class.getCanonicalName(),
      new ArrayLoadSaveValidator<ExcelWriterStepField>( new ExcelWriterStepFieldLoadSaveValidator() ) );

    LoadSaveTester<ExcelWriterStepMeta> loadSaveTester =
      new LoadSaveTester<ExcelWriterStepMeta>( ExcelWriterStepMeta.class, attributes, getterMap, setterMap,
        new HashMap<String, FieldLoadSaveValidator<?>>(), fieldLoadSaveValidatorTypeMap );

    loadSaveTester.testSerialization();
  }

  public class ExcelWriterStepFieldLoadSaveValidator implements FieldLoadSaveValidator<ExcelWriterStepField> {
    @Override
    public boolean validateTestObject( ExcelWriterStepField testObject, Object actual ) {
      //Perform more-extensive test, as equals() method does check on "name" only
      ExcelWriterStepField obj2 = (ExcelWriterStepField) actual;
      return testObject.equals( ( obj2 ) )
        && testObject.getType() == obj2.getType()
        && testObject.getFormat().equals( obj2.getFormat() )
        && testObject.getTitle().equals( obj2.getTitle() )
        && testObject.getTitleStyleCell().equals( obj2.getTitleStyleCell() )
        && testObject.getStyleCell().equals(  obj2.getStyleCell() )
        && testObject.getCommentField().equals(  obj2.getCommentField() )
        && testObject.getCommentAuthorField().equals( obj2.getCommentAuthorField() )
        && testObject.isFormula() == obj2.isFormula()
        && testObject.getHyperlinkField().equals(  obj2.getHyperlinkField() );
    }

    @Override
    public ExcelWriterStepField getTestObject() {
      ExcelWriterStepField obj = new ExcelWriterStepField();
      obj.setName( UUID.randomUUID().toString() );
      obj.setType( UUID.randomUUID().toString() );
      obj.setFormat( UUID.randomUUID().toString() );
      obj.setTitle( UUID.randomUUID().toString() );
      obj.setTitleStyleCell( UUID.randomUUID().toString() );
      obj.setStyleCell( UUID.randomUUID().toString() );
      obj.setCommentField( UUID.randomUUID().toString() );
      obj.setCommentAuthorField( UUID.randomUUID().toString() );
      obj.setFormula( new Random().nextBoolean() );
      obj.setHyperlinkField( UUID.randomUUID().toString() );
      return obj;
    }
  }

  @Test
  public void testSaveLoadStepMetaDeterministic() throws Exception {
    // LoadSaveTester randomizes which booleans it tests
    // non-defaults
    checkSaveLoadDeterministic( meta -> {
      meta.setAutoSizeColums( true );
      meta.setExtendDataValidationRanges( true );
      meta.setCreateParentFolders( true );
      meta.setRetainNullValues( false );
    } );
    // defaults
    checkSaveLoadDeterministic( meta -> {
      meta.setAutoSizeColums( false );
      meta.setExtendDataValidationRanges( false );
      meta.setCreateParentFolders( false );
      meta.setRetainNullValues( true );
    } );
  }

  public void checkSaveLoadDeterministic( Consumer<ExcelWriterStepMeta> setters ) throws Exception {
    final ExcelWriterStepMeta orig = new ExcelWriterStepMeta();
    orig.setOutputFields( new ExcelWriterStepField[0] );

    setters.accept( orig );

    testXmlRoundtrip( orig, () -> new ExcelWriterStepMeta(), this::assertMetaEq );
    testRepoRoundtrip( orig, () -> new ExcelWriterStepMeta(), this::assertMetaEq );
  }

  private <T extends StepMetaInterface> void testXmlRoundtrip( T orig, Supplier<T> ctor, BiConsumer<T, T> assertEq )
    throws KettleException {
    String xml = MessageFormat.format( "<step>{0}</step>", orig.getXML() );

    Document document = XMLHandler.loadXMLString( xml.toString() );
    Node step = XMLHandler.getSubNode( document, "step" );
    IMetaStore metaStore = Mockito.mock( IMetaStore.class );
    final T loaded = ctor.get();
    loaded.loadXML( step, Collections.emptyList(), metaStore );

    assertEq.accept( orig, loaded );
  }

  private <T extends StepMetaInterface> void testRepoRoundtrip( T orig, Supplier<T> ctor, BiConsumer<T, T> assertEq )
    throws KettleException {

    Repository repo = new MemoryRepository();
    IMetaStore metaStore = Mockito.mock( IMetaStore.class );
    final ObjectId transId = () -> "trans-ID", stepId = () -> "step-ID";
    orig.saveRep( repo, metaStore, transId, stepId );
    final T loaded = ctor.get();
    loaded.readRep( repo, metaStore, stepId, Collections.emptyList() );

    assertEq.accept( orig, loaded );
  }

  private void assertMetaEq( ExcelWriterStepMeta orig, ExcelWriterStepMeta loaded ) {
    // add more fields as needed
    assertEquals( "isRetainNullValues", orig.isRetainNullValues(), loaded.isRetainNullValues() );
    assertEquals( "isExtendDataValidationRanges", orig.isExtendDataValidationRanges(),
      loaded.isExtendDataValidationRanges() );
    assertEquals( "isCreateParentFolders", orig.isCreateParentFolders(), loaded.isCreateParentFolders() );
    assertEquals( "isAutoSizeColums", orig.isAutoSizeColums(), loaded.isAutoSizeColums() );
  }
}
