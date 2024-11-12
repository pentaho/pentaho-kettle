/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
