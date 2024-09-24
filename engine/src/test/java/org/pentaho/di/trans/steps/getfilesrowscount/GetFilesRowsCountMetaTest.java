/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.getfilesrowscount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.YNLoadSaveValidator;

public class GetFilesRowsCountMetaTest implements InitializerInterface<StepMetaInterface> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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

    loadSaveTester =
        new LoadSaveTester( GetFilesRowsCountMeta.class, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof GetFilesRowsCountMeta ) {
      ( (GetFilesRowsCountMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testLoadSaveXML() throws KettleException {
    loadSaveTester.testSerialization();
  }

  // Note - cloneTest() removed as it's now covered by the load/save tester.
}
