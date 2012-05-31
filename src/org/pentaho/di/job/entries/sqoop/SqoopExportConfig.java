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
  public static final String EXPORT_DIR = "exportDir";
  public static final String UPDATE_KEY = "updateKey";
  public static final String UPDATE_MODE = "updateMode";
  public static final String INPUT_NULL_STRING = "inputNullString";
  public static final String DIRECT = "direct";
  public static final String STAGING_TABLE = "stagingTable";
  public static final String CLEAR_STAGING_TABLE = "clearStagingTable";
  public static final String BATCH = "batch";
  public static final String INPUT_NULL_NON_STRING = "inputNullNonString";

  @CommandLineArgument(name = "export-dir")
  private String exportDir;
  @CommandLineArgument(name = "update-key")
  private String updateKey;
  @CommandLineArgument(name = "update-mode")
  private String updateMode;
  @CommandLineArgument(name = DIRECT, flag = true)
  private String direct;
  @CommandLineArgument(name = "staging-table")
  private String stagingTable;
  @CommandLineArgument(name = "clear-staging-table", flag = true)
  private String clearStagingTable;
  @CommandLineArgument(name = BATCH, flag = true)
  private String batch;
  @CommandLineArgument(name = "input-null-string")
  private String inputNullString;
  @CommandLineArgument(name = "input-null-non-string")
  private String inputNullNonString;

  public String getExportDir() {
    return exportDir;
  }

  public void setExportDir(String exportDir) {
    String old = this.exportDir;
    this.exportDir = exportDir;
    pcs.firePropertyChange(EXPORT_DIR, old, this.exportDir);
  }

  public String getUpdateKey() {
    return updateKey;
  }

  public void setUpdateKey(String updateKey) {
    String old = this.updateKey;
    this.updateKey = updateKey;
    pcs.firePropertyChange(UPDATE_KEY, old, this.updateKey);
  }

  public String getUpdateMode() {
    return updateMode;
  }

  public void setUpdateMode(String updateMode) {
    String old = this.updateMode;
    this.updateMode = updateMode;
    pcs.firePropertyChange(UPDATE_MODE, old, this.updateMode);
  }

  public String getDirect() {
    return direct;
  }

  public void setDirect(String direct) {
    String old = this.direct;
    this.direct = direct;
    pcs.firePropertyChange(DIRECT, old, this.direct);
  }

  public String getStagingTable() {
    return stagingTable;
  }

  public void setStagingTable(String stagingTable) {
    String old = this.stagingTable;
    this.stagingTable = stagingTable;
    pcs.firePropertyChange(STAGING_TABLE, old, this.stagingTable);
  }

  public String getClearStagingTable() {
    return clearStagingTable;
  }

  public void setClearStagingTable(String clearStagingTable) {
    String old = this.clearStagingTable;
    this.clearStagingTable = clearStagingTable;
    pcs.firePropertyChange(CLEAR_STAGING_TABLE, old, this.clearStagingTable);
  }

  public String getBatch() {
    return batch;
  }

  public void setBatch(String batch) {
    String old = this.batch;
    this.batch = batch;
    pcs.firePropertyChange(BATCH, old, this.batch);
  }

  public String getInputNullString() {
    return inputNullString;
  }

  public void setInputNullString(String inputNullString) {
    String old = this.inputNullString;
    this.inputNullString = inputNullString;
    pcs.firePropertyChange(INPUT_NULL_STRING, old, this.inputNullString);
  }

  public String getInputNullNonString() {
    return inputNullNonString;
  }

  public void setInputNullNonString(String inputNullNonString) {
    String old = this.inputNullNonString;
    this.inputNullNonString = inputNullNonString;
    pcs.firePropertyChange(INPUT_NULL_NON_STRING, old, this.inputNullNonString);
  }
}
