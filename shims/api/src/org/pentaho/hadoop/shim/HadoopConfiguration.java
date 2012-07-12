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

import org.pentaho.hadoop.shim.spi.HadoopShim;
import org.pentaho.hadoop.shim.spi.PigShim;
import org.pentaho.hadoop.shim.spi.SnappyShim;
import org.pentaho.hadoop.shim.spi.SqoopShim;

/**
 * A collection of Hadoop shim implementations for interactive with a Hadoop cluster.
 */
public class HadoopConfiguration {
  private String identifier;

  private String name;

  private HadoopShim hadoopShim;

  private SqoopShim sqoopShim;

  private PigShim pigShim;

  private SnappyShim snappyShim;

  /**
   * Create a new Hadoop configuration with the provided shims. Only 
   * @param identifier Unique identifier for this configuration
   * @param name Friendly name for this configuration
   * @param hadoopShim Hadoop shim
   * @param sqoopShim Sqoop shim (optional)
   * @param pigShim Pig shim (optional)
   * @param snappyShim Snappy shim (optional)
   * @throws NullPointerException when {@code identifier}, {@code name}, or {@code hadoopShim} are {@code null}.
   */
  public HadoopConfiguration(String identifier, String name, HadoopShim hadoopShim, SqoopShim sqoopShim,
      PigShim pigShim, SnappyShim snappyShim) {
    if (identifier == null || name == null || hadoopShim == null) {
      throw new NullPointerException();
    }
    this.identifier = identifier;
    this.name = name;
    this.hadoopShim = hadoopShim;
    this.sqoopShim = sqoopShim;
    this.pigShim = pigShim;
    this.snappyShim = snappyShim;
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
   * @return the Sqoop shim
   * @throws ConfigurationException No Sqoop shim available for this configuration
   */
  public SqoopShim getSqoopShim() throws ConfigurationException {
    if (sqoopShim == null) {
      throw new ConfigurationException("Sqoop not supported");
    }
    return sqoopShim;
  }

  /**
   * Retrieve the Pig shim for this configuration if it's available
   * @return the Pig shim
   * @throws ConfigurationException No Pig shim available for this configuration
   */
  public PigShim getPigShim() throws ConfigurationException {
    if (pigShim == null) {
      throw new ConfigurationException("Pig not supported");
    }
    return pigShim;
  }

  /**
   * Retrieve the Snappy shim for this configuration if it's available
   * @return the Snappy shim
   * @throws ConfigurationException No Snappy shim available for this configuration
   */
  public SnappyShim getSnappyShim() throws ConfigurationException {
    if (snappyShim == null) {
      throw new ConfigurationException("Snappy not supported");
    }
    return snappyShim;
  }

  /**
   * The identifier for this configuration
   */
  @Override
  public String toString() {
    return getIdentifier();
  }
}
