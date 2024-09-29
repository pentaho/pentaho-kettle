/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TextFileOutputMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  public static List<String> getMetaAttributes() {
    return Arrays.asList( "separator", "enclosure", "enclosure_forced", "enclosure_fix_disabled", "header", "footer",
      "format", "compression", "encoding", "endedLine", "fileNameInField", "fileNameField",
      "create_parent_folder", "fileName", "servlet_output", "do_not_open_new_file_init",
      "extention", "append", "split", "haspartno", "add_date", "add_time", "SpecifyFormat", "date_time_format",
      "add_to_result_filenames", "pad", "fast_dump", "splitevery", "OutputFields" );
  }

  public static Map<String, String> getGetterMap() {
    Map<String, String> getterMap = new HashMap<>();
    getterMap.put( "separator", "getSeparator" );
    getterMap.put( "enclosure", "getEnclosure" );
    getterMap.put( "enclosure_forced", "isEnclosureForced" );
    getterMap.put( "enclosure_fix_disabled", "isEnclosureFixDisabled" );
    getterMap.put( "header", "isHeaderEnabled" );
    getterMap.put( "footer", "isFooterEnabled" );
    getterMap.put( "format", "getFileFormat" );
    getterMap.put( "compression", "getFileCompression" );
    getterMap.put( "encoding", "getEncoding" );
    getterMap.put( "endedLine", "getEndedLine" );
    getterMap.put( "fileNameInField", "isFileNameInField" );
    getterMap.put( "fileNameField", "getFileNameField" );
    getterMap.put( "create_parent_folder", "isCreateParentFolder" );
    getterMap.put( "fileName", "getFileName" );
    getterMap.put( "servlet_output", "isServletOutput" );
    getterMap.put( "do_not_open_new_file_init", "isDoNotOpenNewFileInit" );
    getterMap.put( "extention", "getExtension" );
    getterMap.put( "append", "isFileAppended" );
    getterMap.put( "split", "isStepNrInFilename" );
    getterMap.put( "haspartno", "isPartNrInFilename" );
    getterMap.put( "add_date", "isDateInFilename" );
    getterMap.put( "add_time", "isTimeInFilename" );
    getterMap.put( "SpecifyFormat", "isSpecifyingFormat" );
    getterMap.put( "date_time_format", "getDateTimeFormat" );
    getterMap.put( "add_to_result_filenames", "isAddToResultFiles" );
    getterMap.put( "pad", "isPadded" );
    getterMap.put( "fast_dump", "isFastDump" );
    getterMap.put( "splitevery", "getSplitEvery" );
    getterMap.put( "OutputFields", "getOutputFields" );
    return getterMap;
  }

  public static Map<String, String> getSetterMap() {
    Map<String, String> setterMap = new HashMap<>();
    setterMap.put( "separator", "setSeparator" );
    setterMap.put( "enclosure", "setEnclosure" );
    setterMap.put( "enclosure_forced", "setEnclosureForced" );
    setterMap.put( "enclosure_fix_disabled", "setEnclosureFixDisabled" );
    setterMap.put( "header", "setHeaderEnabled" );
    setterMap.put( "footer", "setFooterEnabled" );
    setterMap.put( "format", "setFileFormat" );
    setterMap.put( "compression", "setFileCompression" );
    setterMap.put( "encoding", "setEncoding" );
    setterMap.put( "endedLine", "setEndedLine" );
    setterMap.put( "fileNameInField", "setFileNameInField" );
    setterMap.put( "fileNameField", "setFileNameField" );
    setterMap.put( "create_parent_folder", "setCreateParentFolder" );
    setterMap.put( "fileName", "setFilename" );
    setterMap.put( "servlet_output", "setServletOutput" );
    setterMap.put( "do_not_open_new_file_init", "setDoNotOpenNewFileInit" );
    setterMap.put( "extention", "setExtension" );
    setterMap.put( "append", "setFileAppended" );
    setterMap.put( "split", "setStepNrInFilename" );
    setterMap.put( "haspartno", "setPartNrInFilename" );
    setterMap.put( "add_date", "setDateInFilename" );
    setterMap.put( "add_time", "setTimeInFilename" );
    setterMap.put( "SpecifyFormat", "setSpecifyingFormat" );
    setterMap.put( "date_time_format", "setDateTimeFormat" );
    setterMap.put( "add_to_result_filenames", "setAddToResultFiles" );
    setterMap.put( "pad", "setPadded" );
    setterMap.put( "fast_dump", "setFastDump" );
    setterMap.put( "splitevery", "setSplitEvery" );
    setterMap.put( "OutputFields", "setOutputFields" );
    return setterMap;
  }

  public static Map<String, FieldLoadSaveValidator<?>> getAttributeValidators() {
    return new HashMap<>();
  }

  public static Map<String, FieldLoadSaveValidator<?>> getTypeValidators() {
    Map<String, FieldLoadSaveValidator<?>> typeValidators =
      new HashMap<>();
    typeValidators.put( TextFileField[].class.getCanonicalName(),
      new ArrayLoadSaveValidator<>( new TextFileFieldLoadSaveValidator() ) );
    return typeValidators;
  }

  @Test
  public void testRoundTrip() throws KettleException {
    LoadSaveTester<TextFileOutputMeta> loadSaveTester =
      new LoadSaveTester<>( TextFileOutputMeta.class, getMetaAttributes(),
        getGetterMap(), getSetterMap(), getAttributeValidators(), getTypeValidators() );

    loadSaveTester.testSerialization();
  }

  @Test
  public void testVarReplaceSplit() {
    TextFileOutputMeta meta = new TextFileOutputMeta();
    meta.setDefault();
    meta.setSplitEveryRows( "${splitVar}" );
    VariableSpace varSpace = new Variables();
    assertEquals( 0, meta.getSplitEvery( varSpace ) );
    String fileName = meta.buildFilename( "foo", "txt2", varSpace, 0, null, 3, false, meta );
    assertEquals( "foo.txt2", fileName );
    varSpace.setVariable( "splitVar", "2" );
    assertEquals( 2, meta.getSplitEvery( varSpace ) );
    fileName = meta.buildFilename( "foo", "txt2", varSpace, 0, null, 5, false, meta );
    assertEquals( "foo_5.txt2", fileName );
  }

  public static class TextFileFieldLoadSaveValidator implements FieldLoadSaveValidator<TextFileField> {
    Random rand = new Random();

    @Override
    public TextFileField getTestObject() {
      String name = UUID.randomUUID().toString();
      int type =
        ValueMetaFactory.getIdForValueMeta( ValueMetaFactory.getValueMetaNames()[rand.nextInt( ValueMetaFactory
          .getValueMetaNames().length )] );
      String format = UUID.randomUUID().toString();
      int length = Math.abs( rand.nextInt() );
      int precision = Math.abs( rand.nextInt() );
      String currencySymbol = UUID.randomUUID().toString();
      String decimalSymbol = UUID.randomUUID().toString();
      String groupSymbol = UUID.randomUUID().toString();
      String nullString = UUID.randomUUID().toString();

      return new TextFileField( name, type, format, length, precision, currencySymbol, decimalSymbol, groupSymbol,
          nullString );
    }

    @Override
    public boolean validateTestObject( TextFileField testObject, Object actual ) {
      if ( !( actual instanceof TextFileField ) || testObject.compare( actual ) != 0 ) {
        return false;
      }
      TextFileField act = (TextFileField) actual;
      return testObject.getName().equals( act.getName() )
        && testObject.getType() == act.getType()
        && testObject.getFormat().equals( act.getFormat() )
        && testObject.getLength() == act.getLength()
        && testObject.getPrecision() == act.getPrecision()
        && testObject.getCurrencySymbol().equals( act.getCurrencySymbol() )
        && testObject.getDecimalSymbol().equals( act.getDecimalSymbol() )
        && testObject.getGroupingSymbol().equals( act.getGroupingSymbol() )
        && testObject.getNullString().equals( act.getNullString() );
    }
  }
}
