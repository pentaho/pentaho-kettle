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
package org.pentaho.di.trans.steps.stepmeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class StepMetastructureMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testStepMeta() throws KettleException {
    List<String> attributes = Arrays.asList( "outputRowcount", "rowcountField" );
    // At present, none of the other fields in StepMetastructureMeta are being persisted or read
    // All the other fields in the class are set from message bundles and not persisted. See default() method.
    // MB - 5/2016
    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    LoadSaveTester loadSaveTester = new LoadSaveTester( StepMetastructureMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }

}
