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

package org.pentaho.di.trans.steps.sqlfileoutput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class SQLFileOutputMetaTest {
  LoadSaveTester loadSaveTester;
  Class<SQLFileOutputMeta> testMetaClass = SQLFileOutputMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "databaseMeta", "schemaName", "tablename", "truncateTable", "AddToResult", "createTable", "fileName",
            "extension", "splitEvery", "fileAppended", "stepNrInFilename", "dateInFilename", "timeInFilename",
            "encoding", "dateFormat", "StartNewLine", "createParentFolder", "DoNotOpenNewFileInit" );

    // Note - "partNrInFilename" is used in serialization/deserialization, but there is no getter/setter for it and it's
    // not present in the dialog. Looks like a copy/paste thing, and the value itself will end up serialized/deserialized
    // as false.
    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "truncateTable", "truncateTable" );
        put( "AddToResult", "AddToResult" );
        put( "createTable", "createTable" );
        put( "StartNewLine", "StartNewLine" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

}
