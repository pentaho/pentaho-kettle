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


package org.pentaho.di.trans.steps.clonerow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class CloneRowMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList(
      "nrclones",
      "addcloneflag",
      "cloneflagfield",
      "nrcloneinfield",
      "nrclonefield",
      "addclonenum",
      "clonenumfield" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "nrclones", "getNrClones" );
    getterMap.put( "addcloneflag", "isAddCloneFlag" );
    getterMap.put( "cloneflagfield", "getCloneFlagField" );
    getterMap.put( "nrcloneinfield", "isNrCloneInField" );
    getterMap.put( "nrclonefield", "getNrCloneField" );
    getterMap.put( "addclonenum", "isAddCloneNum" );
    getterMap.put( "clonenumfield", "getCloneNumField" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "nrclones", "setNrClones" );
    setterMap.put( "addcloneflag", "setAddCloneFlag" );
    setterMap.put( "cloneflagfield", "setCloneFlagField" );
    setterMap.put( "nrcloneinfield", "setNrCloneInField" );
    setterMap.put( "nrclonefield", "setNrCloneField" );
    setterMap.put( "addclonenum", "setAddCloneNum" );
    setterMap.put( "clonenumfield", "setCloneNumField" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( CloneRowMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
