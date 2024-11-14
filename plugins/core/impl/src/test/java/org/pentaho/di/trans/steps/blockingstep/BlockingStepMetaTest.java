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


package org.pentaho.di.trans.steps.blockingstep;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class BlockingStepMetaTest {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "pass_all_rows", "directory", "prefix", "cache_size", "compress" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "pass_all_rows", "isPassAllRows" );
    getterMap.put( "directory", "getDirectory" );
    getterMap.put( "prefix", "getPrefix" );
    getterMap.put( "cache_size", "getCacheSize" );
    getterMap.put( "compress", "getCompress" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "pass_all_rows", "setPassAllRows" );
    setterMap.put( "directory", "setDirectory" );
    setterMap.put( "prefix", "setPrefix" );
    setterMap.put( "cache_size", "setCacheSize" );
    setterMap.put( "compress", "setCompress" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( BlockingStepMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
