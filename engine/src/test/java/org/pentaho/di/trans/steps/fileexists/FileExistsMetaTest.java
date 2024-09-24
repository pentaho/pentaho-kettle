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

package org.pentaho.di.trans.steps.fileexists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class FileExistsMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testStepMeta() throws KettleException {
    List<String> attributes = Arrays.asList(
      "filenamefield", "resultfieldname", "includefiletype", "filetypefieldname", "addresultfilenames" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "filenamefield", "getDynamicFilenameField" );
    getterMap.put( "resultfieldname", "getResultFieldName" );
    getterMap.put( "includefiletype", "includeFileType" );
    getterMap.put( "filetypefieldname", "getFileTypeFieldName" );
    getterMap.put( "addresultfilenames", "addResultFilenames" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "filenamefield", "setDynamicFilenameField" );
    setterMap.put( "resultfieldname", "setResultFieldName" );
    setterMap.put( "includefiletype", "setincludeFileType" );
    setterMap.put( "filetypefieldname", "setFileTypeFieldName" );
    setterMap.put( "addresultfilenames", "setaddResultFilenames" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( FileExistsMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
