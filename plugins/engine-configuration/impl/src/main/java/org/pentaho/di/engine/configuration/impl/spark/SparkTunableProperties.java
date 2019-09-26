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

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Currently hard coded because we cannot simply include the bundles as they are designed today.  They have a dependency
 * on Spark which we do not want to introduce into Kettle.  In order to reflect on the tunable operations and determine
 * where which classes are annotated we would need to load the bundle without instantiating the plugins.  This has more
 * challenges than anticipated and we will not tackle them at this time.
 */
public class SparkTunableProperties {
  private static final Map<String, List<String>> stepMap = ImmutableMap.<String, List<String>>builder()
    .put( "ParquetInput", dataFrameWriterTunable() )
    .put( "ParquetOutput", multiTunable( dataFrameWriterTunable(), datasetTunable() ) )
    .put( "AvroOutput", multiTunable( dataFrameWriterTunable(), datasetTunable() ) )
    .put( "AvroInputNew", dataFrameWriterTunable() )
    .put( "OrcOutput", multiTunable( dataFrameWriterTunable(), datasetTunable() ) )
    .put( "OrcInput", dataFrameWriterTunable() )

    .put( "Dummy", datasetTunable() )

    .put( "TableInput", jdbcTunable() )
    // Reminder to add the other steps tunable options
    //    .put( "TextFileInput", datasetTunable()
    //    .put( "HadoopFileInputPlugin", datasetTunable()
    .put( "TextFileOutput", datasetTunable() )
    .put( "HadoopFileOutputPlugin", datasetTunable() )
    .put( "FilterRows", datasetTunable() )
    .put( "StreamLookup", datasetTunable() )
    .put( "SortRows", datasetTunable() )
    .put( "GroupBy", datasetTunable() )
    .put( "MemoryGroupBy", datasetTunable() )
    //    .put( "Unique", datasetTunable()
    //    .put( "UniqueRowsByHashSet", datasetTunable()
    .put( "MergeJoin", datasetTunable() )
    .put( "RecordsFromStream", datasetTunable() )
    .put( "Abort", datasetTunable() )
    .put( "TransExecutor", datasetTunable() )
    .put( "RowsFromResult", datasetTunable() )
    .put( "RowsToResult", datasetTunable() )
    .put( "WriteToLog", datasetTunable() )
    .put( "MetaInject", datasetTunable() )
    .put( "SimpleMapping", datasetTunable() )
    .put( "MappingInput", datasetTunable() )
    .put( "MappingOutput", datasetTunable() )
    .put( "Mapping", datasetTunable() )
    .put( "TableOutput", datasetTunable() )
    .put( "SwitchCase", datasetTunable() )
    .put( "MergeRows", datasetTunable() )
    .put( "JoinRows", datasetTunable() )
    .put( "JavaFilter", datasetTunable() )

    // Step List Pulled from pdi-spark-hbase-ee beans.xml
    .put( "HBaseInput", datasetTunable() )
    .put( "HBaseOutput", datasetTunable() )
    .build();

  /**
   * Retrieve properties for a step
   * @param stepId Step ID to look up
   * @return step properties if found otherwise generic properties
   */
  public static List<String> getProperties( String stepId ) {
    return stepMap.containsKey( stepId ) ? stepMap.get( stepId ) : datasetTunable();
  }

  private static List<String> dataFrameWriterTunable() {
    return Arrays.asList(
      "writer.repartition.columns",
      "writer.bucketing.columns",
      "writer.bucketing.number",
      "writer.sort.columns"
    );
  }

  private static List<String> datasetTunable() {
    return Arrays.asList(
      "cache.before",
      "coalesce.before",
      "num.repartition.before",
      "columns.repartition.before",
      "persist.type.before"
    );
  }

  private static List<String> jdbcTunable() {
    return Arrays.asList(
      "jdbc.columnName",
      "jdbc.lowerBound",
      "jdbc.upperBound",
      "jdbc.numPartitions"
    );
  }

  private static List<String> multiTunable( List<String> ... tunables ) {
    return Stream.of( tunables ).flatMap( List::stream ).collect( Collectors.toList() );
  }

  private SparkTunableProperties() {
  }
}
