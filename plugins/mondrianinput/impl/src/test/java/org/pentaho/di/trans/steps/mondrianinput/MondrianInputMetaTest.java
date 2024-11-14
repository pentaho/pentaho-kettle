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


package org.pentaho.di.trans.steps.mondrianinput;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class MondrianInputMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes = Arrays.asList( "DatabaseMeta", "SQL", "Catalog", "Role",
      "VariableReplacementActive" );

    LoadSaveTester<MondrianInputMeta> loadSaveTester = new LoadSaveTester<>( MondrianInputMeta.class, attributes );
    loadSaveTester.testSerialization();
  }

  @Test
  public void testDefaults() {
    MondrianInputMeta meta = new MondrianInputMeta();
    meta.setDefault();
    assertNull( meta.getDatabaseMeta() );
    assertNotNull( meta.getSQL() );
    assertFalse( Utils.isEmpty( meta.getSQL() ) );
    assertFalse( meta.isVariableReplacementActive() );
  }

  @Test
  public void testGetData() {
    MondrianInputMeta meta = new MondrianInputMeta();
    assertTrue( meta.getStepData() instanceof MondrianData );
  }
}
