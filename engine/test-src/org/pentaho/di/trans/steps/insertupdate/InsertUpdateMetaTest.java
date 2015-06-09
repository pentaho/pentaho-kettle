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

package org.pentaho.di.trans.steps.insertupdate;

import java.util.HashMap;
import java.util.Map;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public class InsertUpdateMetaTest {

  private StepMeta stepMeta;
  private InsertUpdate upd;
  private InsertUpdateData ud;
  private InsertUpdateMeta umi;

  @BeforeClass
  public static void initEnvironment() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "delete1" );

    Map<String, String> vars = new HashMap<String, String>();
    vars.put( "max.sz", "10" );
    transMeta.injectVariables( vars );

    umi = new InsertUpdateMeta();
    ud = new InsertUpdateData();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String deletePid = plugReg.getPluginId( StepPluginType.class, umi );

    stepMeta = new StepMeta( deletePid, "delete", umi );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );
    upd = new InsertUpdate( stepMeta, ud, 1, transMeta, trans );
    upd.copyVariablesFrom( transMeta );
  }

  @Test
  public void testCommitCountFixed() {
    umi.setCommitSize( "100" );
    assertTrue( umi.getCommitSize( upd ) == 100 );
  }

  @Test
  public void testCommitCountVar() {
    umi.setCommitSize( "${max.sz}" );
    assertTrue( umi.getCommitSize( upd ) == 10 );
  }

  @Test
  public void testProvidesModeler() throws Exception {
    InsertUpdateMeta insertUpdateMeta = new InsertUpdateMeta();
    insertUpdateMeta.setUpdateLookup( new String[] {"f1", "f2", "f3"} );
    insertUpdateMeta.setUpdateStream( new String[] {"s4", "s5", "s6"} );

    InsertUpdateData tableOutputData = new InsertUpdateData();
    tableOutputData.insertRowMeta = Mockito.mock( RowMeta.class );
    assertEquals( tableOutputData.insertRowMeta, insertUpdateMeta.getRowMeta( tableOutputData ) );
    assertEquals( 3, insertUpdateMeta.getDatabaseFields().size() );
    assertEquals( "f1", insertUpdateMeta.getDatabaseFields().get( 0 ) );
    assertEquals( "f2", insertUpdateMeta.getDatabaseFields().get( 1 ) );
    assertEquals( "f3", insertUpdateMeta.getDatabaseFields().get( 2 ) );
    assertEquals( 3, insertUpdateMeta.getStreamFields().size() );
    assertEquals( "s4", insertUpdateMeta.getStreamFields().get( 0 ) );
    assertEquals( "s5", insertUpdateMeta.getStreamFields().get( 1 ) );
    assertEquals( "s6", insertUpdateMeta.getStreamFields().get( 2 ) );
  }

  @Test
  public void testCommitCountMissedVar() {
    umi.setCommitSize( "missed-var" );
    try {
      umi.getCommitSize( upd );
      fail();
    } catch ( Exception ex ) {
    }
  }

}
