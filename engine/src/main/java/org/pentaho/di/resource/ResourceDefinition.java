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

public class ResourceDefinition {
  private String filename;
  private String content;

  private String origin;

  /**
   * @param filename
   * @param content
   */
  public ResourceDefinition( String filename, String content ) {
    super();
    this.filename = filename;
    this.content = content;
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
   * @return the content
   */
  public String getContent() {
    return content;
  }

  /**
   * @param content
   *          the content to set
   */
  public void setContent( String content ) {
    this.content = content;
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
}
