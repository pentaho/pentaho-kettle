/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.resource;

import org.pentaho.di.core.EngineMetaInterface;

public class ResourceDefinition {
  private String filename;
  private EngineMetaInterface meta;
  private String origin;
  private boolean exportRequired;

  /**
   * @param filename
   * @param meta
   */
  public ResourceDefinition( String filename, EngineMetaInterface meta ) {
    super();
    this.filename = filename;
    this.meta = meta;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * @param meta
   *          the meta to set
   */
  public void setMeta( EngineMetaInterface meta ) {
    this.meta = meta;
  }

  /**
   * @return the meta
   */
  public EngineMetaInterface getMeta() {
    return meta;
  }

  /**
   * @return the origin of the resource as entered by the user. (optional)
   */
  public String getOrigin() {
    return origin;
  }

  /**
   * @param origin
   *          the origin of the resource as entered by the user. (optional)
   */
  public void setOrigin( String origin ) {
    this.origin = origin;
  }
  
  
  /**
   * @param exportRequired
   */
  public void setExportRequired( boolean exportRequired ) {
    this.exportRequired = exportRequired;
  }
  
  /**
   * @return
   */
  public boolean isExportRequired() {
    return exportRequired;
  }
}
