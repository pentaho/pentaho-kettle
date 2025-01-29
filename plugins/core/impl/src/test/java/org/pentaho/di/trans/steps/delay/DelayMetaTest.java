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


package org.pentaho.di.trans.steps.delay;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class DelayMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testStepMeta() throws KettleException {
    List<String> attributes = Arrays.asList( "timeout", "scaletime" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "timeout", "getTimeOut" );
    getterMap.put( "scaletime", "getScaleTimeCode" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "timeout", "setTimeOut" );
    setterMap.put( "scaletime", "setScaleTimeCode" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( DelayMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
