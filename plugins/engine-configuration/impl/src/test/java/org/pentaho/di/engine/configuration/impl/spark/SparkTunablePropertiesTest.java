/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */
package org.pentaho.di.engine.configuration.impl.spark;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.pentaho.di.core.util.Assert.assertFalse;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class SparkTunablePropertiesTest {

  @Test
  public void testGenericDatasetTunable() {
    assertTrue( SparkTunableProperties.getProperties( "badID" ).contains( "cache" ) );
    assertFalse( SparkTunableProperties.getProperties( "badID" ).contains( "read.jdbc.columnName" ) );

  }

  @Test
  public void testJoinTunable() {
    assertTrue( SparkTunableProperties.getProperties( "MergeJoin" ).contains( "join.broadcast.stepName" ) );
    assertTrue( SparkTunableProperties.getProperties( "JoinRows" ).contains( "join.broadcast.stepName" ) );
    assertTrue( SparkTunableProperties.getProperties( "MergeRows" ).contains( "join.broadcast.stepName" ) );
    assertFalse( SparkTunableProperties.getProperties( "badID" ).contains( "join.broadcast.stepName" ) );

  }

  @Test
  public void testJdbcTunable() {
    List<String> properties = SparkTunableProperties.getProperties( "TableInput" );
    assertTrue( properties.contains( "read.jdbc.columnName" ) );
    assertFalse( properties.contains( "cache" ) );
  }

  @Test
  public void testNonTunable() {
    Arrays.stream( new String[] {"AvroInputNew", "JobExecutor", "SingleThreader",
            "PrioritizeStreams", "BlockUntilStepsFinish", "Append", "S3CSVINPUT", "S3FileOutputPlugin" } ).forEach(
            str -> {
              List<String> properties = SparkTunableProperties.getProperties( str );
              assertTrue( properties.isEmpty() );
            }
    );
  }

  @Test
  // This is requried because if spark is sent a tunning property it does not recognize it
  // ignores it and does inform the user it is being ignored.
  public void guardAgainstBreakingSparkInterface() {
    // dataframe Tunable check
    assertTrue( 4 == SparkTunableProperties.dataFrameWriterTunable().size() );
    assertTrue( "write.partitionBy.columns".equals( SparkTunableProperties.dataFrameWriterTunable().get( 0 ) ) );
    assertTrue( "write.bucketBy.columns".equals( SparkTunableProperties.dataFrameWriterTunable().get( 1 ) ) );
    assertTrue( "write.bucketBy.numBuckets".equals( SparkTunableProperties.dataFrameWriterTunable().get( 2 ) ) );
    assertTrue( "write.sortBy.columns".equals( SparkTunableProperties.dataFrameWriterTunable().get( 3 ) ) );

    // dataset Tunable check
    assertTrue( 5 == SparkTunableProperties.datasetTunable().size() );
    assertTrue( "cache".equals( SparkTunableProperties.datasetTunable().get( 0 ) ) );
    assertTrue( "coalesce".equals( SparkTunableProperties.datasetTunable().get( 1 ) ) );
    assertTrue( "repartition.numPartitions".equals( SparkTunableProperties.datasetTunable().get( 2 ) ) );
    assertTrue( "repartition.columns".equals( SparkTunableProperties.datasetTunable().get( 3 ) ) );
    assertTrue( "persist.storageLevel".equals( SparkTunableProperties.datasetTunable().get( 4 ) ) );

    // broadcast Tunable check
    assertTrue( 1 == SparkTunableProperties.joinTunable().size() );
    assertTrue( "join.broadcast.stepName".equals( SparkTunableProperties.joinTunable().get( 0 ) ) );

    // jdbc Tunable check
    assertTrue( 4 == SparkTunableProperties.jdbcTunable().size() );
    assertTrue( "read.jdbc.columnName".equals( SparkTunableProperties.jdbcTunable().get( 0 ) ) );
    assertTrue( "read.jdbc.lowerBound".equals( SparkTunableProperties.jdbcTunable().get( 1 ) ) );
    assertTrue( "read.jdbc.upperBound".equals( SparkTunableProperties.jdbcTunable().get( 2 ) ) );
    assertTrue( "read.jdbc.numPartitions".equals( SparkTunableProperties.jdbcTunable().get( 3 ) ) );

  }

}
