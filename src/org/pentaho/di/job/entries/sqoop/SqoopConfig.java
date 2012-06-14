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

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.BlockableJobConfig;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.util.AbstractModelList;

/**
 * A collection of configuration objects for a Sqoop job entry.
 */
public abstract class SqoopConfig extends BlockableJobConfig implements XulEventSource, Cloneable {
  public static final String NAMENODE_HOST = "namenodeHost";
  public static final String NAMENODE_PORT = "namenodePort";
  public static final String JOBTRACKER_HOST = "jobtrackerHost";
  public static final String JOBTRACKER_PORT = "jobtrackerPort";

  public static final String DATABASE = "database";
  public static final String SCHEMA = "schema";

  // Common arguments
  public static final String CONNECT = "connect";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String VERBOSE = "verbose";
  public static final String CONNECTION_MANAGER = "connectionManager";
  public static final String DRIVER = "driver";
  public static final String CONNECTION_PARAM_FILE = "connectionParamFile";
  public static final String HADOOP_HOME = "hadoopHome";

  // Output line formatting arguments
  public static final String ENCLOSED_BY = "enclosedBy";
  public static final String ESCAPED_BY = "escapedBy";
  public static final String FIELDS_TERMINATED_BY = "fieldsTerminatedBy";
  public static final String LINES_TERMINATED_BY = "linesTerminatedBy";
  public static final String OPTIONALLY_ENCLOSED_BY = "optionallyEnclosedBy";
  public static final String MYSQL_DELIMITERS = "mysqlDelimiters";

  // Input parsing arguments
  public static final String INPUT_ENCLOSED_BY = "inputEnclosedBy";
  public static final String INPUT_ESCAPED_BY = "inputEscapedBy";
  public static final String INPUT_FIELDS_TERMINATED_BY = "inputFieldsTerminatedBy";
  public static final String INPUT_LINES_TERMINATED_BY = "inputLinesTerminatedBy";
  public static final String INPUT_OPTIONALLY_ENCLOSED_BY = "inputOptionallyEnclosedBy";

  // Code generation arguments
  public static final String BIN_DIR = "binDir";
  public static final String CLASS_NAME = "className";
  public static final String JAR_FILE = "jarFile";
  public static final String OUTDIR = "outdir";
  public static final String PACKAGE_NAME = "packageName";
  public static final String MAP_COLUMN_JAVA = "mapColumnJava";

  // Shared Input/Export options
  public static final String TABLE = "table";
  public static final String NUM_MAPPERS = "numMappers";
  public static final String COMMAND_LINE = "commandLine";
  public static final String MODE = "mode";

  private String namenodeHost;
  private String namenodePort;
  private String jobtrackerHost;
  private String jobtrackerPort;

  private String database;
  private String schema;

  // Properties to support toggling between quick setup and advanced mode in the UI. These should never be saved.
  private transient String connectFromAdvanced;
  private transient String usernameFromAdvanced;
  private transient String passwordFromAdvanced;

  /**
   * Represents the last visible state of the UI and the execution mode.
   */
  public enum Mode {
    QUICK_SETUP,
    ADVANCED_LIST,
    ADVANCED_COMMAND_LINE;
  }

  private String mode;

  // Common arguments
  @CommandLineArgument(name = CONNECT)
  private String connect;

  @CommandLineArgument(name = "connection-manager")
  private String connectionManager;
  @CommandLineArgument(name = DRIVER)
  private String driver;
  @CommandLineArgument(name = USERNAME)
  private String username;
  @CommandLineArgument(name = PASSWORD)
  @Password
  private String password;
  @CommandLineArgument(name = VERBOSE, flag = true)
  private String verbose;
  @CommandLineArgument(name = "connection-param-file")
  private String connectionParamFile;
  @CommandLineArgument(name = "hadoop-home")
  private String hadoopHome;
  // Output line formatting arguments
  @CommandLineArgument(name = "enclosed-by")
  private String enclosedBy;

  @CommandLineArgument(name = "escaped-by")
  private String escapedBy;
  @CommandLineArgument(name = "fields-terminated-by")
  private String fieldsTerminatedBy;
  @CommandLineArgument(name = "lines-terminated-by")
  private String linesTerminatedBy;
  @CommandLineArgument(name = "optionally-enclosed-by")
  private String optionallyEnclosedBy;
  @CommandLineArgument(name = "mysql-delimiters", flag = true)
  private String mysqlDelimiters;
  // Input parsing arguments
  @CommandLineArgument(name = "input-enclosed-by")
  private String inputEnclosedBy;

  @CommandLineArgument(name = "input-escaped-by")
  private String inputEscapedBy;
  @CommandLineArgument(name = "input-fields-terminated-by")
  private String inputFieldsTerminatedBy;
  @CommandLineArgument(name = "input-lines-terminated-by")
  private String inputLinesTerminatedBy;
  @CommandLineArgument(name = "input-optionally-enclosed-by")
  private String inputOptionallyEnclosedBy;
  // Code generation arguments
  @CommandLineArgument(name = "bindir")
  private String binDir;

  @CommandLineArgument(name = "class-name")
  private String className;
  @CommandLineArgument(name = "jar-file")
  private String jarFile;
  @CommandLineArgument(name = OUTDIR)
  private String outdir;
  @CommandLineArgument(name = "package-name")
  private String packageName;
  @CommandLineArgument(name = "map-column-java")
  private String mapColumnJava;

  // Shared Input/Export options
  @CommandLineArgument(name = TABLE)
  private String table;
  @CommandLineArgument(name = "num-mappers")
  private String numMappers;
  private String commandLine;

  /**
   * @return all known arguments for this config object. Some arguments may be synthetic and represent properties
   *         directly set on this config object for the purpose of showing them in the list view of the UI.
   */
  public AbstractModelList<ArgumentWrapper> getAdvancedArgumentsList() {
    final AbstractModelList<ArgumentWrapper> items = new AbstractModelList<ArgumentWrapper>();

    items.addAll(SqoopUtils.findAllArguments(this));

    try {
      items.add(new ArgumentWrapper(NAMENODE_HOST, BaseMessages.getString(getClass(), "NamenodeHost.Label"), false, this,
        getClass().getMethod("getNamenodeHost"), getClass().getMethod("setNamenodeHost", String.class)));
      items.add(new ArgumentWrapper(NAMENODE_PORT, BaseMessages.getString(getClass(), "NamenodePort.Label"), false, this,
        getClass().getMethod("getNamenodePort"), getClass().getMethod("setNamenodePort", String.class)));
      items.add(new ArgumentWrapper(JOBTRACKER_HOST, BaseMessages.getString(getClass(), "JobtrackerHost.Label"), false, this,
        getClass().getMethod("getJobtrackerHost"), getClass().getMethod("setJobtrackerHost", String.class)));
      items.add(new ArgumentWrapper(JOBTRACKER_PORT, BaseMessages.getString(getClass(), "JobtrackerPort.Label"), false, this,
        getClass().getMethod("getJobtrackerPort"), getClass().getMethod("setJobtrackerPort", String.class)));
      items.add(new ArgumentWrapper(BLOCKING_EXECUTION, BaseMessages.getString(getClass(), "BlockingExecution.Label"), false, this,
        getClass().getMethod("getBlockingExecution"), getClass().getMethod("setBlockingExecution", String.class)));
      items.add(new ArgumentWrapper(BLOCKING_POLLING_INTERVAL, BaseMessages.getString(getClass(), "BlockingPollingInterval.Label"), false, this,
        getClass().getMethod("getBlockingPollingInterval"), getClass().getMethod("setBlockingPollingInterval", String.class)));
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }

    return items;
  }

  @Override
  public SqoopConfig clone() {
    return (SqoopConfig) super.clone();
  }

  /**
   * Silently set the following properties: {@code database, connect, username, password}.
   *
   * @param database Database name
   * @param connect Connection string (JDBC connection URL)
   * @param username Username
   * @param password Password
   */
  public void setConnectionInfo(String database, String connect, String username, String password) {
    this.database = database;
    this.connect = connect;
    this.username = username;
    this.password = password;
  }

  /**
   * Copy connection information from temporary "advanced" fields into annotated argument fields.
   */
  public void copyConnectionInfoFromAdvanced() {
    database = null;
    connect = getConnectFromAdvanced();
    username = getUsernameFromAdvanced();
    password = getPasswordFromAdvanced();
  }

  /**
   * Copy the current connection information into the "advanced" fields. These are temporary session properties
   * used to aid the user during configuration via UI.
   */
  public void copyConnectionInfoToAdvanced() {
    setConnectFromAdvanced(getConnect());
    setUsernameFromAdvanced(getUsername());
    setPasswordFromAdvanced(getPassword());
  }

  // All getters/setters below this line

  public String getNamenodeHost() {
    return namenodeHost;
  }

  public void setNamenodeHost(String namenodeHost) {
    String old = this.namenodeHost;
    this.namenodeHost = namenodeHost;
    pcs.firePropertyChange(NAMENODE_HOST, old, this.namenodeHost);
  }

  public String getNamenodePort() {
    return namenodePort;
  }

  public void setNamenodePort(String namenodePort) {
    String old = this.namenodePort;
    this.namenodePort = namenodePort;
    pcs.firePropertyChange(NAMENODE_PORT, old, this.namenodePort);
  }

  public String getJobtrackerHost() {
    return jobtrackerHost;
  }

  public void setJobtrackerHost(String jobtrackerHost) {
    String old = this.jobtrackerHost;
    this.jobtrackerHost = jobtrackerHost;
    pcs.firePropertyChange(JOBTRACKER_HOST, old, this.jobtrackerHost);
  }

  public String getJobtrackerPort() {
    return jobtrackerPort;
  }

  public void setJobtrackerPort(String jobtrackerPort) {
    String old = this.jobtrackerPort;
    this.jobtrackerPort = jobtrackerPort;
    pcs.firePropertyChange(JOBTRACKER_PORT, old, this.jobtrackerPort);
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    String old = this.database;
    this.database = database;
    pcs.firePropertyChange(DATABASE, old, this.database);
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    String old = this.schema;
    this.schema = schema;
    pcs.firePropertyChange(SCHEMA, old, this.schema);
  }

  public String getConnect() {
    return connect;
  }

  public void setConnect(String connect) {
    String old = this.connect;
    this.connect = connect;
    pcs.firePropertyChange(CONNECT, old, this.connect);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    String old = this.username;
    this.username = username;
    pcs.firePropertyChange(USERNAME, old, this.username);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    String old = this.password;
    this.password = password;
    pcs.firePropertyChange(PASSWORD, old, this.password);
  }

  public String getConnectFromAdvanced() {
    return connectFromAdvanced;
  }

  public void setConnectFromAdvanced(String connectFromAdvanced) {
    this.connectFromAdvanced = connectFromAdvanced;
  }

  public String getUsernameFromAdvanced() {
    return usernameFromAdvanced;
  }

  public void setUsernameFromAdvanced(String usernameFromAdvanced) {
    this.usernameFromAdvanced = usernameFromAdvanced;
  }

  public String getPasswordFromAdvanced() {
    return passwordFromAdvanced;
  }

  public void setPasswordFromAdvanced(String passwordFromAdvanced) {
    this.passwordFromAdvanced = passwordFromAdvanced;
  }

  public String getConnectionManager() {
    return connectionManager;
  }

  public void setConnectionManager(String connectionManager) {
    String old = this.connectionManager;
    this.connectionManager = connectionManager;
    pcs.firePropertyChange(CONNECTION_MANAGER, old, this.connectionManager);
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    String old = this.driver;
    this.driver = driver;
    pcs.firePropertyChange(DRIVER, old, this.driver);
  }

  public String getVerbose() {
    return verbose;
  }

  public void setVerbose(String verbose) {
    String old = this.verbose;
    this.verbose = verbose;
    pcs.firePropertyChange(VERBOSE, old, this.verbose);
  }

  public String getConnectionParamFile() {
    return connectionParamFile;
  }

  public void setConnectionParamFile(String connectionParamFile) {
    String old = this.connectionParamFile;
    this.connectionParamFile = connectionParamFile;
    pcs.firePropertyChange(CONNECTION_PARAM_FILE, old, this.connectionParamFile);
  }

  public String getHadoopHome() {
    return hadoopHome;
  }

  public void setHadoopHome(String hadoopHome) {
    String old = this.hadoopHome;
    this.hadoopHome = hadoopHome;
    pcs.firePropertyChange(HADOOP_HOME, old, this.hadoopHome);
  }

  public String getEnclosedBy() {
    return enclosedBy;
  }

  public void setEnclosedBy(String enclosedBy) {
    String old = this.enclosedBy;
    this.enclosedBy = enclosedBy;
    pcs.firePropertyChange(ENCLOSED_BY, old, this.enclosedBy);
  }

  public String getEscapedBy() {
    return escapedBy;
  }

  public void setEscapedBy(String escapedBy) {
    String old = this.escapedBy;
    this.escapedBy = escapedBy;
    pcs.firePropertyChange(ESCAPED_BY, old, this.escapedBy);
  }

  public String getFieldsTerminatedBy() {
    return fieldsTerminatedBy;
  }

  public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
    String old = this.fieldsTerminatedBy;
    this.fieldsTerminatedBy = fieldsTerminatedBy;
    pcs.firePropertyChange(FIELDS_TERMINATED_BY, old, this.fieldsTerminatedBy);
  }

  public String getLinesTerminatedBy() {
    return linesTerminatedBy;
  }

  public void setLinesTerminatedBy(String linesTerminatedBy) {
    String old = this.linesTerminatedBy;
    this.linesTerminatedBy = linesTerminatedBy;
    pcs.firePropertyChange(LINES_TERMINATED_BY, old, this.linesTerminatedBy);
  }

  public String getOptionallyEnclosedBy() {
    return optionallyEnclosedBy;
  }

  public void setOptionallyEnclosedBy(String optionallyEnclosedBy) {
    String old = this.optionallyEnclosedBy;
    this.optionallyEnclosedBy = optionallyEnclosedBy;
    pcs.firePropertyChange(OPTIONALLY_ENCLOSED_BY, old, this.optionallyEnclosedBy);
  }

  public String getMysqlDelimiters() {
    return mysqlDelimiters;
  }

  public void setMysqlDelimiters(String mysqlDelimiters) {
    String old = this.mysqlDelimiters;
    this.mysqlDelimiters = mysqlDelimiters;
    pcs.firePropertyChange(MYSQL_DELIMITERS, old, this.mysqlDelimiters);
  }

  public String getInputEnclosedBy() {
    return inputEnclosedBy;
  }

  public void setInputEnclosedBy(String inputEnclosedBy) {
    String old = this.inputEnclosedBy;
    this.inputEnclosedBy = inputEnclosedBy;
    pcs.firePropertyChange(INPUT_ENCLOSED_BY, old, this.inputEnclosedBy);
  }

  public String getInputEscapedBy() {
    return inputEscapedBy;
  }

  public void setInputEscapedBy(String inputEscapedBy) {
    String old = this.inputEscapedBy;
    this.inputEscapedBy = inputEscapedBy;
    pcs.firePropertyChange(INPUT_ESCAPED_BY, old, this.inputEscapedBy);
  }

  public String getInputFieldsTerminatedBy() {
    return inputFieldsTerminatedBy;
  }

  public void setInputFieldsTerminatedBy(String inputFieldsTerminatedBy) {
    String old = this.inputFieldsTerminatedBy;
    this.inputFieldsTerminatedBy = inputFieldsTerminatedBy;
    pcs.firePropertyChange(INPUT_FIELDS_TERMINATED_BY, old, this.inputFieldsTerminatedBy);
  }

  public String getInputLinesTerminatedBy() {
    return inputLinesTerminatedBy;
  }

  public void setInputLinesTerminatedBy(String inputLinesTerminatedBy) {
    String old = this.inputLinesTerminatedBy;
    this.inputLinesTerminatedBy = inputLinesTerminatedBy;
    pcs.firePropertyChange(INPUT_LINES_TERMINATED_BY, old, this.inputLinesTerminatedBy);
  }

  public String getInputOptionallyEnclosedBy() {
    return inputOptionallyEnclosedBy;
  }

  public void setInputOptionallyEnclosedBy(String inputOptionallyEnclosedBy) {
    String old = this.inputOptionallyEnclosedBy;
    this.inputOptionallyEnclosedBy = inputOptionallyEnclosedBy;
    pcs.firePropertyChange(INPUT_OPTIONALLY_ENCLOSED_BY, old, this.inputOptionallyEnclosedBy);
  }

  public String getBinDir() {
    return binDir;
  }

  public void setBinDir(String binDir) {
    String old = this.binDir;
    this.binDir = binDir;
    pcs.firePropertyChange(BIN_DIR, old, this.binDir);
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    String old = this.className;
    this.className = className;
    pcs.firePropertyChange(CLASS_NAME, old, this.className);
  }

  public String getJarFile() {
    return jarFile;
  }

  public void setJarFile(String jarFile) {
    String old = this.jarFile;
    this.jarFile = jarFile;
    pcs.firePropertyChange(JAR_FILE, old, this.jarFile);
  }

  public String getOutdir() {
    return outdir;
  }

  public void setOutdir(String outdir) {
    String old = this.outdir;
    this.outdir = outdir;
    pcs.firePropertyChange(OUTDIR, old, this.outdir);
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    String old = this.packageName;
    this.packageName = packageName;
    pcs.firePropertyChange(PACKAGE_NAME, old, this.packageName);
  }

  public String getMapColumnJava() {
    return mapColumnJava;
  }

  public void setMapColumnJava(String mapColumnJava) {
    String old = this.mapColumnJava;
    this.mapColumnJava = mapColumnJava;
    pcs.firePropertyChange(MAP_COLUMN_JAVA, old, this.mapColumnJava);
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    String old = this.table;
    this.table = table;
    pcs.firePropertyChange(TABLE, old, this.table);
  }

  public String getNumMappers() {
    return numMappers;
  }

  public void setNumMappers(String numMappers) {
    String old = this.numMappers;
    this.numMappers = numMappers;
    pcs.firePropertyChange(NUM_MAPPERS, old, this.numMappers);
  }

  public String getCommandLine() {
    return commandLine;
  }

  public void setCommandLine(String commandLine) {
    String old = this.commandLine;
    this.commandLine = commandLine;
    pcs.firePropertyChange(COMMAND_LINE, old, this.commandLine);
  }

  public String getMode() {
    return mode;
  }

  public Mode getModeAsEnum() {
    try {
      return Mode.valueOf(getMode());
    } catch (Exception ex) {
      // Not a valid ui mode, return the default
      return Mode.QUICK_SETUP;
    }
  }

  /**
   * Sets the mode based on the enum value
   * @param mode
   */
  public void setMode(Mode mode) {
    setMode(mode.name());
  }

  public void setMode(String mode) {
    String old = this.mode;
    this.mode = mode;
    pcs.firePropertyChange(MODE, old, this.mode);
  }
}
