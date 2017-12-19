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

package org.pentaho.di.trans.steps.excelinput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class ExcelInputMetaTest {
  LoadSaveTester loadSaveTester;
  ExcelInputMeta meta;

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );

    meta = new ExcelInputMeta();
    meta.setFileName( new String[] { "1", "2", "3" } );
    meta.setSheetName( new String[] { "1", "2", "3", "4" } );
    meta.setField( new ExcelInputField[] {
      new ExcelInputField( "1", 1, 1 ),
      new ExcelInputField( "2", 2, 2 ) } );
    meta.normilizeAllocation();
  }

  @Test
  public void testSerialization() throws KettleException {
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
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fileName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "sheetName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "field",
      new ArrayLoadSaveValidator<ExcelInputField>( new ExcelInputFieldLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "spreadSheetType", new SpreadSheetTypeFieldLoadSaveValidator() );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator(), 5 ) );

    loadSaveTester =
      new LoadSaveTester( ExcelInputMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );

    loadSaveTester.testSerialization();
  }

  public class ExcelInputFieldLoadSaveValidator implements FieldLoadSaveValidator<ExcelInputField> {
    final Random rand = new Random();

    @Override
    public ExcelInputField getTestObject() {
      ExcelInputField rtn = new ExcelInputField();
      rtn.setCurrencySymbol( UUID.randomUUID().toString() );
      rtn.setDecimalSymbol( UUID.randomUUID().toString() );
      rtn.setFormat( UUID.randomUUID().toString() );
      rtn.setGroupSymbol( UUID.randomUUID().toString() );
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setTrimType( rand.nextInt( 4 ) );
      rtn.setPrecision( rand.nextInt( 9 ) );
      rtn.setRepeated( rand.nextBoolean() );
      rtn.setLength( rand.nextInt( 50 ) );
      rtn.setType( rand.nextInt( 5 ) + 1 );
      return rtn;
    }

    @Override
    public boolean validateTestObject( ExcelInputField testObject, Object actual ) {
      if ( !( actual instanceof ExcelInputField ) ) {
        return false;
      }
      ExcelInputField another = (ExcelInputField) actual;
      return new EqualsBuilder()
        .append( testObject.getName(), another.getName() )
        .append( testObject.getType(), another.getType() )
        .append( testObject.getLength(), another.getLength() )
        .append( testObject.getFormat(), another.getFormat() )
        .append( testObject.getTrimType(), another.getTrimType() )
        .append( testObject.getPrecision(), another.getPrecision() )
        .append( testObject.getCurrencySymbol(), another.getCurrencySymbol() )
        .append( testObject.getDecimalSymbol(), another.getDecimalSymbol() )
        .append( testObject.getGroupSymbol(), another.getGroupSymbol() )
        .append( testObject.isRepeated(), another.isRepeated() )
        .isEquals();
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
  public void testRepoRoundTripWithNullAttr() throws KettleException {
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

    FieldLoadSaveValidator<String[]> nullStringArrayLoadSaveValidator = new NullStringArrayLoadSaveValidator();

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 1 );

    NullNameExcelInputArrayFieldLoadSaveValidator nullNameExcelInputArrayFieldLoadSaveValidator =
      new NullNameExcelInputArrayFieldLoadSaveValidator();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fileName", nullStringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "sheetName", nullStringArrayLoadSaveValidator );
    attrValidatorMap.put( "field", nullNameExcelInputArrayFieldLoadSaveValidator );
    attrValidatorMap.put( "spreadSheetType", new SpreadSheetTypeFieldLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator(), 1 ) );

    loadSaveTester = new LoadSaveTester( ExcelInputMeta.class, attributes, getterMap,
      setterMap, attrValidatorMap, typeValidatorMap );

    loadSaveTester.testRepoRoundTrip();
  }

  public class NullStringArrayLoadSaveValidator implements FieldLoadSaveValidator<String[]> {
    @Override public String[] getTestObject() {
      return new String[] { null };
    }

    @Override public boolean validateTestObject( String[] original, Object actualObject ) {
      String[] actual = actualObject instanceof String[] ? ( (String[]) actualObject ) : null;
      if ( actual == null || actual.length != 1 || original.length != 1 || original[ 0 ] != null ) {
        return false;
      }
      return StringUtils.EMPTY.equals( actual[ 0 ] );
    }
  }

  public class NullNameExcelInputArrayFieldLoadSaveValidator implements FieldLoadSaveValidator<ExcelInputField[]> {
    @Override public ExcelInputField[] getTestObject() {
      ExcelInputField rtn = new ExcelInputField();
      rtn.setName( null );
      return new ExcelInputField[] { rtn };
    }

    @Override public boolean validateTestObject( ExcelInputField[] original, Object actualObject ) {
      ExcelInputField[] actual =
        actualObject instanceof ExcelInputField[] ? ( (ExcelInputField[]) actualObject ) : null;
      if ( actual == null || actual.length != 1 || original.length != 1 || original[ 0 ].getName() != null ) {
        return false;
      }
      return StringUtils.EMPTY.equals( actual[ 0 ].getName() );
    }
  }

  @Test
  public void testNormilizeAllocation() throws KettleException {
    Assert.assertEquals( 3, meta.getFileName().length );
    Assert.assertEquals( 3, meta.getFileMask().length );
    Assert.assertEquals( 3, meta.getExcludeFileMask().length );
    Assert.assertEquals( 3, meta.getFileRequired().length );
    Assert.assertEquals( 3, meta.getIncludeSubFolders().length );

    Assert.assertEquals( 4, meta.getSheetName().length );
    Assert.assertEquals( 4, meta.getStartRow().length );
    Assert.assertEquals( 4, meta.getStartColumn().length );

    Assert.assertArrayEquals( new String[] { "1", "2", "3" }, meta.getFileName() );
    Assert.assertArrayEquals( new String[] { "1", "2", "3", "4" }, meta.getSheetName() );

    for ( String str : meta.getFileMask() ) {
      Assert.assertEquals( null, str );
    }
    for ( String str : meta.getExcludeFileMask() ) {
      Assert.assertEquals( null, str );
    }
    for ( String str : meta.getFileRequired() ) {
      Assert.assertEquals( null, str );
    }
    for ( String str : meta.getIncludeSubFolders() ) {
      Assert.assertEquals( null, str );
    }
    for ( int itr : meta.getStartRow() ) {
      Assert.assertEquals( 0, itr );
    }
    for ( int itr : meta.getStartColumn() ) {
      Assert.assertEquals( 0, itr );
    }
  }

  @Test
  public void testGetXML() throws KettleException {
    Assert.assertEquals(
      "    <header>N</header>" + SystemUtils.LINE_SEPARATOR
        + "    <noempty>N</noempty>" + SystemUtils.LINE_SEPARATOR
        + "    <stoponempty>N</stoponempty>" + SystemUtils.LINE_SEPARATOR
        + "    <filefield/>" + SystemUtils.LINE_SEPARATOR
        + "    <sheetfield/>" + SystemUtils.LINE_SEPARATOR
        + "    <sheetrownumfield/>" + SystemUtils.LINE_SEPARATOR
        + "    <rownumfield/>" + SystemUtils.LINE_SEPARATOR
        + "    <sheetfield/>" + SystemUtils.LINE_SEPARATOR
        + "    <filefield/>" + SystemUtils.LINE_SEPARATOR
        + "    <limit>0</limit>" + SystemUtils.LINE_SEPARATOR
        + "    <encoding/>" + SystemUtils.LINE_SEPARATOR
        + "    <add_to_result_filenames>N</add_to_result_filenames>" + SystemUtils.LINE_SEPARATOR
        + "    <accept_filenames>N</accept_filenames>" + SystemUtils.LINE_SEPARATOR
        + "    <accept_field/>" + SystemUtils.LINE_SEPARATOR
        + "    <accept_stepname/>" + SystemUtils.LINE_SEPARATOR
        + "    <file>" + SystemUtils.LINE_SEPARATOR
        + "      <name>1</name>" + SystemUtils.LINE_SEPARATOR
        + "      <filemask/>" + SystemUtils.LINE_SEPARATOR
        + "      <exclude_filemask/>" + SystemUtils.LINE_SEPARATOR
        + "      <file_required/>" + SystemUtils.LINE_SEPARATOR
        + "      <include_subfolders/>" + SystemUtils.LINE_SEPARATOR
        + "      <name>2</name>" + SystemUtils.LINE_SEPARATOR
        + "      <filemask/>" + SystemUtils.LINE_SEPARATOR
        + "      <exclude_filemask/>" + SystemUtils.LINE_SEPARATOR
        + "      <file_required/>" + SystemUtils.LINE_SEPARATOR
        + "      <include_subfolders/>" + SystemUtils.LINE_SEPARATOR
        + "      <name>3</name>" + SystemUtils.LINE_SEPARATOR
        + "      <filemask/>" + SystemUtils.LINE_SEPARATOR
        + "      <exclude_filemask/>" + SystemUtils.LINE_SEPARATOR
        + "      <file_required/>" + SystemUtils.LINE_SEPARATOR
        + "      <include_subfolders/>" + SystemUtils.LINE_SEPARATOR
        + "    </file>" + SystemUtils.LINE_SEPARATOR
        + "    <fields>" + SystemUtils.LINE_SEPARATOR
        + "      <field>" + SystemUtils.LINE_SEPARATOR
        + "        <name>1</name>" + SystemUtils.LINE_SEPARATOR
        + "        <type>String</type>" + SystemUtils.LINE_SEPARATOR
        + "        <length>1</length>" + SystemUtils.LINE_SEPARATOR
        + "        <precision>-1</precision>" + SystemUtils.LINE_SEPARATOR
        + "        <trim_type>none</trim_type>" + SystemUtils.LINE_SEPARATOR
        + "        <repeat>N</repeat>" + SystemUtils.LINE_SEPARATOR
        + "        <format/>" + SystemUtils.LINE_SEPARATOR
        + "        <currency/>" + SystemUtils.LINE_SEPARATOR
        + "        <decimal/>" + SystemUtils.LINE_SEPARATOR
        + "        <group/>" + SystemUtils.LINE_SEPARATOR
        + "      </field>" + SystemUtils.LINE_SEPARATOR
        + "      <field>" + SystemUtils.LINE_SEPARATOR
        + "        <name>2</name>" + SystemUtils.LINE_SEPARATOR
        + "        <type>String</type>" + SystemUtils.LINE_SEPARATOR
        + "        <length>2</length>" + SystemUtils.LINE_SEPARATOR
        + "        <precision>-1</precision>" + SystemUtils.LINE_SEPARATOR
        + "        <trim_type>none</trim_type>" + SystemUtils.LINE_SEPARATOR
        + "        <repeat>N</repeat>" + SystemUtils.LINE_SEPARATOR
        + "        <format/>" + SystemUtils.LINE_SEPARATOR
        + "        <currency/>" + SystemUtils.LINE_SEPARATOR
        + "        <decimal/>" + SystemUtils.LINE_SEPARATOR
        + "        <group/>" + SystemUtils.LINE_SEPARATOR
        + "      </field>" + SystemUtils.LINE_SEPARATOR
        + "    </fields>" + SystemUtils.LINE_SEPARATOR
        + "    <sheets>" + SystemUtils.LINE_SEPARATOR
        + "      <sheet>" + SystemUtils.LINE_SEPARATOR
        + "        <name>1</name>" + SystemUtils.LINE_SEPARATOR
        + "        <startrow>0</startrow>" + SystemUtils.LINE_SEPARATOR
        + "        <startcol>0</startcol>" + SystemUtils.LINE_SEPARATOR
        + "        </sheet>" + SystemUtils.LINE_SEPARATOR
        + "      <sheet>" + SystemUtils.LINE_SEPARATOR
        + "        <name>2</name>" + SystemUtils.LINE_SEPARATOR
        + "        <startrow>0</startrow>" + SystemUtils.LINE_SEPARATOR
        + "        <startcol>0</startcol>" + SystemUtils.LINE_SEPARATOR
        + "        </sheet>" + SystemUtils.LINE_SEPARATOR
        + "      <sheet>" + SystemUtils.LINE_SEPARATOR
        + "        <name>3</name>" + SystemUtils.LINE_SEPARATOR
        + "        <startrow>0</startrow>" + SystemUtils.LINE_SEPARATOR
        + "        <startcol>0</startcol>" + SystemUtils.LINE_SEPARATOR
        + "        </sheet>" + SystemUtils.LINE_SEPARATOR
        + "      <sheet>" + SystemUtils.LINE_SEPARATOR
        + "        <name>4</name>" + SystemUtils.LINE_SEPARATOR
        + "        <startrow>0</startrow>" + SystemUtils.LINE_SEPARATOR
        + "        <startcol>0</startcol>" + SystemUtils.LINE_SEPARATOR
        + "        </sheet>" + SystemUtils.LINE_SEPARATOR
        + "    </sheets>" + SystemUtils.LINE_SEPARATOR
        + "    <strict_types>N</strict_types>" + SystemUtils.LINE_SEPARATOR
        + "    <error_ignored>N</error_ignored>" + SystemUtils.LINE_SEPARATOR
        + "    <error_line_skipped>N</error_line_skipped>" + SystemUtils.LINE_SEPARATOR
        + "    <bad_line_files_destination_directory/>" + SystemUtils.LINE_SEPARATOR
        + "    <bad_line_files_extension/>" + SystemUtils.LINE_SEPARATOR
        + "    <error_line_files_destination_directory/>" + SystemUtils.LINE_SEPARATOR
        + "    <error_line_files_extension/>" + SystemUtils.LINE_SEPARATOR
        + "    <line_number_files_destination_directory/>" + SystemUtils.LINE_SEPARATOR
        + "    <line_number_files_extension/>" + SystemUtils.LINE_SEPARATOR
        + "    <shortFileFieldName/>" + SystemUtils.LINE_SEPARATOR
        + "    <pathFieldName/>" + SystemUtils.LINE_SEPARATOR
        + "    <hiddenFieldName/>" + SystemUtils.LINE_SEPARATOR
        + "    <lastModificationTimeFieldName/>" + SystemUtils.LINE_SEPARATOR
        + "    <uriNameFieldName/>" + SystemUtils.LINE_SEPARATOR
        + "    <rootUriNameFieldName/>" + SystemUtils.LINE_SEPARATOR
        + "    <extensionFieldName/>" + SystemUtils.LINE_SEPARATOR
        + "    <sizeFieldName/>" + SystemUtils.LINE_SEPARATOR
        + "    <spreadsheet_type/>" + SystemUtils.LINE_SEPARATOR, meta.getXML() );
  }

  @Test
  public void testClone() throws KettleException {
    ExcelInputMeta clone = (ExcelInputMeta) meta.clone();
    Assert.assertEquals( meta.getXML(), clone.getXML() );
  }
  // Note - removed cloneTest as it's now covered by the load/save tester.


  @Test
  public void testPDI16559() throws Exception {
    StepMockHelper<ExcelInputMeta, ExcelInputData> mockHelper =
            new StepMockHelper<ExcelInputMeta, ExcelInputData>( "excelInput", ExcelInputMeta.class, ExcelInputData.class );

    ExcelInputMeta excelInput = new ExcelInputMeta();
    excelInput.setFileName( new String[] { "file1", "file2", "file3", "file4", "file5" } );
    excelInput.setFileMask( new String[] { "mask1", "mask2", "mask3", "mask4" } );
    excelInput.setExcludeFileMask( new String[] { "excludeMask1", "excludeMask2", "excludeMask3" } );
    excelInput.setIncludeSubFolders( new String[] { "yes", "no" } );
    excelInput.setSheetName( new String[] { "sheet1", "sheet2", "sheet3" } );
    excelInput.setStartRow( new int[] { 0, 15 } );
    excelInput.setStartColumn( new int[] { 9 } );

    excelInput.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = excelInput.getXML();

    int targetLen = excelInput.getFileName().length;
    Assert.assertEquals( targetLen, excelInput.getFileMask().length );
    Assert.assertEquals( targetLen, excelInput.getExcludeFileMask().length );
    Assert.assertEquals( targetLen, excelInput.getFileRequired().length );
    Assert.assertEquals( targetLen, excelInput.getIncludeSubFolders().length );

    targetLen = excelInput.getSheetName().length;
    Assert.assertEquals( targetLen, excelInput.getStartRow().length );
    Assert.assertEquals( targetLen, excelInput.getStartColumn().length );

  }
}
