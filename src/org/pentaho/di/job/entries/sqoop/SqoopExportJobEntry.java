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

import org.pentaho.di.core.annotations.JobEntry;

/**
 * Provides a way to orchestrate <a href="http://sqoop.apache.org/">Sqoop</a> exports.
 */
@JobEntry(id = "SqoopExport",
  name = "Sqoop.Export.PluginName",
  description = "Sqoop.Export.PluginDescription",
  categoryDescription = "BigData.Category.Description",
  image = "sqoop-export.png",
  i18nPackageName = "org.pentaho.di.job.entries.sqoop",
  version = "1"
)
public class SqoopExportJobEntry extends AbstractSqoopJobEntry<SqoopExportConfig> {

  @Override
  protected SqoopExportConfig buildSqoopConfig() {
    return new SqoopExportConfig();
  }

  /**
   * @return the name of the Sqoop export tool: "export"
   */
  @Override
  protected String getToolName() {
    return "export";
  }

}
