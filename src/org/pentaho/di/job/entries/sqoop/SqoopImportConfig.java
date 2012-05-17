/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.sqoop;

/**
 * Configuration for a Sqoop Import
 */
public class SqoopImportConfig extends SqoopConfig {

  @Override
  protected void initArguments() {
    super.initArguments();
    // Import control arguments
    addArgument(new Argument("table"));
    addArgument(new Argument("target-dir"));
    addArgument(new Argument("warehouse-dir"));
    addArgument(new Argument("append", true));
    addArgument(new Argument("as-avrodatafile", true));
    addArgument(new Argument("as-sequencefile", true));
    addArgument(new Argument("as-textfile", true));
    addArgument(new Argument("boundary-query"));
    addArgument(new Argument("columns"));
    addArgument(new Argument("direct", true));
    addArgument(new Argument("direct-split-size"));
    addArgument(new Argument("inline-lob-limit"));
    addArgument(new Argument("num-mappers"));
    addArgument(new Argument("split-by"));
    addArgument(new Argument("query"));
    addArgument(new Argument("where"));
    addArgument(new Argument("compress"));
    addArgument(new Argument("compression-codec"));
    addArgument(new Argument("null-string"));
    addArgument(new Argument("null-non-string"));

    // Incremental import arguments
    addArgument(new Argument("check-column"));
    addArgument(new Argument("incremental"));
    addArgument(new Argument("last-value"));

    // Hive arguments
    addArgument(new Argument("hive-home"));
    addArgument(new Argument("hive-import", true));
    addArgument(new Argument("hive-overwrite", true));
    addArgument(new Argument("create-hive-table", true));
    addArgument(new Argument("hive-table"));
    addArgument(new Argument("hive-drop-import-delims"));
    addArgument(new Argument("hive-partition-key"));
    addArgument(new Argument("hive-partition-value"));
    addArgument(new Argument("map-column-hive"));

    // HBase arguments
    addArgument(new Argument("column-family"));
    addArgument(new Argument("hbase-create-table", true));
    addArgument(new Argument("hbase-row-key"));
    addArgument(new Argument("hbase-table"));
  }
}
