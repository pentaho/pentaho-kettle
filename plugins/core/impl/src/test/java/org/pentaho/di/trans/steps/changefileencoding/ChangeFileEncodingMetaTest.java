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


package org.pentaho.di.trans.steps.changefileencoding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class ChangeFileEncodingMetaTest {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
        Arrays.asList( "filenamefield", "targetfilenamefield", "sourceencoding", "targetencoding",
          "addsourceresultfilenames", "addtargetresultfilenames", "createparentfolder" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "filenamefield", "getDynamicFilenameField" );
    getterMap.put( "targetfilenamefield", "getTargetFilenameField" );
    getterMap.put( "sourceencoding", "getSourceEncoding" );
    getterMap.put( "targetencoding", "getTargetEncoding" );
    getterMap.put( "addsourceresultfilenames", "addSourceResultFilenames" );
    getterMap.put( "addtargetresultfilenames", "addTargetResultFilenames" );
    getterMap.put( "createparentfolder", "isCreateParentFolder" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "filenamefield", "setDynamicFilenameField" );
    setterMap.put( "targetfilenamefield", "setTargetFilenameField" );
    setterMap.put( "sourceencoding", "setSourceEncoding" );
    setterMap.put( "targetencoding", "setTargetEncoding" );
    setterMap.put( "addsourceresultfilenames", "setaddSourceResultFilenames" );
    setterMap.put( "addtargetresultfilenames", "setaddTargetResultFilenames" );
    setterMap.put( "createparentfolder", "setCreateParentFolder" );

    LoadSaveTester loadSaveTester =
        new LoadSaveTester( ChangeFileEncodingMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
