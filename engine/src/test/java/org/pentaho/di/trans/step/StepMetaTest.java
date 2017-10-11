/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.util.AbstractStepMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.utils.TestUtils;
import org.pentaho.di.trans.steps.missing.MissingTrans;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class StepMetaTest {

  private static final Random rand = new Random();

  private static final String STEP_ID = "step_id";

  @Test
  public void cloning() throws Exception {
    StepMeta meta = createTestMeta();
    StepMeta clone = (StepMeta) meta.clone();
    assertEquals( meta, clone );
  }

  @Test
  public void testEqualsHashCodeConsistency() throws Exception {
    StepMeta step = new StepMeta();
    step.setName( "step" );
    TestUtils.checkEqualsHashCodeConsistency( step, step );

    StepMeta stepSame = new StepMeta();
    stepSame.setName( "step" );
    assertTrue( step.equals( stepSame ) );
    TestUtils.checkEqualsHashCodeConsistency( step, stepSame );

    StepMeta stepCaps = new StepMeta();
    stepCaps.setName( "STEP" );
    TestUtils.checkEqualsHashCodeConsistency( step, stepCaps );

    StepMeta stepOther = new StepMeta();
    stepOther.setName( "something else" );
    TestUtils.checkEqualsHashCodeConsistency( step, stepOther );
  }

  @Test
  public void stepMetaXmlConsistency() throws Exception {
    StepMeta meta = new StepMeta( "id", "name", null );
    StepMetaInterface smi = new MissingTrans( meta.getName(), meta.getStepID() );
    meta.setStepMetaInterface( smi  );
    StepMeta fromXml = StepMeta.fromXml( meta.getXML() );
    assertThat( meta.getXML(), is( fromXml.getXML() ) );
  }

  private static StepMeta createTestMeta() throws Exception {
    StepMetaInterface stepMetaInterface = mock( AbstractStepMeta.class );
    when( stepMetaInterface.clone() ).thenReturn( stepMetaInterface );

    StepMeta meta = new StepMeta( STEP_ID, "stepname", stepMetaInterface );
    meta.setSelected( true );
    meta.setDistributes( false );
    meta.setCopiesString( "2" );
    meta.setLocation( 1, 2 );
    meta.setDraw( true );
    meta.setDescription( "description" );
    meta.setTerminator( true );
    meta.setClusterSchemaName( "clusterSchemaName" );

    boolean shouldDistribute = rand.nextBoolean();
    meta.setDistributes( shouldDistribute );
    if ( shouldDistribute ) {
      meta.setRowDistribution( selectRowDistribution() );
    }

    Map<String, Map<String, String>> attributes = new HashMap<String, Map<String, String>>();
    Map<String, String> map1 = new HashMap<String, String>();
    map1.put( "1", "1" );
    Map<String, String> map2 = new HashMap<String, String>();
    map2.put( "2", "2" );

    attributes.put( "qwerty", map1 );
    attributes.put( "asdfg", map2 );
    meta.setAttributesMap( attributes );

    meta.setStepPartitioningMeta( createStepPartitioningMeta( "stepMethod", "stepSchema" ) );
    meta.setTargetStepPartitioningMeta( createStepPartitioningMeta( "targetMethod", "targetSchema" ) );

    meta.setClusterSchema( new ClusterSchema( "cluster_schema", Collections.<SlaveServer>emptyList() ) );

    return meta;
  }

  private static RowDistributionInterface selectRowDistribution() {
    return new FakeRowDistribution();
  }

  private static StepPartitioningMeta createStepPartitioningMeta( String method, String schemaName ) throws Exception {
    StepPartitioningMeta meta = new StepPartitioningMeta( method, new PartitionSchema( schemaName,
      Collections.<String>emptyList() ) );
    meta.setPartitionSchemaName( "schema_name" );
    return meta;
  }

  private static void assertEquals( StepMeta meta, StepMeta another ) {
    assertTrue( EqualsBuilder.reflectionEquals( meta, another, false, StepMeta.class,
      new String[] { "location", "targetStepPartitioningMeta" } ) );

    boolean manualCheck = new EqualsBuilder()
      .append( meta.getLocation().x, another.getLocation().x )
      .append( meta.getLocation().y, another.getLocation().y )
      .isEquals();
    assertTrue( manualCheck );
  }
}
