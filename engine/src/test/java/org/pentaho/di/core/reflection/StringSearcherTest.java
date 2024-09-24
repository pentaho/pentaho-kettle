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
package org.pentaho.di.core.reflection;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StringSearcherTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws org.pentaho.di.core.exception.KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void testSearchConditionCase() {
    String dummyStepname = "Output";
    DummyTransMeta dummyMeta = new DummyTransMeta();
    String dummyStepPid = PluginRegistry.getInstance().getPluginId( StepPluginType.class, dummyMeta );
    StepMeta dummyStep = new StepMeta( dummyStepPid, dummyStepname, dummyMeta );

    List<StringSearchResult> stringList = new ArrayList<StringSearchResult>();
    StringSearcher.findMetaData( dummyStep, 0, stringList, dummyMeta, 0 );

    int checkCount = 0;
    String aResult = null;
    // Check that it found a couple of fields and emits the values properly
    for ( int i = 0; i < stringList.size(); i++ ) {
      aResult = stringList.get( i ).toString();
      if ( aResult.endsWith( "Dummy (stepid)" ) ) {
        checkCount++;
      } else if ( aResult.endsWith( "Output (name)" ) ) {
        checkCount++;
      }
      if ( checkCount == 2 ) {
        break;
      }
    }
    assertEquals( 2, checkCount );

    FilterRowsMeta filterRowsMeta = new FilterRowsMeta();
    Condition condition = new Condition();
    condition.setNegated( false );
    condition.setLeftValuename( "wibble_t" );
    condition.setRightValuename( "wobble_s" );
    condition.setFunction( org.pentaho.di.core.Condition.FUNC_EQUAL );
    filterRowsMeta.setDefault();
    filterRowsMeta.setCondition( condition );

    String filterRowsPluginPid = PluginRegistry.getInstance().getPluginId( StepPluginType.class, filterRowsMeta );
    StepMeta filterRowsStep = new StepMeta( filterRowsPluginPid, "Filter Rows", filterRowsMeta );

    stringList.clear();
    StringSearcher.findMetaData( filterRowsStep, 0, stringList, filterRowsMeta, 0 );

    checkCount = 0;
    for ( int i = 0; i < stringList.size(); i++ ) {
      aResult = stringList.get( i ).toString();
      if ( aResult.endsWith( "FilterRows (stepid)" ) ) {
        checkCount++;
      } else  if ( aResult.endsWith( "Filter Rows (name)" ) ) {
        checkCount++;
      }
      if ( checkCount == 2 ) {
        break;
      }
    }
    assertEquals( 2, checkCount );
  }
}
