/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelinput;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

public class ExcelInputMetaTest {
  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {

    List<String> attributes =
        Arrays.asList( "fileName", "fileMask", "excludeFileMask", "fileRequired", "includeSubFolders", "field",
            "sheetName", "startRow", "startColumn", "spreadSheetType", "fileField", "sheetField", "sheetRowNumberField",
            "rowNumberField", "shortFileFieldName", "extensionFieldName", "pathFieldName", "sizeFieldName",
            "hiddenFieldName", "lastModificationTimeFieldName", "uriNameFieldName", "rootUriNameFieldName" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "excludeFileMask", "getExludeFileMask" );
        put( "shortFileFieldName", "getShortFileNameField" );
        put( "extensionFieldName", "getExtensionField" );
        put( "pathFieldName", "getPathField" );
        put( "sizeFieldName", "getSizeField" );
        put( "hiddenFieldName", "isHiddenField" );
        put( "lastModificationTimeFieldName", "getLastModificationDateField" );
        put( "uriNameFieldName", "getUriField" );
        put( "rootUriNameFieldName", "getRootUriField" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "shortFileFieldName", "setShortFileNameField" );
        put( "extensionFieldName", "setExtensionField" );
        put( "pathFieldName", "setPathField" );
        put( "sizeFieldName", "setSizeField" );
        put( "hiddenFieldName", "setIsHiddenField" );
        put( "lastModificationTimeFieldName", "setLastModificationDateField" );
        put( "uriNameFieldName", "setUriField" );
        put( "rootUriNameFieldName", "setRootUriField" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 1 );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fileName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "sheetName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "field", new ExcelInputFieldFieldLoadSaveValidator() );
    attrValidatorMap.put( "spreadSheetType", new SpreadSheetTypeFieldLoadSaveValidator() );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator(
        new IntLoadSaveValidator(), 1 ) );

    loadSaveTester =
        new LoadSaveTester( ExcelInputMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testLoadSaveXML() throws KettleException {
    loadSaveTester.testXmlRoundTrip();
  }

  @Test
  public void testLoadSaveRepo() throws KettleException {
    loadSaveTester.testRepoRoundTrip();
  }

  public class ExcelInputFieldFieldLoadSaveValidator implements FieldLoadSaveValidator<ExcelInputField[]> {

    @Override public ExcelInputField[] getTestObject() {
      return new ExcelInputField[] { };
    }

    @Override public boolean validateTestObject( ExcelInputField[] testObject, Object actual ) {
      return true;
    }
  }

  public class SpreadSheetTypeFieldLoadSaveValidator implements FieldLoadSaveValidator<SpreadSheetType> {
    @Override public SpreadSheetType getTestObject() {
      return SpreadSheetType.POI;
    }

    @Override public boolean validateTestObject( SpreadSheetType testObject, Object actual ) {
      return true;
    }
  }

  @Test
  public void cloneTest() throws Exception {
    ExcelInputMeta meta = new ExcelInputMeta();
    meta.allocate( 2, 2, 2 );
    meta.setSheetName( new String[] { "aa", "bb" } );
    meta.setFileName( new String[] { "cc", "dd" } );
    meta.setFileMask( new String[] { "ee", "ff" } );
    meta.setExcludeFileMask( new String[] { "gg", "hh" } );
    meta.setFileRequired( new String[] { "ii", "jj" } );
    meta.setIncludeSubFolders( new String[] { "kk", "ll" } );
    meta.setStartRow( new int[] { 10, 50 } );
    meta.setStartColumn( new int[] { 3, 5 } );
    meta.setSpreadSheetType( SpreadSheetType.POI );
    ExcelInputField if1 = new ExcelInputField();
    if1.setCurrencySymbol( "$" );
    if1.setFormat( "format" );
    if1.setGroupSymbol( "x" );
    if1.setLength( 5 );
    if1.setName( "fieldname" );
    if1.setPrecision( 5 );
    if1.setTrimType( 1 );
    if1.setType( 1 );

    ExcelInputField if2 = new ExcelInputField();
    if2.setCurrencySymbol( "#" );
    if2.setFormat( "format2" );
    if2.setGroupSymbol( "x1" );
    if2.setLength( 9 );
    if2.setName( "fieldname2" );
    if2.setPrecision( 3 );
    if2.setTrimType( 2 );
    if2.setType( 2 );
    meta.setField( new ExcelInputField[] { if1, if2 } );
    ExcelInputMeta aClone = (ExcelInputMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getSheetName(), aClone.getSheetName() ) );
    assertTrue( Arrays.equals( meta.getFileName(), aClone.getFileName() ) );
    assertTrue( Arrays.equals( meta.getFileMask(), aClone.getFileMask() ) );
    assertTrue( Arrays.equals( meta.getExcludeFileMask(), aClone.getExcludeFileMask() ) );
    assertTrue( Arrays.equals( meta.getFileRequired(), aClone.getFileRequired() ) );
    assertTrue( Arrays.equals( meta.getIncludeSubFolders(), aClone.getIncludeSubFolders() ) );
    assertTrue( Arrays.equals( meta.getStartRow(), aClone.getStartRow() ) );
    assertTrue( Arrays.equals( meta.getStartColumn(), aClone.getStartColumn() ) );
    ExcelInputField[] clonedFields = aClone.getField();
    assertEquals( meta.getField().length, clonedFields.length );
    ExcelInputField cif1 = clonedFields[0];
    ExcelInputField cif2 = clonedFields[1];
    assertFalse( if1 == cif1 );
    assertFalse( if2 == cif2 );
    assertEquals( if1.toString(), cif1.toString() );
    assertEquals( if2.toString(), cif2.toString() );
    assertEquals( if1.getTrimType(), cif1.getTrimType() );
    assertEquals( if2.getTrimType(), cif2.getTrimType() );
    assertEquals( if1.getGroupSymbol(), cif1.getGroupSymbol() );
    assertEquals( if2.getGroupSymbol(), cif2.getGroupSymbol() );
    assertEquals( if1.getFormat(), cif1.getFormat() );
    assertEquals( if2.getFormat(), cif2.getFormat() );
    assertEquals( meta.getXML(), aClone.getXML() );
  }

}
