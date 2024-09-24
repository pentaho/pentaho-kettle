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

package org.pentaho.di.trans.steps.getslavesequence;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class GetSlaveSequenceMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
        Arrays.asList( "valuename", "slave", "seqname", "increment" );
    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "valuename", "getValuename" );
    getterMap.put( "slave", "getSlaveServerName" );
    getterMap.put( "seqname", "getSequenceName" );
    getterMap.put( "increment", "getIncrement" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "valuename", "setValuename" );
    setterMap.put( "slave", "setSlaveServerName" );
    setterMap.put( "seqname", "setSequenceName" );
    setterMap.put( "increment", "setIncrement" );

    LoadSaveTester tester = new LoadSaveTester( GetSlaveSequenceMeta.class, attributes, getterMap, setterMap );

    tester.testSerialization();
  }
}
