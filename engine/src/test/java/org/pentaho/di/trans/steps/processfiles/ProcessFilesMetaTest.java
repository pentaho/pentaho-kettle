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


package org.pentaho.di.trans.steps.processfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

public class ProcessFilesMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testSerialization() throws KettleException {
    List<String> attributes = Arrays.asList( "DynamicSourceFileNameField", "DynamicTargetFileNameField",
      "OperationType", "AddTargetFileNameToResult", "OverwriteTargetFile", "CreateParentFolder",
      "Simulate" );
    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attributeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attributeMap.put( "OperationType", new IntLoadSaveValidator( ProcessFilesMeta.operationTypeCode.length ) );
    Map<String, FieldLoadSaveValidator<?>> typeMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    LoadSaveTester<ProcessFilesMeta> tester = new LoadSaveTester<ProcessFilesMeta>(
      ProcessFilesMeta.class, attributes, getterMap, setterMap, attributeMap, typeMap );

    tester.testSerialization();
  }
}
