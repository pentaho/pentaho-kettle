/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2020 by Hitachi Vantara : http://www.pentaho.com
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


import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.job.dialog.JobExecutionConfigurationDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.job.JobLogDelegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpoonJobDelegateTest {
  private static final String[] EMPTY_STRING_ARRAY = new String[]{};
  private static final String TEST_VARIABLE_KEY = "variableKey";
  private static final String TEST_VARIABLE_VALUE = "variableValue";
  private static final Map<String, String> MAP_WITH_TEST_VARIABLE = new HashMap<String, String>() {
    {
      put( TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE );
    }
  };
  private static final String TEST_PARAM_KEY = "paramKey";
  private static final String TEST_PARAM_VALUE = "paramValue";
  private static final Map<String, String> MAP_WITH_TEST_PARAM = new HashMap<String, String>() {
    {
      put( TEST_PARAM_KEY, TEST_PARAM_VALUE );
    }
  };
  private static final LogLevel TEST_LOG_LEVEL = LogLevel.BASIC;
  private static final String TEST_START_COPY_NAME = "startCopyName";
  private static final boolean TEST_BOOLEAN_PARAM = true;

  private SpoonJobDelegate delegate;
  private Spoon spoon;
  private JobLogTable jobLogTable;
  private JobMeta jobMeta;
  private ArrayList<JobMeta> jobMap;

  @Before
  public void before() {
    jobMap = new ArrayList<JobMeta>();

    jobMeta = mock( JobMeta.class );
    delegate = mock( SpoonJobDelegate.class );
    spoon = mock( Spoon.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = mock( SpoonTabsDelegate.class );
    spoon.variables = mock( RowMetaAndData.class );
    delegate.spoon = spoon;

    doReturn( jobMap ).when( delegate ).getJobList();
    doReturn( spoon ).when( delegate ).getSpoon();
    jobLogTable = mock( JobLogTable.class );
  }

  @Test
  public void testAddAndCloseTransformation() {
    doCallRealMethod().when( delegate ).closeJob( any() );
    doCallRealMethod().when( delegate ).addJob( any() );
    assertTrue( delegate.addJob( jobMeta ) );
    assertFalse( delegate.addJob( jobMeta ) );
    delegate.closeJob( jobMeta );
    assertTrue( delegate.addJob( jobMeta ) );
  }

  @Test
  @SuppressWarnings( "ResultOfMethodCallIgnored" )
  public void testSetParamsIntoMetaInExecuteJob() throws KettleException {
    doCallRealMethod().when( delegate ).executeJob( jobMeta, true, false, null, false,
        null, 0 );

    JobExecutionConfiguration jobExecutionConfiguration = mock( JobExecutionConfiguration.class );
    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    Shell shell = mock( Shell.class );
    JobExecutionConfigurationDialog jobExecutionConfigurationDialog = mock( JobExecutionConfigurationDialog.class );
    JobGraph activeJobGraph = mock( JobGraph.class );
    activeJobGraph.jobLogDelegate = mock( JobLogDelegate.class );


    doReturn( jobExecutionConfiguration ).when( spoon ).getJobExecutionConfiguration();
    doReturn( rowMeta ).when( spoon.variables ).getRowMeta();
    doReturn( EMPTY_STRING_ARRAY ).when( rowMeta ).getFieldNames();
    doReturn( shell ).when( spoon ).getShell();
    doReturn( jobExecutionConfigurationDialog ).when( delegate )
      .newJobExecutionConfigurationDialog( jobExecutionConfiguration, jobMeta );
    doReturn( activeJobGraph ).when( spoon ).getActiveJobGraph();
    doReturn( MAP_WITH_TEST_VARIABLE ).when( jobExecutionConfiguration ).getVariables();
    doReturn( MAP_WITH_TEST_PARAM ).when( jobExecutionConfiguration ).getParams();
    doReturn( TEST_LOG_LEVEL ).when( jobExecutionConfiguration ).getLogLevel();
    doReturn( TEST_START_COPY_NAME ).when( jobExecutionConfiguration ).getStartCopyName();
    doReturn( TEST_BOOLEAN_PARAM ).when( jobExecutionConfiguration ).isClearingLog();
    doReturn( TEST_BOOLEAN_PARAM ).when( jobExecutionConfiguration ).isSafeModeEnabled();
    doReturn( TEST_BOOLEAN_PARAM ).when( jobExecutionConfiguration ).isExpandingRemoteJob();

    delegate.executeJob( jobMeta, true, false, null, false,
        null, 0 );

    verify( jobMeta ).setVariable( TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE );
    verify( jobMeta ).setParameterValue( TEST_PARAM_KEY, TEST_PARAM_VALUE );
    verify( jobMeta ).activateParameters();
    verify( jobMeta ).setLogLevel( TEST_LOG_LEVEL );
    verify( jobMeta ).setStartCopyName( TEST_START_COPY_NAME );
    verify( jobMeta ).setClearingLog( TEST_BOOLEAN_PARAM );
    verify( jobMeta ).setSafeModeEnabled( TEST_BOOLEAN_PARAM );
    verify( jobMeta ).setExpandingRemoteJob( TEST_BOOLEAN_PARAM );
  }

  @Test
  public void testGetJobEntryDialogClass() throws KettlePluginException {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginMockInterface plugin = mock( PluginMockInterface.class );
    when( plugin.getIds() ).thenReturn( new String[] { "mockJobPlugin" } );
    when( plugin.matches( "mockJobPlugin" ) ).thenReturn( true );
    when( plugin.getName() ).thenReturn( "mockJobPlugin" );

    JobEntryInterface jobEntryInterface = mock( JobEntryInterface.class );
    when( jobEntryInterface.getDialogClassName() ).thenReturn( String.class.getName() );
    when( plugin.getClassMap() ).thenReturn( new HashMap<Class<?>, String>() {{
        put( JobEntryInterface.class, jobEntryInterface.getClass().getName() );
        put( JobEntryDialogInterface.class, JobEntryDialogInterface.class.getName() );
      }} );

    registry.registerPlugin( JobEntryPluginType.class, plugin );

    SpoonJobDelegate delegate = mock( SpoonJobDelegate.class );
    Spoon spoon = mock( Spoon.class );
    delegate.spoon = spoon;
    delegate.log = mock( LogChannelInterface.class );
    when( spoon.getShell() ).thenReturn( mock( Shell.class ) );
    doCallRealMethod().when( delegate ).getJobEntryDialog( any( JobEntryInterface.class ), any( JobMeta.class ) );

    JobMeta meta = mock( JobMeta.class );

    // verify that dialog class is requested from plugin
    try {
      delegate.getJobEntryDialog( jobEntryInterface, meta ); // exception is expected here
    } catch ( Throwable ignore ) {
      verify( jobEntryInterface, never() ).getDialogClassName();
    }

    // verify that the deprecated way is still valid
    when( plugin.getClassMap() ).thenReturn( new HashMap<Class<?>, String>() {{
        put( JobEntryInterface.class, jobEntryInterface.getClass().getName() );
      }} );
    try {
      delegate.getJobEntryDialog( jobEntryInterface, meta ); // exception is expected here
    } catch ( Throwable ignore ) {
      verify( jobEntryInterface, times( 1 ) ).getDialogClassName();
    }

    // cleanup
    registry.removePlugin( JobEntryPluginType.class, plugin );
  }

  @Test
  public void setTransMetaFileNamingWithRepTest() {
    RepositoryDirectoryInterface repDirMock = mock( RepositoryDirectoryInterface.class );
    String directory = "directory";
    DatabaseMeta sourceDataBaseMetaMock = mock( DatabaseMeta.class );
    DatabaseMeta targetDataBaseMetaMock = mock( DatabaseMeta.class );
    String[] tables = { "table1", "table2", "table3" };
    int index = 1;
    TransMeta transMeta = new TransMeta();
    doCallRealMethod().when( delegate ).setTransMetaFileNaming( repDirMock, directory, sourceDataBaseMetaMock, targetDataBaseMetaMock, tables, index, transMeta );
    delegate.setTransMetaFileNaming( repDirMock, directory, sourceDataBaseMetaMock, targetDataBaseMetaMock, tables, index, transMeta );
    String transname =
      "copy ["
        + sourceDataBaseMetaMock + "].[" + "table2"
        + "] to [" + targetDataBaseMetaMock + "]";
    assertEquals( repDirMock, transMeta.getRepositoryDirectory() );
    assertEquals( transname, transMeta.getName() );
    assertNull( transMeta.getFilename() );
  }

  @Test
  public void setTransMetaFileNamingWithoutRepTest() {
    RepositoryDirectoryInterface repDirMock = null;
    String directory = "directory";
    DatabaseMeta sourceDataBaseMetaMock = mock( DatabaseMeta.class );
    DatabaseMeta targetDataBaseMetaMock = mock( DatabaseMeta.class );
    String[] tables = { "table1", "table2", "table3" };
    int index = 1;
    TransMeta transMeta = new TransMeta();
    doCallRealMethod().when( delegate ).setTransMetaFileNaming( repDirMock, directory, sourceDataBaseMetaMock, targetDataBaseMetaMock, tables, index, transMeta );
    delegate.setTransMetaFileNaming( repDirMock, directory, sourceDataBaseMetaMock, targetDataBaseMetaMock, tables, index, transMeta );
    String transname =
      "copy ["
        + sourceDataBaseMetaMock + "].[" + "table2"
        + "] to [" + targetDataBaseMetaMock + "]";
    assertEquals( new RepositoryDirectory().getName(), transMeta.getRepositoryDirectory().getName() );
    assertNull( transMeta.getName() );
    assertEquals( Const.createFilename( directory, transname, "."
      + Const.STRING_TRANS_DEFAULT_EXT ), transMeta.getFilename() );
  }

  public interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }
}
