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
 * Configuration for a Sqoop Export
 */
public class SqoopExportConfig extends SqoopConfig {

  @Override
  protected void initArguments() {
    super.initArguments();
    // Export control arguments
    addArgument(new Argument("table"));
    addArgument(new Argument("export-dir"));
    addArgument(new Argument("update-key"));
    addArgument(new Argument("update-mode"));
    addArgument(new Argument("input-null-string"));
    addArgument(new Argument("num-mappers"));
    addArgument(new Argument("direct", true));
    addArgument(new Argument("staging-table"));
    addArgument(new Argument("clear-staging-table", true));
    addArgument(new Argument("batch", true));
    addArgument(new Argument("input-null-string"));
    addArgument(new Argument("input-null-non-string"));
  }
}
