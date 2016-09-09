/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.propertyinput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class PropertyInputMetaTest implements InitializerInterface<StepMetaInterface> {
  Class<PropertyInputMeta> testMetaClass = PropertyInputMeta.class;
  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes =
        Arrays.asList( "encoding", "fileType", "includeFilename", "resetRowNumber", "resolvevaluevariable",
            "filenameField", "includeRowNumber", "rowNumberField", "rowLimit", "filefield", "isaddresult",
            "dynamicFilenameField", "includeIniSection", "iniSectionField", "section", "shortFileFieldName",
            "pathFieldName", "hiddenFieldName", "lastModificationTimeFieldName", "uriNameFieldName", "rootUriNameFieldName",
            "extensionFieldName", "sizeFieldName", "fileName", "fileMask", "excludeFileMask", "fileRequired",
            "includeSubFolders", "inputFields" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "encoding", "getEncoding" );
        put( "fileType", "getFileType" );
        put( "includeFilename", "includeFilename" );
        put( "resetRowNumber", "resetRowNumber" );
        put( "resolvevaluevariable", "isResolveValueVariable" );
        put( "filenameField", "getFilenameField" );
        put( "includeRowNumber", "includeRowNumber" );
        put( "rowNumberField", "getRowNumberField" );
        put( "rowLimit", "getRowLimit" );
        put( "filefield", "isFileField" );
        put( "isaddresult", "isAddResultFile" );
        put( "dynamicFilenameField", "getDynamicFilenameField" );
        put( "includeIniSection", "includeIniSection" );
        put( "iniSectionField", "getINISectionField" );
        put( "section", "getSection" );
        put( "shortFileFieldName", "getShortFileNameField" );
        put( "pathFieldName", "getPathField" );
        put( "hiddenFieldName", "isHiddenField" );
        put( "lastModificationTimeFieldName", "getLastModificationDateField" );
        put( "uriNameFieldName", "getUriField" );
        put( "rootUriNameFieldName", "getRootUriField" );
        put( "extensionFieldName", "getExtensionField" );
        put( "sizeFieldName", "getSizeField" );
        put( "fileName", "getFileName" );
        put( "fileMask", "getFileMask" );
        put( "excludeFileMask", "getExcludeFileMask" );
        put( "fileRequired", "getFileRequired" );
        put( "includeSubFolders", "getIncludeSubFolders" );
        put( "inputFields", "getInputFields" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "encoding", "setEncoding" );
        put( "fileType", "setFileType" );
        put( "includeFilename", "setIncludeFilename" );
        put( "resetRowNumber", "setResetRowNumber" );
        put( "resolvevaluevariable", "setResolveValueVariable" );
        put( "filenameField", "setFilenameField" );
        put( "includeRowNumber", "setIncludeRowNumber" );
        put( "rowNumberField", "setRowNumberField" );
        put( "rowLimit", "setRowLimit" );
        put( "filefield", "setFileField" );
        put( "isaddresult", "setAddResultFile" );
        put( "dynamicFilenameField", "setDynamicFilenameField" );
        put( "includeIniSection", "setIncludeIniSection" );
        put( "iniSectionField", "setINISectionField" );
        put( "section", "setSection" );
        put( "shortFileFieldName", "setShortFileNameField" );
        put( "pathFieldName", "setPathField" );
        put( "hiddenFieldName", "setIsHiddenField" );
        put( "lastModificationTimeFieldName", "setLastModificationDateField" );
        put( "uriNameFieldName", "setUriField" );
        put( "rootUriNameFieldName", "setRootUriField" );
        put( "extensionFieldName", "setExtensionField" );
        put( "sizeFieldName", "setSizeField" );
        put( "fileName", "setFileName" );
        put( "fileMask", "setFileMask" );
        put( "excludeFileMask", "setExcludeFileMask" );
        put( "fileRequired", "setFileRequired" );
        put( "includeSubFolders", "setIncludeSubFolders" );
        put( "inputFields", "setInputFields" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    FieldLoadSaveValidator<PropertyInputField[]> pifArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<PropertyInputField>( new PropertyInputFieldLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fileName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "inputFields", pifArrayLoadSaveValidator );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    // typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator(), 1 ) );

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface propInputMeta ) {
    if ( propInputMeta instanceof PropertyInputMeta ) {
      ( (PropertyInputMeta) propInputMeta ).allocate( 5, 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  //PropertyInputField
  public class PropertyInputFieldLoadSaveValidator implements FieldLoadSaveValidator<PropertyInputField> {
    final Random rand = new Random();
    @Override
    public PropertyInputField getTestObject() {
      PropertyInputField rtn = new PropertyInputField();
      rtn.setCurrencySymbol( UUID.randomUUID().toString() );
      rtn.setDecimalSymbol( UUID.randomUUID().toString() );
      rtn.setFormat( UUID.randomUUID().toString() );
      rtn.setGroupSymbol( UUID.randomUUID().toString() );
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setTrimType( rand.nextInt( 4 ) );
      rtn.setPrecision( rand.nextInt( 9 ) );
      rtn.setRepeated( rand.nextBoolean() );
      rtn.setLength( rand.nextInt( 50 ) );
      rtn.setSamples( new String[] { UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString() } );
      return rtn;
    }

    @Override
    public boolean validateTestObject( PropertyInputField testObject, Object actual ) {
      if ( !( actual instanceof PropertyInputField ) ) {
        return false;
      }
      PropertyInputField actualInput = (PropertyInputField) actual;
      return ( testObject.toString().equals( actualInput.toString() ) );
    }
  }

}
