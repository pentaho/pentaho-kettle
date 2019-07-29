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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Currently hard coded because we cannot simply include the bundles as they are designed today.  They have a dependency
 * on Spark which we do not want to introduce into Kettle.  In order to reflect on the tunable operations and determine
 * where which classes are annotated we would need to load the bundle without instantiating the plugins.  This has more
 * challenges than anticipated and we will not tackle them at this time.
 */
public class SparkTunableProperties {

  // Special Key; for all steps that do not have an entry in the map.
  public static final String GENERIC_STEP = "SPARK_GENERIC_WRAPPED_STEP";

  public static Map<String, List<String>> fetchProperties() {

    Map<String, List<String>> stepMap = new HashMap<>();

    // Step List Pulled from pdi-spark-engine-operations beans.xml
    stepMap.put( "ParquetInput", dataFrameWriterTunable() );
    stepMap.put( "ParquetOutput", dataFrameWriterTunable() );
    stepMap.put( "AvroOutput", dataFrameWriterTunable() );
    stepMap.put( "AvroInputNew", dataFrameWriterTunable() );
    stepMap.put( "OrcOutput", dataFrameWriterTunable() );
    stepMap.put( "OrcInput", dataFrameWriterTunable() );

    stepMap.put( GENERIC_STEP, datasetTunable() );
    stepMap.put( "Dummy", datasetTunable() );

    stepMap.put( "TableInput", jdbcTunable() );

    //    stepMap.put( "TextFileInput", datasetTunable() );
    //    stepMap.put( "HadoopFileInputPlugin", datasetTunable() );
    //    stepMap.put( "TextFileOutput", datasetTunable() );
    //    stepMap.put( "HadoopFileOutputPlugin", datasetTunable() );
    //    stepMap.put( "FilterRows", datasetTunable() );
    //    stepMap.put( "StreamLookup", datasetTunable() );
    //    stepMap.put( "SortRows", datasetTunable() );
    //    stepMap.put( "GroupBy", datasetTunable() );
    //    stepMap.put( "MemoryGroupBy", datasetTunable() );
    //    stepMap.put( "Unique", datasetTunable() );
    //    stepMap.put( "UniqueRowsByHashSet", datasetTunable() );
    //    stepMap.put( "MergeJoin", datasetTunable() );
    //    stepMap.put( "RecordsFromStream", datasetTunable() );
    //    stepMap.put( "KafkaConsumerInput", datasetTunable() );
    //    stepMap.put( "KinesisConsumer", datasetTunable() );
    //    stepMap.put( "MQTTConsumer", datasetTunable() );
    //    stepMap.put( "Abort", datasetTunable() );
    //    stepMap.put( "TransExecutor", datasetTunable() );
    //    stepMap.put( "RowsFromResult", datasetTunable() );
    //    stepMap.put( "RowsToResult", datasetTunable() );
    //    stepMap.put( "WriteToLog", datasetTunable() );
    //    stepMap.put( "MetaInject", datasetTunable() );
    //    stepMap.put( "SimpleMapping", datasetTunable() );
    //    stepMap.put( "MappingInput", datasetTunable() );
    //    stepMap.put( "MappingOutput", datasetTunable() );
    //    stepMap.put( "Mapping", datasetTunable() );
    //    stepMap.put( "TableOutput", datasetTunable() );
    //    stepMap.put( "SwitchCase", datasetTunable() );
    //    stepMap.put( "MergeRows", datasetTunable() );
    //    stepMap.put( "JoinRows", datasetTunable() );
    //    stepMap.put( "JavaFilter", datasetTunable() );

    // Step List Pulled from pdi-spark-engine-operations-ee beans.xml
    //    stepMap.put( "AmqpConsumer", datasetTunable() );

    // Step List Pulled from pdi-spark-hbase-ee beans.xml
    //    stepMap.put( "HBaseInput", datasetTunable() );
    //    stepMap.put( "HBaseOutput", datasetTunable() );

    return stepMap;
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

}
