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
package org.pentaho.di.trans.steps.csvinput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
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
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.TextFileInputFieldValidator;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

public class CsvInputMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<CsvInputMeta> testMetaClass = CsvInputMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "BufferSize", "Delimiter", "Enclosure", "Encoding", "Filename", "FilenameField", "InputFields", "RowNumField",
            "AddResultFile", "HeaderPresent", "IncludingFilename", "LazyConversionActive", "NewlinePossibleInFields", "RunningInParallel" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "hasHeader", "hasHeader" );
        put( "includeFilename", "includeFilename" );
        put( "includeRowNumber", "includeRowNumber" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "includeFilename", "includeFilename" );
        put( "includeRowNumber", "includeRowNumber" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "InputFields",
        new ArrayLoadSaveValidator<TextFileInputField>( new TextFileInputFieldValidator(), 5 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof CsvInputMeta ) {
      ( (CsvInputMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testClone() {
    final CsvInputMeta original = new CsvInputMeta();
    original.setDelimiter( ";" );
    original.setEnclosure( "'" );
    final TextFileInputField[] originalFields = new TextFileInputField[ 1 ];
    final TextFileInputField originalField = new TextFileInputField();
    originalField.setName( "field" );
    originalFields[ 0 ] = originalField;
    original.setInputFields( originalFields );

    final CsvInputMeta clone = (CsvInputMeta) original.clone();
    // verify that the clone and its input fields are "equal" to the originals, but not the same objects
    Assert.assertNotSame( original, clone );
    Assert.assertEquals( original.getDelimiter(), clone.getDelimiter() );
    Assert.assertEquals( original.getEnclosure(), clone.getEnclosure() );

    Assert.assertNotSame( original.getInputFields(), clone.getInputFields() );
    Assert.assertNotSame( original.getInputFields()[ 0 ], clone.getInputFields()[ 0 ] );
    Assert.assertEquals( original.getInputFields()[ 0 ].getName(), clone.getInputFields()[ 0 ].getName() );
  }
}
