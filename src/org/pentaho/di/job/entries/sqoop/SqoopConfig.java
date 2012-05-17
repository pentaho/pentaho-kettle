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
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.util.AbstractModelList;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * A collection of configuration objects for a Sqoop job entry.
 */
public abstract class SqoopConfig implements XulEventSource, Cloneable {
  protected transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  protected static final String ARG_PREFIX = "--";

  private String jobEntryName;
  private String blockingPollingInterval = String.valueOf(300);
  private Boolean blockingExecution = Boolean.TRUE;
  private String namenodeHost;
  private String namenodePort;
  private String jobtrackerHost;
  private String jobtrackerPort;

  private LinkedHashSet<Argument> arguments;

  public SqoopConfig() {
    this(null);
  }

  /**
   * Copy constructor. Creates a new configuration object based off the configuration settings from {@code config}.
   *
   * @param config Configuration object to copy properties from
   */
  public SqoopConfig(SqoopConfig config) {
    initArguments();
  }

  protected void initArguments() {
    arguments = new LinkedHashSet<Argument>();

    // Common arguments
    addArgument(new Argument("connect"));
    addArgument(new Argument("connection-manager"));
    addArgument(new Argument("driver"));
    addArgument(new Argument("username"));
    addArgument(new Argument("password"));
    addArgument(new Argument("verbose", true));
    addArgument(new Argument("connection-param-file"));
    addArgument(new Argument("hadoop-home"));

    // Output line formatting arguments
    addArgument(new Argument("enclosed-by"));
    addArgument(new Argument("escaped-by"));
    addArgument(new Argument("fields-terminated-by"));
    addArgument(new Argument("lines-terminated-by"));
    addArgument(new Argument("optionally-enclosed-by"));
    addArgument(new Argument("mysql-delimiters", true));

    // Input parsing arguments
    addArgument(new Argument("input-enclosed-by"));
    addArgument(new Argument("input-escaped-by"));
    addArgument(new Argument("input-fields-terminated-by"));
    addArgument(new Argument("input-lines-terminated-by"));
    addArgument(new Argument("input-optionally-enclosed-by"));

    // Code generation arguments
    addArgument(new Argument("bindir"));
    addArgument(new Argument("class-name"));
    addArgument(new Argument("jar-file"));
    addArgument(new Argument("outdir"));
    addArgument(new Argument("package-name"));
    addArgument(new Argument("map-column-java"));
  }

  protected void copyArguments(SqoopConfig config) {
    // Set all known arguments from the ones in config
    Map<String, Argument> lookup = new HashMap<String, Argument>();
    for (Argument ai : config.getArguments()) {
      lookup.put(ai.getName(), ai);
    }
    for (Argument ai : getArguments()) {
      Argument loadedAi = lookup.get(ai.getName());
      if (loadedAi != null && loadedAi.getValue() != null) {
        ai.setValue(loadedAi.getValue());
      }
    }
  }

  /**
   * @see {@link PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)}
   */
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  /**
   * @see {@link PropertyChangeSupport#addPropertyChangeListener(String, java.beans.PropertyChangeListener)}
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
    pcs.addPropertyChangeListener(propertyName, l);
  }

  /**
   * @see {@link PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)}
   */
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  /**
   * @see {@link PropertyChangeSupport#removePropertyChangeListener(String, java.beans.PropertyChangeListener)}
   */
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
    pcs.removePropertyChangeListener(propertyName, l);
  }

  /**
   * Add an argument to this configuration.
   *
   * @param ai Argument to register with this configuration so that it can be configured.
   */
  protected boolean addArgument(Argument ai) {
    return arguments.add(ai);
  }

  /**
   * @return all known arguments for this config object. Some arguments may be synthetic and represent properties
   *         directly set on this config object for the purpose of showing them in the list view of the UI.
   */
  public AbstractModelList<Argument> getArguments() {
    final AbstractModelList<Argument> items = new AbstractModelList<Argument>();

    items.addAll(arguments);

    try {
      items.add(new StringPropertyArgument("namenodeHost", BaseMessages.getString(getClass(), "NamenodeHost.Label"), this,
        getClass().getMethod("setNamenodeHost", String.class), getClass().getMethod("getNamenodeHost")));
      items.add(new StringPropertyArgument("namenodePort", BaseMessages.getString(getClass(), "NamenodePort.Label"), this,
        getClass().getMethod("setNamenodePort", String.class), getClass().getMethod("getNamenodePort")));
      items.add(new StringPropertyArgument("jobtrackerHost", BaseMessages.getString(getClass(), "JobtrackerHost.Label"), this,
        getClass().getMethod("setJobtrackerHost", String.class), getClass().getMethod("getJobtrackerHost")));
      items.add(new StringPropertyArgument("jobtrackerPort", BaseMessages.getString(getClass(), "JobtrackerPort.Label"), this,
        getClass().getMethod("setJobtrackerPort", String.class), getClass().getMethod("getJobtrackerPort")));
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }

    return items;
  }

  /**
   * @return All the command line arguments for this configuration object
   */
  public List<String> getCommandLineArgs() {
    List<String> args = new ArrayList<String>();

    appendArguments(args, arguments);

    return args;
  }

  /**
   * Add all {@link Argument}s to a list of arguments
   *
   * @param args          Arguments to append to
   * @param arguments Arguments to append
   * @return
   */
  protected void appendArguments(List<String> args, Set<? extends Argument> arguments) {
    for (Argument ai : arguments) {
      ai.appendTo(args, ARG_PREFIX);
    }
  }

  public String getJobEntryName() {
    return jobEntryName;
  }

  public void setJobEntryName(String jobEntryName) {
    String old = this.jobEntryName;
    this.jobEntryName = jobEntryName;
    pcs.firePropertyChange("jobEntryName", old, this.jobEntryName);
  }

  public String getBlockingPollingInterval() {
    return blockingPollingInterval;
  }

  public void setBlockingPollingInterval(String blockingPollingInterval) {
    String old = this.blockingPollingInterval;
    this.blockingPollingInterval = blockingPollingInterval;
    pcs.firePropertyChange("blockingPollingInterval", old, this.blockingPollingInterval);
  }

  public Boolean isBlockingExecution() {
    return blockingExecution;
  }

  public void setBlockingExecution(Boolean blockingExecution) {
    Boolean old = this.blockingExecution;
    this.blockingExecution = blockingExecution;
    pcs.firePropertyChange("blockingExecution", old, this.blockingExecution);
  }

  public String getNamenodeHost() {
    return namenodeHost;
  }

  public void setNamenodeHost(String namenodeHost) {
    String old = this.namenodeHost;
    this.namenodeHost = namenodeHost;
    pcs.firePropertyChange("namenodeHost", old, this.namenodeHost);
  }

  public String getNamenodePort() {
    return namenodePort;
  }

  public void setNamenodePort(String namenodePort) {
    String old = this.namenodePort;
    this.namenodePort = namenodePort;
    pcs.firePropertyChange("namenodePort", old, this.namenodePort);
  }

  public String getJobtrackerHost() {
    return jobtrackerHost;
  }

  public void setJobtrackerHost(String jobtrackerHost) {
    String old = this.jobtrackerHost;
    this.jobtrackerHost = jobtrackerHost;
    pcs.firePropertyChange("jobtrackerHost", old, this.jobtrackerHost);
  }

  public String getJobtrackerPort() {
    return jobtrackerPort;
  }

  public void setJobtrackerPort(String jobtrackerPort) {
    String old = this.jobtrackerPort;
    this.jobtrackerPort = jobtrackerPort;
    pcs.firePropertyChange("jobtrackerPort", old, this.jobtrackerPort);
  }

  /**
   * Find an argument in this config by name.
   *
   * @param argumentName Argument name to find
   * @return {@link Argument} whose name is {@code name}; {@code null} if no argument exists
   */
  public Argument getArgument(String argumentName) {
    for (Argument ai : getArguments()) {
      if (argumentName.equals(ai.getName())) {
        return ai;
      }
    }
    return null;
  }

  /**
   * Sets an argument value and fires a property change event for that argument.
   *
   * @param argumentName Name of the argument to set
   * @param value        Value to set it to
   */
  public void setArgumentValue(String argumentName, String value) {
    Argument ai = getArgument(argumentName);
    if (ai != null) {
      String old = ai.getValue();
      ai.setValue(value);
      pcs.firePropertyChange(argumentName, old, ai.getValue());
    }
  }

  @Override
  public SqoopConfig clone() {
    try {
      SqoopConfig config = (SqoopConfig) super.clone();
      // Reinitialize arguments so they are not shared
      config.initArguments();
      // Copy the arguments
      config.copyArguments(this);
      return config;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
