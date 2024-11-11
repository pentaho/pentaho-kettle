/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
