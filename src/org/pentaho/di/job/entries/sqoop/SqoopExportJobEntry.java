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

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProvidesDatabaseConnectionInformation;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import java.util.List;

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
public class SqoopExportJobEntry extends AbstractSqoopJobEntry<SqoopExportConfig> implements ProvidesDatabaseConnectionInformation {

  // Database meta object for UI interactions. Populated during transformation load or configuration changes via UI.
  private transient DatabaseMeta databaseMeta;

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

  /**
   * @return the current database meta. Agile BI uses this to generate a model.
   */
  @Override
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @return the current table name from the configuration. Agile BI uses this to generate a model.
   */
  @Override
  public String getTableName() {
    return environmentSubstitute(getJobConfig().getTable());
  }

  /**
   * @return the current schema name from the configuration. Agile BI uses this to generate a model.
   */
  @Override
  public String getSchemaName() {
    return environmentSubstitute(getJobConfig().getSchema());
  }

  @Override
  public String getMissingDatabaseConnectionInformationMessage() {
    if (Const.isEmpty(getJobConfig().getDatabase()) && !Const.isEmpty(getJobConfig().getConnect())) {
      // We're using advanced configuration, alert the user we cannot visualize unless we're not using a database managed by Kettle
      return BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorMustConfigureDatabaseConnectionFromList");
    }
    // Use the default error message
    return null;
  }

  /**
   * Additionally sets the database meta if a database is set.
   */
  @Override
  public void loadXML(Node node, List<DatabaseMeta> databaseMetas, List<SlaveServer> slaveServers, Repository repository) throws KettleXMLException {
    super.loadXML(node, databaseMetas, slaveServers, repository);
    setDatabaseMeta(DatabaseMeta.findDatabase(databaseMetas, getJobConfig().getDatabase()));
  }

  /**
   * Additionally sets the database meta if a database is set.
   */
  @Override
  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    super.loadRep(rep, id_jobentry, databases, slaveServers);
    setDatabaseMeta(DatabaseMeta.findDatabase(databases, getJobConfig().getDatabase()));
  }

  /**
   * Set the current database meta.
   *
   * @param databaseMeta Database meta representing the database this job is currently configured to export to
   */
  public void setDatabaseMeta(DatabaseMeta databaseMeta) {
    this.databaseMeta = databaseMeta;
  }
}
