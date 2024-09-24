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

package org.pentaho.di.trans.steps.cubeoutput;

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

public class CubeOutputMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "name", "add_to_result_filenames", "do_not_open_newfile_init" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "name", "getFilename" );
    getterMap.put( "add_to_result_filenames", "isAddToResultFiles" );
    getterMap.put( "do_not_open_newfile_init", "isDoNotOpenNewFileInit" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "name", "setFilename" );
    setterMap.put( "add_to_result_filenames", "setAddToResultFiles" );
    setterMap.put( "do_not_open_newfile_init", "setDoNotOpenNewFileInit" );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( CubeOutputMeta.class, attributes, getterMap, setterMap,
          new HashMap<String, FieldLoadSaveValidator<?>>(), new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }
}
