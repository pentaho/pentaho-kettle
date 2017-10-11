/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

/**
 * This describes the top level resource after an export
 *
 * @author matt
 *
 */
public class TopLevelResource {
  private String baseResourceName;
  private String archiveName;
  private String resourceName;

  /**
   * @param baseResourceName
   * @param archiveName
   * @param resourceName
   */
  public TopLevelResource( String baseResourceName, String archiveName, String resourceName ) {
    this.baseResourceName = baseResourceName;
    this.archiveName = archiveName;
    this.resourceName = resourceName;
  }

  /**
   * @return the baseResourceName
   */
  public String getBaseResourceName() {
    return baseResourceName;
  }

  /**
   * @param baseResourceName
   *          the baseResourceName to set
   */
  public void setBaseResourceName( String baseResourceName ) {
    this.baseResourceName = baseResourceName;
  }

  /**
   * @return the archiveName
   */
  public String getArchiveName() {
    return archiveName;
  }

  /**
   * @param archiveName
   *          the archiveName to set
   */
  public void setArchiveName( String archiveName ) {
    this.archiveName = archiveName;
  }

  /**
   * @return the resourceName
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * @param resourceName
   *          the resourceName to set
   */
  public void setResourceName( String resourceName ) {
    this.resourceName = resourceName;
  }

}
