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


package org.pentaho.di.trans.steps.flattener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class FlattenerMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "field_name", "target" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "field_name", "getFieldName" );
    getterMap.put( "target", "getTargetField" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "field_name", "setFieldName" );
    setterMap.put( "target", "setTargetField" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( FlattenerMeta.class, attributes, getterMap, setterMap );

    loadSaveTester.testSerialization();
  }
}
