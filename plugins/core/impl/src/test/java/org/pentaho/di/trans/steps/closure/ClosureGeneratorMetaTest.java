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
package org.pentaho.di.trans.steps.closure;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class ClosureGeneratorMetaTest {
  LoadSaveTester<ClosureGeneratorMeta> loadSaveTester;
  Class<ClosureGeneratorMeta> testMetaClass = ClosureGeneratorMeta.class;

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init( false );
    List<String> attributes =
      Arrays.asList( "rootIdZero", "parentIdFieldName", "childIdFieldName", "distanceFieldName" );

    loadSaveTester = new LoadSaveTester<ClosureGeneratorMeta>( testMetaClass, attributes );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }
}
