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

package org.pentaho.di.trans.steps.loadfileinput;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.YNLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * User: Dzmitry Stsiapanau Date: 12/17/13 Time: 3:11 PM
 */
public class LoadFileInputMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;

  String xmlOrig = "    " + "<include>N</include>    <include_field/>    <rownum>N</rownum>   "
          + " <addresultfile>Y</addresultfile>    <IsIgnoreEmptyFile>N</IsIgnoreEmptyFile>   "
          + " <IsIgnoreMissingPath>N</IsIgnoreMissingPath>    <rownum_field/>   "
          + " <encoding/>    <file>      <name>D:\\DZMITRY</name>      <filemask>*/</filemask>     "
          + " <exclude_filemask>/***</exclude_filemask>      <file_required>N</file_required>     "
          + " <include_subfolders>N</include_subfolders>      </file>    <fields>      </fields>   "
          + " <limit>0</limit>    <IsInFields>N</IsInFields>    <DynamicFilenameField/>   "
          + " <shortFileFieldName/>    <pathFieldName/>    <hiddenFieldName/>    <lastModificationTimeFieldName/>   "
          + " <uriNameFieldName/>    <rootUriNameFieldName/>    <extensionFieldName/>";

  public LoadFileInputMeta createMeta() throws Exception {
    LoadFileInputMeta meta = new LoadFileInputMeta();
    meta.allocate( 1, 0 );
    meta.setIncludeFilename( false );
    meta.setFilenameField( null );
    meta.setAddResultFile( true );
    meta.setIgnoreEmptyFile( false );
    meta.setIncludeRowNumber( false );
    meta.setRowNumberField( null );
    meta.setEncoding( null );
    meta.setFileName( new String[] { "D:\\DZMITRY" } );
    meta.setFileMask( new String[] { "*/" } );
    meta.setExcludeFileMask( new String[] { "/***" } );
    meta.setFileRequired( new String[] { "N" } );
    meta.setIncludeSubFolders( new String[] { "N" } );
    meta.setRowLimit( 0 );
    meta.setIsInFields( false );
    meta.setDynamicFilenameField( null );
    meta.setShortFileNameField( null );
    meta.setPathField( null );
    meta.setIsHiddenField( null );
    meta.setLastModificationDateField( null );
    meta.setUriField( null );
    meta.setRootUriField( null );
    meta.setExtensionField( null );
    return meta;
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetXML() throws Exception {
    LoadFileInputMeta testMeta = createMeta();
    String xml = testMeta.getXML();
    assertEquals( xmlOrig.replaceAll( "\n", "" ).replaceAll( "\r", "" ), xml.replaceAll( "\n", "" ).replaceAll( "\r",
        "" ) );
  }

  @Test
  public void testLoadXML() throws Exception {
    LoadFileInputMeta origMeta = createMeta();
    LoadFileInputMeta testMeta = new LoadFileInputMeta();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse( new InputSource( new StringReader( "<step>" + xmlOrig + "</step>" ) ) );
    IMetaStore metaStore = null;
    testMeta.loadXML( doc.getFirstChild(), null, metaStore );
    assertEquals( origMeta, testMeta );
  }

  @Before
  public void setUp() throws Exception {
    List<String> attributes =
        Arrays.asList( "includeFilename", "filenameField", "includeRowNumber", "rowNumberField", "rowLimit",
            "encoding", "DynamicFilenameField", "fileinfield", "addresultfile", "IsIgnoreEmptyFile", "IsIgnoreMissingPath",
            "shortFileFieldName", "pathFieldName", "hiddenFieldName", "lastModificationTimeFieldName",
            "uriNameFieldName", "rootUriNameFieldName", "extensionFieldName", "includeSubFolders", "fileName",
            "fileMask", "excludeFileMask", "fileRequired", "inputFields" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "includeFilename", "getIncludeFilename" );
        put( "filenameField", "getFilenameField" );
        put( "includeRowNumber", "getIncludeRowNumber" );
        put( "rowNumberField", "getRowNumberField" );
        put( "rowLimit", "getRowLimit" );
        put( "encoding", "getEncoding" );
        put( "DynamicFilenameField", "getDynamicFilenameField" );
        put( "fileinfield", "getFileInFields" );
        put( "addresultfile", "getAddResultFile" );
        put( "IsIgnoreEmptyFile", "isIgnoreEmptyFile" );
        put( "IsIgnoreMissingPath", "isIgnoreMissingPath" );
        put( "shortFileFieldName", "getShortFileNameField" );
        put( "pathFieldName", "getPathField" );
        put( "hiddenFieldName", "isHiddenField" );
        put( "lastModificationTimeFieldName", "getLastModificationDateField" );
        put( "uriNameFieldName", "getUriField" );
        put( "rootUriNameFieldName", "getRootUriField" );
        put( "extensionFieldName", "getExtensionField" );
        put( "includeSubFolders", "getIncludeSubFolders" );
        put( "fileName", "getFileName" );
        put( "fileMask", "getFileMask" );
        put( "excludeFileMask", "getExcludeFileMask" );
        put( "fileRequired", "getFileRequired" );
        put( "inputFields", "getInputFields" );
      }
    };

    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "includeFilename", "setIncludeFilename" );
        put( "filenameField", "setFilenameField" );
        put( "includeRowNumber", "setIncludeRowNumber" );
        put( "rowNumberField", "setRowNumberField" );
        put( "rowLimit", "setRowLimit" );
        put( "encoding", "setEncoding" );
        put( "DynamicFilenameField", "setDynamicFilenameField" );
        put( "fileinfield", "setFileInFields" );
        put( "addresultfile", "setAddResultFile" );
        put( "IsIgnoreEmptyFile", "setIgnoreEmptyFile" );
        put( "IsIgnoreMissingPath", "setIgnoreMissingPath" );
        put( "shortFileFieldName", "setShortFileNameField" );
        put( "pathFieldName", "setPathField" );
        put( "hiddenFieldName", "setIsHiddenField" );
        put( "lastModificationTimeFieldName", "setLastModificationDateField" );
        put( "uriNameFieldName", "setUriField" );
        put( "rootUriNameFieldName", "setRootUriField" );
        put( "extensionFieldName", "setExtensionField" );
        put( "includeSubFolders", "setIncludeSubFolders" );
        put( "fileName", "setFileName" );
        put( "fileMask", "setFileMask" );
        put( "excludeFileMask", "setExcludeFileMask" );
        put( "fileRequired", "setFileRequired" );
        put( "inputFields", "setInputFields" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    FieldLoadSaveValidator<LoadFileInputField[]> lfifArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<LoadFileInputField>( new LoadFileInputFieldLoadSaveValidator(), 5 );

    FieldLoadSaveValidator<String[]> YNArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new YNLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", YNArrayLoadSaveValidator );

    attrValidatorMap.put( "inputFields", lfifArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( LoadFileInputMeta.class, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof LoadFileInputMeta ) {
      ( (LoadFileInputMeta) someMeta ).allocate( 5, 5 );
    }
  }


  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class LoadFileInputFieldLoadSaveValidator implements FieldLoadSaveValidator<LoadFileInputField> {
    final Random rand = new Random();
    @Override
    public LoadFileInputField getTestObject() {
      LoadFileInputField rtn = new LoadFileInputField();
      rtn.setCurrencySymbol( UUID.randomUUID().toString() );
      rtn.setDecimalSymbol( UUID.randomUUID().toString() );
      rtn.setFormat( UUID.randomUUID().toString() );
      rtn.setGroupSymbol( UUID.randomUUID().toString() );
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setElementType( rand.nextInt( 2 ) );
      rtn.setTrimType( rand.nextInt( 4 ) );
      rtn.setType( rand.nextInt( 5 ) );
      rtn.setPrecision( rand.nextInt( 9 ) );
      rtn.setRepeated( rand.nextBoolean() );
      rtn.setLength( rand.nextInt( 50 ) );
      return rtn;
    }
    @Override
    public boolean validateTestObject( LoadFileInputField testObject, Object actualObj ) {
      if ( !( actualObj instanceof LoadFileInputField ) ) {
        return false;
      }
      LoadFileInputField actual = (LoadFileInputField) actualObj;
      boolean tst1 =  ( actual.getXML().equals( testObject.getXML() ) );
      LoadFileInputField aClone = (LoadFileInputField) testObject.clone();
      boolean tst2 = ( actual.getXML().equals( aClone.getXML() ) );
      return ( tst1 && tst2 );
    }
  }

}
