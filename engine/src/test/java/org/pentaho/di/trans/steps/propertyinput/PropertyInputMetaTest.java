/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.propertyinput;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

public class PropertyInputMetaTest implements InitializerInterface<StepMetaInterface> {
  Class<PropertyInputMeta> testMetaClass = PropertyInputMeta.class;
  LoadSaveTester loadSaveTester;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "encoding", "fileType", "includeFilename", "resetRowNumber", "resolvevaluevariable",
            "filenameField", "includeRowNumber", "rowNumberField", "rowLimit", "filefield", "isaddresult",
            "dynamicFilenameField", "includeIniSection", "iniSectionField", "section", "shortFileFieldName",
            "pathFieldName", "hiddenFieldName", "lastModificationTimeFieldName", "uriNameFieldName", "rootUriNameFieldName",
            "extensionFieldName", "sizeFieldName", "fileName", "fileMask", "excludeFileMask", "fileRequired",
            "includeSubFolders", "inputFields" );

    Map<String, String> getterMap = new HashMap<>() {
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
    Map<String, String> setterMap = new HashMap<>() {
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
      new ArrayLoadSaveValidator<>( new StringLoadSaveValidator(), 5 );

    FieldLoadSaveValidator<PropertyInputField[]> pifArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<>( new PropertyInputFieldLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<>();
    attrValidatorMap.put( "fileName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "inputFields", pifArrayLoadSaveValidator );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<>();

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
  public static class PropertyInputFieldLoadSaveValidator implements FieldLoadSaveValidator<PropertyInputField> {
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

  @Test
  public void testOpenNextFile() throws Exception {

    PropertyInputMeta propertyInputMeta = Mockito.mock( PropertyInputMeta.class );
    PropertyInputData propertyInputData = new PropertyInputData();
    FileInputList fileInputList = new FileInputList();
    FileObject fileObject = Mockito.mock( FileObject.class );
    FileName fileName = Mockito.mock( FileName.class );
    when( fileName.getRootURI() ).thenReturn( "testFolder" );
    when( fileName.getURI() ).thenReturn( "testFileName.ini" );

    String header = "test ini data with umlauts";
    String key = "key";
    String testValue = "value-with-äöü";
    String testData = "[" + header + "]\r\n"
            + key + "=" + testValue;
    String charsetEncode = "Windows-1252";

    InputStream inputStream = new ByteArrayInputStream( testData.getBytes(Charset.forName( charsetEncode ) ) );
    FileContent fileContent = Mockito.mock( FileContent.class );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getInputStream() ).thenReturn( inputStream );
    when( fileObject.getName() ).thenReturn( fileName );
    fileInputList.addFile( fileObject );

    propertyInputData.files = fileInputList;
    propertyInputData.propfiles = false;
    propertyInputData.realEncoding = charsetEncode;

    PropertyInput propertyInput = Mockito.mock( PropertyInput.class );

    Field logField = BaseStep.class.getDeclaredField( "log" );
    logField.setAccessible( true );
    logField.set( propertyInput, Mockito.mock( LogChannelInterface.class ) );

    doCallRealMethod().when( propertyInput ).dispose( propertyInputMeta, propertyInputData );

    propertyInput.dispose( propertyInputMeta, propertyInputData );

    Method method = PropertyInput.class.getDeclaredMethod( "openNextFile" );
    method.setAccessible( true );
    method.invoke( propertyInput );

    assertEquals( testValue, propertyInputData.iniConf.getSection( header ).getProperty( key ) );
  }
}
