/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon.delegates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.di.ui.spoon.trans.TransLogDelegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SpoonTransformationDelegateTest {
  private static final String[] EMPTY_STRING_ARRAY = new String[]{};
  private static final String TEST_PARAM_KEY = "paramKey";
  private static final String TEST_PARAM_VALUE = "paramValue";
  private static final Map<String, String> MAP_WITH_TEST_PARAM = new HashMap<String, String>() {
    {
      put( TEST_PARAM_KEY, TEST_PARAM_VALUE );
    }
  };
  private static final LogLevel TEST_LOG_LEVEL = LogLevel.BASIC;
  private static final boolean TEST_BOOLEAN_PARAM = true;

  private SpoonTransformationDelegate delegate;
  private Spoon spoon;

  private TransLogTable transLogTable;
  private TransMeta transMeta;
  private List<TransMeta> transformationMap;

  @Before
  public void before() {
    transformationMap = new ArrayList<TransMeta>();

    transMeta = mock( TransMeta.class );
    delegate = mock( SpoonTransformationDelegate.class );
    spoon = mock( Spoon.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = mock( SpoonTabsDelegate.class );
    spoon.variables = mock( RowMetaAndData.class );
    delegate.spoon = spoon;

    doReturn( transformationMap ).when( delegate ).getTransformationList();
    doReturn( spoon ).when( delegate ).getSpoon();
    doCallRealMethod().when( delegate ).isLogTableDefined( any() );
    transLogTable = mock( TransLogTable.class );
  }

  @Test
  public void testIsLogTableDefinedLogTableDefined() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    doReturn( databaseMeta ).when( transLogTable ).getDatabaseMeta();
    doReturn( "test_table" ).when( transLogTable ).getTableName();

    assertTrue( delegate.isLogTableDefined( transLogTable ) );
  }

  @Test
  public void testIsLogTableDefinedLogTableNotDefined() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    doReturn( databaseMeta ).when( transLogTable ).getDatabaseMeta();

    assertFalse( delegate.isLogTableDefined( transLogTable ) );
  }

  @Test
  public void testAddAndCloseTransformation() {
    doCallRealMethod().when( delegate ).closeTransformation( any() );
    doCallRealMethod().when( delegate ).addTransformation( any() );
    assertTrue( delegate.addTransformation( transMeta ) );
    assertFalse( delegate.addTransformation( transMeta ) );
    delegate.closeTransformation( transMeta );
    assertTrue( delegate.addTransformation( transMeta ) );
  }

  @Test
  @SuppressWarnings( "ResultOfMethodCallIgnored" )
  public void testSetParamsIntoMetaInExecuteTransformation() throws KettleException {
    doCallRealMethod().when( delegate ).executeTransformation( transMeta, true, false, false,
            false, false, null, false, LogLevel.BASIC );

    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    TransExecutionConfiguration transExecutionConfiguration = mock( TransExecutionConfiguration.class );
    TransGraph activeTransGraph = mock( TransGraph.class );
    activeTransGraph.transLogDelegate = mock( TransLogDelegate.class );

    doReturn( rowMeta ).when( spoon.variables ).getRowMeta();
    doReturn( EMPTY_STRING_ARRAY ).when( rowMeta ).getFieldNames();
    doReturn( transExecutionConfiguration ).when( spoon ).getTransExecutionConfiguration();
    doReturn( MAP_WITH_TEST_PARAM ).when( transExecutionConfiguration ).getParams();
    doReturn( activeTransGraph ).when( spoon ).getActiveTransGraph();
    doReturn( TEST_LOG_LEVEL ).when( transExecutionConfiguration ).getLogLevel();
    doReturn( TEST_BOOLEAN_PARAM ).when( transExecutionConfiguration ).isClearingLog();
    doReturn( TEST_BOOLEAN_PARAM ).when( transExecutionConfiguration ).isSafeModeEnabled();
    doReturn( TEST_BOOLEAN_PARAM ).when( transExecutionConfiguration ).isGatheringMetrics();

    delegate.executeTransformation( transMeta, true, false, false, false, false,
            null, false, LogLevel.BASIC );

    verify( transMeta ).setParameterValue( TEST_PARAM_KEY, TEST_PARAM_VALUE );
    verify( transMeta ).activateParameters();
    verify( transMeta ).setLogLevel( TEST_LOG_LEVEL );
    verify( transMeta ).setClearingLog( TEST_BOOLEAN_PARAM );
    verify( transMeta ).setSafeModeEnabled( TEST_BOOLEAN_PARAM );
    verify( transMeta ).setGatheringMetrics( TEST_BOOLEAN_PARAM );
  }
}
