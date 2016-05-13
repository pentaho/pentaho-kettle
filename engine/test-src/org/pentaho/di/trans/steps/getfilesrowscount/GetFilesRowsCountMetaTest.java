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

package org.pentaho.di.trans.steps.getfilesrowscount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class GetFilesRowsCountMetaTest {

  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    List<String> attributes =
        Arrays.asList( "fileName", "fileMask", "excludeFileMask", "includeFilesCount", "filesCountFieldName",
            "rowsCountFieldName", "RowSeparator_format", "RowSeparator", "filefield", "isaddresult",
            "outputFilenameField", "fileRequired", "includeSubFolders", "smartCount" );
    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fileName", "getFileName" );
        put( "fileMask", "getFileMask" );
        put( "excludeFileMask", "getExcludeFileMask" );
        put( "includeFilesCount", "includeCountFiles" );
        put( "filesCountFieldName", "getFilesCountFieldName" );
        put( "rowsCountFieldName", "getRowsCountFieldName" );
        put( "RowSeparator_format", "getRowSeparatorFormat" );
        put( "RowSeparator", "getRowSeparator" );
        put( "filefield", "isFileField" );
        put( "isaddresult", "isAddResultFile" );
        put( "outputFilenameField", "getOutputFilenameField" );
        put( "fileRequired", "getFileRequired" );
        put( "includeSubFolders", "getIncludeSubFolders" );
        put( "smartCount", "isSmartCount" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fileName", "setFileName" );
        put( "fileMask", "setFileMask" );
        put( "excludeFileMask", "setExcludeFileMask" );
        put( "includeFilesCount", "setIncludeCountFiles" );
        put( "filesCountFieldName", "setFilesCountFieldName" );
        put( "rowsCountFieldName", "setRowsCountFieldName" );
        put( "RowSeparator_format", "setRowSeparatorFormat" );
        put( "RowSeparator", "setRowSeparator" );
        put( "filefield", "setFileField" );
        put( "isaddresult", "setAddResultFile" );
        put( "outputFilenameField", "setOutputFilenameField" );
        put( "fileRequired", "setFileRequired" );
        put( "includeSubFolders", "setIncludeSubFolders" );
        put( "smartCount", "setSmartCount" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );
    FieldLoadSaveValidator<String[]> ynArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new YNLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fileName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", ynArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", ynArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester = new LoadSaveTester( GetFilesRowsCountMetaMock.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );

  }

  @Test
  public void testLoadSaveXML() throws KettleException {
    loadSaveTester.testXmlRoundTrip();
  }

  @Test
  public void testLoadSaveRepo() throws KettleException {
    loadSaveTester.testRepoRoundTrip();
  }
  @Test
  public void testClone() throws Exception {
    GetFilesRowsCountMeta meta = new GetFilesRowsCountMeta();
    meta.allocate( 2 );
    meta.setFileName( new String[] { "field1", "field2" } );
    meta.setFileMask( new String[] { "mask1", "mask2" } );
    meta.setFileRequired( new String[] { "Y", "Y" } );
    meta.setIncludeSubFolders( new String[] { "N", "N" } );
    meta.setExcludeFileMask( new String[] { "excludemask1", "excludemask2" } );

    GetFilesRowsCountMeta cloned = (GetFilesRowsCountMeta) meta.clone();
    assertFalse( cloned.getFileName() == meta.getFileName() );
    assertTrue( Arrays.equals( cloned.getFileName(), meta.getFileName() ) );
    assertFalse( cloned.getFileMask() == meta.getFileMask() );
    assertTrue( Arrays.equals( cloned.getFileMask(), meta.getFileMask() ) );
    assertFalse( cloned.getFileRequired() == meta.getFileRequired() );
    assertTrue( Arrays.equals( cloned.getFileRequired(), meta.getFileRequired() ) );
    assertFalse( cloned.getIncludeSubFolders() == meta.getIncludeSubFolders() );
    assertTrue( Arrays.equals( cloned.getIncludeSubFolders(), meta.getIncludeSubFolders() ) );
    assertFalse( cloned.getExludeFileMask() == meta.getExludeFileMask() );
    assertTrue( Arrays.equals( cloned.getExludeFileMask(), meta.getExludeFileMask() ) );
    assertEquals( meta.getXML(), cloned.getXML() );
  }

  public class YNLoadSaveValidator implements FieldLoadSaveValidator<String> {
    Random r = new Random();

    @Override
    public String getTestObject() {
      boolean ltr = r.nextBoolean();
      String letter = ltr ? "Y" : "N";
      return letter;
    }

    @Override
    public boolean validateTestObject( String test, Object actual ) {
      return test.equals( actual );
    }
  }
}
