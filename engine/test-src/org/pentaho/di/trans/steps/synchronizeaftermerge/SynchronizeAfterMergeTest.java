/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.synchronizeaftermerge;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.entries.evaluatetablecontent.JobEntryEvalTableContentTest.DBMockIface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

public class SynchronizeAfterMergeTest {

  private TransMeta transMeta;
  private StepMeta injectorStep;
  private StepMeta synchStep;
  private static List<RowMetaAndData> inputList;

  public static class SynchDBMockIface extends DBMockIface {

    @Override
    public String getDriverClass() {
      return "org.pentaho.di.trans.steps.synchronizeaftermerge.SynchMockDriver";
    }

  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
    Map<Class<?>, String> dbMap = new HashMap<Class<?>, String>();
    dbMap.put( DatabaseInterface.class, SynchDBMockIface.class.getName() );

    PluginRegistry preg = PluginRegistry.getInstance();

    PluginInterface mockDbPlugin = mock( PluginInterface.class );
    when( mockDbPlugin.matches( anyString() ) ).thenReturn( true );
    when( mockDbPlugin.isNativePlugin() ).thenReturn( true );
    when( mockDbPlugin.getMainType() ).thenAnswer( new Answer<Class<?>>() {
      @Override
      public Class<?> answer( InvocationOnMock invocation ) throws Throwable {
        return DatabaseInterface.class;
      }
    } );

    when( mockDbPlugin.getPluginType() ).thenAnswer( new Answer<Class<? extends PluginTypeInterface>>() {
      @Override
      public Class<? extends PluginTypeInterface> answer( InvocationOnMock invocation ) throws Throwable {
        return DatabasePluginType.class;
      }
    } );

    when( mockDbPlugin.getIds() ).thenReturn( new String[] { "Oracle", "mock-db-id" } );
    when( mockDbPlugin.getName() ).thenReturn( "mock-db-name" );
    when( mockDbPlugin.getClassMap() ).thenReturn( dbMap );

    preg.registerPlugin( DatabasePluginType.class, mockDbPlugin );

    inputList = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = {
      new ValueMeta( "personName", ValueMeta.TYPE_STRING ),
      new ValueMeta( "key", ValueMeta.TYPE_STRING ),
      new ValueMeta( "flag", ValueMeta.TYPE_STRING ) };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }
    Object[] r1 = new Object[] { "Ben", "123", "deleted" };

    inputList.add( new RowMetaAndData( rm, r1 ) );

  }

  @Before
  public void setUp() {
    transMeta = new TransMeta();
    transMeta.setName( "synchronize" );

    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    PluginRegistry registry = PluginRegistry.getInstance();
    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    DatabaseMeta dbMeta = new DatabaseMeta();
    dbMeta.setDatabaseType( "mock-db" );

    SynchronizeAfterMergeMeta synchMeta = new SynchronizeAfterMergeMeta();
    synchMeta.setCommitSize( 1 );
    synchMeta.setDatabaseMeta( dbMeta );
    synchMeta.setKeyCondition( new String[] { "=" } );
    synchMeta.setKeyLookup( new String[] { "key" } );
    synchMeta.setKeyStream( new String[] { "personName" } );
    synchMeta.setKeyStream2( new String[] { null } );
    synchMeta.setUpdate( new Boolean[] { Boolean.TRUE } );
    synchMeta.setOperationOrderField( "flag" );
    synchMeta.setOrderDelete( "deleted" );
    synchMeta.setOrderInsert( "insert" );
    synchMeta.setOrderUpdate( "update" );
    synchMeta.setPerformLookup( true );
    synchMeta.setSchemaName( "test" );
    synchMeta.setTableName( "test" );
    synchMeta.settablenameInField( false );
    synchMeta.settablenameField( null );
    synchMeta.setUseBatchUpdate( true );
    synchMeta.setUpdateLookup( new String[] { "key" } );
    synchMeta.setUpdateStream( new String[] { "personName" } );

    String synchStepname = "synch step";
    String synchPid = registry.getPluginId( StepPluginType.class, synchStepname );
    synchStep = new StepMeta( synchPid, synchStepname, synchMeta );
    transMeta.addStep( synchStep );

    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );
    StepErrorMeta sem = new StepErrorMeta( transMeta, synchStep, dummyStep1 );
    synchStep.setStepErrorMeta( sem );
    sem.setEnabled( true );

    TransHopMeta hi = new TransHopMeta( injectorStep, synchStep );
    transMeta.addTransHop( hi );

    TransHopMeta hi1 = new TransHopMeta( synchStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

  }

  @Test
  public void testNotSupport() {
    Trans trans = new Trans( transMeta );
    try {
      trans.prepareExecution( null );

      RowProducer rp = trans.addRowProducer( injectorStep.getName(), 0 );
      trans.startThreads();

      Iterator<RowMetaAndData> it = inputList.iterator();
      while ( it.hasNext() ) {
        RowMetaAndData rm = it.next();
        rp.putRow( rm.getRowMeta(), rm.getData() );
      }
      rp.finished();

      trans.waitUntilFinished();
      StepInterface si = trans.getStepInterface( synchStep.getName(), 0 );
      if ( si.getErrors() > 0 ) {
        org.junit.Assert.fail( "Unexpected error occured" );
      }
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
  }
}
