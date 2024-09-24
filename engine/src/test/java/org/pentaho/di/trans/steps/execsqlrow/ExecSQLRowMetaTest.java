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
package org.pentaho.di.trans.steps.execsqlrow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class ExecSQLRowMetaTest {
  LoadSaveTester loadSaveTester;
  Class<ExecSQLRowMeta> testMetaClass = ExecSQLRowMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "sqlFieldName", "updateField", "insertField", "deleteField", "readField", "commitSize", "sqlFromfile", "sendOneStatement", "databaseMeta" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "sendOneStatement", "IsSendOneStatement" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "sendOneStatement", "SetSendOneStatement" );
      }
    };

    loadSaveTester = new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }
}
