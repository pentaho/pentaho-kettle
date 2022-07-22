/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

public class ExcelOutputMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<ExcelOutputMeta> testMetaClass = ExcelOutputMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "headerFontName", "headerFontSize", "headerFontBold", "headerFontItalic", "headerFontUnderline",
            "headerFontOrientation", "headerFontColor", "headerBackGroundColor", "headerRowHeight", "headerAlignment",
            "headerImage", "rowFontName", "rowFontSize", "rowFontColor", "rowBackGroundColor", "fileName", "extension",
            "password", "headerEnabled", "footerEnabled", "splitEvery", "stepNrInFilename", "dateInFilename", "addToResultFiles",
            "sheetProtected", "timeInFilename", "templateEnabled", "templateFileName", "templateAppend", "sheetname", "useTempFiles",
            "tempDirectory", "encoding", "append", "doNotOpenNewFileInit", "createParentFolder", "specifyFormat", "dateTimeFormat",
            "autoSizeColumns", "nullBlank", "outputFields" );

    // Note - newline (get/set) doesn't appear to be used or persisted. So, it's not included in the load/save tester.

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "sheetProtected", "setProtectSheet" );
        put( "nullBlank", "setNullIsBlank" );
      }
    };

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "outputFields",
        new ArrayLoadSaveValidator<ExcelField>( new ExcelFieldLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "headerFontName", new IntLoadSaveValidator( ExcelOutputMeta.font_name_code.length ) );
    attrValidatorMap.put( "headerFontUnderline", new IntLoadSaveValidator( ExcelOutputMeta.font_underline_code.length ) );
    attrValidatorMap.put( "headerFontOrientation", new IntLoadSaveValidator( ExcelOutputMeta.font_orientation_code.length ) );
    attrValidatorMap.put( "headerFontColor", new IntLoadSaveValidator( ExcelOutputMeta.font_color_code.length ) );
    attrValidatorMap.put( "headerBackGroundColor", new IntLoadSaveValidator( ExcelOutputMeta.font_color_code.length ) );
    attrValidatorMap.put( "headerAlignment", new IntLoadSaveValidator( ExcelOutputMeta.font_alignment_code.length ) );
    attrValidatorMap.put( "rowBackGroundColor", new IntLoadSaveValidator( ExcelOutputMeta.font_color_code.length ) );
    attrValidatorMap.put( "rowFontName", new IntLoadSaveValidator( ExcelOutputMeta.font_name_code.length ) );
    attrValidatorMap.put( "rowFontColor", new IntLoadSaveValidator( ExcelOutputMeta.font_color_code.length ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof ExcelOutputMeta ) {
      ( (ExcelOutputMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class ExcelFieldLoadSaveValidator implements FieldLoadSaveValidator<ExcelField> {
    final Random rand = new Random();
    @Override
    public ExcelField getTestObject() {
      ExcelField rtn = new ExcelField();
      rtn.setFormat( UUID.randomUUID().toString() );
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setType( rand.nextInt( 7 ) );
      return rtn;
    }

    @Override
    public boolean validateTestObject( ExcelField testObject, Object actual ) {
      if ( !( actual instanceof ExcelField ) ) {
        return false;
      }
      ExcelField actualInput = (ExcelField) actual;
      return ( testObject.toString().equals( actualInput.toString() ) );
    }
  }

}
