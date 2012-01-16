/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.di.cluster;

import org.pentaho.di.www.Carte;
import org.pentaho.di.www.SlaveServerConfig;

public class CarteLauncher implements Runnable {
  private SlaveServerConfig config;
  private Carte             carte;
  private Exception         exception;
  private boolean           failure;

  public CarteLauncher() {
    this.carte = null;
  }
  
  public CarteLauncher(String hostname, int port) {
    this();
    this.config = new SlaveServerConfig(hostname, port, false);
  }
  
  public CarteLauncher(SlaveServerConfig config) {
    this();
    this.config = config;
  }

  public void run() {
    try {
      carte = new Carte(config);
    } catch (Exception e) {
      this.exception = e;
      failure = true;
    }
  }

  /**
   * @return the carte
   */
  public Carte getCarte() {
    return carte;
  }

  /**
   * @param carte
   *          the carte to set
   */
  public void setCarte(Carte carte) {
    this.carte = carte;
  }

  /**
   * @return the exception
   */
  public Exception getException() {
    return exception;
  }

  /**
   * @param exception
   *          the exception to set
   */
  public void setException(Exception exception) {
    this.exception = exception;
  }

  /**
   * @return the failure
   */
  public boolean isFailure() {
    return failure;
  }

  /**
   * @param failure
   *          the failure to set
   */
  public void setFailure(boolean failure) {
    this.failure = failure;
  }

  /**
   * @return the config
   */
  public SlaveServerConfig getConfig() {
    return config;
  }

  /**
   * @param config the config to set
   */
  public void setConfig(SlaveServerConfig config) {
    this.config = config;
  }
}
