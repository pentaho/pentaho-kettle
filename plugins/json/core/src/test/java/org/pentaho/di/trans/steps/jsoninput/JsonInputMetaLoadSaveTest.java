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

package org.pentaho.di.trans.steps.jsoninput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JsonInputMetaLoadSaveTest implements InitializerInterface<StepMetaInterface> {

  static final int FILE_COUNT = new Random().nextInt( 20 ) + 1;
  static final int FIELD_COUNT = new Random().nextInt( 20 ) + 1;

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes = Arrays.asList( "includeFilename", "filenameField", "includeRowNumber", "addResultFile",
      "ReadUrl", "removeSourceField", "IgnoreEmptyFile", "doNotFailIfNoFile", "ignoreMissingPath", "defaultPathLeafToNull", "rowNumberField",
      "FileName", "FileMask", "ExcludeFileMask", "FileRequired", "IncludeSubFolders", "InputFields", "rowLimit",
      "inFields", "isAFile", "FieldValue", "ShortFileNameField", "PathField", "HiddenField",
      "LastModificationDateField", "UriField", "UriField", "ExtensionField", "SizeField" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();
    getterMap.put( "includeFilename", "includeFilename" );
    getterMap.put( "includeRowNumber", "includeRowNumber" );
    getterMap.put( "addResultFile", "addResultFile" );

    setterMap.put( "HiddenField", "setIsHiddenField" );

    Map<String, FieldLoadSaveValidator<?>> attributesMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    FieldLoadSaveValidator<?> fileStringArrayValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), FILE_COUNT );

    attributesMap.put( "FileName", fileStringArrayValidator );
    attributesMap.put( "FileMask", fileStringArrayValidator );
    attributesMap.put( "ExcludeFileMask", fileStringArrayValidator );
    attributesMap.put( "FileRequired", fileStringArrayValidator );
    attributesMap.put( "IncludeSubFolders", fileStringArrayValidator );

    Map<String, FieldLoadSaveValidator<?>> typeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeMap.put( JsonInputField.class.getCanonicalName(),
        new ArrayLoadSaveValidator<JsonInputField>( new JsonInputFieldValidator() ) );
    typeMap.put( JsonInputField[].class.getCanonicalName(),
      new ArrayLoadSaveValidator<JsonInputField>( new JsonInputFieldValidator() ) );

    LoadSaveTester tester =
      new LoadSaveTester( JsonInputMeta.class, attributes, new ArrayList<String>(), new ArrayList<String>(),
        getterMap, setterMap, attributesMap, typeMap, this );

    tester.testSerialization();
  }

  @SuppressWarnings( "deprecation" )
  @Override
  public void modify( StepMetaInterface arg0 ) {
    if ( arg0 instanceof JsonInputMeta ) {
      ( (JsonInputMeta) arg0 ).allocate( FILE_COUNT, FIELD_COUNT );
    }
  }

  public static class JsonInputFieldValidator implements FieldLoadSaveValidator<JsonInputField> {

    @Override
    public JsonInputField getTestObject() {
      JsonInputField retval = new JsonInputField( UUID.randomUUID().toString() );
      return retval;
    }

    @Override
    public boolean validateTestObject( JsonInputField testObject, Object actual ) {
      if ( !( actual instanceof JsonInputField ) ) {
        return false;
      }
      return ( (JsonInputField) actual ).getXML().equals( testObject.getXML() );
    }
  }
}
