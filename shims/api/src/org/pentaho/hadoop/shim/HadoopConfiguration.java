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

package org.pentaho.hadoop.shim;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.shim.spi.HadoopShim;
import org.pentaho.hadoop.shim.spi.PentahoHadoopShim;
import org.pentaho.hadoop.shim.spi.PigShim;
import org.pentaho.hadoop.shim.spi.SnappyShim;
import org.pentaho.hadoop.shim.spi.SqoopShim;
import org.pentaho.hbase.shim.spi.HBaseShim;

/**
 * A collection of Hadoop shim implementations for interactive with a Hadoop
 * cluster.
 */
public class HadoopConfiguration {
  private static final Class<?> PKG = HadoopConfiguration.class;

  private String identifier;

  private String name;
  
  /**
   * Root directory for this configuration
   */
  private FileObject location;
  
  private HadoopShim hadoopShim;
  
  private List<PentahoHadoopShim> availableShims;

  /**
   * Create a new Hadoop configuration with the provided shims. Only
   * 
   * @param location Location where this configuration resides
   * @param identifier Unique identifier for this configuration
   * @param name Friendly name for this configuration
   * @param hadoopShim Hadoop shim
   * @param shims Available shims for this Hadoop configuration
   * @throws NullPointerException when {@code identifier}, {@code name}.
   * @throws NullPointerException when {@code identifier}, {@code name}, or {@code hadoopShim} are {@code null}
   */
  public HadoopConfiguration(FileObject location, String identifier, String name, HadoopShim hadoopShim, PentahoHadoopShim... shims) {
    if (location == null || identifier == null || name == null || hadoopShim == null) {
      throw new NullPointerException();
    }
    this.location = location;
    this.identifier = identifier;
    this.name = name;
    this.hadoopShim = hadoopShim;
    
    // Register all provided shims
    availableShims = new ArrayList<PentahoHadoopShim>();
    // Add the hadoop shim to the list so we don't have to handle it special in getShim()
    availableShims.add(hadoopShim);
    for (PentahoHadoopShim shim : shims) {
      if (shim == null) {
        // Skip null shims
        continue;
      }
      availableShims.add(shim);
    }
  }

  /**
   * @return the location (root directory) of this Hadoop configuration
   */
  public FileObject getLocation() {
    return location;
  }
  
  /**
   * @return this configuration's identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * @return the friendly name for this configuration
   */
  public String getName() {
    return name;
  }

  /**
   * @return the Hadoop shim for this configuration
   */
  public HadoopShim getHadoopShim() {
    return hadoopShim;
  }

  /**
   * Retrieve the Sqoop shim for this configuration if it's available
   * 
   * @return the Sqoop shim
   * @throws ConfigurationException No Sqoop shim available for this
   *           configuration
   */
  public SqoopShim getSqoopShim() throws ConfigurationException {
    return getShim(SqoopShim.class);
  }

  /**
   * Retrieve the Pig shim for this configuration if it's available
   * 
   * @return the Pig shim
   * @throws ConfigurationException No Pig shim available for this configuration
   */
  public PigShim getPigShim() throws ConfigurationException {
    return getShim(PigShim.class);
  }

  /**
   * Retrieve the Snappy shim for this configuration if it's available
   * 
   * @return the Snappy shim
   * @throws ConfigurationException No Snappy shim available for this
   *           configuration
   */
  public SnappyShim getSnappyShim() throws ConfigurationException {
    return getShim(SnappyShim.class);
  }

  /**
   * Retrieve the first registered shim that matches the shim type provided.
   * 
   * @param shimType The type of {@code PentahoHadoopShim} to get from this configuration.
   * @return A shim that matches the type
   * @throws ConfigurationException This configuration does not contain a shim that matches the type requested
   */
  public <T extends PentahoHadoopShim> T getShim(Class<T> shimType) throws ConfigurationException {
    for (PentahoHadoopShim shim : availableShims) {
      if (shimType.isAssignableFrom(shim.getClass())) {
        @SuppressWarnings("unchecked")
        T t = (T) shim;
        return t;
      }
    }
    throw new ConfigurationException(BaseMessages.getString(PKG, "Error.UnsupportedShim", getName(), shimType.getSimpleName()));
  }

  /**
   * Retrieve the HBase shim for this configuration if it's available
   * 
   * @return the HBase shim
   * @throws ConfigurationException No HBase shim available for this
   *           configuration
   */
  public HBaseShim getHBaseShim() throws ConfigurationException {
    return getShim(HBaseShim.class);
  }

  /**
   * The identifier for this configuration
   */
  @Override
  public String toString() {
    return getIdentifier();
  }
}
