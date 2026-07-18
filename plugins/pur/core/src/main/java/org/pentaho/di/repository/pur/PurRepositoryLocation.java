/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.repository.pur;

public class PurRepositoryLocation implements java.io.Serializable {

  private static final long serialVersionUID = 2380968812271105007L; /* EESOURCE: UPDATE SERIALVERUID */
  private String url;

  public PurRepositoryLocation( String url ) {
    this.url = url;
  }

  /**
   * 
   * @return URL or <b>null</b>
   */
  public String getUrl() {
    return url;
  }

  public void setUrl( String url ) {
    this.url = url;
  }
}
