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


package org.pentaho.di.trans.steps.edi2xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class Edi2XmlMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testStepMeta() throws KettleException {
    List<String> attributes = Arrays.asList( "inputfield", "outputfield" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "inputfield", "getInputField" );
    getterMap.put( "outputfield", "getOutputField" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "inputfield", "setInputField" );
    setterMap.put( "outputfield", "setOutputField" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( Edi2XmlMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
