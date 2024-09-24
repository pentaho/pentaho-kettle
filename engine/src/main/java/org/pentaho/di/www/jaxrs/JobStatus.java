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

package org.pentaho.di.www.jaxrs;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobStatus {

  private String id;
  private String name;
  private String status;

  public JobStatus() {
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus( String status ) {
    this.status = status;
  }

}
